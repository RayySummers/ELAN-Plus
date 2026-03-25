#import "nl_mpi_avf_frame_AVFFrameGrabber.h"
#import <jni.h>
#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#import <mpi_jni_util.h>

/*
 * A struct to connect a global reference to the Java instance and the
 * corresponding AVAssetImageGenerator. A way to retain the configured
 * image generator for repeated calls from the Java counterpart.
 */
typedef struct {
    jobject jRef;
    AVAssetImageGenerator *generator;
} GrabConnector;

/*
 * A global debug flag.
 */
BOOL avfgDebug = FALSE;

/*
 * Class:     nl_mpi_avf_frame_AVFFrameGrabber
 * Method:    initLog
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_avf_frame_AVFFrameGrabber_initLog
(JNIEnv *env, jclass callerClass, jstring clDescriptor, jstring methName) {
    const char *clChars = env->GetStringUTFChars(clDescriptor, NULL);
    if (clChars == NULL) {
        return; // OutOfMemory thrown
    }
    const char *methChars = env->GetStringUTFChars(methName, NULL);
    if (methChars == NULL) {
        return; // OutOfMemory thrown
    }
    mpijni_initLog(env, clChars, methChars);
    
    // free the allocated memory
    env->ReleaseStringUTFChars(clDescriptor, clChars);
    env->ReleaseStringUTFChars(methName, methChars);
}

/*
 * Creates an AVImageGenerator for the video url, generates one image and sets 
 * some values in the Java class.
 *
 * Class:     nl_mpi_avf_frame_AVFFrameGrabber
 * Method:    initNativeAsset
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_avf_frame_AVFFrameGrabber_initNativeAsset
    (JNIEnv *env, jobject callerObject, jstring videoURL) {
        jlong nextId = -1;
        // initialize assets
        const char *videoURLChars = env->GetStringUTFChars(videoURL, NULL);
        // convert jstring to NSString
        NSString *urlString = [NSString stringWithUTF8String:videoURLChars];
        mjlogWE(env, "N_AVFFrameGrabber.initNativeAsset: video URL string:");
        mjlogJS(env, videoURL);
        //
        NSURL *mediaNSURL = NULL;
        BOOL fileProt = [urlString hasPrefix:@"file:"];
        BOOL absFile = [urlString hasPrefix:@"/"];
        
        if (fileProt || absFile) {
            mediaNSURL = [NSURL fileURLWithPath:urlString isDirectory:NO];
        } else {
            mediaNSURL = [NSURL URLWithString:urlString];
        }
        if (avfgDebug) {
            mjlogfWE(env, "N_AVFFrameGrabber.initNativeAsset: video URL 2: %s", [[mediaNSURL absoluteString] UTF8String]);
        }
        
        AVURLAsset *urlAsset = [AVURLAsset URLAssetWithURL:mediaNSURL options:nil];
        // synchronous call to get the duration
        CMTime cmDuration = [urlAsset duration];
        mjlogfWE(env, "N_AVFFrameGrabber.initNativeAsset: duration: %lld / %d", cmDuration.value, cmDuration.timescale);
        
        // set duration field in the Java object
        jclass classRef = env->GetObjectClass(callerObject);
        jfieldID durFieldId = env->GetFieldID(classRef, "videoDuration", "J");
        env->SetLongField(callerObject, durFieldId, (jlong) ((cmDuration.value / cmDuration.timescale) * 1000));

        // check for video tracks, the first one will be used by the generator
        NSArray *trackArray = [urlAsset tracks];
        mjlogfWE(env, "N_AVFFrameGrabber.initNativeAsset: total track count: %lu", [trackArray count]);
        
        NSArray *videoTrackArray = [urlAsset tracksWithMediaType:AVMediaTypeVideo];
        mjlogfWE(env, "N_AVFFrameGrabber.initNativeAsset: video track count: %lu", [videoTrackArray count]);
        
        if ([videoTrackArray count] > 0) {
            for (int i = 0; i < [videoTrackArray count]; i++) {
                AVAssetTrack *videoTrack = videoTrackArray[i];
                CGSize natSize = [videoTrack naturalSize];
                mjlogfWE(env, "N_AVFFrameGrabber.initNativeAsset: video track %d: width: %.2f, height: %.2f", i, natSize.width, natSize.height);
                if (i == 0) {
                    jfieldID vidWidthId = env->GetFieldID(classRef, "videoWidth", "I");
                    env->SetIntField(callerObject, vidWidthId, natSize.width);
                    jfieldID vidHeightId = env->GetFieldID(classRef, "videoHeight", "I");
                    env->SetIntField(callerObject, vidHeightId, natSize.height);
                }
            }
            
            AVAssetImageGenerator *imageGenerator = [AVAssetImageGenerator assetImageGeneratorWithAsset:urlAsset];
            imageGenerator.requestedTimeToleranceBefore = kCMTimeZero;
            imageGenerator.requestedTimeToleranceAfter = kCMTimeZero;

            // extract a random image to initialize some fields
            CMTime reqTime = CMTimeMake(cmDuration.value / 2, cmDuration.timescale);
            CMTime retTime;
            NSError *imageError = nil;
            
            CGImage *image = [imageGenerator copyCGImageAtTime:reqTime actualTime:&retTime error:&imageError];
            if (imageError == nil) {
                GrabConnector *grabConnector = new GrabConnector();
                grabConnector->generator = imageGenerator;
                grabConnector->jRef = env->NewGlobalRef(callerObject);
                nextId = (jlong) grabConnector;
                
                // bits per component
                size_t bitsPerComp = CGImageGetBitsPerComponent(image);
                jfieldID bitsPerCompId = env->GetFieldID(classRef, "numBitsPerPixelComponent", "I");
                env->SetIntField(callerObject, bitsPerCompId, bitsPerComp);
                // bits per pixel
                size_t bitsPerPixel = CGImageGetBitsPerPixel(image);
                jfieldID bitsPerPixId = env->GetFieldID(classRef, "numBitsPerPixel", "I");
                env->SetIntField(callerObject, bitsPerPixId, bitsPerPixel);
                // bytes per image row
                size_t bytesPerRow = CGImageGetBytesPerRow(image);
                jfieldID bytesPerRowId = env->GetFieldID(classRef, "numBytesPerRow", "I");
                env->SetIntField(callerObject, bytesPerRowId, bytesPerRow);
                if(avfgDebug) {
                    mjlogfWE(env, "N_AVFFrameGrabber.initNativeAsset: image bitsPerComponent: %ld, bitsPerPixel: %ld, bytesPerRow: %ld", bitsPerComp, bitsPerPixel, bytesPerRow);
                }
                // image width and height
                size_t imageWidth = CGImageGetWidth(image);
                size_t imageHeight = CGImageGetHeight(image);
                jfieldID imgWidthId = env->GetFieldID(classRef, "imageWidth", "I");
                env->SetIntField(callerObject, imgWidthId, imageWidth);
                jfieldID imgHeightId = env->GetFieldID(classRef, "imageHeight", "I");
                env->SetIntField(callerObject, imgHeightId, imageHeight);
                if(avfgDebug) {
                    mjlogfWE(env, "N_AVFFrameGrabber.initNativeAsset: image imageWidth: %ld, imageHeight: %ld", imageWidth, imageHeight);
                }
                
                // alpha info
                CGImageAlphaInfo alpha = CGImageGetAlphaInfo(image);
                jfieldID alphInfoId = env->GetFieldID(classRef, "alphaInfo", "Ljava/lang/String;");
                jstring alphaInfoString = NULL;
                if (alpha == kCGImageAlphaNone) {
                    alphaInfoString = env->NewStringUTF("kCGImageAlphaNone");
                } else if (alpha == kCGImageAlphaPremultipliedFirst) {
                    alphaInfoString = env->NewStringUTF("kCGImageAlphaPremultipliedFirst");
                } else if (alpha == kCGImageAlphaPremultipliedLast) {
                    alphaInfoString = env->NewStringUTF("kCGImageAlphaPremultipliedLast");
                } else if (alpha == kCGImageAlphaFirst) {
                    alphaInfoString = env->NewStringUTF("kCGImageAlphaFirst");
                } else if (alpha == kCGImageAlphaLast) {
                    alphaInfoString = env->NewStringUTF("kCGImageAlphaLast");
                }
                if (alphaInfoString != NULL) {
                    env->SetObjectField(callerObject, alphInfoId, alphaInfoString);
                    // the following results in a crash:
                    // Invalid memory access of location 0x7f8d83809a8 rip=0x7fff8bb740dd
                    //env->DeleteLocalRef(alphaInfoString);
                    if (avfgDebug) {
                        const char *aiChars = env->GetStringUTFChars(alphaInfoString, NULL);
                        mjlogfWE(env, "N_AVFFrameGrabber.initNativeAsset: image alpha info: %s", aiChars);
                        env->ReleaseStringUTFChars(alphaInfoString, aiChars);
                    }
                }
                // bitmap info
                CGBitmapInfo bitmapInfo = CGImageGetBitmapInfo(image);
                jfieldID bitmapInfoId = env->GetFieldID(classRef, "bitmapInfo", "Ljava/lang/String;");
                jstring bitmapInfoString;
                if (bitmapInfo == kCGBitmapAlphaInfoMask) {
                    bitmapInfoString = env->NewStringUTF("kCGBitmapAlphaInfoMask");
                } else if (bitmapInfo == kCGBitmapFloatInfoMask) {
                    bitmapInfoString = env->NewStringUTF("kCGBitmapFloatInfoMask");
                } else if (bitmapInfo == kCGBitmapFloatComponents) {
                    bitmapInfoString = env->NewStringUTF("kCGBitmapFloatComponents");
                } else if (bitmapInfo == kCGBitmapByteOrderMask) {
                    bitmapInfoString = env->NewStringUTF("kCGBitmapByteOrderMask");
                } else if (bitmapInfo == kCGBitmapByteOrderDefault) {
                    bitmapInfoString = env->NewStringUTF("kCGBitmapByteOrderDefault");
                } else if (bitmapInfo == kCGBitmapByteOrder16Little) {
                    bitmapInfoString = env->NewStringUTF("kCGBitmapByteOrder16Little");
                } else if (bitmapInfo == kCGBitmapByteOrder32Little) {
                    bitmapInfoString = env->NewStringUTF("kCGBitmapByteOrder32Little");
                } else if (bitmapInfo == kCGBitmapByteOrder16Big) {
                    bitmapInfoString = env->NewStringUTF("kCGBitmapByteOrder16Big");
                } else if (bitmapInfo == kCGBitmapByteOrder32Big) {
                    bitmapInfoString = env->NewStringUTF("kCGBitmapByteOrder32Big");
                } else {
                    bitmapInfoString = env->NewStringUTF("Other Bitmap Info");
                }
                if (bitmapInfoString != NULL) {
                    env->SetObjectField(callerObject, bitmapInfoId, bitmapInfoString);
                    //env->DeleteLocalRef(bitmapInfoString);
                    if (avfgDebug) {
                        const char *bmiChars = env->GetStringUTFChars(bitmapInfoString, NULL);
                        mjlogfWE(env, "N_AVFFrameGrabber.initNativeAsset: image bitmap info: %s", bmiChars);
                        env->ReleaseStringUTFChars(bitmapInfoString, bmiChars);
                    }
                }
                
                // color space
                CGColorSpaceRef colorSpace = CGImageGetColorSpace(image);
                if (colorSpace != NULL) {
                    CGColorSpaceModel csModel = CGColorSpaceGetModel(colorSpace);
                    CFStringRef spaceName = CGColorSpaceCopyName(colorSpace);
                    if (spaceName != NULL) {
                        const char* spaceStr = CFStringGetCStringPtr(spaceName, kCFStringEncodingUTF8);
                        if (avfgDebug) {
                            mjlogfWE(env, "N_AVFFrameGrabber.initNativeAsset: color space name: %s, model: %d", spaceStr != NULL ? spaceStr : "?", csModel);
                        }
                    
                        CFRelease(spaceName);
                    } else {
                        if (avfgDebug) {
                            mjlogfWE(env, "N_AVFFrameGrabber.initNativeAsset: color space name is nil, color space model: %d", csModel);
                        }
                    }
                    jfieldID colorModelId = env->GetFieldID(classRef, "colorModelCG", "Ljava/lang/String;");
                    jstring colorModelString = NULL;
                    if (csModel == kCGColorSpaceModelUnknown) {
                        colorModelString = env->NewStringUTF("kCGColorSpaceModelUnknown");
                    } else if (csModel == kCGColorSpaceModelMonochrome) {
                        colorModelString = env->NewStringUTF("kCGColorSpaceModelMonochrome");
                    } else if (csModel == kCGColorSpaceModelRGB) {
                        colorModelString = env->NewStringUTF("kCGColorSpaceModelRGB");
                    } else if (csModel == kCGColorSpaceModelCMYK) {
                        colorModelString = env->NewStringUTF("kCGColorSpaceModelCMYK");
                    } else if (csModel == kCGColorSpaceModelLab) {
                        colorModelString = env->NewStringUTF("kCGColorSpaceModelLab");
                    } else if (csModel == kCGColorSpaceModelDeviceN) {
                        colorModelString = env->NewStringUTF("kCGColorSpaceModelDeviceN");
                    } else if (csModel == kCGColorSpaceModelIndexed) {
                        colorModelString = env->NewStringUTF("kCGColorSpaceModelIndexed");
                    } else if (csModel == kCGColorSpaceModelPattern) {
                        colorModelString = env->NewStringUTF("kCGColorSpaceModelPattern");
                    }
                    if (colorModelString != NULL) {
                        env->SetObjectField(callerObject, colorModelId, colorModelString);
                        //env->DeleteLocalRef(colorModelString);
                        if (avfgDebug) {
                            const char *cmChars = env->GetStringUTFChars(colorModelString, NULL);
                            mjlogfWE(env, "N_AVFFrameGrabber.initNativeAsset: image color space model: %s", cmChars);
                            env->ReleaseStringUTFChars(colorModelString, cmChars);
                        }
                    }
                    //NSLog(@"Color space retain count: %ld", CFGetRetainCount(colorSpace));// mostly 2 
                    // we got this through a Get, no need to release following the Get Rule?
                    //CGColorSpaceRelease(colorSpace);
                } else {
                    mjlogWE(env, "N_AVFFrameGrabber.initNativeAsset: color space is NULL. Image mask");
                }
                
                //NSLog(@"Image retain count: %ld", CFGetRetainCount(image));
                // finally release the image
                // some crashes have been observed at this point
                // invalid memory access, even if retaincount was 1
                CGImageRelease(image);
            } else {
                // give up on this image generator?
                nextId = -1;
                mjlogfWE(env, "N_AVFFrameGrabber.initNativeAsset: error while extracting image: %s", [imageError description] != nil ? [[imageError description] UTF8String] : "unknown error");
            }
            
        } else {
            nextId = -1;
        }
        
        env->ReleaseStringUTFChars(videoURL, videoURLChars);
        return nextId; // points to the connector or is -1
    }

/*
 * Variant of grabbing the bytes of a frame image based on a java.nio.ByteBuffer.
 * Class:     nl_mpi_avf_frame_AVFFrameGrabber
 * Method:    grabVideoFrame
 * Signature: (JJLjava/nio/ByteBuffer;)I
 */
