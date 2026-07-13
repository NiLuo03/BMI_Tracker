package com.bmitracker.util;

import javafx.animation.AnimationTimer;
import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParticleTextCanvas extends Canvas {

    private final List<Particle> particles = new ArrayList<>();
    private final GraphicsContext gc;
    private AnimationTimer timer;
    private final String[] words;
    private int wordIndex = 0;
    private Color currentColor;
    private int frameCount = 0;

    private static class Particle {
        double x, y, targetX, targetY, vx, vy;
        Color startColor = Color.BLACK, targetColor = Color.BLACK;
        double colorWeight, colorBlendRate, size = 4, maxSpeed = 3;
        boolean dead;

        void update() {
            double dx = targetX - x;
            double dy = targetY - y;
            double dist = Math.sqrt(dx * dx + dy * dy) + 0.001;
            double mult = dist < 80 ? dist / 80 : 1;
            double fx = (dx / dist) * maxSpeed * mult - vx;
            double fy = (dy / dist) * maxSpeed * mult - vy;
            vx += fx * 0.04;
            vy += fy * 0.04;
            x += vx;
            y += vy;
        }

        void draw(GraphicsContext gc) {
            if (colorWeight < 1) colorWeight = Math.min(colorWeight + colorBlendRate, 1);
            double r = startColor.getRed() + (targetColor.getRed() - startColor.getRed()) * colorWeight;
            double g = startColor.getGreen() + (targetColor.getGreen() - startColor.getGreen()) * colorWeight;
            double b = startColor.getBlue() + (targetColor.getBlue() - startColor.getBlue()) * colorWeight;
            gc.setFill(Color.color(r, g, b, 0.9));
            gc.fillOval(x - size / 2, y - size / 2, size, size);
        }

        void kill(double w, double h) {
            if (dead) return;
            dead = true;
            startColor = Color.color(
                startColor.getRed() + (targetColor.getRed() - startColor.getRed()) * colorWeight,
                startColor.getGreen() + (targetColor.getGreen() - startColor.getGreen()) * colorWeight,
                startColor.getBlue() + (targetColor.getBlue() - startColor.getBlue()) * colorWeight);
            targetColor = Color.BLACK;
            colorWeight = 0;
            targetX = x + (Math.random() - 0.5) * w * 2;
            targetY = y + (Math.random() - 0.5) * h * 2;
        }
    }

    public ParticleTextCanvas(double width, double height, String[] words) {
        super(width, height);
        this.words = words;
        this.gc = getGraphicsContext2D();
        this.currentColor = randomColor();
        startAnimation();
    }

    private void startAnimation() {
        nextWord(words[0]);
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                frameCount++;
                gc.setFill(Color.color(0.04, 0.04, 0.06, 0.08));
                gc.fillRect(0, 0, getWidth(), getHeight());

                for (int i = particles.size() - 1; i >= 0; i--) {
                    Particle p = particles.get(i);
                    p.update();
                    p.draw(gc);
                    if (p.dead && (p.x < -100 || p.x > getWidth() + 100
                            || p.y < -100 || p.y > getHeight() + 100)) {
                        particles.remove(i);
                    }
                }

                if (frameCount % 280 == 0) {
                    wordIndex = (wordIndex + 1) % words.length;
                    nextWord(words[wordIndex]);
                }
            }
        };
        timer.start();
    }

    private void nextWord(String word) {
        Canvas offCanvas = new Canvas(getWidth(), getHeight());
        GraphicsContext ctx = offCanvas.getGraphicsContext2D();
        ctx.setFill(Color.WHITE);
        ctx.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 140));
        ctx.setTextAlign(TextAlignment.CENTER);
        ctx.setTextBaseline(VPos.CENTER);
        ctx.fillText(word, getWidth() / 2, getHeight() * 0.25);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage snapshot = offCanvas.snapshot(params, null);
        PixelReader reader = snapshot.getPixelReader();

        int step = 4;
        currentColor = randomColor();

        for (Particle p : particles) p.kill(getWidth(), getHeight());

        List<int[]> coords = new ArrayList<>();
        for (int y = 0; y < getHeight(); y += step) {
            for (int x = 0; x < getWidth(); x += step) {
                if (reader.getColor(x, y).getOpacity() > 0.2) {
                    coords.add(new int[]{x, y});
                }
            }
        }
        Collections.shuffle(coords);

        int reuse = 0;
        for (int[] c : coords) {
            Particle p;
            if (reuse < particles.size()) {
                p = particles.get(reuse);
                p.dead = false;
                reuse++;
            } else {
                p = new Particle();
                p.x = getWidth() / 2 + (Math.random() - 0.5) * 300;
                p.y = getHeight() / 2 + (Math.random() - 0.5) * 200;
                p.maxSpeed = Math.random() * 3 + 2;
                p.size = Math.random() * 3 + 2;
                p.colorBlendRate = Math.random() * 0.01 + 0.002;
                particles.add(p);
            }
            p.startColor = Color.color(
                p.startColor.getRed() + (p.targetColor.getRed() - p.startColor.getRed()) * p.colorWeight,
                p.startColor.getGreen() + (p.targetColor.getGreen() - p.startColor.getGreen()) * p.colorWeight,
                p.startColor.getBlue() + (p.targetColor.getBlue() - p.startColor.getBlue()) * p.colorWeight);
            p.targetColor = currentColor;
            p.colorWeight = 0;
            p.targetX = c[0];
            p.targetY = c[1];
        }

        for (int i = reuse; i < particles.size(); i++) {
            particles.get(i).kill(getWidth(), getHeight());
        }
    }

    private Color randomColor() {
        double hue = Math.random() * 360;
        return Color.hsb(hue, 0.55, 0.92);
    }

    public void stop() {
        if (timer != null) timer.stop();
    }
}
