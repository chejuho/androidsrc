package aflashcard.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import aflashcard.bean.QuestionBean;



public class AFlashCardUtil {
	
	public final static int MODE_MEMORIZE = 0;
	public final static int MODE_QUIZ = 1;
	public final static int DATA_STORE_METHOD_SDCARD = 0;
	public final static int DATA_STORE_METHOD_DB = 1;
	public final static String PREFS_FILE_NAME = "memorizer";
	public static Random random = new Random();
	
	
	
	public AFlashCardUtil()  {
		
	}
	
	/**
	 * まだクリアされないリストを取得
	 * @param originalList
	 * @param mode
	 * @return
	 */
	public List<QuestionBean> getDisplayMemorizeList(List<QuestionBean> originalList, int mode) {
		
		List<QuestionBean> memorizeList = new ArrayList<QuestionBean>();
		if (mode == MODE_MEMORIZE) {
			memorizeList = getMemorizeList(originalList);
		} else if (mode == MODE_QUIZ) {
			memorizeList = getQuizList(originalList);
		}
		return memorizeList;
		
	}
	
	/**
	 * 
	 * @param originalList
	 * @return
	 */
	private List<QuestionBean> getMemorizeList(List<QuestionBean> originalList) {
		
		List<QuestionBean> memorizeList = new ArrayList<QuestionBean>();
		
		for (int i = 0; i < originalList.size(); i++) {
        	if ("0".equals(originalList.get(i).getMemorySign())) {
        		QuestionBean info = new QuestionBean();
        		info.setIndex(i);
        		memorizeList.add(info);
        	}
        }
		return memorizeList;
		
	}
	/**
	 * 
	 * @param originalList
	 * @return
	 */
	private List<QuestionBean> getQuizList(List<QuestionBean> originalList) {
		
		List<QuestionBean> memorizeList = new ArrayList<QuestionBean>();
		
		for (int i = 0; i < originalList.size(); i++) {
        	if ("0".equals(originalList.get(i).getQuizSign())) {
        		QuestionBean info = new QuestionBean();
        		info.setIndex(i);
        		memorizeList.add(info);
        	}
        }
		return memorizeList;
		
	}
	public void shuffle(List<QuestionBean> list) {
		Collections.shuffle(list, random);
	}
	
}