JNIEXPORT jint JNICALL Java_nl_mpi_avf_frame_AVFFrameGrabber_grabVideoFrame
    (JNIEnv *env, jobject callerObject, jlong grabberId, jlong mediaTime, jobject buffer) {
        GrabConnector *grabConnector = (GrabConnector *) grabberId;
        if (grabConnector == nil) {
            mjlogfWE(env, "N_AVFFrameGrabber.grabVideoFrame: no image generator connected with that id: %ld", grabberId);
            return 0;
        }
        AVAssetImageGenerator *imageGenerator = grabConnector->generator;
        
        if (imageGenerator == nil) {
            mjlogfWE(env, "N_AVFFrameGrabber.grabVideoFrame: the image generator is nil for id: %ld", grabberId);
            return 0;
        }
        
        jbyte *bufAddress = (jbyte*) env->GetDirectBufferAddress(buffer);
        jlong bufCapacity = env->GetDirectBufferCapacity(buffer);
        if (avfgDebug) {
            mjlogfWE(env, "N_AVFFrameGrabber.grabVideoFrame: ByteBuffer capacity: %ld", bufCapacity);
        }
        // check size of buffer
        CMTime reqTime = CMTimeMake(mediaTime,  1000);
        CMTime retTime;
        NSError *imageError = nil;
        
        CGImage *image = [imageGenerator copyCGImageAtTime:reqTime actualTime:&retTime error:&imageError];
        if (imageError == nil) {
            if (avfgDebug) {
                mjlogfWE(env, "N_AVFFrameGrabber.grabVideoFrame: request time: %ld, return time: %ld", mediaTime, (long) CMTimeGetSeconds(retTime) * 1000);
            }
            // convert to jbyte array
            CGDataProviderRef imageProvider = CGImageGetDataProvider(image);
            CFDataRef dataRef = CGDataProviderCopyData(imageProvider);
            const UInt8 *dataBuffer = CFDataGetBytePtr(dataRef);
            long length = CFDataGetLength(dataRef);
            
            if (bufAddress != NULL){
                //env->SetByteArrayRegion((jbyteArray) bufAddress, 0, length, (const jbyte*) dataBuffer);
//                for (int j = 0; j < length && j < bufCapacity; j++) {
//                    bufAddress[j] = dataBuffer[j];
//                }
                memcpy(bufAddress, dataBuffer, length);
            }
            CFRelease(dataRef);
            CGImageRelease(image);
            
            return length;
        } else {
            mjlogfWE(env, "N_AVFFrameGrabber.grabVideoFrame: error while extracting image: %s", [imageError description] != nil ? [[imageError description] UTF8String] : "unknown error");
        }
        
        return 0;
    }

