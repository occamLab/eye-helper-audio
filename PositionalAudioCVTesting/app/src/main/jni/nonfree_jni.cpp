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
    JNIEXPORT void JNICALL Java_com_eyehelper_positionalaudiocvtesting_SIFTImpl_runSIFT(JNIEnv* env, jobject thiz, jint width, jint height, jbyteArray yuv, jintArray bgra);
};

 JNIEXPORT void JNICALL Java_com_eyehelper_positionalaudiocvtesting_SIFTImpl_runSIFT(JNIEnv* env, jobject thiz, jint width, jint height, jbyteArray yuv, jintArray bgra)
    {
        jbyte* _yuv  = env->GetByteArrayElements(yuv, 0);
        jint*  _bgra = env->GetIntArrayElements(bgra, 0);

        Mat myuv(height + height/2, width, CV_8UC1, (unsigned char *)_yuv);
        Mat mbgra(height, width, CV_8UC4, (unsigned char *)_bgra);
        Mat mgray(height, width, CV_8UC1, (unsigned char *)_yuv);

        //Please make attention about BGRA byte order
        //ARGB stored in java as int array becomes BGRA at native level
        cvtColor(myuv, mbgra, CV_YUV420sp2BGR, 4);

        vector<KeyPoint> v;
        Mat descriptors;

        SiftFeatureDetector detector;
        detector.detect(myuv, v);
        detector.compute(myuv, v, descriptors);

        for( size_t i = 0; i < v.size(); i++ )
            circle(mbgra, Point(v[i].pt.x, v[i].pt.y), 10, Scalar(0,0,255,255));

        env->ReleaseIntArrayElements(bgra, _bgra, 0);
        env->ReleaseByteArrayElements(yuv, _yuv, 0);
    }