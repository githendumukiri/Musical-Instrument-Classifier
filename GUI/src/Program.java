import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;

/**
 * This class runs our KNN implementation with a graphical user interface.
 *
 * @author Githendu Mukiri
 * @author Mankaran Saggi
 * @version 1.0 2019-05-16
 *
 */
public class Program extends Application implements EventHandler<ActionEvent> {

    private Button button;
    private KNN test = new KNN();

    /**
     * @param args launches javaFX
     */
    public static void main(String[] args) {

        launch(args);
    }

    /**
     * @param primaryStage represents a window in a JavaFX desktop application
     * @throws Exception
     */
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setTitle("Musical Instrument Classifier");
        Label label = new Label("Hold your audio device up to this device's microphone");
        label.setStyle("-fx-text-fill: white");
        label.setId("bold-label");
        this.button = new Button();
        this.button.setText("Start Analyzing");
        this.button.setOnAction(this);
        button.setId("bold-label");
        //button.setOnAction(e -> PopupWindow.display("Starting to Analyze", "Analyzing Audio..."));

        Image image = new Image("image.jpg");
        ImageView size = new ImageView();
        size.setImage(image);
        size.setFitWidth(993);
        size.setFitHeight(534);
        StackPane imageViewer = new StackPane();
        imageViewer.getChildren().add(size);


        VBox topLabel = new VBox();
        topLabel.getChildren().add(label);
        topLabel.setAlignment(Pos.BASELINE_CENTER);

        StackPane buttonPos = new StackPane();
        buttonPos.getChildren().add(this.button);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(topLabel);
        borderPane.setBottom(buttonPos);
        borderPane.setCenter(imageViewer);

        Scene scene = new Scene(borderPane, 1500.0D, 800.0D);
        scene.getStylesheets().add("Theme.css");
        primaryStage.setScene(scene);
        primaryStage.show();

        File trainData = new File("Train Data");
        test.train(trainData);
    }

    /**
     * @param event behavior for analyze audio button
     */
    public void handle(ActionEvent event) {
        if (event.getSource() == this.button) {

            Label label = new Label("Analyzing Audio...");
            label.setStyle("-fx-text-fill: white");
            label.setId("bold-label");

            VBox processLabel = new VBox();
            processLabel.getChildren().add(label);
            processLabel.setAlignment(Pos.BASELINE_CENTER);

            BorderPane borderPane = new BorderPane();
            borderPane.setCenter(processLabel);

            Scene process = new Scene(borderPane, 500, 300);
            process.getStylesheets().add("Theme.css");

            Stage screen = new Stage();
            screen.setScene(process);
            screen.show();
            screen.setTitle("Analyzing Audio...");

            Record mic = new Record();
            String prediction = test.classify(mic.getSample());

            screen.close();


            Label label2 = new Label("Identified a " + prediction);
            label2.setStyle("-fx-text-fill: white");
            label2.setId("bold-label");

            Button exit = new Button("Exit");
            exit.setLayoutY(400);
            exit.setLayoutX(200);
            exit.setOnAction(event1 -> Platform.exit());


            Button reanalyze = new Button("Reanalyze");

            reanalyze.setOnAction(event1 -> handle(event));

            VBox predictionLabel = new VBox();
            predictionLabel.getChildren().add(label2);
            predictionLabel.setAlignment(Pos.BASELINE_CENTER);


            VBox exitButton = new VBox();
            predictionLabel.getChildren().add(exit);
            predictionLabel.setAlignment(Pos.BASELINE_CENTER);


            VBox reanalyzeButton = new VBox();
            predictionLabel.getChildren().add(reanalyze);
            predictionLabel.setAlignment(Pos.BASELINE_CENTER);

            BorderPane borderPane2 = new BorderPane();
            borderPane2.setTop(predictionLabel);
            borderPane2.setLeft(reanalyzeButton);
            borderPane2.setRight(exitButton);

            Scene predict = new Scene(borderPane2, 500, 300);
            predict.getStylesheets().add("Theme.css");

            screen.setScene(predict);
            screen.show();
            screen.setTitle("Prediction");
        }

    }
}
