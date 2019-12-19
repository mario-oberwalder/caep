/*
 * Copyright (c) 2019. Mario Oberwalder mario.oberwalder@gmail.com
 */

package it.oberwalder.caep;

import org.opencv.core.Point;

public class RawResult {

    private Double frameTime;
    private Integer frameNumber;
    private Double confidence;
    private Point point1;

    public RawResult(Double frameTime, Double frameTime1, double confidence, Point point1, Point point2) {
        this.frameTime = frameTime;
        this.frameNumber =  (int) (frameTime1/1);
        this.confidence = confidence;
        this.point1 = point1.clone();
        this.point2 = point2.clone();
    }

    public Double getFrameTime() {
        return frameTime;
    }

    public Integer getFrameNumber() {
        return frameNumber;
    }

    public Double getConfidence() {
        return confidence;
    }

    public Point getPoint1() {
        return point1;
    }

    public Point getPoint2() {
        return point2;
    }

    private Point point2;

    public RawResult(Double frameTime, Integer frameNumber, double confidence, int x1, int y1, int x2, int y2) {
        this.frameTime = frameTime;
        this.frameNumber = frameNumber;
        this.confidence = confidence;
        this.point1 = new Point(x1,y1);
        this.point2 = new Point(x2,y2);
    }

    public RawResult(Double frameTime, double confidence, Point point1, Point point2) {
        this.frameTime = frameTime;
        this.confidence = confidence;
        this.point1 = point1.clone();
        this.point2 = point2.clone();
    }
}
