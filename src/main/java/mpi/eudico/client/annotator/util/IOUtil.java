package mpi.eudico.client.annotator.util;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

public class IOUtil {
    private IOUtil(){
        // Hide constructor
    }

    public static OutputStreamWriter getOutputStreamWriter(String encoding, FileOutputStream out)
        throws UnsupportedEncodingException {
        OutputStreamWriter osw;

        try {
            osw = new OutputStreamWriter(out, encoding);
        } catch (UnsupportedCharsetException uce) {
            osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        }
        return osw;
    }

}
