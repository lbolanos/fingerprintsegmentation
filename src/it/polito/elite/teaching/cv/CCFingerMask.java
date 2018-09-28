package it.polito.elite.teaching.cv;


import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class CCFingerMask {
	private static final int BORDER_SIZE = 8;
	private static final String TAG = "CCFingerMask";

	public static Mat maskFingerP( Mat mat, int mode, IDebugImage di ) {
		if( mode > 4 ) {
			return null;
		}
		Mat imBina = mat.clone();
		if( di != null )	di.writeMat2( "entrada",imBina);
		if( mode == 0 ) {
	    	Imgproc.threshold(mat, imBina, 127, 255, Imgproc.THRESH_BINARY);
	    	Mat kernel = buildKernel( mode, null );
	    	if( di != null )	di.writeMat2( "threshold",imBina);
	    	Imgproc.erode(imBina,imBina, kernel,new Point(-1,-1),3); //refines all edges in the binary image
	    	imBina = makeBorder(imBina, di, 255, null);
	    	//Imgproc.blur( imBina, imBina, new Size(6,6) );
	    	if( di != null )	di.writeMat2( "modo0",imBina);
		}
    	

		if( mode == 4 ) {
			imBina = makeBorder(imBina, di, 255, "init");
		}
		Mat mat2 = null;
		if( mode == 0 ) {
			mat2 = new Mat();
			Imgproc.Canny(imBina, mat2, 50, 50 * 3);
			if( di != null )	di.writeMat("doCanny",mat2);
		} else {
			mat2 = doCanny(imBina, mode );
			if( di != null )	di.writeMat2("doCanny",mat2);
		}		
		mat2 = doContour(mat2, mode );
		if( di != null )	di.writeMat("doContour",mat2);
		Mat mask = null;
		mask = doFill( mat2, mode, di );
		if( mask == null ) {
			if( di == null ) {
				return maskFingerP( mat, mode + 1, di);
			}
			return null;
		}
		long sizeInBytes = mask.total() * mask.elemSize();
		int countNonZero = Core.countNonZero(mask);
		long perct = (long) (sizeInBytes * 0.8);
		long perctD = (long) (sizeInBytes * 0.2);
		if( mode == 0 ) {
			perctD = (long) (sizeInBytes * 0.05);
		}
		logFM( di, "countNonZero=" + countNonZero + " sizeInBytes=" + sizeInBytes + " perct=" + perct + " mode=" + mode);
		if( countNonZero < perctD || countNonZero > perct ) {
			if( di == null ) {
				return maskFingerP( mat, mode + 1, di);
			}
		}
		if( mode == 4 || mode == 0 ) {
			mask = removeBorder(mask, di, "exit");
		}
		//Mat mask = Mat.zeros(mat.rows(), mat.cols(), CvType.CV_8UC1);
		//Imgproc.circle(mask, new Point(mat.cols()/2, mat.rows()/2),
		//           Math.min(mat.rows(), mat.cols())/2, new Scalar(255), -1);
		if( di != null )	di.writeMat("outputMask", mask);
		return mask;
	}

	private static Mat removeBorder(Mat imBina, IDebugImage di, String text) {
		int border = BORDER_SIZE;
		Rect rectCrop = new Rect(border, border, imBina.cols() - (border*2), imBina.rows() - (border*2));
		Mat croppedImage = new Mat(imBina, rectCrop);
		if( di != null && text != null )	di.writeMat("croppedImage" + text,croppedImage);
		return croppedImage;
	}

	private static Mat makeBorder(Mat imBina, IDebugImage di, int color, String text) {
		int border = BORDER_SIZE;
		//Rect rectCrop = new Rect(border, border, imBina.cols() - (border*2), imBina.rows() - (border*2));
		//Mat croppedImage = new Mat(imBina, rectCrop);
		//if( di != null && text != null )	di.writeMat("croppedImage" + text,croppedImage);
		Scalar value = new Scalar( color, color, color, color );
		Mat dst = new Mat();
		Core.copyMakeBorder(imBina.clone(), dst , border, border, border, border, Core.BORDER_CONSTANT, value);
		imBina = dst.clone();
		if( di != null && text != null )	di.writeMat2("copyMakeBorder" + text,imBina);
		return imBina;
	}

	private static void logFM(IDebugImage di, String s) {
		if( di != null ) {
			di.log(TAG,s);
		} else {
			try  {
				Log.d(TAG,s);
			} catch (Exception e) {
				System.out.println(TAG + " " + s);
			}
		}
	}


	private static Mat doFill1( Mat mat2, int mode, IDebugImage di  ) {


		// Set values below 220 to 255.
		Mat im_th = new Mat();
		Imgproc.threshold(mat2, im_th, 220, 255, Imgproc.THRESH_BINARY_INV);
		//Imgproc.threshold(result, im_th, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
		//Imgproc.adaptiveThreshold(result, im_th, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
		if( di != null )	di.writeMat("im_th1",im_th);

		Size kernelSize = new Size(2, 2);
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, kernelSize);
		Mat result = new Mat();
		int iterations = 2;
		Imgproc.morphologyEx(im_th, result, Imgproc.MORPH_ERODE, kernel, new Point(-1,-1), iterations);
		if( di != null )	di.writeMat("morpho",result);



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

		//writeMat("im_floodfill",im_floodfill);
	     /*

	    // Invert flood filled image
	    Mat im_floodfill_inv = new Mat();
	    Core.bitwise_not(im_floodfill, im_floodfill_inv);
	    //writeMat("im_floodfill_inv",im_floodfill_inv);
	    return im_floodfill_inv;

	    /*
	    // Combine the two images to get the foreground.
	    //Mat im_out = (im_th + im_floodfill_inv);
	    Mat dest = new Mat();
	    im_th.copyTo(dest, im_floodfill_inv);
		return dest;
		*/
	}

	private static Mat doFill( Mat mat2, int mode, IDebugImage di  ) {
		// Set values below 220 to 255.
		Mat im_th = new Mat();
		Imgproc.threshold(mat2, im_th, 127, 255, Imgproc.THRESH_BINARY_INV);
		//Imgproc.threshold(result, im_th, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
		//Imgproc.adaptiveThreshold(result, im_th, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
		if( di != null )	di.writeMat("im_th",im_th);


		Mat kernel = null;
		if( mode != 0 ) {
			kernel = buildKernel( mode, null );
		} else {
			kernel = buildKernel( mode, 5 );
		}
		int iterations = 1;
		/*
		Imgproc.morphologyEx(im_th, result, Imgproc.MORPH_ERODE, kernel, new Point(-1,-1), iterations);
	    */
		Mat erosion = new Mat();
		Imgproc.erode(im_th,erosion, kernel,new Point(-1,-1),iterations); //refines all edges in the binary image

		if( di != null )	di.writeMat("erosion",erosion);
		
		Mat result = erosion;
		if( mode != 0 ) {
			result = new Mat();
			Mat opening = new Mat();
			Imgproc.morphologyEx(erosion, opening, Imgproc.MORPH_OPEN, kernel);
			if( di != null )	di.writeMat("opening",opening);
			Imgproc.morphologyEx(opening, result, Imgproc.MORPH_CLOSE, kernel); //this is for further removing small noises and holes in the image
			if( di != null )	di.writeMat("close",result);	
		}
		
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
			if( di != null ) {
				logFM( di, "contourArea=" + contourArea + " sizeInBytes=" + sizeInBytes + " perct=" + perct + " mode=" + mode);
			}
			if( contourArea > perctD && contourArea < perct ) {
				if( maxArea == null || lastMaxArea < contourArea ) {
					maxArea = matOfPoint;
					lastMaxArea = contourArea;
					logFM( di, "Using contourArea=" + contourArea + " sizeInBytes=" + sizeInBytes + " perct=" + perct + " mode=" + mode);
				}
			}
		}
		if( maxArea == null ) {
			logFM( di, "Error no Max Area size=" + contours.size());
			if( mode == 4 ) {
				return doFill1(result, mode, di);
			}
			return null;
		}	
		
	    MatOfPoint2f myPt = new MatOfPoint2f(maxArea.toArray());
		RotatedRect ellipse = Imgproc.fitEllipse( myPt );
		double actualRatio = ellipse.size.width / ellipse.size.height;
		double ratio = 0.65;
		double percent = 1 - actualRatio + 0.5;  //0.6;
		double height = ellipse.size.height * percent;
		double width = ellipse.size.height * ratio * percent;
		
		
		logFM( di, "Ellipse w=" + width + " h=" + height + " ang=" + ellipse.angle + " actualRatio=" + actualRatio + " percent=" + percent);
		RotatedRect ellipse2 = new RotatedRect(ellipse.center,new Size(width , height  ) , 180); 
		
		Mat maskResult = Mat.zeros(mat2.rows(), mat2.cols(), CvType.CV_8UC1);
		ArrayList<MatOfPoint> maxCL = new ArrayList<MatOfPoint>();
		maxCL.add( maxArea );
		Imgproc.drawContours(maskResult,maxCL, -1, new Scalar(255,255,255), -1,Imgproc.LINE_4,new Mat(),0, new Point(0,0));
		//Imgproc.ellipse(maskResult,ellipse,new Scalar(255,255,255),Core.FILLED);
		
		Mat ellM = Mat.zeros(mat2.rows(), mat2.cols(), CvType.CV_8UC1);
		Imgproc.ellipse(ellM,ellipse2,new Scalar(255,255,255),Core.FILLED);
		if( di != null )	di.writeMat("ellM",ellM);
		if( false ) {
			Mat illMask = new Mat();
			maskResult.copyTo(illMask,ellM);
			maskResult = illMask.clone();
		}

		//Mask used to flood filling.
		//Notice the size needs to be 2 pixels than the image.
		///h, w = im_th.shape[:2]
		//mask = np.zeros((h+2, w+2), np.uint8)

		if( di != null )	di.writeMat("drawCountour",maskResult);
		/*
	    Mat mask = Mat.zeros(mat2.rows() + 2, mat2.cols()+ 2, CvType.CV_8UC1);
	    // Flood fill from point (0, 0)
	    Mat im_floodfill = result.clone();
	    Imgproc.floodFill(im_floodfill, mask, new Point(0,0), new Scalar(0));
	    Imgproc.floodFill(im_floodfill, mask, new Point(0, mat2.rows() - 3), new Scalar(0));
	    Imgproc.floodFill(im_floodfill, mask, new Point(mat2.cols() - 3, 0), new Scalar(0));
	    Imgproc.floodFill(im_floodfill, mask, new Point(mat2.cols() - 3 ,mat2.rows() - 3), new Scalar(0));
	    return im_floodfill;

	    //writeMat("im_floodfill",im_floodfill);
	     */


		if( countCenter(maskResult) ) {
			// Invert flood filled image
			Mat im_floodfill_inv = new Mat();
			Core.bitwise_not(maskResult, im_floodfill_inv);
			//Imgproc.morphologyEx(im_floodfill_inv, im_floodfill_inv, Imgproc.MORPH_CLOSE, kernel, new Point(-1,-1), 3);
			//writeMat("im_floodfill_inv",im_floodfill_inv);
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
	}

	private static Mat buildKernel(int mode, Integer size) {
		Size kernelSize = new Size(2, 2);
		if( mode == 3 ) {
			kernelSize = new Size(4, 4);
		}
		if( size != null ) {
			kernelSize = new Size( size, size );
		}
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kernelSize);
		return kernel;
	}

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
		logFM( null, "countCenter NonZero=" + countNonZero + " count=" + count + " perct=" + perct + " =" );
		if( countNonZero < perctD ) {
			return true;
		}
		return false;
	}

	private static Mat doContour(Mat dest, int mode ) {
		Mat mat1 = dest;
		if( mode != 0 ) {
			mat1 = new Mat(dest.rows(),dest.cols(),CvType.CV_8UC1);
			Imgproc.cvtColor(dest, mat1, Imgproc.COLOR_BGR2GRAY);
		}

		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(mat1, contours, hierarchy, Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_TC89_L1 );

		Mat mask = Mat.zeros(dest.rows(), dest.cols(), CvType.CV_8UC1);
		//Imgproc.drawContours(mask, contours, -1, new Scalar(255), 0);//CvType.CV_FILLED);
		Imgproc.drawContours(mask, contours, -1, new Scalar(255,255,255,200), 0,8,hierarchy,1, new Point(0,0));
		return mask;
	}


	/**
	 * Apply Canny
	 *
	 * @param frame
	 *            the current frame
	 * @return an image elaborated with Canny
	 */
	private static Mat doCanny(Mat frame, int mode )
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

	public static Mat optRealFingerP(Mat mat, int mode, IDebugImage di) {
		Mat imBina = mat.clone();

		if( di != null )	di.writeMat2( "entrada",imBina);
    	//Imgproc.threshold(mat, imBina, 170, 255, Imgproc.THRESH_BINARY);
    	//Mat kernel = buildKernel( mode, 1 );
    	//Imgproc.erode(imBina,imBina, kernel,new Point(-1,-1),3); //refines all edges in the binary image
    	//imBina = makeBorder(imBina, di, 255, null);
    	//Imgproc.blur( imBina, imBina, new Size(6,6) );

		//imBina = gamma(imBina);
		
		imBina = clahe(imBina);
		if( di != null )	di.writeMat( "clahe",imBina.clone());
		
		imBina = threshold(imBina);
		if( di != null )	di.writeMat( "threshold",imBina.clone());
		
		imBina = opening(imBina);		
    	if( di != null )	di.writeMat( "opening",imBina.clone());

		return imBina;
	}

	private static Mat closing( Mat imBina ) {
		Mat opening = new Mat();
		Mat kernel = buildKernel( 0, 3 );
		Imgproc.morphologyEx(imBina, opening, Imgproc.MORPH_CLOSE, kernel);
		return opening;
	}
	
	private static Mat opening( Mat imBina ) {
		Mat opening = new Mat();
		Mat kernel = buildKernel( 0, 3 );
		Imgproc.morphologyEx(imBina, opening, Imgproc.MORPH_OPEN, kernel);
		return opening;
	}

	private static Mat threshold(Mat imBina) {
		Mat destination = new Mat();
		Imgproc.threshold(imBina, destination, 130, 255, Imgproc.THRESH_BINARY);
		return destination;
	}
	

	private static Mat erode(Mat imBina) {
		Mat destination = new Mat();
    	Mat kernel = buildKernel( 0, 2 );
    	Imgproc.erode(imBina,destination, kernel,new Point(-1,-1),3); //refines all edges in the binary image
		return destination;
	}
	
	private static Mat gaussianBlur(Mat imBina) {
		Mat destination = new Mat(imBina.rows(),imBina.cols(),imBina.type());
        Imgproc.GaussianBlur(imBina, destination,new Size(45,45), 0);
        return destination;
	}

	private static Mat clahe(Mat imBina) {
		Mat mat1 = new Mat(imBina.rows(),imBina.cols(), CvType.CV_8UC1);
		Imgproc.cvtColor(imBina, mat1, Imgproc.COLOR_RGB2GRAY);
		
		Mat destination = new Mat();
        CLAHE clahe = Imgproc.createCLAHE();
        clahe.apply(mat1, destination);
        return destination;
	}

	private static Mat gamma(Mat imBina) {
		double gamma = 0.3;	
		 Mat lut = new Mat(1, 256, CvType.CV_8UC1);
		 lut.setTo(new Scalar(0));
		 
		for (int i = 0; i < 256; i++) {
	    	lut.put(0, i, Math.pow((double)(1.0 * i/255), 1/gamma) * 255);	
	    }
		Mat out = new Mat();
		Core.LUT(imBina, lut, out);
		return out;
	}

	public static Mat ccFingerFilter(Mat mat, int mode, IDebugImage di) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Mat enhanceFP(Mat mat, int mode, IDebugImage di) {
		// TODO Auto-generated method stub
		return null;
	}

}
