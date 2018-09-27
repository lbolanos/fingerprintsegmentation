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

public class fingerprint {

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
	            		buildFingerMask(f, 0,null);
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
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_03.jpg"), 0, di);
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_08.jpg"), 1);
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_79.jpg"), 3);
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_01.jpg"), 3);
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_81.jpg"), 1, di);
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_82.jpg"), 0, di);
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_83.jpg"), 2, di);
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_12.jpg"), 0, di);
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_27.jpg"), 0, di);
			//buildFingerMask(new File(ORI_FOLDER_ND + "fp_15.jpg"), 0, di);
			buildFingerMask(new File(ORI_FOLDER_ND + "fp_84.jpg"), 0, di);
		}
		
	}

	public static void buildFingerMask( File input, int mode, IDebugImage di ) throws IOException {
		BufferedImage image = ImageIO.read(input);	

        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);       
	
		Mat mask = CCFingerMask.maskFingerP(mat, mode, di);
		Mat mat4 = new Mat();
		mat.copyTo(mat4, mask);	
		if( di == null ) {
			di = new LocalDebugImage();
		}
		mat4 = CCFingerMask.enhanceFP(mat, mode, di);
		di.writeMat2(input.getName(),mat4);
	}


	private static class LocalDebugImage implements IDebugImage {

		@Override
		public void writeMat2(String filename, Mat mat2) {
			//Mat mat1 = new Mat(mat2.rows(),mat2.cols(), CvType.CV_8UC1);
			//Imgproc.cvtColor(mat2, mat1, Imgproc.COLOR_RGB2GRAY);
			writeMat(filename,mat2);
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
	
/*
	private static void buildFingerMask(File input, int mode ) throws IOException {
		if( mode > 4 ) {
			return;
		}
        BufferedImage image = ImageIO.read(input);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        
        Mat matInn = mat.clone();
        if( mode == 4 ) {
        	Mat imBina = new Mat();
        	Imgproc.threshold(matInn, imBina, 100, 255, Imgproc.THRESH_BINARY);
        	Imgproc.blur( imBina, imBina, new Size(6,6) );
        	if( DEBUG )	writeMat2(DEST_FOLDER + "imBina",imBina);
        	int border = 8;
        	Rect rectCrop = new Rect(border, border, matInn.cols() - (border*2), matInn.rows() - (border*2));
        	Mat croppedImage = new Mat(imBina, rectCrop);
        	if( DEBUG )	writeMat2(DEST_FOLDER + "croppedImage",croppedImage);
        	Scalar value = new Scalar( 255, 255, 255, 255 );
        	Mat dst = new Mat();
			Core.copyMakeBorder(croppedImage.clone(), dst , border, border, border, border, Core.BORDER_CONSTANT, value);
			matInn = dst.clone();
			if( DEBUG )	writeMat2(DEST_FOLDER + "copyMakeBorder3",matInn);			
        }

        Mat mat2 = doCanny(matInn, mode );
        if( DEBUG )	writeMat2(DEST_FOLDER + "doCanny",mat2);
        mat2 = doContour(mat2, mode );
        if( DEBUG )	writeMat(DEST_FOLDER + "doContour",mat2);
        Mat mask = null;
        mask = doFill(mat2, mode );
        if( mask == null ) {
        	if( !DEBUG ) {
        		buildFingerMask( input, mode + 1);            	
        	}
        	return;
        }
        long sizeInBytes = mask.total() * mask.elemSize();
        int countNonZero = Core.countNonZero(mask);
        long perct = (long) (sizeInBytes * 0.8);
        long perctD = (long) (sizeInBytes * 0.2);
        System.out.println( "countNonZero=" + countNonZero + " sizeInBytes=" + sizeInBytes + " perct=" + perct + " mode=" + mode);
        if( countNonZero < perctD || countNonZero > perct ) {
        	if( !DEBUG ) {
        		buildFingerMask( input, mode + 1);
            	return;
        	}
        }
        
        
        //Mat mask = Mat.zeros(mat.rows(), mat.cols(), CvType.CV_8UC1);
        //Imgproc.circle(mask, new Point(mat.cols()/2, mat.rows()/2), 
        //           Math.min(mat.rows(), mat.cols())/2, new Scalar(255), -1);
        if( DEBUG )	writeMat(DEST_JPG, mask);
        
        Mat mat4 = new Mat();
        mat.copyTo(mat4, mask);
        writeMat2(DEST_FOLDER + input.getName(),mat4);
	}
	
	private static void writeMat2(String filename, Mat mat2) throws IOException {
		Mat mat1 = new Mat(mat2.rows(),mat2.cols(),CvType.CV_8UC1);
        Imgproc.cvtColor(mat2, mat1, Imgproc.COLOR_RGB2GRAY);
		writeMat(filename,mat1);
	}
	

	private static Mat doFill1( Mat mat2, int mode  ) throws IOException {
		
		
		 // Set values below 220 to 255.
	    Mat im_th = new Mat();
	    Imgproc.threshold(mat2, im_th, 220, 255, Imgproc.THRESH_BINARY_INV);
	    //Imgproc.threshold(result, im_th, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
	    //Imgproc.adaptiveThreshold(result, im_th, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
	    if( DEBUG )	writeMat(DEST_FOLDER + "im_th1",im_th);
	    
		Size kernelSize = new Size(2, 2);
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, kernelSize);		
		Mat result = new Mat();
		int iterations = 2;
		Imgproc.morphologyEx(im_th, result, Imgproc.MORPH_ERODE, kernel, new Point(-1,-1), iterations);
		if( DEBUG )	writeMat(DEST_FOLDER + "result1",result);

	    
	     
	    //Mask used to flood filling.
	    //Notice the size needs to be 2 pixels than the image.
	    ///h, w = im_th.shape[:2]
	    //mask = np.zeros((h+2, w+2), np.uint8)
	    Mat mask = Mat.zeros(mat2.rows() + 2, mat2.cols()+ 2, CvType.CV_8UC1);
	    
	    // Flood fill from point (0, 0)
	    Mat im_floodfill = result.clone();
	    Imgproc.floodFill(im_floodfill, mask, new Point(0,0), new Scalar(0));
	    Imgproc.floodFill(im_floodfill, mask, new Point(0, mat2.rows() - 3), new Scalar(0));
	    Imgproc.floodFill(im_floodfill, mask, new Point(mat2.cols() - 3, 0), new Scalar(0));
	    Imgproc.floodFill(im_floodfill, mask, new Point(mat2.cols() - 3 ,mat2.rows() - 3), new Scalar(0));
	    return im_floodfill;
	    
	    //writeMat(DEST_FOLDER + "im_floodfill",im_floodfill);
	     /*
	    
	    // Invert flood filled image
	    Mat im_floodfill_inv = new Mat();
	    Core.bitwise_not(im_floodfill, im_floodfill_inv);
	    //writeMat(DEST_FOLDER + "im_floodfill_inv",im_floodfill_inv);
	    return im_floodfill_inv;
	    
	    /*
	    // Combine the two images to get the foreground.
	    //Mat im_out = (im_th + im_floodfill_inv);
	    Mat dest = new Mat();
	    im_th.copyTo(dest, im_floodfill_inv);
		return dest;
		*/
/*	}

	private static Mat doFill( Mat mat2, int mode  ) throws IOException {
		
		
		 // Set values below 220 to 255.
	    Mat im_th = new Mat();
	    Imgproc.threshold(mat2, im_th, 127, 255, Imgproc.THRESH_BINARY_INV);
	    //Imgproc.threshold(result, im_th, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
	    //Imgproc.adaptiveThreshold(result, im_th, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
	    if( DEBUG )	writeMat(DEST_FOLDER + "im_th",im_th);
	    
	    
		Size kernelSize = new Size(2, 2);
        if( mode == 3 ) {
        	kernelSize = new Size(4, 4);
        }
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kernelSize);		
		Mat result = new Mat();
		int iterations = 1;
		/*
		Imgproc.morphologyEx(im_th, result, Imgproc.MORPH_ERODE, kernel, new Point(-1,-1), iterations);
	    */
/*	    Mat erosion = new Mat();
	    Imgproc.erode(im_th,erosion, kernel,new Point(-1,-1),iterations); //refines all edges in the binary image

	    if( DEBUG )	writeMat(DEST_FOLDER + "erosion",erosion);
	    Mat opening = new Mat();
		Imgproc.morphologyEx(erosion, opening, Imgproc.MORPH_OPEN, kernel);
		if( DEBUG )	writeMat(DEST_FOLDER + "opening",opening);
		Imgproc.morphologyEx(opening, result, Imgproc.MORPH_CLOSE, kernel); //this is for further removing small noises and holes in the image
		if( DEBUG )	writeMat(DEST_FOLDER + "result",result);
		//Imgproc.findContours(closing,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(result, contours, hierarchy, Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE );
		MatOfPoint maxArea = null;
		double lastMaxArea = 0D;
		long sizeInBytes = result.total() * result.elemSize();
		for (MatOfPoint matOfPoint : contours) {
			double contourArea = Imgproc.contourArea(matOfPoint);
	        long perct = (long) (sizeInBytes * 0.95);
	        long perctD = (long) (sizeInBytes * 0.2);
	        if( DEBUG ) {
				System.out.println( "contourArea=" + contourArea + " sizeInBytes=" + sizeInBytes + " perct=" + perct + " mode=" + mode);
	        }
	        if( contourArea > perctD && contourArea < perct ) {
	        	if( maxArea == null || lastMaxArea < contourArea ) {	        		
					maxArea = matOfPoint;
					lastMaxArea = contourArea;
					System.out.println( "Write contourArea=" + contourArea + " sizeInBytes=" + sizeInBytes + " perct=" + perct + " mode=" + mode);
				}
	        }
		}
		if( maxArea == null ) {
			System.out.println( "Error no Max Area size=" + contours.size());
	        if( mode == 4 ) {
	        	return doFill1(result, mode);
	        }
	        return null;
		}
		Mat maskResult = Mat.zeros(mat2.rows(), mat2.cols(), CvType.CV_8UC1);
		ArrayList<MatOfPoint> maxCL = new ArrayList<MatOfPoint>();
		maxCL.add( maxArea );
		Imgproc.drawContours(maskResult,maxCL, -1, new Scalar(255,255,255), -1,Imgproc.LINE_4,new Mat(),0, new Point(0,0));		
	     
	    //Mask used to flood filling.
	    //Notice the size needs to be 2 pixels than the image.
	    ///h, w = im_th.shape[:2]
	    //mask = np.zeros((h+2, w+2), np.uint8)
		
		if( DEBUG )	writeMat(DEST_FOLDER + "maskResult",maskResult);
		/*
	    Mat mask = Mat.zeros(mat2.rows() + 2, mat2.cols()+ 2, CvType.CV_8UC1);	    
	    // Flood fill from point (0, 0)
	    Mat im_floodfill = result.clone();
	    Imgproc.floodFill(im_floodfill, mask, new Point(0,0), new Scalar(0));
	    Imgproc.floodFill(im_floodfill, mask, new Point(0, mat2.rows() - 3), new Scalar(0));
	    Imgproc.floodFill(im_floodfill, mask, new Point(mat2.cols() - 3, 0), new Scalar(0));
	    Imgproc.floodFill(im_floodfill, mask, new Point(mat2.cols() - 3 ,mat2.rows() - 3), new Scalar(0));
	    return im_floodfill;
	    
	    //writeMat(DEST_FOLDER + "im_floodfill",im_floodfill);
	     */
		
		
 /*       if( countCenter(maskResult) ) {
		    // Invert flood filled image
		    Mat im_floodfill_inv = new Mat();
		    Core.bitwise_not(maskResult, im_floodfill_inv);
		    //Imgproc.morphologyEx(im_floodfill_inv, im_floodfill_inv, Imgproc.MORPH_CLOSE, kernel, new Point(-1,-1), 3);
		    //writeMat(DEST_FOLDER + "im_floodfill_inv",im_floodfill_inv);
		    return im_floodfill_inv;
        }
        return maskResult;
	    
	    /*
	    // Combine the two images to get the foreground.
	    //Mat im_out = (im_th + im_floodfill_inv);
	    Mat dest = new Mat();
	    im_th.copyTo(dest, im_floodfill_inv);
		return dest;
		*/
/*	}
	
	private static boolean countCenter( Mat img ) {	    
		int rows = img.rows(); //Calculates number of rows
		int cols = img.cols(); //Calculates number of columns
		int ch = img.channels(); //Calculates number of channels (Grayscale: 1, RGB: 3, etc.)

		int count = 0;
		int countNonZero = 0;
		for (int i=rows/2; i < ( (rows / 2 ) + 15 ); i++)
		{
		    for (int j=cols/2; j <  ( (cols / 2 ) + 15 ); j++)
		    {
		        double[] data = img.get(i, j); //Stores element in an array
		        for (int k = 0; k < ch; k++) //Runs for the available number of channels
		        {
		        	count++;
		        	if( data[k] > 0 ) {
		        		countNonZero++;
		        	}
		        }		        
		    }
		}

        long perct = (long) (count * 0.7);
        long perctD = (long) (count * 0.3);
		System.out.println( "countCenter NonZero=" + countNonZero + " count=" + count + " perct=" + perct + " =" );
		if( countNonZero < perctD ) {
			return true;
		}
		return false;
	}

	private static Mat doContour(Mat dest, int mode ) {
		Mat mat1 = new Mat(dest.rows(),dest.cols(),CvType.CV_8UC1);
		Imgproc.cvtColor(dest, mat1, Imgproc.COLOR_BGR2GRAY);
		
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(mat1, contours, hierarchy, Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_TC89_L1 );
		
		Mat mask = Mat.zeros(dest.rows(), dest.cols(), CvType.CV_8UC1);
		//Imgproc.drawContours(mask, contours, -1, new Scalar(255), 0);//CvType.CV_FILLED);
		Imgproc.drawContours(mask, contours, -1, new Scalar(255,255,255,200), 0,8,hierarchy,1, new Point(0,0));
		return mask;
	}

	public static Mat bufferedImageToMat(String filename ) throws IOException {
		File input = new File(filename);
        BufferedImage image = ImageIO.read(input);	

        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat.clone();
	}
	
	public static void writeMat( String fileName, Mat mat1 ) throws IOException {
		byte[] data1 = new byte[mat1.rows() * mat1.cols() * (int)(mat1.elemSize())];
        mat1.get(0, 0, data1);
        BufferedImage image1 = new BufferedImage(mat1.cols(),mat1.rows(), BufferedImage.TYPE_BYTE_GRAY);
        image1.getRaster().setDataElements(0, 0, mat1.cols(), mat1.rows(), data1);

        File ouptut = new File(fileName + ".bmp");
        ImageIO.write(image1, "bmp", ouptut);
	}
	
	/**
	 * Apply Canny
	 * 
	 * @param frame
	 *            the current frame
	 * @return an image elaborated with Canny
	 */
/*	private static Mat doCanny(Mat frame, int mode )
	{
		// init
		Mat grayImage = new Mat();
		Mat detectedEdges = new Mat();
		
		// convert to grayscale
		Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

        if( mode == 1 ) {
        	Imgproc.blur( grayImage, grayImage, new Size(4,4) );
        } else  if( mode == 2 ) {
        	Imgproc.blur( grayImage, grayImage, new Size(3,3) );
        } else {
        	Imgproc.blur( grayImage, grayImage, new Size(1,1) );
        }
		
		// reduce noise with a 3x3 kernel
		Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));
		int threshold = 50;
		
		// canny detector, with ratio of lower:upper threshold of 3:1
		Imgproc.Canny(detectedEdges, detectedEdges, threshold, threshold * 3);
		
		// using Canny's output as a mask, display the result
		Mat dest = new Mat();
		frame.copyTo(dest, detectedEdges);
		return dest;
	}
	*/
	

}