/*
 * Variant of grabbing the bytes of a frame image based on a Java byte[] array.
 * Class:     nl_mpi_avf_frame_AVFFrameGrabber
 * Method:    grabVideoFrameBA
 * Signature: (JJ[B)I
 */
JNIEXPORT jint JNICALL Java_nl_mpi_avf_frame_AVFFrameGrabber_grabVideoFrameBA
    (JNIEnv *env, jobject callerObject, jlong grabberId, jlong mediaTime, jbyteArray byteArr)  {
        GrabConnector *grabConnector = (GrabConnector *) grabberId;
        if (grabConnector == nil) {
            mjlogfWE(env, "N_AVFFrameGrabber.grabVideoFrameBA: no image generator connected with that id: %ld", grabberId);
            return 0;
        }
        
        AVAssetImageGenerator *imageGenerator = grabConnector->generator;
        
        if (imageGenerator == nil) {
            mjlogfWE(env, "N_AVFFrameGrabber.grabVideoFrameBA: the image generator is nil for id: %ld", grabberId);
            return 0;
        }
        
        jlong arrLength = env->GetArrayLength(byteArr);
        if (avfgDebug) {
            mjlogfWE(env, "N_AVFFrameGrabber.grabVideoFrameBA: byte array length: %ld", arrLength);
        }
        // check size of buffer
        CMTime reqTime = CMTimeMake(mediaTime,  1000);
        CMTime retTime;
        NSError *imageError = nil;
        
        CGImage *image = [imageGenerator copyCGImageAtTime:reqTime actualTime:&retTime error:&imageError];
        if (imageError == nil) {
            if (avfgDebug) {
                mjlogfWE(env, "N_AVFFrameGrabber.grabVideoFrameBA: request time: %ld, return time: %ld", mediaTime, (long) CMTimeGetSeconds(retTime) * 1000);
            }
            // convert to jbyte array
            CGDataProviderRef imageProvider = CGImageGetDataProvider(image);
            CFDataRef dataRef = CGDataProviderCopyData(imageProvider);
            const UInt8 *dataBuffer = CFDataGetBytePtr(dataRef);
            long length = CFDataGetLength(dataRef);
            
            env->SetByteArrayRegion(byteArr, 0, length, (const jbyte*) dataBuffer);

            CFRelease(dataRef);
            CGImageRelease(image);
            
            return length;
        } else {
            mjlogfWE(env, "N_AVFFrameGrabber.grabVideoFrameBA: error while extracting image: %s", [imageError description] != nil ? [[imageError description] UTF8String] : "unknwon error");
        }
        
        return 0;
    }

