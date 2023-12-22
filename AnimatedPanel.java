package minesweeper_java;

import javax.swing.*;
import java.awt.*;

public abstract class AnimatedPanel extends JPanel implements Runnable {
    private final int WIDTH;
    private final int HEIGHT;
    private int delay;
    private double targetFrameRate;
    public float frameRate;

    public AnimatedPanel(int width, int height, double frameRate) {
        this.WIDTH = width;
        this.HEIGHT = height;
        this.targetFrameRate = frameRate;
        this.delay = calcFrameRate(targetFrameRate);
        initPanel();
    }

    private int calcFrameRate(double frameRate) {
        if (!Double.isNaN(frameRate)) this.frameRate = (float) frameRate;
        else this.frameRate = 60f;
        return (int) ((1d / frameRate) * 1000d);
    }

    public void setTargetFrameRate(double frameRate) {
        targetFrameRate = frameRate;
        this.delay = calcFrameRate(frameRate);
    }

    private void initPanel() {
        this.setBackground(new Color(0x04510A));
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Thread animator = new Thread(this);
        animator.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        //this.setBackground(Color.green);
        draw(g);
    }

    public abstract void draw(Graphics g);

    //public Point po;
    private long last = 0;
    @Override
    public void run() {
        long beforeTime,timeDiff,sleep;
        beforeTime = System.currentTimeMillis();
        while(true) {
            //po = MouseInfo.getPointerInfo().getLocation();
            this.repaint();
            avgFrameRate(beforeTime, last);

            timeDiff = System.currentTimeMillis() - beforeTime;
            sleep = delay - timeDiff;
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            last = beforeTime;
            beforeTime = System.currentTimeMillis();
        }
    }

    private void avgFrameRate(long now, long last) {
        double frameTimeSecs = (now - last) / 1e9;
        double avgFrameTimeSecs = 1d / frameRate;
        final double ALPHA = 0.05;
        avgFrameTimeSecs = (1.0 - ALPHA) * avgFrameTimeSecs + ALPHA * frameTimeSecs;
        frameRate = (float) (1d / avgFrameTimeSecs);
    }
}
