package com.eyehelper.positionalaudiocvtesting;

/**
 * Created by sihrc on 10/4/14.
 */

public class SIFTImpl {
    static
    {
        try
        {
            // Load necessary libraries.
            System.loadLibrary("opencv_java");
            System.loadLibrary("nonfree");
            System.loadLibrary("nonfree_jni");
        }
        catch( UnsatisfiedLinkError e )
        {
            System.err.println("Native code library failed to load.\n" + e);
        }
    }
    public static native void runSIFT();
}