/* 
 * Project:	JMMFPlayer, Microsoft Media Foundation Player for Java
 * Author:	Han Sloetjes
 * Version:	1.0, March 2012
 */
#include "MMFPlayer.h"
#include "MMFUtil.h"
#include "mpi_jni_util.h"
#include <mferror.h>
#include <comdef.h>

MMFPlayer::MMFPlayer() {
	initFields();
}

MMFPlayer::MMFPlayer(bool synchronous) {
	if (MMFUtil::JMMF_DEBUG) {
		mjlogf("N_MMFPlayer.constructor: creating a player (synchronous mode: %d)", synchronous);
	}
	initFields();
	synchronousMode = synchronous;
}

MMFPlayer::MMFPlayer(const wchar_t *path) {
	if (MMFUtil::JMMF_DEBUG) {
		mjlogf("N_MMFPlayer.constructor: creating a player with file path: %s", path);
	}
	initFields();
}

MMFPlayer::MMFPlayer(const wchar_t *path, bool synchronous) {
	if (MMFUtil::JMMF_DEBUG) {
		mjlogf("N_MMFPlayer.constructor: creating a player (synchronous mode: %d) with file path: %s", synchronous, path);
	}	
	initFields();
	synchronousMode = synchronous;
}

/*
* It is (now) assumed the player and the session are closed before deleting 
* the player.
*/
MMFPlayer::~MMFPlayer() {
	if (MMFUtil::JMMF_DEBUG) {
		mjlog("N_MMFPlayer.destructor: cleaning up player");
	}

	if (!cleanUpCalled) {
		//cleanUpOnClose();

		// test 
		cleanUpCalled = true;
		HRESULT	hr = FinalClosing();
		if (FAILED(hr)) {
			mjlogf("N_MMFPlayer.destructor: failed to finalize close operation: (%d, 0x0x%X).", hr, hr);
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.destructor: successfully finalized the close operation");
			}
		}
	}
}

void MMFPlayer::initFields() {
	InitializeCriticalSection(&m_criticalSection);
	m_hwndVideo = NULL;
	m_pSource = NULL;
	m_pVideoDisplay = NULL;
	m_pSession = NULL;
	m_pTopology = NULL;
	m_pRate = NULL;
	m_pRateSupport = NULL;
	m_pClock = NULL;
	//m_pVolume = NULL;
	m_pStreamVolume = NULL;
	m_nRefCount = 1;
	m_state = PlayerState_NoSession;
	userPlaybackRate = 1;
	thinEnabled = FALSE;
	cleanUpCalled = false;
	topoInited = false;
	duration = 0;
	stopTime = 0;
	tempW = 0;
	tempH = 0;
	initVolume = 1;
	initMediaTime = 0;
	m_pStopTimer = NULL;
	m_pTimerCancelKey = NULL;
	pStopState = new StopTimeState();// initialize before first call to Invoke
	m_pEndTimer = NULL;
	pEndState = new StopTimeState();
	pendingAction = new PendingAction();
	clearPendingAction(pendingAction);
	cachedAction = new PendingAction();
	clearPendingAction(cachedAction);
	synchronousMode = false;
}

void MMFPlayer::clearPendingAction(PendingAction *pAction) {
	if (pAction != NULL) {
		pAction->action = DoNothing;
		pAction->timeValue = -1;
		pAction->rateValue = 1.0;
	}
}

/*
* Delegates to either the synchronous or the asynchronous version of start()
*/
HRESULT MMFPlayer::start() {
	if (synchronousMode) {
		return startSynchronous();
	} else {
		return startA();
	}
}

/*
* The default, asynchronous, start method.
*/
HRESULT MMFPlayer::startA() {
	if (m_state == PlayerState_Started || pendingAction->action == SetStateStarted) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.startA: the player is alreay started");
		}
		return MF_E_INVALIDREQUEST;
	}
	EnterCriticalSection(&m_criticalSection);
	HRESULT hr = S_OK;
	if (m_pSession != NULL) {
		// reset the rate
		if (m_pRate != NULL) {
			float curRate = 0.0F;
			//BOOL thin;
			hr = m_pRate->GetRate(FALSE, &curRate);
			if (curRate != userPlaybackRate) {
				m_pSession->Pause();// pause without pending action
				hr = m_pRate->SetRate(FALSE, userPlaybackRate);// set rate without pending
				if (FAILED(hr)) {
					mjlogf("N_MMFPlayer.startA: failed to set the playback rate to %f.", userPlaybackRate);
				} else {
					if (MMFUtil::JMMF_DEBUG) {
						mjlogf("N_MMFPlayer.startA: successfully set the playback rate to %f.", userPlaybackRate);
					}
				}
			} else {
				if (MMFUtil::JMMF_DEBUG) {
					mjlogf("N_MMFPlayer.startA: the playback rate is already the user rate %f.", userPlaybackRate);
				}
			}
		}
				
		// here check state of the player
		PROPVARIANT varStart;
		PropVariantInit(&varStart);

		varStart.vt = VT_EMPTY;

		HRESULT hr = m_pSession->Start(&GUID_NULL, &varStart);// start from current position

		if (SUCCEEDED(hr)) {
			// Start is an asynchronous operation. However, we can treat our state 
			// as being already started. If Start fails later, we'll get an 
			// MESessionStarted event with an error code, and will update our state.
			//m_state = PlayerState_Started;
			pendingAction->action = SetStateStarted;

			if (stopTime > 0 && m_pStopTimer != NULL) {
				hr = m_pStopTimer->SetTimer(0, stopTime, this, pStopState, &m_pTimerCancelKey);
				if (FAILED(hr)) {
					mjlogf("N_MMFPlayer.startA: failed to set the stop time for the stop time timer %lld", stopTime);
				} else {
					if (MMFUtil::JMMF_DEBUG) {
						mjlogf("N_MMFPlayer.startA: successfully set the stop time for the stop time timer %lld", stopTime);
					}
				}
			}
			if (duration > 2000000 && m_pEndTimer != NULL) {// 200 ms
				hr = m_pEndTimer->SetTimer(0, duration - 2000000, this, pEndState, &m_pTimerCancelKey);
				if (FAILED(hr)) {
					mjlog("N_MMFPlayer.startA: failed to set the end of media timer");
				} else {
					if (MMFUtil::JMMF_DEBUG) {
						mjlog("N_MMFPlayer.startA: successfully set time for the end of media timer");
					}
				}
			}
		} else {
			mjlogf("N_MMFPlayer.startA: failed to start the player (%ld)", hr);
		}

		PropVariantClear(&varStart);
		
	} else {
		mjlog("N_MMFPlayer.startA: cannot start the player, the session is null");
		hr = E_UNEXPECTED;
	}

	LeaveCriticalSection(&m_criticalSection);
	return hr;
}

/*
* The start method in synchronous mode. GetEvent is called immediately after calls
* to session method that result in an event.
*/
HRESULT MMFPlayer::startSynchronous() {
	if (!synchronousMode) {
		mjlog("N_MMFPlayer.startSynchronous: function called while not in synchronous mode");
	}
	if (m_state == PlayerState_Started) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.startSynchronous: the player is already started");
		}
		return MF_E_INVALIDREQUEST;
	}
	EnterCriticalSection(&m_criticalSection);
	HRESULT hr = S_OK;
	if (m_pSession != NULL) {
		// reset the rate
		if (m_pRate != NULL) {
			float curRate = 0.0F;
			//BOOL thin;
			hr = m_pRate->GetRate(FALSE, &curRate);
			if (curRate != userPlaybackRate) {
				hr = m_pSession->Pause();
				
				if (FAILED(hr)) {
					mjlog("N_MMFPlayer.startSynchronous: cannot pause the player in order to change the rate");
				} else {
					if (MMFUtil::JMMF_DEBUG) {
						mjlog("N_MMFPlayer.startSynchronous: successfully paused the player in order to change the rate");
					}

					HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionPaused);

					if (SUCCEEDED(ehr)) {
						// what to do if an error is returned
						if (MMFUtil::JMMF_DEBUG) {
							mjlog("N_MMFPlayer.startSynchronous: received session paused success event");
						}
					} else {
						mjlogf("N_MMFPlayer.startSynchronous: received session paused error event (%d, 0x0x%X)", ehr, ehr);
					}
				}
				
				hr = m_pRate->SetRate(FALSE, userPlaybackRate);
				if (FAILED(hr)) {
					mjlogf("N_MMFPlayer.startSynchronous: cannot set playback rate to %f", userPlaybackRate);
				} else {
					if (MMFUtil::JMMF_DEBUG) {
						mjlogf("N_MMFPlayer.startSynchronous: successfully set the playback rate to %f", userPlaybackRate);
					}

					HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionRateChanged);

					if (SUCCEEDED(ehr)) {
						// what to do if an error is returned
						if (MMFUtil::JMMF_DEBUG) {
							mjlog("N_MMFPlayer.startSynchronous: received change session rate success event");
						}
					} else {
						mjlogf("N_MMFPlayer.startSynchronous: received change session rate error event (%d, 0x0x%X)", ehr, ehr);
					}

				}
			} else {
				if (MMFUtil::JMMF_DEBUG) {
					mjlogf("N_MMFPlayer.startSynchronous: the rate is already to user playback rate %f", userPlaybackRate);
				}
			}
		}
				
		// here check state of the player
		PROPVARIANT varStart;
		PropVariantInit(&varStart);

		varStart.vt = VT_EMPTY;

		HRESULT hr = m_pSession->Start(&GUID_NULL, &varStart);// start from current position
		
		if (SUCCEEDED(hr)) {
			// Start is an asynchronous operation. Get event from the session. Do it here or after setting stop times?
			HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionStarted);

			if (SUCCEEDED(ehr)) {
				// what to do if an error is returned
				if (MMFUtil::JMMF_DEBUG) {
					mjlog("N_MMFPlayer.startSynchronous: received session started success event");
				}
			} else {
				mjlogf("N_MMFPlayer.startSynchronous: received session started error event (%d, 0x0x%X)", ehr, ehr);
			}
			// this should now have been set in the processing of the event
			//m_state = PlayerState_Started;
			/* test comment 24-03 when timer are used an asynchronous event is generated and somehow that leads to MULTIPLE_SUBSCRIBERS error
			// when getting the event
			if (stopTime > 0 && m_pStopTimer != NULL) {
				hr = m_pStopTimer->SetTimer(0, stopTime, this, pStopState, &m_pTimerCancelKey);
				if (FAILED(hr)) {
					printf("N_MMFPlayer Error: startSynchronous: Failed to set the stop time for the stop time timer.\n");
				} else {
					if (MMFUtil::JMMF_DEBUG) {
						printf("N_MMFPlayer Info: startSynchronous: Successfully set the stop time for the stop time timer %lld.\n", stopTime);
					}
				}
			}
			if (duration > 2000000 && m_pEndTimer != NULL) {// 200 ms
				hr = m_pEndTimer->SetTimer(0, duration - 2000000, this, pEndState, &m_pTimerCancelKey);
				if (FAILED(hr)) {
					printf("N_MMFPlayer Error: startSynchronous: Failed to set the end of media timer.\n");
				} else {
					if (MMFUtil::JMMF_DEBUG) {
						printf("N_MMFPlayer Info: startSynchronous: Successfully set the time for the end of media timer.\n");
					}
				}
			}
			*/
		} else {
			mjlogf("N_MMFPlayer.startSynchronous: starting the player failed (%d, 0x0x%X)", hr ,hr);
		}

		PropVariantClear(&varStart);
		
	} else {
		mjlog("N_MMFPlayer.startSynchronous: cannot start the player, session is null");
		hr = E_UNEXPECTED;
	}

	LeaveCriticalSection(&m_criticalSection);
	return hr;
}

/*
* Delegates to either the synchronous or the asynchronous version of stop().
*/
HRESULT MMFPlayer::stop() {
	if (synchronousMode) {
		return stopSynchronous();
	} else {
		return stopA();
	}
}

/*
* For temporary stopping the player, pause() needs to be called. 
* stop() resets the play back and should only be called before closing the session.
*/
HRESULT MMFPlayer::stopA() {
    // only pause if the player has started
	if (m_state != PlayerState_Started && m_state != PlayerState_Paused) {
        return MF_E_INVALIDREQUEST;
    }

	EnterCriticalSection(&m_criticalSection);
	// if session is null return error
	if (m_pSession == NULL) {
		LeaveCriticalSection(&m_criticalSection);
		return E_UNEXPECTED;
	}

	HRESULT hr = m_pSession->Stop();// pauses the session

	if (SUCCEEDED(hr)) {
		//m_state = PlayerState_Stopped;
		pendingAction->action = SetStateStopped;
	}
	// setRate(0)?
	LeaveCriticalSection(&m_criticalSection);

	return hr;
}

/*
* Stops the player before closing, synchronously.
*/
HRESULT MMFPlayer::stopSynchronous() {
    // only stop if the player has started
	if (m_state != PlayerState_Started && m_state != PlayerState_Paused) {
        return MF_E_INVALIDREQUEST;
    }

	EnterCriticalSection(&m_criticalSection);
	// if session is null return error
	if (m_pSession == NULL) {
		LeaveCriticalSection(&m_criticalSection);
		return E_UNEXPECTED;
	}

	HRESULT hr = m_pSession->Stop();// pauses the session

	if (SUCCEEDED(hr)) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.stopSynchronous: successfully stopped the player");
		}
		HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionStopped);

		if (SUCCEEDED(ehr)) {
			// what to do if an error is returned
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.stopSynchronous: received session stopped success event");
			}
		} else {
			mjlogf("N_MMFPlayer.stopSynchronous: received session stopped error event (%d, 0x0x%X)", ehr, ehr);
		}

	}
	else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogf("N_MMFPlayer.stopSynchronous: stopping the player failed (%d, 0x0x%X)", hr, hr);
		}
	}
	// setRate(0)?
	LeaveCriticalSection(&m_criticalSection);

	return hr;
}

/*
* Delegates to either the synchronous or the asynchronous variant of pause().
*/
HRESULT MMFPlayer::pause() {
	if (synchronousMode) {
		return pauseSynchronous();
	} else {
		return pauseA();
	}
}

/*
* The default, asynchronous, pause method.
* Pausing by setting the rate to 0 while the player remains "started"
* doesn't seem to work; the player keeps playing. Setting the rate to 0
* means "scrubbing", one image is produced, but in combination with "started" 
* this has no effect.?
*/
HRESULT MMFPlayer::pauseA() {
	EnterCriticalSection(&m_criticalSection);
    // only pause if the player has started
	if (m_state != PlayerState_Started && pendingAction->action != SetStateStarted) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.pauseA: the player is not started");
		}
		LeaveCriticalSection(&m_criticalSection);
        return MF_E_INVALIDREQUEST;
    }
	// if session is null return error
	if (m_pSession == NULL) {
		LeaveCriticalSection(&m_criticalSection);
		return E_UNEXPECTED;
	}

	HRESULT hr = m_pSession->Pause();// pauses the session

	if (SUCCEEDED(hr)) {
		//m_state = PlayerState_Paused;
		pendingAction->action = SetStatePaused;// store current time?
		pendingAction->timeValue = getMediaPosition();
		if (MMFUtil::JMMF_DEBUG) {
			__int64 curTime = getMediaPosition();
			mjlogf("N_MMFPlayer.pauseA: paused the player at %lld", curTime);
		}
	} else {
		__int64 curTime = getMediaPosition();
		mjlogf("N_MMFPlayer.pauseA: error while pausing the player at %lld, %d (0x0x%X).", curTime, hr, hr);
	}

	LeaveCriticalSection(&m_criticalSection);

	return hr;
}

