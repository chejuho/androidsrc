package aflashcard.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import aflashcard.bean.QuestionBean;
import aflashcard.util.FileUtil;
import aflashcard.util.AFlashCardUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//ファイルの読み書き
public class AFlashcardMain extends Activity {

	 private static final int MENU_SETTING = 0;
	 private static final int MENU_BACK = 1;
	 private static final int MENU_RESET = 2;
	 private static final int MENU_MEMORIZER = 3;
	 private static final int MENU_QUIZ = 4;
	 private static final int MENU_AUTO = 5;
	 private static final int MENU_MANUAL = 6;

	
	private String _filePath = "";
	
	// 0: イメージなし  1:イメージある
	private int _imageDis = 0;
	
	private DisplayMetrics _metrics = null;
	private GestureDetector _gestureDetector = null;

	public final static int REQUEST_SETTING = 0;
	private final static int MODE_MEMORIZE = AFlashCardUtil.MODE_MEMORIZE;
	private final static int MODE_QUIZ = AFlashCardUtil.MODE_QUIZ;

	/** UI比率 */
	
	private final static float HEADER_MARGIN = 30;//1.9f * 0.1f;
	private final static float HEADER_RATE = 1.0f * 0.1f;//1.9f * 0.1f;
	private final static float DISPLAY_RATE = 0.6f * 0.1f;//0.7f * 0.1f;
	private final static float PROBLEM_RATE = 4.15f * 0.1f;//2.8f * 0.1f;
	private final static float ANSWER_RATE = 4.15f * 0.1f;//2.8f * 0.1f;
	//private final static float FOOTER_RATE = 1.45f * 0.1f;//1.8f * 0.1f;

	/** AutoFontに関する定数 */
	// 値が増加すると文字サイズが大きくなる
	private final static int MIN_FONT_NUMBER = 6;
	//private final static float DISPLAY_PART_MARGIN = 20f;
	private final static float DISPLAY_PART_UP_MARGIN = 0.15f;//0.15f;
	private final static float DISPLAY_PART_DOWN_MARGIN = 0.15f;//0.15f;
	private final static float FONT_SIZE_WIDTH_RATE = 1.1f;
	private final static float FONT_MARGIN = 2f;
	private final static float FONT_DECLINE_RATE = 0.95f;//0.98f;

	private final static int DATA_STORE_METHOD_SDCARD = 0;
	// AutoFontSize
	private boolean _autoFontSize = true;
	// 暗記：暗記完了/暗記する/総リスト数
	// Quiz：×/○/総問題　0/0/0
	private int _progress1 = 0;
	private int _progress2 = 0;
	private int _progress3 = 0;

	// 0 : memorizer, 1 : quiz
	private int _mode = MODE_MEMORIZE;
	private int _progressCnt = 0;

	private int _wordDisplayMode = 0;

	private boolean _startSign = false;
	private List<QuestionBean> _originalMemorizerBeanList = new ArrayList<QuestionBean>();
	private List<QuestionBean> _displayMemorizerBeanList = new ArrayList<QuestionBean>();
	private List<QuestionBean> _backMemorizerBeanList = null;
	private AFlashCardUtil _memorizeUtil = null;

	private boolean _fileReadError = false;
	private boolean _randomSign = false; 
	private boolean _displayOption = true;
	private final Handler mHandler = new Handler() {
		/**
		 * handleMessage
		 */
		public void handleMessage(android.os.Message msg) {

			if (!_startSign) {
				this.removeMessages(0);
				return; // stop
			} else {
				int autoSlideTime = getSharedPreferences(
						AFlashCardUtil.PREFS_FILE_NAME, MODE_PRIVATE).getInt(
						"autoSlideTime", 5);
				nextWordDisplay();
				this.sendEmptyMessageDelayed(0, autoSlideTime * 1000);
			}

		}

		private void nextWordDisplay() {
			// 処理部
			QuestionBean bean = nextWordProcess();
			updateTopPartDisplay();
			updateCenterPartDisplay(bean);
		}
	};
	
	/**
	 * backlistボタン
	 */
	private View.OnClickListener backlistBtnListener = new View.OnClickListener() {

		public void onClick(View view) {

			backList();

		}

	};
	/**
	 * Nextボタン
	 */
	private View.OnClickListener nextBtnListener = new View.OnClickListener() {

		public void onClick(View view) {

			nextBtnAction();

		}

	};
	
	/**
	 * 暗記完了ボタン
	 */
	private View.OnClickListener memoryCompleteListener = new View.OnClickListener() {

		public void onClick(View view) {

			// 暗記を完了してない場合
			QuestionBean info = removeProblemList();
			
			if (_displayMemorizerBeanList.size() == 0) {
				completeProcess(_mode);
				return;
			}
			addBackList(info, true);
			QuestionBean newBean = nextWordProcess();
			updateTopPartDisplay();
			updateCenterPartDisplay(newBean);
			
			mHandler.removeMessages(0);
			int autoSlideTime = getSharedPreferences(
					AFlashCardUtil.PREFS_FILE_NAME, MODE_PRIVATE).getInt(
					"autoSlideTime", 5);
			// 自動モード実行
			mHandler.sendEmptyMessageDelayed(0, autoSlideTime * 1000);
		}
	};

