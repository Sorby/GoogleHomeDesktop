package me.sorby.googlehome.main;

import me.sorby.googlehome.gui.MainWindow;

public class Starter {
    public static void main(String[] args) {
        //Enable debug messages in slf4j logger
        //System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        new MainWindow();
    }
}
