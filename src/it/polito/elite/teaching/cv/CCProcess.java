package it.polito.elite.teaching.cv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import android.util.Log;

public class CCProcess {

	private static final String TAG = "CCProcess";


	public static Mat resize(Mat refMat, Mat ccMat, int mode, IDebugImage di) {		
		Mat lat = imbinarize(ccMat, di);
		Mat masked = new Mat();		
		Mat maskCC = CCFingerMask.maskFingerP(ccMat, mode, null);
		lat.copyTo(masked, maskCC);
		if( di != null )	di.writeMat2( "masked",masked);	
		
		//trim
		Mat trim = trim(masked);
		if( di != null )	di.writeMat2( "trim", trim);
		
		double mediaDistanceCC = getMediaDistance(trim);
		double mediaDistanceRef = getMediaDistance(refMat);
		double perc = mediaDistanceRef / mediaDistanceCC;
		
		int width = (int) (trim.cols() * perc);
		int height = (int) (trim.rows() * perc);
		Mat resizedFrame = new Mat(width, height, trim.type());
        Imgproc.resize(trim, resizedFrame, new Size(width, height));
        if( di != null )	di.writeMat2( "resize", resizedFrame);
        
		return resizedFrame;
	}

	public static Mat adjusReftMask(Mat resizedFrame, Mat refMask, int position, IDebugImage di ) {
		int finalWidth = resizedFrame.width();
        int finalHeight = resizedFrame.height();
        
        int currWidth = refMask.width();
        int currHeight = refMask.height();
        
        int diffWidth = finalWidth - currWidth;
        int diffHeight = finalHeight - currHeight;
        int diffHalfWidth = diffWidth / 2;
        if( diffHalfWidth >= 0 && diffHeight >= 0 ) {
            diffHeight = diffHeight / 4;
        	int top = diffHeight * position;
        	int bottom = diffHeight * ( 4 - position );
        	int left = diffHalfWidth;
        	int right = diffHalfWidth;
        	int xpad = finalWidth - currWidth - left - right;
        	right += xpad;
        	int ypad = finalHeight - currHeight - top - bottom;
        	bottom += ypad;
        	return makeBorder( refMask, top, bottom, left, right, 0, di, "adjusReftMask" + position );
        } else if( diffHalfWidth < 0 && diffHeight < 0 ) {
        	diffHeight = diffHeight / 2;
        	int x = (0-diffHalfWidth);
        	int y = (0-diffHeight);
        	int width = finalWidth;
        	int height = finalHeight;
        	return removeBorder( refMask, x,  y,  width,  height,  di, "NegativeAdjusReftMask" + position );
        } else
        if( diffHalfWidth < 0 ) {
        	diffHeight = diffHeight / 4;
        	int x = (0-diffHalfWidth);
        	int y = 0;
        	int width = finalWidth;
        	int height = currHeight;
        	Mat temp = removeBorder( refMask, x,  y,  width,  height,  di, "NegativeWidth" + position );
        	
        	int top = diffHeight * position;
        	int bottom = diffHeight * ( 4 - position );
        	int left = 0;
        	int right = 0;
        	return makeBorder( temp, top, bottom, left, right, 0, di, "PositiveHeight" + position );        	
        } else {
        	diffHeight = diffHeight / 2;
        	int x = 0;
        	int y = (0-diffHeight);
        	int width = currWidth;
        	int height = finalHeight;
        	Mat temp = removeBorder( refMask, x,  y,  width,  height,  di, "NegativeHeight" + position );
        	
        	int top = 0;
        	int bottom = 0;
        	int left = diffHalfWidth;
        	int right = diffHalfWidth;
        	return makeBorder( temp, top, bottom, left, right, 0, di, "PositiveWidtht" + position );               	
        }
	}
	
	private static Mat makeBorder(Mat imBina, int top, int bottom, int left, int right, int color, IDebugImage di, String text) {
		Scalar value = new Scalar( color, color, color, color );
		Mat dst = new Mat();
		Core.copyMakeBorder(imBina.clone(), dst , top, bottom, left, right, Core.BORDER_CONSTANT, value);
		imBina = dst.clone();
		if( di != null && text != null )	di.writeMat2("copyMakeBorder" + text,imBina);
		return imBina;
	}
	
	private static Mat removeBorder(Mat imBina, int x, int y, int width, int height, IDebugImage di, String text) {
		Rect rectCrop = new Rect( x, y, width, height);
		Mat croppedImage = new Mat(imBina, rectCrop);
		if( di != null && text != null )	di.writeMat("croppedImage" + text,croppedImage);
		return croppedImage;
	}



