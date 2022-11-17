package com.aerobiosys.flutteruart;

//import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

/// The callback interface

public class UartService extends android.app.Application {

    //private static final String TAG = "uart";
    private Uart mUart = null;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;

    public interface ReaderCallback {
        void onDataReceived(final byte[] buffer, final int size);
    }

    private ReaderCallback mCallback = null;

    private class ReadThread extends Thread {

        int count = 0;
        @Override
        public void run() {
            super.run();
            while(!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[64];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);
                    count ++;
                    //Log.e(TAG,"Size " + size + ", Count" + count + ", Buffer "+  buffer.toString());
                    if (size > 0) {
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public boolean writeData(byte[] writeText){
        /*Serial Port Write Implementation*/
        try {
            mOutputStream.write(writeText);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean writeData(String writeText){
        /*Serial Port Write Implementation*/
        try {
            mOutputStream.write(writeText.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void onDataReceived(final byte[] buffer, final int size){
        /*Serial Port Read Implementation*/
        if(mCallback != null) {
            mCallback.onDataReceived(buffer, size);
        }
    }

    public Uart openSerialPort(File device, ReaderCallback callback, int baudRate) throws SecurityException, IOException, InvalidParameterException {
        if (mUart == null) {
            /* Read serial port parameters */

            /* Open the serial port */
            mUart = new Uart(device, baudRate);
            mInputStream = mUart.getInputStream();
            mOutputStream = mUart.getOutputStream();
            mCallback = callback;

            mReadThread = new ReadThread();
            mReadThread.start();

        }
        return mUart;
    }

    public void closeSerialPort() {

        if (mReadThread != null)
            mReadThread.interrupt();
        mCallback = null;

        if (mUart != null) {
            mUart.close();
            mUart = null;
        }
    }

    public UartService(File device, ReaderCallback callback, int baudRate) throws SecurityException, IOException {
        openSerialPort(device, callback, baudRate);
    }
}