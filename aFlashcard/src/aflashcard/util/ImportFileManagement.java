package aflashcard.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import aflashcard.bean.QuestionBean;
import android.os.Environment;

public class ImportFileManagement {
	
	private List<File> _list = null;
	private File nowPath = null;
	public final static int SUCCESS = 0;
	public final static int ERROR_INPUT = 1;
	public final static int ERROR_NAME_DUPLICATION = 2;
	public final static int ERROR_FORMAT = 3;
	public final static int ERROR_FILENAME = 4;
	public final static int ERROR_SYSTEM = 5;
	public final static int ERROR_SRCNOTFOUND = 6;
	
	public final static String FILTER_KEYWORD_txt = ".txt";
	public final static String FILTER_KEYWORD_csv = ".csv";
	public final static String FILTER_KEYWORD_zip = ".zip";
	public final static String FILTER_KEYWORD_xls = ".xls";
	
	private static Comparator<File> fileSort = new Comparator<File>(){
		public int compare(File src, File target){
				//int result = 0;
				if (src == null) {
					return 0;
				} else if (src.isFile()) {
					if (target == null) {
						return 1;
					} else if (target.isDirectory()) {
						return 1;
					} else {
						return src.getName().compareTo(target.getName());
					}
				} else {
					if (target == null) {
						return 1;
					} else if (target.isDirectory()) {
						return src.getName().compareTo(target.getName());
					} else {
						return -1;
					}
				}
			}
		};
	
	public ImportFileManagement(List<File> list) {
		
		_list = list;
		nowPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
		_list.addAll(getSubListFiles(nowPath));
		
	}
	
	public void goParentPath() {
		_list.clear();
		nowPath = new File(nowPath.getParent());
		_list.addAll(getSubListFiles(nowPath));
	}
	
	public void goPath(File file) {
		nowPath = file;
		_list.clear();
		_list.addAll(getSubListFiles(nowPath));
	}
	public void goHome() {
		nowPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
		_list.clear();
		_list.addAll(getSubListFiles(nowPath));
	}
	/**
	 * 
	 * @param file
	 * @param name
	 * @return 0 : 正常、1 : 入力チェック、　2 : ファイル重複エラー、3 : Formatエラー、4 :　FilenameError 、5:システムエラー
	 */
	public int executeImport(File file, String name, boolean duplcateCheck) {
		int result = SUCCESS;
		if (name == null || name.length() == 0) {
			result = ERROR_INPUT;
			return result;
		}
		
		if (name.indexOf("\\") >= 0) {
			result = ERROR_FILENAME;
			return result;
    	}
		
		//重複チェック
		if (duplcateCheck) {
			File dir = new File(FileUtil.DEFAULT_DIR);
			for (File naibuFile : dir.listFiles()) {
				if (naibuFile.getName().equals(name)) {
					result = ERROR_NAME_DUPLICATION;
					return result;
				}
			}
		}
		try {
			List<QuestionBean> list = FileUtil.fileToMemorizeInfoList(file.getAbsolutePath(), true);
			FileUtil.resourcesProcess(list, file);
			
			if (list.size() == 0) {
				result = ERROR_FORMAT;
				return result;
			}
			
			FileUtil.memorizeInfoListToFile(FileUtil.DEFAULT_DIR + name, list, true);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			result = ERROR_FILENAME;
			
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			result = ERROR_SYSTEM;
			
			return result;
		}
		
		return result;
		
	}
	
	
	private List<File> getSubListFiles(File now) {
		List<File> list = new ArrayList<File>();
		
		if (!now.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
			list.add(null);
		}
		
		for (File file : now.listFiles(new TextFileFilter())) {
			list.add(file);
		}
		Collections.sort(list, fileSort);
		return list;
	}
	
	
	/**
	 *  @author Administrator
	 *
	 */
	private class TextFileFilter implements FileFilter  {

		
		public boolean accept(File pathname) {
			
			//ディレクトリならばfalseを返却(リストに追加しない)
			if(pathname.isDirectory()){
				return true;
			} else if (pathname.getName().toLowerCase().endsWith(FILTER_KEYWORD_txt)) {
				return true;
			} else if (pathname.getName().toLowerCase().endsWith(FILTER_KEYWORD_csv)) {
				return true;
			} else if (pathname.getName().toLowerCase().endsWith(FILTER_KEYWORD_zip)) {
				return true;
			}else if (pathname.getName().toLowerCase().endsWith(FILTER_KEYWORD_xls)) {
				return true;
			}
			return false;
		}
	}
	
	
	

}
