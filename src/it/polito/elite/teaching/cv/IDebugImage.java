package it.polito.elite.teaching.cv;

import org.opencv.core.Mat;

public interface IDebugImage {

	void writeMat2(String s, Mat croppedImage);

	void writeMat(String s, Mat mat2);

	void log(String tag,String s);
}
