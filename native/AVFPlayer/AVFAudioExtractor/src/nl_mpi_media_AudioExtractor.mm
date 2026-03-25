#import "nl_mpi_media_AudioExtractor.h"
#import "AudioExtractor.h"
#import "JAVFConnector.h"
#import <jni.h>
#import <jni_md.h>
#import <mpi_jni_util.h>

using namespace std;

#ifdef __cplusplus
extern "C" {
#endif

	/*
	 * Initializes an AVFAudioExtractor and links it to a global reference to the caller class.
	 * Class:     nl_mpi_media_AudioExtractor
	 * Method:    initNative
	 * Signature: (Ljava/lang/String;)J
	 */
	JNIEXPORT jlong JNICALL Java_nl_mpi_media_AudioExtractor_initNative
	(JNIEnv *env, jobject callerObj, jstring mediaPath) {
		// convert string
        const char *mediaURLChars = env->GetStringUTFChars(mediaPath, NULL);
        // convert jstring to NSString
        NSString *urlString = [NSString stringWithUTF8String:mediaURLChars];
        mjlogfWE(env, "N_AVFAudioExtractor.initNative: media URL string: %s", [urlString UTF8String]);
        //
        NSURL *mediaNSURL = NULL;
        BOOL fileProt = [urlString hasPrefix:@"file:"];
        BOOL absFile = [urlString hasPrefix:@"/"];
        
        if (fileProt || absFile) {
            mediaNSURL = [NSURL fileURLWithPath:urlString isDirectory:NO];
        } else {
            mediaNSURL = [NSURL URLWithString:urlString];
        }
        // initialize assets
        AudioExtractor *audioGen = [[AudioExtractor alloc] initWithURL: mediaNSURL];
        jlong retValue = 0;
        
        if ([audioGen lastError] != nil) {
            mjlogfWE(env, "N_AVFAudioExtractor.initNative: unable to create an audio extractor: %s", [[audioGen lastError] description] != nil ? [[[audioGen lastError] description] UTF8String] : "unknown error");
            //audioGen = 0;
        } else {
            JAVFConnector *auConnector = new JAVFConnector();
            auConnector->audioExtractor = audioGen;
            auConnector->jRef = env->NewGlobalRef(callerObj);
            
            retValue = (jlong) auConnector;
        }
        
        env->ReleaseStringUTFChars(mediaPath, mediaURLChars);
        
        if ([AudioExtractor isDebugMode]) {
            mjlogfWE(env, "N_AVFAudioExtractor.initNative: created audio extractor with id: %lld", retValue);
        }

		return retValue;
	}

	/*
	 * Returns the sample frequency of the audio track.
	 * Class:     nl_mpi_media_AudioExtractor
	 * Method:    getSampleFrequency
	 * Signature: (J)I
	 */
	JNIEXPORT jint JNICALL Java_nl_mpi_media_AudioExtractor_getSampleFrequency
	(JNIEnv *env, jobject callerObj, jlong objId) {
        JAVFConnector *auConnector = (JAVFConnector *) objId;
        
        if (auConnector) {
            AudioExtractor *audioGen = auConnector->audioExtractor;
            
            if ([AudioExtractor isDebugMode]) {
                mjlogfWE(env, "N_AVFAudioExtractor.getSampleFrequency: sample frequency: %.2f", [audioGen sampleFreq]);
            }
            
            return (jint) [audioGen sampleFreq];
        }
        
		return 0;
	}

	/*
	 * Returns the bits per sample value of the audio.
	 * Class:     nl_mpi_media_AudioExtractor
	 * Method:    getBitsPerSample
	 * Signature: (J)I
	 */
	JNIEXPORT jint JNICALL Java_nl_mpi_media_AudioExtractor_getBitsPerSample
	(JNIEnv *env, jobject callerObj, jlong objId) {
        JAVFConnector *auConnector = (JAVFConnector *) objId;
        
        if (auConnector) {
            AudioExtractor *audioGen = auConnector->audioExtractor;
            
            if ([AudioExtractor isDebugMode]) {
                mjlogfWE(env, "N_AVFAudioExtractor.getBitsPerSample: bits per sample: %d", [audioGen bitsPerSample]);
            }
            
            return [audioGen bitsPerSample];
        }
        
		return 0;
	}

	/*
	 * Returns the number of audio channels.
	 * Class:     nl_mpi_media_AudioExtractor
	 * Method:    getNumberOfChannels
	 * Signature: (J)I
	 */
	JNIEXPORT jint JNICALL Java_nl_mpi_media_AudioExtractor_getNumberOfChannels
	(JNIEnv *env, jobject callerObj, jlong objId) {
        JAVFConnector *auConnector = (JAVFConnector *) objId;
        
        if (auConnector) {
            AudioExtractor *audioGen = auConnector->audioExtractor;
            
            if ([AudioExtractor isDebugMode]) {
                mjlogfWE(env, "N_AVFAudioExtractor.getNumberOfChannels: number of channels: %d", [audioGen numberOfChannels]);
            }
            
            return [audioGen numberOfChannels];
        }

		return 0;
	}

	/*
	 * Returns the media duration in milliseconds. 
	 * Class:     nl_mpi_media_AudioExtractor
	 * Method:    getDurationMs
	 * Signature: (J)J
	 */
	JNIEXPORT jlong JNICALL Java_nl_mpi_media_AudioExtractor_getDurationMs
	(JNIEnv *env, jobject callerObj, jlong objId) {
        JAVFConnector *auConnector = (JAVFConnector *) objId;
        
        if (auConnector) {
            AudioExtractor *audioGen = auConnector->audioExtractor;
            
            if ([AudioExtractor isDebugMode]) {
                mjlogfWE(env, "N_AVFAudioExtractor.getDurationMs: duration in milliseconds: %ld", [audioGen mediaDurationMs]);
            }
            
            return (jlong) [audioGen mediaDurationMs];
        }

		return 0;
	}

	/*
	 * Returns the media duration in seconds.
	 * Class:     nl_mpi_media_AudioExtractor
	 * Method:    getDurationSec
	 * Signature: (J)D
	 */
	JNIEXPORT jdouble JNICALL Java_nl_mpi_media_AudioExtractor_getDurationSec
	(JNIEnv *env, jobject callerObj, jlong objId) {
        JAVFConnector *auConnector = (JAVFConnector *) objId;
        
        if (auConnector) {
            AudioExtractor *audioGen = auConnector->audioExtractor;
            
            if ([AudioExtractor isDebugMode]) {
                mjlogfWE(env, "N_AVFAudioExtractor.getDurationSec: duration in seconds: %.4f", [audioGen mediaDurationSeconds]);
            }
            
            return [audioGen mediaDurationSeconds];
        }

		return 0.0;
	}

	/*
	 * Returns the (random) size in bytes of the first copied buffer. The AV Foundation
     * creates buffers of different sizes for read and decode actions (possibly depending
     * on the compression and chunk size of the source audio?).
	 * Class:     nl_mpi_media_AudioExtractor
	 * Method:    getSampleBufferSize
	 * Signature: (J)J
	 */
	JNIEXPORT jlong JNICALL Java_nl_mpi_media_AudioExtractor_getSampleBufferSize
	(JNIEnv *env, jobject callerObj, jlong objId) {
        JAVFConnector *auConnector = (JAVFConnector *) objId;
        
        if (auConnector) {
            AudioExtractor *audioGen = auConnector->audioExtractor;
            
            if ([AudioExtractor isDebugMode]) {
                mjlogfWE(env, "N_AVFAudioExtractor.getSampleBufferSize: buffer size: %ld", [audioGen bytesPerSampleBuffer]);
            }
            
            return (jlong) [audioGen bytesPerSampleBuffer];
        }
        
		return 0;
	}

	/*
	 * Returns the (random) duration of the first copied buffer in milliseconds, the actual
     * duration of each copied buffer varies.
	 * Class:     nl_mpi_media_AudioExtractor
	 * Method:    getSampleBufferDurationMs
	 * Signature: (J)J
	 */
	JNIEXPORT jlong JNICALL Java_nl_mpi_media_AudioExtractor_getSampleBufferDurationMs
	(JNIEnv *env, jobject callerObject, jlong objId) {
        JAVFConnector *auConnector = (JAVFConnector *) objId;

        if (auConnector) {
            AudioExtractor *audioGen = auConnector->audioExtractor;
            
            if ([AudioExtractor isDebugMode]) {
                mjlogfWE(env, "N_AVFAudioExtractor.getSampleBufferDurationMs: buffer duration: %ld (ms)", (jlong) ([audioGen durationSecPerSampleBuffer] * 1000));
            }
            
            return (jlong) ([audioGen durationSecPerSampleBuffer] * 1000);
        }
        
		return 0;
	}

	/*
	 * Returns the (random) duration of the first copied buffer in seconds, the actual
     * duration of each copied buffer varies.
	 * Class:     nl_mpi_media_AudioExtractor
	 * Method:    getSampleBufferDurationSec
	 * Signature: (J)D
	 */
	JNIEXPORT jdouble JNICALL Java_nl_mpi_media_AudioExtractor_getSampleBufferDurationSec
	(JNIEnv *env, jobject callerObject, jlong objId) {
        JAVFConnector *auConnector = (JAVFConnector *) objId;
        
        if (auConnector) {
            AudioExtractor *audioGen = auConnector->audioExtractor;
            
            if ([AudioExtractor isDebugMode]) {
                mjlogfWE(env, "N_AVFAudioExtractor.getSampleBufferDurationSec: buffer duration: %.4f (sec)", [audioGen durationSecPerSampleBuffer]);
            }
            
            return [audioGen durationSecPerSampleBuffer];
        }
        
		return 0.0;
	}

	/*
	 * Retrieves the decoded samples for the specified time span, copies them into
     * the provided ByteBuffer and returns the number of copied bytes.
	 * Class:     nl_mpi_media_AudioExtractor
	 * Method:    getSamples
	 * Signature: (JDDLjava/nio/ByteBuffer;)I
	 */
	JNIEXPORT jint JNICALL Java_nl_mpi_media_AudioExtractor_getSamples
	(JNIEnv *env, jobject callerObj, jlong objId, jdouble fromTime, jdouble toTime, jobject byteBuffer) {
        if (toTime - fromTime <= 0) {
            return 0;
        }
        JAVFConnector *auConnector = (JAVFConnector *) objId;
        int numCopied = 0;
        
        if (auConnector) {
            AudioExtractor *audioGen = auConnector->audioExtractor;
            char * bufAddress = (char *) env->GetDirectBufferAddress(byteBuffer);
            size_t bufCapacity = (size_t) env->GetDirectBufferCapacity(byteBuffer);
            
            numCopied = [audioGen getSamplesFromTime:fromTime duration:(toTime - fromTime) bufferAddress:bufAddress bufferLength:bufCapacity];
            
            if ([AudioExtractor isDebugMode]) {
                mjlogfWE(env, "N_AVFAudioExtractor.getSamples: from: %.4f, to: %.4f (sec), bytes copied: %d", fromTime, toTime, numCopied);
            }
        }
        
		return numCopied;
	}

    /*
     * Retrieves the decoded samples for the specified time span, copies them into
     * the provided byte array and returns the number of copied bytes.
     * Class:     nl_mpi_media_AudioExtractor
     * Method:    getSamplesBA
     * Signature: (JDD[B)I
     */
    JNIEXPORT jint JNICALL Java_nl_mpi_media_AudioExtractor_getSamplesBA
    (JNIEnv *env, jobject callerObj, jlong objId, jdouble fromTime, jdouble toTime, jbyteArray byteArray) {
        if (toTime - fromTime <= 0) {
            return 0;
        }
        JAVFConnector *auConnector = (JAVFConnector *) objId;
        int numCopied = 0;
        
        if (auConnector) {
            AudioExtractor *audioGen = auConnector->audioExtractor;
            size_t arrLength = (size_t) env->GetArrayLength(byteArray);
            jbyte* byteAddress = env->GetByteArrayElements(byteArray, NULL);
            if (byteAddress == NULL) {
                return 0;//OutOfMemoryException?
            }
            numCopied =  [audioGen getSamplesFromTime:fromTime duration:(toTime - fromTime) bufferAddress:(char *)byteAddress bufferLength:arrLength];

            env->ReleaseByteArrayElements(byteArray, byteAddress, 0);
            
            if ([AudioExtractor isDebugMode]) {
                mjlogfWE(env, "N_AVFAudioExtractor.getSamplesBA: from: %.4f, to: %.4f (sec), bytes copied: %d", fromTime, toTime, numCopied);
            }
        }
        
        return numCopied;
    }
                                                                                                    

	/*
	 * Retrieves a sample and copies it to the ByteBuffer.
     * The decoder fills a native buffer which usually is much larger than the number
     * of bytes required for a single sample. This method attempts to read an interval
     * with the duration of a default sample buffer.
	 * Returns the number of copied bytes.
	 * Class:     nl_mpi_media_AudioExtractor
	 * Method:    getSample
	 * Signature: (JDLjava/nio/ByteBuffer;)I
	 */
	JNIEXPORT jint JNICALL Java_nl_mpi_media_AudioExtractor_getSample
	(JNIEnv *env, jobject callerObj, jlong objId, jdouble fromTime, jobject byteBuffer) {
        JAVFConnector *auConnector = (JAVFConnector *) objId;
        
        if (auConnector) {
            double dur = [auConnector->audioExtractor durationSecPerSampleBuffer];
            
            return Java_nl_mpi_media_AudioExtractor_getSamples(env, callerObj, objId, fromTime, fromTime + dur, byteBuffer);
        }

		return 0;
	}


    /*
     * Retrieves a sample and copies it to the byte array.
     * The decoder fills a native buffer which usually is much larger than the number
     * of bytes required for a single sample. This method attempts to read an interval
     * with the duration of a default sample buffer.
     * Returns the number of copied bytes.
     * Class:     nl_mpi_media_AudioExtractor
     * Method:    getSampleBA
     * Signature: (JD[B)I
     */
    JNIEXPORT jint JNICALL Java_nl_mpi_media_AudioExtractor_getSampleBA
    (JNIEnv *env, jobject callerObj, jlong objId, jdouble fromTime, jbyteArray byteArray) {
        JAVFConnector *auConnector = (JAVFConnector *) objId;
        
        if (auConnector) {
            double dur = [auConnector->audioExtractor durationSecPerSampleBuffer];
            
            return Java_nl_mpi_media_AudioExtractor_getSamplesBA(env, callerObj, objId, fromTime, fromTime + dur, byteArray);
        }

        return 0;
    }

	/*
     * Passes the call to the native implementation, though it is currently ignored
     * there. An interval has to be provided and there doesn't seem to be any
     * performance gain in passing a random interval with the given start time
     * without actually reading the bytes.
	 * Class:     nl_mpi_media_AudioExtractor
	 * Method:    setPositionSec
	 * Signature: (JD)Z
	 */
	JNIEXPORT jboolean JNICALL Java_nl_mpi_media_AudioExtractor_setPositionSec
	(JNIEnv *env, jobject callerObject, jlong objId, jdouble seekTime) {
        JAVFConnector *auConnector = (JAVFConnector *) objId;
        
        if (auConnector) {
            AudioExtractor *audioGen = auConnector->audioExtractor;
            
            return (jboolean) [audioGen setPositionSec:seekTime];
            //return JNI_TRUE;
        }
		return JNI_FALSE;
	}

	/*
	 * Deletes the global reference and the AudioExtractor instance.
	 * Class:     nl_mpi_media_AudioExtractor
	 * Method:    release
	 * Signature: (J)V
	 */
	JNIEXPORT void JNICALL Java_nl_mpi_media_AudioExtractor_release
	(JNIEnv *env, jobject callerObj, jlong objId) {
		// delete global ref etc
        JAVFConnector *auConnector = (JAVFConnector *) objId;
        
        if (auConnector) {
            AudioExtractor *audioGen = auConnector->audioExtractor;
            
            if ([AudioExtractor isDebugMode]) {
                mjlogfWE(env, "N_AVFAudioExtractor.release: releasing reader with id: %ld", objId);
            }
            
            env->DeleteGlobalRef(auConnector->jRef);
            [audioGen releaseReader];
            
            free(auConnector);
        }

	}

	/*
	 * Enables or disables debug mode.
	 * Class:     nl_mpi_media_AudioExtractor
	 * Method:    setDebugMode
	 * Signature: (Z)V
	 */
	JNIEXPORT void JNICALL Java_nl_mpi_media_AudioExtractor_setDebugMode
	(JNIEnv *env, jclass callerClass, jboolean debugMode) {
        [AudioExtractor setDebugMode: (BOOL) (debugMode != 0)];
	}

	/*
	 * Returns whether the debug mode is on or off.
	 * Class:     nl_mpi_media_AudioExtractor
	 * Method:    isDebugMode
	 * Signature: ()Z
	 */
	JNIEXPORT jboolean JNICALL Java_nl_mpi_media_AudioExtractor_isDebugMode
	(JNIEnv *env, jclass callerClass) {
        return (jboolean) [AudioExtractor isDebugMode];
	}

    /*
     * Tries to setup a class and methodID reference for native code to log to.
     * Class:     nl_mpi_media_AudioExtractor
     * Method:    initLog
     * Signature: (Ljava/lang/String;Ljava/lang/String;)V
     */
    JNIEXPORT void JNICALL Java_nl_mpi_media_AudioExtractor_initLog
    (JNIEnv *env, jclass callerClass, jstring logClassName, jstring logMethodName) {
        const char *clChars = env->GetStringUTFChars(logClassName, NULL);
        if (clChars == NULL) {
            return; // OutOfMemory thrown
        }
        const char *methChars = env->GetStringUTFChars(logMethodName, NULL);
        if (methChars == NULL) {
            return; // OutOfMemory thrown
        }
        mpijni_initLog(env, clChars, methChars);
        if ([AudioExtractor isDebugMode]) {
            mjlogfWE(env, "N_AudioExtractor.initLog: set Java log method: %s - %s", clChars, methChars);
        }
        
        // free the allocated memory
        env->ReleaseStringUTFChars(logClassName, clChars);
        env->ReleaseStringUTFChars(logMethodName, methChars);
    }

#ifdef __cplusplus
}
#endif
