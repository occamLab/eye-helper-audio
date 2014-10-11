// The major functions performing the SIFT processing
#include "opencv2/core/core.hpp"
#include <opencv2/highgui/highgui.hpp>
#include "opencv2/features2d/features2d.hpp"
#include <opencv2/nonfree/nonfree.hpp>
#include <jni.h>
#include <android/log.h>
#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>

using namespace cv;
using namespace std;

#define APPNAME "MyApp"

// JNI interface functions, be careful about the naming.
extern "C"
{
    JNIEXPORT void JNICALL Java_com_eyehelper_positionalaudiocvtesting_SIFTImpl_runSIFT(JNIEnv* env, jobject thiz, jint width, jint height, jbyteArray image, jintArray keypoints);
};

 JNIEXPORT void JNICALL Java_com_eyehelper_positionalaudiocvtesting_SIFTImpl_runSIFT(JNIEnv* env, jobject thiz, jint width, jint height, jbyteArray image, jintArray keypoints)
    {
        jbyte* _image  = env->GetByteArrayElements(image, 0);
        jint*  _keypoints = env->GetIntArrayElements(keypoints, 0);

        Mat mImage(height, width, CV_8UC4, (unsigned char *)_image);
        Mat mKeypoints(height, width, CV_8UC4, (unsigned char *)_keypoints);
        //Mat mgray(height, width, CV_8UC1, (unsigned char *)_yuv);

        //Please make attention about BGRA byte order
        //ARGB stored in java as int array becomes BGRA at native level
        cvtColor(mImage, mKeypoints, CV_YUV420sp2BGR, 4);

        vector<KeyPoint> v;
        Mat descriptors;

        SiftFeatureDetector detector;
        detector.detect(mImage, v);
        detector.compute(mImage, v, descriptors);

        for( size_t i = 0; i < v.size(); i++ )
            circle(mImage, Point(v[i].pt.x, v[i].pt.y), 10, Scalar(0,0,255,255));

        env->ReleaseIntArrayElements(keypoints, _keypoints, 0);
        env->ReleaseByteArrayElements(image, _image, 0);
    }