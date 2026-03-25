package mpi.eudico.util;

import java.util.Random;

public final class RandomNumberGenerator {

    private Random nativeRandomGenerator;

    private static class RandomNumberGeneratorHolder {
        private static final RandomNumberGenerator INSTANCE = new RandomNumberGenerator();
    }

    private RandomNumberGenerator() {
        nativeRandomGenerator = new Random();
    }

    public static RandomNumberGenerator getInstance() {
        return RandomNumberGeneratorHolder.INSTANCE;
    }

    public int getNewRandomNumber(int bound) {
        return nativeRandomGenerator.nextInt(bound);
    }

    public String postFixToMakeUnique(String str, int bound) {
        return str.concat(String.valueOf(getNewRandomNumber(bound)));
    }
}
