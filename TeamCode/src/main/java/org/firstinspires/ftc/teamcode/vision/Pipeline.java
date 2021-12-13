package org.firstinspires.ftc.teamcode.vision;

import static org.opencv.core.Core.BORDER_DEFAULT;
import static org.opencv.core.Core.inRange;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_NONE;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2HSV;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2BGR;
import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_SIMPLEX;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.line;
import static org.opencv.imgproc.Imgproc.putText;
import static org.opencv.imgproc.Imgproc.rectangle;
import static org.opencv.imgproc.Imgproc.threshold;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;
import java.util.List;

public class Pipeline extends OpenCvPipeline {

    private static int position;

    public static int getPosition(){
        return position;
    }

    @Override
    public Mat processFrame(Mat input) {
        Rect rectCrop = new Rect(0, 320, 1280, 640);
        Mat subInput = input.submat(rectCrop);
        Mat bgr = new Mat();
        Mat hsv = new Mat();
        cvtColor(subInput, bgr, COLOR_RGB2BGR);
        cvtColor(bgr, hsv, COLOR_BGR2HSV);
        GaussianBlur(bgr, hsv,new Size(5, 5), BORDER_DEFAULT);
        Mat masked = new Mat();
        Scalar lower = new Scalar(16, 4, 159);
        Scalar upper = new Scalar(38, 175, 255);
        inRange(hsv, lower, upper, masked);
        Mat thresh_img = new Mat();
        threshold(masked, thresh_img, 128, 255, THRESH_BINARY | THRESH_OTSU);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        findContours(thresh_img, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_NONE);
        if (contours.size() == 0) {
            position = 0;
        } else {
            double max = 0;
            int index = 0;
            MatOfPoint temp = new MatOfPoint();
            for (MatOfPoint point : contours) {
                if (contourArea(point) > max) {
                    max = contourArea(point);
                    index++;
                    temp = point;
                }
            }
            contours.clear();
            contours.add(temp);
        }
        drawContours(input, contours, -1, new Scalar(0, 255, 0), 1);

        int x = boundingRect(contours.get(0)).x;
        int y = boundingRect(contours.get(0)).y;
        int width = boundingRect(contours.get(0)).width;
        int height = boundingRect(contours.get(0)).height;
        int firstBound = (int) (input.rows() / 3);
        int secondBound = (int) ((input.rows() / 3) * 2);
        rectangle(input, new Point(x, y), new Point((x + width), (y + height)), new Scalar(0, 255, 0), 2);
        line(input, new Point(firstBound, 0),new Point(firstBound, input.cols()), new Scalar(255, 0, 0), 2);
        line(input, new Point(secondBound, 0), new Point(secondBound, input.cols()), new Scalar(255, 0, 0), 2);
        int centerX = (int) ((x + (x + width)) / 2);
        if (centerX < firstBound) {
            putText(input, "Left", new Point(x, y), FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            position = 1;
        } else if (centerX > secondBound) {
            putText(input, "Right", new Point(x, y), FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            position =  2;
        } else {
            putText(input, "Center", new Point(x, y), FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            position = 3;
        }
        return null;
    }
}
