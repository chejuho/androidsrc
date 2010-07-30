package aflashcard.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import aflashcard.util.FileUtil;
import aflashcard.util.ImportFileManagement;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ImportList extends Activity {

	private DisplayMetrics _metrics = null;
	private ListView _importList = null;
	private ImportListAdapter _importListAdapter = null;
	
	private ImportFileManagement _fileManagement = null;
	
	private LinearLayout _listPartTop = null;
	
	private ImageButton _backListBtn = null;
	private ImageButton _sdcardhomeBtn = null;
	private final static int SHOW_IMPORTLIST_ACTIVITY = 2;
	
	private final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;

	private List<String> _pathList = null;
	/**
	 * Settingボタンを押した場合の処理を行う
	 */
	private View.OnClickListener backListBtnListener = new View.OnClickListener() {
		
		public void onClick(View view) {
			Intent intent = new Intent(); 
			// メイン画面(インテント)に返却コードを送信  
			setResult(RESULT_OK, intent);  
			// 終了  
			finish();  
		}
	};
	
	
	private View.OnClickListener sdCardHomeBtnListener = new View.OnClickListener() {
		public void onClick(View view) {
			int size = _pathList.size() - 2;
			for (int i = 0; i < size; i++) {
	    		_pathList.remove(2);
	    		_listPartTop.removeViewAt(2);
	    	}
			_fileManagement.goHome();
	    	_importListAdapter.notifyDataSetChanged();
	    	_listPartTop.invalidate();
		}
	};

	private DialogInterface.OnClickListener importSuccessListener = new DialogInterface.OnClickListener() {
	     public void onClick(DialogInterface dialog, int whichButton) {
	         /* ここにNOの処理 */
	    }
	 };
	private View.OnClickListener pathGoBtnListener = new View.OnClickListener() {
		public void onClick(View v) {
	    	File btnPath = (File) v.getTag();
	    	int i = 0;
	    	for (String path : _pathList) {
	    		if (path.equals(btnPath.getAbsolutePath())) {
	    			break;
	    		}
	    		i++;
	    	}
	    	int size = _pathList.size();
	    	for (int k = i + 1,j = i + 1; j < size; j++) {
	    		_pathList.remove(k);
	    		_listPartTop.removeViewAt(k);
	    	}
	    	_fileManagement.goPath(btnPath);
	    	_importListAdapter.notifyDataSetChanged();
	    	_listPartTop.invalidate();
	    }
	};
	
	private AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {

		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			
			ListView listView = (ListView) parent;
			final File item = (File) listView.getItemAtPosition(position);
			
			if (!item.isDirectory()) {
				return true;
			}
			
			File[] files = item.listFiles();
			 
			int size = files.length;
		 		for (int i = 0; i < size; i++) {
		 			File importFile = files[i];
		 			if (importFile.isFile()) {
						if (isCanImport(importFile)) {
							if (i == 0) {
				 				importExecute(importFile, "", false);
				 			} else {
				 				importExecute(importFile, "", true);
				 			}
 						}
					}
		 		}
			
			return true;
		}
		
	};
	private boolean isCanImport(File importFile) {
		if (importFile.getName().toLowerCase().endsWith(ImportFileManagement.FILTER_KEYWORD_txt) || 
				importFile.getName().toLowerCase().endsWith(ImportFileManagement.FILTER_KEYWORD_csv) || 
				importFile.getName().toLowerCase().endsWith(ImportFileManagement.FILTER_KEYWORD_xls)) {
			return true;
		}
		return false;
		
	}
	private AdapterView.OnItemClickListener listClickListener = new AdapterView.OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			 ListView listView = (ListView) parent;
		     // クリックされたアイテムを取得します
			 final File item = (File) listView.getItemAtPosition(position);
			 //../をクリックした場合
			 if (item == null) {
				 _fileManagement.goParentPath();
				 _importListAdapter.notifyDataSetChanged();
				 _listPartTop.removeViewAt(_pathList.size() - 1);
				 _pathList.remove(_pathList.size() - 1);
			 } else if (item.isDirectory()) {
				 Log.d("item.getAbsolutePath()", item.getAbsolutePath());
				 Button pathGo = new Button(ImportList.this);
				 pathGo.setTag(item);
				 pathGo.setText(item.getName());
				 pathGo.setOnClickListener(pathGoBtnListener);
				 _listPartTop.addView(pathGo, new LinearLayout.LayoutParams(WRAP_CONTENT, 54));
				 _pathList.add(item.getAbsolutePath());
				 _fileManagement.goPath(item);
				 _importListAdapter.notifyDataSetChanged();
			 } else {
				 
				 if (item.getName().toLowerCase().endsWith("zip")) {
						
					//OKボタンが押下された時に入力された年月をビューに設定する
		         	UnzipTask task = new UnzipTask();
		         	task.execute(item.getAbsolutePath(), FileUtil.SDCARD_DIR);
						
				 } else {
					 importExecute(item, "", false); 
				 }
			 }
		}
	};

		private void importExecute(final File item, String filename, final boolean repeatSign) {
			// Create EditText
			 final EditText  edtInput = new EditText(ImportList.this);
			 edtInput.setSingleLine(true);
			 
			 if (filename.length() == 0) {
				 edtInput.setText(getFileNmae(item.getName()));
			 } else {
				 edtInput.setText(filename);
			 }
			 
         	 
         		// Show Dialog
			 new AlertDialog.Builder(ImportList.this)
			 .setTitle(getString(R.string.import_confirmMsg))
			 .setView(edtInput)
			 .setPositiveButton(getString(R.string.import_confirmMsgYes), new DialogInterface.OnClickListener() {
				 public void onClick(DialogInterface dialog, int whichButton) {
					 final String name = edtInput.getText().toString();
					 	AlertDialog.Builder alertDialog = null;
					 	int result = _fileManagement.executeImport(item, name, true);
    	            	 
                		 if (result == ImportFileManagement.ERROR_INPUT) {
                			 
                			 alertDialog = makeInfoDialog(
                					 R.string.import_FailMsgTitle,
                					 R.string.import_FailMsg1, 
                					 new DialogInterface.OnClickListener() {
                						 public void onClick(DialogInterface dialog, int whichButton) {
                							 importExecute(item, name, repeatSign);
                						 }
                					 });
                			 alertDialog.show();
                			 return;
    	            		
    	            	 } else if (result == ImportFileManagement.ERROR_NAME_DUPLICATION) {
    	            		 
    	            		 alertDialog = makeTwoSelectDialog(
    	            				 R.string.import_FailMsg2, 
    	            				 R.string.import_OverWrite, 
    	            				 R.string.import_confirmMsgYes,
    	            				 R.string.import_confirmMsgNo, 
    	            				 new DialogInterface.OnClickListener() {
    	                			     public void onClick(DialogInterface dialog, int whichButton) {
    	                			    	 int reResult = _fileManagement.executeImport(item, name, false);
    	                			    	 AlertDialog.Builder reAlertDialog = null;
    	                			    	 if (reResult == ImportFileManagement.ERROR_FORMAT) {
    	                			    		 reAlertDialog = makeInfoDialog(R.string.import_FailMsgTitle, R.string.import_FailMsg3, null);
    	                			    		 reAlertDialog.show();
    	        		            		 } else if (reResult == ImportFileManagement.ERROR_FILENAME) {
    	        		            			 reAlertDialog = makeInfoDialog(
    	        		            					 R.string.import_FailMsgTitle,
    	        		            					 R.string.import_FailMsg4, 
    	        		            					 new DialogInterface.OnClickListener() {
    	        		            						 public void onClick(DialogInterface dialog, int whichButton) {
    	        		            							 importExecute(item, name, repeatSign);
    	        		            						 }
    	        		            					 });
    	        		            			 reAlertDialog.show();
    	        		            		 } else if (reResult == ImportFileManagement.ERROR_SYSTEM) {
    	        		            			 reAlertDialog = makeInfoDialog(R.string.import_FailMsgTitle, R.string.import_FailMsg5, null);
    	        		            			 reAlertDialog.show();
    	        		            		 } else {
    	        		            			 Log.d("item.getParent()", item.getParent());
    	        		            			 if (0 == item.getParent().indexOf("/sdcard/aFlashCard")) {
    	        		            				 item.delete();
    	        		            			 }
    	        		            			 if (!repeatSign) {
    	        		            				 reAlertDialog =  makeSuccessDisplayDialog(name);
    		        		            			 reAlertDialog.show();
    	        		            			 }
    	        		            			 
    	        		            		 }
    	                			     }
    	                			 }, 
    	                			 new DialogInterface.OnClickListener() {
    	                			     public void onClick(DialogInterface dialog, int whichButton) {
    	                			         /* ここにNOの処理 */
    	                			    }
    	                			 });
    	            		 alertDialog.show();
                		 } else if (result == ImportFileManagement.ERROR_FORMAT) {
                			 alertDialog = makeInfoDialog(R.string.import_FailMsgTitle, R.string.import_FailMsg3, null);
                			 alertDialog.show();
                		 } else if (result == ImportFileManagement.ERROR_FILENAME) {
                			 alertDialog = makeInfoDialog(
                					 R.string.import_FailMsgTitle,
                					 R.string.import_FailMsg4, 
                					 new DialogInterface.OnClickListener() {
                						 public void onClick(DialogInterface dialog, int whichButton) {
                							 importExecute(item, name, repeatSign);
                						 }
                					 });
                			 alertDialog.show();
                		 } else if (result == ImportFileManagement.ERROR_SYSTEM) {
                			 alertDialog = makeInfoDialog(R.string.import_FailMsgTitle, R.string.import_FailMsg5, null);
                			 alertDialog.show();
                		 } else {
                			 Log.d("item.getParent()", item.getParent());
	            			 if (0 == item.getParent().indexOf("/sdcard/aFlashCard")) {
	            				 item.delete();
	            			 }
                			 if (!repeatSign) {
                				 alertDialog = makeSuccessDisplayDialog(name);
                    			 alertDialog.show();
                			 }
                			
                			 
                		 }
                		 //alertDialog.show();
    	             }
    	         })
    	         .setNegativeButton(getString(R.string.import_confirmMsgNo), new DialogInterface.OnClickListener() {
    	             public void onClick(DialogInterface dialog, int whichButton) {
    	                 
    	                 File parentFile = item.getParentFile();
    	                 if (0 == item.getParent().indexOf("/sdcard/aFlashCard")) {
    	                	 FileUtil.imageInfoDelete(item, false);
            				 item.delete();
            			 }
    	                 if (parentFile.list().length == 0) {
    	 					parentFile.delete();
    	 				}
    	             }
    	         })
    	         .show();
			}
		
		
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTheme(android.R.style.Theme_Dialog);
		setContentView(R.layout.importlist);
		
		//Component生成
		findResource();
		//配置・リスナー設定
		initComponents();
		
		
	}
	
	
	@Override  
	protected void onActivityResult(int requestCode, int resultCode,Intent data){  
		if (requestCode == SHOW_IMPORTLIST_ACTIVITY) {  
			if (resultCode == RESULT_OK){  
				Intent intent = new Intent(); 
				// メイン画面(インテント)に返却コードを送信  
				setResult(RESULT_OK, intent);  
				// 終了  
				finish();
			}  
		} 
	} 
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	
	
	private void findResource() {
		_metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(_metrics);
		
		_listPartTop = (LinearLayout) findViewById(R.id.importdisplayPartTop);
		
		_importList = (ListView) findViewById(R.id.importlistview);
		List<File> importFileList = new ArrayList<File>();
		_fileManagement = new ImportFileManagement(importFileList);
		_importListAdapter = new ImportListAdapter(this, importFileList);
		
		//_importBtn = (ImageButton) findViewById(R.id.importStartBtn);
		_backListBtn = (ImageButton) findViewById(R.id.listBackBtn);
		_sdcardhomeBtn = (ImageButton) findViewById(R.id.sdcardhomeBtn);
		
		_pathList = new ArrayList<String>();
		
		_pathList.add("");
		_pathList.add(FileUtil.SDCARD);
	}

	

	private void initComponents() {
		
		
		_backListBtn.setOnClickListener(backListBtnListener);
		_importList.setAdapter(_importListAdapter);
		_importList.setOnItemClickListener(listClickListener);
		_importList.setOnItemLongClickListener(longClickListener);
		_sdcardhomeBtn.setOnClickListener(sdCardHomeBtnListener); 
		
	}

	private String getFileNmae(String name) {
		
		int extIndex = name.indexOf(".");
		
		if (extIndex < 0) {
			return name;
		} 
		return name.substring(0, extIndex);
		
		
	}
	
	private AlertDialog.Builder makeInfoDialog(int title, int msg, DialogInterface.OnClickListener listener) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle(title);
		alertDialog.setMessage(msg);
		alertDialog.setPositiveButton("OK", listener);
		
		return alertDialog;
	}
	
	private AlertDialog.Builder makeTwoSelectDialog(
			int title,
			int msg, 
			int positiveDis,
			int negativeDis,
			DialogInterface.OnClickListener positiveListener,
			DialogInterface.OnClickListener negativeListener) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle(title);
		alertDialog.setMessage(msg);
		alertDialog.setPositiveButton(positiveDis, positiveListener);
		alertDialog.setNegativeButton(negativeDis, negativeListener);
		return alertDialog;
	}

	private AlertDialog.Builder makeSuccessDisplayDialog(
			final String name) {
		
		final String[] menulist = ImportList.this.getResources().getStringArray(R.array.importSuccessListMenu);
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle(R.string.import_SuccessMsgTitle);
		alertDialog.setItems(menulist, new DialogInterface.OnClickListener() {
			 public void onClick(DialogInterface dialog, int whichButton) {
				 if (whichButton == 1){
					// インテントへのインスタンス生成  
			     	Intent intent = new Intent(ImportList.this, AFlashcardMain.class);
			     	File storeFile = new File(FileUtil.DEFAULT_DIR, name);
			     	intent.putExtra("DATA_FILE_PATH", storeFile.getAbsolutePath()); 
			     	// サブ画面(インテント)の起動  
			     	startActivityForResult(intent, SHOW_IMPORTLIST_ACTIVITY);  
			     	finish();
				 } else if (whichButton == 2){
					Intent intent = new Intent(); 
         			// メイン画面(インテント)に返却コードを送信  
         			setResult(RESULT_OK, intent);  
         			// 終了  
         			finish();
				 }
                    	
			 }
		 });
		return alertDialog;
	}
	
	 public class UnzipTask extends AsyncTask<String, Integer, List<File>> {  
		 
		 // 処理中ダイアログ
	     private ProgressDialog progressDialog = null;

		 // コンストラクタ  
		 public UnzipTask() {    	
		 }  
		 
		 @Override
		 protected void onPreExecute() {
			 // バックグラウンドの処理前にUIスレッドでダイアログ表示
			 progressDialog = new ProgressDialog(ImportList.this);
			 progressDialog.setMessage(getString(R.string.processMsg));
			 progressDialog.setIndeterminate(true);
			 progressDialog.show();
		 }
		 
		 // バックグラウンドで実行する処理  
		 @Override  
		 protected List<File> doInBackground(String... params) {  
			 List<File> upzipFileList = null; 
			 try {
				 upzipFileList = FileUtil.unZip(params[0], params[1]);
			} catch (IOException e) {
				e.printStackTrace();
				progressDialog.dismiss();
				if (upzipFileList != null) {
					FileUtil.deleteFileList(upzipFileList);
				}
				makeInfoDialog(R.string.import_FailMsgTitle, R.string.import_FailMsg5, null);
			}
			return upzipFileList;
		 }  
		 
		 @Override
		 protected void onPostExecute(List<File> upzipFileList) {
			 // 処理中ダイアログをクローズ
			 progressDialog.dismiss();
			 final List<File> importFileList = new ArrayList<File>();
			 for (File unzipFile: upzipFileList) {
					if (unzipFile.isFile()) {
						if (unzipFile.getName().toLowerCase().endsWith(ImportFileManagement.FILTER_KEYWORD_txt) || 
								unzipFile.getName().toLowerCase().endsWith(ImportFileManagement.FILTER_KEYWORD_csv)) {
							importFileList.add(unzipFile);
						}
					}
				}
				if (importFileList.size() > 0) {
					
					int size = importFileList.size();
				 		for (int i = 0; i < size; i++) {
				 			File importFile = importFileList.get(i);
				 			if (i == 0) {
				 				importExecute(importFile, "", false);
				 			} else {
				 				importExecute(importFile, "", true);
				 			}
				 		}
				 		
				} else {
					makeInfoDialog(R.string.import_FailMsgTitle, R.string.importFilenotFound, null).show();
				}
        }
	 }  
	/**
	 * ArrayAdapterを拡張したクラス。Hashmap一覧表示させている。
	 * 
	 * @author hyoromo
	 */
	private class ImportListAdapter extends BaseAdapter {

		private LayoutInflater layoutInflater = null;
		private List<File> importList = null;

		public ImportListAdapter(Context context, List<File> list) {
			super();
			layoutInflater = LayoutInflater.from(context);
			importList = list;
			
		}

		/**
		 * 画面に表示される毎に呼び出される
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = layoutInflater.inflate(R.layout.importrow, null);
			ImageView fileimageIcon = (ImageView)  convertView.findViewById(R.id.fileimageIcon);
			TextView textFileName = (TextView)  convertView.findViewById(R.id.textFileName);
			TextView textFileSize = (TextView)  convertView.findViewById(R.id.textFileSize);
			
			File file = importList.get(position);
			
			if (file == null) {
				textFileName.setText("../");
				textFileSize.setText("");
				
			} else {
				if (file.isDirectory()) {
					fileimageIcon.setImageResource(R.drawable.iconfolder);
					textFileName.setText(file.getName());
					textFileSize.setText("");
				} else {
					
					if (file.getName().toLowerCase().endsWith("zip")) {
						fileimageIcon.setImageResource(R.drawable.iconzip);
					} else if (file.getName().toLowerCase().endsWith("xls")) {
						fileimageIcon.setImageResource(R.drawable.iconexcel);
					} else {
						fileimageIcon.setImageResource(R.drawable.icontext);
					}
						
					
					textFileName.setText(file.getName());
					if (1 > file.length() / 1000) {
						textFileSize.setText("1KB");
					} else {
						textFileSize.setText(String.valueOf(file.length() / 1000) + "KB");
					}
					
				}
				
			}
			
			return convertView;
		}


		public int getCount() {
			return importList.size();
		}

		public Object getItem(int position) {
			return importList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}
	}
}
