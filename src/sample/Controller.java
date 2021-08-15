package sample;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Controller {

    private boolean audioFile, imageFile;
    private File file;
    private Image image;

    //FXML
    @FXML private HBox hBoxImage;
    @FXML private HBox hBoxText;
    @FXML private Label directoryText;
    @FXML private Label compressionInfo;
    @FXML private ImageView imageBefore = new ImageView();
    @FXML private ImageView imageAfter = new ImageView();

    public Controller(){
        audioFile = false;
        imageFile = false;
    }

    // set up interface after file load - for both audio & image files
    public void onFileSuccess(boolean isAudioFile, boolean isImageFile){
        ImageProcessing.filepath = file.getAbsolutePath();
        directoryText.setText(
                String.join(":", "File opened", String.valueOf(file.getAbsoluteFile())));
        audioFile = isAudioFile;
        imageFile = isImageFile;

        // hide unnecessary interface elements
        hBoxText.setOpacity(isImageFile ? 100 : 0);
        hBoxImage.setOpacity(isImageFile ? 100 : 0);
        compressionInfo.setOpacity(isImageFile ? 0 : 100);

        // pre-process images
        if (imageFile){
            imageBefore.setImage(image);
            imageAfter.setImage(ImageProcessing.convert(image));
        }
    }

    public void openFileAudio(MouseEvent event){
        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("wav File", "*.wav"));
        fileChooser.setTitle("Open File...");
        file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            onFileSuccess(true, false);
            AudioProcessing.compress(file);
            compressionInfo.setText(AudioProcessing.getRatio(file));
        }
    }

    public void openFileImage(MouseEvent event) {
        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("bmp File", "*.bmp"));
        fileChooser.setTitle("Open File...");
        file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            image = new Image(file.toURI().toString());
            onFileSuccess(false, true);
        }
    }

    public void quit(){
        System.exit(0);
    }

}
