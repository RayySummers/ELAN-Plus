package mpi.eudico.client.annotator;

/**
 * Defines the methods to be implemented by a TimeScaleUser.
 * A TimeScaleUser has direct access to the {@code TimeScale}.
 */
public interface TimeScaleUser extends TimeScaleListener {
    /**
     * Sets the global, shared {@code TimeScale} instance.
     *
     * @param timeScale the {@code TimeScale} instance
     */
    public void setGlobalTimeScale(TimeScale timeScale);

    /**
     * Returns the global {@code TimeScale} object.
     *
     * @return the global {@code TimeScale}
     */
    public long getGlobalTimeScaleIntervalBeginTime();

    /**
     * Sets the interval begin time of the global {@code TimeScale}.
     *
     * @param time the new begin time
     */
    public void setGlobalTimeScaleIntervalBeginTime(long time);

    /**
     * Returns the interval end time of the global {@code TimeScale}.
     *
     * @return the global interval end time
     */
    public long getGlobalTimeScaleIntervalEndTime();

    /**
     * Sets the interval end time of the global {@code TimeScale}.
     *
     * @param time the new global interval end time
     */
    public void setGlobalTimeScaleIntervalEndTime(long time);

    /**
     * Returns the interval duration of the global {@code TimeScale}.
     *
     * @return the global interval duration
     */
    public long getGlobalTimeScaleIntervalDuration();

    /**
     * Returns the global milliseconds per pixel value.
     *
     * @return the global milliseconds per pixel
     */
    public float getGlobalTimeScaleMsPerPixel();

    /**
     * Sets the global milliseconds per pixel value.
     *
     * @param step the milliseconds per pixel
     */
    public void setGlobalTimeScaleMsPerPixel(float step);

    /**
     * Notification of a change in the global {@code TimeScale}.
     */
    @Override
	public void updateTimeScale();
}
