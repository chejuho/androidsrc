package aflashcard.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.mozilla.universalchardet.UniversalDetector;

import aflashcard.bean.QuestionBean;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Environment;
import android.util.Log;

public class FileUtil {
	
	private static final String HEXINDEX = "0123456789abcdef          ABCDEF";
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public final static String DEFAULT_FILE = "\\data\\data\\aflashcard.activity\\files\\sample.txt";
	public final static String DEFAULT_DIR = "\\data\\data\\aflashcard.activity\\files\\";
	public final static String SDCARD_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aFlashcard/" ;
	public final static String IMPORT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/flashCard_import" ;
	public final static String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
	private final static String SEPARATION = "\\|";
	private static final String SEPARATE = "|";
	private static final String COMMA = "\\,";
	private static final String TAB = "\t";
	
	private static Comparator<File> fileSort = new Comparator<File>(){
		public int compare(File src, File target){
			int diff = src.getName().compareTo(target.getName());
			return diff;
			}
		};
	
	public static List<File> getMemorizerGroupList() {
		
		List<File> memorizerGroupList = new ArrayList<File>();
		File dataDir = new File(DEFAULT_DIR);
		
		if (!dataDir.exists()) {
			dataDir.mkdirs();
		}
		
		for (File file : dataDir.listFiles()) {
			
			if (file.isFile()) {
				memorizerGroupList.add(file);
			}
			
		}
		Collections.sort(memorizerGroupList, fileSort);
		return memorizerGroupList;
	}
	public static List<QuestionBean> fileToMemorizeInfoList(String filePath, boolean displayOption)
		throws IOException {

		File dataFile = new File(filePath);
		FileInputStream inputStream = new FileInputStream(dataFile);
		
		if (dataFile.getName().toLowerCase().endsWith("xls")) {
			return xlsFileStreamToMemorizeInfoList(inputStream);
		} else {
			String encoding = getEncoding(dataFile);
			return csvFileStreamToMemorizeInfoList(inputStream, displayOption, encoding);
		}
		
	}
	/**
	 * 
	 * @param list
	 * @param importFile
	 * @param newFilename
	 * @throws Exception 
	 */
	public static void resourcesProcess(List<QuestionBean> list, File importFile) {
		
		for (QuestionBean bean : list) {
			
			String questionNewImagePath = imageFileMove(bean.getQuestion(), importFile);
			String answerNewImagePath = imageFileMove(bean.getAnswer(), importFile);
			
			if (questionNewImagePath.length() != 0) {
				bean.setQuestion(questionNewImagePath);
			}
			if (answerNewImagePath.length() != 0) {
				bean.setAnswer(answerNewImagePath);
			}
			
			
		}
	}
	public static void deleteFileList(List<File> list) {
		if (list != null) {
			for (File file: list) {
				file.delete();
			}
		}
	}
	private static List<QuestionBean> xlsFileStreamToMemorizeInfoList(InputStream inputStream) throws IOException {
		
		List<QuestionBean> list = new ArrayList<QuestionBean>(); 
		try {
			POIFSFileSystem fs = new POIFSFileSystem(inputStream);
			HSSFWorkbook wb = new HSSFWorkbook(fs);
 
			HSSFSheet sheet = wb.getSheetAt(0);
			
			int firstRow = sheet.getFirstRowNum();
			int lastRow = sheet.getLastRowNum();
			//for rows
			for (int rowIdx = firstRow; rowIdx <= lastRow; rowIdx++) {

				HSSFRow row = sheet.getRow(rowIdx + 1);
				
				if (row == null)
					continue;

				short firstCell = row.getFirstCellNum();

				QuestionBean bean = new QuestionBean();
				
				for (short cellIdx = firstCell; cellIdx < 2; cellIdx++) {
					String data = null;

					HSSFCell cell = row.getCell(cellIdx);
					if (cell != null) {
						int type = cell.getCellType();
						switch (type) {
						case HSSFCell.CELL_TYPE_BOOLEAN:
							boolean bdata = cell.getBooleanCellValue();
							data = String.valueOf(bdata);
							break;
						case HSSFCell.CELL_TYPE_NUMERIC:
							double ddata = cell.getNumericCellValue();
							data = String.valueOf(ddata);
							break;
						case HSSFCell.CELL_TYPE_STRING:
							data = cell.getStringCellValue();
							break;
						case HSSFCell.CELL_TYPE_BLANK:
							data = cell.getStringCellValue();
							break;
						case HSSFCell.CELL_TYPE_ERROR:
						case HSSFCell.CELL_TYPE_FORMULA:
						default:
							continue;
						}
						
						if (cellIdx == 0) {
							bean.setQuestion(data.trim());
						} else if(cellIdx == 1) {
							bean.setAnswer(data.trim());
						} 
					}
				}
				list.add(bean);
			}
		} catch (IOException e) {
			throw e;
		}  finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
		
		return list;
	}
	
	
	private static String imageFileMove(String pathInfo, File importFile) {
		
		boolean isExists = false;
		String imagePath = getImagePath(pathInfo);
		
		if (imagePath.length() == 0) {
			return "";
		}
		
		File srcImagePath = new File(importFile.getParent() + FILE_SEPARATOR + imagePath);
		
		isExists = srcImagePath.exists();
		
		if (!isExists) {
			
			File searchFile = getFileSearch(new File(importFile.getParent()), srcImagePath.getName());
			if (searchFile != null) {
				srcImagePath = searchFile;
			} else {
				return "";
			}
		}
		File destFile = null;
		if (isExists) {
			destFile = new File(SDCARD_DIR, imagePath);
		} else {
			destFile = new File(SDCARD_DIR + "images/" + srcImagePath.getName());
			
			
		}
		
		if (!destFile.exists()) {
			if (0 == srcImagePath.getParent().indexOf("/sdcard/aFlashCard")) {
				destFile = srcImagePath;
			 } else {
				 destFile.getParentFile().mkdirs();
				 fileMoveProcess(srcImagePath, destFile);

			 }
		}

		
		return "*I" + destFile.getAbsolutePath()+ "I*";
	}
	
