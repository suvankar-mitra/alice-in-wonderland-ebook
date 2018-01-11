package com.suvankarmitra.fullscreentextdemo3;

import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joe on 03.09.15.
 */
public class Pagination {
    private final boolean mIncludePad;
    private final int mWidth;
    private final int mHeight;
    private final float mSpacingMult;
    private final float mSpacingAdd;
    private final CharSequence mText;
    private final TextPaint mPaint;
    private final List<CharSequence> mPages;

    public Pagination(CharSequence text, int pageW, int pageH, TextPaint paint, float spacingMult, float spacingAdd, boolean inclidePad) {
        this.mText = text;
        this.mWidth = pageW;
        this.mHeight = pageH;
        this.mPaint = paint;
        this.mSpacingMult = spacingMult;
        this.mSpacingAdd = spacingAdd;
        this.mIncludePad = inclidePad;
        this.mPages = new ArrayList<CharSequence>();

        // This is to chop the text to pages, where page break is: --NEW_PAGE--
        // It will break the text according to the page height and text length also.
        String[] chapters = mText.toString().split("--NEW_PAGE--");
        int pageStartOffset = 0;
        for(String chap:chapters) {
            CharSequence cs = mText.subSequence(pageStartOffset, chap.length() + pageStartOffset);
            if (chap.length() > 0)
                cs = mText.subSequence(pageStartOffset, chap.length() + pageStartOffset);
            if (!cs.toString().trim().isEmpty()) {
                layout(cs);
            }
            pageStartOffset += chap.length() + "--NEW_PAGE--".length();
        }
    }

    private void layout(final CharSequence mText) {
        final StaticLayout layout = new StaticLayout(mText, mPaint, mWidth, Layout.Alignment.ALIGN_NORMAL, mSpacingMult, mSpacingAdd, mIncludePad);

        final int lines = layout.getLineCount();
        final CharSequence text = layout.getText();

        int startOffset = 0;
        int height = mHeight;

        for (int i = 0; i < lines; i++) {
            if (height < layout.getLineBottom(i)) {
                // When the layout height has been exceeded
                addPage(text.subSequence(startOffset, layout.getLineStart(i)));
                startOffset = layout.getLineStart(i);
                height = layout.getLineTop(i) + mHeight;
            }

            if (i == lines - 1) {
                // Put the rest of the text into the last page
                addPage(text.subSequence(startOffset, layout.getLineEnd(i)));
                return;
            }
        }
    }

    private void addPage(CharSequence text) {
        if(!text.toString().trim().isEmpty())
            mPages.add(text);
    }

    public int size() {
        return mPages.size();
    }

    public CharSequence get(int index) {
        return (index >= 0 && index < mPages.size()) ? mPages.get(index) : null;
    }
}
