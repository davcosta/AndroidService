package com.jessicathornsby.datalayer;

public abstract class Element {
    private boolean isFocused = false;
    private String label = "";
    public Element(String label)
    {
        this.label = label;
    }

    public void setFocused(boolean focused)
    {
        this.isFocused = focused;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isFocused() {
        return isFocused;
    }

    public abstract int onClick();
}
