package aflashcard.activity;

import java.io.File;
import java.util.List;
import java.util.Locale;

import aflashcard.util.FileUtil;
import aflashcard.util.AFlashCardUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class AFlashcardList extends Activity {

	private DisplayMetrics _metrics = null;
	private ListView _memorizerNameList = null;
	//private LinearLayout _headerPart = null;
	//private LinearLayout _listPart = null;
	//private FrameLayout _footerPart = null;
	
	private TextView _emptyView = null;
	private TextView _title = null;
	
	private ImageButton _importBtn = null;
	private ImageButton _webopenBtn = null;
	
	private final static int HEADER_MARGIN = 30;
	private final static int HEADER = 30;
	private final static int FOOTER = 66;
	
	private final static int SHOW_MEMORIZER_ACTIVITY = 0;
	private final static int SHOW_SETTING_ACTIVITY = 1;
	private final static int SHOW_IMPORTLIST_ACTIVITY = 2;
	
	private final static int CONTEXT_ITEM_DELETE = 1000;
	private final static int CONTEXT_ITEM_EDIT = 1001;

	
	
	
	/**
	 * importボタンを押した場合の処理を行う
	 */
	private View.OnClickListener importBtnListener = new View.OnClickListener() {
		
		public void onClick(View view) {
			 // インテントへのインスタンス生成  
		     Intent intent = new Intent(AFlashcardList.this, ImportList.class);
		    	           
		     // サブ画面(インテント)の起動  
		     startActivityForResult(intent, SHOW_IMPORTLIST_ACTIVITY);  
		}
	};
	/**
	 * webopenボタンを押した場合の処理を行う
	 */
	private View.OnClickListener webopenBtnListener = new View.OnClickListener() {
		
		public void onClick(View view) {
			//Lo	
			String language = getResources().getConfiguration().locale.getLanguage();
			Intent bi = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.itfrees.com/mflashcard?localUserId=" + language));  
			startActivity(bi);  
		}
	};
	
	
	private AdapterView.OnItemClickListener listClickListener = new AdapterView.OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			 ListView listView = (ListView) parent;
		     // クリックされたアイテムを取得します
			 File item =  (File) listView.getItemAtPosition(position);
		     // インテントへのインスタンス生成  
		     Intent intent = new Intent(AFlashcardList.this, AFlashcardMain.class);
		     intent.putExtra("DATA_FILE_PATH", item.getAbsolutePath()); 
		     // サブ画面(インテント)の起動  
		     startActivityForResult(intent, SHOW_MEMORIZER_ACTIVITY);  
		}
		
	};
	

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTheme(android.R.style.Theme_Dialog);
		setContentView(R.layout.flashcardlist);
		
		//Message表示
		msgBoxView();
		//Component生成
		findResource();
		//配置・リスナー設定
		initComponents();
		
		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		
		super.onCreateContextMenu(menu, view, menuInfo);
		
		AdapterContextMenuInfo contextMenuInfo = (AdapterContextMenuInfo)  menuInfo;
		
		ListView listView = (ListView) view;
		// クリックされたアイテムを取得します
		File item =  (File) listView.getItemAtPosition(contextMenuInfo.position);
		
		menu.setHeaderTitle(item.getName());
		menu.add(0, CONTEXT_ITEM_EDIT, 0, getString(R.string.contextMenuEdit));
		menu.add(0, CONTEXT_ITEM_DELETE, 0, getString(R.string.contextMenuDelete));
    	
		
	}
	//メニューのアイテムが選択された際に起動される
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	
    	AdapterContextMenuInfo contextMenuInfo = (AdapterContextMenuInfo)item.getMenuInfo();
    	
    	ListView listView = (ListView)contextMenuInfo.targetView.getParent();
    	
    	final File targetFile =  (File) listView.getItemAtPosition(contextMenuInfo.position);

    	switch (item.getItemId()) {
		case CONTEXT_ITEM_EDIT:
			
			filenameEdit(targetFile, targetFile.getName());
			break;
		case CONTEXT_ITEM_DELETE:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.deleteConfirmMsgTitle));
			builder.setPositiveButton(getString(R.string.deleteConfirmYes),new android.content.DialogInterface.OnClickListener() {
		        public void onClick(android.content.DialogInterface dialog,int whichButton) {
		        	FileUtil.imageInfoDelete(targetFile, true);
		        	if (targetFile.delete()) {
		        		//Component生成
						//findResource();
						//配置・リスナー設定
						initComponents();
		        		Toast.makeText(AFlashcardList.this, getString(R.string.SuccessMsg), Toast.LENGTH_LONG).show();
		        	} else {
		        		Toast.makeText(AFlashcardList.this, getString(R.string.deleteFailMsg), Toast.LENGTH_LONG).show();
		        	}
		        }
		    });
			builder.setNegativeButton(getString(R.string.deleteConfirmNo),new android.content.DialogInterface.OnClickListener() {
		        public void onClick(android.content.DialogInterface dialog,int whichButton) {
		           
		        }
		    });
			builder.create();
	    	builder.show();
			break;
		default:
			break;
		}
    	

    	return true;
    }

	
	private void filenameEdit(final File targetFile, String newName) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.editConfirmMsgTitle));
		final EditText edtInput = new EditText(this);
		edtInput.setSingleLine(true);
		edtInput.setText(newName);
		builder.setView(edtInput);
		builder.setPositiveButton(getString(R.string.editConfirmYes),new android.content.DialogInterface.OnClickListener() {
	        public void onClick(android.content.DialogInterface dialog,int whichButton) {
	        	
	        	String newFileName = edtInput.getEditableText().toString().trim();
	        	final File newFile = new File(FileUtil.DEFAULT_DIR + newFileName);
	        	if (targetFile.getName().equals(newFileName)) {
	        		Toast.makeText(AFlashcardList.this, getString(R.string.SuccessMsg), Toast.LENGTH_LONG).show();
	        		return;
	        	}
	        	
	        	if (newFileName.indexOf("\\") >= 0 || newFileName.indexOf("/") >= 0) {
	        		final AlertDialog.Builder alertDialog = new AlertDialog.Builder(AFlashcardList.this);
	        		alertDialog.setTitle(R.string.renameError);
            		alertDialog.setMessage(R.string.renameErrorMsg2);
            		alertDialog.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            			public void onClick(DialogInterface dialog, int whichButton) {
            				filenameEdit(targetFile, edtInput.getEditableText().toString());
            			}
            		});
            		alertDialog.create();
            		alertDialog.show();
            		return;
	        	}
	        	
	        	if (newFile.exists()) {
	        		final AlertDialog.Builder alertDialog = new AlertDialog.Builder(AFlashcardList.this);
	        		alertDialog.setTitle(R.string.renameError);
            		alertDialog.setMessage(R.string.renameErrorMsg1);
            		alertDialog.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            			public void onClick(DialogInterface dialog, int whichButton) {
            				filenameEdit(targetFile, edtInput.getEditableText().toString());
            			}
            		});
            		alertDialog.create();
            		alertDialog.show();
            		return;
	        	}
	        	if (targetFile.renameTo(newFile)) {
	        		initComponents();
	        		Toast.makeText(AFlashcardList.this, getString(R.string.SuccessMsg), Toast.LENGTH_LONG).show();
	        	} else {
	        		final AlertDialog.Builder alertDialog = new AlertDialog.Builder(AFlashcardList.this);
	        		alertDialog.setTitle(R.string.renameError);
            		alertDialog.setMessage(R.string.renameErrorMsg2);
            		alertDialog.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            			public void onClick(DialogInterface dialog, int whichButton) {
            				filenameEdit(targetFile, edtInput.getEditableText().toString());
            			}
            		});
            		alertDialog.create();
            		alertDialog.show();
	        	}
	        }
		});
		builder.setNegativeButton(getString(R.string.editConfirmNo),new android.content.DialogInterface.OnClickListener() {
	        public void onClick(android.content.DialogInterface dialog,int whichButton) {
	           
	        }
	    });
		builder.create();
    	builder.show();
		
	
	}

	@Override  
	protected void onActivityResult(int requestCode, int resultCode,Intent data){  
		
		initComponents();
	} 
	@Override
	protected void onPause() {
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void msgBoxView() {
		
		final String nowMemorizerInfo = getSharedPreferences(AFlashCardUtil.PREFS_FILE_NAME, MODE_PRIVATE).getString("nowMemorizerInfo", "");
		File file = new File(nowMemorizerInfo);
		if (nowMemorizerInfo.length() != 0 && file.exists()) {
			new AlertDialog.Builder(this)
			.setTitle(file.getName() + " " + getString(R.string.listStartMsgTitle))
			.setMessage(getString(R.string.listStartMsgContent))
			.setPositiveButton(getString(R.string.listStartMsgYes), new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int whichButton) {
			    	// インテントへのインスタンス生成  
			    	Intent intent = new Intent(AFlashcardList.this, AFlashcardMain.class);
			        /* ここにYESの処理 */
			    	// サブ画面(インテント)にメイン画面 EditText01 テキストを送信  
			    	intent.putExtra("DATA_FILE_PATH", nowMemorizerInfo);  
			    	           
			    	// サブ画面(インテント)の起動  
			    	startActivityForResult(intent, SHOW_MEMORIZER_ACTIVITY);  
			    	
			    }
			})
			.setNegativeButton(getString(R.string.listStartMsgNo), new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int whichButton) {
			        /* ここにNOの処理 */
			    	return;
			    }
			})
			.show();
		}
		
		
	}
	
	private void findResource() {
		_metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(_metrics);
		
		
		_memorizerNameList = (ListView) findViewById(R.id.memorizerlistview);
		
		_importBtn = (ImageButton) findViewById(R.id.listimportBtn);
		_webopenBtn = (ImageButton) findViewById(R.id.webopenBtn);
		_emptyView = (TextView) findViewById(R.id.empty);
		
		_title = (TextView) findViewById(R.id.title);
		
	}

	

	private void initComponents() {
		final String nowMemorizerInfo = getSharedPreferences(AFlashCardUtil.PREFS_FILE_NAME, MODE_PRIVATE).getString("nowMemorizerInfo", "");
		File file = new File(nowMemorizerInfo);
		
		if (file.exists()) {
			_title.setText(file.getName());
		} else {
			_title.setText("");
		}
		
		_importBtn.setOnClickListener(importBtnListener);
		_webopenBtn.setOnClickListener(webopenBtnListener);
		List<File> memorizerGroupList = FileUtil.getMemorizerGroupList();
		
		MemorizerListAdapter adapter = new MemorizerListAdapter(this, memorizerGroupList);
		_memorizerNameList.setAdapter(adapter);
		_memorizerNameList.setOnItemClickListener(listClickListener);
		_memorizerNameList.setEmptyView(_emptyView);
		registerForContextMenu(_memorizerNameList);
	}

	/**
	 * ArrayAdapterを拡張したクラス。Hashmap一覧表示させている。
	 * 
	 * @author hyoromo
	 */
	private class MemorizerListAdapter extends BaseAdapter {

		private LayoutInflater layoutInflater = null;
		private List<File> memorizerList = null;

		public MemorizerListAdapter(Context context, List<File> list) {
			super();
			layoutInflater = LayoutInflater.from(context);
			memorizerList = list;
		}

		/**
		 * 画面に表示される毎に呼び出される
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = layoutInflater.inflate(R.layout.flashcardlistrow, null);
			TextView listName = (TextView)  convertView.findViewById(R.id.listname);
			listName.setText(memorizerList.get(position).getName());
			
			return convertView;
		}


		public int getCount() {
			return memorizerList.size();
		}

		public Object getItem(int position) {
			return memorizerList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}
	}
}
