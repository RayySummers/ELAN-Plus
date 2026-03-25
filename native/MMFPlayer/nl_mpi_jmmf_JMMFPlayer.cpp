/* 
 * Project:	JMMFPlayer, Microsoft Media Foundation Player for Java
 * Author:	Han Sloetjes
 * Version:	1.0, May 2011
 */
#include <jawt.h>
#include <jawt_md.h>
#include "nl_mpi_jmmf_JMMFPlayer.h"
#include "MMFPlayer.h"
#include "mpi_jni_util.h"
#include "MMFUtil.h"

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    createPlayerFor
 * Signature: (JLjava/lang/String;Ljava/awt/Component;)Z
 */
JNIEXPORT jboolean JNICALL Java_nl_mpi_jmmf_JMMFPlayer_createPlayerFor
	(JNIEnv *env, jobject thisObj, jlong id, jstring filepath, jobject compObj) {
	wchar_t *str;
	str = mpijni_ConvertToWChars(env, filepath);
	//printf("MMF JNI createPlayerFor: media path is: %ls\n", str);
	mjlogWE(env, "N_JMMFPlayer.createPlayerFor: ");
	mjlogJS(env, filepath);
	// try, catch exception
	MMFPlayer* dsp = (MMFPlayer*)id;

	if (dsp != NULL) {		
		// get the window handle of compObj
		JAWT awt;
		awt.version = JAWT_VERSION_9;
		JAWT_DrawingSurface* ds;
		JAWT_DrawingSurfaceInfo* dsi;
		JAWT_Win32DrawingSurfaceInfo* dsi_win;
		jboolean result;
		jint lock;
		HWND hwnd;

		result = JAWT_GetAWT(env, &awt);
		if (result == JNI_FALSE) {
			mjlogfWE(env, "N_JMMFPlayer.createPlayerFor: unable to get the AWT, id %lld", id);
			delete(str);
			return (jlong)-1;
		}
		else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogfWE(env, "N_JMMFPlayer.createPlayerFor: successfully got the AWT, id %lld", id);
			}
		}
		ds = awt.GetDrawingSurface(env, compObj);
		if (ds == NULL) {
			mjlogfWE(env, "N_JMMFPlayer.createPlayerFor: unable to get the Drawing Surface, id %lld", id);
			delete(str);
			return (jlong)-1;
		}
		else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogfWE(env, "N_JMMFPlayer.createPlayerFor: successfully got the Drawing Surface, id %lld", id);
			}
		}
		lock = ds->Lock(ds);
		if ((lock & JAWT_LOCK_ERROR) != 0) {
			mjlogfWE(env, "N_JMMFPlayer.createPlayerFor: unable to get the lock of the Drawing Surface, id %lld", id);
			delete(str);
			return (jlong)-1;
		}
		else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogfWE(env, "N_JMMFPlayer.createPlayerFor: successfully got the lock of the Drawing Surface, id %lld", id);
			}
		}

		// Get the drawing surface info
		dsi = ds->GetDrawingSurfaceInfo(ds);

		// Get the platform-specific drawing info
		dsi_win = (JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo;

		// get the handle
		hwnd = (HWND) dsi_win->hwnd;
		//if (MMFUtil::JMMF_DEBUG) {
			//mjlogfWE(env, "N_JMMFPlayer.createPlayerFor: canvas HWND handle: %p, id %lld", hwnd, id);
		//}
		// Free the drawing surface info
		ds->FreeDrawingSurfaceInfo(dsi);

		// Unlock the drawing surface
		ds->Unlock(ds);

		// Free the drawing surface
		awt.FreeDrawingSurface(ds);
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.createPlayerFor: successfully retrieved the window handle HWND of the canvas: %p, id %lld", hwnd, id);
		}
		HRESULT hr;

		hr = dsp->initSession(str, hwnd);
		if (FAILED(hr)) {
			// throw JMMFException
			//jthrowable throwObj;
			jclass exClass;
			//jmethodID mid; 

			char *mes = "Unknown error occurred while creating a Microsoft Media Foundation player.\n";

			mjlogWE(env, "N_JMMFPlayer.createPlayerFor: an unknown error occurred while creating a Microsoft Media Foundation player");

			exClass = env->FindClass("nl/mpi/jmmf/JMMFException");
			//mid = env->GetMethodID(exClass, "<init>", "(Ljava/lang/String;)V");

			if (exClass != NULL) {
				env->ThrowNew(exClass, mes);
			} else {
				exClass = env->FindClass("java/lang/Exception");
				env->ThrowNew(exClass, mes);
			}

			delete(str);
			return JNI_FALSE;
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.createPlayerFor: player not found, id %lld", id);
		}
	}
	// MMFPlayer maintains its own copy
	delete(str);
	return JNI_TRUE;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    initPlayer
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_jmmf_JMMFPlayer_initPlayer
	(JNIEnv *env, jobject thisObj, jboolean synchronous) {
		mjlogfWE(env, "N_JMMFPlayer.initPlayer: WINVER: %ld", WINVER);

		MMFPlayer* jmmf = new MMFPlayer(synchronous != 0); // (bool) synchronous C4800 warning
		if (jmmf != NULL) {
			jmmf->jRef = (__int64)env->NewGlobalRef(thisObj);
			if (MMFUtil::JMMF_DEBUG) {
				mjlogfWE(env, "N_JMMFPlayer.initPlayer: player instance created, id %lld", (jlong)jmmf);
			}
		}
		else {
			mjlogWE(env, "N_JMMFPlayer.initPlayer: unable to create a player instance");
		}

		//if (MMFUtil::JMMF_DEBUG) {
		//	mjlogfWE(env, "N_JMMFPlayer.initPlayer: player instance created, %d", nextId);
		//}
		return (jlong) jmmf;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    start
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_start
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;

	if (dsp != NULL) {
		HRESULT hr = dsp->start();
		if (FAILED(hr)) {
			mjlogfWE(env, "N_JMMFPlayer.start: could not start the media, id %lld", id);
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.start: player not found, id %lld", id);
		}
	}
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    stop
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_stop
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;

	if (dsp != NULL) {
		HRESULT hr = dsp->stop();
		if (FAILED(hr)) {
			mjlogfWE(env, "N_JMMFPlayer.stop: could not stop the media, maybe it was already stopped, id %lld", id);
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.stop: player not found, id %lld", id);
		}
	}
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    pause
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_pause
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;

	if (dsp != NULL) {
		HRESULT hr = dsp->pause();
		if (FAILED(hr)) {
			mjlogfWE(env, "N_JMMFPlayer.pause: could not pause the media, id %lld", id);
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.pause: player not found, id %lld", id);
		}
	}
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    isPlaying
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_nl_mpi_jmmf_JMMFPlayer_isPlaying
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;
	jboolean playing = JNI_FALSE;

	if (dsp != NULL) {
		playing = (jboolean) dsp->isPlaying();
	}  else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.isPlaying: player not found, id %lld", id);
		}
	}

	return playing;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getState
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getState
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;

	if (dsp != NULL) {
		int state = dsp->getPlayerState();
		return (jint) state;
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.getState: player not found, id %lld", id);
		}
	}

	return -1;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    setRate
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_setRate
	(JNIEnv *env, jobject thisObj, jlong id, jfloat rate) {
	MMFPlayer* dsp = (MMFPlayer*)id;

	if (dsp != NULL) {
		// the play back rate is a double, the default is 1.0
		dsp->setRate(rate);
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.setRate: player not found, id %lld", id);
		}
	}
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getRate
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getRate
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;
	jfloat rate = 1.0;

	if (dsp != NULL) {
		// the play back rate is returned as a double, the default is 1.0
		rate = (jfloat) dsp->getRate();
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.getRate: player not found, id %lld", id);
		}
	}
	
	return rate;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    setVolume
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_setVolume
	(JNIEnv *env, jobject thisObj, jlong id, jfloat volume) {
	MMFPlayer* dsp = (MMFPlayer*)id;

	if (dsp != NULL) {
		// the volume is a float, between 0.0 and 1.0, the default is 1.0
		dsp->setVolume(volume);
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.setVolume: player not found, id %lld", id);
		}
	}
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getVolume
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getVolume
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;
	float volume = 1.0;

	if (dsp != NULL) {
		// the volume is a float, between 0.0 and 1.0, the default is 1.0
		volume = (jfloat) dsp->getVolume();
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.getVolume: player not found, id %lld", id);
		}
	}
	return (jfloat) 1;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    setMediaTime
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_setMediaTime
	(JNIEnv *env, jobject thisObj, jlong id, jlong time) {
	MMFPlayer* dsp = (MMFPlayer*)id;

	if (dsp != NULL) {
		// the default media time format is "reference time", units of 100 nano seconds
		dsp->setMediaPosition(time);
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.setMediaTime: player not found, id %lld", id);
		}
	}
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getMediaTime
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getMediaTime
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;
	jlong medTime = 0;

	if (dsp != NULL) {
		// the default media time format is "reference time", units of 100 nano seconds
		medTime = (jlong) dsp->getMediaPosition();
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.getMediaTime: player not found, id %lld", id);
		}
	}

	return medTime;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getDuration
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getDuration
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;

	if (dsp != NULL) {
		__int64 dur = dsp->getDuration();
		// the default media time format is "reference time", units of 100 nano seconds
		return (jlong) dur;
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.getDuration: player not found, id %lld", id);
		}
	}

	return (jlong) 0;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getFrameRate
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getFrameRate
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;
	jdouble frameRate = 1.0;

	if (dsp != NULL) {
		double timePerFrame = dsp->getTimePerFrame();

		if (timePerFrame > 0) {
			frameRate = (jdouble) (1 / timePerFrame);
		}
		else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogfWE(env, "N_JMMFPlayer.getFrameRate: invalid time per frame %f", timePerFrame);
			}
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.getFrameRate: player not found, id %lld", id);
		}
	}

	return frameRate;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getTimePerFrame
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getTimePerFrame
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;
	jdouble timePerFrame = 1.0;

	if (dsp != NULL) {
		timePerFrame = dsp->getTimePerFrame();
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.getTimePerFrame: player not found, id %lld", id);
		}
	}

	return timePerFrame;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getAspectRatio
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getAspectRatio
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;
	jfloat aspectRatio = 0;

	if (dsp != NULL) {
		long x = dsp->getOrgVideoWidth();
		long y = dsp->getOrgVideoHeight();

		if (y > 0) {
			aspectRatio = (jfloat) x / y;
		} else {
			mjlogfWE(env, "N_JMMFPlayer.getAspectRatio: cannot calculate the aspect ratio, height is 0, id %lld", id);
		}

	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.getAspectRatio: player not found, id %lld", id);
		}
	}

	return aspectRatio;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getOriginalSize
 * Signature: (J)Ljava/awt/Dimension;
 */
