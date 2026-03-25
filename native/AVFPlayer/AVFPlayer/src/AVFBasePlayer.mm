//
//  AVFBasePlayer.mm
//  AVFPlayer
//
//  Created by Han Sloetjes.
//  Copyright (c) 2019 MPI. All rights reserved.
//  The base implementation of a AVFoundation based player, contains
//  creation of the player, storing in the map and deletion of the player
//
#import "AVFBasePlayer.h"
#import "AVFLog.h"
#import <AVFoundation/AVFoundation.h>
#import <Foundation/Foundation.h>
#import <map>
#import <mpi_jni_util.h>

// the scope of these fields is this class / this file
static long customPlayerId = 10000;
static std::map<long, id> customPlayerMap;

@implementation AVFBasePlayer

@synthesize player;
@synthesize mediaAsset;
@synthesize hasAudio;
@synthesize hasVideo;
@synthesize videoWidth;
@synthesize videoHeight;
@synthesize nominalFrameRate;
@synthesize frameDurationMs;
@synthesize frameDurationSeconds;
@synthesize lastError;

/*
 * Creates a AVURLAsset, AVPlayerItem and AVPlayer
 */
- (id) initWithURL: (NSURL *) url {
    self = [super init];
    // initialize some fields
    videoWidth = 0;
    videoHeight = 0;
    intervalEndObserver = nil;
    
    if ([AVFLog isLoggable:AVFLogInfo]) {
        mjlogf("AVFBasePlayer.initWithURL: creating player for URL: %s", [[url absoluteString] UTF8String]);
    }
    NSDictionary *initOptions = @{ AVURLAssetPreferPreciseDurationAndTimingKey : @YES };
    mediaAsset = [AVURLAsset URLAssetWithURL:url options:initOptions];
    CMTime assDuration = [mediaAsset duration];
    if ([AVFLog isLoggable:AVFLogFine]) {
        mjlogf("AVFBasePlayer.initWithURL: media asset duration: %lld, %d (%.4f sec.)", assDuration.value, assDuration.timescale, assDuration.timescale != 0 ? assDuration.value/(float)assDuration.timescale : 0.0);
    }

    AVPlayerItem *playerItem = [AVPlayerItem playerItemWithAsset:mediaAsset];
    // Create the AVPlayer
    player = [AVPlayer playerWithPlayerItem:playerItem];
    [player setActionAtItemEnd:AVPlayerActionAtItemEndPause];
    
    CMTime medDuration = [[player currentItem] duration];
    if ([AVFLog isLoggable:AVFLogFine]) {
        mjlogf("AVFBasePlayer.initWithURL: player duration: %lld, %d (%.4f sec.)", medDuration.value, medDuration.timescale, medDuration.timescale != 0 ? medDuration.value/(float)medDuration.timescale : 0.0);
    }
    [self detectTracks];
    
    return self;
}

/*
 * Detects whether there is an audio track and, if so, what the frame rate is.
 * Detects whether there is a video track and, if so, stores frame rate and natural size.
 */
- (void) detectTracks {
    if (mediaAsset != nil) {
        
        // check audio tracks
        NSArray *audioTrackArray = [mediaAsset tracksWithMediaType:AVMediaTypeAudio];
        
        if ([audioTrackArray count] > 0) {
            AVAssetTrack *firstTrack = audioTrackArray[0];
            nominalFrameRate = [firstTrack nominalFrameRate];
            hasAudio = TRUE;
            
            if ([AVFLog isLoggable:AVFLogInfo]) {
                mjlogf("AVFBasePlayer.detectTracks: audio track frame rate: %.4f", nominalFrameRate);
            }
        } else {
            hasAudio = FALSE;
        }
        
        // videoTrack = [[mediaAsset tracksWithMediaType:AVMediaTypeVideo] firstObject];
        // if (videoTrack != NULL) { etc.
        NSArray *videoTrackArray = [mediaAsset tracksWithMediaType:AVMediaTypeVideo];
        if ([videoTrackArray count] > 0) {
            AVAssetTrack *videoTrack = videoTrackArray[0];
            hasVideo = TRUE;
            
            CGSize natSize = [videoTrack naturalSize];
            videoWidth = natSize.width;
            videoHeight = natSize.height;
            if ([AVFLog isLoggable:AVFLogInfo]) {
                mjlogf("AVFBasePlayer.detectTracks: video track 0: width: %.2f, height %.2f", videoWidth, videoHeight);
            }
            /*for (int i = 0; i < [videoTrackArray count]; i++) {
             AVAssetTrack *vidTrack = videoTrackArray[i];
             CGSize naturalSize = [vidTrack naturalSize];
             NSLog(@"Video track %d: width: %f, height %f", i, naturalSize.width, naturalSize.height);
             }*/
            // if there is video, its frame rate is used
            nominalFrameRate = [videoTrack nominalFrameRate];
            if ([AVFLog isLoggable:AVFLogInfo]) {
                mjlogf("AVFBasePlayer.detectTracks: video track frame rate: %.4f", nominalFrameRate);
            }
        } else {
            hasVideo = FALSE;
        }
        
        if (nominalFrameRate != 0) {
            frameDurationMs = 1000 / nominalFrameRate;
            frameDurationSeconds = 1 / nominalFrameRate;
            
            if ([AVFLog isLoggable:AVFLogInfo]) {
                mjlogf("AVFBasePlayer.detectTracks: frame duration (ms): %.2f, (sec): %.4f", frameDurationMs, frameDurationSeconds);
            }
        }
    } else {
        if ([AVFLog isLoggable:AVFLogWarning]) {
            mjlog("AVFBasePlayer.detectTracks: media asset is null");
        }
    }
}

