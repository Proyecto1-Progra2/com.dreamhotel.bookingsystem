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

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;

public class RegisterRoomView extends BorderPane implements Runnable {

    private Client client;
    private Pane contentPane;

    private ComboBox<String> cbStatus, cbStyle;
    private TextField tRoomNumber, tPrice;

    private volatile boolean isRunning = true;

    private Stage primaryStage; //para que quede bien File
    private Alert alert = FXUtility.alert("Room", "Register Room");

    private ArrayList<Image> images;

    public RegisterRoomView(Client client, Pane contentPane) {
        this.setStyle("-fx-border-color: black; -fx-background-color: white;");
        this.setPrefSize(300, 200);
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
        cbStatus.getItems().addAll("Available", "Maintenance", "Booked");

        cbStyle = new ComboBox<>();
        cbStyle.getItems().addAll("Standar", "Deluxe", "Suite", "Family");

        tRoomNumber = new TextField();
        tPrice = new TextField();
        Button btnRegister = new Button("Register");
        //cargar imagenes
        Button btnCargar = new Button("Upload images of the rooms");//revisar si esta bien traducido

        ImageView imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setPreserveRatio(true);

        // Contenido del formulario
        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        contenido.getChildren().addAll(
                new Label("Room Number:"),
                tRoomNumber,
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
        //int i = 1
        //int pos = i;

        btnCargar.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select an image");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.png", "*.jpeg", "*.gif")
            );
            //declarar una variable int pos = 1
            File archivo = fileChooser.showOpenDialog(primaryStage);//probar si ese primaryStage si es la imagen
            if (archivo != null) {
                try {
                    this.images.add(new Image(archivo, this.tRoomNumber.getText()));
                    //byte[] datos = archivoABytes(archivo);
                    //int pos = posicionBox.getValue();
                    //imagenData.guardarImagen(datos, pos); //con un pos, guardo cuantas imagenes hay en esa habitación
                    imageView.setImage(new javafx.scene.image.Image(archivo.toURI().toString()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            //pos++;
        });

        btnRegister.setOnAction(e -> this.roomRegister(new Room(this.tRoomNumber.getText(), cbStatus.getValue(),
                cbStyle.getValue(), Double.parseDouble(this.tPrice.getText())))); //debo agregar un arreglo de bytes

        this.setTop(titleBar);
        this.setCenter(contenido);

        contentPane.getChildren().add(this);
    }

    private void roomRegister(Room room) {
        this.client.getSend().println(Action.ROOM_REGISTER+room.toString());
    }

    @Override
    public void run() {
        while (this.isRunning) {
            try {
                if (this.client.getRegistered() == 1) {
                    // Todas las actualizaciones de UI deben ir dentro de Platform.runLater
                    Platform.runLater(() -> { //
//                        alert = FXUtility.alert("Room", "Register Room"); //
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
                // Es mejor manejar la InterruptedException de una manera que no lance RuntimeException directamente,
                // ya que eso detendría tu hilo. Por ejemplo, restaurar el estado de interrupción del hilo
                // y salir del bucle si el hilo fue interrumpido.
                Thread.currentThread().interrupt(); // Restaura el estado de interrupción
                throw new RuntimeException("Hilo interrumpido durante la espera", e);
            }
        }
    }

    //convierte una imagen a bytes
    public static byte[] archivoABytes(File archivo) throws IOException {
        try (InputStream is = new FileInputStream(archivo);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            byte[] datos = new byte[1024];
            int n;
            while ((n = is.read(datos)) != -1)
                buffer.write(datos, 0, n);

            return buffer.toByteArray();
        }
    }
}