/*
* The synchronous variant of pausing the player.
*/
HRESULT MMFPlayer::pauseSynchronous() {
    // only pause if the player has started
	if (m_state != PlayerState_Started) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.pauseSynchronous: the media player is not started");
		}
        return MF_E_INVALIDREQUEST;
    }
	// if session is null return error
	if (m_pSession == NULL) {
		return E_UNEXPECTED;
	}
	EnterCriticalSection(&m_criticalSection);//TODO need lock in synchronous mode?

	HRESULT hr = m_pSession->Pause();// pauses the session

	if (SUCCEEDED(hr)) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.pauseSynchronous: successfully paused the player");
		}
		HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionPaused);
		if (SUCCEEDED(ehr)) {
			// ignore the return value?
			//ProcessMediaEventSynchronous(pEvent);
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.pauseSynchronous: received session paused success event");
			}
		} else {
			mjlogf("N_MMFPlayer.pauseSynchronous: received session paused error event (%d, 0x0x%X)", ehr, ehr);
		}

		if (MMFUtil::JMMF_DEBUG) {
			__int64 curTime = getMediaPosition();
			mjlogf("N_MMFPlayer.pauseSynchronous: paused the player at %lld", curTime);
		}
	} else {
		__int64 curTime = getMediaPosition();
		mjlogf("N_MMFPlayer.pauseSynchronous: error while pausing the player at %lld, %d (0x0x%X)", curTime, hr, hr);
	}

	LeaveCriticalSection(&m_criticalSection);

	return hr;
}

/*
* Delegates to either the synchronous or asynchronous version of initSessionWithFile().
*/
HRESULT MMFPlayer::initSessionWithFile(const wchar_t *path) {
	if (synchronousMode) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.initSessionWithFile: init session in synchronous mode");
		}
		return initSessionWithFileSynchronous(path);
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.initSessionWithFile: init session in asynchronous mode");
		}
		return initSessionWithFileA(path);
	}
}

/*
* Initialization in the default, asynchronous, mode.
* Initializes the session. In case of "audio only" it immediately creates a topology
* and sets it for the media session.
* In case of video initialization is done partially, first when the video window is available
* the topology is created and passed to the session.
*/
HRESULT MMFPlayer::initSessionWithFileA(const wchar_t *path) {
	mediaPath = MMFUtil::copyWchar(path);
	//wprintf(L"N_MMFPlayer Info: initSessionWithFile: Media path: %s\n", mediaPath);

	// Initialize the COM library
	HRESULT hr = CoInitializeEx(NULL, COINIT_MULTITHREADED);//COINIT_MULTITHREADED  COINIT_APARTMENTTHREADED
	
	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.initSessionWithFileA: failed to initialize COM: (%d, 0x0x%X)", hr, hr);
		return hr;
	}
	hr = MFStartup(MF_VERSION);
	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.initSessionWithFileA: failed to startup MediaFoundation: (%d, 0x0x%X)", hr, hr);
		return hr;
	}

	// create an event that will be fired when the asynchronous IMFMediaSession::Close() 
    // operation is complete
    m_closeCompleteEvent = CreateEvent(NULL, FALSE, FALSE, NULL);

	if (m_closeCompleteEvent == NULL) {
		mjlog("N_MMFPlayer.initSessionWithFileA: failed to create a 'close complete' event");
		//return E_UNEXPECTED; // should fail??
	}

	EnterCriticalSection(&m_criticalSection);

	hr = MFCreateMediaSession(NULL, &m_pSession);

	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.initSessionWithFileA: failed to create a media session: (%d, 0x0x%X)", hr, hr);
		LeaveCriticalSection(&m_criticalSection);
		return hr;
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.initSessionWithFileA: successfully created a media session");
		}
	}
	
	// Start pulling events from the media session
    hr = m_pSession->BeginGetEvent((IMFAsyncCallback*)this, NULL);
	
	if (FAILED(hr)){
		mjlogf("N_MMFPlayer.initSessionWithFileA: failed to BeginGetEvent (%d, 0x0x%X)", hr, hr);
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.initSessionWithFileA: successfully called BeginGetEvent");
		}
	}
	// create media source
	hr = CreateMediaSource(mediaPath);

	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.initSessionWithFileA: failed to create a media source (%d, 0x0x%X)", hr, hr);
		LeaveCriticalSection(&m_criticalSection);
		return hr;
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.initSessionWithFileA: created a media source");
		}
	}
	// check major media type; in case of audio only create the topology and set it for the session
	// in case of video partially create a topology and wait for the window handle
	// create topology
	if (isVideo && m_hwndVideo == NULL) {
		LeaveCriticalSection(&m_criticalSection);

		return hr;
	}

	hr = CreateTopologyFromSource();
	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.initSessionWithFileA: failed to create a topology (%d, 0x%X)", hr, hr);
		LeaveCriticalSection(&m_criticalSection);
		return hr;
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogf("N_MMFPlayer.initSessionWithFileA: successfully created a topology");
		}
	}

	//hr = m_pSession->SetTopology(MFSESSION_SETTOPOLOGY_NORESOLUTION, m_pTopology);
	hr = m_pSession->SetTopology(0, m_pTopology);

	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.initSessionWithFileA: failed to set the topology of the session (%d, 0x%X)", hr, hr);
	} else {
		mjlog("N_MMFPlayer.initSessionWithFileA: successfully set the topology of the session");
	}

	LeaveCriticalSection(&m_criticalSection);

	return hr;

}

/*
* Initialize a session synchronously.
*/
HRESULT MMFPlayer::initSessionWithFileSynchronous(const wchar_t *path) {
	mediaPath = MMFUtil::copyWchar(path);
	//wprintf(L"N_MMFPlayer Info: initSessionWithFileSynchronous: Media path: %s\n", mediaPath);

	// Initialize the COM library
	HRESULT hr = CoInitializeEx(NULL, COINIT_MULTITHREADED);//COINIT_MULTITHREADED  COINIT_APARTMENTTHREADED
	
	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.initSessionWithFileSynchronous: failed to initialize COM: (%d, 0x%X)", hr, hr);
		return hr;
	}
	hr = MFStartup(MF_VERSION);
	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.initSessionWithFileSynchronous: failed to startup MediaFoundation: (%d, 0x%X)", hr, hr);
		return hr;
	}

	EnterCriticalSection(&m_criticalSection);// in synchronous mode locking is not necessary?

	hr = MFCreateMediaSession(NULL, &m_pSession);// is synchronous

	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.initSessionWithFileSynchronous: failed to create a media session: (%d, 0x%X)", hr, hr);
		LeaveCriticalSection(&m_criticalSection);
		return hr;
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.initSessionWithFileSynchronous: successfully created a media session");
		}
	}
	
	// create media source
	hr = CreateMediaSource(mediaPath);// is currently synchronous

	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.initSessionWithFileSynchronous: failed to create a media source: (%d, 0x%X)", hr, hr);
		LeaveCriticalSection(&m_criticalSection);
		return hr;
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.initSessionWithFileSynchronous: successfully created a media source");
		}
	}
	// check major media type; in case of audio only create the topology and set it for the session
	// in case of video partially create a topology and wait for the window handle
	// create topology
	if (isVideo && m_hwndVideo == NULL) {
		LeaveCriticalSection(&m_criticalSection);

		return hr;
	}

	hr = CreateAndSetTopologySynchronous();

	LeaveCriticalSection(&m_criticalSection);

	return hr;

}

/*
* Fully initializes a media session for the specified file and using the 
* specified window handle for the video.
*/
HRESULT MMFPlayer::initSession(const wchar_t *path, HWND hwnd) {// TODO remove HWND if init without handle works
	m_hwndVideo = hwnd;
	return this->initSessionWithFile(path);
}

/*
* Changing the media file is not suppported, for each file played a new MMFPlayer is created. 
*/
HRESULT MMFPlayer::setMediaFile(const wchar_t *path) {
	return E_NOTIMPL;
}

/*
* Returns whether the session contains video. Should only be called after initialization of the session. 
*/
bool MMFPlayer::isVisualMedia() {
	return isVideo;
}

/*
* Sets the new window in case of an existing VideoDisplayControl object, 
* initializes the topology in case it is the first time a window is set.
* In the latter case it is assumed the media source (file) has been set.
*/
HRESULT MMFPlayer::setOwnerWindow(HWND hwnd) {
	if (MMFUtil::JMMF_DEBUG) {
		mjlogf("N_MMFPlayer.setOwnerWindow: window handle %p", hwnd);
	}
	if (hwnd == NULL) {
		// don't try to set the window to NULL?
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.setOwnerWindow: setting window handle to NULL not supported");
		}
		return E_POINTER;
	}

	if (m_hwndVideo == NULL) {
		// first initialization
		if (m_pSource == NULL) {
			mjlog("N_MMFPlayer.setOwnerWindow: setting the window handle while there is no source is not supported");
			return E_UNEXPECTED;
		}
		m_hwndVideo = hwnd;
		HRESULT hr = S_OK;

		if (synchronousMode) {
			hr = CreateAndSetTopologySynchronous();
			return hr;
		}

		EnterCriticalSection(&m_criticalSection);
		hr = CreateTopologyFromSource();
		if (FAILED(hr)) {
			mjlogf("N_MMFPlayer.setOwnerWindow: failed to create a topology with window handle: (%d, 0x%X)", hr, hr);
			LeaveCriticalSection(&m_criticalSection);
			return hr;
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.setOwnerWindow: successfully created a topology with window handle");
			}
		}

		//hr = m_pSession->SetTopology(MFSESSION_SETTOPOLOGY_NORESOLUTION, m_pTopology);
		hr = m_pSession->SetTopology(0, m_pTopology);

		if (FAILED(hr)) {
			mjlogf("N_MMFPlayer.setOwnerWindow: failed to set the topology of the session: (%d, 0x%X)", hr, hr);
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.setOwnerWindow: successfully set the topology of the session");
			}
		}

		LeaveCriticalSection(&m_criticalSection);

		return hr;

	} else {
		// check player state, use critical section?
		// check for NULL?
		m_hwndVideo = hwnd;
		HRESULT hr = S_OK;
		if (m_pVideoDisplay != NULL) {
			hr = m_pVideoDisplay->SetVideoWindow(hwnd);
			if (FAILED(hr)) {
				mjlogf("N_MMFPlayer.setOwnerWindow: failed to set the window handle for the Video Display: (%d, 0x%X)", hr, hr);
			} else {
				if (MMFUtil::JMMF_DEBUG) {
					mjlogf("N_MMFPlayer.setOwnerWindow: successfully set the window handle for the Video Display");
				}
			}
		} else {
			mjlogf("N_MMFPlayer.setOwnerWindow: the Video Display is NULL");
			hr = E_POINTER;
		}
		return hr;
	}
}

// copied from MMF's MF_BasicPlayback example
//  Creates a media source from a URL.

HRESULT MMFPlayer::CreateMediaSource(PCWSTR sURL) {
    MF_OBJECT_TYPE ObjectType = MF_OBJECT_INVALID;

    IMFSourceResolver* pSourceResolver = NULL;
    IUnknown* pSource = NULL;

	if (m_pSource != NULL) {
		m_pSource->Release();
		m_pSource = NULL;
	}
    EnterCriticalSection(&m_criticalSection);

	if (MMFUtil::JMMF_DEBUG) {
		mjlogf("N_MMFPlayer.CreateMediaSource: creating source resolver for: %s", sURL);//does this work
	}
    // Create the source resolver.
    HRESULT hr = MFCreateSourceResolver(&pSourceResolver);
    if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.CreateMediaSource: failed to create a source resolver (%d, 0x%X)", hr, hr);
		if (pSourceResolver != NULL) {
			pSourceResolver->Release();
		}
		LeaveCriticalSection(&m_criticalSection);
        return hr;
    }
	//if (MMFUtil::JMMF_DEBUG) {
	//	wprintf(L"N_MMFPlayer Info: CreateMediaSource: Creating a source object for URL: %s\n", sURL);
	//}
    // Use the source resolver to create the media source.

    // Note: For simplicity this sample uses the synchronous method on
    // IMFSourceResolver to create the media source. However, creating a media 
    // source can take a noticeable amount of time, especially for a network 
    // source. For a more responsive UI, use the asynchronous 
    // BeginCreateObjectFromURL method.
	// Note: if this is changed initSessionWithFileSynchronous needs to changed not to call this method

    hr = pSourceResolver->CreateObjectFromURL(// synchronous
                sURL,                       // URL of the source.
                MF_RESOLUTION_MEDIASOURCE | 
					MF_RESOLUTION_CONTENT_DOES_NOT_HAVE_TO_MATCH_EXTENSION_OR_MIME_TYPE,// Create a source object.
                NULL,                       // Optional property store.
                &ObjectType,                // Receives the created object type.
                &pSource                    // Receives a pointer to the media source.
            );
    if (FAILED(hr)){
		//printf("N_MMFPlayer Error: CreateMediaSource: Failed to create a source object from URL: %d (0x%X).\n", hr, hr);
		mjlogf("N_MMFPlayer.CreateMediaSource: failed to create a source object from URL (%d, 0x%X)", hr, hr);
		//_com_error e( hr );
		//wprintf(L"Error: %s.\n", e.Description());

        if (pSourceResolver != NULL) {
			pSourceResolver->Release();
			pSourceResolver = NULL;
		}
		if (pSource != NULL) {
			pSource->Release();
			pSource = NULL;
		}
		LeaveCriticalSection(&m_criticalSection);

        return hr;
    }

    // Get the IMFMediaSource interface from the media source.
    hr = pSource->QueryInterface(IID_PPV_ARGS(&m_pSource));
	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.CreateMediaSource: failed to get the MediaSource interface (%d, 0x%X)", hr, hr);
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.CreateMediaSource: successfully received the MediaSource interface");
		}
	}

	if (SUCCEEDED(hr)) {
		BOOL hasVideo = FALSE;
		HRESULT hres = MMFUtil::HasVideoMediaType(m_pSource, &hasVideo);
		if (FAILED(hres)) {
			mjlogf("N_MMFPlayer.CreateMediaSource: unable to check whether the media type has video (%d, 0x%X)", hres, hres);
		} else {
			mjlogf("N_MMFPlayer.CreateMediaSource: the media type has video: %d ", hasVideo);
			if (hasVideo) {
				isVideo = true;
			} else {
				isVideo = false;
			}
			//isVideo = (bool) hasVideo;
		}
		//CheckMajorMediaType(m_pSource);
	}

	if (pSourceResolver != NULL) {
		pSourceResolver->Release();
		pSourceResolver = NULL;
	}
	if (pSource != NULL) {
		pSource->Release();
		pSource = NULL;
	}
	
	LeaveCriticalSection(&m_criticalSection);

    return hr;
}

//  copied and adapted from MMF's MF_BasicPlayback example
//  Creates a playback topology from the media source.
//
//  Pre-condition: The media source must be created already.
//                 Call CreateMediaSource() before calling this method.

