package com.aerobiosys.flutteruart;

import androidx.annotation.NonNull;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.BinaryMessenger;

import com.aerobiosys.flutteruart.*;

/** FlutteruartPlugin */
public class FlutteruartPlugin implements FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {

  private int m_InterfaceId;
  private final UartService[] mSerialPort = {null, null};
  private EventChannel m_EventChannel;
  private EventChannel.EventSink m_EventSink;
  private BinaryMessenger m_Messenger;
  private Handler m_handler;

  private void beginUart(Result result, int uartId) {
    m_InterfaceId = uartId;
    try {
      int i =0;
      for(String devicePath : Configs.uartdevicePath) {
        mSerialPort[i] = new UartService(new File(devicePath), mReaderCallback);
        i++;
      }
      result.success(true);
    } catch (SecurityException e) {
      result.error("E001", "Read/Write permission denied", null);
    } catch (IOException e) {
      result.error("E002", "Port cannot be opened", null);
    } catch (InvalidParameterException e) {
      result.error("E003", "Serial port not configured", null);
    }
  }

  private final UartService.ReaderCallback mReaderCallback = new UartService.ReaderCallback() {
    @Override
    public void onDataReceived(byte[] buffer, int size) {
      if ( m_EventSink != null ) {
        m_handler.post(new Runnable() {
          @Override
          public void run() {
            if ( m_EventSink != null ) {
              m_EventSink.success(buffer);
            }
          }
        });
      }
    }
  };

  private boolean write(byte[]  writeString) {
    return mSerialPort[m_InterfaceId].writeData(writeString);
  }

  private void closeUart()
  {
    mSerialPort[m_InterfaceId].closeSerialPort();
  }


  /// The EventChannel that will the communication between Flutter and native Android
  @Override
  public void onListen(Object o, EventChannel.EventSink eventSink) {
    m_EventSink = eventSink;
  }

  @Override
  public void onCancel(Object o) {
    m_EventSink = null;
  }

  ///Register Binary Messenger
  private void register(BinaryMessenger messenger) {
    m_handler = new Handler(Looper.getMainLooper());
    m_Messenger = messenger;
    m_EventChannel = new EventChannel(m_Messenger, "flutteruart/stream");
    m_EventChannel.setStreamHandler(this);
  }

  private void unregister() {
    m_EventChannel.setStreamHandler(null);
    m_Messenger = null;
  }

  // Used to load the 'native-lib' library on application startup.
  static {
    System.loadLibrary("native-lib");
  }
  
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    register(flutterPluginBinding.getBinaryMessenger());
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutteruart");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {

      case "begin":
        beginUart(result, call.argument("uartId"));
        break;

      case "write":
        result.success(write(call.argument("data")));
        break;

      case "close":
        closeUart();
        break;

      default:
        result.notImplemented();
        break;
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    unregister();
    channel.setMethodCallHandler(null);
  }
}
