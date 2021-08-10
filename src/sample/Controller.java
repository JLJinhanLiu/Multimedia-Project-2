package sample;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Controller {

    private boolean audioFile, imageFile;
    private int imageStage;
    private File file;
    private WaveVisualization waveVisualization;

    //image & effects
    private Image image;

    private Reflection reflection = new Reflection();

    //FXML
    @FXML private BorderPane borderPane;
    @FXML private HBox hBoxImage;
    @FXML private HBox hBoxText;
    @FXML private Label directoryText;
    @FXML private Label imageBeforeText;
    @FXML private Label imageAfterText;
    @FXML private Label imageStateText;
    @FXML private ImageView imageBefore = new ImageView();
    @FXML private ImageView imageAfter = new ImageView();
    @FXML private BarChart<Integer, Double> barChart;

    public Controller(){
        audioFile = false;
        imageFile = false;
        waveVisualization = new WaveVisualization(800, 200);
        imageStage = 0;
        reflection.setBottomOpacity(1);
        reflection.setTopOpacity(1);
        reflection.setTopOffset(-28);
        reflection.setFraction(0.85);
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
//        waveVisualization.setOpacity(isAudioFile ? 100 : 0);
        barChart.setOpacity(isAudioFile? 1 : 0);
        barChart.setEffect(reflection);
        barChart.getData().clear();

        // pre-process images
        if (isImageFile){
            imageBefore.setImage(image);
            imageAfter.setImage(ImageProcessing.quantization(image));
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
//            borderPane.setCenter(waveVisualization);
            waveVisualization.getWaveService().startService(
                    String.valueOf(file.getAbsoluteFile()), WaveFormService.WaveFormJob.AMPLITUDES_AND_WAVEFORM);
        }
    }

    public void generateGraph(){
        if (audioFile){
            barChart.getData().clear(); // clear barchart

            // grab read waveform data from API and convert into chart data
            float[] waveformData = WaveFormService.getResultingWaveform();
            System.out.println(waveformData.length);
            XYChart.Series series = new XYChart.Series();
            for (int i = 0; i < waveformData.length; i++){
                series.getData().add(new XYChart.Data(String.valueOf(i), (double)waveformData[i]));
            }

            // apply dataset to chart
            barChart.getData().add(series);
        }
    }

    public void audioFade(){
        if (audioFile){
            float[] waveformData = WaveFormService.getResultingWaveform().clone();
            float midpoint = (float)waveformData.length / 2;
            float scale = 20 / midpoint;
            for (int i = 0; i <= midpoint; i++){
                double factor = Math.pow(10, (-20 + scale * i) / 20);
                waveformData[i] = (float) (waveformData[i] * factor);
                waveformData[waveformData.length - 1 - i]  = (float) (waveformData[waveformData.length - 1 - i]  * factor);
            }

            // I'm too lazy to refactor it, sorry
            barChart.getData().clear();
            XYChart.Series series = new XYChart.Series();
            for (int i = 0; i < waveformData.length; i++){
                series.getData().add(new XYChart.Data(String.valueOf(i), (double)waveformData[i]));
            }

            // apply dataset to chart
            barChart.getData().add(series);
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
