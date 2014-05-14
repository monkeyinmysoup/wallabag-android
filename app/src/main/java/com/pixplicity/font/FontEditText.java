package com.pixplicity.font;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import com.pixplicity.wallabag.WallabagApplication;

/**
 * Extension of {@link EditText} to cope with custom typefaces.
 *
 * @author Pixplicity
 */
@TargetApi(3)
public class FontEditText extends EditText {
    public FontEditText(Context context) {
        this(context, null, 0);
    }

    public FontEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
		WallabagApplication.setFont(this);
    }
}