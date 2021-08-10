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

import static java.lang.Math.*;

public class ImageProcessing {

    public static String filepath;

    private static int ARGB;

    private static double[][] YMatrix = new double[8][8];
    private static double[][] UMatrix = new double[8][8];
    private static double[][] VMatrix = new double[8][8];
    private static double[][] DCTMatrix = new double[8][8];
    private static double[][] DCTMatrixT = new double[8][8];
    private static final double[][] QutMatrix = {
            {1,1,2,4,8,16,32,64},
            {1,1,2,4,8,16,32,64},
            {2,2,2,4,8,16,32,64},
            {4,4,4,4,8,16,32,64},
            {8,8,8,8,8,16,32,64},
            {16,16,16,16,16,16,32,64},
            {32,32,32,32,32,32,32,64},
            {64,64,64,64,64,64,64,64}
    };

    public static Image convert(Image input){
        WritableImage writableImage = new WritableImage((int) input.getWidth(), (int) input.getHeight());
        WritableImage quantizedImage = new WritableImage((int) input.getWidth(), (int) input.getHeight());
        PixelReader pixelReader = input.getPixelReader();
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        PixelWriter quantizedWriter = quantizedImage.getPixelWriter();
        calculateDCT();

        // read 8x8 block
        for (int i = 0; i < input.getHeight() / 8; i++){    // break into 8x1 height segments
            for (int j = 0; j < input.getWidth() / 8; j++){ // break into 1x8 width segments

                for (int k = 0; k < 8; k++){                // read pixels in the 8x8 segment
                    for (int l = 0; l < 8; l++){
                        ARGB = pixelReader.getArgb(8*j + k, 8*i + l);
                        convertYUV(k, l);     // convert to YUV and store in matrix
                    }
                }

                DCTTransform();                     // apply DCT transform to each 8x8 matrix
                quantization();                     // apply quantization

                for (int k = 0; k < 8; k++){                // write quantized result
                    for (int l = 0; l < 8; l++){
                        quantizedWriter.setColor(8*j + k, 8*i + l, convertRGB(k, l));
                    }
                }

                invDCTTransform();                  // convert quantized matrix back

                for (int k = 0; k < 8; k++){                // write pixels in the 8x8 segment
                    for (int l = 0; l < 8; l++){
                        // convert to RGB and write to image
                        pixelWriter.setColor(8*j + k, 8*i + l, convertRGB(k, l));
                    }
                }

            }
        }
        writeToFile("-compressed", writableImage);
        writeToFile("-quantized", quantizedImage);
        return writableImage;
    }

    private static void calculateDCT(){
        double value;
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                value = (i == 0 ? sqrt(1.0/8.0) : sqrt(2.0/8.0))
                        * cos(((2.0 * (double)j + 1.0) * (double)i * PI) / (2.0 * 8.0));
                DCTMatrix[i][j] = value;
                DCTMatrixT[j][i] = value;
            }
        }
    }

    private static double[][] abMultiply(double[][] X, double[][] Y){
        // adopted from programming assignment 3 C++ code
        // assuming multiplication is legal on X and Y, check skipped
        double temp = 0;
        double[][] tempVec = new double[8][8];
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                for (int k = 0; k < 8; k++){
                    temp += X[i][k] * Y[k][j];
                }
                tempVec[i][j] = temp;
                temp = 0;
            }
        }
        return tempVec;
    }

    private static void convertYUV(int x, int y) {
        // modified from programming assignment 1 and project 1 code
        double R,G,B;
        R = (ARGB << 8) >>> 24;
        G = (ARGB << 16) >>> 24;
        B = (ARGB << 24) >>> 24;
        YMatrix[x][y] = 0.299*R + 0.587*G + 0.114*B;
        UMatrix[x][y] = -0.299*R - 0.587*G + 0.886*B;
        VMatrix[x][y] = 0.701*R - 0.587*G - 0.114*B;
    }

    private static void DCTTransform(){
        YMatrix = abMultiply(abMultiply(DCTMatrix, YMatrix), DCTMatrixT);
        UMatrix = abMultiply(abMultiply(DCTMatrix, UMatrix), DCTMatrixT);
        VMatrix = abMultiply(abMultiply(DCTMatrix, VMatrix), DCTMatrixT);
    }

    private static void quantization(){
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                YMatrix[i][j] = round(YMatrix[i][j]/QutMatrix[i][j]);
                UMatrix[i][j] = round(UMatrix[i][j]/QutMatrix[i][j]);
                VMatrix[i][j] = round(VMatrix[i][j]/QutMatrix[i][j]);
            }
        }
    }

    private static void invDCTTransform(){
        YMatrix = abMultiply(abMultiply(DCTMatrixT, YMatrix), DCTMatrix);
        UMatrix = abMultiply(abMultiply(DCTMatrixT, UMatrix), DCTMatrix);
        VMatrix = abMultiply(abMultiply(DCTMatrixT, VMatrix), DCTMatrix);
    }

    private static Color convertRGB(int i, int j){
        double R,G,B;
        R = (YMatrix[i][j] + VMatrix[i][j]) / 256;
        // value acquired by inverting YUV matrix
        G = (YMatrix[i][j] + UMatrix[i][j]*-0.19420783645655877342 + -0.50936967632027257241*VMatrix[i][j]) / 256;
        B = (YMatrix[i][j] + UMatrix[i][j]) / 256;
        if (R > 1) { R = 1; }
        else if (R < 0) { R = 0; }
        if (G > 1) { G = 1; }
        else if (G < 0) { G = 0; }
        if (B > 1) { B = 1; }
        else if (B < 0) { B = 0; }
        return new Color(R, G, B,1);
    }

    private static void writeToFile(String properties, WritableImage writableImage){
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null),
                    "png", new File(String.join("", filepath + properties + ".png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
