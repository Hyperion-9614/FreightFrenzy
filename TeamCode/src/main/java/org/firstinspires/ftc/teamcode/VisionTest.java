package org.firstinspires.ftc.teamcode;

import android.annotation.SuppressLint;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.vision.CameraDaemon;
import org.openftc.easyopencv.OpenCvCamera;

@TeleOp
public class VisionTest extends LinearOpMode
{

    @SuppressLint("DefaultLocale")
    @Override
    public void runOpMode()
    {

        OpenCvCamera camera = CameraDaemon.initPhoneCamera(hardwareMap, true);
        CameraDaemon.streamCamera(camera, 1280, 960);

        telemetry.addLine("Waiting for start");
        telemetry.update();
        waitForStart();

        while (opModeIsActive())
        {
            telemetry.addData("Frame Count", camera.getFrameCount());
            telemetry.addData("FPS", String.format("%.2f", camera.getFps()));
            telemetry.addData("Total frame time ms", camera.getTotalFrameTimeMs());
            telemetry.addData("Pipeline time ms", camera.getPipelineTimeMs());
            telemetry.addData("Overhead time ms", camera.getOverheadTimeMs());
            telemetry.addData("Theoretical max FPS", camera.getCurrentPipelineMaxFps());
            telemetry.update();
        }
    }
}