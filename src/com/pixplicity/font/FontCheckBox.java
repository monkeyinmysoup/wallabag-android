package com.pixplicity.font;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

import com.pixplicity.wallabag.WallabagApplication;

/**
 * Extension of {@link CheckBox} to cope with custom typefaces.
 *
 * @author Pixplicity
 */
@TargetApi(3)
public class FontCheckBox extends CheckBox {
    public FontCheckBox(Context context) {
        this(context, null, 0);
    }

    public FontCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
			WallabagApplication.setFont(this);
        }
    }
}