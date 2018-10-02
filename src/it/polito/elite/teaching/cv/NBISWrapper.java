package it.polito.elite.teaching.cv;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;

import org.opencv.core.Mat;


public class NBISWrapper {
	private static final String TAG = "NBIS";
	private String ToolPath = "";

	public NBISWrapper() {
	}

	public void init( String localPath) {
		this.ToolPath = localPath;
	}

	public int getNFIQScore(String filePath) throws Exception {
		if (!this.isCommandExist("nfiq.exe")) {
			throw new FileNotFoundException("nfiq.exe is not found in this location");
		} else if (!this.isFileExist(filePath)) {
			throw new FileNotFoundException(filePath + " is not found in this location");
		} else {
			String command = "";
			command = command + this.ToolPath + "/" + "nfiq.exe";
			//command = command + " -raw ";
			//command = command + String.valueOf(RAWFileWidth) + "," + RAWFileHeight + "," + RAWFileDepth + " ";
			command = command + " " + filePath;
			String output = CmdLineExecutor.execCommand(command);
			output = output.replace("\n", "");

			try {
				return Integer.valueOf(output);
			} catch (Exception var8) {
				var8.printStackTrace();
				return 0;
			}
		}
	}

	public String createWSQ(String RAWFileWithPath, int RAWFileWidth, int RAWFileHeight, int RAWFileDepth, WSQCompression compression) throws Exception {
		AppUtils.logFM( null,TAG, RAWFileWithPath);
		if (!this.isCommandExist("cwsq.exe")) {
			throw new FileNotFoundException("cwsq.exe is not found in this location");
		} else {
			AppUtils.logFM( null,"WSQ_GENERATION", RAWFileWithPath);
			if (!this.isFileExist(RAWFileWithPath)) {
				throw new FileNotFoundException(RAWFileWithPath + " is not found in this location");
			} else {
				String nToolPath = this.ToolPath + "/" + "cwsq.exe";// + "_" + Calendar.getInstance().getTimeInMillis();
				//AppUtils.copyExecutableFile("cwsq", nToolPath, ac);
				String filepath = RAWFileWithPath.substring(0, RAWFileWithPath.lastIndexOf("."));
				String command = "";
				command = command + nToolPath;
				command = command + " " + compression.getCompression() + " wsq ";
				command = command + RAWFileWithPath + " -r ";
				command = command + String.valueOf(RAWFileWidth) + "," + RAWFileHeight + "," + RAWFileDepth + ",500";
				String output = CmdLineExecutor.execCommand(command);
				output = output.replace("\n", "");
				//AppUtils.removeExecutable(nToolPath);
				return filepath + ".wsq";
			}
		}
	}

	public String generateXYT(String WSQORJPEGFileWithPath, String FileNameToGenerate) throws Exception {
		if (!this.isCommandExist("mindtct.exe")) {
			throw new FileNotFoundException("mindtct.exe is not found in this location");
		} else if (!this.isFileExist(WSQORJPEGFileWithPath)) {
			throw new FileNotFoundException(WSQORJPEGFileWithPath + " is not found in this location");
		} else {
			String m1 = "";
			if( StaticConfig.USE_M1 ){
				m1 = " -m1 ";
			}
			String nToolPath = this.ToolPath + "/" + "mindtct.exe";// + "_" + Calendar.getInstance().getTimeInMillis();
			//AppUtils.copyExecutableFile("mindtct", nToolPath, ac.getApplicationContext());
			String command = "";
			command = command + nToolPath;
			command = command + m1 + " -b ";
			command = command + WSQORJPEGFileWithPath + " ";
			command = command + FileNameToGenerate;
			String res = CmdLineExecutor.execCommand(command);
			//AppUtils.removeExecutable(nToolPath);
			return res;
		}
	}

	public String generate8BitJPEG(String JPEGFileWithPath, String JPEG8BitFileWithPath) throws Exception {
		if (!this.isCommandExist("jpegtran.exe")) {
			throw new FileNotFoundException("jpegtran.exe is not found in this location");
		} else {
			String nToolPath = this.ToolPath + "/" + "jpegtran.exe";// + "_" + Calendar.getInstance().getTimeInMillis();
			//AppUtils.copyExecutableFile("jpegtran", nToolPath, ac);
			String command = "";
			command = command + nToolPath;
			command = command + " -grayscale ";
			command = command + " -outfile " + JPEG8BitFileWithPath;
			command = command + " " + JPEGFileWithPath;			 
			String res = CmdLineExecutor.execCommand(command);
			//AppUtils.removeExecutable(nToolPath);
			return res;
		}
	}

	public int bozorth(String XYTFile1WithPath, String XYTFile2WithPath) throws Exception {
		if (!this.isCommandExist("bozorth3.exe")) {
			throw new FileNotFoundException("bozorth3.exe is not found in this location");
		} else if (!this.isFileExist(XYTFile1WithPath)) {
			throw new FileNotFoundException(XYTFile1WithPath + " is not found in this location");
		} else if (!this.isFileExist(XYTFile2WithPath)) {
			throw new FileNotFoundException(XYTFile2WithPath + " is not found in this location");
		} else {
			String m1 = "";
			if( StaticConfig.USE_M1 ){
				m1 = "-m1 ";
			}
			String nToolPath = this.ToolPath + "/" + "bozorth3.exe";// + "_" + Calendar.getInstance().getTimeInMillis();
			//AppUtils.copyExecutableFile("bozorth3", nToolPath, ac);
			String command = "";
			command = command + nToolPath;
			command = command + " " + m1;
			command = command + XYTFile1WithPath + " ";
			command = command + XYTFile2WithPath;
			String output = CmdLineExecutor.execCommand(command);
			//AppUtils.removeExecutable(nToolPath);
			output = output.replace("\n", "");
			return Integer.valueOf(output);
		}
	}

	private boolean isCommandExist(String command) {
		File file = new File(this.ToolPath + "/" + command);
		return file.exists();
	}

	private boolean isFileExist(String FileWithPath) {
		File file = new File(FileWithPath);
		return file.exists();
	}

	public int obtainScore(File refF, Mat finalCC, String cc ) throws Exception {
		LocalDebugImage.s_writeMat( cc,finalCC, true );
		String baseFiltererdFilePath = StaticConfig.DEST_FOLDER + cc ;
		
		String xytRef = obtainXYT(refF.getAbsolutePath(), refF.getName().split("\\.(?=[^\\.]+$)")[0]);
		String xytCC = obtainXYT(baseFiltererdFilePath + ".jpg", cc);
		
		int score = this.bozorth( xytRef + ".xyt", xytCC + ".xyt");
		AppUtils.logFM(null, TAG, " ref=" + refF.getName() + " cc=" + cc + " score=" + score  );
		return score;
	}

	private String obtainXYT(String storedFiltererdFilePath, String name) throws Exception {
		String baseFiltererdFilePath = StaticConfig.DEST_FOLDER + name ;
		this.generate8BitJPEG(storedFiltererdFilePath, baseFiltererdFilePath  + "_gs.jpeg");
		int nfiqScore = this.getNFIQScore(baseFiltererdFilePath  + "_gs.jpeg");
		AppUtils.logFM(null, TAG, " ref=" + name + " nfiqScore=" + nfiqScore  );
		this.generateXYT( baseFiltererdFilePath + "_gs.jpeg", baseFiltererdFilePath);
		return baseFiltererdFilePath;
	}
}
