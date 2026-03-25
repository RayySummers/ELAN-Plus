package mpi.eudico.client.annotator.util;

import javax.swing.*;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

public class ResourceUtil implements ClientLogger  {

    private ResourceUtil() {
        // Hide constructor.
    }

    public static Optional<ImageIcon> getImageIconFrom(String resName) {
        Optional<ImageIcon> imageIcon = Optional.empty();

        try {
            imageIcon = Optional.of(new ImageIcon(getResource(resName)));
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error getting resource", e);
        }
        return imageIcon;
    }

    public static URL getResource(String resName) {
        return Objects.requireNonNull(ResourceUtil.class.getResource(resName));
    }

    public static InputStream getResourceAsStream(String resName) {
        return Objects.requireNonNull(ResourceUtil.class.getResourceAsStream(resName));
    }

}
