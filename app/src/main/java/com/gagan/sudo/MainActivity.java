package com.gagan.sudo;

import static org.opencv.android.CameraRenderer.LOGTAG;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
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

            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
            Mat binary = new Mat(src.rows(), src.cols(), src.type(), new Scalar(0));
            Imgproc.threshold(gray, binary, 100, 255, Imgproc.THRESH_BINARY_INV);

            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchey = new Mat();
            Imgproc.findContours(binary, contours, hierarchey, Imgproc.RETR_TREE,
                    Imgproc.CHAIN_APPROX_SIMPLE);
            //Drawing the Contours
            Scalar color = new Scalar(0, 255, 0);
            Imgproc.drawContours(src, contours, -1, color, 2, Imgproc.LINE_8,
                    hierarchey, 2, new Point() ) ;
//            Core.flip(input_rgba.t(), input_rgba, 1);

//            MatOfPoint corners = new MatOfPoint();
//            Imgproc.goodFeaturesToTrack(input_gray, corners, 20,0.01, 10, new Mat(), 3, false);
//            Point[] cornersArr = corners.toArray();
//
//            for(int i = 0; i < corners.rows(); i++){
//                Imgproc.circle(input_rgba, cornersArr[i], 10, new Scalar(0, 255, 0), 2);
//            }
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