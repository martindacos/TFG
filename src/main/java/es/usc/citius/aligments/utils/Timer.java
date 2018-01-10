package es.usc.citius.aligments.utils;

import java.util.concurrent.TimeUnit;

public class Timer {

    private long startTime = 0;
    private long endTime = 0;
    private long elapsedTime = 0;

    public long start() {
        elapsedTime = 0;

        return resume();
    }

    public long pause() {
        endTime = System.nanoTime();
        increaseElapsedTime();

        return endTime;
    }

    public long resume() {
        endTime = 0;
        startTime = System.nanoTime();

        return startTime;
    }

    public long stop() {
        return pause();
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    private void increaseElapsedTime() {
        elapsedTime += endTime - startTime;
    }

    public String getReadableElapsedTime() {
        final long mins = TimeUnit.NANOSECONDS.toMinutes(elapsedTime);
        final long secs = TimeUnit.NANOSECONDS.toSeconds(elapsedTime) - TimeUnit.MINUTES.toSeconds(mins);
        final long millis = TimeUnit.NANOSECONDS.toMillis(elapsedTime) - TimeUnit.MINUTES.toMillis(mins) - TimeUnit.SECONDS.toMillis(secs);
        return String.format("%02d min, %02d sec, %02d millis", mins, secs, millis);
    }

    public String getElapsedTimeInSeconds() {
        final long secs = TimeUnit.NANOSECONDS.toSeconds(elapsedTime);
        final long millis = TimeUnit.NANOSECONDS.toMillis(elapsedTime) - TimeUnit.SECONDS.toMillis(secs);
        return String.format("%01d.%03d", secs, millis);
    }
}
