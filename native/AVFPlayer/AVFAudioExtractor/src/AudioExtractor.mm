//
//  AudioExtractor.m
//  AVFAudioExtractor
//
//  Created by hasloe on 14/01/2021.
//  Copyright © 2021 MPI. All rights reserved.
//

#import "AudioExtractor.h"
#import <Foundation/Foundation.h>
#import <CoreFoundation/CFDictionary.h>
#import <mpi_jni_util.h>

static BOOL AVFDebug = FALSE;

/*
 * A class that extracts the (first) audio track from a video (or an audio) file
 * and returns decoded audio samples for a specified time interval to the caller.
 * Usually for the purpose of visualization of a waveform or similar. If this
 * works, extracting a wav file from a video is no longer necessary in order to
 * see the waveform.
 *
 */
@implementation AudioExtractor

@synthesize mediaAsset;
@synthesize audioTrack;
@synthesize javaRef;
@synthesize lastError;
@synthesize hasAudio;
@synthesize sampleFreq;
@synthesize sampleDurationMs;
@synthesize sampleDurationSeconds;
@synthesize mediaDurationMs;
@synthesize mediaDurationSeconds;
@synthesize numberOfChannels;
@synthesize bitsPerSample;
@synthesize bytesPerSampleBuffer;
@synthesize durationSecPerSampleBuffer;

/*
 * Initializes a AVURLAsset and creates a AVAssetReader for it if there is
 * at least one audio track. An AVAssetReaderTrackOutput is added to the reader
 * which sets the requested range and reads sample buffers in a loop and copies
 * the bytes to a larger buffer provided by the caller.
 * The output format (in case of audio) has to be Linear PCM, the number of
 * channels and bits-per-sample properties can be specified. An AVAudioPlayer
 * is created for retrieving the number of channels in the audio track (this
 * propery doesn't seem to be available from the AVAssetTrack or from a
 * SampleBuffer (?).
 */
- (id) initWithURL: (NSURL *) url {
    self = [super init];
    // hardcode / initialize the requested bitsPerSample value
    bitsPerSample = 16;
    
    mediaAsset = [AVURLAsset URLAssetWithURL:url options:@{ AVURLAssetPreferPreciseDurationAndTimingKey : @YES }];
    // create a reader as a quick test
    NSError *initError = nil;
    AVAssetReader *assetReader = [AVAssetReader assetReaderWithAsset:mediaAsset error:&initError];
    
    if (initError != nil) {
        mjlogf("AudioExtractor.initWithURL: AssetReader is NULL: %s", [[initError localizedDescription] UTF8String]);
        [self setLastError: initError];
        assetReader = nil;
    } else {
        // use AVAudioPlayer to detect the number of channels, does not work for mpg files
        NSError *auPlayerError;
        AVAudioPlayer *auPlayer = [[AVAudioPlayer alloc] initWithContentsOfURL:url error:&auPlayerError];
        if (auPlayerError == nil) {
            numberOfChannels = (int) [auPlayer numberOfChannels];
            if (AVFDebug) {
                mjlogf("AudioExtractor.initWithURL: created an AVAudioPlayer, #channels: %d", numberOfChannels);
            }
            if (numberOfChannels > 2) {
                numberOfChannels = 2;
            }
            // release player
            auPlayer = nil;
        } else {
            if (AVFDebug) {
                mjlog("AudioExtractor.initWithURL: unable to create an AVAudioPlayer");
            }
            // default
            numberOfChannels = 2;
        }
        
        NSArray<AVAssetTrack *> *audioTracks = [mediaAsset tracksWithMediaType: AVMediaTypeAudio];
        if ([audioTracks count] > 0) {
            if (AVFDebug) {
                mjlogf("AudioExtractor.initWithURL: the file has %lu audio track(s)", [audioTracks count]);
            }
            CMTime cmDuration = [mediaAsset duration];
            mediaDurationSeconds = CMTimeGetSeconds(cmDuration);
            mediaDurationMs = 1000 * mediaDurationSeconds;
            if (AVFDebug) {
                mjlogf("AudioExtractor.initWithURL: media duration seconds %.2f", mediaDurationSeconds);
            }
            hasAudio = YES;
            audioTrack = [audioTracks firstObject];
            [self detectTrackProperties:audioTrack];
            // decoder configuration settings; LPCM is required, the number of channels
            // may have been detected by the AudioPlayer (a property that doesn't seem
            // to be detectable from a sample buffer), bit depth just has to be set
            trackOutputSettings = @{AVFormatIDKey : @(kAudioFormatLinearPCM), AVNumberOfChannelsKey : @(numberOfChannels), AVLinearPCMBitDepthKey: @(bitsPerSample), AVLinearPCMIsFloatKey : @(NO)};
        
            [self detectSampleProperties];
        } else {
            if (AVFDebug) {
                mjlog("AudioExtractor.initWithURL: the file has no audio tracks");
            }
        }
        
    }
    
    return self;
}

