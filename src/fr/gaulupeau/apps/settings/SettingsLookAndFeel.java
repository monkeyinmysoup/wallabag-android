package fr.gaulupeau.apps.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import fr.gaulupeau.apps.wallabag.R;

public class SettingsLookAndFeel extends SettingsBase {
	public static final int SANS = 0;
	public static final int SERIF = 1;
	public static final int DYMAMIC = 0;
	public static final int LANDSCAPE = 1;
	public static final int PORTRAIT = 2;
	
	private static final int[] fontStyleOptions = new int[] {R.string.sans_serif, R.string.serif};
	private static final int[] orientationOptions = new int[] {R.string.dynamic, R.string.landscape, R.string.portrait};
	private static final int[] alignOptions = new int[] {R.string.left, R.string.center, R.string.right, R.string.justified};
	public static final String DARK_THEME = "DarkTheme";
	public static final String FONT_SIZE = "FontSize";
	public static final String FONT_STYLE = "FontStyle";
	public static final String ORIENTATION = "Orientation";
	public static final String IMMERSIVE = "Immersive";
	public static final String ALIGN = "TextAlign";
	public static final String KEEP_SCREEN_ON = "KeepScreenOn";
	
	private static final int fontSizeMin = 14;
	
	private boolean isDarkThemeSelected;
	private int fontSize;
	private int fontStyle;
	private int textAlign;
	private int orientation;
	private boolean isImmerviveModeSelected;
	private boolean isScreenAlwaysOn;

	private int progressFromSize() {
		return fontSize - fontSizeMin;
	}

	private int fontSizeFromProgress(int progress) {
		return progress + fontSizeMin;
	}
	
	@Override
	protected void createUI(ListView list, GeneralPurposeListViewAdapter adapter, LayoutInflater inflater){
		
		//Dark theme
		View darkThemeLayout = inflater.inflate(R.layout.look_and_feel_dark_theme, null);
		final CheckBox darkThemeCheckBox = (CheckBox) darkThemeLayout.findViewById(R.id.dark_theme_check_box);
		
		darkThemeCheckBox.setChecked(isDarkThemeSelected);
		
		darkThemeLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				darkThemeCheckBox.toggle();
			}
		});
		
		darkThemeCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				isDarkThemeSelected = isChecked;
			}
		});

		//Font size		
		View fontSizeLayout = inflater.inflate(R.layout.look_and_feel_font_size, null);
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

		//Font style
		View fontStyleLayout = inflater.inflate(R.layout.look_and_feel_font_style, null);
		final TextView fontType = (TextView) fontStyleLayout.findViewById(R.id.font_type);
		
		fontType.setText(getStringFontStyle(fontStyle));
		
		fontStyleLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(SettingsLookAndFeel.this);
				String[] choices = new String[] {getString(fontStyleOptions[0]), getString(fontStyleOptions[1])};
				builder.setSingleChoiceItems(choices, fontStyle, new DialogInterface.OnClickListener() {				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						fontStyle = which;
						fontType.setText(getStringFontStyle(which));
						dialog.dismiss();
					}
				});
				AlertDialog alert = builder.create();
			    alert.show();
			}
		});
		
		
		//Text align
		View textAlignLayout = inflater.inflate(R.layout.look_and_feel_text_align, null);
		final TextView textAlignTextView = (TextView)textAlignLayout.findViewById(R.id.align_type);
		
		textAlignTextView.setText(getStringAlign(textAlign));
		
		textAlignLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(SettingsLookAndFeel.this);
				String[] choices = new String[] {getStringAlign(0), getStringAlign(1), getStringAlign(2), getStringAlign(3)};
				builder.setSingleChoiceItems(choices, textAlign, new DialogInterface.OnClickListener() {				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						textAlign = which;
						textAlignTextView.setText(getStringAlign(which));
						dialog.dismiss();
					}
				});
				AlertDialog alert = builder.create();
			    alert.show();
			}
		});
		
		//Orientation
		View orientationLayout = inflater.inflate(R.layout.look_and_feel_orientation, null);
		final TextView orientationTypeView = (TextView) orientationLayout.findViewById(R.id.orientation_type);
		
		orientationTypeView.setText(getStringOrientation(orientation));
		
		orientationLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(SettingsLookAndFeel.this);
				String[] choices = new String[] {getStringOrientation(0), getStringOrientation(1), getStringOrientation(2)};
				builder.setSingleChoiceItems(choices, orientation, new DialogInterface.OnClickListener() {				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						orientation = which;
						orientationTypeView.setText(getStringOrientation(which));
						dialog.dismiss();
					}
				});
				AlertDialog alert = builder.create();
			    alert.show();
			}
		});
		
		//Immersive mode
		View immersiveModeLayout = inflater.inflate(R.layout.look_and_feel_immersive_mode, null);
		final CheckBox immmersiveCheckBox = (CheckBox) immersiveModeLayout.findViewById(R.id.immersive_check_box);
		
		immmersiveCheckBox.setChecked(isImmerviveModeSelected);
		
		immersiveModeLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				immmersiveCheckBox.toggle();
			}
		});
		
		immmersiveCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				isImmerviveModeSelected = isChecked;
			}
		});
		
		//Keep screen on
		View screenOnLayout = inflater.inflate(R.layout.look_and_feel_keep_screen_on, null);
		final CheckBox screenOnCheckBox = (CheckBox) screenOnLayout.findViewById(R.id.screen_on_check_box);
		
		screenOnCheckBox.setChecked(isScreenAlwaysOn);
		
		screenOnLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				screenOnCheckBox.toggle();
			}
		});
		
		screenOnCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				isScreenAlwaysOn = isChecked;
			}
		});
		
		adapter.addView(darkThemeLayout);
		adapter.addView(fontSizeLayout);
		adapter.addView(fontStyleLayout);
		adapter.addView(textAlignLayout);
		adapter.addView(orientationLayout);
		adapter.addView(immersiveModeLayout);
		adapter.addView(screenOnLayout);
		
		list.setAdapter(adapter);
	}

	@Override
	protected void getSettings(){
		isDarkThemeSelected = settings.getBoolean(DARK_THEME, false);
		fontSize = settings.getInt(FONT_SIZE, 16);
		fontStyle = settings.getInt(FONT_STYLE, 0);
		textAlign = settings.getInt(ALIGN, 0);
		orientation = settings.getInt(ORIENTATION, 0);
		isImmerviveModeSelected = settings.getBoolean(IMMERSIVE, true);
		isScreenAlwaysOn = settings.getBoolean(KEEP_SCREEN_ON, false);
	}
	
	@Override
	protected void saveSettings() {
		Editor editor = settings.edit();

		editor.putBoolean(DARK_THEME, isDarkThemeSelected);
		editor.putInt(FONT_SIZE , fontSize);
		editor.putInt(FONT_STYLE, fontStyle);
		editor.putInt(ALIGN, textAlign);
		editor.putInt(ORIENTATION, orientation);
		editor.putBoolean(IMMERSIVE, isImmerviveModeSelected);
		editor.putBoolean(KEEP_SCREEN_ON, isScreenAlwaysOn);
		
		editor.commit();
	}
	
	private String getStringFontStyle(int which){
		return getString(fontStyleOptions[which]);
	}
	
	private String getStringOrientation(int which){
		return getString(orientationOptions[which]);
	}
	
	private String getStringAlign(int which) {
		return getString(alignOptions[which]);
	}
}