	/**
	 * 次の問題へボタン
	 */
	private View.OnClickListener quizNextBtnListener = new View.OnClickListener() {

		public void onClick(View view) {

			nextQuizStart();
		}

	};
	/**
	 * 次の問題へボタン
	 */
	private View.OnClickListener menuBtnListener = new View.OnClickListener() {

		public void onClick(View view) {

			openOptionsMenu();
		}

	};
	
	
	/**
	 * questionMarkBtnボタン
	 */
	private View.OnClickListener questionMarkBtnListener = new View.OnClickListener() {

		public void onClick(View view) {
			ImageView answerDisImg = (ImageView) findViewById(R.id.answerDisImg);
			TextView answerDis = (TextView) findViewById(R.id.answer);
			ImageButton questionMarkBtn = (ImageButton) findViewById(R.id.questionMarkBtn);
			
			questionMarkBtn.setVisibility(View.INVISIBLE);
			
			if (_imageDis == 0) {
				answerDis.setVisibility(View.VISIBLE);
			} else {
				answerDisImg.setVisibility(View.VISIBLE);
			}
			
			
		}
	};

	

	/**
	 * questionMarkBtnボタン
	 */
	private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {

		@Override
		public boolean onDoubleTap(MotionEvent event) {
			return false;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent event) {
			return false;
		}

		@Override
		public boolean onDown(MotionEvent event) {
			return false;
		}

		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2,
				float velocityX, float velocityY) {
			Log.d(this.getClass().getName(), "onFling");
        	boolean next = false;
        	next = Math.abs(event1.getX() - event2.getX()) > Math.abs(event1.getY() - event2.getY()); 
        	
        	if (next && !_startSign) {
        		if (event1.getX() < event2.getX()) {
      				nextBtnAction();
        	    	
        		} else {
        			//displayCountDown();
        			removeBackList();
        		}
        	}
        	return next;
		}

		@Override
		public void onLongPress(MotionEvent event) {
			super.onLongPress(event);
		}

		@Override
		public boolean onScroll(MotionEvent event1, MotionEvent event2,
				float distanceX, float distanceY) {

			Log.d(this.getClass().getName(), "onScroll");

			return false;
		}

