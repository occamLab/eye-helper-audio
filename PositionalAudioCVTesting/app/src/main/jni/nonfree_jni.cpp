// The major functions performing the SIFT processing
#include "opencv2/core/core.hpp"
#include <opencv2/highgui/highgui.hpp>
#include "opencv2/features2d/features2d.hpp"
#include <opencv2/nonfree/nonfree.hpp>
#include <jni.h>

using namespace cv;

int run_demo()
{
     const char * imgInFile = "/sdcard/nonfree/img1.jpg";
     const char * imgOutFile = "/sdcard/nonfree/img1_result.jpg";

     Mat image;
     image = imread(imgInFile, CV_LOAD_IMAGE_COLOR);

     vector<KeyPoint> keypoints;
     Mat descriptors;

     SiftFeatureDetector detector;
     detector.detect(image, keypoints);
     detector.compute(image, keypoints, descriptors);

       /* Some other processing, please check the download package for details. */

 return 0;
}

// JNI interface functions, be careful about the naming.
extern "C"
{
    JNIEXPORT void JNICALL Java_com_eyehelper_positionalaudiocvtesting_SIFTImpl_runSIFT(JNIEnv *, jobject);
};

 JNIEXPORT void JNICALL Java_com_eyehelper_positionalaudiocvtesting_SIFTImpl_runSIFT(JNIEnv *, jobject)
{
    run_demo();
}