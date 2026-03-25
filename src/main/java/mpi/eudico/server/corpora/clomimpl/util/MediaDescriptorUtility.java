package mpi.eudico.server.corpora.clomimpl.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import nl.mpi.util.FileExtension;
import nl.mpi.util.FileUtility;


/**
 * A utility class for creating, checking and updating media descriptors.
 *
 * @version 2020 the existing MediaDescriptorUtil has been split into a "core" part
 * and a "client" part
 * 
 * @author Han Sloetjes
 */
public class MediaDescriptorUtility {
	/**
	 * Private constructor.
	 */
    private MediaDescriptorUtility() {
		super();
	}

	/**
     * Checks the existence of the file denoted by the media descriptor.
     *
     * @param md the media descriptor
     *
     * @return true when the file exists, false otherwise
     */
    public static boolean checkLinkStatus(MediaDescriptor md) {
        if ((md == null) || (md.mediaURL == null) ||
                (md.mediaURL.length() < 5)) {
            return false;
        }

        //wwj: return true if it is remote file
        if (FileUtility.isRemoteFile(md.mediaURL)) {
            return true;
        }

        // remove the file: part of the URL, leading slashes are no problem
        int colonPos = md.mediaURL.indexOf(':');
        String fileName = md.mediaURL.substring(colonPos + 1);

        // replace all back slashes by forward slashes
        fileName = fileName.replace('\\', '/');

        File file = new File(fileName);

        if (!file.exists()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Creates a media descriptor for the specified file.
     *
     * @param filePath the full path of the file
     *
     * @return a MediaDescriptor
     */
    public static MediaDescriptor createMediaDescriptor(String filePath) {
        if ((filePath == null) || (filePath.length() == 0)) {
            return null;
        }

        String mediaURL = FileUtility.pathToURLString(filePath);

        if (mediaURL == null) {
            return null;
        }

        String mimeType = null;
        String mediaExtension;

        if (mediaURL.indexOf('.') > -1) {
            mediaExtension = mediaURL.substring(mediaURL.lastIndexOf('.') + 1);
        } else {
            mediaExtension = mediaURL.substring(mediaURL.length() - 3); // of no use, at least with JMF
        }

        mimeType = MediaDescriptorUtility.mimeTypeForExtension(mediaExtension);

        MediaDescriptor md = new MediaDescriptor(mediaURL, mimeType);

        return md;
    }

    /**
     * Creates a Vector of mediadescriptors for the specified files.
     *
     * @param fileNames a collection of files
     *
     * @return a Vector of MediaDescriptors
     */
    public static List<MediaDescriptor> createMediaDescriptors(List<String> fileNames) {
        List<MediaDescriptor> mediaDescriptors = new ArrayList<MediaDescriptor>();

        if (fileNames == null) {
            return mediaDescriptors;
        }

mdloop:
        for (int i = 0; i < fileNames.size(); i++) {
            String path = fileNames.get(i);
            MediaDescriptor nextMD = MediaDescriptorUtility.createMediaDescriptor(path);

            if (nextMD == null) {
                continue;
            }

            for (int j = 0; j < mediaDescriptors.size(); j++) {
                MediaDescriptor otherMD = mediaDescriptors.get(j);

                if (otherMD.mediaURL.equals(nextMD.mediaURL)) {
                    // don't add the same file twice?
                    continue mdloop;
                }

                // should this automatic detection of extracted_from remain??
                if (nextMD.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE) &&
                        MediaDescriptorUtility.isVideoType(otherMD)) {
                    if (FileUtility.sameNameIgnoreExtension(nextMD.mediaURL,
                                otherMD.mediaURL)) {
                        nextMD.extractedFrom = otherMD.mediaURL;
                    }
                }

                if (otherMD.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE) &&
                        MediaDescriptorUtility.isVideoType(nextMD)) {
                    if (FileUtility.sameNameIgnoreExtension(nextMD.mediaURL,
                                otherMD.mediaURL)) {
                        otherMD.extractedFrom = nextMD.mediaURL;
                    }
                }
            }

            mediaDescriptors.add(nextMD);
        }

        return mediaDescriptors;
    }

