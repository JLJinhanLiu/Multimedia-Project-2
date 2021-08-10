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

    public static Image quantization(Image image){


        return writableImage;
    }

    private static void writeToFile(String properties){
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null),
                    "png", new File(String.join("", filepath + properties + ".png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
