package com.pixplicity.font;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import com.pixplicity.wallabag.WallabagApplication;

/**
 * Extension of {@link AutoCompleteTextView} to cope with custom typefaces.
 *
 * @author Pixplicity
 */
@TargetApi(3)
public class FontAutoCompleteTextView extends AutoCompleteTextView {
    public FontAutoCompleteTextView(Context context) {
        this(context, null, 0);
    }

    public FontAutoCompleteTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontAutoCompleteTextView(Context context, AttributeSet attrs,
                                    int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
			WallabagApplication.setFont(this);
        }
    }
}