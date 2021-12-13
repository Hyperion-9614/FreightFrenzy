package org.firstinspires.ftc.teamcode.vision;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvInternalCamera;
import org.openftc.easyopencv.OpenCvPipeline;

public class CameraDaemon {

    public static OpenCvCamera initWebcam(HardwareMap hardwareMap, boolean livePreview, OpenCvPipeline pipeline){
        OpenCvCamera webcam;
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        WebcamName webcamName = hardwareMap.get(WebcamName.class, "Webcam");
        if (livePreview){
            webcam = OpenCvCameraFactory.getInstance().createWebcam(webcamName, cameraMonitorViewId);
        }
        else{
            webcam = OpenCvCameraFactory.getInstance().createWebcam(webcamName);
        }
        webcam.setPipeline(pipeline);
        webcam.setViewportRenderer(OpenCvCamera.ViewportRenderer.GPU_ACCELERATED);
        return webcam;
    }

    public static OpenCvCamera initPhoneCamera(HardwareMap hardwareMap, boolean livePreview, OpenCvPipeline pipeline){
        OpenCvCamera phoneCam;
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        if (livePreview) {
            phoneCam = OpenCvCameraFactory.getInstance().createInternalCamera(OpenCvInternalCamera.CameraDirection.BACK, cameraMonitorViewId);
        }
        else {
            phoneCam = OpenCvCameraFactory.getInstance().createInternalCamera(OpenCvInternalCamera.CameraDirection.BACK);
        }
        phoneCam.setPipeline(pipeline);
        phoneCam.setViewportRenderer(OpenCvCamera.ViewportRenderer.GPU_ACCELERATED);
        return phoneCam;
    }

    public static void streamCamera(OpenCvCamera camera, int width, int height){
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                camera.startStreaming(width, height, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {
            }
        });
    }

}
