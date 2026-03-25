//
//  JAVFConnector.h
//  AVFAudioExtractor
//
//  Created by hasloe on 20/01/2021.
//  Copyright © 2021 MPI. All rights reserved.
//
#include <jni.h>
#include <jni_md.h>
#include "AudioExtractor.h"

#ifndef JAVFConnector_h
#define JAVFConnector_h
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Connects a global Java reference to the corresponding
 * instance of the native audio extractor class. The global
 * reference should prevent garbage collection and deallocation
 * of the counterpart.
 */
typedef struct {
    jobject jRef;
    AudioExtractor *audioExtractor;
} JAVFConnector;

#ifdef __cplusplus
}
#endif

#endif /* JAVFConnector_h */
