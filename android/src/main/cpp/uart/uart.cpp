#include <fcntl.h>
#include <termios.h>
#include <unistd.h>
#include "uart.h"

#include "android/log.h"

static const char *TAG="serial_port";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

typedef struct speed_array_tag{
	jint speed;
	speed_t speed_bits;
}speed_array;

speed_array g_speed_array[] = {
		{0,       B0},
		{50,      B50},
		{75,      B75},
		{110,     B110},
		{134,     B134},
		{150,     B150},
		{200,     B200},
		{300,     B300},
		{600,     B600},
		{1200,    B1200},
		{1800,    B1800},
		{2400,    B2400},
		{4800,    B4800},
		{9600,    B9600},
		{19200,   B19200},
		{38400,   B38400},
		{57600,   B57600},
		{115200,  B115200},
		{230400,  B230400},
		{460800,  B460800},
		{500000,  B500000},
		{576000,  B576000},
		{921600,  B921600},
		{1000000, B1000000},
		{1152000, B1152000},
		{1500000, B1500000},
		{2000000, B2000000},
		{2500000, B2500000},
		{3000000, B3000000},
		{3500000, B3500000},
		{4000000, B4000000}
};

JNIEXPORT jobject JNICALL Java_com_aerobiosys_flutteruart_Uart_open
  (JNIEnv *env, jclass thiz, jstring path, jint baudrate)
{
	int uart_fd;
	speed_t uart_speed;
	jobject mFileDescriptor;

	/* Find Baud Rate*/
	for(int i = 0; i < sizeof(g_speed_array)/sizeof(speed_array); i++)
		if(baudrate == g_speed_array[i].speed)
			uart_speed = g_speed_array[i].speed_bits;

	const char *uart_path = env->GetStringUTFChars(path, NULL);
	uart_fd = open(uart_path, O_RDWR);
	LOGD("Opening UART %s with flags 0x%x, fd = %d", uart_path, O_RDWR, uart_fd);
	env->ReleaseStringUTFChars(path, uart_path);
	if (uart_fd == -1)
	{
		LOGE("Unable to Open UART");
		return NULL;
	}

    /* Configuration of UART */
	{
		struct termios uart_config;
		bool ret_val = JNI_FALSE;
		LOGD("Configuring UART");
		if (0 == tcgetattr(uart_fd, &uart_config))
		{
			cfmakeraw(&uart_config);
			cfsetispeed(&uart_config, uart_speed);
			cfsetospeed(&uart_config, uart_speed);
			if (0 == tcsetattr(uart_fd, TCSANOW, &uart_config))
			{
				ret_val = JNI_TRUE;
			}
		}

		if(ret_val != JNI_TRUE)
		{
			LOGD("Configuring serial port Failed");
			close(uart_fd);
		}

	}
	/* Creating file descriptor for Java*/
	{
		jclass cFileDescriptor = env->FindClass("java/io/FileDescriptor");
		mFileDescriptor = env->NewObject(cFileDescriptor, env->GetMethodID(cFileDescriptor, "<init>", "()V"));
		env->SetIntField(mFileDescriptor, env->GetFieldID(cFileDescriptor, "descriptor", "I"), (jint)uart_fd);
	}


	return mFileDescriptor;
}

JNIEXPORT void JNICALL Java_com_aerobiosys_flutteruart_Uart_close
  (JNIEnv *env, jobject thiz)
{
	jclass SerialPortClass = env->GetObjectClass(thiz);
	jclass FileDescriptorClass = env->FindClass("java/io/FileDescriptor");

	jfieldID mFdID = env->GetFieldID(SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
	jfieldID descriptorID = env->GetFieldID(FileDescriptorClass, "descriptor", "I");

	jobject mFd = env->GetObjectField(thiz, mFdID);
	jint descriptor = env->GetIntField(mFd, descriptorID);

	LOGD("close(fd = %d)", descriptor);
	close(descriptor);
}