/*
 * To be called only once for a track, tries to detect a few properties, some are
 * essential for handling and interpreting the retrieved bytes, e.g. the sample
 * frequency.
 */
- (void) detectTrackProperties: (AVAssetTrack *) track {
    if (track != nil) {
        CMTimeScale timeScale = [track naturalTimeScale];
        sampleFreq = timeScale;
        
        mjlogf("AudioExtractor.detectTrackProperties:\n\tmedia type: %s\n\ttotal sample data length: %lld\n\tnatural time scale: %d", [[track mediaType] UTF8String], [track totalSampleDataLength], timeScale);
        
        if (AVFDebug) {
            mjlogf("\tcan provide sample cursors: %d", [track canProvideSampleCursors]);//yes
            mjlogf("\tis self contained: %d", [track isSelfContained]);// yes
            mjlogf("\tnominal frame rate: %.2f", [track nominalFrameRate]);
            mjlogf("\testimated data rate: %.2f", [track estimatedDataRate]);
            //mjlogf("\thas audio samples dependencies: %d", [track hasAudioSampleDependencies]);// no
            NSArray *forDescs = [track formatDescriptions];// not there
            if (forDescs != nil) {// not clear if this can happen
                for (id cmd in forDescs) {
                    CMFormatDescriptionRef cmdRef = (__bridge CMFormatDescriptionRef) cmd;
                    mjlogf("\tformat description: %u", CMFormatDescriptionGetMediaSubType(cmdRef));
                }
            }
            CMTime minFrDur = [track minFrameDuration];// 0 / 0
            mjlogf("\tminimal frame duration: %f (%lld / %d)", (minFrDur.value / (double)minFrDur.timescale), minFrDur.value, minFrDur.timescale);
            
            // only log part of the above in a released version
            mjlogf("\tnominal frame rate: %f\n\testimated data rate: %f\n\tminimal frame duration: %f (%lld / %d)", [track nominalFrameRate], [track estimatedDataRate], (minFrDur.value / (double)minFrDur.timescale), minFrDur.value, minFrDur.timescale);
        }
    }
}

/*
 * To be called only once, tries to detect some properties of the sample buffers.
 * It reads a small fragment of the media file and initializes a few sample and
 * sample buffer related members of this class (the usual size of a buffer, the
 * duration of single sample, the number of samples in the buffer etc.).
 * The buffers often have different sizes (probably depending on the compressed chunks
 * of the source), therefore the stored buffer related properties are not of much use.
 */
