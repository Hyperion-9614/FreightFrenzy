package org.firstinspires.ftc.teamcode.vision;

import org.opencv.core.Mat;
import org.openftc.easyopencv.OpenCvPipeline;

public class NativePipeline extends OpenCvPipeline
{
    @Override
    public Mat processFrame(Mat input)
    {
        nativeProcessFrame(input.nativeObj);
        return input;
    }

    native int nativeProcessFrame(long frame_ptr);

    static
    {
        System.loadLibrary("TeamCode");
    }
}