HRESULT MMFPlayer::CreateTopologyFromSource(){
	if (m_pSession == NULL) {
		return E_FAIL;
	}
	
	if (m_pSource == NULL) {
		return E_FAIL;
	}

    IMFPresentationDescriptor* pSourcePD = NULL;
    DWORD cSourceStreams = 0;

	EnterCriticalSection(&m_criticalSection);

    // Create a new topology.
    HRESULT hr = MFCreateTopology(&m_pTopology);
    if (FAILED(hr)) {
		if (m_pTopology != NULL) {
			m_pTopology->Release();
		}
		mjlogf("N_MMFPlayer.CreateTopologyFromSource: failed to create a topology (%ld, 0x%X)", hr, hr);
		LeaveCriticalSection(&m_criticalSection);
		return hr;
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.CreateTopologyFromSource: successfully created a topology");
		}
	}

    // Create the presentation descriptor for the media source.
    hr = m_pSource->CreatePresentationDescriptor(&pSourcePD);// synchronous
    if (FAILED(hr)) {
		if (pSourcePD != NULL) {
			pSourcePD->Release();
			pSourcePD = NULL;
		}
		mjlogf("N_MMFPlayer.CreateTopologyFromSource: failed to create a presentation descriptor (%ld, 0x%X)", hr, hr);
		LeaveCriticalSection(&m_criticalSection);
		return hr;
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogf("N_MMFPlayer.CreateTopologyFromSource: successfully created a presentation descriptor");
		}
		// check if the duration can already been retrieved
		
		HRESULT hhrr = pSourcePD->GetUINT64(MF_PD_DURATION, (UINT64*)&duration);
		if (FAILED(hhrr)) {
			mjlogf("N_MMFPlayer.CreateTopologyFromSource: cannot retrieve the media duration (%ld, 0x%X)", hhrr, hhrr);
		} else {
			mjlogf("N_MMFPlayer.CreateTopologyFromSource: media duration %lld", duration);
		}
	}

    // Get the number of streams in the media source.
    hr = pSourcePD->GetStreamDescriptorCount(&cSourceStreams);
    if (FAILED(hr)) {
		if (pSourcePD != NULL) {
			pSourcePD->Release();
			pSourcePD = NULL;
		}
		mjlogf("N_MMFPlayer.CreateTopologyFromSource: cannot get the number of stream descriptors (%ld, 0x%X)", hr, hr);
		LeaveCriticalSection(&m_criticalSection);
		return hr;
	}
	else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogf("N_MMFPlayer.CreateTopologyFromSource: the number of stream descriptors is %ld", cSourceStreams);
		}
	}

    // For each stream, create the topology nodes and add them to the topology.
	for (DWORD i = 0; i < cSourceStreams; i++) {
		IMFStreamDescriptor* pStreamDesc = NULL;
		BOOL pfSelected;
		pSourcePD->GetStreamDescriptorByIndex(i, &pfSelected, &pStreamDesc);

		hr = AddBranchToPartialTopology(pSourcePD, i);
        if (FAILED(hr)) {
			if (pSourcePD != NULL) {
				pSourcePD->Release();
				pSourcePD = NULL;
			}
			mjlogf("N_MMFPlayer.CreateTopologyFromSource: failed to add branch (stream %ld) to topology (%ld, 0x%X)", i, hr, hr);
			LeaveCriticalSection(&m_criticalSection);
			return hr;
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.CreateTopologyFromSource: successfully added branch (stream %ld) to topology", i);
			}
		}
		if (pStreamDesc != NULL) {
			pStreamDesc->Release();
		}
    }

	if (pSourcePD != NULL) {
		pSourcePD->Release();
		pSourcePD = NULL;
	}
	LeaveCriticalSection(&m_criticalSection);
    return hr;
}

HRESULT MMFPlayer::CreateAndSetTopologySynchronous() {
	HRESULT hr;
	hr = CreateTopologyFromSource();// is synchronous
	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.CreateAndSetTopologySynchronous: failed to create a topology (%ld, 0x%X)", hr, hr);
		return hr;
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.CreateAndSetTopologySynchronous: successfully created a topology");
		}
	}

	//hr = m_pSession->SetTopology(MFSESSION_SETTOPOLOGY_NORESOLUTION, m_pTopology);
	hr = m_pSession->SetTopology(0, m_pTopology);

	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.CreateAndSetTopologySynchronous: failed to set the topology of the session (%ld, 0x%X)", hr, hr);
	} else {
		mjlog("N_MMFPlayer.CreateAndSetTopologySynchronous: successfully set the topology of the session");

		HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionTopologyStatus);

		if (FAILED(ehr)) {
			mjlogf("N_MMFPlayer.CreateAndSetTopologySynchronous: an error occurred while setting the topology of the session (%ld, 0x%X)", ehr, ehr);
			hr = ehr;
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.CreateAndSetTopologySynchronous: successfully set the topology of the session");
			}
		}
	}

	return hr;
}

// copied and adapted from CPlayer
//  Adds a topology branch for one stream.
//
//  pSourcePD: The source's presentation descriptor.
//  iStream: Index of the stream to render.
//
//  Pre-conditions: The topology must be created already.
//
//  Notes: For each stream, we must do the following:
//    1. Create a source node associated with the stream.
//    2. Create an output node for the renderer.
//    3. Connect the two nodes.
//  The media session will resolve the topology, so we do not have
//  to worry about decoders or other transforms.

HRESULT MMFPlayer::AddBranchToPartialTopology(
    IMFPresentationDescriptor *pSourcePD,
    DWORD iStream) {
	if (m_pTopology == NULL) {
		return E_UNEXPECTED;
	}
	HRESULT hr = S_OK;

	IMFStreamDescriptor* pStreamDescriptor = NULL;
	IMFTopologyNode* pSourceNode = NULL;
	IMFTopologyNode* pOutputNode = NULL;
    BOOL streamSelected = FALSE;

	do {
		hr = pSourcePD->GetStreamDescriptorByIndex(iStream, &streamSelected, 
            &pStreamDescriptor);
		if (FAILED(hr)){
			mjlogf("N_MMFPlayer.AddBranchToPartialTopology: failed to get the stream descriptor at index %d (%ld, 0x%X)", iStream, hr, hr);
			break;
		}
		else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.AddBranchToPartialTopology: got the stream descriptor at index %d, is selected %d", iStream, streamSelected);
			}
		}

		// Create the topology branch only if the stream is selected - IE if the user(?) wants to play it.
		if (streamSelected) {
			// Create a source node for this stream.
            hr = CreateSourceStreamNode(pSourcePD, pStreamDescriptor, &pSourceNode);
			if (FAILED(hr)){
				mjlogf("N_MMFPlayer.AddBranchToPartialTopology: failed to create a source node for index %d (%ld, 0x%X)", iStream, hr, hr);
				break;
			}
			else {
				if (MMFUtil::JMMF_DEBUG) {
					mjlogf("N_MMFPlayer.AddBranchToPartialTopology: created a source node for index %d", iStream);
				}
			}

			// Create the output, sink node for the renderer.
            hr = CreateOutputNode(pStreamDescriptor, &pOutputNode);
            if (FAILED(hr)){
				mjlogf("N_MMFPlayer.AddBranchToPartialTopology: failed to create a sink node for index %d (%ld, 0x%X)", iStream, hr, hr);
				break;
			}
			else {
				if (MMFUtil::JMMF_DEBUG) {
					mjlogf("N_MMFPlayer.AddBranchToPartialTopology: created a sink node for index %d", iStream);
				}
			}

			// Add the source and sink nodes to the topology.
            hr = m_pTopology->AddNode(pSourceNode);
			if (FAILED(hr)){
				mjlogf("N_MMFPlayer.AddBranchToPartialTopology: failed to add the source node to the topology %d (%ld, 0x%X)", iStream, hr, hr);
				break;
			}
			else {
				if (MMFUtil::JMMF_DEBUG) {
					mjlogf("N_MMFPlayer.AddBranchToPartialTopology: added the source node to the topology for index %d", iStream);
				}
			}

			hr = m_pTopology->AddNode(pOutputNode);
			if (FAILED(hr)){
				mjlogf("N_MMFPlayer.AddBranchToPartialTopology: failed to add the sink node to the topology %d (%ld, 0x%X)", iStream, hr, hr);
				break;
			}
			else {
				if (MMFUtil::JMMF_DEBUG) {
					mjlogf("N_MMFPlayer.AddBranchToPartialTopology: added the sink node to the topology for index %d", iStream);
				}
			}

			// Connect the source node to the sink node.  The resolver will find the
            // intermediate nodes needed to convert media types.
            hr = pSourceNode->ConnectOutput(0, pOutputNode, 0);
			// cache the source node, remove when setting the stop time is done differently
			if (SUCCEEDED(hr)) {
				IMFMediaTypeHandler *pHandler = NULL;
			    GUID guidMajorType = GUID_NULL;
				HRESULT cr;
				cr = pStreamDescriptor->GetMediaTypeHandler(&pHandler);
				if (SUCCEEDED(cr)) {
					cr = pHandler->GetMajorType(&guidMajorType);
					if (SUCCEEDED(cr)){
						if (guidMajorType == MFMediaType_Video) {
							if (MMFUtil::JMMF_DEBUG) {
								mjlogf("N_MMFPlayer.AddBranchToPartialTopology: major source type is video at index %d", iStream);
							}
							// in order to get the frame rate (= num frames per second) get the media type from the handler, 
							// and call MFGetAttributeRatio
							IMFMediaType *pMediaType = NULL;
							cr = pHandler->GetCurrentMediaType(&pMediaType);
							if (SUCCEEDED(cr)) {
								cr = MFGetAttributeRatio(pMediaType, MF_MT_FRAME_RATE, &frameRateNumerator, &frameRateDenominator);
								if (FAILED(cr)) {
									mjlogf("N_MMFPlayer.AddBranchToPartialTopology: cannot retrieve the frame rate from the media type (%ld, 0x%X)", cr, cr);
								} else {
									mjlogf("N_MMFPlayer.AddBranchToPartialTopology: frame rate, numerator: %d, denominator: %d", frameRateNumerator, frameRateDenominator);
								}
							}
							
							if (pMediaType != NULL) {
								pMediaType->Release();
								pMediaType = NULL;
							}
						}
						else if (guidMajorType == MFMediaType_Audio){
							if (MMFUtil::JMMF_DEBUG) {
								mjlogf("N_MMFPlayer.AddBranchToPartialTopology: major source type is audio at index %d", iStream);
							}
						}
						else {
							if (MMFUtil::JMMF_DEBUG) {
								mjlogf("N_MMFPlayer.AddBranchToPartialTopology: major source type is video nor audio at index %d", iStream);
							}
						}
					}
				}
				if (pHandler != NULL) {
					pHandler->Release();
					pHandler = NULL;
				}
			} 
			else {
				hr = S_FALSE;
				if (MMFUtil::JMMF_DEBUG) {
					mjlogf("N_MMFPlayer.AddBranchToPartialTopology: failed to connect source and sink node for index %d (%lld, 0x%X)", iStream, hr, hr);
				}
			}
		}
		else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.AddBranchToPartialTopology: the stream descriptor at index %d is not selected", iStream);
			}
		}
	} while (false);

	if (pStreamDescriptor != NULL) {
		pStreamDescriptor->Release();
		pStreamDescriptor = NULL;
	}
	if (pSourceNode != NULL) {
		pSourceNode->Release();
		pSourceNode = NULL;
	}
	if (pOutputNode != NULL) {
		pOutputNode->Release();
		pOutputNode = NULL;
	}

	return hr;
}

void CheckMajorMediaType(IMFMediaSource *pMediaSource) {
	// not implemented yet
}

//  Creates a source-stream node for a stream.
//
//  pSourcePresD: Presentation descriptor for the media source.
//  pSourceStreamD: Stream descriptor for the stream.
//  ppNode: Receives a pointer to the new node.

HRESULT MMFPlayer::CreateSourceStreamNode(IMFPresentationDescriptor *pSourcePresD,
	IMFStreamDescriptor *pSourceStreamD, IMFTopologyNode **ppNode) {
	HRESULT hr = S_OK;
	
	if (pSourcePresD == NULL || pSourceStreamD == NULL || ppNode == NULL) {
		return E_POINTER;
	}

	IMFTopologyNode *pNode = NULL;

	do {
		// Create the source-stream node.
		HRESULT hr = MFCreateTopologyNode(MF_TOPOLOGY_SOURCESTREAM_NODE, &pNode);
		if (FAILED(hr)) {
			mjlogf("N_MMFPlayer.CreateSourceStreamNode: failed to create topology node (%ld, 0x%X)", hr, hr);
			break;
		}
		// Associate the node with the source by passing in a pointer to the media source
        // and indicating that it is the source
        hr = pNode->SetUnknown(MF_TOPONODE_SOURCE, m_pSource);
		if (FAILED(hr)) {
			mjlogf("N_MMFPlayer.CreateSourceStreamNode: failed to set the source for the node (%ld, 0x%X)", hr, hr);
			break;
		}
		// Set the node presentation descriptor attribute of the node by passing 
        // in a pointer to the presentation descriptor
        hr = pNode->SetUnknown(MF_TOPONODE_PRESENTATION_DESCRIPTOR, pSourcePresD);
		if (FAILED(hr)) {
			mjlogf("N_MMFPlayer.CreateSourceStreamNode: failed to set the presentation descriptor for the node (%ld, 0x%X)", hr, hr);
			break;
		}
		// Set the node stream descriptor attribute by passing in a pointer to the stream
        // descriptor
        hr = pNode->SetUnknown(MF_TOPONODE_STREAM_DESCRIPTOR, pSourceStreamD);
		if (FAILED(hr)) {
			mjlogf("N_MMFPlayer.CreateSourceStreamNode: failed to set the stream descriptor for the node (%ld, 0x%X)", hr, hr);
			break;
		} 
	} while (false);

	if (pNode != NULL) {
		// Return the IMFTopologyNode pointer to the caller.
		*ppNode = pNode;
		(*ppNode)->AddRef();

		pNode->Release();
	}
	return hr;
}

