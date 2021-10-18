//
// Created by Aditya Mangalampalli on 10/17/21.
//

#include "opencv2/core.hpp"
#include <jni.h>
#include <jni.h>

using namespace cv;
using namespace std;

extern "C"
JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcontroller_vision_Vision_markerDetection(JNIEnv *env, jclass type, jlong addr_rgba) {
    // TODO: implement markerDetection()
    Mat &img = *(Mat *) addr_rgba;
}