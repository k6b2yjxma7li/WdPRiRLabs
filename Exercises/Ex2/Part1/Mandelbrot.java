package WdPRiRLabs.Exercises.Ex2.Part1;

import java.lang.Math;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import WdPRiRLabs.Exercises.Ex2.Complex;
// import WdPRiRLabs.Exercises.Ex1.Viewer; //TODO: not needed for this part of the project

import javax.imageio.ImageIO;
import java.io.File;

/**
 * Mandelbrot set generation
 */
public class Mandelbrot {

    public static class BufferedImageBlockRenderer implements Runnable {
        private int xStartingPixelAddress;
        private int yStartingPixelAddress;
        private int blockPixelWidth;
        private int blockPixelHeight;
        private Complex pixelSize;
        private BufferedImage renderedBufferedImage;

        public BufferedImageBlockRenderer() {}

        public BufferedImageBlockRenderer(
            BufferedImage bufferedImage,
            int xStartingPixelAddress, int yStartingPixelAddress,
            int blockPixelWidth, int blockPixelHeight,
            Complex pixelSize
        ) {
            this.renderedBufferedImage = bufferedImage;
            this.xStartingPixelAddress = xStartingPixelAddress;
            this.yStartingPixelAddress = yStartingPixelAddress;
            this.blockPixelWidth = blockPixelWidth;
            this.blockPixelHeight = blockPixelHeight;
            this.pixelSize = pixelSize;


        }

        @Override
        public void run() {
            int xPixelAddr;
            int yPixelAddr;
            for (int xRelativePixelAddr = 0; xRelativePixelAddr < this.blockPixelWidth; xRelativePixelAddr++) {
                for (int yRelativePixelAddr = 0; yRelativePixelAddr < this.blockPixelHeight; yRelativePixelAddr++) {
                    xPixelAddr = xRelativePixelAddr+this.xStartingPixelAddress;
                    yPixelAddr = yRelativePixelAddr+this.yStartingPixelAddress;
                    this.renderedBufferedImage.setRGB(
                        xPixelAddr,
                        yPixelAddr,
                        getPixelColorInt(
                            this.pixelSize,
                            xPixelAddr,
                            yPixelAddr
                        )
                    );
                }
            }

        }
    
        
    }

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

    // Threads pool/array size
    public static int threadsCount = Runtime.getRuntime().availableProcessors();

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

    public static int getPixelColorInt(Complex pixelSize, int xPixelAddress, int yPixelAddress) {
        /*
         * Acquires color value of the pixel at specified x and y addresses and of specified size (Complex)
         */
        Complex cComplex;
        double dx = pixelSize.getRe();
        double dy = pixelSize.getIm();
        cComplex = new Complex(
            xLimLower + xPixelAddress * dx,
            yLimLower + yPixelAddress * dy
        );
        return colorMapper(
            mandelbrotStepsCount(
                cComplex
            )
        ).getRGB();
    }

    public static BufferedImage getMandelbrotBufferredImage(
            int pixelsWidth, int pixelsHeight,
            Complex domainLimitLower, Complex domainLimitUpper) throws InterruptedException {
        /*
         * Calculates Mandelbrot set shape pixel by pixel
         */
        BufferedImage mandelbrotBufferedImage = new BufferedImage(
                pixelsWidth,
                pixelsHeight,
                BufferedImage.TYPE_INT_ARGB
        );

        Complex domainRange = domainLimitUpper.minus(domainLimitLower);
        Complex pixelSize = new Complex(
            Math.abs(domainRange.getRe()) / pixelsWidth,
            Math.abs(domainRange.getIm()) / pixelsHeight
        );

        int blockPixelWidth;
        int residualPixelWidth;
        // image width may not be divisible by threads count
        residualPixelWidth = pixelsWidth % threadsCount;
        blockPixelWidth = (pixelsWidth - residualPixelWidth)/threadsCount;

        Runnable[] bufferedImageBlockRenderer = new BufferedImageBlockRenderer[threadsCount];
        Thread[] threadsArray = new Thread[threadsCount];

        // initiating runnables and putting them into threads
        int currentBlockPixelWidth = blockPixelWidth;
        for (int threadNo = 0; threadNo < threadsCount; threadNo++) {
            if (threadNo == threadsCount-1) {
                // filling image with last block (potentially bigger than others)
                currentBlockPixelWidth += residualPixelWidth;
            }
            bufferedImageBlockRenderer[threadNo] = new BufferedImageBlockRenderer(
                mandelbrotBufferedImage,
                threadNo*blockPixelWidth, 0,
                currentBlockPixelWidth, pixelsHeight, pixelSize
            );
            threadsArray[threadNo] = new Thread(bufferedImageBlockRenderer[threadNo]);
        }

        // starting threads
        for (int threadNo = 0; threadNo < threadsCount; threadNo++) {
            threadsArray[threadNo].start();
        }

        // joining threads
        for (int threadNo = 0; threadNo < threadsCount; threadNo++) {
            threadsArray[threadNo].join();
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
            try {
                getMandelbrotBufferredImage(
                    imageResolution, imageResolution,
                    domainLimitLower, domainLimitUpper
                );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long endNanoTime = System.nanoTime();
            generationTimes[i] = (endNanoTime - startNanoTime) / 1.0e+9;
        }
        return generationTimes;
    }

    public static void main(String[] args) throws IOException {
        String classPath = "./WdPRiRLabs/Exercises/Ex2/Part1/";
        System.out.println("Starting Mandelbrot calculations");
        int populationSize = 100;
        BufferedWriter writer = new BufferedWriter(
            new FileWriter(classPath+"mandelbrot_par_"+Integer.toString(populationSize)+".csv", true)
        );
        
        int[] imageResolutions = {32, 64, 128, 256, 512, 1024, 2048, 4096, 8192};
        System.out.println("Resolution\tMean\tStdDev");
        writer.write("Resolution\tMean\tStdDev");
        writer.newLine();
        long startNanoTime = System.nanoTime();
        for (int imgRes : imageResolutions) {
            double[] generationTimes = singleBenchmark(imgRes, populationSize);
            double mean = sum(generationTimes)/generationTimes.length;
            double stddev = Math.sqrt(sum(square(generationTimes))/generationTimes.length - mean*mean);
            String line = String.format("%d\t%f\t%f", imgRes, mean, stddev);
            writer.write(line);
            writer.newLine();
            System.out.println(line);
        }
        long endNanoTime = System.nanoTime();
        System.out.printf("Images generated in: %fs\n", (endNanoTime-startNanoTime)/1.0e+9);
        writer.close();
        // // Show the last picture as a window
        // Viewer mainWindow = new Viewer(960, 960);
        // BufferedImage mandelbrotBufferedImage;
        // try {
        //     mandelbrotBufferedImage = getMandelbrotBufferredImage(imageResolutions[0], imageResolutions[0], domainLimitLower, domainLimitUpper);
        //     mainWindow.displayBufferedImage(mandelbrotBufferedImage);
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // }
    }
}