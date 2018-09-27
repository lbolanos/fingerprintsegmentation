package it.polito.elite.teaching.cv;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class fingerprint2 {

	private static final String DEST_FOLDER = "C:\\WORK\\wamp\\www\\cloudtimeroot\\trunk\\FalconMobile\\FalconIdenty\\app\\src\\main\\assets\\test\\out\\";
	private static final String DEST_JPG = DEST_FOLDER + "test1";
	private static final String ORI_FOLDER = "C:\\WORK\\wamp\\www\\cloudtimeroot\\trunk\\FalconMobile\\FalconIdenty\\app\\src\\main\\assets\\test\\";
	private static final String ORI_JPG = ORI_FOLDER + "_before15327351164805.png.jpg";
	//private static final String ORI_JPG = ORI_FOLDER + "fp_64.jpg";

	public static void main(String[] args) throws IOException {

		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		//Mat frame = bufferedImageToMat(ORI_JPG);
		
		//frame = doCanny(frame);
		
		//writeMat(DEST_JPG,frame);
		
		
		File input = new File(ORI_JPG);
        BufferedImage image = ImageIO.read(input);	

        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);

        Mat mat2 = doCanny(mat);
        writeMat2(DEST_FOLDER + "doCanny",mat2);
        mat2 = doContour(mat2);
        writeMat(DEST_FOLDER + "doContour",mat2);
        mat2 = doFill(mat2);
        
        writeMat(DEST_JPG,mat2);
		
	}
	
	private static void writeMat2(String filename, Mat mat2) throws IOException {
		Mat mat1 = new Mat(mat2.rows(),mat2.cols(),CvType.CV_8UC1);
        Imgproc.cvtColor(mat2, mat1, Imgproc.COLOR_RGB2GRAY);
		writeMat(filename,mat1);
	}

	private static Mat doFill( Mat mat2 ) throws IOException {
		
		
		 // Set values below 220 to 255.
	    Mat im_th = new Mat();
	    Imgproc.threshold(mat2, im_th, 127, 255, Imgproc.THRESH_BINARY_INV);
	    //Imgproc.threshold(result, im_th, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
	    //Imgproc.adaptiveThreshold(result, im_th, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
	    writeMat(DEST_FOLDER + "im_th",im_th);
	    
		Size kernelSize = new Size(2, 2);
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, kernelSize);		
		Mat result = new Mat();
		int iterations = 2;
		Imgproc.morphologyEx(im_th, result, Imgproc.MORPH_ERODE, kernel, new Point(-1,-1), iterations);
		writeMat(DEST_FOLDER + "result",result);

	    
	     
	    //Mask used to flood filling.
	    //Notice the size needs to be 2 pixels than the image.
	    ///h, w = im_th.shape[:2]
	    //mask = np.zeros((h+2, w+2), np.uint8)
	    Mat mask = Mat.zeros(mat2.rows() + 2, mat2.cols()+ 2, CvType.CV_8UC1);
	    
	    // Flood fill from point (0, 0)
	    Mat im_floodfill = result.clone();
	    Imgproc.floodFill(im_floodfill, mask, new Point(0,0), new Scalar(0));
	    writeMat(DEST_FOLDER + "im_floodfill",im_floodfill);
	     
	    // Invert flood filled image
	    Mat im_floodfill_inv = new Mat();
	    Core.bitwise_not(im_floodfill, im_floodfill_inv);
	    writeMat(DEST_FOLDER + "im_floodfill_inv",im_floodfill_inv);
	     
	    // Combine the two images to get the foreground.
	    //Mat im_out = (im_th + im_floodfill_inv);
	    Mat dest = new Mat();
	    im_th.copyTo(dest, im_floodfill_inv);
		return dest;
	}

	private static Mat doContour(Mat dest) {
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
	private static Mat doCanny(Mat frame)
	{
		// init
		Mat grayImage = new Mat();
		Mat detectedEdges = new Mat();
		
		// convert to grayscale
		Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

		//Imgproc.blur( grayImage, grayImage, new Size(3,3) );
		
		// reduce noise with a 3x3 kernel
		Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));
		int threshold = 100;
		
		// canny detector, with ratio of lower:upper threshold of 3:1
		Imgproc.Canny(detectedEdges, detectedEdges, threshold, threshold * 3);
		
		// using Canny's output as a mask, display the result
		Mat dest = new Mat();
		frame.copyTo(dest, detectedEdges);
		return dest;
	}
	
	

}
