package aflashcard.activity;
import aflashcard.activity.R;
import aflashcard.activity.R.id;
import aflashcard.activity.R.layout;
import aflashcard.util.AFlashCardUtil;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;


/**
 * Setting画面Activity
 * @author チェジュホ
 *
 */
public class Setting extends Activity {
	
	private final static int MIN_FONT = 15;
	private final static int FONT_CHANGE_UNIT = 5;
	/**
	 * Setting完了ボタンを押した場合の処理を行う
	 */
	private View.OnClickListener settingCompleteBtnListener = new View.OnClickListener() {
		
		public void onClick(View view) {
			
			storePreferences();
			Intent intent = new Intent(); 
			// メイン画面(インテント)に返却コードを送信  
			setResult(RESULT_OK, intent);  
			// 終了  
			finish();  
		}
	};
	/**
	 * スライドDownボタンを押した場合の処理を行う
	 */
	private OnSeekBarChangeListener slideTimebarBarChangeListener = new OnSeekBarChangeListener() {

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
			if (fromTouch) {
				Log.d("onProgressChanged", String.valueOf(progress));
				TextView slideTimeValue = (TextView) findViewById(R.id.slideTimeValue);
				slideTimeValue.setText(String.valueOf(progress + 1));
			}
		}

		public void onStartTrackingTouch(SeekBar seekBar) {}

		public void onStopTrackingTouch(SeekBar seekBar) {}
	};
	
	/**
	 * スライドDownボタンを押した場合の処理を行う
	 */
	private OnSeekBarChangeListener fontSizebarChangeListener = new OnSeekBarChangeListener() {

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
			if (fromTouch) {
				TextView fontSizeView = (TextView) findViewById(R.id.fontSize);
				fontSizeView.setText(String.valueOf(progressTofontSize(progress)));
			}
		}

		public void onStartTrackingTouch(SeekBar seekBar) {}

		public void onStopTrackingTouch(SeekBar seekBar) {}
	};