    /**
     * Returns a mime-type for a given file extension.  Works only for a very
     * limited set of known file types.
     *
     * @param fileExtension the file extension
     *
     * @return a Mime-Type String from the <code>MediaDescriptor</code> class
     */
    public static String mimeTypeForExtension(String fileExtension) {
        if ((fileExtension == null) || (fileExtension.length() < 2)) {
            return MediaDescriptor.UNKNOWN_MIME_TYPE;
        }

        String lowExt = fileExtension.toLowerCase();

        for (String element : FileExtension.MPEG_EXT) {
            if (lowExt.equals(element)) {
                return MediaDescriptor.MPG_MIME_TYPE;
            }
        }

        for (String element : FileExtension.MPEG4_EXT) {
            if (lowExt.equals(element)) {
                return MediaDescriptor.MP4_MIME_TYPE;
            }
        }

        for (String element : FileExtension.WAV_EXT) {
            if (lowExt.equals(element)) {
                return MediaDescriptor.WAV_MIME_TYPE;
            }
        }

        for (String element : FileExtension.QT_EXT) {
            if (lowExt.equals(element)) {
                return MediaDescriptor.QUICKTIME_MIME_TYPE;
            }
        }

        for (String element : FileExtension.MISC_AUDIO_EXT) {
            if (lowExt.equals(element)) {
                return MediaDescriptor.GENERIC_AUDIO_TYPE;
            }
        }

        for (String element : FileExtension.MISC_VIDEO_EXT) {
            if (lowExt.equals(element)) {
                return MediaDescriptor.GENERIC_VIDEO_TYPE;
            }
        }

        // 2010 HS: add for images
        for (String element : FileExtension.IMAGE_MEDIA_EXT) {
            if (lowExt.equals(element)) {
            	if (lowExt.equals("jpg") || lowExt.equals("jpeg")) {
            		return MediaDescriptor.JPEG_TYPE;
            	}
            	return "image/" + lowExt;// add all to MediaDescriptor?
            }
        }

        return MediaDescriptor.UNKNOWN_MIME_TYPE;
    }

    /**
     * Returns a array of file extensions for the given mimetype.  Works only for a very
     * limited set of known file types.
     *
     * @param mimeType the mimeType
     *
     * @return a file extension String[] from the <code>FileExtension</code> class
     */
    public static String[] extensionForMimeType(String mimeType) {
        if ((mimeType == null) ) {
            return null;
        }

        if (mimeType.equals(MediaDescriptor.MPG_MIME_TYPE)) {
            return FileExtension.MPEG_EXT;
        }

        if (mimeType.equals(MediaDescriptor.MP4_MIME_TYPE)) {
            return FileExtension.MPEG4_EXT;
        }

        if (mimeType.equals(MediaDescriptor.WAV_MIME_TYPE)) {
            return FileExtension.WAV_EXT;
        }

        if (mimeType.equals(MediaDescriptor.QUICKTIME_MIME_TYPE)) {
            return FileExtension.QT_EXT;
        }

        if (mimeType.equals(MediaDescriptor.JPEG_TYPE) || mimeType.startsWith("image")) {
            return FileExtension.IMAGE_MEDIA_EXT;
        }

        return null;
    }


    /**
     * Returns whether the specified mime type is a known video type.
     *
     * @param mimeType the mime type string
     *
     * @return true if the specified mimetype is known to be a video type
     *
     * @see #isVideoType(MediaDescriptor)
     */
    public static boolean isVideoType(String mimeType) {
        if (mimeType == null) {
            return false;
        }

        return (mimeType.equals(MediaDescriptor.GENERIC_VIDEO_TYPE) ||
        mimeType.equals(MediaDescriptor.MPG_MIME_TYPE) ||
        mimeType.equals(MediaDescriptor.QUICKTIME_MIME_TYPE)||
        mimeType.equals(MediaDescriptor.MP4_MIME_TYPE));
    }

    /**
     * Returns whether the specified MediaDescriptor is of a known video type.
     *
     * @param descriptor the mediadescriptor
     *
     * @return true if the specified mediadescriptor is of a known video type
     *
     * @see #isVideoType(String)
     */
    public static boolean isVideoType(MediaDescriptor descriptor) {
        if (descriptor == null) {
            return false;
        }

        return MediaDescriptorUtility.isVideoType(descriptor.mimeType);
    }

}
