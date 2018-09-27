package it.polito.elite.teaching.cv;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class realFP {

	//private static final String DEST_FOLDER = "C:\\WORK\\wamp\\www\\cloudtimeroot\\trunk\\FalconMobile\\FalconIdenty\\app\\src\\main\\assets\\test\\out\\";
	private static final String DEST_FOLDER = "D:\\Download\\temp\\out\\";
	private static final String DEST_JPG = DEST_FOLDER + "test1";
	private static final String ORI_FOLDER = "C:\\WORK\\wamp\\www\\cloudtimeroot\\trunk\\FalconMobile\\FalconIdenty\\app\\src\\main\\assets\\test\\";
	private static final String ORI_FOLDER_ND = "D:\\work\\tensorflow\\fingerprintmask\\object_detection\\models\\model\\images\\";

	private static final boolean DEBUG = true;

	public static void main(String[] args) throws IOException {

		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		if( !DEBUG )	{
			File actual = new File(ORI_FOLDER_ND);
	        for( File f : actual.listFiles()){
	            if(f.isFile()) {
	            	if( f.getName().replaceAll(".*\\.", "").equals("jpg") ) {
	                    System.out.println( f.getName() );
	            		buildOptimizeRealFP(f, 0,null);
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
			buildOptimizeRealFP(new File(ORI_FOLDER_ND + "fp_87.jpg"), 0, di);
		}
		
	}

	public static void buildOptimizeRealFP( File input, int mode, IDebugImage di ) throws IOException {
		BufferedImage image = ImageIO.read(input);	

        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);       
	
		//Mat mat4 = CCFingerMask.optRealFingerP(mat, mode, di);
		Mat mat4 = ProcessActivity.optRealFingerP(mat, mode, di);
		
		di.writeMat(input.getName(),mat4);
	}


	private static class LocalDebugImage implements IDebugImage {

		@Override
		public void writeMat2(String filename, Mat mat1) {
			//Mat mat1 = new Mat(mat2.rows(),mat2.cols(), CvType.CV_8UC1);
			///Imgproc.cvtColor(mat, mat1, Imgproc.COLOR_RGB2GRAY);
			writeMat(filename,mat1);
		}

		@Override
		public void writeMat(String fileName, Mat mat) {
			Mat mat1 = mat.clone();
			if (mat1.channels() != 1) {				
				Imgproc.cvtColor(mat1, mat1, Imgproc.COLOR_RGB2GRAY);
			}
			if (CvType.depth(mat1.type()) == CvType.CV_32F) {
				mat1.convertTo(mat1, CvType.CV_8UC1);
			}
			byte[] data1 = new byte[mat1.rows() * mat1.cols() * (int)(mat1.elemSize())];
	        mat1.get(0, 0, data1);
			BufferedImage image1 = new BufferedImage(mat1.cols(),mat1.rows(), BufferedImage.TYPE_BYTE_GRAY);
			image1.getRaster().setDataElements(0, 0, mat1.cols(), mat1.rows(), data1);

			File ouptut = new File(DEST_FOLDER + fileName + ".bmp");
			try {
				ImageIO.write(image1, "bmp", ouptut);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		@Override
		public void log(String tag,String s) {
			System.out.println(s);
		}
	}
	

}
