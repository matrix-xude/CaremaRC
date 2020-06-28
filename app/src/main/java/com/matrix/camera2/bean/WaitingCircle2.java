package com.matrix.camera2.bean;

/**
 * author : xxd
 * date   : 2020/6/28
 * desc   :
 */
public class WaitingCircle2 {

    private float circleX;
    private float circleY;
    private float startRadius;
    private float endRadius;
    private float currentRadius;
    private long duration;

    public WaitingCircle2(float circleX, float circleY, float startRadius, float endRadius, long duration) {
        this.circleX = circleX;
        this.circleY = circleY;
        this.startRadius = startRadius;
        this.endRadius = endRadius;
        this.duration = duration;
    }

    public float getCircleX() {
        return circleX;
    }

    public void setCircleX(float circleX) {
        this.circleX = circleX;
    }

    public float getCircleY() {
        return circleY;
    }

    public void setCircleY(float circleY) {
        this.circleY = circleY;
    }

    public float getStartRadius() {
        return startRadius;
    }

    public void setStartRadius(float startRadius) {
        this.startRadius = startRadius;
    }

    public float getEndRadius() {
        return endRadius;
    }

    public void setEndRadius(float endRadius) {
        this.endRadius = endRadius;
    }

    public float getCurrentRadius() {
        return currentRadius;
    }

    public void setCurrentRadius(float currentRadius) {
        this.currentRadius = currentRadius;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
