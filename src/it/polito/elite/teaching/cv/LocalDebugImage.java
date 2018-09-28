package it.polito.elite.teaching.cv;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class LocalDebugImage implements IDebugImage {
	//private static final String DEST_FOLDER = "C:\\WORK\\wamp\\www\\cloudtimeroot\\trunk\\FalconMobile\\FalconIdenty\\app\\src\\main\\assets\\test\\out\\";
	private static final String DEST_FOLDER = "D:\\Download\\temp\\14\\out\\";
	private static final boolean BMP = false;

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

		String jpg = "jpg";
		if( BMP ) {
			jpg = "bmp";
		}
		File ouptut = new File(DEST_FOLDER + fileName + "." + jpg);
		try {
			ImageIO.write(image1, jpg, ouptut);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void log(String tag,String s) {
		System.out.println(s);
	}
}