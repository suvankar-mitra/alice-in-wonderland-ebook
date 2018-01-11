package com.suvankarmitra.fullscreentextdemo3;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by suvankar on 12/27/17.
 */

public class ViewAdapter extends RecyclerView.Adapter<ViewAdapter.MyViewHolder> {
    private List<Story> stories;
    private Context context;

    ViewAdapter(Context context, List<Story> stories) {
        this.context = context;
        this.stories = stories;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_view_page, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Story story = stories.get(position);
        if(story.getText()!=null) {
            holder.textView.setText(story.getText());
            if(story.getPageNum()>0) {
                String num = story.getPageNum()+"";
                holder.pageNum.setText(num);
            }
            else
                holder.pageNum.setText("");
        }
        if(story.isBookmarked())
            holder.bookmark.setVisibility(View.VISIBLE);
        else
            holder.bookmark.setVisibility(View.INVISIBLE);

        holder.textView.setTextIsSelectable(true);
    }

    private String TAG = "ViewAdapter";
    @Override
    public int getItemCount() {
        return stories.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textView;
        TextView pageNum;
        FontManager manager = new FontManager(context);
        ImageView bookmark;

        public MyViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text_view);
            manager.setTypeface(textView,"jos");
            textView.setFocusable(true);
            textView.setClickable(true);
            textView.setSelected(true);
            textView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if(b) {
                        Log.d(TAG, "onClick: toggleRecyclerViewScale");
                        ((MainActivity)context).toggleRecyclerViewScale();
                        ((MainActivity)context).hideStatusBar();
                        ((MainActivity)context).toggleControlsVisibility();
                    }
                }
            });
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: toggleRecyclerViewScale");
                    ((MainActivity)context).toggleRecyclerViewScale();
                    ((MainActivity)context).hideStatusBar();
                    ((MainActivity)context).toggleControlsVisibility();
                }
            });

            pageNum = (TextView) itemView.findViewById(R.id.page_num);

            bookmark = (ImageView) itemView.findViewById(R.id.page_bookmark_image);

        }
    }
}