- (void) detectSampleProperties {
    // create a new reader instance
    NSError *initError = nil;
    AVAssetReader *curReader = [AVAssetReader assetReaderWithAsset:mediaAsset error:&initError];
    
    if (initError != nil) {
        mjlogf("AudioExtractor.detectSampleProperties: AssetReader is NULL: %s", [[initError localizedDescription] UTF8String]);
        curReader = nil;
        return;
    }
    
    AVAssetReaderTrackOutput *curTrackOutput = [AVAssetReaderTrackOutput assetReaderTrackOutputWithTrack:audioTrack outputSettings:trackOutputSettings];
    
    if (curTrackOutput != nil) {
        [curReader addOutput:curTrackOutput];
        [curTrackOutput markConfigurationAsFinal];
        
        double sDur = mediaDurationSeconds < 0.5 ? mediaDurationSeconds : 0.5;
        [curReader setTimeRange:CMTimeRangeFromTimeToTime(CMTimeMakeWithSeconds(0.0, sampleFreq), CMTimeMakeWithSeconds(sDur, sampleFreq))];
        [curTrackOutput markConfigurationAsFinal];
        BOOL readyForRead = [curReader startReading];
        
        if (!readyForRead) {
            mjlog("AudioExtractor.detectSampleProperties: the reader is not ready for reading, cannot detect properties");
        }
        
        int count = 0;
        // check the reader status in a loop (ensuring the loop is not endless)
        // is it possible that copyNextSampleBuffer returns a non-null buffer while
        // the reader status never reaches status Completed?
        while (curReader.status != AVAssetReaderStatusCompleted && count < 20) {//assetReader.status == AVAssetReaderStatusReading
            CMSampleBufferRef sampleRef = [curTrackOutput copyNextSampleBuffer];

            if (sampleRef != NULL) {
                CMSampleTimingInfo timingInfoOut;
                CMSampleBufferGetSampleTimingInfo(sampleRef, 0, &timingInfoOut);
                double presTime = CMTimeGetSeconds(timingInfoOut.presentationTimeStamp);

                CMTime bufferDuration = CMSampleBufferGetDuration(sampleRef);
                CMItemCount numSamples = CMSampleBufferGetNumSamples(sampleRef);
                size_t totalSampleSize = CMSampleBufferGetTotalSampleSize(sampleRef);
                size_t sampleSize = CMSampleBufferGetSampleSize(sampleRef, 0);
                bitsPerSample = (int) (4 * sampleSize);
                CFTypeID bufferType = CMSampleBufferGetTypeID();
                
                // initialize some members once
                sampleDurationSeconds = CMTimeGetSeconds(timingInfoOut.duration);
                sampleDurationMs = 1000 * sampleDurationSeconds;
                // size and duration of a buffer are varying
                durationSecPerSampleBuffer = (bufferDuration.value / (double) bufferDuration.timescale);
                bytesPerSampleBuffer = (int) totalSampleSize;
                // output
                mjlogf("AudioExtractor.detectSampleProperties: \n\tbuffer type: %lu\n\tsample duration: %.7f", bufferType, sampleDurationSeconds);
                if (AVFDebug) {
                    mjlogf("\tsample presentation time %.4f: ", presTime);
                }
                // == num samples * sample duration, the buffer duration CMTime has the number of samples in the buffer as the value and the sample frequency as the timescale (e.g. 44100 or 48000)
                mjlogf("\tsample buffer duration %.4f (%lld / %d)", (bufferDuration.value / (double) bufferDuration.timescale), bufferDuration.value, bufferDuration.timescale);
                mjlogf("\tnumber of samples: %ld\n\ttotal sample size: %ld\n\tbuffer sample size: %ld", numSamples, totalSampleSize, sampleSize);
                    
                    /* the following doesn't provide much information
                    CMBlockBufferRef blockBuffer = CMSampleBufferGetDataBuffer(sampleRef);
                    if (blockBuffer) {
                        size_t bbLength = CMBlockBufferGetDataLength(blockBuffer);
                        NSLog(@"\tblock buffer length: %zu", bbLength);// same as total sample size
                    }
                    
                    CMFormatDescriptionRef formatRef = CMSampleBufferGetFormatDescription(sampleRef);
                    if (formatRef) {
                        CMMediaType mediaMainType = CMFormatDescriptionGetMediaType(formatRef);
                        NSString * mediaMainString;// -> 'soun'
                        if (mediaMainType == kCMMediaType_Audio) {
                            mediaMainString = @"soun";
                        } else {
                            mediaMainString = @"unknown";
                        }
                        
                        CMMediaType mediaSubType = CMFormatDescriptionGetMediaSubType(formatRef);
                        unichar c[4];
                        c[0] = (mediaSubType >> 24) & 0xFF;
                        c[1] = (mediaSubType >> 16) & 0xFF;
                        c[2] = (mediaSubType >> 8) & 0xFF;
                        c[3] = (mediaSubType >> 0) & 0xFF;
                        NSString *subString = [NSString stringWithCharacters:c length:4];
                        // subString -> 'lpcm'
                        NSLog(@"AudioExtractor_detectSampleProperties: CMFormatDescription %@, %@", mediaMainString, subString);
                        CFDictionaryRef formatDict = CMFormatDescriptionGetExtensions(formatRef);
                        
                        if (formatDict) {
                            CFIndex keyCount = CFDictionaryGetCount(formatDict);
                            CFStringRef * keys = new CFStringRef[keyCount];
                            CFDictionaryGetKeysAndValues(formatDict, (const void **)&keys, (const void **)NULL);
                            for (int i = 0; i < keyCount; i++) {
                                NSLog(@"Format Ext %d key: %@, value %@", i, keys[i], CFDictionaryGetValue(formatDict, keys[i]));
                            }
                        } else {
                           NSLog(@"AudioExtractor_detectSampleProperties: no CMFormatDescription Extensions");
                        }
                    } else {
                        NSLog(@"AudioExtractor_detectSampleProperties: no CMFormatDescription");
                    }
                    
                    CMItemCount sizeArrEntries = 0;
                    CMItemCount sizeNeededOut;// -> 1
                    CMSampleBufferGetSampleSizeArray(sampleRef, sizeArrEntries, NULL, &sizeNeededOut);
                    NSLog(@"AudioExtractor_detectSampleProperties: get sample size array: %ld", sizeNeededOut);

                    CMItemCount timingArrayIn = 0;
                    CMItemCount timingArrayNeededOut;// -> 1
                    CMSampleBufferGetOutputSampleTimingInfoArray(sampleRef, timingArrayIn, NULL, &timingArrayNeededOut);
                    NSLog(@"AudioExtractor_detectSampleProperties: output sample timing array: %ld", timingArrayNeededOut);
                    // still no way to get the number of audio channels and bits per sample?
                     */
                [curReader cancelReading];

                CMSampleBufferInvalidate(sampleRef);
                CFRelease(sampleRef);
                break;
            } else {
                BOOL endLoop = TRUE;
                switch(curReader.status) {
                    case AVAssetReaderStatusUnknown:
                        mjlog("AudioExtractor.detectSampleProperties: sample buffer is nil, rading status unknown");
                        break;
                    case AVAssetReaderStatusFailed:
                        mjlogf("AudioExtractor.detectSampleProperties: sample buffer is nil, reading status failed, error: ", ([curReader error] != nil && [[curReader error] description] != nil) ? [[[curReader error] description] UTF8String] : "unknown");
                        break;
                    case AVAssetReaderStatusCancelled:
                        mjlog("AudioExtractor.detectSampleProperties: sample buffer is nil, reading status cancelled");
                        break;
                    case AVAssetReaderStatusCompleted:
                        mjlog("AudioExtractor.detectSampleProperties: sample buffer is nil, reading status completed");
                        break;
                    case AVAssetReaderStatusReading:
                        if (AVFDebug) {
                            mjlog("AudioExtractor.detectSampleProperties: sample buffer is nil, reading status still reading");
                        }
                        // unlikely or impossible to occur
                        endLoop = FALSE;
                        count++;
                        break;
                }
                if (AVFDebug) {
                    mjlog("AudioExtractor.detectSampleProperties: sample buffer of last read is nil");
                }
                
                if (endLoop) {
                    break;// break the reader loop
                }
            }
        }
        if (AVFDebug) {
            mjlogf("AudioExtractor.detectSampleProperties: number of iterations: %d", count);
        }
    } else {
        if (AVFDebug) {
            mjlog("AudioExtractor.detectSampleProperties: failed to create a track output");
        }
    }
    curReader = nil;
    curTrackOutput = nil;
}

