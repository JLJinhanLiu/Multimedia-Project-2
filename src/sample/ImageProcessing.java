package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ImageProcessing {

    public static String filepath;

    private static WritableImage writableImage;
    private static PixelReader pixelReader;
    private static PixelWriter pixelWriter;

    // for dithering
    private static final int ditherSize = 4;    // can change between 4 and 2
    private static final int divisor = 256 / (ditherSize * ditherSize + 1);
    private static final int[][] matrixMap = {{0, 8, 2, 10},{12, 4, 14, 6},{3, 11, 1, 9},{15, 7, 13, 5}};   // for 4x4
//    private static final int[][] matrixMap = {{0, 2},{3, 1}};                                               // for 2x2

    // for auto leveling
    private static final int threshold = 200;       // threshold that determines what brackets to throw out
    private static final int discardBracket = 100;  // set how many brackets the program can throw out

    private static void writeToFile(String properties){
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null),
                    "png", new File(String.join("", filepath + properties + ".png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static WritableImage greyscaleImage(Image input){
        writableImage = new WritableImage((int) input.getWidth(), (int) input.getHeight());
        pixelReader = input.getPixelReader();
        pixelWriter = writableImage.getPixelWriter();

        // read the RGB value for the old image
        // paint the calculated brightness value to writeableImage
        for (int i = 0; i < input.getHeight(); i++) {
            for (int j = 0; j < input.getWidth(); j++) {
                pixelWriter.setColor(j, i, Color.grayRgb(
                        (int) (((double)((pixelReader.getArgb(j, i) << 8) >>> 24) * 0.299 +     //R
                                (double)((pixelReader.getArgb(j, i) << 16) >>> 24) * 0.587 +    //G
                                (double)((pixelReader.getArgb(j, i) << 24) >>> 24) * 0.114)     //B
                                )));
            }
        }

        writeToFile("greyscale");

        return writableImage;
    }

    public static WritableImage ditherImage(Image input) {
        writableImage = new WritableImage((int)input.getWidth(), (int)input.getHeight());
        pixelReader = input.getPixelReader();
        pixelWriter = writableImage.getPixelWriter();

        // read the brightness value for the old image
        for (int i = 0; i < input.getHeight(); i++) {
            for (int j = 0; j < input.getWidth(); j++) {
                // only need one of the RGB values as all are the same
                // normalize and compare to matrixMap
                if (matrixMap[i % ditherSize][j % ditherSize] >= ((pixelReader.getArgb(j, i) << 24) >>> 24) / divisor){
                    pixelWriter.setColor(j, i, Color.BLACK);
                } else {
                    pixelWriter.setColor(j, i, Color.WHITE);
                }
            }
        }

        writeToFile("dither");

        return writableImage;
    }

    public static WritableImage correctImage(Image input){
        writableImage = new WritableImage((int)input.getWidth(), (int)input.getHeight());
        pixelReader = input.getPixelReader();
        pixelWriter = writableImage.getPixelWriter();
        int readValue, R, G, B;
        int[] lowerBound = {0, 0, 0}, upperBound = {255, 255, 255}, discarded = {0, 0, 0};
        int[][] pixelProperties = new int[3][256];        // get numbers of pixels in [0..255] R, G, B values
        double doubleR, doubleG, doubleB;
        double[] scaling = new double[3];

        for (int i = 0; i < input.getHeight(); i++) {
            for (int j = 0; j < input.getWidth(); j++) {
                readValue = pixelReader.getArgb(j, i);
                pixelProperties[0][(readValue << 8) >>> 24] ++;     //R
                pixelProperties[1][(readValue << 16) >>> 24] ++;    //G
                pixelProperties[2][(readValue << 24) >>> 24] ++;    //B
            }
        }

        // find the bracket where numbers of R, G and B pixels are past the threshold
        // i = R, G or B
        // j = brackets of pixel intensities
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 255/2; j++){
                if (lowerBound[i] == 0){
                    if (pixelProperties[i][j] > threshold){
                        lowerBound[i] = j;
                    }
                    discarded[i]++;
                }
                if (upperBound[i] == 255){
                    if (pixelProperties[i][255 - j] > threshold){
                        upperBound[i] = 255 - j;
                    }
                    discarded[i]++;
                }
                if (discarded[i] >= discardBracket){
                    if (lowerBound[i] == 0){
                        lowerBound[i] = j;
                    }
                    if (upperBound[i] == 255){
                        upperBound[i] = 255 - j;
                    }
                    break;
                } else if (lowerBound[i] != 0 && upperBound[i] != 255){
                    break;
                }
            }
            // find the scaling factor for the channel
            scaling[i] = 255 / ((double)upperBound[i] - (double)lowerBound[i]);
        }

        // read value from the old image
        // apply scaling to each RGB channel
        for (int i = 0; i < input.getHeight(); i++) {
            for (int j = 0; j < input.getWidth(); j++) {
                readValue = pixelReader.getArgb(j, i);
                R = (readValue << 8) >>> 24;
                G = (readValue << 16) >>> 24;
                B = (readValue << 24) >>> 24;

                if (R >= upperBound[0] || R <= lowerBound[0]){
                    doubleR = R / upperBound[0];
                } else {
                    doubleR = ((double)R - (double)lowerBound[0]) * scaling[0] / 255;
                }
                if (G >= upperBound[1] || G <= lowerBound[1]){
                    doubleG = G / upperBound[1];
                } else {
                    doubleG = ((double)G - (double)lowerBound[1]) * scaling[1] / 255;
                }
                if (B >= upperBound[2] || B <= lowerBound[2]){
                    doubleB = B / upperBound[2];
                } else {
                    doubleB = ((double)B - (double)lowerBound[2]) * scaling[2] / 255;
                }

                pixelWriter.setColor(j, i, new Color(doubleR, doubleG, doubleB, 1));
                }
            }

        writeToFile("autoLevel");

        return writableImage;
    }
}
