package com.aerobiosys.flutteruart;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import android.util.Log;

public class Uart {

    private static final String TAG = "Uart";
    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public Uart(File device, int baudRate) throws SecurityException, IOException {

        if (!device.canRead() || !device.canWrite()) {
            Log.e(TAG, "UART Permission not granted");
            throw new SecurityException();
        }

        mFd = open(device.getAbsolutePath(), baudRate);
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    // JNI
    private native static FileDescriptor open(String path, int baudRate);
    public native void close();
    static {
        System.loadLibrary("native-lib");
    }
}