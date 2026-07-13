package com.bmitracker.util;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class MeshGradientBackground extends Canvas {

    private final GraphicsContext gc;
    private AnimationTimer timer;
    private double time = 0;

    private static final Color[][] COLOR_SETS = {
        { Color.rgb(8, 70, 130), Color.rgb(100, 50, 140), Color.rgb(10, 140, 180), Color.rgb(30, 100, 130) },
        { Color.rgb(6, 50, 90), Color.rgb(80, 30, 120), Color.rgb(0, 100, 140), Color.rgb(50, 60, 100) },
    };

    public MeshGradientBackground(double width, double height) {
        super(width, height);
        this.gc = getGraphicsContext2D();
        startAnimation();
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                time += 0.003;
                render();
            }
        };
        timer.start();
    }

    private void render() {
        double w = getWidth();
        double h = getHeight();
        Color[] colors = COLOR_SETS[0];

        gc.clearRect(0, 0, w, h);

        renderBlob(w * (0.35 + Math.sin(time * 0.7) * 0.20), h * (0.40 + Math.cos(time * 0.8) * 0.25),
                w * 0.65, colors[0], 0.35);
        renderBlob(w * (0.60 + Math.cos(time * 0.6) * 0.15), h * (0.30 + Math.sin(time * 0.9) * 0.20),
                w * 0.55, colors[1], 0.30);
        renderBlob(w * (0.45 + Math.sin(time * 0.55) * 0.18), h * (0.55 + Math.cos(time * 0.7) * 0.22),
                w * 0.60, colors[2], 0.28);
        renderBlob(w * (0.50 + Math.cos(time * 0.65) * 0.12), h * (0.45 + Math.sin(time * 0.75) * 0.18),
                w * 0.50, colors[3], 0.25);
    }

    private void renderBlob(double cx, double cy, double radius, Color color, double alpha) {
        double r = color.getRed();
        double g = color.getGreen();
        double b = color.getBlue();
        Color transparent = Color.color(r, g, b, 0);

        gc.save();
        gc.setGlobalAlpha(alpha);
        gc.setFill(new javafx.scene.paint.RadialGradient(
                0, 0, cx / getWidth(), cy / getHeight(), radius / getWidth(),
                false, CycleMethod.NO_CYCLE,
                new Stop(0, color),
                new Stop(1, transparent)));
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.restore();
    }

    public void stop() {
        if (timer != null) timer.stop();
    }
}