JNIEXPORT jobject JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getOriginalSize
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;
	jobject size;

	if (dsp != NULL) {
		long h = dsp->getOrgVideoHeight();

		if (h > 0) {
			long w = dsp->getOrgVideoWidth();
			
			jclass clazz = env->FindClass("java/awt/Dimension");
			jmethodID mid = env->GetMethodID(clazz, "<init>", "(II)V");
			size = env->NewObject(clazz, mid, (jint) w, (jint) h);
		} else {
			size = NULL;
			mjlogfWE(env, "N_JMMFPlayer.getOriginalSize: height is 0, id %lld", id);
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.getOriginalSize: player not found, id %lld", id);
		}
		size = NULL;
	}

	return size;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    setVisualComponent
 * Signature: (JLjava/awt/Component;)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_setVisualComponent
	(JNIEnv *env, jobject thisObj, jlong id, jobject compObj) {
	// if compObj is NULL set the video owner to the void pointer
		// this is problematic?
	if (compObj == NULL) {
		MMFPlayer* dsp = (MMFPlayer*)id;

		if (dsp != NULL) {

			HRESULT hr = dsp->setOwnerWindow(NULL);
			if (FAILED(hr)){
				mjlogfWE(env, "N_JMMFPlayer.setVisualComponent: failed to set the window handle to null, player id %lld", id);
			}
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogfWE(env, "N_JMMFPlayer.setVisualComponent: player not found, id %lld", id);
			}
		}

		return;
	}
	jint compMonitor = env->MonitorEnter(compObj);
	if (compMonitor != JNI_OK) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogWE(env, "N_JMMFPlayer.setVisualComponent: unable to enter the component monitor");
		}
		return;
	}
	// get the right player, set the owner
	// get the window handle of compObj
	JAWT awt;
	awt.version = JAWT_VERSION_9;
	JAWT_DrawingSurface* ds;
	JAWT_DrawingSurfaceInfo* dsi;
	JAWT_Win32DrawingSurfaceInfo* dsi_win;
	jboolean result;
	jint lock;
	HWND hwnd;

	result = JAWT_GetAWT(env, &awt);
	if (result == JNI_FALSE) {
		mjlogfWE(env, "N_JMMFPlayer.setVisualComponent: unable to get the AWT, id %lld", id);
		env->MonitorExit(compObj);
		return;
	}
	else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.setVisualComponent: successfully got the AWT, id %lld", id);
		}
	}
	ds = awt.GetDrawingSurface(env, compObj);
	if (ds == NULL) {
		mjlogfWE(env, "N_JMMFPlayer.setVisualComponent: unable to get the Drawing Surface, id %lld", id);
		env->MonitorExit(compObj);
		return;
	}
	else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.setVisualComponent: successfully got the Drawing Surface, id %lld", id);
		}
	}
	lock = ds->Lock(ds);
	if ((lock & JAWT_LOCK_ERROR) != 0) {
		mjlogfWE(env, "N_JMMFPlayer.setVisualComponent: unable to get lock the Drawing Surface, id %lld", id);
		env->MonitorExit(compObj);
		return;
	}
	else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.setVisualComponent: successfully locked the Drawing Surface, id %lld", id);
		}
	}

	// Get the drawing surface info
	dsi = ds->GetDrawingSurfaceInfo(ds);

	// Get the platform-specific drawing info
	dsi_win = (JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo;

	// get the handle
	hwnd = (HWND) dsi_win->hwnd;
	if (MMFUtil::JMMF_DEBUG) {
		mjlogfWE(env, "N_JMMFPlayer.setVisualComponent: got the HWND canvas handle: %p, id %lld", hwnd, id);
	}

	// Free the drawing surface info
	ds->FreeDrawingSurfaceInfo(dsi);

	// Unlock the drawing surface
	ds->Unlock(ds);

	// Free the drawing surface
	awt.FreeDrawingSurface(ds);
	MMFPlayer* dsp = (MMFPlayer*)id;

	if (dsp != NULL) {
		HRESULT hr = dsp->setOwnerWindow(hwnd);
		if (FAILED(hr)){
			mjlogfWE(env, "N_JMMFPlayer.setVisualComponent: failed to set the window handle of the player, id %lld", id);
		} 
		else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogfWE(env, "N_JMMFPlayer.setVisualComponent: window handle successfully applied, id %lld", id);
			}
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.setVisualComponent: player not found, id %lld", id);
		}
	}

	jint compMonExit = env->MonitorExit(compObj);
	if (compMonExit != JNI_OK) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogWE(env, "N_JMMFPlayer.setVisualComponent: unable to exit the component monitor");
		}
	}
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    setVisible
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_setVisible
	(JNIEnv *env, jobject thisObj, jlong id, jboolean visible) {
	if (MMFUtil::JMMF_DEBUG) {
		mjlogWE(env, "N_JMMFPlayer.setVisible: not implemented");
	}
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    setVideoSourcePos
 * Signature: (JIIII)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_setVideoSourcePos
	(JNIEnv *env, jobject thisObj, jlong id, jfloat x, jfloat y, jfloat w, jfloat h) {
	MMFPlayer* dsp = (MMFPlayer*)id;

	if (dsp != NULL) {
		HRESULT hr = dsp->setVideoSourcePos(x, y, w, h);
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.setVideoSourcePos: setting source position x=%f, y=%f, w=%f, h=%f, id %lld", x, y, w, h, id);
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.setVideoSourcePos: player not found, id %lld", id);
		}
	}
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    setVideoDestinationPos
 * Signature: (JIIII)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_setVideoDestinationPos
	(JNIEnv *env, jobject thisObj, jlong id, jint x, jint y, jint w, jint h) {
	MMFPlayer* dsp = (MMFPlayer*)id;

	if (dsp != NULL) {
		HRESULT hr = dsp->setVideoDestinationPos(x, y, w, h);
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.setVideoDestinationPos: setting destination position x=%f, y=%f, w=%f, h=%f, id %lld", x, y, w, h, id);
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.setVideoDestinationPos: player not found, id %lld", id);
		}
	}
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    setVideoSourceAndDestPos
 * Signature: (JFFFFIIII)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_setVideoSourceAndDestPos
	(JNIEnv *env, jobject thisObj, jlong id, jfloat sx, jfloat sy, jfloat sw, jfloat sh, jint dx, jint dy, jint dw, jint dh) {
	MMFPlayer* dsp = (MMFPlayer*)id;

	if (dsp != NULL) {
		HRESULT hr = dsp->setVideoSourceAndDestPos(sx, sy, sw, sh, dx, dy, dw, dh);
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.setVideoSourceAndDestPos: setting source and destination position sx=%f, sy=%f, sw=%f, sh=%f, dx=%f, dy=%f, dw=%f, dh=%f, id %lld", 
				sx, sy, sw, sh, dx, dy, dw, dh, id);
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.setVideoSourceAndDestPos: player not found, id %lld", id);
		}
	}
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getVideoDestinationPos
 * Signature: (J)[I
 */
JNIEXPORT jintArray JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getVideoDestinationPos
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;
	jintArray iArray = NULL;

	if (dsp != NULL) {
		const int size = 4;
		long vdPos[size];
		HRESULT hr = dsp->getVideoDestinationPos(&vdPos[0], &vdPos[1], &vdPos[2], &vdPos[3]);
		if (SUCCEEDED(hr)) {
			iArray = env->NewIntArray((jsize) size);
			jint isize = env->GetArrayLength(iArray);
			jboolean isCopy;
			jint* pIntArray;
			pIntArray = env->GetIntArrayElements(iArray, &isCopy);
			pIntArray[0] = vdPos[0];
			pIntArray[1] = vdPos[1];
			pIntArray[2] = vdPos[2];
			pIntArray[3] = vdPos[3];

			env->ReleaseIntArrayElements(iArray, pIntArray, 0);
		} else {
			iArray = NULL;
			if (MMFUtil::JMMF_DEBUG) {
				mjlogfWE(env, "N_JMMFPlayer.getVideoDestinationPos: failed to retrieve the video destination position, id %lld", id);
			}
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.getVideoDestinationPos: player not found, id %lld", id);
		}
	}

	return iArray;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    initWithFile
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_jmmf_JMMFPlayer_initWithFile
	(JNIEnv *env, jobject thisObj, jstring filepath, jboolean synchronous) {
	mjlogfWE(env, "N_JMMFPlayer.initWithFile: WINVER: %ld", WINVER);
	mjlogWE(env, "N_JMMFPlayer.initWithFile: file path:");
	mjlogJS(env, filepath);

	wchar_t *str;
	str = mpijni_ConvertToWChars(env, filepath);
	//wprintf(L"MMF JNI initWithFile: media path: %s\n", str);

	// try, catch exception
	MMFPlayer* dsp = new MMFPlayer(synchronous != 0);//(bool) synchronous
	// if dsp is NULL throw OutOfMemory
	if (dsp != NULL) {
		dsp->jRef = (__int64)env->NewGlobalRef(thisObj);
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.initWithFile: player instance created, id %lld", (jlong)dsp);
		}
	}
	else {
		mjlogfWE(env, "N_JMMFPlayer.initWithFile: unable to create a player instance (OutOfMemory?)");
		delete(str);
		return (jlong)-1;
	}
	HRESULT hr;
	hr = dsp->initSessionWithFile(str);

	if (FAILED(hr)) {
		// throw JMMFException
		//jthrowable throwObj;
		jclass exClass;
		//jmethodID mid; 

		char *mes = "Unknown error occurred while creating a Microsoft Media Foundation player.\n";

		mjlogWE(env, "N_JMMFPlayer.initWithFile: an unknown error occurred while creating a Microsoft Media Foundation player");

		exClass = env->FindClass("nl/mpi/jmmf/JMMException");
		//mid = env->GetMethodID(exClass, "<init>", "(Ljava/lang/String;)V");

		if (exClass != NULL) {
			env->ThrowNew(exClass, mes);
		} else {
			exClass = env->FindClass("java/lang/Exception");
			env->ThrowNew(exClass, mes);
		}

		delete(str);
		return (jlong)-1;
	}
	// MMFPlayer maintains its own copy
	delete(str);
	return (jlong)dsp;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    initWithFileAndOwner
 * Signature: (Ljava/lang/String;Ljava/awt/Component;)J
 kan weg??
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_jmmf_JMMFPlayer_initWithFileAndOwner
	(JNIEnv *env, jobject thisObj, jstring filepath, jobject compObj) {
	mjlogfWE(env, "N_JMMFPlayer.initWithFileAndOwner: WINVER: %ld", WINVER);
	mjlogWE(env, "N_JMMFPlayer.initWithFileAndOwner: file path:");
	mjlogJS(env, filepath);

	wchar_t *str;
	str = mpijni_ConvertToWChars(env, filepath);
	// try, catch exception
	MMFPlayer* dsp = new MMFPlayer();
	if (dsp != NULL) {
		dsp->jRef = (__int64)env->NewGlobalRef(thisObj);
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.initWithFileAndOwner: player instance created, id %lld", (jlong)dsp);
		}
	}
	else {
		mjlogfWE(env, "N_JMMFPlayer.initWithFileAndOwner: unable to create a player instance (OutOfMemory?)");
		delete(str);
		return (jlong)-1;
	}
	jint compMonitor = env->MonitorEnter(compObj);
	if (compMonitor != JNI_OK) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogWE(env, "N_JMMFPlayer.setVisualComponent: unable to enter the component monitor");
		}
		return (jlong)-1;
	}
	// get the window handle of compObj
	JAWT awt;
	awt.version = JAWT_VERSION_9;
	JAWT_DrawingSurface* ds;
	JAWT_DrawingSurfaceInfo* dsi;
	JAWT_Win32DrawingSurfaceInfo* dsi_win;
	jboolean result;
	jint lock;
	HWND hwnd;

	result = JAWT_GetAWT(env, &awt);
	if (result == JNI_FALSE) {
		mjlogfWE(env, "N_JMMFPlayer.initWithFileAndOwner: unable to get the AWT, id %lld", (jlong)dsp);
		delete(str);
		env->MonitorExit(compObj);
		return (jlong)-1;
	}
	else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.initWithFileAndOwner: successfully got the AWT, id %lld", (jlong)dsp);
		}
	}
	ds = awt.GetDrawingSurface(env, compObj);
	if (ds == NULL) {
		mjlogfWE(env, "N_JMMFPlayer.initWithFileAndOwner: unable to get the Drawing Surface, id %lld", (jlong)dsp);
		delete(str);
		env->MonitorExit(compObj);
		return (jlong)-1;
	}
	else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.initWithFileAndOwner: successfully got the Drawing Surface, id %lld", (jlong)dsp);
		}
	}
	lock = ds->Lock(ds);
	if ((lock & JAWT_LOCK_ERROR) != 0) {
		mjlogfWE(env, "N_JMMFPlayer.initWithFileAndOwner: unable to acquire the lock of the Drawing Surface, id %lld", (jlong)dsp);
		delete(str);
		env->MonitorExit(compObj);
		return (jlong)-1;
	}
	else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.initWithFileAndOwner: successfully acquired the lock of the Drawing Surface, id %lld", (jlong)dsp);
		}
	}

	// Get the drawing surface info
	dsi = ds->GetDrawingSurfaceInfo(ds);

	// Get the platform-specific drawing info
	dsi_win = (JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo;

	// get the handle
	hwnd = (HWND) dsi_win->hwnd;
	if (MMFUtil::JMMF_DEBUG) {
		mjlogfWE(env, "N_JMMFPlayer.initWithFileAndOwner: got the HWND canvas handle: %p, id %lld", hwnd, (jlong)dsp);
	}
	// Free the drawing surface info
	ds->FreeDrawingSurfaceInfo(dsi);

	// Unlock the drawing surface
	ds->Unlock(ds);

	// Free the drawing surface
	awt.FreeDrawingSurface(ds);
	//if (MMFUtil::JMMF_DEBUG) {
		//mjlogfWE(env, "N_JMMFPlayer.initWithFileAndOwner: retrieved the window handle: %p", hwnd);
		//fflush(stdout);
	//}
	jint compMonExit = env->MonitorExit(compObj);
	if (compMonExit != JNI_OK) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogWE(env, "N_JMMFPlayer.setVisualComponent: unable to exit the component monitor");
		}
	}

	HRESULT hr;
	hr = dsp->initSession(str, hwnd);
	if (FAILED(hr)) {
		// throw JMMFException
		//jthrowable throwObj;
		jclass exClass;
		//jmethodID mid; 

		char *mes = "Unknown error occurred while creating a Microsoft Media Foundation player.\n";

		mjlogWE(env, "N_JMMFPlayer.initWithFileAndOwner: unknown error occurred while creating a Microsoft Media Foundation player");

		exClass = env->FindClass("nl/mpi/jmmf/JMMFException");
		//mid = env->GetMethodID(exClass, "<init>", "(Ljava/lang/String;)V");

		if (exClass != NULL) {
			env->ThrowNew(exClass, mes);
		} else {
			exClass = env->FindClass("java/lang/Exception");
			env->ThrowNew(exClass, mes);
		}

		delete(str);
		return (jlong)-1;
	}

	if (MMFUtil::JMMF_DEBUG) {
		mjlogfWE(env, "N_JMMFPlayer.initWithFileAndOwner: successfully created a player: %lld", (jlong)dsp);
	}
	// MMFPlayer maintains its own copy
	delete(str);
	return (jlong)dsp;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getFileType
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getFileType
	(JNIEnv *env, jobject thisObj, jstring file) {
	return NULL;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    isVisualMedia
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_nl_mpi_jmmf_JMMFPlayer_isVisualMedia
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;

	if (dsp != NULL) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.isVisualMedia: player has visual media %d, id %lld", dsp->isVisualMedia(), id);
		}
		if (dsp->isVisualMedia()) {
			return JNI_TRUE;
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.isVisualMedia: player not found, id %lld", id);
		}
	}

	return JNI_FALSE;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    setStopTime
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_setStopTime
	(JNIEnv *env, jobject thisObj, jlong id, jlong time) {
	MMFPlayer* dsp = (MMFPlayer*)id;

	if (dsp != NULL) {
		HRESULT hr = dsp->setStopPosition((__int64) time);
		if (FAILED(hr)) {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogfWE(env, "N_JMMFPlayer.setStopTime: failed to set the stop time (%ld, 0x%X), id %lld", hr, hr, id);
			}
			// throw an exception?
		}
		else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogfWE(env, "N_JMMFPlayer.setStopTime: successfully set the stop time to %lld, id %lld", time, id);
			}
		}

	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.setStopTime: player not found, id %lld", id);
		}
	}
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getStopTime
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getStopTime
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;
	jlong stopTime = 1;

	if (dsp != NULL) {
		stopTime = (jlong) dsp->getStopPosition();
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.getStopTime: the current stop time of the player, %lld, id %lld", stopTime, id);
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.getStopTime: player not found, id %lld", id);
		}
	}

	return stopTime;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getSourceHeight
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getSourceHeight
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;
	jint sheight = 1;

	if (dsp != NULL) {
		sheight = (jint) dsp->getOrgVideoHeight();
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.getSourceHeight: the video height of the player, %ld, id %lld", sheight, id);
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.getSourceHeight: player not found, id %lld", id);
		}
	}

	return sheight;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getSourceWidth
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getSourceWidth
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;
	jint swidth = 1;

	if (dsp != NULL) {
		swidth = dsp->getOrgVideoWidth();
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.getSourceWidth: the video width of the player, %ld, id %lld", swidth, id);
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.getSourceWidth: player not found, id %lld", id);
		}
	}

	return swidth;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getPreferredAspectRatio
 * Signature: (J)[I
 */
JNIEXPORT jintArray JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getPreferredAspectRatio
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;
	jfloat aspectRatio = 1;

	if (dsp != NULL) {
		long x;
		long y;
		HRESULT hr = dsp->getPreferredAspectRatio(&x, &y);
		if (SUCCEEDED(hr)) {
			jboolean isCopy;
			jintArray iArray = env->NewIntArray(2);
			jint *pIA = env->GetIntArrayElements(iArray, &isCopy);
			pIA[0] = (jint) x;
			pIA[1] = (jint) y;

			env->ReleaseIntArrayElements(iArray, pIA, 0);
			if (MMFUtil::JMMF_DEBUG) {
				mjlogfWE(env, "N_JMMFPlayer.getPreferredAspectRatio: aspect ratio x=%ld, y=%ld, id %lld", x, y, id);
			}
			return iArray;
		} else {
			mjlogfWE(env, "N_JMMFPlayer.getPreferredAspectRatio: cannot get the preferred aspect ratio (%ld, 0x%X), id %lld", hr, hr, id);
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.getPreferredAspectRatio: player not found, id %lld", id);
		}
	}

	return NULL;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getCurrentImage
 * Signature: (J)[B
 */
JNIEXPORT jbyteArray JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getCurrentImage
	(JNIEnv *env, jobject thisObj, jlong id, jobject header) {
	MMFPlayer* dsp = (MMFPlayer*)id;
	jbyteArray bArray;

	if (dsp != NULL) {
		if (!dsp->isPlaying()) {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogfWE(env, "N_JMMFPlayer.getCurrentImage: player is paused, id %lld", id);
			}

			HRESULT hr;
			BITMAPINFOHEADER biHeader;
			biHeader.biSize = sizeof(BITMAPINFOHEADER);
			BYTE *pDibData; 
			DWORD dibSize;

			hr = dsp->getCurrentImage(&biHeader, &pDibData, &dibSize);
			if (SUCCEEDED(hr)) {
				// convert to a jbyteArray
				int size = (int) dibSize;
				bArray = env->NewByteArray((jsize) size);
				jint isize = env->GetArrayLength(bArray);
				jboolean isCopy;
				jbyte* pByteArray;
				pByteArray = env->GetByteArrayElements(bArray, &isCopy);

				for (int i = 0; i < size; i++) {
					pByteArray[i] = (jbyte) pDibData[i];
				}
				if (MMFUtil::JMMF_DEBUG) {
					mjlogfWE(env, "N_JMMFPlayer.getCurrentImage: image array copied to jbyte array, id %lld", id);
				}
				//if(isCopy == JNI_TRUE) { // always release?
					env->ReleaseByteArrayElements(bArray, pByteArray, 0);
				//}
				
				//free(pDibData);
				CoTaskMemFree(pDibData);
				if (header != NULL) {
					jclass headerClass = env->GetObjectClass(header);
					jfieldID fid = env->GetFieldID(headerClass, "size", "J");
					if (fid != NULL) {
						env->SetLongField(header, fid, (jlong) biHeader.biSize);
					}
					fid = env->GetFieldID(headerClass, "width", "I");
					if (fid != NULL) {
						env->SetIntField(header, fid, (jint) biHeader.biWidth);
					}
					fid = env->GetFieldID(headerClass, "height", "I");
					if (fid != NULL) {
						env->SetIntField(header, fid, (jint) biHeader.biHeight);
					}
					fid = env->GetFieldID(headerClass, "planes", "I");
					if (fid != NULL) {
						env->SetIntField(header, fid, (jint) biHeader.biPlanes);
					}
					fid = env->GetFieldID(headerClass, "bitCount", "I");
					if (fid != NULL) {
						env->SetIntField(header, fid, (jint) biHeader.biBitCount);
					}
					fid = env->GetFieldID(headerClass, "compression", "I");
					if (fid != NULL) {
						env->SetIntField(header, fid, (jint) biHeader.biCompression);
					}
					fid = env->GetFieldID(headerClass, "sizeImage", "J");
					if (fid != NULL) {
						env->SetLongField(header, fid, (jlong) biHeader.biSizeImage);
					}
					fid = env->GetFieldID(headerClass, "xPelsPerMeter", "J");
					if (fid != NULL) {
						env->SetLongField(header, fid, (jlong) biHeader.biXPelsPerMeter);
					}
					fid = env->GetFieldID(headerClass, "yPelsPerMeter", "J");
					if (fid != NULL) {
						env->SetLongField(header, fid, (jlong) biHeader.biYPelsPerMeter);
					}
					fid = env->GetFieldID(headerClass, "clrUsed", "Z");
					if (fid != NULL) {
						env->SetBooleanField(header, fid, (jboolean) biHeader.biClrUsed);
					}
					fid = env->GetFieldID(headerClass, "clrImportant", "Z");
					if (fid != NULL) {
						env->SetBooleanField(header, fid, (jboolean) biHeader.biClrImportant);
					}
					//printf("MMF JNI: Size of image buffer: %ld\n", dibSize);
					//printf("MMF JNI: Header, bit count %ld\n", biHeader.biBitCount);
					//printf("MMF JNI: Header, image width %ld\n", biHeader.biWidth);
					//printf("MMF JNI: Header, image height %ld\n", biHeader.biHeight);
					//printf("MMF JNI: Header, size %ld\n", biHeader.biSize);
					//printf("MMF JNI: Header, size image %ld\n", biHeader.biSizeImage);
					//printf("MMF JNI: Header, compression %ld\n", biHeader.biCompression);
					//printf("MMF JNI: Header, no. planes %ld\n", biHeader.biPlanes);
					//fflush(stdout);
				}
			} else {
				mjlogfWE(env, "N_JMMFPlayer.getCurrentImage: no buffer created, no buffer filled (%ld, 0x%X), id %lld", hr, hr, id);
				bArray = NULL;
			}
		} else {
			mjlogfWE(env, "N_JMMFPlayer.getCurrentImage: the player is not paused, id %lld", id);
			// stop the player or return an error? 
			bArray = NULL;
		}
	} else {
		if (dsp != NULL) {
			mjlogfWE(env, "N_JMMFPlayer.getCurrentImage: player not found, id %lld", id);
		}
		bArray = NULL;
	}
	if (MMFUtil::JMMF_DEBUG) {
		mjlogfWE(env, "N_JMMFPlayer.getCurrentImage: returning image byte array %d, id %lld", (bArray != NULL), id);
	}

	return bArray;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getImageAtTime
 * Signature: (JJ)[B
 */
JNIEXPORT jbyteArray JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getImageAtTime
	(JNIEnv *env, jobject thisObj, jlong id, jobject header, jlong time) {
	mjlogWE(env, "N_JMMFPlayer.getImageAtTime: not implemented");
	return NULL;// TO DO
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    repaintVideo
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_repaintVideo
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;

	if (dsp != NULL) {
		dsp->repaintVideo();
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.repaintVideo: player not found, id %lld", id);
		}
	}
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    enableDebugMode
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_enableDebugMode
	(JNIEnv *env, jclass clazz, jboolean debug) {
	mjlogfWE(env, "N_JMMFPlayer.enableDebugMode: setting debug mode: %d", debug);
	MMFUtil::JMMF_DEBUG = (debug != 0);// (bool) debug C4800 warning
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    correctAtPause
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_correctAtPause
	(JNIEnv *env, jclass clazz, jboolean correct) {
	mjlogfWE(env, "N_JMMFPlayer.correctAtPause: correct media position when pausing the player: %d", correct);
	if (correct != 0) {//(bool) correct
		MMFUtil::JMMF_CORRECT_AT_PAUSE = TRUE;
	} else {
		MMFUtil::JMMF_CORRECT_AT_PAUSE = FALSE;
	}
}

/*
* Class:     nl_mpi_jmmf_JMMFPlayer
* Method:    initLog
* Signature: (Ljava/lang/String;Ljava/lang/String;)V
*/
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_initLog
	(JNIEnv *env, jclass callerClass, jstring clDescriptor, jstring methodName) {
	const char *clChars = env->GetStringUTFChars(clDescriptor, NULL);
	if (clChars == NULL) {
		return; // OutOfMemory thrown
	}
	const char *methChars = env->GetStringUTFChars(methodName, NULL);
	if (methChars == NULL) {
		return; // OutOfMemory thrown
	}
	mpijni_initLog(env, clChars, methChars);

	if (MMFUtil::JMMF_DEBUG) {
		mjlogfWE(env, "N_JMMFPlayer.initLog: set Java log method: %s - %s", clChars, methChars);
	}

	// free the allocated memory
	env->ReleaseStringUTFChars(clDescriptor, clChars);
	env->ReleaseStringUTFChars(methodName, methChars);
}



/*
* This option is currently not used. Redirecting standard out to a file works
* but not if the specified file is the same as the file the Java logger is
* using (probably because it is locked by the Java logger).
*
* Class:     nl_mpi_jmmf_JMMFPlayer
* Method:    setLogFile
* Signature: (Ljava/lang/String;)V
*/
/*
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_setLogFile
	(JNIEnv *env, jclass clazz, jstring logPath) {

	const char *utfCh = env->GetStringUTFChars(logPath, NULL);
	//printf("MMF JNI setLogFile: setting log file for stdout output: %s\n", utfCh);
	mjlogfWE(env, "N_JMMFPlayer.setLogFile: setting log file for stdout output: %s", utfCh);
	MMFUtil::setStdOut(utfCh);
	env->ReleaseStringUTFChars(logPath, utfCh);
}
*/

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    clean
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_clean
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;

	if (dsp != NULL) {
		env->DeleteGlobalRef((jobject)(dsp->jRef));
		// instead of calling cleanUpOnClose rely on the destructor of MMFPlayer
		delete (dsp);
		/*
		HRESULT hr = dsp->cleanUpOnClose();
		if (SUCCEEDED(hr)) {
			delete (dsp);
		}
		*/
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.clean: player deleted, id %lld", id);
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.clean: player not found, id %lld", id);
		}
	}

}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    closeSession
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_closeSession
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;

	if (dsp != NULL) {
		HRESULT hr = dsp->CloseSession();

		if (FAILED(hr)) {
			mjlogfWE(env, "N_JMMFPlayer.closeSession: closing the Session failed (%ld, 0x%X), id %lld", hr, hr, id);
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.closeSession: player not found, id %lld", id);
		}
	}

}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    deletePlayer
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_deletePlayer
	(JNIEnv *env, jobject thisObj, jlong id) {
	MMFPlayer* dsp = (MMFPlayer*)id;

	if (dsp != NULL) {
		env->DeleteGlobalRef((jobject)(dsp->jRef));
		// instead of calling cleanUpOnClose rely on the destructor of MMFPlayer
		delete (dsp);

	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogfWE(env, "N_JMMFPlayer.deletePlayer: player not found, id %lld", id);
		}
	}

}

#ifdef __cplusplus
}
#endif

