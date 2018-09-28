package it.polito.elite.teaching.cv;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class resizeImage {

	private static final String ORI_FOLDER = "D:\\Download\\temp\\14\\entrada\\";	
	private static final boolean DEBUG = true;

	public static void main(String[] args) throws IOException {

		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		if( !DEBUG )	{
			File actual = new File(ORI_FOLDER);
	        for( File f : actual.listFiles()){
	            if(f.isFile()) {
	            	if( f.getName().replaceAll(".*\\.", "").equals("jpg") ) {
	                    System.out.println( f.getName() );
	            		//buildOptimizeRealFP(f, 0,null);
	            	}
	            }
	        }
		} else {
			IDebugImage di = new LocalDebugImage();
			//buildFingerMask(new File(ORI_FOLDER + "fp_59.jpg"), 1);
			//buildFingerMask(new File(ORI_FOLDER + "fp_64.jpg"), 1);
			//buildFingerMask(new File(ORI_FOLDER + "_before1532735116480.png.jpg"), 1);
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_33.jpg"), 1);
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_00.jpg"), 1);
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_08.jpg"), 1);
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_79.jpg"), 3);
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_01.jpg"), 3);
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_81.jpg"), 1, di);
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_82.jpg"), 0, di);
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_83.jpg"), 2, di);
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_12.jpg"), 0, di);
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_27.jpg"), 0, di);
			buildOptimizeRealFP("1", 0, di);
		}
		
	}

	private static void buildOptimizeRealFP(String name, int i, IDebugImage di) throws IOException {
		buildOptimizeRealFP(new File(ORI_FOLDER + name + "ref.jpg"),new File(ORI_FOLDER + name + "cc.jpg"), 0, di);		
	}

	public static void buildOptimizeRealFP( File refF, File ccF, int mode, IDebugImage di ) throws IOException {
		Mat refMat = getMat(refF);
		if( di != null )	di.writeMat2( "refMat", refMat);
		Mat ccMat = getMat(ccF);	
		
		Mat resizedFrame = CCProcess.resize(refMat, ccMat, mode, di);
		
		Mat mask = CCFingerMask.maskFingerP(refMat, mode, null);
		mask = CCProcess.trim(mask);
		if( di != null )	di.writeMat2( "maskRef", mask);
        
        int position = 0;
        Mat finalCC = new Mat();
        Mat maskCC1 = CCProcess.adjusReftMask( resizedFrame, mask, position++, di );
        resizedFrame.copyTo(finalCC, maskCC1);
        finalCC = CCProcess.trim(finalCC);
        //finalCC = CCProcess.adjusReftMask( refMat, finalCC, 2, di );
		if( di != null )	di.writeMat2( "finalCC1",finalCC);
		
		finalCC = new Mat();
        Mat maskCC2 = CCProcess.adjusReftMask( resizedFrame, mask, position++, di );
        resizedFrame.copyTo(finalCC, maskCC2);
        finalCC = CCProcess.trim(finalCC);
        //finalCC = CCProcess.adjusReftMask( refMat, finalCC, 2, di );
		if( di != null )	di.writeMat2( "finalCC2",finalCC);
		
		finalCC = new Mat();
        Mat maskCC3 = CCProcess.adjusReftMask( resizedFrame, mask, position++, di );
        resizedFrame.copyTo(finalCC, maskCC3);
        finalCC = CCProcess.trim(finalCC);
        //finalCC = CCProcess.adjusReftMask( refMat, finalCC, 2, di );
		if( di != null )	di.writeMat2( "finalCC3",finalCC);
		
		finalCC = new Mat();
        Mat maskCC4 = CCProcess.adjusReftMask( resizedFrame, mask, position++, di );
        resizedFrame.copyTo(finalCC, maskCC4);
        finalCC = CCProcess.trim(finalCC);
        //finalCC = CCProcess.adjusReftMask( refMat, finalCC, 2, di );
		if( di != null )	di.writeMat2( "finalCC4",finalCC);
		
	}

	private static Mat getMat(File refF) throws IOException {
		BufferedImage image = ImageIO.read(refF);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
	}


	

}
