/* 
 * Project:	MPI JNI utilities
 * Author:	Han Sloetjes
 * Version: April 2021
 */

#ifndef included_MPIJNIUtil
#define included_MPIJNIUtil

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif
/*
* Converts a jstring to a wchar_t array/string. 
*/
JNIEXPORT wchar_t * JNICALL mpijni_ConvertToWChars
	(JNIEnv *, jstring);

/*
* Converts a wchar_t array to a jchar array.
*/
JNIEXPORT jchar * JNICALL mpijni_ConvertToJchar
	(JNIEnv *, wchar_t *, size_t *);

/*
* Initializes a few global variables for logging to a Java logger.
* The second parameter should be the JNI class descriptor
* to the Java class exposing a static method accepting one String
* parameter.
* The third method should hold the name of the method to call
*/
JNIEXPORT void JNICALL mpijni_initLog(JNIEnv *, const char *, const char *);

/*
* Logs a message to a logger in Java. Uses a global reference
* to the Java_JVM to obtain a JNIEnv pointer.
*/
JNIEXPORT void JNICALL mjlog(const char *);

/*
* Formats and logs a message to a logger in Java. Uses a global
* reference to the Java_JVM to obtain a JNIEnv pointer.
*/
JNIEXPORT void JNICALL mjlogf(const char *, ...);

/*
* Logs a message to a logger in Java, using an already available
* JNIEnv pointer.
*/
JNIEXPORT void JNICALL mjlogWE(JNIEnv *, const char *);

/*
* Formats and Logs a message to a logger in Java, using an already
* available JNIEnv pointer.
*/
JNIEXPORT void JNICALL mjlogfWE(JNIEnv *, const char *, ...);

/*
* Logs a jstring message to a logger in Java, using an already
* available JNIEnv pointer.
*/
JNIEXPORT void JNICALL mjlogJS(JNIEnv *, jstring);


/*
 Could have a function to replace stdout by a file to write to,
 in combination with the usual printf() etc. functions
 
 freopen("/Users/Shared/Temp/elanout.log", "a+", stdout);
 printf("Native info message: %s", info);
 */
#ifdef __cplusplus
}
#endif

#endif
