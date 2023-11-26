package WdPRiRLabs.Exercises.Ex1;

import java.lang.Math;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import WdPRiRLabs.Exercises.Ex1.Complex;

/**
 * Mandelbrot
 */
public class Mandelbrot {

    public static double magnitudeThreshold = 2.0;
    public static int maxSteps = 200;

    public static double xLimLower = -2.1;
    public static double xLimUpper = 0.6;
    public static double yLimLower = -1.2;
    public static double yLimUpper = 1.2;

    public static Complex domainLimitLower = new Complex(xLimLower, yLimLower);
    public static Complex domainLimitUpper = new Complex(xLimUpper, yLimUpper);

    public static int pixelsResolution = 961;

    public static double defaultPixelBrightness = 1.0;
    public static double defaultPixelSaturation = 1.0;

    public static void displayBufferedImage(BufferedImage myBufferedImage) {
        JFrame myJFrame = null;
        JLabel myJLabel = null;
        myJFrame = new JFrame();
        myJFrame.setTitle("Mandelbrot");
        myJFrame.setSize(480, 640);
        myJFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        myJLabel = new JLabel();
        myJLabel.setIcon(new ImageIcon(myBufferedImage));
        myJFrame.getContentPane().add(myJLabel, BorderLayout.CENTER);
        myJFrame.setLocationRelativeTo(null);
        myJFrame.pack();
        myJFrame.setVisible(true);
    }

    public static Color colorMapper(int myInt) {
        double pixelBrightness = defaultPixelBrightness;
        double hue = Math.pow(myInt, 1. / 3.) / Math.pow(maxSteps, 1. / 3.);
        if (myInt == maxSteps) {
            pixelBrightness = 0.0f;
        }
        return Color.getHSBColor(
                1 - (float) hue,
                (float) defaultPixelSaturation,
                (float) pixelBrightness);
    }

    public static Complex mandelbrotStep(Complex znComplex, Complex cComplex) {
        return znComplex.times(znComplex).minus(cComplex);
    }

    public static int mandelbrotStepsCount(Complex cComplex) {
        Complex zComplex = new Complex(0.0, 0.0);
        int i = 0;
        for (i = 0; (i < maxSteps) && (zComplex.mag() < magnitudeThreshold); i++) {
            zComplex = mandelbrotStep(zComplex, cComplex);
            // System.out.printf("%.3f+i%.3f\n", zComplex.getRe(), zComplex.getIm());
        }
        return i;
    }

    public static BufferedImage getMandelbrotBufferredImage(
        int pixelWidth, int pixelHeight,
        Complex domainLimitLower, Complex domainLimitUpper) {
        BufferedImage mandelbrotBufferedImage = new BufferedImage(
            pixelWidth,
            pixelHeight,
            BufferedImage.TYPE_INT_ARGB
        );

        Complex domainRange = domainLimitUpper.minus(domainLimitLower);
        double dx = Math.abs(domainRange.getRe())/pixelWidth;
        double dy = Math.abs(domainRange.getIm())/pixelHeight;

        System.out.printf("(dx, dy) = (%f, %f)", dx, dy);

        Complex cComplex;
        int currentSteps;

        Color currentPixelColor;

        for ( int yPixelAddr = 0; yPixelAddr < pixelsResolution; yPixelAddr++) {
            for ( int xPixelAddr = 0; xPixelAddr < pixelsResolution; xPixelAddr++) {
                // System.out.printf("Pixel: (%d, %d)\r", xPixelAddr, yPixelAddr);
                cComplex = new Complex(
                    xLimLower + xPixelAddr*dx,
                    yLimLower + yPixelAddr*dy
                );
                currentSteps = mandelbrotStepsCount(cComplex);
                currentPixelColor = colorMapper(currentSteps);
                mandelbrotBufferedImage.setRGB(
                    xPixelAddr,
                    yPixelAddr,
                    currentPixelColor.getRGB()
                );
            }
        }
        return mandelbrotBufferedImage;
    }

    // public static long[] 
    public static void main(String[] args) {
        System.out.println("Starting Mandelbrot calculations");
        long startNanoTime = System.nanoTime();
        BufferedImage mandelbrotBufferedImage = getMandelbrotBufferredImage(
            pixelsResolution, pixelsResolution,
            domainLimitLower, domainLimitUpper
        );
        displayBufferedImage(mandelbrotBufferedImage);
        long endNanoTime = System.nanoTime();
        System.out.printf("Image %d x %d generated in: %fs\n", pixelsResolution, pixelsResolution, (endNanoTime-startNanoTime)/1.0e+9);
    }
}