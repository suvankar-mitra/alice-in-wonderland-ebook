package com.suvankarmitra.fullscreentextdemo3;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;


/**
 * A simple {@link Fragment} subclass.
 */
public class AudioBookFragment extends Fragment {

    public AudioBookFragment() {
        // Required empty public constructor
    }

    ImageView backgroundImageView;
    ImageView playerAlbumArt;
    ImageButton back;
    SeekBar seekBar;
    TextView title;
    Context context;
    MediaPlayer mediaPlayer;
    ImageButton playButton;
    ImageButton nextButton;
    ImageButton prevButton;
    TextView curTime, totTime;
    RelativeLayout progressBar;
    boolean isPlaying = false;
    boolean hasAudioFocus = true;
    private boolean initialStage = true;
    private boolean firstLoad = true;
    private Handler mHandler = new Handler();

    private boolean seekBarDragged = false;
    UrlProvider urlProvider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_audio_book, container, false);
        context = getContext();
        FontManager fm = new FontManager(context);

        playerAlbumArt = (ImageView) view.findViewById(R.id.player_album_art);
        backgroundImageView = (ImageView) view.findViewById(R.id.player_background);
//        Blurry.with(context)
//                .from(BitmapFactory.decodeResource(context.getResources(),
//                    R.drawable.alices_adventures_in_wonderland_book_cover))
//                .into(backgroundImageView);

        back = (ImageButton) view.findViewById(R.id.player_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack();
            }
        });

        seekBar = (SeekBar) view.findViewById(R.id.player_seekBar);
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    int sec = i / 1000;
                    int min = sec/60;
                    sec = sec - min*60;
                    String s = sec<10 ? "0"+sec : sec+"";
                    String m = min<10 ? "0"+min : min+"";
                    curTime.setText(m+":"+s);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarDragged = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int i = seekBar.getProgress();
                if(mediaPlayer!=null)
                    mediaPlayer.seekTo(i);
                seekBarDragged = false;
            }
        });
        title = (TextView) view.findViewById(R.id.player_album_title);
        fm.setTypeface(title,"jos");

        progressBar = (RelativeLayout) view.findViewById(R.id.player_progressbar);
        playButton = (ImageButton) view.findViewById(R.id.player_play_btn);
        mediaPlayer = new MediaPlayer();
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int result = am.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int i) {
                switch (i) {
                    case AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK :
                        mediaPlayer.setVolume(0.5f,0.5f);
                        break;
                    case AUDIOFOCUS_GAIN_TRANSIENT :
                    case AUDIOFOCUS_GAIN:
                        mediaPlayer.setVolume(1f,1f);
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS :
                        hasAudioFocus = false;
                        pauseMedia();
                        break;
                }
            }
        },AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Start playback.
            hasAudioFocus = true;
        } else {
            hasAudioFocus = false;
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if(initialStage) {
            String [] urls = {"https://drive.google.com/uc?export=download&id=1buBea8t9tBi6FfBRPLlOcq8KNjmiDWU-",
                    "https://drive.google.com/uc?export=download&id=1INc2eEv21oH6VDhTQIS9-NRI1eSPybZx",
                    "https://drive.google.com/uc?export=download&id=14zhG5MzXY0648keZJ8sji3z35JYli0X9",
                    "https://drive.google.com/uc?export=download&id=1nQnyP8mvR0VNOB8hPTIzT-0FbBugMrO8",
                    "https://drive.google.com/uc?export=download&id=1cmnSFsJhA-PCU3iMmgjuqR7F6l99BqOR",
                    "https://drive.google.com/uc?export=download&id=1ebLH_xhqLWr1PPM0OCo1ljW_8PNXU4_D",
                    "https://drive.google.com/uc?export=download&id=1axUIxp0JQ2GiBeC_Uzftp6PMkrvMsPN6",
                    "https://drive.google.com/uc?export=download&id=1Ajf_fTFZeAaD_Ef-sS-OvMstEr4MoGG4",
                    "https://drive.google.com/uc?export=download&id=1R2GwC72f5-Ryf__YluRudIneOiUjMUFy",
                    "https://drive.google.com/uc?export=download&id=1KrOgkqNxEbbrwRY49ZFP0B0EAT_jkPLc"};
            String [] titles = {"Alice's Adevntures in Wonderland\nChapter - I",
                    "Alice's Adevntures in Wonderland\nChapter - II",
                    "Alice's Adevntures in Wonderland\nChapter - III" ,
                    "Alice's Adevntures in Wonderland\nChapter - IV"  ,
                    "Alice's Adevntures in Wonderland\nChapter - V"   ,
                    "Alice's Adevntures in Wonderland\nChapter - VI"  ,
                    "Alice's Adevntures in Wonderland\nChapter - VII" ,
                    "Alice's Adevntures in Wonderland\nChapter - VIII",
                    "Alice's Adevntures in Wonderland\nChapter - IX"  ,
                    "Alice's Adevntures in Wonderland\nChapter - X"};
            urlProvider = new UrlProvider(urls,titles);
            String url = urlProvider.getUrlById(getCurrentTrackNumber());//urlProvider.getNextUrl();
            new Player().execute(url);
            title.setText(urlProvider.getTitle(url));
            //new Player().execute("https://ia802508.us.archive.org/5/items/testmp3testfile/mpthreetest.mp3");
        }
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playAndPauseMedia();
            }
        });

        curTime = (TextView) view.findViewById(R.id.player_cur_time);
        totTime = (TextView) view.findViewById(R.id.player_total_time);

        nextButton = (ImageButton) view.findViewById(R.id.player_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(urlProvider.hasNextUrl()) {
                    isPlaying = false;
                    if(mediaPlayer!=null) {
                        if(mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                            mediaPlayer.reset();
                        }
                    }
                    progressBar.setVisibility(View.VISIBLE);
                    new Player().execute(urlProvider.getNextUrl());
                } else if(!urlProvider.hasNextUrl()) {
                    Snackbar.make(view,"No more stories to tell!", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        prevButton = (ImageButton) view.findViewById(R.id.player_prev);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(urlProvider.hasPrevUrl()) {
                    isPlaying = false;
                    if(mediaPlayer!=null) {
                        if(mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                            mediaPlayer.reset();
                        }
                    }
                    progressBar.setVisibility(View.VISIBLE);
                    new Player().execute(urlProvider.getPrevUrl());
                } else if(!urlProvider.hasPrevUrl()) {
                    Snackbar.make(view,"No previous story to tell!", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void playAndPauseMedia() {
        if(!isPlaying) {
            playMedia();
        } else {
            pauseMedia();
        }
    }

    private void playMedia() {
        try {
            if(mediaPlayer!=null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        } catch (IllegalStateException e){}
        playButton.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
        isPlaying = true;
    }

    private void pauseMedia() {
        try{
            if(mediaPlayer!=null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        } catch (IllegalStateException e){}
        playButton.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
        isPlaying = false;
    }

    String TAG = "audio";
    class Player extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(final String... params) {
            Boolean prepared;
            try {
                Log.d(TAG, "doInBackground: ");
                Uri uri = Uri.parse(params[0]);
                mediaPlayer.setDataSource(context,uri);
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(final MediaPlayer mediaPlayer) {
                        if(mediaPlayer.getDuration() >0) {
                            seekBar.setVisibility(View.VISIBLE);
                            Log.d(TAG, "onPrepared: duration = " + mediaPlayer.getDuration());
                            int sec = mediaPlayer.getDuration() / 1000;
                            int min = sec / 60;
                            sec = sec - min * 60;
                            String s = sec < 10 ? "0" + sec : sec + "";
                            String m = min < 10 ? "0" + min : min + "";
                            Log.d(TAG, "onPrepared: " + m + ":" + s);
                            totTime.setText(m + ":" + s);
                            seekBar.setMax(mediaPlayer.getDuration());
                        } else {
                            seekBar.setVisibility(View.GONE);
                            totTime.setText("Streaming audio");
                        }
                        ((MainActivity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(isPlaying && !seekBarDragged && mediaPlayer != null && mediaPlayer.isPlaying()){
                                    int mCurrentPosition = mediaPlayer.getCurrentPosition();
                                    seekBar.setProgress(mCurrentPosition);
                                    int sec = mediaPlayer.getCurrentPosition() / 1000;
                                    int min = sec/60;
                                    sec = sec - min*60;
                                    String s = sec<10 ? "0"+sec : sec+"";
                                    String m = min<10 ? "0"+min : min+"";
                                    curTime.setText(m+":"+s);
                                }
                                mHandler.postDelayed(this, 1000);
                            }
                        });
                        progressBar.setVisibility(View.INVISIBLE);
                        if(firstLoad) {
                            try{
                                Log.d(TAG, "onPrepared: getCurrentTrackPosition "+getCurrentTrackPosition());
                                mediaPlayer.seekTo(getCurrentTrackPosition());
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            firstLoad = false;
                        }
                        mediaPlayer.start();
                        isPlaying = true;
                        playButton.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
                        title.setText(urlProvider.getTitle(params[0]));
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.d(TAG, "onCompletion: ");
                        // TODO Auto-generated method stub
                        initialStage = true;
                        isPlaying=false;
                        playButton.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        Log.d(TAG, "onCompletion: "+urlProvider.hasNextUrl());
                        if(urlProvider.hasNextUrl()) {
                            new Player().execute(urlProvider.getNextUrl());
                        }
                    }
                });
                mediaPlayer.prepareAsync();
                prepared = true;
                Log.d(TAG, "doInBackground: done");
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                Log.d("IllegarArgument", e.getMessage());
                prepared = false;
                isPlaying = false;
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                prepared = false;
                isPlaying = false;
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                prepared = false;
                isPlaying = false;
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                prepared = false;
                isPlaying = false;
                e.printStackTrace();
            }
            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            Log.d("Prepared", "//" + result);
            initialStage = false;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveCurrentTrack();
        isPlaying = false;
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
        } catch (Exception e){}
        mediaPlayer = null;
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mediaPlayer = MediaPlayer.create(context,notification);;
        if(mediaPlayer!=null) {
            mediaPlayer.release();
            isPlaying = false;
        }
    }

    private void saveCurrentTrack() {
        SharedPreferences sharedPref = ((MainActivity)context).getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("TRACK", urlProvider.getCurrentUrlId());
        editor.putInt("CURRENT_POSITION", mediaPlayer.getCurrentPosition());
        editor.apply();
    }

    private int getCurrentTrackNumber() {
        SharedPreferences sharedPref = ((MainActivity)context).getPreferences(Context.MODE_PRIVATE);
        int defaultValue = 0;
        int index = sharedPref.getInt("TRACK", defaultValue);
        return index;
    }

    private int getCurrentTrackPosition() {
        SharedPreferences sharedPref = ((MainActivity)context).getPreferences(Context.MODE_PRIVATE);
        int defaultValue = 0;
        int index = sharedPref.getInt("CURRENT_POSITION", defaultValue);
        return index;
    }

    class UrlProvider{
        private List<String> urls = new ArrayList<>(10);
        private Map<String,String> titles = new HashMap<>(10);
        int currentUrlId = 0;
        int currentTitle = 0;
        boolean goingForward = true;

        public UrlProvider(String[] urls, String[] titles) {
            int i = 0;
            for(String u:urls) {
                this.urls.add(u);
                this.titles.put(u,titles[i++]);
            }
        }

        public int getCurrentUrlId() {
            if(goingForward)
                return currentUrlId-1;
            else
                return currentUrlId+1;
        }

        public String getNextUrl() {
            goingForward = true;
            if(currentUrlId < urls.size())
                return urls.get(currentUrlId++);
            else return "";
        }
        public String getUrlById(int i) {
            if(i>=0 || i<urls.size()) {
                currentUrlId = i+1;
                return urls.get(i);
            }
            return "";
        }
        public String getTitleById(int i) {
            if(i>=0 || i<urls.size()) {
                String u = urls.get(i);
                return titles.get(u);
            }
            return "";
        }
        public String getTitle(String url) {
            return titles.get(url);
        }
        public String getPrevUrl() {
            goingForward = false;
            if(currentUrlId > 0)
                return urls.get(--currentUrlId);
            else return "";
        }
        public boolean hasNextUrl() {
            return currentUrlId <urls.size();
        }
        public boolean hasPrevUrl() {
            return currentUrlId > 0;
        }
    }
}
