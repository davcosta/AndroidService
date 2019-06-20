package com.jessicathornsby.datalayer;

public class HomeButton extends Element{
    private static final int HOME_BUTTON = 1004;

    public HomeButton(String label) {
        super(label);
    }

    @Override
    public int onClick() {
        return HOME_BUTTON;
    }
}
