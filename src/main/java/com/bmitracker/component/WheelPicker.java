package com.bmitracker.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.List;

public class WheelPicker<T> extends Pane {

    private static final double STIFFNESS = 300;
    private static final double DAMPING = 28;

    private final ObservableList<T> items = FXCollections.observableArrayList();
    private final Canvas canvas = new Canvas();

    private double itemHeight = 36;
    private int visibleItems = 5;
    private double currentOffset = 0;

    private final IntegerProperty selectedIndex = new SimpleIntegerProperty(-1);
    private final ObjectProperty<T> value = new SimpleObjectProperty<>();

    private double dragStartY;
    private double dragStartOffset;
    private boolean dragging = false;

    private AnimationTimer snapTimer;
    private double snapVel, snapPos, snapTarget;
    private long snapLastTime;

    private Color textColor = Color.rgb(255, 255, 255, 0.85);
    private Color centerBarBg = Color.rgb(255, 255, 255, 0.06);
    private Color centerBarStroke = Color.rgb(255, 255, 255, 0.12);
    private Color fadeTopColor = Color.rgb(20, 20, 25, 0.94);
    private Color fadeBottomColor = Color.rgb(20, 20, 25, 0.94);

    public void setLightTheme(boolean light) {
        if (light) {
            textColor = Color.rgb(30, 30, 30, 0.92);
            centerBarBg = Color.rgb(0, 0, 0, 0.08);
            centerBarStroke = Color.rgb(0, 0, 0, 0.20);
            fadeTopColor = Color.rgb(245, 245, 250, 0.96);
            fadeBottomColor = Color.rgb(245, 245, 250, 0.96);
        } else {
            textColor = Color.rgb(255, 255, 255, 0.85);
            centerBarBg = Color.rgb(255, 255, 255, 0.06);
            centerBarStroke = Color.rgb(255, 255, 255, 0.12);
            fadeTopColor = Color.rgb(20, 20, 25, 0.94);
            fadeBottomColor = Color.rgb(20, 20, 25, 0.94);
        }
        draw();
    }

    public WheelPicker() {
        getStyleClass().add("wheel-picker");
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        getChildren().add(canvas);

        widthProperty().addListener(o -> draw());
        heightProperty().addListener(o -> draw());

        selectedIndex.addListener((obs, old, idx) -> {
            int i = idx.intValue();
            if (i >= 0 && i < items.size()) value.set(items.get(i));
        });

        setOnMousePressed(e -> {
            cancelSnap();
            dragging = true;
            dragStartY = e.getY();
            dragStartOffset = currentOffset;
        });

        setOnMouseDragged(e -> {
            if (!dragging) return;
            double dy = e.getY() - dragStartY;
            currentOffset = clampOffset(dragStartOffset + dy);
            draw();
        });

        setOnMouseReleased(e -> {
            if (!dragging) return;
            dragging = false;
            snapToClosest();
        });

        setOnScroll(e -> {
            int idx = selectedIndex.get();
            if (items.isEmpty()) return;
            if (idx < 0) idx = 0;
            idx += e.getDeltaY() > 0 ? -1 : 1;
            idx = Math.max(0, Math.min(items.size() - 1, idx));
            snapToIndex(idx);
        });
    }

    public ObservableList<T> getItems() { return items; }

    public void setItems(List<T> itemList) {
        items.setAll(itemList);
        draw();
    }

    public int getSelectedIndex() { return selectedIndex.get(); }
    public IntegerProperty selectedIndexProperty() { return selectedIndex; }

    public T getValue() { return value.get(); }
    public ObjectProperty<T> valueProperty() { return value; }

    public void setValue(T val) {
        int idx = items.indexOf(val);
        if (idx >= 0) setSelectedIndex(idx);
    }

    public void setSelectedIndex(int idx) {
        if (items.isEmpty()) return;
        idx = Math.max(0, Math.min(items.size() - 1, idx));
        cancelSnap();
        selectedIndex.set(idx);

        double h = getHeight();
        if (h > 0) {
            currentOffset = snapOffsetForIndex(idx, h);
            draw();
        }
    }

    public double getItemHeight() {return itemHeight; }
    public void setItemHeight(double h) { itemHeight = h; draw(); }

