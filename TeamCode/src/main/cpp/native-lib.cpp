//
// Created by Aditya Mangalampalli on 10/17/21.
//

#include <jni.h>
#include <jni.h>
#include "opencv2/core.hpp"
#include <opencv2/imgproc.hpp>

using namespace cv;
using namespace std;

extern "C"
JNIEXPORT int JNICALL
Java_org_firstinspires_ftc_robotcontroller_vision_Vision_markerDetection(JNIEnv *env, jclass type, jlong addr_rgba) {
    // TODO: implement markerDetection()
    Mat &img = *(Mat *) addr_rgba;
    Mat hsv;
    GaussianBlur(img, hsv, Size(5, 5), BORDER_DEFAULT);
    cvtColor(hsv, hsv, COLOR_BGR2HSV);
    Mat masked;
    Scalar lower = Scalar(27, 26, 189);
    Scalar upper = Scalar(40, 255, 255);
    inRange(hsv, lower, upper, masked);
    bitwise_and(hsv, hsv, masked);
    cvtColor(hsv, hsv, COLOR_BGR2GRAY);
    Mat thresh_img;
    threshold(hsv, thresh_img, 100, 255, THRESH_BINARY + THRESH_OTSU);
    vector<vector<Point>> contour;
    findContours(contour, thresh_img, RETR_EXTERNAL, CHAIN_APPROX_NONE);
    if (contour.empty()) {
        return 0;
    } else {
        double max = 0;
        int index = 0;
        for (auto &i : contour) {
            if (contourArea(i) > max) {
                max = contourArea(i);
                index++;
            }
        }
        contour.erase(contour.begin(), contour.begin() + index - 1);
        contour.erase(contour.begin() + 1, contour.end());
    }
    int x = boundingRect(contour).x;
    int y = boundingRect(contour).y;
    int width = boundingRect(contour).width;
    int height = boundingRect(contour).height;
    int firstBound = int(img.size[1] / 3);
    int secondBound = int((img.size[1] / 3) * 2);
    rectangle(img, Point(x, y), Point((x + width), (y + height)), Scalar(0, 255, 0), 2);
    line(img, Point(firstBound, 0), Point(firstBound, img.size[0]), Scalar(255, 0, 0), 2);
    line(img, Point(secondBound, 0),Point(secondBound, img.size[0]),Scalar(255, 0, 0), 2);
    int centerX = int((x+ (x + width)) / 2);
    if (centerX < firstBound){
        putText(img, "Left", Point(x, y), FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2);
        return 1;
    }
    else if (centerX > secondBound){
        putText(img, "Right", Point(x, y), FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2);
        return 2;
    }
    else{
        putText(img, "Center", Point(x, y), FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2);
        return 3;
    }
}