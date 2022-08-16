package com.gagan.sudo;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.ImageProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class HelperUtils {

    public static final String MODEL_PATH = "assets/model.tflite";

    // find the biggest contour.
    public static List<MatOfPoint> findBiggestContours(List<MatOfPoint> contours){
        List<MatOfPoint> contourList = new ArrayList<>();
        MatOfPoint biggestContour = null;
        double maxArea = 0; // try 50000 for now 10000 is fine.
        for(int i = 0; i < contours.size(); i++){
            MatOfPoint currentContour = contours.get(i);

            double area = Imgproc.contourArea(currentContour);

            MatOfPoint approxContour = new MatOfPoint();
            MatOfPoint2f approxContour2f = new MatOfPoint2f();
            MatOfPoint2f currentContour2f = new MatOfPoint2f();

            currentContour.convertTo(currentContour2f, CvType.CV_32FC2);

            double arcLength = Imgproc.arcLength(currentContour2f, true);
            Imgproc.approxPolyDP(currentContour2f, approxContour2f, 0.1 * arcLength, true);

            approxContour2f.convertTo(approxContour, CvType.CV_32S);


            if(area > maxArea && approxContour.size().height == 4) {
                maxArea = area;
                biggestContour = approxContour;
            }
        }

        if(biggestContour != null) contourList.add(biggestContour);
//        System.out.println("area: " + maxArea);
        return contourList;
    }


    // Preprocess the image.
    public static Mat preProcess(Mat image){

        return image;
    }


    // Reorder the points.
    public static MatOfPoint2f sortPoints(MatOfPoint2f src){

        return src;
    }

    // Concat two image horizontally.
    public static Mat ConcatHorizontal(List<Mat> source){
        Mat dst = new Mat();
        Core.hconcat(source, dst);
        return dst;
    }

    // Concat two image vertically.
    public static Mat ConcatVertical(List<Mat> source){
        Mat dst = new Mat();
        Core.hconcat(source, dst);
        return dst;
    }

    // split the sudoku box into smaller boxes.
    public static List<Mat> splitIntoBoxes(Mat box){  // need to fix the orientation
        List<Mat> boxes = new ArrayList<>();
        int width = box.width() / 9;
        int height = box.height() / 9;
        for(int i = 0; i < 9; i++){
            for(int j = 8; j >= 0; j--){
                Rect rec = new Rect(new Point(i * width, j * height), new Point(i * width + width, j * height + height));
                Mat temp = box.submat(rec);

                // add code for refining the boxes here.

                boxes.add(temp);
            }
        }
        return boxes;
    }

    // fit the frame size of camera output.
    public static Mat fitFrame(Mat image, Size size){
        Mat dst = new Mat();
        Imgproc.resize(image, dst, size);
        return dst;
    }

    // cut out the outside portion of the boxes.
    public static Mat refineBoxes(Mat box){
        return box;
    }




//    public static MappedByteBuffer loadModelFile(Activity activity) throws IOException {
//        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(getModelPath());
//        FileInputStream inputStream = new  FileInputStream(fileDescriptor.getFileDescriptor());
//        FileChannel fileChannel = inputStream.getChannel();
//        long startOffset = fileDescriptor.getStartOffset();
//        long declaredLength = fileDescriptor.getDeclaredLength();
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
//    }
//
//    public static String getModelPath() {
//        return MODEL_PATH;
//    }

    //get prediction here
    public static List<Integer> getPrediction(List<Mat> boxes){
        List<Integer> results = new ArrayList<>();

        return results;
    }

}
