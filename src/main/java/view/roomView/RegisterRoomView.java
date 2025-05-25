package view.roomView;

import domain.Image;
import domain.Room;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sockets.Client;
import utils.Action;
import utils.FXUtility;
import utils.ImageUtils;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;

public class RegisterRoomView extends BorderPane implements Runnable {

    private Client client;
    private Pane contentPane;

    private ComboBox<String> cbStatus, cbStyle;
    private TextField tRoomNumber, tPrice, tHotelNumber;

    private ImageView imageView;

    private volatile boolean isRunning = true;

    private Stage primaryStage; //para que quede bien File
    private Alert alert = FXUtility.alert("Room", "Register Room");

    private File archivo;
    private byte[] image;

    private ArrayList<Image> images;
    private ArrayList<ImageView> imageViews;

    public RegisterRoomView(Client client, Pane contentPane) {
        this.setStyle("-fx-border-color: black; -fx-background-color: white;");
        this.setPrefSize(530, 530);
        this.setLayoutX(100);
        this.setLayoutY(100);

        this.contentPane = contentPane;
        this.images = new ArrayList<>();

        this.initComponents();
        this.client = client;

        Thread thread = new Thread(this);
        thread.start();
    }

    private void initComponents() {
        // Título con botón cerrar
        HBox titleBar = new HBox();
        Label title = new Label("Register Room");
        Button closeBtn = new Button("X");

        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setSpacing(10);
        titleBar.setStyle("-fx-background-color: #cccccc; -fx-padding: 5;");
        titleBar.getChildren().addAll(title, closeBtn);

        closeBtn.setOnAction(e -> {
            this.isRunning = false;
            contentPane.getChildren().remove(this);
        });

        // Hacer movible la ventana y limitarla al contentPane
        final double[] dragOffset = new double[2];
        titleBar.setOnMousePressed(e -> {
            dragOffset[0] = e.getSceneX() - this.getLayoutX();
            dragOffset[1] = e.getSceneY() - this.getLayoutY();
        });

        titleBar.setOnMouseDragged(e -> {
            double newX = e.getSceneX() - dragOffset[0];
            double newY = e.getSceneY() - dragOffset[1];

            // Restringir al área visible del contentPane
            newX = Math.max(0, Math.min(newX, contentPane.getWidth() - this.getWidth()));
            newY = Math.max(0, Math.min(newY, contentPane.getHeight() - this.getHeight()));

            this.setLayoutX(newX);
            this.setLayoutY(newY);
        });

        // Recuperar datos
        cbStatus = new ComboBox<>();
        cbStatus.setPromptText("Select Status"); // Placeholder
        cbStatus.getItems().addAll("Available", "Maintenance", "Booked");

        cbStyle = new ComboBox<>();
        cbStyle.setPromptText("Select Style"); // Placeholder
        cbStyle.getItems().addAll("Standar", "Deluxe", "Suite", "Family");

        tRoomNumber = new TextField();
        tPrice = new TextField();
        Button btnRegister = new Button("Register");
        tHotelNumber = new TextField();
        //cargar imagenes
        Button btnCargar = new Button("Upload images of the rooms");//revisar si esta bien traducido

        imageView = new ImageView();
        imageView.setFitWidth(150); // Tamaño más pequeño para la vista previa
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 5;");


        // Contenido del formulario
        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        contenido.getChildren().addAll(
                new Label("Room Number:"),
                tRoomNumber,
                new Label("Hotel Number:"),
                tHotelNumber,
                new Label("Room Status:"),
                cbStatus,
                new Label("Room Style:"),
                cbStyle,
                new Label("Room Price:"),
                tPrice,
                btnCargar,
                btnRegister,
                imageView
        );

        btnCargar.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select an image");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.png", "*.jpeg", "*.gif")
            );
            archivo = fileChooser.showOpenDialog(primaryStage);
            if (archivo != null) {
                try {
                    this.image = ImageUtils.archivoABytes(archivo);
                    imageView.setImage(new javafx.scene.image.Image(archivo.toURI().toString()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            //pos++;
        });

        btnRegister.setOnAction(e -> this.roomRegister(new Room(this.tRoomNumber.getText(), cbStatus.getValue(),
                cbStyle.getValue(), Double.parseDouble(this.tPrice.getText()), new Image(this.tRoomNumber.getText(), this.image),
                tHotelNumber.getText()))
        );

        this.setTop(titleBar);
        this.setCenter(contenido);

        contentPane.getChildren().add(this);
    }

    private void roomRegister(Room room) {
        String encodedImage = Base64.getEncoder().encodeToString(room.getImage().getImage());

        this.client.getSend().println(Action.ROOM_REGISTER + "-" +
                room.getRoomNumber() + "-" +
                room.getStatus() + "-" +
                room.getStyle() + "-" +
                room.getPrice() + "-" +
                room.getImage().getRoomNumber() + "-" +  // este es igual a room.getRoomNumber()
                encodedImage + "-" +
                room.getHotelNumber()
        );
    }

    @Override
    public void run() {
        while (this.isRunning) {
            try {
                if (this.client.getRegistered() == 1) {
                    // Todas las actualizaciones de UI deben ir dentro de Platform.runLater
                    Platform.runLater(() -> { //
                        alert.setContentText("Room registered successfully!"); //
                        alert.setAlertType(Alert.AlertType.CONFIRMATION); //
                        alert.showAndWait(); // Muestra la alerta y espera que el usuario la cierre

                        this.tRoomNumber.setText("");
                        this.cbStyle.getSelectionModel().clearSelection();
                        this.cbStatus.getSelectionModel().clearSelection();
                        this.tPrice.setText("");
                    });
                    this.client.setRegistered(0);
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restaura el estado de interrupción
                throw new RuntimeException("Hilo interrumpido durante la espera", e);
            }
        }
    }
}