//  Creates an output node for a stream.
//
//  pSourceStreamD: Stream descriptor for the stream.
//  ppNode: Receives a pointer to the new node.
//
//  Notes:
//  This function does the following:
//  1. Chooses a renderer based on the media type of the stream.
//  2. Creates an IActivate object for the renderer.
//  3. Creates an output topology node.
//  4. Sets the IActivate pointer on the node.
HRESULT MMFPlayer::CreateOutputNode(IMFStreamDescriptor *pSourceStreamD,
    IMFTopologyNode **ppNode) {
	if (pSourceStreamD == NULL || ppNode == NULL) {
		return E_POINTER;
	}

	HRESULT hr = S_OK;

	IMFTopologyNode *pNode = NULL;
    IMFMediaTypeHandler *pHandler = NULL;
    IMFActivate *pRendererActivate = NULL;

    GUID guidMajorType = GUID_NULL;

	do {
		// Get the media type handler for the stream.
		hr = pSourceStreamD->GetMediaTypeHandler(&pHandler);
		if (FAILED(hr)) {
			mjlogf("N_MMFPlayer.CreateOutputNode: failed to get the media type handler of the source stream (%ld, 0x%X)", hr, hr);
			break;
		}
		
		// Get the major media type.
		hr = pHandler->GetMajorType(&guidMajorType);
		if (FAILED(hr)) {
			mjlogf("N_MMFPlayer.CreateOutputNode: failed to get the major type of the media handler (%ld, 0x%X)", hr, hr);
			break;
		}

		// Create an IMFActivate controller object for the renderer, based on the media type
        // The activation objects are used by the session in order to create the renderers 
        // only when they are needed - i.e. only right before starting playback.  The 
        // activation objects are also used to shut down the renderers.
        if (guidMajorType == MFMediaType_Audio) {
            // if the stream major type is audio, create the audio renderer.
            hr = MFCreateAudioRendererActivate(&pRendererActivate);
			if (FAILED(hr)) {
				mjlogf("N_MMFPlayer.CreateOutputNode: failed to create an audio renderer activation object (%ld, 0x%X)", hr, hr);
			}
        } else if (guidMajorType == MFMediaType_Video) {
			if (m_hwndVideo == NULL) {
				hr = E_UNEXPECTED;
				if (FAILED(hr)) {
					mjlogf("N_MMFPlayer.CreateOutputNode: failed to create a video renderer activation object without window handle (%ld, 0x%X)", hr, hr);
				}
			} else {
				// if the stream major type is video, create the video renderer, passing in the
				// video window handle - that's where the video will be playing.
				hr = MFCreateVideoRendererActivate(m_hwndVideo, &pRendererActivate);
				if (FAILED(hr)) {
					mjlogf("N_MMFPlayer.CreateOutputNode: failed to create a video renderer activation object with window handle (%ld, 0x%X)", hr, hr);
				}
			}
        } else {
            // fail if the stream type is not video or audio.  For example, fail
            // if we encounter a CC stream.
            hr = E_FAIL;
			mjlog("N_MMFPlayer.CreateOutputNode: failed to create a node, the major type is audio nor video");
        }
		if (FAILED(hr)) {
			break;
		}


		// Create a downstream node, the node that will represent the renderer.
		hr = MFCreateTopologyNode(MF_TOPOLOGY_OUTPUT_NODE, &pNode);

		if (FAILED(hr)) {
			mjlogf("N_MMFPlayer.CreateOutputNode: failed to create an output node for the renderer (%ld, 0x%X)", hr, hr);
			break;
		}
		// Store the IActivate object in the sink node - it will be extracted later by the
        // media session during the topology render phase.
        hr = pNode->SetObject(pRendererActivate);
		if (FAILED(hr)) {
			mjlogf("N_MMFPlayer.CreateOutputNode: failed to add the render activation object to the output node (%ld, 0x%X)", hr, hr);
		}
	} while (false);

	if (pNode != NULL) {
		// Return the IMFTopologyNode pointer to the caller.
		*ppNode = pNode;
		(*ppNode)->AddRef();

		pNode->Release();
	}
	if (pHandler != NULL) {
		pHandler->Release();
	}
	if (pRendererActivate != NULL) {
		pRendererActivate->Release();
	}
	return hr;
}

//  Callback for asynchronous BeginGetEvent method.
//
//  pAsyncResult: Pointer to the result.

HRESULT MMFPlayer::Invoke(IMFAsyncResult *pResult) {
	if (MMFUtil::JMMF_DEBUG) {
		mjlog("N_MMFPlayer.Invoke: invoke called");
	}
	if (m_pSession == NULL || cleanUpCalled) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.Invoke: invoke called after the session has been closed and player is cleaning up");
		}
		return E_UNEXPECTED;
	}
	if (m_state == PlayerState_Closed) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.Invoke: invoke called after the session has been closed");
		}
		return E_UNEXPECTED;
	}
	if (pResult == NULL) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.Invoke: Result is NULL");
		}
		return E_UNEXPECTED;
	}
    IMFMediaEvent *pEvent = NULL;
	IUnknown *punkState = NULL;
	HRESULT hr = S_OK;

	EnterCriticalSection(&m_criticalSection);

	do {
		HRESULT hhrr = pResult->GetState(&punkState);
		if (SUCCEEDED(hhrr)) {
			// the result status is S_OK in case a timer has been called at the designated time
			// when the player is stopped by hand an event is generated but the status is an error code
			HRESULT statusHR = pResult->GetStatus();

			if (punkState == pStopState) {
				if (MMFUtil::JMMF_DEBUG) {
					mjlogf("N_MMFPlayer.Invoke: received the stop time event, status is (%ld, 0x%X)", statusHR, statusHR);
				}
				punkState->Release();
				if (SUCCEEDED(statusHR)) {
					if (synchronousMode) {// this should not happen anymore, no mixing of synchronous and asynchronous calls
						// pull out the end event to be able to continue in sync mode
						HRESULT eehr = m_pSession->EndGetEvent(pResult, &pEvent);
						mjlogf("N_MMFPlayer.Invoke: Stop got EndGetEvent (%ld, 0x%X)", eehr, eehr);
					}
					hr = ProcessStopTimeEvent();
				}
				break;
			} 
			else if (punkState == pEndState) {
				if (MMFUtil::JMMF_DEBUG) {
					mjlogf("N_MMFPlayer.Invoke: received end of media event, status is (%ld, 0x%X)", statusHR, statusHR);
				}
				punkState->Release();
				if (SUCCEEDED(statusHR)) {
					if (synchronousMode) {// this should not happen anymore, no mixing of synchronous and asynchronous calls
						// pull out the end event to be able to continue in sync mode
						HRESULT eehr = m_pSession->EndGetEvent(pResult, &pEvent);
						mjlogf("N_MMFPlayer.Invoke: End got EndGetEvent (%ld, 0x%X)", eehr, eehr);
					}
					hr = ProcessEndTimeEvent();
				}
				break;
			}
			
		}
		// handle events
		hr = m_pSession->EndGetEvent(pResult, &pEvent);

		if (FAILED(hr)) {
			mjlogf("N_MMFPlayer.Invoke: failed to get the media end event (%ld, 0x%X)", hr, hr);
			break;
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.Invoke: successfully got the media end event");
			}
		}
		// handle event if the player is not closing
		
		//if (m_state != PlayerState_Closing) {
			hr = ProcessMediaEvent(pEvent);
           	if (FAILED(hr)) {
				mjlogf("N_MMFPlayer.Invoke: failed to process the media event (%ld, 0x%X)", hr, hr);
				//break; // don't break, keep on pulling events
			} else {
				if (MMFUtil::JMMF_DEBUG) {
					mjlog("N_MMFPlayer.Invoke: successfully processed the media event");
				}
			}
		//}
		
		if (hr != S_FALSE && m_state != PlayerState_Closed) {
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.Invoke: begin get event");
			}
			hr = m_pSession->BeginGetEvent((IMFAsyncCallback*)this, NULL);
		}
		
		
	} while (false);

	if (punkState != NULL) {
		punkState->Release();
	}
	if (pEvent != NULL) {
		pEvent->Release();
	}

	LeaveCriticalSection(&m_criticalSection);

	return hr;
}

//
//  Called by Invoke() to do the actual event processing, and determine what, if anything,
//  needs to be done.  Returns S_FALSE if the media event type is MESessionClosed.
//
HRESULT MMFPlayer::ProcessMediaEvent(IMFMediaEvent *pMediaEvent) {
    HRESULT hrStatus = S_OK;            // Event status
    HRESULT hr = S_OK;
    UINT32 TopoStatus = MF_TOPOSTATUS_INVALID; 
    MediaEventType eventType;
    do {
		if( pMediaEvent == NULL) {
			hr = E_POINTER;	
			mjlog("N_MMFPlayer.ProcessMediaEvent: the event is NULL");
			break;
		}

        // Get the event type.
        hr = pMediaEvent->GetType(&eventType);
		if(FAILED(hr)) {
			mjlog("N_MMFPlayer.ProcessMediaEvent: failed to get the media event type");
			break;
		} else {
			// Switch on the event type. Update the internal state of the Player as needed.
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.ProcessMediaEvent: the media event type is: ");
				switch(eventType) {
					case MESessionTopologySet:
						mjlogf("%d - MESessionTopologySet", eventType);
						break;
					case MESessionStarted:
						mjlogf("%d - MESessionStarted", eventType);
						break;
					case MESessionPaused:
						mjlogf("%d - MESessionPaused", eventType);
						break;
					case MESessionStopped:
						mjlogf("%d - MESessionStopped", eventType);
						break;
					case MESessionClosed:
						mjlogf("%d - MESessionClosed", eventType);
						break;
					case MESessionEnded:
						mjlogf("%d - MESessionEnded", eventType);
						break;
					case MESessionRateChanged:
						mjlogf("%d - MESessionRateChanged", eventType);
						break;
					case MESessionScrubSampleComplete:
						mjlogf("%d - MESessionScrubSampleComplete", eventType);
						break;
					case MESessionCapabilitiesChanged:
						mjlogf("%d - MESessionCapabilitiesChanged", eventType);
						break;
					case MESessionTopologyStatus:
						mjlogf("%d - MESessionTopologyStatus", eventType);
						break;
					case MESessionNotifyPresentationTime:
						mjlogf("%d - MESessionNotifyPresentationTime", eventType);
						break;
					case MENewPresentation:
						mjlogf("%d - MENewPresentation", eventType);
						break;
					case MESessionStreamSinkFormatChanged:
						mjlogf("%d - MESessionStreamSinkFormatChanged", eventType);
						break;
					case MEEndOfPresentation:
						mjlogf("%d - MEEndOfPresentation", eventType);
						break;
					default:
						mjlogf("%d", eventType);
				}
			}
		}

        // Get the event status. If the operation that triggered the event did
        // not succeed, the status is a failure code.
        hr = pMediaEvent->GetStatus(&hrStatus);
		if(FAILED(hr)) {
			mjlogf("N_MMFPlayer.ProcessMediaEvent: failed to get the media event status (%ld, 0x%X)", hr, hr);
			break;
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.ProcessMediaEvent: the media event status is (%ld, 0x%X)", hrStatus, hrStatus);
			}
		}

        // Check if the async operation succeeded.
        if (FAILED(hrStatus)) {
            hr = hrStatus;
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.ProcessMediaEvent: the media event status indicates an error: (%ld, 0x%X)", hrStatus, hrStatus);
			}
            //break;//?? this prevents checking the event type, maybe don't break?
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.ProcessMediaEvent: the media event status is OK");
			}
		}

		// Switch on the event type. Update the internal state of the Player as needed.
		// Because of the do-while(false) pattern we don't use a switch statement here
        if (eventType == MESessionTopologyStatus) {
            // Get the status code.
            hr = pMediaEvent->GetUINT32(MF_EVENT_TOPOLOGY_STATUS, (UINT32*)&TopoStatus);
            if(FAILED(hr)) {
				mjlogf("N_MMFPlayer.ProcessMediaEvent: failed to get the topology status (%ld, 0x%X)", hr, hr);
				break;
			}

            if (TopoStatus == MF_TOPOSTATUS_READY) {
				if (MMFUtil::JMMF_DEBUG) {
					mjlog("N_MMFPlayer.ProcessMediaEvent: topology status: Ready");
				}
                m_state = PlayerState_Ready;//??
			
                hr = OnTopologyReady();
            }
		} else if (eventType == MESessionTopologySet) {
			// Get the status code? No, this does not work with MESessionTopologySet.
			// The status here is the "hrStatus" retrieved from the event through the GetStatus call above

			if (FAILED(hrStatus)) {
				mjlogf("N_MMFPlayer.ProcessMediaEvent: MESessionTopologySet event with status (%ld, 0x%X)", hrStatus, hrStatus);
				if (hrStatus == MF_E_CANNOT_CREATE_SINK) {
					mjlog("\tError: MF_E_CANNOT_CREATE_SINK");
				}
			}
		} else if (eventType == MEEndOfPresentation || eventType == MESessionEnded) {
            //m_state = PlayerState_Stopped;
			m_state = PlayerState_Paused;
			// the session rewinds to begin time, try to prevent this//
			//setMediaPosition(getDuration() - 800000);
        } else if (eventType == MESessionClosed) {
			if (pendingAction->action == SetStateClosed) {
				m_state = PlayerState_Closed;
				clearPendingAction(pendingAction);
			}
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.ProcessMediaEvent: setting session close event");
			}
            // signal to anybody listening that the session is closed
			// maybe this is not needed since the player is managed in a JVM
            //SetEvent(m_closeCompleteEvent);
		} else if (eventType == MESessionPaused) {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.ProcessMediaEvent: media paused at time %lld", getMediaPosition());
			}
			if (pendingAction->action == SetStatePaused) {
				m_state = PlayerState_Paused;
				if (MMFUtil::JMMF_CORRECT_AT_PAUSE) { 
					// this forces the video 2 or 3 frames back to the position of the time
					if (pendingAction->timeValue > 0) {
						setMediaPosition(pendingAction->timeValue);
					} else {
						mjlog("N_MMFPlayer.ProcessMediaEvent: the pending media pause time was set to -1");
					}
				}
				clearPendingAction(pendingAction);
			} 
			//else if (pendingAction->action == SetMediaPosition) {
			//}
			//setMediaPosition(getMediaPosition());
		} else if (eventType == MESessionStarted) {
			if (pendingAction->action == SetStateStarted) {
				clearPendingAction(pendingAction);
				clearPendingAction(cachedAction);
				if (MMFUtil::JMMF_DEBUG) {
					mjlog("N_MMFPlayer.ProcessMediaEvent: set started state event");
				}
				m_state = PlayerState_Started;
			} else if (pendingAction->action == SetMediaPosition) {
				if (MMFUtil::JMMF_DEBUG) {
					mjlogf("N_MMFPlayer.ProcessMediaEvent: started media event, as part of a seek position operation: %lld", pendingAction->timeValue);
				}
			} else {
				if (MMFUtil::JMMF_DEBUG) {
					mjlogf("N_MMFPlayer.ProcessMediaEvent: started media event without setting started state: %lld", getMediaPosition());
				}
			} 
		} else if (eventType == MESessionStopped) {
			if (pendingAction->action == SetStateStopped) {
					clearPendingAction(pendingAction);
					m_state = PlayerState_Stopped;
					
				}
		} else if (eventType == MESessionRateChanged) {
			if (FAILED(hrStatus)) {
				mjlogf("N_MMFPlayer.ProcessMediaEvent: setting rate failed, requested rate %f, actual rate %f", userPlaybackRate, getRate());
			}
			if (pendingAction->action == SetRate) {
				clearPendingAction(pendingAction);
				
				if (cachedAction->action == SetRate) {
					setRateCached((float)cachedAction->rateValue);
					clearPendingAction(cachedAction);
				}
			}
		} else if (eventType == MESessionScrubSampleComplete) {
			if (pendingAction->action == SetMediaPosition) {
				if (MMFUtil::JMMF_DEBUG) {
					mjlogf("N_MMFPlayer.ProcessMediaEvent: started media event, as part of a seek operation: %lld", getMediaPosition());
					if (pendingAction->timeValue != getMediaPosition()) {
						mjlogf("N_MMFPlayer.ProcessMediaEvent: media set to the wrong position: %lld, %lld", pendingAction->timeValue, getMediaPosition());
					}
				}
				clearPendingAction(pendingAction);
				// the last event of a set position or seek action is the scrub complete
				if (cachedAction->action == SetMediaPosition) {
					setMediaPositionCached(cachedAction->timeValue);
					clearPendingAction(cachedAction);
				} else {
					m_state = PlayerState_Paused;
				}
			}

		}
    }
    while(false);
	
    return hr;
}

