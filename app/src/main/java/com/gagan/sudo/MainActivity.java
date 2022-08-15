package com.gagan.sudo;

import static org.opencv.android.CameraRenderer.LOGTAG;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class MainActivity extends CameraActivity {

    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            switch(status){
                case LoaderCallbackInterface
                        .SUCCESS: {
                    mOpenCvCameraView.enableView();
                } break;
                default:  {
                    super.onManagerConnected(status);
                }break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.javaCameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(cvCameraViewListener);
    }

    @Override
    protected List<?extends CameraBridgeViewBase> getCameraViewList(){
        return Collections.singletonList(mOpenCvCameraView);
    }

    private CameraBridgeViewBase.CvCameraViewListener2 cvCameraViewListener = new CameraBridgeViewBase.CvCameraViewListener2() {
        @Override
        public void onCameraViewStarted(int width, int height) {

        }

        @Override
        public void onCameraViewStopped() {

        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            Mat src = inputFrame.rgba();
            Mat gray = inputFrame.gray();

            Size frameSize = src.size();  // always the return any frame size == src frame size  (use resize for that).


            // some preprocessing here.
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
            Mat binary = new Mat(src.rows(), src.cols(), src.type(), new Scalar(0));
            Imgproc.threshold(gray, binary, 100, 255, Imgproc.THRESH_BINARY_INV);


            // find all contours.
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_TREE,
                    Imgproc.CHAIN_APPROX_SIMPLE);

            // finding the biggest contours.
            List<MatOfPoint> biggestContour = Utils.findBiggestContours(contours);


            // if the list of biggestContour is not empty then only.
            if(!biggestContour.isEmpty()) {
                //Drawing the Contours
                Scalar color = new Scalar(0, 255, 0); // color of the line.
                Imgproc.drawContours(src, biggestContour, -1, color, 2);


                // wraping the sudoku image.
                MatOfPoint2f approx = new MatOfPoint2f();
                biggestContour.get(0).convertTo(approx, CvType.CV_32F);

                Moments moment = Imgproc.moments(approx);
                int x = (int) (moment.get_m10() / moment.get_m00());
                int y = (int) (moment.get_m01() / moment.get_m00());

                Point[] sortedPoints = new Point[4];

//                System.out.println("sortedpt: " + sortedPoints[0]);
                double[] data;
                int count = 0;
                for(int i=0; i<approx.rows(); i++){
                    data = approx.get(i, 0);
                    double datax = data[0];
                    double datay = data[1];
                    if(datax < x && datay < y){
                        sortedPoints[0]=new Point(datax,datay);
                        count++;
                    }else if(datax > x && datay < y){
                        sortedPoints[1]=new Point(datax,datay);
                        count++;
                    }else if (datax < x && datay > y){
                        sortedPoints[2]=new Point(datax,datay);
                        count++;
                    }else if (datax > x && datay > y){
                        sortedPoints[3]=new Point(datax,datay);
                        count++;
                    }
                }

                if(sortedPoints[0] != null && sortedPoints[1] != null && sortedPoints[2] != null && sortedPoints[3] != null){
                    MatOfPoint2f init = new MatOfPoint2f(
                            sortedPoints[0],
                            sortedPoints[1],
                            sortedPoints[2],
                            sortedPoints[3]
                    );

                    MatOfPoint2f dst = new MatOfPoint2f(
                            new Point(0, 0),
                            new Point(450-1,0),
                            new Point(0,450-1),
                            new Point(450-1,450-1)
                    );

                    Mat warpMat = Imgproc.getPerspectiveTransform(init,dst);

                    Mat destImage = new Mat();
                    Imgproc.warpPerspective(src, destImage, warpMat, src.size());

                    Rect rec = new Rect(new Point(0, 0), new Point(450, 450)); // Rect((0, 0), (0 + x, 0 + y)) -> opposite points.
                    Mat mt1 = destImage.submat(rec);

                    Mat mt2 = new Mat();
                    Imgproc.resize(mt1, mt2, frameSize);
//                    Size emptyArea = new Size(160, 480);
//                    Mat mt2 = new Mat(emptyArea, CvType.CV_8UC4);
//
//                    System.out.println("dim :" + mt1.dims() + " col :" + mt1.cols() + " rows :" + mt1.rows() + " type :" + mt1.type()) ;
//
//                    Mat mt = new Mat();
//                    List<Mat> source = Arrays.asList(mt2, mt1);
//                    Core.hconcat(source, mt);
////
//                    System.out.println("destination image-> dim :" + mt.dims() + " col :" + mt.cols() + " rows :" + mt.rows() + " type :" + mt.type()) ;
//                    System.out.println("tyep" + destImage.size());
//                    return mt2;
                    List<Mat> boxes = Utils.splitIntoBoxes(mt2);
                    Mat box = boxes.get(8);
                    box = Utils.fitFrame(box, frameSize);
                    return box;
                }
            }
            return src;
        }
    };


    @Override
    protected void onPause() {
        super.onPause();
        if(mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Log.d(LOGTAG, "OpenCv not found, Initializing.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        }else{
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
    }
}

//public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
//
//    CameraBridgeViewBase cameraBridgeViewBase;
//
//    Mat mat1, mat2, mat3;
//
//    BaseLoaderCallback baseLoaderCallback;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.javaCameraView);
//        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
//        cameraBridgeViewBase.setCvCameraViewListener(this);
//
//        baseLoaderCallback = new BaseLoaderCallback(this) {
//            @Override
//            public void onManagerConnected(int status) {
//                System.out.println("OpenCvworking lol ");
//                switch(status){
//                    case BaseLoaderCallback.SUCCESS:
//                        cameraBridgeViewBase.enableView();
//                        break;
//                    default:
//                        super.onManagerConnected(status);
//                        break;
//                }
//            }
//        };
//    }
//
//    @Override
//    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//        mat1 = inputFrame.rgba();
//        return mat1;
//    }
//
//    @Override
//    public void onCameraViewStopped() {
//        mat1.release();
//        mat2.release();
//        mat3.release();
//    }
//
//    @Override
//    public void onCameraViewStarted(int width, int height) {
//        mat1 = new Mat(width, height, CvType.CV_8UC4);
//        mat1 = new Mat(width, height, CvType.CV_8UC4);
//        mat1 = new Mat(width, height, CvType.CV_8UC4);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if(cameraBridgeViewBase != null){
//            cameraBridgeViewBase.disableView();
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if(!OpenCVLoader.initDebug()){
//            Toast.makeText(this, "there is a problem.", Toast.LENGTH_SHORT).show();
//        }else{
//            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if(cameraBridgeViewBase != null){
//            cameraBridgeViewBase.disableView();
//        }
//    }
//}