//	private OnItemSelectedListener sortListMenuItemClickListener = new OnItemSelectedListener() {
//
//		public void onItemSelected(AdapterView<?> adapter, View view, int position,
//				long id) {
//			Spinner fileListMenu = (Spinner) findViewById(R.id.fileList);
//			String sortid = (String) adapter.getItemAtPosition(position);
//			//メニューが変更した場合だけ
//			if (!_sort.equals(sortid)) {
//				_sort = sortid;
//				Map<String, String> fileList = _util.getFileList(sortid);
//				MapAdapter filenameAdapter = new MapAdapter(SettingActivity.this, fileList);
//				fileListMenu.setAdapter(filenameAdapter);
//			}
//		}
//
//		public void onNothingSelected(AdapterView<?> adapter) {
//		}
//		
//	};

	private RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
		// ラジオグループのチェック状態が変更された時に呼び出されます
	    public void onCheckedChanged(
	        RadioGroup group,
	        int checkedId) { // チェック状態が変更されたラジオボタンの ID が渡されます
	        
	    	SeekBar fontSizebar = (SeekBar) findViewById(R.id.fontSizebar);
	    	
	    	if (R.id.autoSizeOn == checkedId) {
	    		fontSizebar.setEnabled(false);
	    	} else {
	    		fontSizebar.setEnabled(true);
	    	}
	    		
	    }

	};
	
    /**
     * 画面が生成される場合呼び出される
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //①レイアウトの生成
        setContentView(R.layout.setting);
        
        //ボタン設定
		Button settingCompleteBtn = (Button) findViewById(R.id.settingCompleteBtn);
		SeekBar slideTimebar = (SeekBar) findViewById(R.id.slideTimebar);
		SeekBar fontSizebar = (SeekBar) findViewById(R.id.fontSizebar);
		
		settingCompleteBtn.setOnClickListener(settingCompleteBtnListener);
		//②画面設定値取得
		SharedPreferences pref = getSharedPreferences(AFlashCardUtil.PREFS_FILE_NAME, MODE_PRIVATE);
		
		//ファイル名設定
		int dataStoreMethod = pref.getInt("dataStoreMethod", AFlashCardUtil.DATA_STORE_METHOD_SDCARD);
		
		//ファイル名設定
//		String sortId = getSharedPreferences(MemorizeUtil.PREFS_FILE_NAME, MODE_PRIVATE).getString("sortId", "");
//		_sort = sortId;
//		String fileId = getSharedPreferences(MemorizeUtil.PREFS_FILE_NAME, MODE_PRIVATE).getString("memorizeFileId", "");
		//時間設定
		int autoSlideTime = pref.getInt("autoSlideTime", 2);
		
		//Randomモード設定
		boolean randomSign = pref.getBoolean("randomSign", true);
		
		//表示モード設定
		int wordDisplayMode = pref.getInt("wordDisplayMode", 1);
		
		//フォントサイズ
		int fontSize = pref.getInt("fontSize", 40);
		
		//Autoフォント
		boolean autoFontSize = pref.getBoolean("autoFontSize", true);
	
		//Display OPtion
		boolean displayOption = pref.getBoolean("displayOption", true);
		
		//③画面初期値設定
		//Spinner sortListMenu = (Spinner) findViewById(R.id.sortList);
		//Spinner fileListMenu = (Spinner) findViewById(R.id.fileList);
		TextView slideTime = (TextView) findViewById(R.id.slideTimeValue);
		RadioGroup ramdomGroup = (RadioGroup) findViewById(R.id.randomGroup);
		RadioGroup fontAutoGroup = (RadioGroup) findViewById(R.id.fontAuto);
		RadioGroup wordDisplayModeGroup = (RadioGroup) findViewById(R.id.wordDisplayModeGroup);
		RadioGroup displayOptionGroup = (RadioGroup) findViewById(R.id.displayOptionGroup);
		TextView fontSizeView = (TextView) findViewById(R.id.fontSize);

		
		//ソート情報取得
//		try {
//			_util = new MemorizeUtil(dataStoreMethod);
//			Map<String, String> sortList = _util.getSortAllList();
//			Map<String, String> fileNameList = _util.getFileList(sortId);
//			ファイル名設定
//			MapAdapter sortAdapter = new MapAdapter(this, sortList);
//			MapAdapter filenameAdapter = new MapAdapter(this, fileNameList);
//			sortListMenu.setAdapter(sortAdapter);
//			fileListMenu.setAdapter(filenameAdapter);
//			setSpinnerMenuSelect(sortListMenu, sortAdapter, sortId);
//			setSpinnerMenuSelect(fileListMenu, filenameAdapter, fileId);
//			
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//			AlertDialog.Builder ad = new AlertDialog.Builder(this);
//			ad.setTitle(getString(R.string.fileReadErrorMsgHead));
//			ad.setMessage(getString(R.string.fileReadErrorMsg));
//			ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int whichButton) {
//				}
//			});
//			ad.create();
//			ad.show();
//		}

		//sortListMenu.setOnItemSelectedListener(sortListMenuItemClickListener);
		
		slideTimebar.setOnSeekBarChangeListener(slideTimebarBarChangeListener);
		
		fontSizebar.setOnSeekBarChangeListener(fontSizebarChangeListener);
		
		slideTimebar.setProgress(autoSlideTime);
		fontSizebar.setProgress(fontSizeToprogress(fontSize));
		//時間設定
		slideTime.setText(String.valueOf(autoSlideTime));
		
		//Autoモード設定
		if (randomSign) {
			ramdomGroup.check(R.id.random);
		} else {
			ramdomGroup.check(R.id.order);
		}
		//表示モード設定
		if (wordDisplayMode == 0) {
			wordDisplayModeGroup.check(R.id.wordDisplayModeProblem);
		} else if (wordDisplayMode == 1){
			wordDisplayModeGroup.check(R.id.wordDisplayModeProblemAnswer);
		} else {
		}
		//Autoフォント設定
		if (autoFontSize) {
			fontSizebar.setEnabled(false);
			fontAutoGroup.check(R.id.autoSizeOn);
		} else {
			fontSizebar.setEnabled(true);
			fontAutoGroup.check(R.id.autoSizeOff);
		}
		//表示オプション true:問題ー解答 false:解答ー問題
		if (displayOption) {
			displayOptionGroup.check(R.id.displayOptionQA);
		} else {
			displayOptionGroup.check(R.id.displayOptionAQ);
		}
		
		fontAutoGroup.setOnCheckedChangeListener(onCheckedChangeListener);
		//フォントサイズ設定
		fontSizeView.setText(String.valueOf(fontSize));
    }
   
	/**
     * 画面が閉じる場合呼び出される
     */
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    }
    
    /**
     * 設定情報を基でPreferencesに保存する
     */
    private void storePreferences() {
    	//Spinner sortListMenu = (Spinner) findViewById(R.id.sortList);
		//Spinner fileListMenu = (Spinner) findViewById(R.id.fileList);
		TextView slideTime = (TextView) findViewById(R.id.slideTimeValue);
		RadioGroup ramdomGroup = (RadioGroup) findViewById(R.id.randomGroup);
		RadioGroup wordDisplayModeGroup = (RadioGroup) findViewById(R.id.wordDisplayModeGroup);
		RadioGroup fontAutoGroup = (RadioGroup) findViewById(R.id.fontAuto);
		RadioGroup displayOptionGroup = (RadioGroup) findViewById(R.id.displayOptionGroup);
		TextView fontSizeView = (TextView) findViewById(R.id.fontSize);
		
		SharedPreferences preferences = getSharedPreferences(AFlashCardUtil.PREFS_FILE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		
		//ファイル名設定
		//editor.putString("sortId", (String) sortListMenu.getSelectedItem());
		//editor.putString("memorizeFileId", (String) fileListMenu.getSelectedItem());
		
		//editor.putInt("progressCnt", 0);
		
		//時間設定
		editor.putInt("autoSlideTime", Integer.parseInt(slideTime.getText().toString()));
		
		//Autoモード設定
		if (ramdomGroup.getCheckedRadioButtonId() == R.id.random) {
			editor.putBoolean("randomSign", true);
		} else {
			editor.putBoolean("randomSign", false);
		}
		
		//表示モード設定
		if (wordDisplayModeGroup.getCheckedRadioButtonId() == R.id.wordDisplayModeProblem) {
			editor.putInt("wordDisplayMode", 0);
		} else if (wordDisplayModeGroup.getCheckedRadioButtonId() == R.id.wordDisplayModeProblemAnswer){
			editor.putInt("wordDisplayMode", 1);
		} else {
			editor.putInt("wordDisplayMode", 2);
		}
		
		//FontAutoモード設定
		if (fontAutoGroup.getCheckedRadioButtonId() == R.id.autoSizeOn) {
			editor.putBoolean("autoFontSize", true);
		} else {
			editor.putBoolean("autoFontSize", false);
		}
		
		//displayOption設定
		if (displayOptionGroup.getCheckedRadioButtonId() == R.id.displayOptionQA) {
			editor.putBoolean("displayOption", true);
		} else {
			editor.putBoolean("displayOption", false);
		}
		
		//フォントサイズ
		editor.putInt("fontSize", Integer.parseInt(fontSizeView.getText().toString()));
		editor.commit();
    }
    
    
//    private void setSpinnerMenuSelect(Spinner spinner, MapAdapter adapter, String id) {
//		
//    	if (adapter.getCount()==0) {
//    		return;
//    	}
//    	
//    	int select = adapter.getEqualSelectId(id);
//    	//NotFoundの場合
//    	if (select == adapter.getCount()) {
//    		spinner.setSelection(0);
//    	} else {
//    		spinner.setSelection(select);
//    	}
//    	
//	}
    private int progressTofontSize(int progress) {
    	return MIN_FONT + (FONT_CHANGE_UNIT * progress);
    }
    private int fontSizeToprogress(int fontsize) {
    	return (fontsize - MIN_FONT) / FONT_CHANGE_UNIT;
    }
    
    /**
    * ArrayAdapterを拡張したクラス。Hashmap一覧表示させている。
    *
    * @author hyoromo
    */
//        private class MapAdapter extends BaseAdapter {
//            
//        	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
//        	private Context ctx = null;
//        	private Map<String, String> infoMap = null;
//        	private List<String> keyList = null;
//        	private List<String> valueList = null;
//        	
//            public MapAdapter(Context context, Map<String, String> map) {
//            	super();
//            	ctx = context;
//            	infoMap = map;
//            	keyList = new ArrayList<String>();
//            	valueList = new ArrayList<String>();
//            	 for (Map.Entry<String, String> entry : infoMap.entrySet()) {
//            		 keyList.add(entry.getKey());
//            		 valueList.add(entry.getValue());
//        		 }
//            }
//        		           
//            /**
//		    * 画面に表示される毎に呼び出される
//		    */
//            public View getView(int position, View convertView, ViewGroup parent) {
//            	LinearLayout layout = new LinearLayout(ctx);
//            	TextView textview = new TextView(ctx);
//            	textview.setTextColor(Color.BLACK);
//            	textview.setTextSize(15.f);
//            	textview.setText(valueList.get(position));
//            	layout.addView(textview, new LinearLayout.LayoutParams(WC, WC));
//                return layout;
//            }
//            public int getEqualSelectId(String id) {
//            	int i = 0;
//            	for (String key : keyList) {
//            		if (id.equals(key)) {
//            			break;
//            		}
//            		i++;
//            	}
//            	return i;
//            }
//            
//			public int getCount() {
//				return infoMap.size();
//			}
//			public Object getItem(int position) {
//				return keyList.get(position);
//			}
//			public long getItemId(int position) {
//				return position;
//			}
//        }
}