/*
* Checks a media event that has been produced in synchronous event handling mode.
*/
HRESULT MMFPlayer::ProcessMediaEventSynchronous(IMFMediaEvent *pMediaEvent) {
    HRESULT hrStatus = S_OK;            // Event status
    HRESULT hr = S_OK;
    UINT32 TopoStatus = MF_TOPOSTATUS_INVALID; 
    MediaEventType eventType;
    do {
		if( pMediaEvent == NULL) {
			hr = E_POINTER;	
			mjlog("N_MMFPlayer.ProcessMediaEventSynchronous: the event is NULL");
			break;
		}

        // Get the event type.
        hr = pMediaEvent->GetType(&eventType);
		if(FAILED(hr)) {
			mjlog("N_MMFPlayer.ProcessMediaEventSynchronous:  failed to get the media event type");
			break;
		} else {
			// Switch on the event type. Update the internal state of the Player as needed.
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.ProcessMediaEventSynchronous: the media event type is: ");
				switch(eventType) {
					case MESessionTopologySet:
						mjlogf("%d - MESessionTopologySet", eventType);
						break;
					case MESessionStarted:
						mjlogf("%d - MESessionStarted", eventType);
						break;
					case MESessionPaused:
						mjlogf("%d - MMESessionPaused", eventType);
						break;
					case MESessionStopped:
						mjlogf("%d - MESessionStopped", eventType);
						break;
					case MESessionClosed:
						mjlogf("%d - MESessionClosed", eventType);
						break;
					case MESessionEnded:
						mjlogf("%d - MESessionEnded", eventType);
						break;
					case MESessionRateChanged:
						mjlogf("%d - MESessionRateChanged", eventType);
						break;
					case MESessionScrubSampleComplete:
						mjlogf("%d - MESessionScrubSampleComplete", eventType);
						break;
					case MESessionCapabilitiesChanged:
						mjlogf("%d - MESessionCapabilitiesChanged", eventType);
						break;
					case MESessionTopologyStatus:
						mjlogf("%d - MESessionTopologyStatus", eventType);
						break;
					case MESessionNotifyPresentationTime:
						mjlogf("%d - MESessionNotifyPresentationTime", eventType);
						break;
					case MENewPresentation:
						mjlogf("%d - MENewPresentation", eventType);
						break;
					case MESessionStreamSinkFormatChanged:
						mjlogf("%d - MESessionStreamSinkFormatChanged", eventType);
						break;
					case MEEndOfPresentation:
						mjlogf("%d - MEEndOfPresentation", eventType);
						break;
					default:
						mjlogf("%d", eventType);
				}
			}
		}

        // Get the event status. If the operation that triggered the event did
        // not succeed, the status is a failure code.
        hr = pMediaEvent->GetStatus(&hrStatus);
		if(FAILED(hr)) {
			mjlogf("N_MMFPlayer.ProcessMediaEventSynchronous:  failed to get the media event status (%ld, 0x%X)", hr, hr);
			break;
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.ProcessMediaEventSynchronous: the media event status is (%ld, 0x%X)", hrStatus, hrStatus);
			}
		}

        // Check if the media operation succeeded.
        if (FAILED(hrStatus)) {
            hr = hrStatus;
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.ProcessMediaEventSynchronous: the media event status indicates an error (%ld, 0x%X)", hr, hrStatus);
			}
            //break;//?? this prevents checking the event type, maybe don't break?
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.ProcessMediaEventSynchronous: the media event status is OK");
			}
		}

		// Switch on the event type. Update the internal state of the Player as needed.
		// Because of the do-while(false) pattern we don't use a switch statement here
        if (eventType == MESessionClosed) {
			m_state = PlayerState_Closed;
			
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.ProcessMediaEventSynchronous: setting session close event");
			}
		} else if (eventType == MESessionPaused) {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.ProcessMediaEventSynchronous:  media paused at time %lld", getMediaPosition());
			}

			m_state = PlayerState_Paused;
		} else if (eventType == MESessionStarted) {
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.ProcessMediaEventSynchronous: set started state");
			}
			m_state = PlayerState_Started; 
		} else if (eventType == MESessionStopped) {
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.ProcessMediaEventSynchronous: set stopped state");
			}
			m_state = PlayerState_Stopped;
		} else if (eventType == MESessionRateChanged) {
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.ProcessMediaEventSynchronous: the rate changed");
			}
			if (FAILED(hrStatus)) {
				mjlogf("N_MMFPlayer.ProcessMediaEventSynchronous:  setting the rate failed, requested rate %f, actual rate %f", userPlaybackRate, getRate());
			}
		} else if (eventType == MESessionScrubSampleComplete) {
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.ProcessMediaEventSynchronous: scrub frame complete");
			}
			m_state = PlayerState_Paused;
		} else if (eventType == MESessionTopologyStatus) {
				// only in this case we'll worry about the event status
			if (FAILED(hrStatus)) {
				mjlogf("N_MMFPlayer.ProcessMediaEventSynchronous:  the topology status event indicates a status error (%ld, 0x%X)", hrStatus, hrStatus);
				hr = hrStatus;
				break;
			}
			HRESULT subHr = pMediaEvent->GetUINT32(MF_EVENT_TOPOLOGY_STATUS, (UINT32*)&TopoStatus);
			
			if (FAILED(subHr)) {
				mjlogf("N_MMFPlayer.ProcessMediaEventSynchronous:  failed to get the topology status of the event (%ld, 0x%X)", subHr, subHr);
				hr = subHr;
				break;
			}
			if (TopoStatus == MF_TOPOSTATUS_READY) {
				if (MMFUtil::JMMF_DEBUG) {
					mjlog("N_MMFPlayer.ProcessMediaEventSynchronous: topology status: Ready");
				}
				m_state = PlayerState_Ready;//??

				hr = OnTopologyReady();
				break;
			} else {
				if (MMFUtil::JMMF_DEBUG) {
					mjlog("N_MMFPlayer.ProcessMediaEventSynchronous: topology status: Not Ready");
				}
			}
		} else if (eventType == MESessionTopologySet) {
			// The status here is the "hrStatus" retrieved from the event through the GetStatus call above

			if (FAILED(hrStatus)) {
				mjlogf("N_MMFPlayer.ProcessMediaEventSynchronous: MESessionTopologySet event with status (%ld, 0x%X)", hrStatus, hrStatus);
				if (hrStatus == MF_E_CANNOT_CREATE_SINK) {
					mjlog("\tError: MF_E_CANNOT_CREATE_SINK");
				}
			}
		}
    }
    while(false);
	
    return hr;
}

/*
* Method to pull events from the session until the expected event occurred.
* If S_OK is returned the pEvent parameter will hold the event.
* The caller is responsible for releasing the event. 
*/
HRESULT MMFPlayer::PullMediaEventsUntilEventTypeSynchronous(MediaEventType eventType) {
	if (m_pSession == NULL) {
		mjlog("N_MMFPlayer.PullMediaEventsUntilEventTypeSynchronous: the media session is null");

		return E_POINTER;
	}

	if (MMFUtil::JMMF_DEBUG) {
		mjlogf("N_MMFPlayer.PullMediaEventsUntilEventTypeSynchronous: start pulling until event type %d", eventType);
	}

	IMFMediaEvent *pEvent = NULL;
	HRESULT hr = S_OK;
	HRESULT hrType = S_OK;
	MediaEventType currentType = MEUnknown;
	int counter = 0;
	int maxNumEvents = 10;
	do {
		counter++;
		hr = m_pSession->GetEvent(0, &pEvent);
		if (FAILED(hr)) {
			mjlogf("N_MMFPlayer.PullMediaEventsUntilEventTypeSynchronous: unable to GetEvent from Session (%d, 0x%X)", hr, hr);
			if (MMFUtil::JMMF_DEBUG) {
				if (hr == MF_E_MULTIPLE_SUBSCRIBERS) {
					mjlog("N_MMFPlayer.PullMediaEventsUntilEventTypeSynchronous: MF_E_MULTIPLE_SUBSCRIBERS");
					// calling m_pSession->EndGetEvent(0,&pEvent); here doesn't help, it doesn't remove the multiple subscribers error
				} 
				else if (hr == MF_E_NO_EVENTS_AVAILABLE) {
					mjlog("N_MMFPlayer.PullMediaEventsUntilEventTypeSynchronous: MF_E_NO_EVENTS_AVAILABLE");
				} 
				else if (hr == MF_E_SHUTDOWN) {
					mjlog("N_MMFPlayer.PullMediaEventsUntilEventTypeSynchronous: MF_E_SHUTDOWN");
				}
				else if (hr == E_INVALIDARG) {
					mjlog("N_MMFPlayer.PullMediaEventsUntilEventTypeSynchronous: E_INVALIDARG");
				}
				else {
					mjlog("N_MMFPlayer.PullMediaEventsUntilEventTypeSynchronous: unknown error");
				}			
			}
			break;
		}
		// use other result 
		hr = ProcessMediaEventSynchronous(pEvent);

		if (FAILED(hr)) {
			mjlogf("N_MMFPlayer.PullMediaEventsUntilEventTypeSynchronous: error during processing of the event (%d, 0x%X)", hr, hr);
			break;
		} else {
			hrType = pEvent->GetType(&currentType);
			if (FAILED(hrType)) {//
				mjlogf("N_MMFPlayer.PullMediaEventsUntilEventTypeSynchronous: unable to GetType of the event (%d, 0x%X)", hrType, hrType);
				hr = hrType;
				break;
			} else {
				if (eventType == currentType) {
					// success
					// the caller can check status etc.
					if (MMFUtil::JMMF_DEBUG) {
						mjlogf("N_MMFPlayer.PullMediaEventsUntilEventTypeSynchronous: found the right event type in %d attempts", counter);
					}
					break;
				} else {
					if (MMFUtil::JMMF_DEBUG) {
						mjlogf("N_MMFPlayer.PullMediaEventsUntilEventTypeSynchronous: not the right event type in attempt: %d", counter);
					}
				}
			}
		}

		if (counter == maxNumEvents - 1) {
			hr = S_FALSE;//To Do find an appropriate return value
			mjlogf("N_MMFPlayer.PullMediaEventsUntilEventTypeSynchronous: did not get the requested event type within %d tries", maxNumEvents);
		}

	} while (counter < 10);

	if (pEvent != NULL) {
		pEvent->Release();
	}

	return hr;
}

/*
* Stops the player after receiving a StopTimer event via Invoke.
*/
HRESULT MMFPlayer::ProcessStopTimeEvent() {
	if (cleanUpCalled) {
		mjlog("N_MMFPlayer.ProcessStopTimeEvent: timer callback called after closing the session");
	}
	HRESULT hr = S_OK;
	__int64 curTime = getMediaPosition();
	if (MMFUtil::JMMF_DEBUG) {
		mjlogf("N_MMFPlayer.ProcessStopTimeEvent: timer callback called at %lld", curTime);
	}
	__int64 diff = stopTime - curTime;
	if (diff > -1000000 && diff < 1000000) {// arbitrary precision
		hr = pause();
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogf("N_MMFPlayer.ProcessStopTimeEvent: false callback, wrong time: %lld, stop time: %lld", curTime, stopTime);
		}
	}
	return hr;
}

/*
* Stops the player when a (close to) end of media timer event has been received. 
* By default the session rewinds to the begin of media once the end has been reached.
*/
HRESULT MMFPlayer::ProcessEndTimeEvent() {
	if (cleanUpCalled) {
		mjlogf("N_MMFPlayer.ProcessEndTimeEvent: timer callback called after closing the session");
	}
	HRESULT hr = S_OK;
	__int64 curTime = getMediaPosition();
	if (MMFUtil::JMMF_DEBUG) {
		mjlogf("N_MMFPlayer.ProcessEndTimeEvent: timer callback called at %lld, duration is %lld.\n", curTime, duration);
	}

	hr = pause();

	return hr;
}

/*
* Returns the current player state, without taking into account pending
* player state changes.
*/
int MMFPlayer::getPlayerState() {
	return m_state;
}

/*
* Get the video display, clock, rate support, rate control etc. when the topology is resolved and ready
*/
HRESULT MMFPlayer::OnTopologyReady() {
	if (topoInited) {
		mjlog("N_MMFPlayer.OnTopologyReady: the topology was already initialized");
		return S_FALSE;
	}
	HRESULT hr = S_OK;

    do {
		// release any previous instance of the m_pVideoDisplay interface
        //m_pVideoDisplay->Release();

		// Ask the session for the IMFVideoDisplayControl interface. This interface is 
        // implemented by the EVR (Enhanced Video Renderer) and is exposed by the media 
        // session as a service.  The session will query the topology for the right 
        // component and return this EVR interface.  The interface will be used to tell the
        // video to repaint whenever the hosting window receives a WM_PAINT window message.
        hr = MFGetService(m_pSession, MR_VIDEO_RENDER_SERVICE,  IID_IMFVideoDisplayControl,
                (void**)&m_pVideoDisplay);
		if (FAILED(hr)) {
			mjlog("N_MMFPlayer.OnTopologyReady: failed to get the video renderer service (VideoDisplayControl)");
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.OnTopologyReady: successfully got the video renderer service (VideoDisplayControl)");
			}
			// load the first video frame? 
			// the rendering prefs used here require Win 7 or >, comment out for Vista
			m_pVideoDisplay->SetRenderingPrefs(MFVideoRenderPrefs_DoNotRepaintOnStop |
				MFVideoRenderPrefs_AllowBatching | MFVideoRenderPrefs_AllowOutputThrottling);
			m_pVideoDisplay->SetAspectRatioMode(MFVideoARMode_None);
			if (tempW != 0 && tempH != 0) {
				setVideoDestinationPos(0, 0, tempW, tempH);
			}
		}
		// The audio volume interface.
		// This may fail, e.g.when there is no audio.
		/*
		HRESULT hhrr = MFGetService(m_pSession, MR_POLICY_VOLUME_SERVICE, 
			__uuidof(IMFSimpleAudioVolume), (void**)&m_pVolume);
		if (FAILED(hhrr)) {
			printf("N_MMFPlayer Warning: OnTopologyReady: Unable to get the volume control, maybe there is no audio: %d.\n", hhrr);
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				printf("N_MMFPlayer Info: OnTopologyReady: Successfully got the volume control.\n");
			}
		}
		*/
		// audio stream and channel level control
		
		HRESULT hhrr = MFGetService(m_pSession, MR_STREAM_VOLUME_SERVICE,
			__uuidof(IMFAudioStreamVolume), (void**)&m_pStreamVolume);
				if (FAILED(hhrr)) {
			mjlogf("N_MMFPlayer.OnTopologyReady: unable to get the audio stream volume control, maybe there is no audio (%d, 0x%X)", hhrr, hhrr);
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.OnTopologyReady: successfully got the audio stream volume control");
			}
		}
		
		// get clock and rate control
		IMFClock *pClock = NULL;
		// Get the presentation clock (optional)
		hhrr = m_pSession->GetClock(&pClock);
		if (SUCCEEDED(hhrr)) {
			hr = pClock->QueryInterface(__uuidof(IMFPresentationClock), (void**)&m_pClock);
			// get a Timer objects
			if (SUCCEEDED(hr)) {
				if (MMFUtil::JMMF_DEBUG) {
					mjlog("N_MMFPlayer.OnTopologyReady: successfully got the Clock interface");
				}
				if (!synchronousMode) {
					hr = m_pClock->QueryInterface(__uuidof(IMFTimer) ,(void**) &m_pStopTimer);

					if (FAILED(hr)) {
						mjlogf("N_MMFPlayer.OnTopologyReady: failed to get a Timer instance for stop time notifications (%d, 0x%X)", hr, hr);
					} else {
						if (MMFUtil::JMMF_DEBUG) {
							mjlog("N_MMFPlayer.OnTopologyReady: successfully got a Timer instance for stop time notifications");
						}
					}
					hr = m_pClock->QueryInterface(__uuidof(IMFTimer) ,(void**) &m_pEndTimer);

					if (FAILED(hr)) {
						mjlogf("N_MMFPlayer.OnTopologyReady: failed to get a Timer instance for end of media notifications (%d, 0x%X)", hr, hr);
					} else {
						if (MMFUtil::JMMF_DEBUG) {
							mjlog("N_MMFPlayer.OnTopologyReady: successfully got a Timer instance for end of media notifications");
						}
					}
				}
			}
		} else {
			mjlogf("N_MMFPlayer.OnTopologyReady: failed to get a Clock interface (%d, 0x%X)", hhrr, hhrr);
			hr = hhrr;
		}

		// Get the rate control interface (optional)
		hhrr = MFGetService(m_pSession, MF_RATE_CONTROL_SERVICE, __uuidof(IMFRateControl), (void**)&m_pRate);
		if (FAILED(hhrr)) {
			mjlogf("N_MMFPlayer.OnTopologyReady: failed to get the Rate Control (%d, 0x%X)", hhrr, hhrr);
			//hr = hhrr; //non crucial control, don't fail
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.OnTopologyReady: successfully got the Rate Control");
			}
			// try to get a rate support object as well
			hhrr = MFGetService(m_pSession, MF_RATE_CONTROL_SERVICE, __uuidof(IMFRateSupport),
				(void**)&m_pRateSupport);
			if (FAILED(hhrr)) {
				mjlogf("N_MMFPlayer.OnTopologyReady: failed to get the Rate Support (%d, 0x%X)", hhrr, hhrr);
			} else {
				mjlog("N_MMFPlayer.OnTopologyReady: successfully got the Rate Control");
			}
		}

	} while (false);
	//m_state = PlayerState_Ready;
	topoInited = true;
	setVolume(initVolume);
	setMediaPosition(initMediaTime);
	
	return hr;
}

