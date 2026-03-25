/*
 * Project: MPI JNI utilities
 * Author:  Han Sloetjes
 * Version: April 2021
 */
#include "mpi_jni_util.h"
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <wchar.h>

#ifdef __cplusplus
extern "C" {
#endif

// global variables
JavaVM * mj_jvmGl;
jclass mj_logClassGlb = NULL;
jmethodID mj_logMID   = NULL;

/*
* Helper function for converting jstring to wchar_t. From Eclipse, IBM
*/
JNIEXPORT wchar_t * JNICALL mpijni_ConvertToWChars
	(JNIEnv *env, jstring str) {
	//get the string and its length into original and len
	// is this good enough for localization?
	const jchar * original = env->GetStringChars(str, 0);
	const jsize len = env->GetStringLength(str);

	//allocate extra one for the null
	wchar_t *converted = new wchar_t[len + 1];

	//copy from original into converted
	memcpy(converted, original, sizeof(wchar_t)*len);
	env->ReleaseStringChars(str, original);

	//null terminate it
	converted[len] = 0;

	return converted;
}

JNIEXPORT jchar * JNICALL  mpijni_ConvertToJchar
	(JNIEnv *env, wchar_t *in, size_t *length) {
	//wprintf(L"In wchar: %s\n", in);
	size_t len = wcslen(in);
	*length = (size_t)len;
	jchar *out = new jchar[len];

	//copy from original into converted
	memcpy(out, in, sizeof(jchar)*len);

	return out;
}

JNIEXPORT void JNICALL mpijni_initLog(JNIEnv *env, const char *classDesc, const char *methodName) {
	if (mj_jvmGl == NULL) {
		int jvmInt = env->GetJavaVM(&mj_jvmGl);// 0 on success, < 0 in case of error
		if (jvmInt < 0) {
			// print an error message
			printf("Could not attach the native library to the Java logging %d", jvmInt);
			fflush(stdout);
			return;
		}
	}
    if (mj_logClassGlb == NULL) {
        jclass localLogRef = NULL;
        localLogRef = env->FindClass(classDesc);
        if (localLogRef != NULL) {
            mj_logClassGlb = (jclass)env->NewGlobalRef(localLogRef);
            mj_logMID = env->GetStaticMethodID(mj_logClassGlb, methodName, "(Ljava/lang/String;)V");
            // clean up
            env->DeleteLocalRef(localLogRef);
        }
    }
}

JNIEXPORT void JNICALL mjlog(const char *msg) {
    if (mj_logClassGlb != NULL) {
        JNIEnv *env;
        mj_jvmGl->AttachCurrentThread((void **)&env, NULL);

        jstring mesgStr = env->NewStringUTF(msg);
        if (mesgStr != NULL) {
            env->CallStaticVoidMethod(mj_logClassGlb, mj_logMID, mesgStr);
            env->DeleteLocalRef(mesgStr); // not really necessary
        }
    }
}

JNIEXPORT void JNICALL mjlogf(const char *msg, ...) {
	char result [256];
	va_list args;
	va_start(args, msg);
	int num = vsnprintf(result, 256, msg, args);
	va_end(args);
	if (num < 0) {
		return;
	}
	mjlog(result);
}

JNIEXPORT void JNICALL mjlogWE(JNIEnv *env, const char *msg) {
	if (mj_logMID != NULL) {
		jstring mesgStr = env->NewStringUTF(msg);
        if (mesgStr != NULL) {
            env->CallStaticVoidMethod(mj_logClassGlb, mj_logMID, mesgStr);
            env->DeleteLocalRef(mesgStr);
        }
	}
}

JNIEXPORT void JNICALL mjlogfWE(JNIEnv *env, const char *msg, ...) {
    if (mj_logMID != NULL) {
        char result [256];
        va_list args;
        va_start(args, msg);
        int num = vsnprintf(result, 256, msg, args);
        va_end(args);
        if (num < 0) {
            return;
        }
        jstring mesgStr = env->NewStringUTF(result);
        if (mesgStr != NULL) {
            env->CallStaticVoidMethod(mj_logClassGlb, mj_logMID, mesgStr);
            env->DeleteLocalRef(mesgStr);
        }
    }
}

JNIEXPORT void JNICALL mjlogJS(JNIEnv *env, jstring msg) {
	if (mj_logMID != NULL) {
		env->CallStaticVoidMethod(mj_logClassGlb, mj_logMID, msg);
	}
}

#ifdef __cplusplus
}
#endif
