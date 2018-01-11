package com.suvankarmitra.fullscreentextdemo3;

import android.util.Log;

/**
 * Created by suvankar on 12/27/17.
 */

public class Story {
    private CharSequence text;
    private int pageNum;
    private boolean bookmarked = false;

    public Story(CharSequence text) {
        this.text = text;
    }

    public CharSequence getText() {
        return text;
    }

    public void setText(CharSequence text) {
        this.text = text;
    }

    @Override
    public int hashCode() {
        return text.length();
    }

    @Override
    public boolean equals(Object obj) {
        return text.toString().equals(((Story) obj).getText().toString());
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageNum() {
        return pageNum;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }
}