		@Override
		public void onShowPress(MotionEvent event) {
			super.onShowPress(event);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent event) {
			openOptionsMenu();
			return false;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent event) {
			return false;
		}

	};

	// 初期化
	@Override
	public void onCreate(Bundle icicle) {
		
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// ①レイアウトの生成
		setContentView(R.layout.flashcardmain);

		_gestureDetector = new GestureDetector(this, gestureListener);

		_mode = getSharedPreferences(AFlashCardUtil.PREFS_FILE_NAME, MODE_PRIVATE).getInt("mode", MODE_MEMORIZE);

		_metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(_metrics);

		// 一覧画面から渡されたインテントオブジェクトを取得  
		_filePath = getIntent().getExtras().getCharSequence("DATA_FILE_PATH").toString();
		// UI部品サイズを調節
		resize();

		init(_mode);

	}
	
	@Override  
	protected void onActivityResult(int requestCode, int resultCode,Intent data){  
		init(_mode);
	} 
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);

		menu.add(0 , MENU_SETTING , Menu.NONE , R.string.settingBtn).setIcon(R.drawable.settingicon);
		menu.add(0 , MENU_BACK ,Menu.NONE , R.string.listBackBtn).setIcon(R.drawable.listback);
		menu.add(0 , MENU_RESET ,Menu.NONE , R.string.resetBtn).setIcon(R.drawable.reseticon);
		menu.add(0 , MENU_QUIZ ,Menu.NONE , R.string.quizModeBtn).setIcon(R.drawable.quizicon);
		menu.add(0 , MENU_MEMORIZER ,Menu.NONE , R.string.memorizeModeBtn).setIcon(R.drawable.brainicon);
		menu.add(0 , MENU_AUTO ,Menu.NONE , R.string.autoBtn).setIcon(android.R.drawable.ic_media_play);
		menu.add(0 , MENU_MANUAL ,Menu.NONE , R.string.manualBtn).setIcon(android.R.drawable.ic_media_pause);
		
		return ret;
	}
    
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		if (_mode == MODE_MEMORIZE) {
			 menu.findItem(MENU_QUIZ).setVisible(true);
			 menu.findItem(MENU_MEMORIZER).setVisible(false);
			 if (_startSign) {
					menu.findItem(MENU_MANUAL).setVisible(true);
					menu.findItem(MENU_AUTO).setVisible(false);
				} else {
					menu.findItem(MENU_MANUAL).setVisible(false);
					menu.findItem(MENU_AUTO).setVisible(true);
				}
		} else {
			 menu.findItem(MENU_QUIZ).setVisible(false);
			 menu.findItem(MENU_MEMORIZER).setVisible(true);
			 menu.findItem(MENU_AUTO).setVisible(false);
			 menu.findItem(MENU_MANUAL).setVisible(false);
		}
		
		
	
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		boolean ret = true;
		Intent intent = null;
        switch (item.getItemId()) {
	        default:
	            ret = super.onOptionsItemSelected(item);
	            break;
	        case MENU_SETTING :
	            ret = true;
	            _startSign = false;
				mHandler.removeMessages(0);
				modeSave();
				progressCntSave();
				writeProblem(_mode);
				intent = new Intent(AFlashcardMain.this, Setting.class);
				startActivityForResult(intent, REQUEST_SETTING);
	            break;
	        case MENU_BACK :
	            ret = true;
	            backList();
	           
	            break;
	        case MENU_RESET :
	            ret = true;
	            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(AFlashcardMain.this);
	    		alertDialog.setTitle(R.string.initTitle);
	    		alertDialog.setPositiveButton(R.string.listStartMsgYes,  new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int whichButton) {
	    				_startSign = false;
	    				mHandler.removeMessages(0);
	    				_progressCnt = 0;
	    				progressCntSave();
	    				for (int i = 0; i < _originalMemorizerBeanList.size(); i++) {
	    					if (_mode == MODE_MEMORIZE) {
	    						_originalMemorizerBeanList.get(i).setMemorySign("0");
	    					} else {
	    						_originalMemorizerBeanList.get(i).setQuizSign("0");
	    					}

	    				}
	    				try {
	    					FileUtil.memorizeInfoListToFile(_filePath, _originalMemorizerBeanList, _displayOption);
	    				} catch (IOException e) {
	    					e.printStackTrace();
	    					Toast.makeText(AFlashcardMain.this,
	    							getString(R.string.fileWriteErrorMsg),
	    							Toast.LENGTH_LONG).show();
	    				}
	    				init(_mode);
	    			}
	    		});
	    		alertDialog.setNegativeButton(R.string.listStartMsgNo,  new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int whichButton) {
	    				
	    			}
	    		});
	    		alertDialog.create();
	    		alertDialog.show();
	            break;
	        case MENU_MEMORIZER :
	            ret = true;
	            writeProblem(MODE_QUIZ);
	            progressCntSave();
	            init(MODE_MEMORIZE);
	            break;
	        case MENU_QUIZ :
	            ret = true;
	            writeProblem(MODE_MEMORIZE);
				progressCntSave();
				init(MODE_QUIZ);
	            break;
	        case MENU_AUTO :
	            ret = true;
	         // スタート
				_startSign = true;

				// ボタン設定
				getSharedPreferences(AFlashCardUtil.PREFS_FILE_NAME, MODE_PRIVATE).edit().putBoolean("autoMode", true).commit();
				int autoSlideTime = getSharedPreferences(
						AFlashCardUtil.PREFS_FILE_NAME, MODE_PRIVATE).getInt(
						"autoSlideTime", 5);
				// 自動モード実行
				mHandler.sendEmptyMessageDelayed(0, autoSlideTime * 1000);
	            break;
	        case MENU_MANUAL :
	            ret = true;
	            _startSign = false;
				mHandler.removeMessages(0);
				// autoモードにTrue設定
				getSharedPreferences(AFlashCardUtil.PREFS_FILE_NAME, MODE_PRIVATE).edit().putBoolean("autoMode", false).commit();

	            break;
        }
        return ret;

	}
	
	@Override
	protected void onDestroy() {
		_startSign = false;
		super.onDestroy();
		progressCntSave();
		modeSave();
		writeProblem(_mode);
		SharedPreferences preferences = getSharedPreferences(AFlashCardUtil.PREFS_FILE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("nowMemorizerInfo", _filePath);
		editor.commit();
	}
	private void resize() {

		// float widthPixels = metrics.widthPixels;
		float heightPixels = _metrics.heightPixels - HEADER_MARGIN;

		LinearLayout headerPart = (LinearLayout) findViewById(R.id.headerPart);
		LinearLayout displayPart = (LinearLayout) findViewById(R.id.displayPart);
		LinearLayout problemPart = (LinearLayout) findViewById(R.id.problemPart);
		LinearLayout answerPart = (LinearLayout) findViewById(R.id.answerPart);
		

		// heightPixels * HEADER_RATE
		Log.d("resize", "heightPixels=" + heightPixels);
		Log.d("resize", "HEADER_RATE=" + Float.valueOf(heightPixels * HEADER_RATE).intValue());
		Log.d("resize", "DISPLAY_RATE=" + Float.valueOf(heightPixels * DISPLAY_RATE).intValue());
		Log.d("resize", "PROBLEM_RATE" + Float.valueOf(heightPixels * PROBLEM_RATE).intValue());
		Log.d("resize", "ANSWER_RATE=" + Float.valueOf(heightPixels * ANSWER_RATE).intValue());
		
		// headerPart.getLayoutParams().height = 65;
		headerPart.getLayoutParams().height = Float.valueOf(heightPixels * HEADER_RATE).intValue();
		// displayPart.getLayoutParams().height = 25;
		displayPart.getLayoutParams().height = Float.valueOf(heightPixels * DISPLAY_RATE).intValue();
		problemPart.getLayoutParams().height = Float.valueOf(heightPixels * PROBLEM_RATE).intValue();
		answerPart.getLayoutParams().height = Float.valueOf(heightPixels * ANSWER_RATE).intValue();
		// footerPart.getLayoutParams().height = 65;
		

	}
	private void backList() {
		_startSign = false;
		mHandler.removeMessages(0);
		modeSave();
		progressCntSave();	
		
		Intent intent = new Intent(); 
		// メイン画面(インテント)に返却コードを送信  
		setResult(RESULT_OK, intent);
		// 終了  
		finish();
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		 return _gestureDetector.onTouchEvent(event);

	}

	private void init(int mode) {

		int dataStoreMethod = getSharedPreferences(
				AFlashCardUtil.PREFS_FILE_NAME, MODE_PRIVATE).getInt("dataStoreMethod", DATA_STORE_METHOD_SDCARD);

		// 機能追加 0:メモリモード、1:Quizモード
		int fontSize = getSharedPreferences(AFlashCardUtil.PREFS_FILE_NAME,MODE_PRIVATE).getInt("fontSize", 40);

		_wordDisplayMode = getSharedPreferences(AFlashCardUtil.PREFS_FILE_NAME,MODE_PRIVATE).getInt("wordDisplayMode", 1);
		_autoFontSize = getSharedPreferences(AFlashCardUtil.PREFS_FILE_NAME,MODE_PRIVATE).getBoolean("autoFontSize", true);
		_randomSign = getSharedPreferences(AFlashCardUtil.PREFS_FILE_NAME,MODE_PRIVATE).getBoolean("randomSign", false);
		_displayOption = getSharedPreferences(AFlashCardUtil.PREFS_FILE_NAME,MODE_PRIVATE).getBoolean("displayOption", true);
		TextView problemDis = (TextView) findViewById(R.id.problemDis);
		TextView answer = (TextView) findViewById(R.id.answer);
		if (!_autoFontSize) {
			problemDis.setTextSize(fontSize);
			answer.setTextSize(fontSize);
		}
		
		// ②ボタンリスナー設定
		ImageButton memoryCompleteBtn = (ImageButton) findViewById(R.id.memoryCompleteBtn);
		
		ImageButton menuBtn = (ImageButton) findViewById(R.id.menuBtn);
		// 追加
		Button quizNextBtn = (Button) findViewById(R.id.quizNextBtn);

		ImageButton questionMarkBtn = (ImageButton) findViewById(R.id.questionMarkBtn);
		
		ImageButton backlistBtn = (ImageButton) findViewById(R.id.backlistBtn);
		
		backlistBtn.setOnClickListener(backlistBtnListener);
		memoryCompleteBtn.setOnClickListener(memoryCompleteListener);
		menuBtn.setOnClickListener(menuBtnListener);
		// 追加
		quizNextBtn.setOnClickListener(quizNextBtnListener);

		questionMarkBtn.setOnClickListener(questionMarkBtnListener);

		// 追加Layout設定
		setLayout(mode);

		// ③情報取得
		try {
			_memorizeUtil = new AFlashCardUtil();
			_originalMemorizerBeanList = FileUtil.fileToMemorizeInfoList(_filePath, _displayOption);
			_displayMemorizerBeanList = _memorizeUtil.getDisplayMemorizeList(_originalMemorizerBeanList, mode);
			_fileReadError = false;
			if (_randomSign) {
				_memorizeUtil.shuffle(_displayMemorizerBeanList);
			}
		} catch (IOException e) {
			_fileReadError = true;
			AlertDialog.Builder ad = new AlertDialog.Builder(this);
			ad.setTitle(getString(R.string.fileReadErrorMsgHead));
			ad.setMessage(getString(R.string.fileReadErrorMsg));
			ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			});
			ad.create();
			ad.show();
		}
		
		_backMemorizerBeanList = new ArrayList<QuestionBean>();
		if (mode == MODE_MEMORIZE) {
			memorizeModeStart();
		} else {
			quizModeStart();
		}
	}

	/**
	 * 
	 * @param mode
	 * @return　完了サイン
	 */
	private QuestionBean removeProblemList() {
		QuestionBean info = _displayMemorizerBeanList.remove(_progressCnt);
		processCountDown();
		return info;
	}

	// Quiz開始
	private void quizModeStart() {
		_startSign = false;
		_mode = MODE_QUIZ;
		mHandler.removeMessages(0);
		_progressCnt = getProgressCnt();
		_progress2 = _displayMemorizerBeanList.size();
		_progress3 = _originalMemorizerBeanList.size();
		_progress1 = _progress3 - _progress2;
		updateTopPartDisplay();
		if (_displayMemorizerBeanList.size() == 0) {
			completeProcess(MODE_QUIZ);
			return;
		}

		updateCenterPartDisplay(_originalMemorizerBeanList.get(_displayMemorizerBeanList.get(_progressCnt).getIndex()));
	}

	// Memorizer開始
	private void memorizeModeStart() {
		_mode = MODE_MEMORIZE;
		_progressCnt = getProgressCnt();
		_progress2 = _displayMemorizerBeanList.size();
		_progress3 = _originalMemorizerBeanList.size();
		_progress1 = _progress3 - _progress2;
		if (_displayMemorizerBeanList.size() == 0) {
			completeProcess(MODE_MEMORIZE);
			return;
		}
		//addBackList(_displayMemorizerBeanList.get(_progressCnt), false);
		updateDisplay(_originalMemorizerBeanList.get(_displayMemorizerBeanList.get(_progressCnt).getIndex()));

		// 自動
		if (getSharedPreferences(AFlashCardUtil.PREFS_FILE_NAME, MODE_PRIVATE).getBoolean("autoMode", true)) {
			_startSign = true;
			int autoSlideTime = getSharedPreferences(
					AFlashCardUtil.PREFS_FILE_NAME, MODE_PRIVATE).getInt(
					"autoSlideTime", 5);
			// 自動モード実行
			mHandler.sendMessageDelayed(mHandler.obtainMessage(0), autoSlideTime * 1000);
		}

	}

	private void progressCntSave() {
		//String fileId = getSharedPreferences(MemorizeUtil.PREFS_FILE_NAME,MODE_PRIVATE).getString("memorizeFileId", "");
		SharedPreferences.Editor editor = getSharedPreferences(AFlashCardUtil.PREFS_FILE_NAME, MODE_PRIVATE).edit();
		editor.putInt(_filePath + _mode, _progressCnt);
		editor.commit();
	}

	private int getProgressCnt() {
		//String fileId = getSharedPreferences(MemorizeUtil.PREFS_FILE_NAME,MODE_PRIVATE).getString("memorizeFileId", "");
		int progressCnt = getSharedPreferences(AFlashCardUtil.PREFS_FILE_NAME, MODE_PRIVATE).getInt(_filePath + _mode, 0);
		if (progressCnt >= _displayMemorizerBeanList.size() || progressCnt < 0) {
			progressCnt = 0;
		}
		return progressCnt;
	}

	private void modeSave() {
		SharedPreferences.Editor editor = getSharedPreferences(
				AFlashCardUtil.PREFS_FILE_NAME, MODE_PRIVATE).edit();
		editor.putInt("mode", _mode);
		editor.commit();
	}

	/**
	 * 
	 * @param mode
	 */
	private void setLayout(int mode) {

		ImageButton memoryCompleteBtn = (ImageButton) this.findViewById(R.id.memoryCompleteBtn);
		Button quizNextBtn = (Button) this.findViewById(R.id.quizNextBtn);

		TextView answer = (TextView) this.findViewById(R.id.answer);
		EditText quizAnswer = (EditText) this.findViewById(R.id.quizAnswer);

		ImageButton questionMarkBtn = (ImageButton) findViewById(R.id.questionMarkBtn);

		questionMarkBtn.setVisibility(View.INVISIBLE);

		if (mode == MODE_MEMORIZE) {

			memoryCompleteBtn.setVisibility(View.VISIBLE);
			quizNextBtn.setVisibility(View.INVISIBLE);

			answer.setVisibility(View.VISIBLE);
			quizAnswer.setVisibility(View.INVISIBLE);

			memoryCompleteBtn.setEnabled(true);

		} else if (mode == MODE_QUIZ) {

			memoryCompleteBtn.setVisibility(View.INVISIBLE);

			quizAnswer.setVisibility(View.VISIBLE);
			quizAnswer.setText("");

			answer.setVisibility(View.INVISIBLE);
			quizNextBtn.setVisibility(View.VISIBLE);

		}

	}

	/**
     * 
     */
	private void completeProcess(int mode) {
		mHandler.removeMessages(0);
		_startSign = false;
		_progressCnt = -1;

		ImageButton memoryCompleteBtn = (ImageButton) this.findViewById(R.id.memoryCompleteBtn);
		Button quizNextBtn = (Button) this.findViewById(R.id.quizNextBtn);

		TextView answer = (TextView) this.findViewById(R.id.answer);

		EditText quizAnswer = (EditText) this.findViewById(R.id.quizAnswer);

		TextView problemDis = (TextView) this.findViewById(R.id.problemDis);
		TextView answerDis = (TextView) this.findViewById(R.id.answer);

		ImageButton questionMarkBtn = (ImageButton) findViewById(R.id.questionMarkBtn);

		ImageView problemDisImg = (ImageView) this.findViewById(R.id.problemDisImg);
		ImageView answerDisImg = (ImageView) this.findViewById(R.id.answerDisImg);
		
		problemDisImg.setVisibility(View.INVISIBLE);
		answerDisImg.setVisibility(View.INVISIBLE);
		
		float height = _metrics.heightPixels * PROBLEM_RATE;
		float width = _metrics.widthPixels;
		if (mode == MODE_MEMORIZE) {
			
			quizNextBtn.setVisibility(View.INVISIBLE);
			
			questionMarkBtn.setVisibility(View.INVISIBLE);
			
			problemDis.setVisibility(View.VISIBLE);
			answerDis.setVisibility(View.VISIBLE);

			memoryCompleteBtn.setEnabled(false);

			updateTopPartDisplay();

			
			problemDis.setTextSize(getAutoFontSize(getResources().getText(R.string.endLabel1).toString(), height, width));
			answerDis.setTextSize(getAutoFontSize(getResources().getText(R.string.endLabel2).toString(), height, width));
			problemDis.setText(getResources().getText(R.string.endLabel1));
			answerDis.setText(getResources().getText(R.string.endLabel2));

			// FORMAT エラー
			if (_originalMemorizerBeanList.size() == 0 && !_fileReadError) {
				AlertDialog.Builder ad = new AlertDialog.Builder(
						AFlashcardMain.this);
				ad.setTitle(getString(R.string.fomatError));
				ad.setMessage(getString(R.string.fomatErrorMessage));
				ad.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						});
				ad.create();
				ad.show();
				problemDis.setTextSize(getAutoFontSize(getResources().getText(R.string.fomatError).toString(), height, width));
				answerDis.setTextSize(getAutoFontSize(getResources().getText(R.string.fomatErrorMessage).toString(), height, width));
				problemDis.setText(getResources().getText(R.string.fomatError));
				answerDis.setText(getResources().getText(R.string.fomatErrorMessage));
			}

		} else {
			memoryCompleteBtn.setVisibility(View.INVISIBLE);

			problemDis.setVisibility(View.VISIBLE);
			quizAnswer.setVisibility(View.INVISIBLE);
			answer.setVisibility(View.VISIBLE);
			quizNextBtn.setVisibility(View.INVISIBLE);
			
			questionMarkBtn.setVisibility(View.INVISIBLE);
			
			updateTopPartDisplay();

			problemDis.setTextSize(getAutoFontSize(getResources().getText(R.string.endLabel3).toString(), height, width));
			answerDis.setTextSize(getAutoFontSize(getResources().getText(R.string.endLabel2).toString(), height, width));
			problemDis.setText(getResources().getText(R.string.endLabel3));
			answerDis.setText(getResources().getText(R.string.endLabel2));
			// FORMAT エラー
			if (_originalMemorizerBeanList.size() == 0) {
				AlertDialog.Builder ad = new AlertDialog.Builder(
						AFlashcardMain.this);
				ad.setTitle(getString(R.string.fomatError));
				ad.setMessage(getString(R.string.fomatErrorMessage));
				ad.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						});
				ad.create();
				ad.show();
			}

		}
	}

	

	private void updateDisplay(QuestionBean bean) {
		updateTopPartDisplay();
		updateCenterPartDisplay(bean);
	}


	/**
     * 
     */
	private void updateTopPartDisplay() {
		_progress2 = _displayMemorizerBeanList.size();
		_progress3 = _originalMemorizerBeanList.size();
		_progress1 = _progress3 - _progress2;
		TextView progressCnt1 = (TextView) this.findViewById(R.id.progressCnt1);
		TextView progressCnt2 = (TextView) this.findViewById(R.id.progressCnt2);
		TextView progressCnt3 = (TextView) this.findViewById(R.id.progressCnt3);
		
		TextView progressCnt = (TextView) this.findViewById(R.id.progressCnt);
		TextView memorizeCnt = (TextView) this.findViewById(R.id.memorizeCnt);

		progressCnt1.setText(String.valueOf(_progress1));
		progressCnt2.setText(String.valueOf(_progress2));
		progressCnt3.setText(String.valueOf(_progress3));
		
		progressCnt.setText(String.valueOf(_progressCnt + 1));
		memorizeCnt.setText(String.valueOf(_progress2));
	}

	/**
	 * 
	 * @param bean
	 */
	private void updateCenterPartDisplay(QuestionBean bean) {

		TextView problemDis = (TextView) this.findViewById(R.id.problemDis);
		TextView answerDis = (TextView) this.findViewById(R.id.answer);
		ImageView problemDisImg = (ImageView) this.findViewById(R.id.problemDisImg);
		ImageView answerDisImg = (ImageView) this.findViewById(R.id.answerDisImg);
		ImageButton questionMarkBtn = (ImageButton) findViewById(R.id.questionMarkBtn);
		EditText quizAnswer = (EditText) findViewById(R.id.quizAnswer);
		
		Log.d("MemorizeWordsActivity", "wordDisplayMode=" + _wordDisplayMode);
		Log.d("MemorizeWordsActivity", "Count=" + _progressCnt);
		// AUTO FONT サイズの場合
		if (_autoFontSize) {
			float height = _metrics.heightPixels * PROBLEM_RATE;
			//problemDis.setPadding(0, (int)(height * DISPLAY_PART_UP_MARGIN), 0, (int)(height * DISPLAY_PART_DOWN_MARGIN));
			float width = _metrics.widthPixels;

			float problemFontSize = getAutoFontSize(bean.getQuestion(), height, width);
			float answerFontSize = getAutoFontSize(bean.getAnswer(), height, width);
			// problemDis.setLineSpacing(5,5);
			problemDis.setTextSize(problemFontSize);
			answerDis.setTextSize(answerFontSize);
		}
		
		if (_mode == MODE_MEMORIZE) {
			_imageDis = 0;
			problemDis.setVisibility(View.VISIBLE);
			problemDisImg.setVisibility(View.INVISIBLE);
			problemDis.setText(bean.getQuestion());
			answerDis.setVisibility(View.VISIBLE);
			answerDisImg.setVisibility(View.INVISIBLE);
			answerDis.setText(bean.getAnswer());
			
			switch (_wordDisplayMode) {
			// 問題だけ
			case 0:
				answerDis.setVisibility(View.INVISIBLE);
				questionMarkBtn.setVisibility(View.VISIBLE);
				break;
			// 問題と答え両方
			default:
				break;
			}
		} else {
			problemDis.setVisibility(View.VISIBLE);
			problemDisImg.setVisibility(View.INVISIBLE);
			problemDis.setText(bean.getQuestion());
			quizAnswer.setEnabled(true);
			quizAnswer.setText("");
		}
		
		resourceInfoProcess(bean);
	}
	
	private void resourceInfoProcess(QuestionBean bean) {
		
		//imageView処理
		imageViewProcess(bean);
		
		
		
	}
	private void imageViewProcess(QuestionBean bean) {
		TextView problemDis = (TextView) findViewById(R.id.problemDis);
		TextView answerDis = (TextView) findViewById(R.id.answer);
		ImageView problemDisImg = (ImageView) findViewById(R.id.problemDisImg);
		ImageView answerDisImg = (ImageView) findViewById(R.id.answerDisImg);
		EditText quizAnswer = (EditText) findViewById(R.id.quizAnswer);
		ImageButton questionMarkBtn = (ImageButton) findViewById(R.id.questionMarkBtn);
		
		String questionImagePath = FileUtil.getImagePath(bean.getQuestion());
		String answerImagePath = FileUtil.getImagePath(bean.getAnswer());
		//暗記モード
		if (_mode == MODE_MEMORIZE) {
			
			if (questionImagePath.length() != 0) {
				problemDis.setVisibility(View.INVISIBLE);
				problemDisImg.setVisibility(View.VISIBLE);
				problemDisImg.setImageURI(Uri.parse(questionImagePath));
			} 
			if (answerImagePath.length() != 0) {
				_imageDis = 1;
				answerDis.setVisibility(View.INVISIBLE);
				answerDisImg.setVisibility(View.VISIBLE);
				answerDisImg.setImageURI(Uri.parse(answerImagePath));
				
				if (0 == _wordDisplayMode) {
					answerDisImg.setVisibility(View.INVISIBLE);
					questionMarkBtn.setVisibility(View.VISIBLE);
				}
			} 
		//Quizモード	
		} else {
			if (questionImagePath.length() != 0) {
				problemDis.setVisibility(View.INVISIBLE);
				problemDisImg.setVisibility(View.VISIBLE);
				problemDisImg.setImageURI(Uri.parse(questionImagePath));
			} 
			if (answerImagePath.length() != 0) {
				answerDisImg.setVisibility(View.INVISIBLE);
				quizAnswer.setEnabled(false);
				quizAnswer.setText("Image");
			} 
		}
		
	}
	/**
	 * 
	 * @return
	 */
	private QuestionBean nextWordProcess() {

		processCountUp();
		return _originalMemorizerBeanList.get(_displayMemorizerBeanList.get(_progressCnt).getIndex()) ;
	}

	private void nextQuizStart() {

		final EditText quizAnswer = (EditText) findViewById(R.id.quizAnswer);
		String inputAnswer = quizAnswer.getText().toString();
		
		QuestionBean bean = _originalMemorizerBeanList.get(_displayMemorizerBeanList.get(_progressCnt).getIndex());

		//正解
		if (answerCheck(bean, inputAnswer)) {
			// 追加
			addBackList(_displayMemorizerBeanList.get(_progressCnt), true);
			removeProblemList();
			//completeCountUp();
			if (_displayMemorizerBeanList.size() == 0) {
				completeProcess(_mode);
				return;
			}
			QuestionBean newBean = nextWordProcess();
			updateCenterPartDisplay(newBean);
			updateTopPartDisplay();
			
		} else {
			// 追加
			addBackList(_displayMemorizerBeanList.get(_progressCnt), false);
			AlertDialog.Builder ad = new AlertDialog.Builder(AFlashcardMain.this);
			ad.setTitle(getString(R.string.quizMessageTitle));

			ad.setMessage(makeAlertMessage(inputAnswer, bean.getAnswer()));
			ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					QuestionBean newBean = nextWordProcess();
					updateCenterPartDisplay(newBean);
					updateTopPartDisplay();
				}
			});
			ad.create();
			ad.show();
		}
	}

	private void nextBtnAction() {
		if (_startSign) {
			return;
		}
		
		addBackList(_displayMemorizerBeanList.get(_progressCnt), false);
		if (_mode == MODE_MEMORIZE) {
			if ("1".equals(_displayMemorizerBeanList.get(_progressCnt).getMemorySign())) {
				removeProblemList();
			}
		} else {
			if ("1".equals(_displayMemorizerBeanList.get(_progressCnt).getQuizSign())) {
				removeProblemList();
			}
		}
		
		QuestionBean bean = nextWordProcess();
		updateTopPartDisplay();
		updateCenterPartDisplay(bean);

	}

	private String makeAlertMessage(String inputAnswer, String answer) {
		StringBuilder sb = new StringBuilder();

		sb.append(getString(R.string.quizInput) + inputAnswer);
		sb.append("\n");
		sb.append(getString(R.string.quizAnswer) + answer);

		return sb.toString();
	}
	
	private boolean answerCheck(QuestionBean bean, String inputAnswer) {
		
		String inputAns = inputAnswer.trim();
		
		if (inputAns.equalsIgnoreCase(bean.getAnswer().trim())) {
			return true;
		}
		if (bean.getAnswers() != null) {
			for (String ans : bean.getAnswers()) {
				Log.d("getAnswerList", ans);
				if (inputAns.equalsIgnoreCase(ans.trim())) {
					return true;
				}
			}
		}
		
		return false;
		
	}

	/**
     * 
     */
	private void writeProblem(int mode) {

		if (_memorizeUtil == null) {
			return;
		}
		
		//String fileId = getSharedPreferences(MemorizeUtil.PREFS_FILE_NAME, MODE_PRIVATE).getString("memorizeFileId", "");

		for (QuestionBean bean : _originalMemorizerBeanList) {
			if (mode == MODE_MEMORIZE) {
				bean.setMemorySign("1");
			} else {
				bean.setQuizSign("1");
			}
		}
		for (QuestionBean displayMemorizerBean : _displayMemorizerBeanList) {
			if (mode == MODE_MEMORIZE) {
				_originalMemorizerBeanList.get(displayMemorizerBean.getIndex()).setMemorySign("0"); 
			} else {
				_originalMemorizerBeanList.get(displayMemorizerBean.getIndex()).setQuizSign("0");
			}
		}

		try {			
			FileUtil.memorizeInfoListToFile(_filePath, _originalMemorizerBeanList, _displayOption);
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(AFlashcardMain.this,
					getString(R.string.fileWriteErrorMsg), Toast.LENGTH_LONG).show();
			Toast.makeText(AFlashcardMain.this,
					e.toString(), Toast.LENGTH_LONG).show();
		}
	}

	private float getAutoFontSize(String text, float height, float width) {
		Log.d("getAutoFontSizeInput", "height=" + Float.toString(height)
				+ "width=" + Float.toString(width));
		int n = 1;
		float autoFontSize = 20;

		if (height <= 0 || width <= 0) {
			return autoFontSize;
		}

		final float displayHeight = height - (height * (DISPLAY_PART_UP_MARGIN + DISPLAY_PART_DOWN_MARGIN));

		float fontSize = getFontSize(displayHeight, width);
		Float inputTextLength = getInputTextLength(fontSize, width);

		int textSize = text.length();

		autoFontSize = fontSize;
		float nowDisplayHeight = displayHeight;
		while (textSize > inputTextLength.intValue() * n) {
			nowDisplayHeight = nowDisplayHeight * FONT_DECLINE_RATE;
			autoFontSize = getFontSize(nowDisplayHeight, width);
			Log.d("displayHeight", "displayHeight="+ Float.toString(nowDisplayHeight));
			Log.d("autoFontSize", "N=" + n + ",autoFontSize="+ Float.toString(autoFontSize));
			inputTextLength = getInputTextLength(autoFontSize, width);
			
			if (displayHeight / (n + 1) >= autoFontSize) {
				n++;
			}
		}

		return autoFontSize;

	}

	private float getFontSize(float height, float width) {
		float heightSubAfterMargin = height;
		float fontSize = 0;
		// 6字が入れない場合 高:幅= 1:1仮定
		if (width < heightSubAfterMargin * MIN_FONT_NUMBER) {
			fontSize = width / MIN_FONT_NUMBER;
		} else {
			fontSize = heightSubAfterMargin;
		}
		return fontSize - FONT_MARGIN;
		// return fontSize * 0.8f;
	}

	private float getInputTextLength(float fontsize, float w) {
		Log.d("inputTextLength input", "fontsize=" + Float.toString(fontsize)
				+ "width=" + w);
		Log.d("inputTextLength return", Float.toString(w
				/ (fontsize * FONT_SIZE_WIDTH_RATE)));
		return w / (fontsize * FONT_SIZE_WIDTH_RATE);
	}
	/**
	 * Addボタン
	 */
	private void addBackList(QuestionBean info, boolean completeSign) {

		if (completeSign && _mode == MODE_MEMORIZE) {
			info.setMemorySign("1");
		} else if (completeSign && _mode != MODE_MEMORIZE) {
			info.setQuizSign("1");
		}

		_backMemorizerBeanList.add(info);
	}
	
	/**
	 * Backボタン
	 */
	private void removeBackList() {

		if (_backMemorizerBeanList.size() == 0 || _displayMemorizerBeanList.size() == 0) {
			return;
		}
		QuestionBean info = _backMemorizerBeanList.remove(_backMemorizerBeanList.size() - 1);
		int beforeCnt = _progressCnt - 1;
		if (beforeCnt == -1) {
			beforeCnt = _displayMemorizerBeanList.size() - 1;
		}
		if(info.getIndex() != _displayMemorizerBeanList.get(beforeCnt).getIndex()){
			_displayMemorizerBeanList.add(_progressCnt, info);
			
		} 
		else {
			processCountDown();
		}
		updateTopPartDisplay();
		updateCenterPartDisplay(_originalMemorizerBeanList.get(info.getIndex()));

	}

	private void processCountUp() {
		int totalCnt = _displayMemorizerBeanList.size();
		++_progressCnt;
		if (_progressCnt >= totalCnt) {
			if (_randomSign) {
				_memorizeUtil.shuffle(_displayMemorizerBeanList);
			}
			_progressCnt = 0;
		}
	}
	private void processCountDown() {
		
		--_progressCnt;
		if (_progressCnt == -1) {
			_progressCnt = _displayMemorizerBeanList.size() - 1;
		}
	}
}