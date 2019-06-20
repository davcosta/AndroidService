package com.jessicathornsby.datalayer;

import java.util.ArrayList;

public class Menu {
    private String menuName = "";
    private ArrayList<Element> menuButtons = new ArrayList<Element>();
    private int currentFocused = -1;

    public Menu(String menuName)
    {
        this.menuName = menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getMenuName() {
        return menuName;
    }

    public void addButton(Element element)
    {
        menuButtons.add(element);
    }

    public void removeButton(int index){
        menuButtons.remove(index);
    }

    public ArrayList<Element> getMenuButtons()
    {
        return menuButtons;
    }

    public int getMenuSize()
    {
        return menuButtons.size();
    }

    public void focusElement(int index)
    {
        currentFocused = index;
        menuButtons.get(index).setFocused(true);
    }
    public void unfocusElement(int index)
    {
        currentFocused = index;
        menuButtons.get(index).setFocused(false);
    }
    public int getCurrentFocused() {
        return currentFocused;
    }
}
