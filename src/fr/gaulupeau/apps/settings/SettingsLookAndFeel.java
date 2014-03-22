package fr.gaulupeau.apps.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import fr.gaulupeau.apps.wallabag.R;

public class SettingsLookAndFeel extends SettingsBase {
	private static final String[] items = new String[] {"Sans serif", "Serif"};
	private int fontSize = 16;
	private static final int fontSizeMin = 14;

	private int progressFromSize() {
		return fontSize - fontSizeMin;
	}

	private int fontSizeFromProgress(int progress) {
		return progress + fontSizeMin;
	}
	
	@Override
	protected void createUI(ListView list, ListViewAdapter adapter, LayoutInflater inflater){
		View darkThemeLayout = inflater.inflate(R.layout.dark_theme, null);
		final CheckBox darkThemeCheckBox = (CheckBox) darkThemeLayout
				.findViewById(R.id.dark_theme_check_box);
		darkThemeLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				darkThemeCheckBox.setChecked(!darkThemeCheckBox.isChecked());
			}
		});

		View fontSizeLayout = inflater.inflate(R.layout.font_size, null);
		SeekBar fontBar = (SeekBar) fontSizeLayout.findViewById(R.id.font_bar);
		final TextView fontSizeText = (TextView) fontSizeLayout.findViewById(R.id.font_size_text);
		fontSizeText.setText(fontSize + "pt");
		fontBar.setProgress(progressFromSize());
		fontBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {						
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}						
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				fontSize = fontSizeFromProgress(progress);
				fontSizeText.setText(fontSize + "pt");							
			}
		});
		
		View fontStyleLayout = inflater.inflate(R.layout.font_style, null);
		final TextView fontType = (TextView) fontStyleLayout.findViewById(R.id.font_type);
		
		fontStyleLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(SettingsLookAndFeel.this);
				
				builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						fontType.setText(items[which]);									
					}
				});
				AlertDialog alert = builder.create();
			    alert.show();
			}
		});

		View rotationLockLayout = inflater.inflate(R.layout.rotation_lock, null);
		
		rotationLockLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			}
		});
		
		View immersiveModeLayout = inflater.inflate(R.layout.immersive_mode, null);
		final CheckBox immmersiveCheckBox = (CheckBox) immersiveModeLayout.findViewById(R.id.immersive_check_box);
		immersiveModeLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				immmersiveCheckBox.setChecked(!immmersiveCheckBox.isChecked());
			}
		});
		
		adapter.addView(darkThemeLayout);
		adapter.addView(fontSizeLayout);
		adapter.addView(fontStyleLayout);
		adapter.addView(rotationLockLayout);
		adapter.addView(immersiveModeLayout);
		
		list.setAdapter(adapter);
	}
}
