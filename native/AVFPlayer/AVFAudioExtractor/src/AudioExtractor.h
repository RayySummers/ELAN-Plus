//
//  AudioExtractor.h
//  AVFAudioExtractor
//  See AudioExtractor.mm for a description.

//  Created by Han Sloetjes on 13/01/2021.
//  Copyright © 2021 mpi. All rights reserved.
//

#ifndef AudioExtractor_h
#define AudioExtractor_h

#import <AVFoundation/AVFoundation.h>

@class AVAssetReader;

@interface AudioExtractor : NSObject {
    @private
        NSDictionary<NSString *, id> *trackOutputSettings;
}

@property (retain, readonly) AVURLAsset    *mediaAsset;
@property (retain, readonly) AVAssetTrack  *audioTrack;
@property (atomic)           NSError       *lastError;
@property (atomic)           NSInteger     javaRef;
// some media and track properties
@property (nonatomic, readonly) BOOL   hasAudio;
@property (nonatomic, readonly) double sampleFreq;// the encoded samples per second
@property (nonatomic, readonly) double sampleDurationMs; // the duration per sample in millisecond
@property (nonatomic, readonly) double sampleDurationSeconds; // the duration per sample in seconds
@property (nonatomic, readonly) double mediaDurationMs; // the duration of the media in millisecond
@property (nonatomic, readonly) double mediaDurationSeconds; // the duration of the media in seconds
@property (nonatomic, readonly) int    numberOfChannels; // number of audio channels
@property (nonatomic, readonly) int    bitsPerSample; // the bits per sample value
// the following buffer related properties are not very meaningful because they are varying
// in each decode and copy action
@property (nonatomic, readonly) int    bytesPerSampleBuffer; // the number of bytes in the buffer after a read action
@property (nonatomic, readonly) double durationSecPerSampleBuffer; // duration in seconds of the data in the buffer after a read action

/*
 * Initializes the reader and all necessary objects for audio extraction.
 */
- (id) initWithURL: (NSURL *) url;

/*
 * Request to set the position or cursor of the reader to the specified
 * position. Ignored by this implementation.
 */
- (BOOL) setPositionSec: (double) time;

/*
 * Retrieves the samples from the specified time interval and copies the
 * decoded byte into the given buffer.
 */
- (int) getSamplesFromTime: (double) fromTime duration: (double) rangeDuration bufferAddress: (char *) destBuffer bufferLength: (size_t) destBufferSize;

/*
 * Checks the audio and video tracks of the asset and stores some information in properties.
 */
//- (void) detectTrackProperties: (AVAssetTrack *) track;

/*
 * Initializes some properties from a sample buffer.
 */
//- (void) detectSampleProperties;

/*
 * Enables or disables debug messages.
 */
+ (void) setDebugMode: (BOOL) mode;

/*
 * Returns whether debug messages are enabled.
 */
+ (BOOL) isDebugMode;

/*!
 @method releaseReader
 @discussion
    Releases all resources.
 */
- (void) releaseReader;

@end

#endif /* AudioExtractor_h */