// IUnknown methods
HRESULT MMFPlayer::QueryInterface(REFIID riid, void** ppv) {
	if (MMFUtil::JMMF_DEBUG) {
		//mjlog("N_MMFPlayer.QueryInterface: QueryInterface called");
	}
	
	HRESULT hr = S_OK;

    if(ppv == NULL) {
        return E_POINTER;
    }

    if(riid == __uuidof(IMFAsyncCallback)) {
        *ppv = static_cast<IMFAsyncCallback*>(this);
    }
    else if(riid == __uuidof(IUnknown)) {
        *ppv = static_cast<IUnknown*>(this);
    }
    else {
        *ppv = NULL;
        hr = E_NOINTERFACE;
    }

	if(SUCCEEDED(hr)) {
        AddRef();
	}

    return hr;
}

// IUnknown method
ULONG MMFPlayer::AddRef() {
	if (MMFUtil::JMMF_DEBUG) {
		//mjlog("N_MMFPlayer.AddRef: AddRef called");
	}
	
    return InterlockedIncrement(&m_nRefCount);
}

// IUnknown method
ULONG MMFPlayer::Release() {
	if (MMFUtil::JMMF_DEBUG) {
		//mjlog("N_MMFPlayer.Release: Release called");
	}
	
    ULONG uCount = InterlockedDecrement(&m_nRefCount);
    if (uCount == 0) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.Release: count = 0, player can be deleted");
		}
        //delete this;// do not delete
		// deletion is performed by the JNI code
    }
    return uCount;
}

/**
* Tries to close and release all resources, calls MFShutdown and
* CoUninitialize etc.
*/
HRESULT MMFPlayer::cleanUpOnClose() {
	if (cleanUpCalled) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.cleanUpOnClose: clean has already been called");
		}
		return S_OK;
	}
	// stop player, end session, release all objects etc
	cleanUpCalled = true;
	if (m_state == PlayerState_Started || m_state == PlayerState_Paused) {
		stop();
		// should wait for Stopped event? Or skip this and just rely on Session->Close
	}

	if(m_pVideoDisplay != NULL) {
		//m_pVideoDisplay->SetVideoWindow(NULL);
		m_pVideoDisplay->Release();
		m_pVideoDisplay = NULL;
	}
	m_state = PlayerState_Closing;
	pendingAction->action = SetStateClosed;
	HRESULT hr = m_pSession->Close();

	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.cleanUpOnClose: closing the Session failed (%ld, 0x%X)", hr, hr);
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.cleanUpOnClose: closing the Session succeeded");
		}
	}
	// wait for close event or continue from Invoke when the closed event has been received?
	
	DWORD result = WaitForSingleObject(m_closeCompleteEvent, 3000);
	if (result == WAIT_TIMEOUT)	{
		mjlog("N_MMFPlayer.cleanUpOnClose: closing the Session timed out (wait time 3 sec.)");
		return E_UNEXPECTED;
	}
	hr = FinalClosing();
	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.cleanUpOnClose: failed to finalize close operation (%d, 0x%X)", hr, hr);
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.cleanUpOnClose: successfully finalized the close operation");
		}
	}
	
	return hr;
}
/*
* Tries to close the session without waiting for the close event.
*/
HRESULT MMFPlayer::CloseSession() {
	
	if (cleanUpCalled) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.CloseSession: clean has already been called");
		}
		return E_UNEXPECTED;
	}

	if (m_state == PlayerState_Closing || m_state == PlayerState_Closed) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.CloseSession: the Session is already closed or is closing");
		}
		return E_UNEXPECTED;
	}
	
	if (synchronousMode) {
		return CloseSessionSynchronous();
	} else {
		return CloseSessionA();
	}
}

/*
* The asynchronous part of closing the session. 
*/
HRESULT MMFPlayer::CloseSessionA() {
	// stop the stop timer but a callback can still be expected after this
	// not necessary? leads to crashes?
	HRESULT hr = S_OK;
	//if (m_pStopTimer != NULL) {
	//	//hr = m_pStopTimer->CancelTimer(m_pTimerCancelKey);
	//	if (FAILED(hr)) {
	//		printf("N_MMFPlayer Info: CloseSession: Failed to cancel the stop timer.\n");
	//	}
	//}

	// It is assumed the player is stopped or paused beforehand!
	if (m_state == PlayerState_Started || m_state == PlayerState_Paused) {
		stop();
		// should wait for Stopped event? Or skip this and just rely on Session->Close
	}

	if(m_pVideoDisplay != NULL) {
		//m_pVideoDisplay->SetVideoWindow(NULL);
		m_pVideoDisplay->Release();
		m_pVideoDisplay = NULL;
	}
	EnterCriticalSection(&m_criticalSection);
	m_state = PlayerState_Closing;
	pendingAction->action = SetStateClosed;
	hr = m_pSession->Close();

	if (MMFUtil::JMMF_DEBUG) {
		mjlogf("N_MMFPlayer.CloseSessionA: successfully closed the Session (%d, 0x%X)", hr, hr);
	}
	LeaveCriticalSection(&m_criticalSection);

	return hr;
}

/*
* The synchronous part of closing the session.
*/
HRESULT MMFPlayer::CloseSessionSynchronous() {
	HRESULT hr = S_OK;
	// It is assumed the player is stopped or paused beforehand!
	if (m_state == PlayerState_Started || m_state == PlayerState_Paused) {
		stop();
	}

	if(m_pVideoDisplay != NULL) {
		//m_pVideoDisplay->SetVideoWindow(NULL);
		m_pVideoDisplay->Release();
		m_pVideoDisplay = NULL;
	}

	EnterCriticalSection(&m_criticalSection);

	m_state = PlayerState_Closing;
	pendingAction->action = SetStateClosed;
	hr = m_pSession->Close();

	if (SUCCEEDED(hr)) {
		HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionClosed);
		if (SUCCEEDED(ehr)) {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.CloseSessionSynchronous: successfully closed the Session (%d, 0x%X)", ehr, ehr);
			}
		} else {
			mjlogf("N_MMFPlayer.CloseSessionSynchronous: failed to get the Session closed event (%d, 0x%X)", ehr, ehr);
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogf("N_MMFPlayer.CloseSessionSynchronous: failed to close the Session (%d, 0x%X)", hr, hr);
		}
	}

	LeaveCriticalSection(&m_criticalSection);

	return hr;
}
/*
* This is called when the session has been closed (and should not otherwise be called).
* First shutdown and close the source and session and then release other objects.
* Then call MFShutdown? 
*/
HRESULT MMFPlayer::FinalClosing() {
	HRESULT hr = S_OK;
	//m_state = PlayerState_Closing;

	if (m_pSource != NULL) {
		hr = m_pSource->Shutdown();
		if (FAILED(hr) && MMFUtil::JMMF_DEBUG) {
			mjlogf("N_MMFPlayer.FinalClosing: failed to shutdown the Source (%ld, 0x%X)", hr, hr);
		}
	}
	if (m_pSession != NULL) {
		hr = m_pSession->Shutdown();
		if (FAILED(hr) && MMFUtil::JMMF_DEBUG) {
			mjlogf("N_MMFPlayer.FinalClosing: failed to shutdown the Session (%ld, 0x%X)", hr, hr);
		}
	}

	if (m_pSource != NULL) {
		m_pSource->Release();
		m_pSource = NULL;
	}

	if (m_pSession != NULL) {
		m_pSession->Release();
		m_pSession = NULL;
	}

	if (m_pStopTimer != NULL) {
		//m_pStopTimer->CancelTimer(m_pTimerCancelKey);

		m_pStopTimer->Release();
		m_pStopTimer = NULL;
	}
	if (m_pTimerCancelKey != NULL) {
		m_pTimerCancelKey->Release();
		m_pTimerCancelKey = NULL;
	}
	if (pStopState != NULL) {
		delete(pStopState);
		pStopState = NULL;
	}
	if (m_pEndTimer != NULL) {
		m_pEndTimer->Release();
		m_pEndTimer = NULL;
	}
	if (pEndState != NULL) {
		delete(pEndState);
		pEndState = NULL;
	}

	// release pointers
	if (m_pTopology != NULL) {
		m_pTopology->Release();
		m_pTopology = NULL;
	}
	if(m_pRate != NULL) {
		m_pRate->Release();
		m_pRate = NULL;
	}
	if (m_pRateSupport != NULL) {
		m_pRateSupport->Release();
		m_pRateSupport = NULL;
	}
	if (m_pClock != NULL) {
		m_pClock->Release();
		m_pClock = NULL;
	}
	//if (m_pVolume != NULL) {
	//	m_pVolume->Release();
	//	m_pVolume = NULL;
	//}
	if (m_pStreamVolume != NULL) {
		m_pStreamVolume->Release();
		m_pStreamVolume = NULL;
	}
	
	m_hwndVideo = NULL;
	delete (pendingAction);
	delete (cachedAction);
	//hr = MFShutdown();
	//if (FAILED(hr) && MMFUtil::JMMF_DEBUG) {
	//	printf("N_MMFPlayer Error: FinalClosing: Failed to shutdown MF.\n");
	//}

	// temp ??
	//CloseHandle(m_closeCompleteEvent);

//	printf("N_MMFPlayer Info: FinalClosing: Closed the Close Event.\n");
	DeleteCriticalSection(&m_criticalSection);
//	printf("N_MMFPlayer Info: FinalClosing: Deleted the Critical Section State.\n");
	//CoUninitialize();

	return hr;
}


	//HRESULT setVideoWindowPos(long, long, long, long);
HRESULT MMFPlayer::setVideoSourcePos(float x, float y, float w, float h) {
	if (m_pVideoDisplay == NULL) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.setVideoSourcePos: there is no video display");
		}
		return E_POINTER;
	}
	MFVideoNormalizedRect normRect;
	normRect.left = x;
	normRect.top = y;
	normRect.right = w;
	normRect.bottom = h;
	HRESULT hr = S_OK; 
	hr = m_pVideoDisplay->SetVideoPosition(&normRect, NULL);
	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.setVideoSourcePos: failed to set the video source rectangle to %f, %f, %f, %f (%ld, 0x%X)", x, y, w, h, hr, hr);
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogf("N_MMFPlayer.setVideoSourcePos: successfully set the video source rectangle to %f, %f, %f, %f", x, y, w, h);
		}
	}
	return hr;
}

HRESULT MMFPlayer::setVideoDestinationPos(long x, long y, long width, long height) {
	if (m_pVideoDisplay == NULL) {
		tempW = width; // cache
		tempH = height;
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.setVideoDestinationPos: there is no video display");
		}
		return E_POINTER;
	}
	
	RECT rect;
	rect.left = x;
	rect.top = y;
	rect.right = width;
	rect.bottom = height;
	//HRESULT hr = S_OK;
	HRESULT hr = m_pVideoDisplay->SetVideoPosition(NULL, (LPRECT) &rect);

	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.setVideoDestinationPos: failed to set the video destination rectangle to %ld, %ld, %ld, %ld (%ld, 0x%X)", x, y, width, height, hr, hr);
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogf("N_MMFPlayer.setVideoDestinationPos: successfully set the video destination rectangle to %ld, %ld, %ld, %ld", x, y, width, height);
		}
	}
	return hr;
}

HRESULT MMFPlayer::setVideoSourceAndDestPos(float sx, float sy, float sw, float sh, long x, long y, long width, long height) {
	if (m_pVideoDisplay == NULL) {
		tempW = width; // cache
		tempH = height;
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.setVideoSourceAndDestPos: there is no video display");
		}
		return E_POINTER;
	}
	HRESULT hr = S_OK;
	// source normalized rect
	MFVideoNormalizedRect normRect;
	normRect.left = sx;
	normRect.top = sy;
	normRect.right = sw;
	normRect.bottom = sh;
	// destination rect
	RECT rect;
	rect.left = x;
	rect.top = y;
	rect.right = width;
	rect.bottom = height;

	hr = m_pVideoDisplay->SetVideoPosition(&normRect, (LPRECT) &rect);

	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.setVideoSourceAndDestPos: failed to set the source and destination rectangle to: %f, %f, %f, %f, %ld, %ld, %ld, %ld (%ld, 0x%X)", sx, sy, sw, sh, x, y, width, height, hr, hr);
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogf("N_MMFPlayer.setVideoSourceAndDestPos: successfully set the source and destination rectangle to: %f, %f, %f, %f, %ld, %ld, %ld, %ld", sx, sy, sw, sh, x, y, width, height);
		}
	}
	return hr;
}

	//HRESULT setVisible(long);
	//int getState(void);
/*
* Returns true if the player state = started.
*/
bool MMFPlayer::isPlaying() {
	return (m_state == PlayerState_Started || pendingAction->action == SetStateStarted);
}

/*
* Delegates to either the synchronous or the asynchronous version of setRate(). 
*/
void MMFPlayer::setRate(double rate) {
	if (synchronousMode) {
		setRateSynchronous(rate);
	} else {
		setRateA(rate);
	}
}

