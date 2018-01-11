package com.suvankarmitra.fullscreentextdemo3;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

public class MainActivity extends AppCompatActivity {
    Pagination mPagination = null;
    CharSequence mText;
    RecyclerView mRecyclerView;
    ViewAdapter viewAdapter;
    List<Story> stories = new ArrayList<>();
    private int mCurrentIndex = 0;
    final String TAG = "BAAL";
    private ProgressBar progressBar;
    private boolean statusBarShown = false;
    private boolean recyclerScaleDown = false;
    private SeekBar mSeekBar;
    private boolean controlPanelShown = false;
    //ImageButton chapters;
    private RelativeLayout topPanelLayout;
    private TextView seekPosition;
    private LinearLayout bottomPanelLayout;
    private ImageButton refresh;
    private ImageButton playAudio;

    final List<String> lines = new ArrayList<>();

    private float scaleFactor = 0.75f;
    private int animDuration = 200;
    Animation scaleDown = new ScaleAnimation(
            1f, scaleFactor, // Start and end values for the X axis scaling
            1f, scaleFactor, // Start and end values for the Y axis scaling
            Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
            Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
    Animation scaleUp = new ScaleAnimation(
            scaleFactor, 1f, // Start and end values for the X axis scaling
            scaleFactor, 1f, // Start and end values for the Y axis scaling
            Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
            Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
    Animation slideUpFromBottom;
    Animation slideDownToBottom;
    Animation slideUpToTop;
    Animation slideDownFromTop;

    private boolean refreshFile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hideStatusBar();

        Animation wiggle = AnimationUtils.loadAnimation(this, R.anim.wiggle_anim);
        playAudio = (ImageButton) findViewById(R.id.play);
        playAudio.startAnimation(wiggle);

        playAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!controlPanelShown) {
                    return; //do nothing if the control panel is not shown.
                }
                FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction().addToBackStack("MainActivity")
                        .setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_right,
                                R.anim.enter_from_right,R.anim.exit_to_right)
                        .add(android.R.id.content,new AudioBookFragment())
                        .commit();
            }
        });

        //chapters = (ImageButton) findViewById(R.id.chapters);
        topPanelLayout = (RelativeLayout) findViewById(R.id.topPanel);
        seekPosition = (TextView) findViewById(R.id.seekBarPageIndicator);
        bottomPanelLayout = (LinearLayout) findViewById(R.id.bottomPanel);

        refresh = (ImageButton) findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!controlPanelShown) {
                    return; //if controls are not shown then do nothing.
                }
                refreshFile = true;
                mPagination = null;
                mCurrentIndex = 0;
                pageNum = 0;
                mSeekBar.setProgress(0);
                Snackbar.make(view,"Refreshing from cloud!", Snackbar.LENGTH_SHORT).show();
                refreshStory();
            }
        });

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //if scroll is initiated by user
                if(b) {
                    mRecyclerView.scrollToPosition(i);
                    //saveLastRead(i);
                }
                if(i<=0){
                    seekPosition.setText(seekBar.getMax()+" Pages");
                } else {
                    seekPosition.setText(i+"|"+seekBar.getMax());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int i = seekBar.getProgress();
                mRecyclerView.scrollToPosition(i);
                mCurrentIndex = i;
                saveLastRead(i);
            }
        });

        progressBar = findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);
        mRecyclerView = findViewById(R.id.recycler_view);
        viewAdapter = new ViewAdapter(MainActivity.this,stories);
        mRecyclerView.setAdapter(viewAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL,false);
        mRecyclerView.setLayoutManager(layoutManager);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerView);
        
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == SCROLL_STATE_IDLE) {
                    LinearLayoutManager layoutManager = ((LinearLayoutManager)recyclerView.getLayoutManager());
                    int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                    mCurrentIndex = firstVisiblePosition;
                    Log.d(TAG, "onScrollStateChanged: "+firstVisiblePosition);
                    hideStatusBar();
                    saveLastRead();
                    mSeekBar.setProgress(mCurrentIndex);
                }
            }
        });

        refreshStory();

        LinearLayout previous = (LinearLayout) findViewById(R.id.previous);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentIndex > 0) {
                    mCurrentIndex--;
                    viewAdapter.notifyItemChanged(mCurrentIndex);
                    mRecyclerView.scrollToPosition(mCurrentIndex);
                    //update();
                    hideStatusBar();
                    saveLastRead();
                }
            }
        });

        LinearLayout next = (LinearLayout) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentIndex < mPagination.size()) {
                    mCurrentIndex++;
                    Log.d(TAG, "onClick: next="+mCurrentIndex);
                    viewAdapter.notifyItemChanged(mCurrentIndex);
                    mRecyclerView.scrollToPosition(mCurrentIndex);
                    //update();
                    hideStatusBar();
                    saveLastRead();
                }
            }
        });

        scaleDown.setFillAfter(true); // Needed to keep the result of the animation
        scaleDown.setDuration(animDuration);
        scaleUp.setFillAfter(true); // Needed to keep the result of the animation
        scaleUp.setDuration(animDuration);

        slideUpFromBottom = AnimationUtils.loadAnimation(this,R.anim.slide_up_from_bottom);
        slideUpFromBottom.setDuration(animDuration);
        slideUpFromBottom.setStartOffset(250);
        slideUpFromBottom.setFillAfter(true);
        slideDownToBottom = AnimationUtils.loadAnimation(this,R.anim.slide_down_to_bottom);
        slideDownToBottom.setDuration(animDuration);
        slideDownToBottom.setFillAfter(true);
        slideDownFromTop = AnimationUtils.loadAnimation(this, R.anim.slide_down_from_top);
        slideDownFromTop.setStartOffset(250);
        slideDownFromTop.setDuration(animDuration);
        slideDownFromTop.setFillAfter(true);
        slideUpToTop = AnimationUtils.loadAnimation(this, R.anim.slide_up_to_top);
        slideUpToTop.setDuration(animDuration);
        slideUpToTop.setFillAfter(true);

        bottomPanelLayout.startAnimation(slideDownToBottom);
        topPanelLayout.startAnimation(slideUpToTop);
    }

    public void toggleStatusBar() {
        if(statusBarShown) {
            hideStatusBar();
        } else {
            showStatusBar();
        }
    }

    public void toggleControlsVisibility() {
        if(controlPanelShown) {
            //mSeekBar.setVisibility(View.INVISIBLE);slideUpFromBottom.setDuration(100);
            bottomPanelLayout.startAnimation(slideDownToBottom);
            topPanelLayout.startAnimation(slideUpToTop);
            controlPanelShown = false;
            mSeekBar.setEnabled(false);
        } else {
            //mSeekBar.setVisibility(View.VISIBLE);
            mSeekBar.setEnabled(true);
            bottomPanelLayout.startAnimation(slideUpFromBottom);
            topPanelLayout.startAnimation(slideDownFromTop);
            controlPanelShown = true;
        }
        //mSeekBar.setProgress(mCurrentIndex);
    }

    public void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        statusBarShown = false;
    }

    private void showStatusBar() {
        View decorView = getWindow().getDecorView();
        // Show Status Bar.
        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        decorView.setSystemUiVisibility(uiOptions);
        statusBarShown = true;
    }

    public void toggleRecyclerViewScale() {
        Log.d(TAG, "toggleRecyclerViewScale: ");
        if (!recyclerScaleDown) {
            mRecyclerView.startAnimation(scaleDown);
            recyclerScaleDown = true;
        } else {
            mRecyclerView.startAnimation(scaleUp);
            recyclerScaleDown = false;
        }
    }

    private void createPages() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            sb.append(lines.get(i));
        }
        String book_content = sb.toString();

        Spanned htmlString;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            htmlString = Html.fromHtml(book_content,Html.FROM_HTML_OPTION_USE_CSS_COLORS);
        } else {
            htmlString = Html.fromHtml(book_content);
        }
        mText = TextUtils.concat(htmlString);

        final TextView tvContent = findViewById(R.id.demo_tv);
        FontManager manager = new FontManager(this);
        manager.setTypeface(tvContent,"jos");
        tvContent.setVisibility(View.INVISIBLE);
        tvContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Removing layout listener to avoid multiple calls
                tvContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                mPagination = new Pagination(mText,
                                tvContent.getWidth(),
                                tvContent.getHeight(),
                                tvContent.getPaint(),
                                tvContent.getLineSpacingMultiplier(),
                                tvContent.getLineSpacingExtra(),
                                tvContent.getIncludeFontPadding());
                updateAll();
            }
        });
    }

    private int pageNum = 0;

    private void updateAll() {
        for (int i =0 ;i<mPagination.size(); i++) {
            final CharSequence text = mPagination.get(i);

            Story story = new Story(text);
            if(stories.size()>0)
                story.setPageNum(++pageNum);
            story.setBookmarked(isBookmarked(pageNum));
            stories.add(story);
            viewAdapter.notifyDataSetChanged();
        }
        Log.d(TAG, "updateAll: total pages="+mPagination.size());
        mCurrentIndex = getLastReadPage();
        viewAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(mCurrentIndex);
        mSeekBar.setMax(mPagination.size()-1);
        mSeekBar.setProgress(mCurrentIndex);
    }

    private void refreshStory() {
        progressBar.setVisibility(View.VISIBLE);
        stories.clear();
        viewAdapter.notifyDataSetChanged();
        URL[] urls = new URL[13];
        try {
            //urls[0] = new URL("https://drive.google.com/uc?export=download&id=1f00xSIN4CETQn83vSMc2HLVx0DqgpoqM");
            urls[0] = new URL("https://drive.google.com/uc?export=download&id=1pXvWijW-H4J4nqNR_s9gitf4lc-7A4wK");
            urls[1] = new URL("https://drive.google.com/uc?export=download&id=1DvJs7egQ1EAcX03_uzDuuAgz6gEAZeY5");
            urls[2] = new URL("https://drive.google.com/uc?export=download&id=1WNTX4MK3qs1BiioxvWrT8BspGt5IfhxH");
            urls[3] = new URL("https://drive.google.com/uc?export=download&id=1iMfp_mQWYou64h4zeI8A4Iz7SuQH4kpd");
            urls[4] = new URL("https://drive.google.com/uc?export=download&id=1rE29b5DifTchFM5t7xBPNbLyBSDtNPyj");
            urls[5] = new URL("https://drive.google.com/uc?export=download&id=10OATd2xHUERahlXkG_Y-EDTCehcBmwBV");
            urls[6] = new URL("https://drive.google.com/uc?export=download&id=158ZFfsMAagWPsCWuAQQjcMogTi8S3fEN");
            urls[7] = new URL("https://drive.google.com/uc?export=download&id=1ztUatavGggIWudfVqtb7CbvxKis3vJs_");
            urls[8] = new URL("https://drive.google.com/uc?export=download&id=1usPw3GEmkZa1LcFBUUfdka7FnXuITIVf");
            urls[9] = new URL("https://drive.google.com/uc?export=download&id=1rThhanv4COOW4Mnpa-n5n1W34nuPDoWR");
            urls[10] = new URL("https://drive.google.com/uc?export=download&id=1J3SEdFzrAx7A8eDZzKvaRDVs5KKCdp8s");
            urls[11] = new URL("https://drive.google.com/uc?export=download&id=1cRXjZnXFrZeLyqXdLlmy2aRWmztoenTs");
            urls[12] = new URL("https://drive.google.com/uc?export=download&id=1dAQEkxgwkaMtNyrwE_rEkfJaXYJ8Q63T");
            //
            new DownloadFilesTask().execute(urls);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private class DownloadFilesTask extends AsyncTask<URL, Integer, Long> {
        protected Long doInBackground(URL... urls) {

            Long totalSize = 0l;
            lines.clear();
            String story = "alice_in_wonderland.txt";
            if(!refreshFile) {
                try {
                    if (openFileInput(story).available() > 0) {
                        Log.d(TAG, "doInBackground: Reading from file");

                        BufferedReader in = new BufferedReader(new InputStreamReader(openFileInput(story)));
                        String str;
                        while ((str = in.readLine()) != null) {
                            lines.add(str);
                            totalSize = totalSize + str.length();
                        }
                        in.close();
                        return totalSize;
                    }
                }  catch(IOException e){
                    e.printStackTrace();
                }
            }

            try(FileOutputStream outputStream = openFileOutput(story, Context.MODE_PRIVATE);) {
                for (URL url : urls) {
                    try {
                        //First open the connection
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(60000); // timing out in a minute

                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                        String str;
                        while ((str = in.readLine()) != null) {
                            lines.add(str);
                            outputStream.write((str + "\n").getBytes());
                            totalSize = totalSize + str.length();
                        }
                        in.close();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //file refreshed from cloud
                refreshFile = false;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return totalSize;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Long result) {
            Log.d(TAG, "onPostExecute: Calling createPages");
            createPages();
            progressBar.setVisibility(View.GONE);
            mSeekBar.setMax(stories.size());
        }
    }

    private void saveLastRead() {
        Log.d(TAG, "saveLastRead: "+mCurrentIndex);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("PAGE_NUMBER", mCurrentIndex);
        editor.apply();
    }

    private void saveLastRead(int progress) {
        Log.d(TAG, "saveLastRead: "+progress);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("PAGE_NUMBER", progress);
        editor.apply();
    }

    private int getLastReadPage() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        int defaultValue = 0;
        int index = sharedPref.getInt("PAGE_NUMBER", defaultValue);
        Log.d(TAG, "getLastReadPage: "+index);
        return index;
    }

    private boolean isBookmarked(int pageId) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        boolean defaultValue = false;
        return sharedPref.getBoolean("BOOKMARK"+pageId, defaultValue);
    }

    @Override
    protected void onDestroy() {
        saveLastRead();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        hideStatusBar();
        super.onResume();
    }

    public void finish(View view) {
        finish();
    }

    public void toggleBookmark(View view) {
        if(!controlPanelShown) {
            return; //if controls are not shown then do nothing.
        }
        stories.get(mCurrentIndex).setBookmarked(!stories.get(mCurrentIndex).isBookmarked());
        viewAdapter.notifyItemChanged(mCurrentIndex);
        if(stories.get(mCurrentIndex).isBookmarked()) {
            Snackbar.make(view,"Page "+mCurrentIndex+" bookmarked!",Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(view,"Page "+mCurrentIndex+" bookmark removed!",Snackbar.LENGTH_SHORT).show();
        }
        //saving the bookmark
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("BOOKMARK"+mCurrentIndex, stories.get(mCurrentIndex).isBookmarked());
        editor.apply();
    }
}
