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
    private final List<Ambient> ambients = new ArrayList<>();
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

    private static class Ambient {
        double x, y, vx, vy, size, alpha, hue, phase, baseX, baseY;
    }

    // 构造器：初始化粒子系统、环境光点、启动动画循环
    public ParticleTextCanvas(double width, double height, String[] words) {
        super(width, height);
        this.words = words;
        this.gc = getGraphicsContext2D();
        this.currentColor = randomColor();
        initAmbients();
        startAnimation();
    }

    // 生成 60 个随机环境光点，缓慢漂浮营造背景氛围
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
        // 动画周期：粒子聚合（converge）→ 驻留展示（hold）→ 400 帧后切词消散（scatter）
        nextWord(words[0]);
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // 帧循环：擦除画布 → 更新/绘制粒子 → 环境光点 → 定时切词
                frameCount++;
                gc.setFill(Color.color(0.04, 0.04, 0.06, 0.08));
                gc.fillRect(0, 0, getWidth(), getHeight());

                // 粒子物理更新 + 越界死亡粒子回收
                for (int i = particles.size() - 1; i >= 0; i--) {
                    Particle p = particles.get(i);
                    p.update();
                    p.draw(gc);
                    if (p.dead && (p.x < -100 || p.x > getWidth() + 100
                            || p.y < -100 || p.y > getHeight() + 100)) {
                        particles.remove(i);
                    }
                }

                // 环境点缀粒子持续游走
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

                // 每 400 帧切换下一个词，触发新一轮聚合 → 消散
                if (frameCount % 400 == 0) {
                    wordIndex = (wordIndex + 1) % words.length;
                    nextWord(words[wordIndex]);
                }
            }
        };
        timer.start();
    }

    // particle→word 映射：将文字渲染为像素坐标，每个非透明像素成为一个粒子的目标位置
    private void nextWord(String word) {
        Canvas offCanvas = new Canvas(getWidth(), getHeight());
        GraphicsContext ctx = offCanvas.getGraphicsContext2D();
        ctx.setFill(Color.WHITE);
        ctx.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 140));
        ctx.setTextAlign(TextAlignment.CENTER);
        ctx.setTextBaseline(VPos.CENTER);
        ctx.fillText(word, getWidth() / 2, getHeight() * 0.18);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage snapshot = offCanvas.snapshot(params, null);
        PixelReader reader = snapshot.getPixelReader();

        // 采样步长 4px，平衡渲染精度与性能
        int step = 4;
        // 旧粒子标记为消散，新粒子用新颜色
        currentColor = randomColor();

        for (Particle p : particles) p.kill(getWidth(), getHeight());

        // 逐像素扫描文字区域，收集不透明像素坐标作为粒子目标点（text partitioning）
        List<int[]> coords = new ArrayList<>();
        for (int y = 0; y < getHeight(); y += step) {
            for (int x = 0; x < getWidth(); x += step) {
                if (reader.getColor(x, y).getOpacity() > 0.2) {
                    coords.add(new int[]{x, y});
                }
            }
        }
        Collections.shuffle(coords);

        // 复用现有粒子或创建新粒子，设定新目标坐标与颜色
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

        // 多余粒子标记为消散
        for (int i = reuse; i < particles.size(); i++) {
            particles.get(i).kill(getWidth(), getHeight());
        }
    }

    // 生成下一轮粒子的随机鲜艳色
    private Color randomColor() {
        double hue = Math.random() * 360;
        return Color.hsb(hue, 0.55, 0.92);
    }

    public void stop() {
        if (timer != null) timer.stop();
    }
}
