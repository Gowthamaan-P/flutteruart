#ifndef DEMO_UART_H
#define DEMO_UART_H

#include <jni.h>

#ifdef __cplusplus

extern "C" {
#endif

JNIEXPORT jobject JNICALL Java_com_aerobiosys_flutteruart_Uart_open
  (JNIEnv *, jclass, jstring, jint);

JNIEXPORT void JNICALL Java_com_aerobiosys_flutteruart_Uart_close
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif

#endif //DEMO_UART_H