/*
* The default, asynchronous, implementation of setting the play back rate.
* Sets the rate of the player. Checks whether the rate is supported
* and tries "thinning" if that is the only way to achieve the requested rate.
* The player should be paused before changing the rate. 
*/
void MMFPlayer::setRateA(double rate) {
	if (m_pRate == NULL) {
		if (topoInited) {
			mjlog("N_MMFPlayer.setRateA: unable to set the playback rate; the Rate Control is null");
		}
		return;
	}
	EnterCriticalSection(&m_criticalSection);

	BOOL thin = FALSE;
	float actRate = 0.0F;
	HRESULT grhr = m_pRate->GetRate(&thin, &actRate);
	if (grhr == S_OK && actRate == rate) {// nothing changes. Check for pending changes?
		if (MMFUtil::JMMF_DEBUG) {
			mjlogf("N_MMFPlayer.setRateA: the playback rate is already as requested %f", rate);
		}
		LeaveCriticalSection(&m_criticalSection);
		return;
	}

	if (pendingAction->action != DoNothing) {
		// don't overwrite seek actions
		if (cachedAction->action == DoNothing || cachedAction->action == SetRate) {
			cachedAction->action = SetRate;
			cachedAction->rateValue = (float) rate;
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.setRateA: setting the cached action rate to %f", rate);
			}
		}
		else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.setRateA: the cached action prevented setting the rate to %f", rate);
			}
		}
		//LeaveCriticalSection(&m_criticalSection);
		//return;
	}

	actRate = (float) rate;
	float nearRate = 0.0F;
	HRESULT hr;

	if (m_pRateSupport != NULL) {
		hr = m_pRateSupport->IsRateSupported(FALSE, actRate, &nearRate);
		if (FAILED(hr)) {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.setRateA: rate not supported without thinning, nearest rate %f", nearRate);
			}
			hr = m_pRateSupport->IsRateSupported(TRUE, actRate, &nearRate);
			if (FAILED(hr)) {
				if (MMFUtil::JMMF_DEBUG) {
					mjlogf("N_MMFPlayer.setRateA: rate not supported with thinning, nearest rate %f", nearRate);
				}
				//rate not supported, try nearest
				actRate = nearRate;
			}
			thin = TRUE;
		}
	}
	if (MMFUtil::JMMF_DEBUG) {
		mjlogf("N_MMFPlayer.setRateA: thinning of stream %i, setting actual rate to %f", thin, actRate);
	}
	
	// before setting the rate check the player state
	if (m_state != PlayerState_Paused && m_state != PlayerState_Stopped && m_state != PlayerState_Ready) {
		m_pSession->Pause();
	}
	// test without thinning?
	//hr = m_pRate->SetRate(thin, actRate);
	hr = m_pRate->SetRate(FALSE, actRate);
	thinEnabled = thin;
	
	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.setRateA: failed to set the playback rate to %f", actRate);
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogf("N_MMFPlayer.setRateA: succesfully set the playback rate to %f", actRate);
		}
		userPlaybackRate = actRate;
	}

	pendingAction->action = SetRate;
	pendingAction->rateValue = actRate;
	// check in ProcessMediaEvent if the rate change succeeded
	LeaveCriticalSection(&m_criticalSection);
}

/*
* The synchronous variant of setting the play back rate.
*/
void MMFPlayer::setRateSynchronous(double rate) {
	if (m_pRate == NULL) {
		if (topoInited) {
			mjlog("N_MMFPlayer.setRateSynchronous: unable to set the playback rate; the Rate Control is null");
		}
		return;
	}
	//EnterCriticalSection(&m_criticalSection);// not needed in synchronous mode? Leads to a dead lock in synchronous mode in the context of ELAN.

	BOOL thin = FALSE;
	float actRate = 0.0F;
	HRESULT grhr = m_pRate->GetRate(&thin, &actRate);
	if (grhr == S_OK && actRate == rate) {// nothing changes.
		if (MMFUtil::JMMF_DEBUG) {
			mjlogf("N_MMFPlayer.setRateSynchronous: the playback rate is already as requested %f", rate);
		}
		//LeaveCriticalSection(&m_criticalSection);
		return;
	}

	actRate = (float) rate;
	float nearRate = 0.0F;
	HRESULT hr;

	if (m_pRateSupport != NULL) {
		hr = m_pRateSupport->IsRateSupported(FALSE, actRate, &nearRate);
		if (FAILED(hr)) {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.setRateSynchronous: the playback rate is not supported without thinning, nearest rate %f", nearRate);
			}
			hr = m_pRateSupport->IsRateSupported(TRUE, actRate, &nearRate);
			if (FAILED(hr)) {
				if (MMFUtil::JMMF_DEBUG) {
					mjlogf("N_MMFPlayer.setRateSynchronous: the playback rate is not supported with thinning, nearest rate %f", nearRate);
				}
				//rate not supported, try nearest
				actRate = nearRate;
			}
			thin = TRUE;
		}
	}
	if (MMFUtil::JMMF_DEBUG) {
		mjlogf("N_MMFPlayer.setRateSynchronous: thinning of stream %i, setting actual rate to %f", thin, actRate);
	}

	// before setting the rate check the player state
	if (m_state != PlayerState_Paused && m_state != PlayerState_Stopped && m_state != PlayerState_Ready) {
		hr = m_pSession->Pause();
		
		if (FAILED(hr)) {
			mjlogf("N_MMFPlayer.setRateSynchronous: failed to pause the playback for changing the rate (%ld, 0x%X)", hr, hr);
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.setRateSynchronous: paused the playback for changing the rate ");
			}
			
			HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionPaused);
			
			if (SUCCEEDED(ehr)) {
				mjlog("N_MMFPlayer.setRateSynchronous: received session paused event for changing the rate ");
			} else {
				mjlogf("N_MMFPlayer.setRateSynchronous: did not receive the session paused event for changing the rate (%ld, 0x%X)", ehr, ehr);
			}
		}
	}
	// test without thinning?
	//hr = m_pRate->SetRate(thin, actRate);
	hr = m_pRate->SetRate(FALSE, actRate);
	thinEnabled = thin;
	
	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.setRateSynchronous: failed to set the playback rate to %f (%ld, 0x%X)", actRate, hr, hr);
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogf("N_MMFPlayer.setRateSynchronous: successfully set the playback rate to %f", actRate);
		}
		userPlaybackRate = actRate;

		HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionRateChanged);

		if (SUCCEEDED(ehr)) {
			// what to do if an error is returned
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.setRateSynchronous: received the session rate changed event, the playback rate is set to %f", actRate);
			}
		} else {
			mjlogf("N_MMFPlayer.setRateSynchronous: did not receive the session rate changed event (%ld, 0x%X)", ehr, ehr);
		}
	}

	//LeaveCriticalSection(&m_criticalSection);
}

void MMFPlayer::setRateCached(float rate) {
	if (m_pRate == NULL) {
		mjlog("N_MMFPlayer.setRateCached: unable to cache the playback rate; the Rate Control is null");
		return;
	}
	if (MMFUtil::JMMF_DEBUG) {
		mjlogf("N_MMFPlayer.setRateCached: setting the cached playback rate to %f", rate);
	}
	HRESULT hr;
	float actRate = (float) rate;
	float nearRate = 0.0F;

	if (m_pRateSupport != NULL) {
		hr = m_pRateSupport->IsRateSupported(FALSE, actRate, &nearRate);
		if (FAILED(hr)) {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.setRateCached: playback rate not supported without thinning, nearest rate %f", nearRate);
			}
			hr = m_pRateSupport->IsRateSupported(TRUE, actRate, &nearRate);
			if (FAILED(hr)) {
				if (MMFUtil::JMMF_DEBUG) {
					mjlogf("N_MMFPlayer.setRateCached: playback rate not supported with thinning, nearest rate %f", nearRate);
				}
				//rate not supported, try nearest
				actRate = nearRate;
			}
		}
	}

	hr = m_pRate->SetRate(FALSE, actRate);
	
	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.setRateCached: failed to set the playback rate to %f (%ld, 0x%X)", rate, hr, hr);
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogf("N_MMFPlayer.setRateCached: successfully set the playback rate to %f", rate);
		}
		userPlaybackRate = actRate;
	}
}

/*
* Returns the rate of the player. The "thinning" flag is ignored. 
*/
double MMFPlayer::getRate() {
	if (m_pRate == NULL) {
		mjlog("N_MMFPlayer.getRate: unable to get the playback rate; the Rate Control is null (returning 1.0)");
		return 1.0;
	}
	EnterCriticalSection(&m_criticalSection);//?? need to lock for getting the rate?
	HRESULT hr;
	//BOOL pfThin;// the thinning flag is ignored in this call
	float pflRate = 0.0F;
	hr = m_pRate->GetRate(NULL, &pflRate);
	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.getRate: failed to get the playback rate (%ld, 0x%X)", hr, hr);
		pflRate = 1.0;
		//return 1.0;
	}
	else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogf("N_MMFPlayer.getRate: successfully got the playback rate %f", pflRate);
		}
	}
	LeaveCriticalSection(&m_criticalSection);
	return pflRate;
}

/*
* Sets the volume, a value between 0.0 and 1.0
*/
void MMFPlayer::setVolume(float volume) {
	/*
	if (m_pVolume == NULL) {
		if (topoInited) {
			printf("N_MMFPlayer Error: setVolume: Unable to set the volume, no volume control.\n");
		}
		// cache the value?
		initVolume = volume;
		return;
	}
	HRESULT hr = m_pVolume->SetMasterVolume(volume);
	*/
	if (m_pStreamVolume == NULL) {
		if (topoInited) {
			mjlog("N_MMFPlayer.setVolume: unable to set the audio stream volume, no audio stream volume control");
		}
		// cache the value?
		initVolume = volume;
		return;
	}

	UINT32 chCount;
	HRESULT hr = m_pStreamVolume->GetChannelCount(&chCount);
	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.setVolume: failed to get the number of audio channels (%ld, 0x%X)", hr, hr);
	} else {
		float *vols = new float[chCount];
		for (UINT32 i = 0; i < chCount; i++) {
			vols[i] = volume;
		}
		hr = m_pStreamVolume->SetAllVolumes(chCount, vols);
		delete[] vols;
	}
	
	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.setVolume: failed to set the volume to %f (%ld, 0x%X)", volume, hr, hr);
	}
	else {
		if (MMFUtil::JMMF_DEBUG) {
			mjlogf("N_MMFPlayer.setVolume: successfully set the volume to %f", volume);
		}
	}
}


float MMFPlayer::getVolume() {
	/*
	if (m_pVolume == NULL) {
		if (topoInited) {
			printf("N_MMFPlayer Error: getVolume: Unable to get the volume, no volume control.\n");
		}
		return 1;
	}
	float volume = 1;
	HRESULT hr = m_pVolume->GetMasterVolume(&volume);
	if (FAILED(hr)) {
		printf("N_MMFPlayer Error: getVolume: Failed to get the volume level.\n");
	}
	return volume;
	*/
	if (m_pStreamVolume == NULL) {
		if (topoInited) {
			mjlog("N_MMFPlayer.getVolume: unable to get the audio stream volume, no audio stream volume control");
		}
		return 1;
	}
	
	UINT32 chCount;
	HRESULT hr = m_pStreamVolume->GetChannelCount(&chCount);

	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.getVolume: failed to get the number of audio channels (%ld, 0x%X)", hr, hr);
		return 1;
	}

	float *vols = new float[chCount];
	// initialize the floats
	for (UINT32 i = 0; i < chCount; i++) {
		vols[i] = 1;
	}
	hr = m_pStreamVolume->GetAllVolumes(chCount, vols);

	if (FAILED(hr)) {
		mjlogf("N_MMFPlayer.getVolume: failed to get the audio stream volume levels (%ld, 0x%X)", hr, hr);
		return 1;
	}
	// currently return the first channels level
	float chzero = vols[0];
	delete[] vols;
	if (MMFUtil::JMMF_DEBUG) {
		mjlogf("N_MMFPlayer.getVolume: successfully got the audio stream volume levels, returning %f", chzero);
	}
	return chzero;
}	
	//void setBalance(long);
	//long getBalance(void);

/*
* Delegates to either the synchronous or the asynchronous version of setMediaPosition()
*/
void MMFPlayer::setMediaPosition(__int64 medPosition) {
	if (synchronousMode) {
		setMediaPositionSynchronous(medPosition);
	} else {
		setMediaPositionA(medPosition);
	}
}

/*
* The default, asynchronous, implementation of set media position.
* Sets the position of the media playhead.
* In Media Foundation the only way to do this seems via the Start function.
* Therefore if the player is paused, the rate should be set to 0 in order
* not to start the player.
*/
void MMFPlayer::setMediaPositionA(__int64 medPosition) {
	if (m_pSession == NULL || !topoInited) {
		if (topoInited) {
			mjlog("N_MMFPlayer.setMediaPositionA: unable to set media position, no media session");
		}
		initMediaTime = medPosition;
		return;
	}
	if (MMFUtil::JMMF_DEBUG) {
		mjlogf("N_MMFPlayer.setMediaPositionA: request to set the media position to %lld", medPosition);
	}
	if (medPosition < 0) {
		mjlogf("N_MMFPlayer.setMediaPositionA: unable to set the media position, position < 0 (%lld)", medPosition);
		return;
	} else if (medPosition > duration) {
		mjlogf("N_MMFPlayer.setMediaPositionA: unable to set the media position, position > duration (%lld, %lld)", medPosition, duration);
		return;
	}

	// critical section
	EnterCriticalSection(&m_criticalSection);
	// what to do if the player is already seeking? return, ignore this request or have a queue of set position requests?
	if (pendingAction->action == SetMediaPosition) {
		cachedAction->action = SetMediaPosition;
		cachedAction->timeValue = medPosition;
		if (MMFUtil::JMMF_DEBUG) {
			mjlogf("N_MMFPlayer.setMediaPositionA: caching the request to set the media position to %lld", medPosition);
		}
		LeaveCriticalSection(&m_criticalSection);	
		return;
	}

	const MFTIME plPosTime(medPosition);
	PROPVARIANT varStart;
	PropVariantInit(&varStart);
    varStart.vt = VT_I8;
    varStart.hVal.QuadPart = plPosTime;

	HRESULT hr;
	if (m_state == PlayerState_Started) {
		m_state = PlayerState_SeekingPosition;
		pendingAction->action = SetMediaPosition;
		pendingAction->timeValue = medPosition;

		//hr = m_pSession->Start(NULL, &varStart);
		// first pause
		hr = m_pSession->Pause();
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.setMediaPositionA: successfully paused a started player before setting the position");
		}
		if (FAILED(hr)) {
			mjlogf("N_MMFPlayer.setMediaPositionA: failed to pause the started player before setting the position (%ld, 0x%X)", hr, hr);
		}
		if (m_pRate != NULL) {
			// setRate
			hr = m_pRate->SetRate(FALSE, 0);// zero means scrubbing, single frame
			if (SUCCEEDED(hr)) {
				// scrub
				hr = m_pSession->Start(NULL, &varStart);
			}
		}
	} else {
		if (m_pRate != NULL) {
			// check rate
			float curRate = 0.0F;
			//BOOL thinned;
			hr = m_pRate->GetRate(FALSE, &curRate);
			// check result
			if (curRate != 0) {
				hr = m_pRate->SetRate(FALSE, 0);// zero means scrubbing, single frame
				if (FAILED(hr)) {
					mjlogf("N_MMFPlayer.setMediaPositionA: failed to set the rate to 0 (scrubbing) (%ld, 0x%X)", hr, hr);
				}
				m_state = PlayerState_SeekingPosition;
				pendingAction->action = SetMediaPosition;
				pendingAction->timeValue = medPosition;
				hr = m_pSession->Start(NULL, &varStart);
				if (FAILED(hr) && MMFUtil::JMMF_DEBUG) {
					mjlogf("N_MMFPlayer.setMediaPositionA: failed to set the media position of a possibly paused player (%ld, 0x%X)", hr, hr);
				}
		
			} else {// rate == 0
				m_state = PlayerState_SeekingPosition;
				pendingAction->action = SetMediaPosition;
				pendingAction->timeValue = medPosition;
				hr = m_pSession->Start(NULL, &varStart);
				if (FAILED(hr) && MMFUtil::JMMF_DEBUG) {
					mjlogf("N_MMFPlayer.setMediaPositionA: failed to set the media position of a paused player (%ld, 0x%X)", hr, hr);
				}
			}
		} else {// what else? just set time?
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.setMediaPositionA: setting the media position of a paused player without rate control");
			}
			m_state = PlayerState_SeekingPosition;
			pendingAction->action = SetMediaPosition;
			pendingAction->timeValue = medPosition;
			hr = m_pSession->Start(NULL, &varStart);
			if (FAILED(hr) && MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.setMediaPositionA: failed to set the media position of a paused player without rate control (%ld, 0x%X)", hr, hr);
			}
		}
	}
	PropVariantClear(&varStart);
	// leave critical section
	LeaveCriticalSection(&m_criticalSection);
}

