package com.pixplicity.font;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.pixplicity.wallabag.WallabagApplication;

/**
 * Usage:
 * <pre>
 * MenuInflater inflater = getMenuInflater();
 * inflater.inflate(R.menu.cool_menu, menu);
 * getLayoutInflater().setFactory(new MenuFontFactory());
 * </pre>
 */
public class MenuFontFactory implements LayoutInflater.Factory {

    @Override
	public View onCreateView(String name, Context context,
                             AttributeSet attrs) {

        if (name.equalsIgnoreCase(
                "com.android.internal.view.menu.IconMenuItemView")) {
            try {
                LayoutInflater li = LayoutInflater.from(context);
                final View view = li.createView(name, null, attrs);
                new Handler().post(new Runnable() {
                    @Override
					public void run() {
						WallabagApplication.setFont((TextView) view);
                    }
                });
                return view;
			} catch (InflateException ie) {
				ie.printStackTrace();
			} catch (ClassNotFoundException cce) {
				cce.printStackTrace();
            }
        }
        return null;
    }
}