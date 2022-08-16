package com.gagan.sudo;

import static org.opencv.android.CameraRenderer.LOGTAG;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
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
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class MainActivity extends CameraActivity {

    private CameraBridgeViewBase mOpenCvCameraView;
    private Interpreter tflite;

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

        try {
            tflite = new Interpreter(loadModelFile(MainActivity.this));
            System.out.println("lock and loaded.");
        } catch (IOException e) {
            e.printStackTrace();
        }

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


            Bitmap bitmap = Bitmap.createBitmap(gray.cols(), gray.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(gray, bitmap);

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            ImageProcessor imageProcessor = new ImageProcessor.Builder()
                    .add(new ResizeOp(32, 32, ResizeOp.ResizeMethod.BILINEAR))
                    .add(new NormalizeOp(0.0F, 1.0F))
                    .add(new TransformToGrayscaleOp())
                    .build();

            TensorImage tensorImage = new TensorImage(DataType.FLOAT32);

            tensorImage.load(bitmap);
            tensorImage = imageProcessor.process(tensorImage);
//            TensorBuffer outputBuffer = TensorBuffer.createFixedSize(new int[]{1, 10}, DataType.FLOAT32);
//            System.out.println("info" + tensorImage.getBuffer() + ' ' + DataType.FLOAT32.byteSize());
            float[][] output = new float[1][10];
            tflite.run(tensorImage.getBuffer(), output);
//            float[] data2 = outputBuffer. getFloatArray();
            String s = "";
            for(int i = 0; i < output[0].length; i++) s = s + Float.toString(output[0][i]) + " ";
            System.out.println("hurraaah " + s);







//            Size frameSize = src.size();  // always the return any frame size == src frame size  (use resize for that).
//
//
//            // some preprocessing here.
//            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
//            Mat binary = new Mat(src.rows(), src.cols(), src.type(), new Scalar(0));
//            Imgproc.threshold(gray, binary, 100, 255, Imgproc.THRESH_BINARY_INV);
//
//
//            // find all contours.
//            List<MatOfPoint> contours = new ArrayList<>();
//            Mat hierarchy = new Mat();
//            Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_TREE,
//                    Imgproc.CHAIN_APPROX_SIMPLE);
//
//            // finding the biggest contours.
//            List<MatOfPoint> biggestContour = Utils.findBiggestContours(contours);
//
//
//            // if the list of biggestContour is not empty then only.
//            if(!biggestContour.isEmpty()) {
//                //Drawing the Contours
//                Scalar color = new Scalar(0, 255, 0); // color of the line.
//                Imgproc.drawContours(src, biggestContour, -1, color, 2);
//
//
//                // wraping the sudoku image.
//                MatOfPoint2f approx = new MatOfPoint2f();
//                biggestContour.get(0).convertTo(approx, CvType.CV_32F);
//
//                Moments moment = Imgproc.moments(approx);
//                int x = (int) (moment.get_m10() / moment.get_m00());
//                int y = (int) (moment.get_m01() / moment.get_m00());
//
//                Point[] sortedPoints = new Point[4];
//
////                System.out.println("sortedpt: " + sortedPoints[0]);
//                double[] data;
//                int count = 0;
//                for(int i=0; i<approx.rows(); i++){
//                    data = approx.get(i, 0);
//                    double datax = data[0];
//                    double datay = data[1];
//                    if(datax < x && datay < y){
//                        sortedPoints[0]=new Point(datax,datay);
//                        count++;
//                    }else if(datax > x && datay < y){
//                        sortedPoints[1]=new Point(datax,datay);
//                        count++;
//                    }else if (datax < x && datay > y){
//                        sortedPoints[2]=new Point(datax,datay);
//                        count++;
//                    }else if (datax > x && datay > y){
//                        sortedPoints[3]=new Point(datax,datay);
//                        count++;
//                    }
//                }
//
//                if(sortedPoints[0] != null && sortedPoints[1] != null && sortedPoints[2] != null && sortedPoints[3] != null){
//                    MatOfPoint2f init = new MatOfPoint2f(
//                            sortedPoints[0],
//                            sortedPoints[1],
//                            sortedPoints[2],
//                            sortedPoints[3]
//                    );
//
//                    MatOfPoint2f dst = new MatOfPoint2f(
//                            new Point(0, 0),
//                            new Point(450-1,0),
//                            new Point(0,450-1),
//                            new Point(450-1,450-1)
//                    );
//
//
//                    // wrap the sudoku board into 450 * 450 mat.
//                    Mat warpMat = Imgproc.getPerspectiveTransform(init,dst);
//                    Mat destImage = new Mat();
//                    Imgproc.warpPerspective(src, destImage, warpMat, src.size());
//
//
//                    // cut out the sudoku board int 450 * 450 dimension.
//                    Rect rec = new Rect(new Point(0, 0), new Point(450, 450)); // Rect((0, 0), (0 + x, 0 + y)) -> opposite points.
//                    Mat sudokuBoard = destImage.submat(rec);
//
//
//                    // split the sudoku into 81 small images of boxes and then refine them.
//                    List<Mat> boxes = Utils.splitIntoBoxes(sudokuBoard);
//
////                    List<Integer> results = Utils.getPrediction(boxes);
//
//
//
//                    ImageProcessor imageProcessor =
//                            new ImageProcessor.Builder()
//                                    .add(new ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
//                                    .build();
//
//
//
//                    //
//                    Mat box = boxes.get(0);
//                    box = Utils.fitFrame(box, frameSize);
//                    return box;
//                }
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
        tflite.close();
    }



    public MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd("model.tflite");
        FileInputStream inputStream = new  FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}