/*
* The synchronous variant of set media position.
*/
void MMFPlayer::setMediaPositionSynchronous(__int64 medPosition) {
	if (m_pSession == NULL || !topoInited) {
		if (topoInited) {
			mjlog("N_MMFPlayer.setMediaPositionSynchronous: unable to set media position, no media session");
		}
		initMediaTime = medPosition;
		return;
	}
	if (MMFUtil::JMMF_DEBUG) {
		mjlogf("N_MMFPlayer.setMediaPositionSynchronous: request to set the media position to %lld", medPosition);
	}
	if (medPosition < 0) {
		mjlogf("N_MMFPlayer.setMediaPositionSynchronous: unable to set the media position, position < 0 (%lld)", medPosition);
		return;
	} else if (medPosition > duration) {
		mjlogf("N_MMFPlayer.setMediaPositionSynchronous: unable to set the media position, position > duration (%lld, %lld)", medPosition, duration);
		return;
	}
	// critical section
	EnterCriticalSection(&m_criticalSection);

	const MFTIME plPosTime(medPosition);
	PROPVARIANT varStart;
	PropVariantInit(&varStart);
    varStart.vt = VT_I8;
    varStart.hVal.QuadPart = plPosTime;

	HRESULT hr;
	if (m_state == PlayerState_Started) {
		//m_state = PlayerState_SeekingPosition;
		// first pause

		hr = m_pSession->Pause();
		
		if (SUCCEEDED(hr)) {
			HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionPaused);
			// ignore the return value?
			if (SUCCEEDED(ehr)) {
				// ignore the return value?
				if (MMFUtil::JMMF_DEBUG) {
					mjlog("N_MMFPlayer.setMediaPositionSynchronous: received the session paused event before setting the media position");
				}
			} else {
				mjlogf("N_MMFPlayer.setMediaPositionSynchronous: failed to receive the session paused event before setting the media position (%ld, 0x%X)", ehr, ehr);
			}

		} else {
			mjlogf("N_MMFPlayer.setMediaPositionSynchronous: failed to pause the started player (%ld, 0x%X)", hr, hr);

		}
	} 
	
	if (m_pRate != NULL) {
		// check rate
		float curRate = 0.0F;
		//BOOL thinned;
		hr = m_pRate->GetRate(FALSE, &curRate);
		// check result
		if (curRate != 0) {
			// setRate
			hr = m_pRate->SetRate(FALSE, 0);// zero means scrubbing, single frame

			if (SUCCEEDED(hr)) {
				HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionRateChanged);
				if (SUCCEEDED(ehr)) {
					// ignore the return value?
					if (MMFUtil::JMMF_DEBUG) {
						mjlog("N_MMFPlayer.setMediaPositionSynchronous: received the session rate changed event, rate set to 0");
					}
				} else {
					mjlogf("N_MMFPlayer.setMediaPositionSynchronous: failed to receive the session rate changed event (%ld, 0x%X)", ehr, ehr);
				}
			}
		}
	}

	// scrub sample
	hr = m_pSession->Start(NULL, &varStart);

	if (SUCCEEDED(hr)) {
		HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionScrubSampleComplete);
		if (SUCCEEDED(ehr)) {
			// ignore the return value?
			if (MMFUtil::JMMF_DEBUG) {
				mjlog("N_MMFPlayer.setMediaPositionSynchronous: received the session scrub sample complete event");
			}
		} else {
			mjlogf("N_MMFPlayer.setMediaPositionSynchronous: failed to receive the session scrub sample complete event (%ld, 0x%X)", ehr, ehr);
		}
	}

	PropVariantClear(&varStart);
	// leave critical section
	LeaveCriticalSection(&m_criticalSection);
}

/*
* Sets the cached media position without checking the rate and paused state etc.
*/
void MMFPlayer::setMediaPositionCached(__int64 medPosition) {
	if (m_pSession == NULL) {
		mjlog("N_MMFPlayer.setMediaPositionCached: unable to cache media position, no media session");
		return;
	}
	if (MMFUtil::JMMF_DEBUG) {
		mjlogf("N_MMFPlayer.setMediaPositionCached: setting the cached media position of a paused player %lld", medPosition);
	}

	const MFTIME plPosTime(medPosition);
	PROPVARIANT varStart;
	PropVariantInit(&varStart);
    varStart.vt = VT_I8;
    varStart.hVal.QuadPart = plPosTime;
	m_state = PlayerState_SeekingPosition;

	HRESULT hr = m_pSession->Start(NULL, &varStart);
	if (FAILED(hr) && MMFUtil::JMMF_DEBUG) {
		mjlogf("N_MMFPlayer.setMediaPositionCached: failed to set the media position of a paused player (%ld, 0x%X)", hr, hr);
	}

	pendingAction->action = SetMediaPosition;
	pendingAction->timeValue = medPosition;
	PropVariantClear(&varStart);
}

/*
* Returns the current media position (if there is a presentation clock).
*/
__int64 MMFPlayer::getMediaPosition() {
	if (m_pClock != NULL) {
		//EnterCriticalSection(&m_criticalSection);// don't need the lock to get the time?

		MFTIME curTime;
		HRESULT hr = m_pClock->GetTime(&curTime);
		if (SUCCEEDED(hr)) {
			//LeaveCriticalSection(&m_criticalSection);
			//if (MMFUtil::JMMF_DEBUG) {
			//	printf("N_MMFPlayer Info: getMediaPosition: Successfully got the media time.\n");
			//}
			return curTime;
		} else {
			if (MMFUtil::JMMF_DEBUG && topoInited) {
				mjlogf("N_MMFPlayer.getMediaPosition: failed to get the media time (%ld, 0x%X)", hr, hr);
			}
		}

		//LeaveCriticalSection(&m_criticalSection);
	} else {
		if (MMFUtil::JMMF_DEBUG && topoInited) {
			mjlogf("N_MMFPlayer.getMediaPosition: unable to get the media time, no Clock");
		}
	}

	return 0;
}

/**
* Returns the duration.
* ToDo: check if the duration is always reliable/available before the topology is fully resolved.
*/
__int64 MMFPlayer::getDuration() {
	//if (duration == 0) {
		// get the duration after initialization
	//}
	return duration;
}

/*
* Sets the current stop time. 
* Implemented by setting a timer that calls invoke on a callback object at the specified time.
* Updating the timer has to be done AFTER starting the player.
*
* Setting an attribute on each output topology node is not suited:  
* - it can be done before the media session first starts
* - it can be done after the session started, but with a limit of roughly 7 minutes.
*/
HRESULT MMFPlayer::setStopPosition(__int64 stopPosition) {
	if (stopPosition <= duration) {
		stopTime = stopPosition;
	} else {
		stopTime = duration;
	}
	if (MMFUtil::JMMF_DEBUG) {
		mjlogf("N_MMFPlayer.setStopPosition: setting stoptime to %lld (requested %lld)", stopTime, stopPosition);
	}
	// check the player state?
	HRESULT hr = S_OK;
	if (!synchronousMode) {
		if (m_pStopTimer != NULL && m_pTimerCancelKey != NULL) {
			hr = m_pStopTimer->CancelTimer(m_pTimerCancelKey);

			if (FAILED(hr)) {
				mjlogf("N_MMFPlayer.setStopPosition: failed to cancel the timer (%ld, 0x%X)", hr, hr);
			} else {
				if (MMFUtil::JMMF_DEBUG) {
					mjlogf("N_MMFPlayer.setStopPosition: successfully canceled the timer");
				}
			}
		}
	}
	//if (m_pStopTimer != NULL) {
	//	hr = m_pStopTimer->SetTimer(0, stopTime, this, NULL, &m_pTimerCancelKey);
	//	if (FAILED(hr)) {
	//		printf("N_MMFPlayer Error: setStopTime: failed to set the stop time for the timer.\n");
	//	} else {
	//		printf("N_MMFPlayer Info: setStopTime: successfully set the stop time for the timer %d.\n", stopTime);
	//	}
	//}
	return hr;
}

/*
* Returns the current stop time.
*/
__int64 MMFPlayer::getStopPosition() {
	return stopTime;
}

/*
* Returns the time per frame (in case of video) in seconds.
*/
double MMFPlayer::getTimePerFrame() {
	if (isVideo) {
		if (frameRateNumerator != 0 && frameRateDenominator != 0) {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.getTimePerFrame: time per frame is %f (%u / %u)", (frameRateDenominator / (double)frameRateNumerator), frameRateDenominator, frameRateNumerator);
			}
			return frameRateDenominator / (double) frameRateNumerator;
		} else {
			mjlogf("N_MMFPlayer.getTimePerFrame: frame rate numerator (%u) or denominator (%u) is 0", frameRateNumerator, frameRateDenominator);
		}
	}
	return 0.0;
}

long MMFPlayer::getOrgVideoWidth() {
	if (m_pVideoDisplay != NULL) {
		SIZE origSize = {0, 0};
		SIZE origAR = {0, 0};
		HRESULT hr = m_pVideoDisplay->GetNativeVideoSize(&origSize, &origAR);
		if (SUCCEEDED(hr)) {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.getOrgVideoWidth: original w * h and ar_x * ar_y: %d * %d, %d * %d", origSize.cx, origSize.cy, origAR.cx, origAR.cy);
			}
			return origSize.cx;
		} else {
			mjlogf("N_MMFPlayer.getOrgVideoWidth: failed to get the original video width (%ld, 0x%X)", hr, hr);
		}
	} else {
		if (topoInited) {
			mjlog("N_MMFPlayer.getOrgVideoWidth: unable to get the original video width, no Video Display Control");
		}
	}
	return 0;
}

long MMFPlayer::getOrgVideoHeight() {
	if (m_pVideoDisplay != NULL) {
		SIZE origSize = {0, 0};
		SIZE origAR = {0, 0};
		HRESULT hr = m_pVideoDisplay->GetNativeVideoSize(&origSize, &origAR);
		if (SUCCEEDED(hr)) {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.getOrgVideoHeight: original w * h and ar_x * ar_y: %d * %d, %d * %d", origSize.cx, origSize.cy, origAR.cx, origAR.cy);
			}
			return origSize.cy;
		} else {
			mjlogf("N_MMFPlayer.getOrgVideoHeight: failed to get the original video height (%ld, 0x%X)", hr, hr);
		}
	} else {
		if (topoInited) {
			mjlog("N_MMFPlayer.getOrgVideoHeight: unable to get the original video height, no Video Display Control");
		}
	}
	return 0;
}

/*
* Returns the position of the video destination rect relative to the clipping window.
* x and y can be negative.
*/
HRESULT MMFPlayer::getVideoDestinationPos(long *x, long *y, long *w, long *h) {
	HRESULT hr = S_OK;
	if (m_pVideoDisplay != NULL) {
		MFVideoNormalizedRect normRect;
		RECT rect;
		hr = m_pVideoDisplay->GetVideoPosition(&normRect, (LPRECT) &rect);
		if (SUCCEEDED(hr)) {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.getVideoDestinationPos: video destination position is: %d, %d, %d, %d", rect.left, rect.top, rect.right, rect.bottom);
			}
			*x = rect.left;
			*y = rect.top;
			*w = rect.right;
			*h = rect.bottom;
		} else {
			mjlogf("N_MMFPlayer.getVideoDestinationPos: failed to get the video destination position (%ld, 0x%X)", hr, hr);
		}
	}  else {
		mjlog("N_MMFPlayer.getVideoDestinationPos: unable to get the video destination position, no Video Display Control");
	}
	return hr;
}

HRESULT MMFPlayer::getPreferredAspectRatio(long *width, long *height) {
	if (m_pVideoDisplay != NULL) {
		SIZE origSize = {0, 0};
		SIZE origAR = {0, 0};
		HRESULT hr = m_pVideoDisplay->GetNativeVideoSize(&origSize, &origAR);
		if (SUCCEEDED(hr)) {
			if (MMFUtil::JMMF_DEBUG) {
				mjlogf("N_MMFPlayer.getPreferredAspectRatio: original w * h and ar_x * ar_y: %d * %d, %d * %d", origSize.cx, origSize.cy, origAR.cx, origAR.cy);
			}
			*width = origAR.cx;
			*height = origAR.cy;
		} else {
			mjlogf("N_MMFPlayer.getPreferredAspectRatio: failed to get the preferred aspect ratio (%ld, 0x%X)", hr, hr);
		}
		return hr;
	} else {
		if (topoInited) {
			mjlog("N_MMFPlayer.getPreferredAspectRatio: unable to get the preferred aspect ratio, no Video Display Control");
		}
	}
	return E_POINTER;
}

/*
* Returns the current image.
*/
HRESULT MMFPlayer::getCurrentImage(BITMAPINFOHEADER *pBih, BYTE **pDib, DWORD *pcbDib) {
	if (m_pVideoDisplay == NULL) {
		mjlog("N_MMFPlayer.getCurrentImage: unable to get the image, no Video Display Control");
		return E_POINTER;
	}
	__int64 curPos = getMediaPosition();
	
	HRESULT hr = m_pVideoDisplay->GetCurrentImage(pBih, pDib, pcbDib, &curPos);

	if(SUCCEEDED(hr)) {
		if (MMFUtil::JMMF_DEBUG) {
			mjlog("N_MMFPlayer.getCurrentImage: successfully extracted the current video image");
		}
	} else {
		mjlogf("N_MMFPlayer.getCurrentImage: failed to retrieve the current video image (%ld, 0x%X)", hr, hr);
	}

	return hr;
}

HRESULT MMFPlayer::getOwnerWindow(HWND *hwnd) {
	// just return m_hwndVideo ?? or get it from the video display control
	if (m_hwndVideo == NULL) {
		return E_POINTER;
	} else {
		*hwnd = m_hwndVideo;
		return S_OK;
	}
}

/*
* Tries to repaint the video if there is a video display object.
* Returns a success code even if there is no frame to display yet.
*/
void MMFPlayer::repaintVideo() {
	if (m_pVideoDisplay != NULL) {
		HRESULT hr = m_pVideoDisplay->RepaintVideo();
		if (MMFUtil::JMMF_DEBUG) {
			if (FAILED(hr)) {
				mjlogf("N_MMFPlayer.repaintVideo: failed to repaint the video (%ld, 0x%X)", hr, hr);
			}
		}
		// try to enforce scrubbing by reusing the setMediaPosition functionality
		// alternatively a "scrubCurrentFrame" function could be added
		if (!isPlaying()) {
			int64_t t = getMediaPosition();
			setMediaPosition(t);
		}
	}
}

/*
* Returns true if the mode is synchronous, false otherwise 
*/
bool MMFPlayer::isSynchronousMode() {
	return synchronousMode;
}