	private static double getMediaDistance(Mat mat) {
		List<Integer> stockList = new ArrayList<Integer>();
		
		int center = mat.width() / 2;
        int height = mat.height();
        int countBlack = 0;
        int countWhite = 0;
        int oldCountWhite = 0;
        for (int y = 0; y < height; y++) {
            double[] ds = mat.get(y, center);
			if(ds[0] > 0) {
				if( countWhite > 0 && countBlack > 0 && oldCountWhite > 0 ) {
					stockList.add( countBlack + countWhite / 2 + oldCountWhite + 2 );
					oldCountWhite = countWhite;
					countBlack = 0;
					countWhite = 0;
            	} else if( countWhite > 0 && countBlack > 0 && oldCountWhite == 0 ) {
            		countWhite = 0;
            	}
				countWhite++;
            } else {
            	if( countWhite > 0 && countBlack > 0 && oldCountWhite == 0 ) {
            		oldCountWhite = countBlack;
            		countBlack = 0;
            	}
            	countBlack++;
            }
        }
		return medianValue( stockList );
	}

	private static double medianValue(List<Integer> list) {
		list.sort(Comparator.naturalOrder());
		double median = list.get(list.size()/2);
		if(list.size()%2 == 0) median = (median + list.get(list.size()/2-1)) / 2;
		return median;
	}

	private static Mat imbinarize(Mat ccMat, IDebugImage di) {
		Mat inBinarize = ccMat.clone();
		//ccMat.copyTo(masked, mask);
		
		//colorspace gray
		Imgproc.cvtColor(inBinarize, inBinarize, Imgproc.COLOR_RGB2GRAY);
		if( di != null )	di.writeMat2( "inBinarize",inBinarize);
		
		//auto-level
		Mat normalize = new Mat();
		Core.normalize(inBinarize, normalize, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1);
		if( di != null )	di.writeMat2( "normalize", normalize);

		
		//gamma
		Mat gamma = gamma( normalize );
		if( di != null )	di.writeMat2( "gamma",gamma);
		
		//-enhance
		Mat denoise = new Mat();
		Photo.fastNlMeansDenoising(gamma, denoise);
		if( di != null )	di.writeMat2( "denoise",denoise);
		
		//-equalize
		// apply histogram equalization
		Mat equalized = new Mat();
        Imgproc.equalizeHist(denoise, equalized);
        if( di != null )	di.writeMat2( "equalized",equalized);

		//-negate
        Mat negate = new Mat();
        Core.bitwise_not(equalized, negate);        
        if( di != null )	di.writeMat2( "negate",negate);
        
		//-lat
        Mat lat = new Mat();
        //Imgproc.adaptiveThreshold(negate, lat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11,2 );//20, 10);
        Imgproc.adaptiveThreshold(negate, lat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV, 25, -20 );
        if( di != null )	di.writeMat2( "lat",lat);
		return lat;
	}
	
	public static Mat trim( Mat frame ) {
		Mat result = new Mat();
		Core.reduce(frame, result, 0, Core.REDUCE_SUM, CvType.CV_32S);
		int x = getFirstOne(result);
		int width = getLastOne(result) - x;		
		Core.reduce(frame, result, 1, Core.REDUCE_SUM, CvType.CV_32S);
		int y = getFirstYOne(result);
		int height = getLastYOne(result) - y;	
		
		Rect rectCrop = new Rect( x, y, width, height);
		Mat croppedImage = new Mat(frame, rectCrop);
		return croppedImage;
	}

	private static int getFirstOne(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if(mat.get(y, x)[0] > 0) {
                	return x;
                }
            }
        }
        return 0;
	}

    private static int getLastOne(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        for (int y = 0; y < height; y++) {
            for (int x = width-1; x >= 0; --x) {
                if(mat.get(y, x)[0] > 0) {
                	return x;
                }
            }
        }
        return 0;
	}

	private static int getFirstYOne(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if(mat.get(y, x)[0] > 0) {
                	return y;
                }
            }
        }
        return 0;
	}

    private static int getLastYOne(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        for (int y = height -1; y >= 0; --y) {
        	for (int x = 0; x < width; x++) {
                if(mat.get(y, x)[0] > 0) {
                	return y;
                }
            }
        }
        return 0;
	}
	/**
     * Normalize the image to have zero mean and unit standard deviation.
     */
    private void normalizeImage(Mat src, Mat dst) {

        MatOfDouble mean = new MatOfDouble(0.0);
        MatOfDouble std = new MatOfDouble(0.0);

        // get mean and standard deviation
        Core.meanStdDev(src, mean, std);
        Core.subtract(src, Scalar.all(mean.toArray()[0]), dst);
        Core.meanStdDev(dst, mean, std);
        Core.divide(dst, Scalar.all(std.toArray()[0]), dst);
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


    /**
     * Print/Log the given mat.
     *
     * @param mat
     */
    private static void logMat(Mat mat) {

        int width = mat.width();
        int height = mat.height();

        ArrayList<Double> list = new ArrayList<Double>();

        logFM(null, "Start Printing Mat");

        for (int y = 0; y < height; y++) {
            list.clear();
            for (int x = 0; x < width; x++) {
                list.add(mat.get(y, x)[0]);
            }
            logFM(null, list.toString());
        }

        logFM(null, "Finish Printing Mat");
    }
    

	private static void logFM(IDebugImage di, String s) {
		if( di != null ) {
			di.log(TAG,s);
		} else {
			try  {
				Log.d(TAG,s);
			} catch (Exception e) {
				System.out.println(s);
			}
		}
	}
}
