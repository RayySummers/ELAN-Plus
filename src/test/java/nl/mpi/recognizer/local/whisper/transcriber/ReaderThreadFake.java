package nl.mpi.recognizer.local.whisper.transcriber;

import nl.mpi.recognizer.local.whisper.WhisperStandaloneRecognizer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ReaderThreadFake extends ReaderThread {
    public ReaderThreadFake(WhisperStandaloneRecognizer pRecognizer) {
        super(pRecognizer);
    }

    @Override
    public BufferedReader getBufferedReader(InputStream inputStream) {
        return super.getBufferedReader(new ByteArrayInputStream("0".getBytes(UTF_8)));
    }
}
