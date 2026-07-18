package com.bmitracker.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

public class WheelPicker<T> extends Region {

    private static final double STIFFNESS = 300;
    private static final double DAMPING = 28;

    private final ObservableList<T> items = FXCollections.observableArrayList();
    private final List<Label> itemLabels = new ArrayList<>();
    private final Pane contentPane = new Pane();
    private final Rectangle topOverlay = new Rectangle();
    private final Rectangle bottomOverlay = new Rectangle();
    private final Rectangle centerBar = new Rectangle();

    private double itemHeight = 36;
    private int visibleItems = 5;
    private double currentOffset = 0;
    private double viewHeight;

    private final IntegerProperty selectedIndex = new SimpleIntegerProperty(-1);
    private final ObjectProperty<T> value = new SimpleObjectProperty<>();

    private double dragStartY;
    private double dragStartOffset;
    private boolean dragging = false;

    private AnimationTimer snapTimer;
    private double snapVel, snapPos, snapTarget;
    private long snapLastTime;

    public WheelPicker() {
        getStyleClass().add("wheel-picker");
        contentPane.setPickOnBounds(false);
        getChildren().add(contentPane);
        centerBar.setMouseTransparent(true);
        getChildren().add(centerBar);
        topOverlay.setMouseTransparent(true);
        bottomOverlay.setMouseTransparent(true);
        getChildren().addAll(topOverlay, bottomOverlay);

        selectedIndex.addListener((obs, old, idx) -> {
            int i = idx.intValue();
            if (i >= 0 && i < items.size()) value.set(items.get(i));
        });

        setOnMousePressed(e -> {
            requestFocus();
            cancelSnap();
            dragging = true;
            dragStartY = e.getY();
            dragStartOffset = currentOffset;
        });

        setOnMouseDragged(e -> {
            if (!dragging) return;
            double dy = e.getY() - dragStartY;
            double raw = dragStartOffset + dy;
            currentOffset = clampOffset(raw);
            contentPane.setTranslateY(currentOffset);
            refreshItemTransforms();
        });

        setOnMouseReleased(e -> {
            if (!dragging) return;
            dragging = false;
            snapToClosest();
        });

        setOnScroll(e -> {
            int dir = e.getDeltaY() > 0 ? -1 : 1;
            int idx = selectedIndex.get() + dir;
            idx = Math.max(0, Math.min(items.size() - 1, idx));
            snapToIndex(idx);
        });

        setFocusTraversable(true);
        setOnKeyPressed(e -> {
            int idx = selectedIndex.get();
            switch (e.getCode()) {
                case UP: case LEFT:
                    idx = Math.max(0, idx - 1); snapToIndex(idx); break;
                case DOWN: case RIGHT:
                    idx = Math.min(items.size() - 1, idx + 1); snapToIndex(idx); break;
            }
        });
    }

    public ObservableList<T> getItems() { return items; }

    public void setItems(List<T> itemList) {
        items.setAll(itemList);
        rebuildItems();
        if (!items.isEmpty()) snapToIndex(0);
    }

    public int getSelectedIndex() { return selectedIndex.get(); }
    public IntegerProperty selectedIndexProperty() { return selectedIndex; }

    public T getValue() { return value.get(); }
    public ObjectProperty<T> valueProperty() { return value; }

    public void setValue(T val) {
        int idx = items.indexOf(val);
        if (idx >= 0) snapToIndex(idx);
    }

    public double getItemHeight() { return itemHeight; }
    public void setItemHeight(double h) { itemHeight = h; centerBar.setHeight(h); requestLayout(); }

    public int getVisibleItems() { return visibleItems; }
    public void setVisibleItems(int n) { visibleItems = n; requestLayout(); }

    private double snapOffsetForIndex(int idx) {
        return viewHeight / 2.0 - idx * itemHeight - itemHeight / 2.0;
    }

    private int indexForOffset(double offset) {
        double centerOff = viewHeight / 2.0 - itemHeight / 2.0;
        return (int) Math.round((centerOff - offset) / itemHeight);
    }

    private double clampOffset(double off) {
        double maxOff = snapOffsetForIndex(0);
        double minOff = snapOffsetForIndex(items.size() - 1);
        return Math.max(minOff, Math.min(maxOff, off));
    }