/*
 * Sets some fields to NULL (no direct deallocation in ARC).
 */
- (void) releasePlayer {
    //videoTrack = NULL;
    //outputSettings = NULL;
    player = NULL;
    mediaAsset = NULL;
}

/*
 * Sets the stop time in milliseconds for playing a selection.
 */
- (void) setStopTime: (long) stopTime {
    NSMutableArray *times = [NSMutableArray array];
    CMTime endTime = CMTimeMake(stopTime, 1000);
    if ([AVFLog isLoggable:AVFLogFine]) {
        mjlogf("AVFBasePlayer.setStopTime: stop time in milliseconds: %ld", stopTime);
    }
    
    [times addObject:[NSValue valueWithCMTime:endTime]];
    AVPlayer * __weak weakPlayer = player;
    AVFBasePlayer * __weak wrapPlayer = self;
    intervalEndObserver = [player addBoundaryTimeObserverForTimes:times queue:dispatch_get_main_queue() usingBlock:^{
        [weakPlayer pause];
        [wrapPlayer removeStopTime];
        if ([AVFLog isLoggable:AVFLogFine]) {
            mjlogf("AVFBasePlayer.setStopTime_block: paused at milliseconds %ld",(long) (1000 * CMTimeGetSeconds([weakPlayer currentTime])));
        }
    }];
}

/*
 * Sets the stop time in seconds for playing a selection.
 */
- (void) setStopTimeSeconds: (double) stopTimeSeconds {
    NSMutableArray *times = [NSMutableArray array];
    CMTime endTime = CMTimeMakeWithSeconds(stopTimeSeconds, 1000);
    if ([AVFLog isLoggable:AVFLogFine]) {
        mjlogf("AVFBasePlayer.setStopTimeSeconds: stop time in seconds: %.4f", stopTimeSeconds);
    }
    
    [times addObject:[NSValue valueWithCMTime:endTime]];
    AVPlayer * __weak weakPlayer = player;
    AVFBasePlayer * __weak wrapPlayer = self;
    intervalEndObserver = [player addBoundaryTimeObserverForTimes:times queue:dispatch_get_main_queue() usingBlock:^{
        [weakPlayer pause];
        [wrapPlayer removeStopTime];
        if ([AVFLog isLoggable:AVFLogFine]) {
            mjlogf("AVFBasePlayer.setStopTimeSeconds_block: paused at seconds %.4f",CMTimeGetSeconds([weakPlayer currentTime]));
        }
    }];
}

/*
 * Unsets the stop time, removes the stop time observer.
 */
- (void) removeStopTime {
    if (intervalEndObserver != nil) {
        [player removeTimeObserver:intervalEndObserver];
        intervalEndObserver = nil;
        if ([AVFLog isLoggable:AVFLogFine]) {
            mjlog("AVFBasePlayer_removeStopTime: removed and nullified observer");
        }
    } else {
        if ([AVFLog isLoggable:AVFLogFine]) {
            mjlog("AVFBasePlayer_removeStopTime: end observer is nil");
        }
    }
}

/*
 * Class methods for creating an id for a player and storing it in and
 * retrieving it from the player map.
 */
+ (long) createIdAndStorePlayer: (id) avfPlayer {
    if (avfPlayer != NULL) {
        long nextId = customPlayerId++;
        customPlayerMap[nextId] = avfPlayer;
        return nextId;
    }
    return 0;
}

/*
 * Returns the player for the specified player id.
 */
+ (id) getPlayerWithId: (long) playerId {
    return customPlayerMap[playerId];// can be null
}

/*
 * Removes the player with the specified id from the map.
 */
+ (void) removePlayerWithId: (long) playerId {
    customPlayerMap.erase(playerId);
}

@end

