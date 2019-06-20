package com.jessicathornsby.datalayer;

public class BackgroundButton extends Element {
    private static final int BACKGROUND_BUTTON = 1004;

    public BackgroundButton(String label) {
        super(label);
    }

    @Override
    public int onClick() {
        return BACKGROUND_BUTTON;
    }
}