/*
 * Sets the read position of the reader. Empty implementation.
 * This is not useful for this framework since a range has to be
 * provided to the reader and/or trackoutput, not just a starting point.
 * Also, a new reader is now created for every read action (initially one
 * reader was reused for all subsequent read actions).
 */
- (BOOL) setPositionSec : (double) seekTime {
    return FALSE;
}

/*
 * A new implementation in which a new asset reader and track output are created every
 * time samples are requested. The AVAssetReaderTrackOutput is now configured
 * with markConfigurationAsFinal; the AVAssetReader is no longer reset for reading new
 * time ranges.
 *
 * The start time and duration of the requested interval have to be passed to this function,
 * though the time range for the asset reader has to be specified with a atart and end
 * time. The destination buffer (e.g. a Java DirectBuffer or byte array) needs to be of
 * sufficient size. It seems the presentation time of the first buffer (almost) exactly
 * matches the requested time, so there is no need to skip bytes at the start of the buffer
 * in the first iteration.
 */
- (int) getSamplesFromTime: (double) fromTime duration: (double) rangeDuration bufferAddress: (char *) destBuffer bufferLength: (size_t) destBufferSize {
    size_t numCopied = 0;
    // to be able to cancel reading, keep track of the number of samples read
    signed long totalSamplesRead = 0;
    signed long totalSamplesRequested = (long)(rangeDuration * sampleFreq);
    if (AVFDebug) {
        mjlogf("AudioExtractor.getSamplesFromTime: number of samples requested: %ld", totalSamplesRequested);
    }
    
    // create a new reader instance
    NSError *initError = nil;
    AVAssetReader *curReader = [AVAssetReader assetReaderWithAsset:mediaAsset error:&initError];
    
    if (initError != nil) {
        mjlogf("AudioExtractor.getSamplesFromTime: AssetReader is NULL: %s", [[initError localizedDescription] UTF8String]);
        [self setLastError: initError];
        curReader = nil;
        return 0;
    }
    // create a new track output instance
    AVAssetReaderTrackOutput *curTrackOutput = [AVAssetReaderTrackOutput assetReaderTrackOutputWithTrack:audioTrack outputSettings:trackOutputSettings];
    [curReader addOutput:curTrackOutput];
    [curTrackOutput markConfigurationAsFinal];
    
    if (curTrackOutput != nil) {
        CMTime cmFromTime = CMTimeMakeWithSeconds(fromTime, sampleFreq);
        CMTime cmEndTime = CMTimeMakeWithSeconds(fromTime + rangeDuration, sampleFreq);
        
        [curReader setTimeRange:CMTimeRangeFromTimeToTime(cmFromTime, cmEndTime)];
        BOOL readyForRead = [curReader startReading];
        
        if (!readyForRead) {
            mjlog("AudioExtractor.getSamplesFromTime: the reader is not ready for reading, cannot produce samples");
            
            return 0;
        }
       
        // check the reader status in a loop
        // with "markConfigurationAsFinal" the completed status should be reached and
        // "copyNextSampleBuffer" should return nil/NULL in the end
        unsigned int failedCopyAttempts = 0;
        while (curReader.status != AVAssetReaderStatusCompleted  && failedCopyAttempts < 10) {
            CMSampleBufferRef sampleRef = [curTrackOutput copyNextSampleBuffer];

            if (sampleRef != NULL) {
                CMSampleTimingInfo timingInfoOut;
                CMSampleBufferGetSampleTimingInfo(sampleRef, 0, &timingInfoOut);
                // check the presentation time on the first read
                //double presTime = CMTimeGetSeconds(timingInfoOut.presentationTimeStamp);
                size_t totalSampleSize = CMSampleBufferGetTotalSampleSize(sampleRef);
            
                totalSamplesRead += CMSampleBufferGetNumSamples(sampleRef);
                
                CMBlockBufferRef blockBuffer = CMSampleBufferGetDataBuffer(sampleRef);
                if (blockBuffer) {
                    // maybe check if it is contiguous
                    //Boolean blockCont = CMBlockBufferIsRangeContiguous(blockBuffer, 0, totalSampleSize);
                    //size_t bbLength = CMBlockBufferGetDataLength(blockBuffer);
                    
                    // variables for copying
                    size_t lengthAtOffset;
                    char * outAddress;
                    OSStatus pointerStatus = CMBlockBufferGetDataPointer(blockBuffer , 0, &lengthAtOffset, NULL, &outAddress);
                    
                    if (pointerStatus == kCMBlockBufferNoErr) {
                        //if lengthAtOffset != totalSampleSize only part of the buffer can be copied, an error situation
                        if (lengthAtOffset != totalSampleSize) {
                            //log and return?
                            if (AVFDebug) {
                                mjlogf("AudioExtractor.getSamplesFromTimes: length at offset is not equal to total sample size: %lu - %lu", lengthAtOffset, totalSampleSize);
                            }
                        }
                        
                        // the number of bytes that can be copied depends on remaining space in the destination buffer and the available bytes in the source
                        size_t remainingSize = destBufferSize - numCopied;
                        size_t copySize = lengthAtOffset < remainingSize ? lengthAtOffset : remainingSize;
                        
                        memcpy(destBuffer + numCopied, outAddress, copySize);
                        numCopied += copySize;
                    } else {
                        // log the error, should this be a reason to break the reading loop?
                        if (AVFDebug) {
                            mjlogf("AudioExtractor.getSamplesFromTimes: error status getting buffer data pointer: %d", pointerStatus);
                        }
                        failedCopyAttempts++;
                    }
                } else {
                    if (AVFDebug) {
                        mjlog("AudioExtractor.getSamplesFromTimes: unable to get the block buffer of the data buffer");
                    }
                    // break the reading loop?
                    failedCopyAttempts++;
                }
            } else {
                if (AVFDebug) {
                    mjlogf("AudioExtractor.getSamplesFromTimes: sample buffer of last read is nil, reader status %d", curReader.status);
                }
                
                break;
            }

            // clean up, release
            if (sampleRef != nil) {
                CMSampleBufferInvalidate(sampleRef);
                CFRelease(sampleRef);
                sampleRef = nil;
            }
        }
    } else {
        if (AVFDebug) {
            mjlog("AudioExtractor.getSamplesFromTimes: the track ouput is nil");
        }
    }
    // probably not really necessary, not likely to happen
    if (curReader.status == AVAssetReaderStatusReading) {
        [curReader cancelReading];
    }
    
    curReader = nil;
    curTrackOutput = nil;
    return (int) numCopied;
}

/*
 * Cancels reading from the reader and removes some references.
 */
- (void) releaseReader {
    mediaAsset  = nil;
    audioTrack  = nil;
}

/*
 * Enables or disables debug mode.
 */
+ (void) setDebugMode: (BOOL) mode {
    mjlogf("AudioExtractor.setDebugMode: %d", mode);
    AVFDebug = mode;
}

/*
 * Returns the current debug state.
 */
+ (BOOL) isDebugMode {
    return AVFDebug;
}


@end
