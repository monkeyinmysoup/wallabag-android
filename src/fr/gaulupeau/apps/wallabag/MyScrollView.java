package fr.gaulupeau.apps.wallabag;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView {

	private OnViewScrollListener onScrollViewListener;

	public MyScrollView(Context context) {
		super(context);
	}
	
	public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
	
	public MyScrollView(Context context, AttributeSet attrs) {
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