	private static File getFileSearch(File searchDir, String searchName) {
		
		
		File[] files = searchDir.listFiles();
		
		for (File file : files) {
			if (file.isDirectory()) {
				File searchFiles[] = file.listFiles();
				for (File searchFile : searchFiles) {
					if (searchFile.isDirectory()) {
						File search = getFileSearch(searchFile, searchName);
						if (search != null) {
							return search;
						}
					} else {
						if (searchName.equals(searchFile.getName())) {
							return searchFile;
						}
					}
					
				}
				
			}
		}
		
		return null;
		
	}
	public static boolean imageInfoDelete(File deleteFile, boolean absoluteSign) {
		
		boolean result = false;
		
		List<QuestionBean> list;
		try {
			list = fileToMemorizeInfoList(deleteFile.getAbsolutePath(), true);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		for (QuestionBean bean : list) {
			String questionImagePath = getImagePath(bean.getQuestion());
			String answerImagePath = getImagePath(bean.getAnswer());
			File parentFile = null;
			if (questionImagePath.length() != 0) {
				File questionSrcImagePath = null;
				if (absoluteSign) {
					questionSrcImagePath = new File(questionImagePath);
				} else {
					questionSrcImagePath = getSrcFile(deleteFile.getParentFile(), questionImagePath);
				}
				
				parentFile = questionSrcImagePath.getParentFile();
				result = questionSrcImagePath.delete();
				if (parentFile.listFiles() != null && parentFile.listFiles().length == 0) {
					parentFile.delete();
				}
				
			}
			if (answerImagePath.length() != 0) {
				File answerSrcImagePath = null;
				if (absoluteSign) {
					answerSrcImagePath = new File(answerImagePath);
				} else {
					answerSrcImagePath = getSrcFile(deleteFile.getParentFile(), answerImagePath);
				}
				
				parentFile = answerSrcImagePath.getParentFile();
				result = answerSrcImagePath.delete();
				if (parentFile.listFiles() != null && parentFile.listFiles().length == 0) {
					parentFile.delete();
				}
			}
		}
		return result;
	}
	private static File getSrcFile(File dir, String path) {
		
		File srcImagePath = new File(dir + FILE_SEPARATOR + path);
		boolean isExists = srcImagePath.exists();
		if (!isExists) {
			File searchFile = getFileSearch(dir, srcImagePath.getName());
			if (searchFile != null) {
				srcImagePath = searchFile;
			} 
		}
		return srcImagePath;
	}
	public static void memorizeInfoListToFile(String filePath, List<QuestionBean> saveList, boolean displayOption) throws IOException {

		FileOutputStream fos = null;
		try {
			File dataDir = new File(filePath);

			fos = new FileOutputStream(dataDir);

			for (QuestionBean save : saveList) {
				String write = "";
				if (displayOption) {
					write = save.getMemorySign() + SEPARATE + save.getQuizSign() + SEPARATE + save.getQuestion() + SEPARATE + save.getAnswer() + outputAnswers(save.getAnswers());
				} else {
					write = save.getMemorySign() + SEPARATE + save.getQuizSign() + SEPARATE + save.getAnswer() + outputAnswers(save.getAnswers()) + SEPARATE + save.getQuestion();
				}
				fos.write(write.getBytes("UTF-8"));
				fos.write("\r\n".getBytes("UTF-8"));
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (fos != null) {
					fos.flush();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
		}
	}
	
	public static List<QuestionBean> csvFileToMemorizeInfoList(File file, boolean displayOption)
			throws IOException {

		File dataDir = new File(DEFAULT_DIR + file.getPath(), file.getName());
		FileInputStream inputStream = null;
		if (dataDir.exists()) {
			inputStream = new FileInputStream(dataDir);
		} else {
			inputStream = new FileInputStream(file);
		}
		//FileInputStream inputStream = new FileInputStream(file);
		String encoding = getEncoding(file);
		return csvFileStreamToMemorizeInfoList(inputStream, displayOption, encoding);
	}

	public static void memorizeInfoListTocsvFile(File file, List<QuestionBean> saveList, boolean displayOption) throws IOException {

		FileOutputStream fos = null;
		try {
			String encoding = getEncoding(file);
			File dataDir = new File(DEFAULT_DIR + file.getPath());
			if (!dataDir.exists()) {
				dataDir.mkdirs();
			}
			
			fos = new FileOutputStream(new File(dataDir, file.getName()));

			for (QuestionBean save : saveList) {
				String write = "";
				if (displayOption) {
					write = save.getMemorySign() + SEPARATE + save.getQuizSign() + SEPARATE + save.getQuestion() + SEPARATE + save.getAnswer();
				} else {
					write = save.getMemorySign() + SEPARATE + save.getQuizSign() + SEPARATE + save.getAnswer() + SEPARATE + save.getQuestion();
				}
				fos.write(write.getBytes(encoding));
				fos.write("\r\n".getBytes(encoding));
				// fos.write(save.toCSVFormatString().getBytes("UTF-8"));
				// fos.write(save.toCSVFormatString().getBytes());
				// Log.d("MemorizeWordsActivity", save.toString());
				// fos.write("\r\n".getBytes("UTF-8"));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw e;
			
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				if (fos != null) {					
					fos.flush();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
		}
	}

	public static List<QuestionBean> defaultFileToMemorizeInfoList(boolean displayOption)
			throws IOException {
		
		FileInputStream inputStream = new FileInputStream(new File(DEFAULT_FILE));
		String encoding = getEncoding(new File(DEFAULT_FILE));
		return csvFileStreamToMemorizeInfoList(inputStream, displayOption, encoding);
	}

	public static void sampleFileCreate(Activity activity) throws IOException {
		BufferedReader reader = null;
		BufferedWriter writer = null;

		File sampleFile = new File(DEFAULT_FILE);

		if (sampleFile.exists()) {
			return;
		}
		File dir = new File(DEFAULT_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		try {
			// reader = new BufferedReader(new
			// InputStreamReader(activity.getAssets().open("sample.txt"),
			// "UTF-8"));
			// writer = new BufferedWriter(new OutputStreamWriter(new
			// FileOutputStream(sampleFile), "UTF-8"));
			reader = new BufferedReader(new InputStreamReader(activity.getAssets().open("sample.txt")));
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sampleFile)));

			for (String line = null; (line = reader.readLine()) != null;) {
				writer.write(line);
				writer.write("\r\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;

		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
		}
	}
	public static String getImagePath(String path) {
		String imagePath = "";
		String patternStr = "\\*I(.+)I\\*";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(path);
		if (matcher.find()) {
			imagePath = matcher.group(1);
		}
		return imagePath;
	}
	public static String getRemoveTag(String answer) {
		return answer.replaceAll("\\*A(.+)A\\*", "");
		
	}
	
	
	public static String[] getAnswerList(String answer) {
		String answerList = "";
		String patternStr = "\\*A(.+)A\\*";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(answer);
		if (matcher.find()) {
			answerList = matcher.group(1);
			return answerList.split(",");
		}
		return null;
		
		
	}
	
	public static String outputAnswers(String[] answers) {
		
		StringBuilder sb = new StringBuilder();
		
		if (answers != null) {
			sb.append("*A");
			for (String answer : answers) {
				sb.append(answer);
				sb.append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("A*");
			return sb.toString();
		}
		
		return "";
		
	}
	
	public static List<File> unZip(String zipfile, String unzipDir) throws IOException {
		Enumeration enumer = null;
		
		List<File> createFileList = new ArrayList<File>(); 
		ZipFile zf = null;
		
		try {
			zf = new ZipFile(zipfile, "UTF-8");
			enumer = zf.getEntries();

			while (enumer.hasMoreElements()) {
				ZipEntry target = (ZipEntry) enumer.nextElement();
				createFileList.add(saveEntry(target, unzipDir, zf));
				
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (ZipException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
		
		return createFileList;
	}
	

	

	private static File saveEntry(ZipEntry target, String root, ZipFile zf) throws ZipException,
			IOException {

		File file = null;
		try {
			
			file = new File(root, target.getName());
			if (target.isDirectory()) {
				file.mkdirs(); 
			} else {
				InputStream is = zf.getInputStream(target);
				BufferedInputStream bis = new BufferedInputStream(is);
				File dir = new File(file.getParent());
				dir.mkdirs();
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos);

				int c;
				byte buf[] = new byte[1024];
				while ((c = bis.read(buf, 0, 1024)) != -1) {
					bos.write(buf, 0, c);
				}
				bos.close();
				fos.close();
			}
		} catch (ZipException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
		return file;
	}
	private static List<QuestionBean> csvFileStreamToMemorizeInfoList(
			InputStream inputStream, boolean displayOption, String encoding) throws IOException {

		List<QuestionBean> lineList = new ArrayList<QuestionBean>();
		BufferedReader reader = null;
		boolean first = true;
		try {
			byte[] bom = new byte[3];          
			inputStream.read(bom, 0, 3);
			String firstStr = byteToHex(bom);
			 if (firstStr.equals("EFBBBF") ) {
	            	firstStr = "";
			 } 
			reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
			String line = null;
			int i = 0;
			while ((line = reader.readLine()) != null) {
				if (first) {
					line = new String(hexToByte(firstStr)) + line;
					first = false;
				} 
				QuestionBean bean = changeOnlineTOBean(line, displayOption);
				if (bean != null) {
					bean.setIndex(i);
					lineList.add(bean);
				}
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				throw e;

			}
		}
		return lineList;
	}
	
	
	private static String getEncoding(File file) {
		String encoding = null;
		UniversalDetector detector = new UniversalDetector(null);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			int nread;
			 byte[] buf = new byte[4096];
			while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
	           detector.handleData(buf, 0, nread);
			}
			 // (3)
	        detector.dataEnd();
	        // (4)
	        encoding = detector.getDetectedCharset();
	        //Log.d("getEncoding", encoding);
	        // (5)
	        detector.reset();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();

			}
		}
        if (encoding == null) {
        	encoding = "UTF-8";
        }
        
        return encoding;
	}
	/**
	 * 
	 * @param srcfile
	 * @param destfile
	 * @throws IOException
	 */
	private static void fileMoveProcess(File srcfile, File destfile)  {
		
		FileChannel srcChannel = null;
		FileChannel destChannel = null;
		try {
			srcChannel = new FileInputStream(srcfile).getChannel();
			destChannel = new FileOutputStream(destfile).getChannel();
			srcChannel.transferTo(0, srcChannel.size(), destChannel);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
	        try {
	        	if (srcChannel != null) {
	        		srcChannel.close();
	        	}
	        	if (destChannel != null) {
	        		destChannel.close();
	        	}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	    }
	}
	
	private static Bitmap fitImageNoMargin(Bitmap baseImage,int width,int height){
        
        Point pt=calculateFitImage(baseImage,width,height,null);//TODO gc free
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(baseImage,
                        pt.x, pt.y, true); 
        return resizedBitmap;
	}
	
	/**
     * 画像が枠に収まるようリサイズする場合のサイズを計算します
     * 
     * @param baseImage
     * @param width
     * @param height
     * @param receiver
     * @return
     */
	private static Point calculateFitImage(Bitmap baseImage,int width,int height,Point receiver){
            if(baseImage==null){
                    throw new RuntimeException("baseImage is null");
            }
            if(receiver==null){
                    receiver=new Point();
            }
            int dw=width;
            int dh=height;
            
            
            if(dw!=0 && dh!=0 ){
                    double waspect=(double)dw/baseImage.getWidth();
                    double haspect=(double)dh/baseImage.getHeight();
                    if(waspect>haspect){//fit h
                            dw=(int) (baseImage.getWidth()*haspect);
                            
                    }else{
                            dh=(int)(baseImage.getHeight()*waspect);
                    }
            }
            receiver.x=dw;
            receiver.y=dh;
            return receiver;
    }


	/**
	 * 
	 * @param line
	 * @return
	 */
	private static QuestionBean changeOnlineTOBean(String line, boolean displayOption) {

		QuestionBean bean = null;
		String separete = getSeparate(line);
		int commaSu = getInSeparateSize(line, separete);

		if (commaSu == 1 || commaSu == 2 || commaSu == 3) {
			bean = new QuestionBean();
			int i = 3 - commaSu;
			for (String data : line.split(separete)) {
				bean.setValue(i, data, displayOption);
				i++;
			}
//			String[] lineArray = line.split(SEPARATION);
//			for (int i = 3 - commaSu,j = 0; j < lineArray.length; i++, j++) {
//				bean.setValue(i, lineArray[j], displayOption);
//			}
		}
		return bean;
	}

	private static int getInSeparateSize(String line, String separete) {
		int totalSize = line.length();
		int afterRemoveSize = line.replaceAll(separete, "").length();

		return totalSize - afterRemoveSize;

	}
	
	private static String getSeparate(String line) {
		
		if (line.split(SEPARATION).length > 1) {
			return SEPARATION;
		} else if (line.split(TAB).length > 1) {
			return TAB;
		} else if (line.split(COMMA).length > 1) {
			return COMMA;
		}
		return SEPARATION;
	}
	private static byte[] hexToByte(String s) {
	       int l = s.length() / 2;
	       byte data[] = new byte[l];
	       int j = 0;

	       for (int i = 0; i < l; i++) {
	           char c = s.charAt(j++);
	           int n, b;

	           n = HEXINDEX.indexOf(c);
	           b = (n & 0xf) << 4;
	           c = s.charAt(j++);
	           n = HEXINDEX.indexOf(c);
	           b += (n & 0xf);
	           data[i] = (byte) b;
	       }
	       return data;
	}
	private static String byteToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for ( int i = 0; i < data.length;  i++) {
			buf.append(byteToHex(data[i]).toUpperCase());
		}
		return buf.toString();
	}
	private static String byteToHex(byte data) {
		StringBuffer buf = new StringBuffer();
		buf.append(toHexChar((data >>> 4) & 0x0F));
		buf.append(toHexChar(data & 0x0F));
		return buf.toString();
	}

	private static char toHexChar(int i) {
		if ((i >= 0) && (i <= 9)) {
			return (char) ('0' + i);
		} else {
			return (char) ('a' + (i -10));
		}	
	}

}
