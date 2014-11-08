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

// Get keypoints and descriptors for training image
extern "C"
{
    JNIEXPORT jfloatArray JNICALL Java_com_eyehelper_positionalaudiocvtesting_cv_SIFTWrapper_runSIFT(JNIEnv* env, jobject obj, jint height, jint width, jshortArray image, jintArray dimens);
};

 JNIEXPORT jfloatArray JNICALL Java_com_eyehelper_positionalaudiocvtesting_cv_SIFTWrapper_runSIFT(JNIEnv* env, jobject obj, jint height, jint width, jshortArray image, jintArray dimens)
    {
        jfloatArray result;
        jshort* _image  = env->GetShortArrayElements(image, 0);
        jint* _dimens = env->GetIntArrayElements(dimens, 0);

        Mat mImage(height, width, CV_8UC4, (unsigned char *) _image);
        Mat mKeypoints(height, width, CV_8UC4, (unsigned char *) _image);
        //Mat mgray(height, width, CV_8UC1, (unsigned char *)_yuv);

        //Please make attention about BGRA byte order
        //ARGB stored in java as int array becomes BGRA at native level
//        cvtColor(mImage, mKeypoints, CV_YUV420sp2BGR, 4);

        vector<KeyPoint> v;
        Mat descriptor;

        SiftFeatureDetector detector;
        detector.detect(mImage, v);
        detector.compute(mImage, v, descriptor);

        int size = v.size() * (2 + descriptor.cols);
        result = env->NewFloatArray(size);
        jfloat fill[size];
        int ptr = 0;

        for( size_t i = 0; i < v.size(); i++ ) {
//            circle(mImage, Point(v[i].pt.x, v[i].pt.y), 10, Scalar(0,0,255,255));
            fill[ptr] = v[i].pt.x; ptr ++;
            fill[ptr] = v[i].pt.y; ptr ++;
            for (size_t j = 0; j<descriptor.cols; j++){
                fill[ptr] = (float) descriptor.at<float>(i,j); ptr++;
            }
        }

        _dimens[0] = v.size();
        _dimens[1] = descriptor.cols;


        env->ReleaseIntArrayElements(dimens, _dimens, 0);
        env->ReleaseShortArrayElements(image, _image, 0);
        env->SetFloatArrayRegion(result, 0, size, fill);

        return result;
    }