/*
 * Saves the frame at the specified mediaTime to the specified imagePath using the CGImageDestination classes 
 * and functions. The format of the image is based on the extension of the specified imagePath 
 * (png (default), jpg, bmp)
 * Class:     nl_mpi_avf_frame_AVFFrameGrabber
 * Method:    saveFrameNativeAVF
 * Signature: (JLjava/lang/String;J)Z
 */
JNIEXPORT jboolean JNICALL Java_nl_mpi_avf_frame_AVFFrameGrabber_saveFrameNativeAVF
    (JNIEnv *env, jobject callerObject, jlong grabberId, jstring imagePath, jlong mediaTime) {
        GrabConnector *grabConnector = (GrabConnector *) grabberId;
        
        if (grabConnector == nil) {
            mjlogfWE(env, "N_AVFFrameGrabber.saveFrameNativeAVF: no image generator connected with that id: %ld", grabberId);
            return JNI_FALSE;
        }
        
        AVAssetImageGenerator *imageGenerator = grabConnector->generator;
        
        if (imageGenerator == nil) {
            mjlogfWE(env, "N_AVFFrameGrabber.saveFrameNativeAVF: the image generator is nil for id: %ld", grabberId);
            return JNI_FALSE;
        }
        
        const char *imageURLChars = env->GetStringUTFChars(imagePath, NULL);
        // convert jstring to NSString
        NSString *urlImgString = [NSString stringWithUTF8String:imageURLChars];
        if (avfgDebug) {
            mjlogfWE(env, "N_AVFFrameGrabber.saveFrameNativeAVF: image URL: %s", [urlImgString UTF8String]);
        }
        NSURL *imgNSURL = [NSURL fileURLWithPath:urlImgString isDirectory:NO];
        
        //CMTime reqTime = CMTimeMake((mediaTime / 1000) * cmDuration.timescale, cmDuration.timescale);
        CMTime reqTime = CMTimeMake(mediaTime,  1000);
        if (avfgDebug) {
            mjlogfWE(env, "N_AVFFrameGrabber.saveFrameNativeAVF: requested image time: %lld / %d", reqTime.value, reqTime.timescale);
        }
        
        if (CMTimeCompare(reqTime, [[imageGenerator asset] duration]) == 1) {
            mjlogWE(env, "N_AVFFrameGrabber.saveFrameNativeAVF: the requested time is greater than the media duration");
            env->ReleaseStringUTFChars(imagePath, imageURLChars);
            return JNI_FALSE;
        }
        
        CMTime retTime;
        NSError *imageError = nil;
        jboolean writeSuccess = JNI_FALSE;
        CGImage *image = [imageGenerator copyCGImageAtTime:reqTime actualTime:&retTime error:&imageError];
        if (imageError == nil) {
            if (avfgDebug) {
                mjlogfWE(env, "N_AVFFrameGrabber.saveFrameNativeAVF: request time: %ld, return time: %lld", mediaTime, retTime.value);
            }
            CFStringRef imageType = kUTTypePNG;
            const char *jpegType = ".jpg";
            NSString *jpgString = [NSString stringWithUTF8String:jpegType];
            const char *bmpType = ".bmp";
            NSString *bmpString = [NSString stringWithUTF8String:bmpType];
            if ([urlImgString hasSuffix:jpgString]) {
                imageType = kUTTypeJPEG;
            } else if ([urlImgString hasSuffix:bmpString]) {
                imageType = kUTTypeBMP;
            }
            // save to file
            CGImageDestinationRef destRef = CGImageDestinationCreateWithURL((CFURLRef)imgNSURL, imageType, 1, NULL);
            CGImageDestinationAddImage(destRef, image, NULL);
            writeSuccess = CGImageDestinationFinalize(destRef);
            if (avfgDebug) {
               mjlogfWE(env, "N_AVFFrameGrabber.saveFrameNativeAVF: image stored: %i", writeSuccess);
            }
            CGImageRelease(image);
            CFRelease(destRef);
        } else {
            mjlogfWE(env, "N_AVFFrameGrabber.saveFrameNativeAVF: error extracting image from the video: %s", [imageError description] != nil ? [[imageError description] UTF8String] : "unknown error");
        }

        env->ReleaseStringUTFChars(imagePath, imageURLChars);
        return writeSuccess;
    }

/*
 * Removes the ImageGenerator with the specified id from the Dictionary.
 * Class:     nl_mpi_avf_frame_AVFFrameGrabber
 * Method:    release
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_avf_frame_AVFFrameGrabber_release
    (JNIEnv *env, jobject callerObject, jlong grabberId) {
        GrabConnector *grabConnector = (GrabConnector *) grabberId;
        
        if (grabConnector == nil) {
            mjlogfWE(env, "N_AVFFrameGrabber.release: no image generator connected with that id: %ld", grabberId);
            return;
        }
        
        AVAssetImageGenerator *imageGenerator = grabConnector->generator;
        env->DeleteGlobalRef(grabConnector->jRef);
        
        delete(&imageGenerator);
        delete(grabConnector);
        //free(&imageGenerator);
        //free(grabConnector);
    }

/*
 * Class:     nl_mpi_avf_frame_AVFFrameGrabber
 * Method:    setDebugMode
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_avf_frame_AVFFrameGrabber_setDebugMode
(JNIEnv *env, jclass callerClass, jboolean enableMode) {
    mjlogfWE(env, "N_AVFFrameGrabber.setDebugMode: debug mode enabled: %d", enableMode);
    avfgDebug = (BOOL) enableMode;
}