    public int getVisibleItems() { return visibleItems; }
    public void setVisibleItems(int n) { visibleItems = n; draw(); }

    private double snapOffsetForIndex(int idx, double h) {
        return h / 2.0 - idx * itemHeight - itemHeight / 2.0;
    }

    private double snapOffsetForIndex(int idx) {
        return snapOffsetForIndex(idx, getHeight());
    }

    private int indexForOffset(double offset) {
        double h = getHeight();
        if (h <= 0) return 0;
        double centerOff = h / 2.0 - itemHeight / 2.0;
        return (int) Math.round((centerOff - offset) / itemHeight);
    }

    private double clampOffset(double off) {
        if (items.isEmpty()) return 0;
        double h = getHeight();
        if (h <= 0) return 0;
        double maxOff = snapOffsetForIndex(0, h);
        double minOff = snapOffsetForIndex(items.size() - 1, h);
        return Math.max(minOff, Math.min(maxOff, off));
    }

    private void snapToClosest() {
        if (items.isEmpty()) return;
        int idx = indexForOffset(currentOffset);
        idx = Math.max(0, Math.min(items.size() - 1, idx));
        snapToIndex(idx);
    }

    private void snapToIndex(int idx) {
        if (items.isEmpty()) return;
        double h = getHeight();
        if (h <= 0) return;
        idx = Math.max(0, Math.min(items.size() - 1, idx));

        cancelSnap();
        selectedIndex.set(idx);
        snapTarget = snapOffsetForIndex(idx, h);
        snapPos = currentOffset;
        snapVel = 0;
        snapLastTime = 0;

        snapTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (snapLastTime == 0) { snapLastTime = now; return; }
                double dt = Math.min((now - snapLastTime) / 1e9, 0.03);
                snapLastTime = now;
                double springForce = -STIFFNESS * (snapPos - snapTarget);
                double dampForce = -DAMPING * snapVel;
                double accel = springForce + dampForce;
                snapVel += accel * dt;
                snapPos += snapVel * dt;
                snapPos = clampOffset(snapPos);
                currentOffset = snapPos;
                draw();
                if (Math.abs(snapPos - snapTarget) < 0.5 && Math.abs(snapVel) < 5) {
                    currentOffset = snapTarget;
                    draw();
                    stop();
                    snapTimer = null;
                }
            }
        };
        snapTimer.start();
    }

    private void cancelSnap() {
        if (snapTimer != null) { snapTimer.stop(); snapTimer = null; }
    }

    private void draw() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0 || items.isEmpty()) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        double halfRange = visibleItems * itemHeight / 2.0;

        for (int i = 0; i < items.size(); i++) {
            double centerY = i * itemHeight + currentOffset + itemHeight / 2.0;
            if (centerY < -itemHeight || centerY > h + itemHeight) continue;

            double dist = Math.abs(centerY - h / 2.0) / halfRange;
            dist = Math.min(dist, 1.0);

            double scale = 1.0 - 0.3 * dist;
            double alpha = 1.0 - 0.7 * dist;

            gc.setGlobalAlpha(alpha);
            gc.setFill(textColor);
            gc.setFont(Font.font("Microsoft YaHei", 16 * scale));
            gc.fillText(String.valueOf(items.get(i)), w / 2.0, centerY);
        }
        gc.setGlobalAlpha(1.0);

        double centerBarY = h / 2.0 - itemHeight / 2.0;
        gc.setFill(centerBarBg);
        gc.fillRect(0, centerBarY, w, itemHeight);
        gc.setStroke(centerBarStroke);
        gc.setLineWidth(0.5);
        gc.strokeRect(0, centerBarY, w, itemHeight);

        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, fadeTopColor),
                new Stop(1, Color.TRANSPARENT)));
        gc.fillRect(0, 0, w, h / 2.0);

        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(1, fadeBottomColor)));
        gc.fillRect(0, h / 2.0, w, h / 2.0);
    }

    @Override
    protected double computePrefWidth(double height) {
        return 140;
    }

    @Override
    protected double computePrefHeight(double width) {
        return visibleItems * itemHeight;
    }
}
