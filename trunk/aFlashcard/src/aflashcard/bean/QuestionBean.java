package aflashcard.bean;

import java.io.Serializable;

import aflashcard.util.FileUtil;


public class QuestionBean implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
//	// テーブル名
//    public static final String TABLE_NAME = "memorize_list";
//
//	// カラム名
//    public static final String COLUMN_ID = "id";
//    public static final String COLUMN_SIGN = "person_name";
//    public static final String COLUMN_PROBLEM = "problem";
//    public static final String COLUMN_ANSWER = "answer";

    private int index = 0;
	
	private String memorySign = "0";
	private String quizSign = "0";
	private String question = "";
	private String answer = "";
	
	private String[] answers = null;
//	private String questionImagePath = "";
//	private String answerImagePath = "";
	

	public QuestionBean() {}
	
//	public String getQuestionImagePath() {
//		return questionImagePath;
//	}
//
//	public void setQuestionImagePath(String imagePath) {
//		this.questionImagePath = imagePath;
//	}
//	public String getAnswerImagePath() {
//		return answerImagePath;
//	}
//
//	public void setAnswerImagePath(String answerImagePath) {
//		this.answerImagePath = answerImagePath;
//	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}

	public String getMemorySign() {
		return memorySign;
	}
	public void setMemorySign(String memorySign) {
		this.memorySign = memorySign;
	}
	public String getQuizSign() {
		return quizSign;
	}
	public void setQuizSign(String quizSign) {
		this.quizSign = quizSign;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public String[] getAnswers() {
		return answers;
	}

	public void setAnswers(String[] answers) {
		this.answers = answers;
	}
	public void setValue(int i, String value, boolean displayOption) {
		switch (i) {
		case 0 : 
			this.setMemorySign(value);
			break;
		case 1 : 
			this.setQuizSign(value);
			break;
		case 2 : 
			if (displayOption) {
				this.setQuestion(FileUtil.getRemoveTag(value));
			} else {
				this.setAnswer(FileUtil.getRemoveTag(value));
				this.setAnswers(FileUtil.getAnswerList(value));
			}
			break;
		case 3 : 
			if (displayOption) {
				this.setAnswer(FileUtil.getRemoveTag(value));
				this.setAnswers(FileUtil.getAnswerList(value));
			} else {
				this.setQuestion(FileUtil.getRemoveTag(value));
			}
			
			break;

		}	
	}

	
}
