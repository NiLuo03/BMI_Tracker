package com.bmitracker.component;

import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class TitleBar extends HBox {

    private final Button minBtn = new Button("─");
    private final Button maxBtn = new Button("□");
    private final Button closeBtn = new Button("✕");
    private double xOff, yOff;
    private double prevX, prevY, prevW, prevH;
    private boolean maximized;

    public TitleBar() {
        getStyleClass().add("title-bar");
        setAlignment(Pos.CENTER_LEFT);
        setPrefHeight(32);
        setMinHeight(32);
        setMaxHeight(32);

        closeBtn.getStyleClass().addAll("title-bar-btn", "title-bar-close-btn");
        minBtn.getStyleClass().add("title-bar-btn");
        maxBtn.getStyleClass().add("title-bar-btn");

        setOnMousePressed(e -> {
            Stage s = getStage();
            if (s != null) { xOff = e.getSceneX(); yOff = e.getSceneY(); }
        });
        setOnMouseDragged(e -> {
            Stage s = getStage();
            if (s != null && !maximized) { s.setX(e.getScreenX() - xOff); s.setY(e.getScreenY() - yOff); }
        });

        minBtn.setOnAction(e -> { Stage s = getStage(); if (s != null) s.setIconified(true); });
        maxBtn.setOnAction(e -> {
            Stage s = getStage();
            if (s == null) return;
            if (!maximized) {
                prevX = s.getX(); prevY = s.getY();
                prevW = s.getWidth(); prevH = s.getHeight();
                Rectangle2D vb = Screen.getPrimary().getVisualBounds();
                s.setX(vb.getMinX()); s.setY(vb.getMinY());
                s.setWidth(vb.getWidth()); s.setHeight(vb.getHeight());
                maxBtn.setText("❐");
            } else {
                s.setX(prevX); s.setY(prevY);
                s.setWidth(prevW); s.setHeight(prevH);
                maxBtn.setText("□");
            }
            maximized = !maximized;
        });
        closeBtn.setOnAction(e -> { Stage s = getStage(); if (s != null) s.close(); });

        Region filler = new Region();
        HBox.setHgrow(filler, Priority.ALWAYS);
        getChildren().addAll(filler, minBtn, maxBtn, closeBtn);
        setCursor(Cursor.DEFAULT);
    }

    public Button getMinBtn() { return minBtn; }
    public Button getMaxBtn() { return maxBtn; }
    public Button getCloseBtn() { return closeBtn; }

    private Stage getStage() {
        if (getScene() != null && getScene().getWindow() instanceof Stage s) return s;
        return null;
    }
}
