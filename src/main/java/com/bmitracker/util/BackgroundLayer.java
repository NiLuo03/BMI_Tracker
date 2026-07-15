package com.bmitracker.util;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * 主界面背景幕布 - 支持切换纯色背景
 * 用法: BackgroundLayer.setColor("#050f0a")
 */
public class BackgroundLayer extends Region {

    private static BackgroundLayer instance;

    public static BackgroundLayer getInstance() {
        if (instance == null) instance = new BackgroundLayer();
        return instance;
    }

    private BackgroundLayer() {
        setStyle("-fx-background-color: #050f0a;");
    }

    public void setColor(String hex) {
        setStyle("-fx-background-color: " + hex + ";");
    }

    public void setColor(Color color) {
        setStyle("-fx-background-color: #" + color.toString().substring(2, 8) + ";");
    }
}
