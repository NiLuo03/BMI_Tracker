package com.bmitracker.util;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class ParticleCanvas extends Canvas {

    private final List<Ambient> ambients = new ArrayList<>();
    private final GraphicsContext gc;
    private AnimationTimer timer;
    private int frameCount = 0;

    private static class Ambient {
        double x, y, vx, vy, size, alpha, hue, phase, baseX, baseY;
    }

    public ParticleCanvas(double width, double height) {
        super(width, height);
        this.gc = getGraphicsContext2D();
        initAmbients();
        startAnimation();
    }

    private void initAmbients() {
        for (int i = 0; i < 60; i++) {
            Ambient a = new Ambient();
            a.x = Math.random() * getWidth();
            a.y = Math.random() * getHeight();
            a.baseX = a.x;
            a.baseY = a.y;
            a.vx = (Math.random() - 0.5) * 0.4;
            a.vy = (Math.random() - 0.5) * 0.4;
            a.size = Math.random() * 2 + 1;
            a.alpha = Math.random() * 0.25 + 0.08;
            a.hue = Math.random() * 360;
            a.phase = Math.random() * Math.PI * 2;
            ambients.add(a);
        }
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                frameCount++;
                gc.setFill(Color.color(0.04, 0.04, 0.06, 0.08));
                gc.fillRect(0, 0, getWidth(), getHeight());

                for (Ambient a : ambients) {
                    a.x = a.baseX + Math.sin(frameCount * 0.02 + a.phase) * 30;
                    a.y = a.baseY + Math.cos(frameCount * 0.025 + a.phase) * 20;
                    a.baseX += a.vx;
                    a.baseY += a.vy;
                    if (a.baseX < -20) a.baseX = getWidth() + 20;
                    if (a.baseX > getWidth() + 20) a.baseX = -20;
                    if (a.baseY < -20) a.baseY = getHeight() + 20;
                    if (a.baseY > getHeight() + 20) a.baseY = -20;
                    gc.setFill(Color.hsb(a.hue, 0.4, 0.85, a.alpha));
                    gc.fillOval(a.x - a.size / 2, a.y - a.size / 2, a.size, a.size);
                }
            }
        };
        timer.start();
    }

    public void stop() {
        if (timer != null) timer.stop();
    }
}
