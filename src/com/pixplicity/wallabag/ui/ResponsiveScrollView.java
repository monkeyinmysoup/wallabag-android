package com.pixplicity.wallabag.ui;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class ResponsiveScrollView extends ScrollView {

	private OnViewScrollListener onScrollViewListener;

	public ResponsiveScrollView(Context context) {
		super(context);
	}
	
	public ResponsiveScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
	
	public ResponsiveScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
	
	public void setOnScrollViewListener(OnViewScrollListener onViewScrollListener){
		this.onScrollViewListener = onViewScrollListener;
	}
	
	@Override
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {
		super.onScrollChanged(x, y, oldx, oldy);
		
		if(onScrollViewListener != null)
			onScrollViewListener.onScrollChanged(x, y, oldx, oldy);
	}
}