    private void rebuildItems() {
        contentPane.getChildren().clear();
        itemLabels.clear();
        for (T item : items) {
            Label lbl = new Label(String.valueOf(item));
            lbl.getStyleClass().add("wheel-item-label");
            lbl.setAlignment(Pos.CENTER);
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setMaxHeight(Double.MAX_VALUE);
            lbl.setFont(Font.font("Microsoft YaHei", 16));
            lbl.setTextFill(Color.rgb(255, 255, 255, 0.85));
            itemLabels.add(lbl);
            contentPane.getChildren().add(lbl);
        }
        requestLayout();
    }

    private void refreshItemTransforms() {
        double h = viewHeight;
        if (h <= 0) return;
        double halfRange = visibleItems * itemHeight / 2.0;
        for (int i = 0; i < itemLabels.size(); i++) {
            Label lbl = itemLabels.get(i);
            double itemCenterY = i * itemHeight + currentOffset + itemHeight / 2.0;
            double dist = Math.abs(itemCenterY - h / 2.0) / halfRange;
            dist = Math.min(dist, 1.0);
            lbl.setScaleX(1.0 - 0.3 * dist);
            lbl.setScaleY(1.0 - 0.3 * dist);
            lbl.setOpacity(1.0 - 0.7 * dist);
        }
    }

    private void snapToClosest() {
        if (items.isEmpty()) return;
        int idx = indexForOffset(currentOffset);
        idx = Math.max(0, Math.min(items.size() - 1, idx));
        snapToIndex(idx);
    }

    private void snapToIndex(int idx) {
        if (items.isEmpty()) return;
        idx = Math.max(0, Math.min(items.size() - 1, idx));

        if (viewHeight <= 0) {
            currentOffset = snapOffsetForIndex(idx);
            contentPane.setTranslateY(currentOffset);
            selectedIndex.set(idx);
            return;
        }

        cancelSnap();
        selectedIndex.set(idx);
        snapTarget = snapOffsetForIndex(idx);
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
                contentPane.setTranslateY(currentOffset);
                refreshItemTransforms();
                if (Math.abs(snapPos - snapTarget) < 0.5 && Math.abs(snapVel) < 5) {
                    currentOffset = snapTarget;
                    contentPane.setTranslateY(currentOffset);
                    refreshItemTransforms();
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

    @Override
    protected void layoutChildren() {
        viewHeight = visibleItems * itemHeight;
        double w = getWidth();
        double h = viewHeight;
        if (w <= 0 || h <= 0) return;

        contentPane.setClip(new Rectangle(w, h));
        for (int i = 0; i < itemLabels.size(); i++) {
            Label lbl = itemLabels.get(i);
            lbl.setLayoutX(0);
            lbl.setLayoutY(i * itemHeight);
            lbl.setPrefWidth(w);
            lbl.setPrefHeight(itemHeight);
        }
        currentOffset = clampOffset(currentOffset);
        contentPane.setTranslateY(currentOffset);
        refreshItemTransforms();

        topOverlay.setWidth(w);
        topOverlay.setHeight(h / 2.0);
        topOverlay.setX(0);
        topOverlay.setY(0);
        topOverlay.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(10, 10, 15, 0.95)),
                new Stop(1, Color.TRANSPARENT)));

        bottomOverlay.setWidth(w);
        bottomOverlay.setHeight(h / 2.0);
        bottomOverlay.setX(0);
        bottomOverlay.setY(h / 2.0);
        bottomOverlay.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(1, Color.rgb(10, 10, 15, 0.95))));

        centerBar.setWidth(w);
        centerBar.setX(0);
        centerBar.setY(h / 2.0 - itemHeight / 2.0);
        centerBar.setFill(Color.rgb(255, 255, 255, 0.06));
        centerBar.setStroke(Color.rgb(255, 255, 255, 0.12));
        centerBar.setStrokeWidth(0.5);
    }

    @Override
    protected double computePrefWidth(double height) { return 80; }

    @Override
    protected double computePrefHeight(double width) { return visibleItems * itemHeight; }
}
