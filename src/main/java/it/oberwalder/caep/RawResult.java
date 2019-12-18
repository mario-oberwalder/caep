/*
 * Copyright (c) 2019. Mario Oberwalder mario.oberwalder@gmail.com
 */

package it.oberwalder.caep;

import org.opencv.core.Point;

public class RawResult {

    private Double timeStamp;
    private Double confidence;
    private Point point1;
    private Point point2;

    public RawResult(Double timeStamp, double confidence, int x1, int y1, int x2, int y2) {
        this.timeStamp = timeStamp;
        this.confidence = confidence;
        this.point1 = new Point(x1,y1);
        this.point2 = new Point(x2,y2);
    }

    public RawResult(Double timeStamp, double confidence, Point point1, Point point2) {
        this.timeStamp = timeStamp;
        this.confidence = confidence;
        this.point1 = point1.clone();
        this.point2 = point2.clone();
    }
}
