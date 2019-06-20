package com.jessicathornsby.datalayer;

public class BackButton extends Element {
    private static final int BACK_BUTTON = 1003;
    public BackButton(String label) {
        super(label);
    }

    @Override
    public int onClick() {
        return BACK_BUTTON;
    }
}
