package WdPRiRLabs.Exercises.Ex2;

import java.lang.Math;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.awt.Color;
import java.awt.List;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import WdPRiRLabs.Exercises.Ex1.Complex;
import WdPRiRLabs.Exercises.Ex1.Viewer;

import javax.imageio.ImageIO;
import java.io.File;

/**
 * Mandelbrot set generation
 */
public class Mandelbrot {

    // Maximum magnitude during 'explosion' checks
    public static double magnitudeThreshold = 2.0;
    // Maximum number of steps to check 'explosion' of complex number
    public static int maxSteps = 200;

    // Required domain
    public static double xLimLower = -2.1;
    public static double xLimUpper = 0.6;
    public static double yLimLower = -1.2;
    public static double yLimUpper = 1.2;

    public static Complex domainLimitLower = new Complex(xLimLower, yLimLower);
    public static Complex domainLimitUpper = new Complex(xLimUpper, yLimUpper);

    // Resolution of the image in pixels
    public static int pixelsResolution = 1024;

    // colorMapper parameters
    public static double defaultPixelBrightness = 1.0;
    public static double defaultPixelSaturation = 1.0;

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
        return znComplex.times(znComplex).plus(cComplex);
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
            int pixelsWidth, int pixelsHeight,
            Complex domainLimitLower, Complex domainLimitUpper) {
        BufferedImage mandelbrotBufferedImage = new BufferedImage(
                pixelsWidth,
                pixelsHeight,
                BufferedImage.TYPE_INT_ARGB);

        Complex domainRange = domainLimitUpper.minus(domainLimitLower);
        double dx = Math.abs(domainRange.getRe()) / pixelsWidth;
        double dy = Math.abs(domainRange.getIm()) / pixelsHeight;

        Complex cComplex;
        int currentSteps;

        Color currentPixelColor;

        for (int yPixelAddr = 0; yPixelAddr < pixelsHeight; yPixelAddr++) {
            for (int xPixelAddr = 0; xPixelAddr < pixelsWidth; xPixelAddr++) {
                // System.out.printf("Pixel: (%d, %d)\r", xPixelAddr, yPixelAddr);
                cComplex = new Complex(
                        xLimLower + xPixelAddr * dx,
                        yLimLower + yPixelAddr * dy);
                currentSteps = mandelbrotStepsCount(cComplex);
                currentPixelColor = colorMapper(currentSteps);
                mandelbrotBufferedImage.setRGB(
                        xPixelAddr,
                        yPixelAddr,
                        currentPixelColor.getRGB());
            }
        }
        return mandelbrotBufferedImage;
    }

    public static double[] square(double[] values) {
        double[] squared = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            squared[i] = values[i] * values[i];
        }
        return squared;
    }

    public static double sum(double[] values) {
        double sumValue = 0;
        for (double value : values) {
            sumValue += value;
        }
        return sumValue;
    }

    public static double[] singleBenchmark(int imageResolution, int populationSize) {
        /**
         * Returns array of doubles containing execution times in seconds
         */
        double[] generationTimes = new double[populationSize];
        for (int i = 0; i < populationSize; i++) {
            long startNanoTime = System.nanoTime();
            getMandelbrotBufferredImage(
                    imageResolution, imageResolution,
                    domainLimitLower, domainLimitUpper);
            long endNanoTime = System.nanoTime();
            generationTimes[i] = (endNanoTime - startNanoTime) / 1.0e+9;
        }
        return generationTimes;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Starting Mandelbrot calculations");
        int populationSize = 1;
        // BufferedWriter writer = new BufferedWriter(
        //     new FileWriter("./mandelbrot_seq_"+Integer.toString(populationSize)+".csv", true)
        // );
        
        // int[] imageResolutions = {32, 64, 128, 256, 512, 1024, 2048, 4096, 8192};
        // System.out.println("Resolution\tMean\tStdDev");
        // writer.write("Resolution\tMean\tStdDev");
        // writer.newLine();
        // long startNanoTime = System.nanoTime();
        // for (int imgRes : imageResolutions) {
        //     double[] generationTimes = singleBenchmark(imgRes, populationSize);
        //     double mean = sum(generationTimes)/generationTimes.length;
        //     double stddev = Math.sqrt(sum(square(generationTimes))/generationTimes.length - mean*mean);
        //     String line = String.format("%d\t%f\t%f", imgRes, mean, stddev);
        //     writer.write(line);
        //     writer.newLine();
        //     System.out.println(line);
        // }
        // long endNanoTime = System.nanoTime();
        // System.out.printf("Images generated in: %fs\n", (endNanoTime-startNanoTime)/1.0e+9);
        // writer.close();

        //TODO: multithreading
        // Generate single images
        int[] imageResolutions = {1024};
        BufferedImage mandelbrotBufferedImage = null;
        for (int imgRes : imageResolutions) {
            mandelbrotBufferedImage = getMandelbrotBufferredImage(
                imgRes, imgRes,
                domainLimitLower, domainLimitUpper
            );
        }
        // Show the last picture as a window
        Viewer mainWindow = new Viewer(960, 960);
        mainWindow.displayBufferedImage(mandelbrotBufferedImage);
    }
}