package com.jessicathornsby.datalayer;

import java.util.Date;

public class Gesture {
    private String gesture;
    private String orientation;
    private long timeStamp;

    public Gesture(String gesture, String orientation)
    {
        this.gesture = gesture;
        this.orientation = orientation;
        timeStamp = new Date().getTime();
    }

    public String getGesture() {
        return gesture;
    }

    public String getOrientation() {
        return orientation;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
