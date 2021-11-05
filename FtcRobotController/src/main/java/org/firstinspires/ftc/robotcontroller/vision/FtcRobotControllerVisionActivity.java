package org.firstinspires.ftc.robotcontroller.vision;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.qualcomm.ftcrobotcontroller.R;

import org.firstinspires.ftc.robotcontroller.internal.FtcRobotControllerActivity;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

@SuppressWarnings("ALL")
public class FtcRobotControllerVisionActivity extends FtcRobotControllerActivity implements
        CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "RCActivity:Vision";
    private static boolean killOpenCV = false;
    private static boolean openCVKilled = false;

    public static int markerPosition;

    JavaCameraView javaCameraView;
    Mat mRgba;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            //noinspection SwitchStatementWithTooFewBranches
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    System.loadLibrary("native-lib");
                    javaCameraView.enableView();
                    break;
                default:
                    Log.d(TAG, "callback: could not load OpenCV");
            }
        }
    };

    public static void killOpenCV() {
        killOpenCV = true;
    }

    public static void reviveOpenCV() {
        killOpenCV = false;
    }

    @Override //start to stream frames from camera to algorithm
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override //when the camera view is stopped, release all the mats
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override //run the vision pipeline for the frames
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        markerPosition = Vision.markerDetection(mRgba.getNativeObjAddr());
        Log.d("OpenCV Marker", String.valueOf(markerPosition));
        return mRgba;
    }

    public static int getMarkerPosition(){
        return markerPosition;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            boolean success = OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
            if (success) {
                Log.d(TAG, "asynchronous initialization succeeded!");
            } else {
                Log.d(TAG, "asynchronous initialization failed!");
            }
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Thing is null: " + (findViewById(R.id.java_camera_view) == null));
        javaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view);
        javaCameraView.setCameraPermissionGranted();
        javaCameraView.setVisibility(View.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
        killOpenCV = false;
    }

    //turn off the view to save GPU and CPU power
    public void disableView() {
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    //enable view to start running the pipeline
    public void enableView() {
        if (javaCameraView != null) {
            javaCameraView.enableView();
        }
    }

    @Override //disable the view when the activity is stopped
    public void onDestroy() {
        super.onDestroy();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }
}