package view.imagesView;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import sockets.Client;
import utils.Action;

public class RoomImagesView extends BorderPane implements Runnable{

    private Client client;
    private Pane contentPane;
    private String roomNumber, hotelNumber;
    private volatile boolean isRunning = true;

    private ImageView imageView;

    public RoomImagesView(Client client, Pane contentPane, String roomNumber, String hotelNumber) {
        this.setStyle("-fx-border-color: black; -fx-background-color: white;");
        this.setPrefSize(680, 530);
        this.setLayoutX(70);
        this.setLayoutY(100);
        this.contentPane = contentPane;
        this.roomNumber = roomNumber;
        this.hotelNumber = hotelNumber;

        this.initComponents();
        this.client = client;

        this.requestImages(Action.IMAGE_REQUEST, this.roomNumber, this.hotelNumber);

        Thread thread = new Thread(this);
        thread.start();
    }

    private void initComponents() {
        HBox titleBar = new HBox();
        Label title = new Label("Room List");
        Button closeBtn = new Button("X");

        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setSpacing(10);
        titleBar.setStyle("-fx-background-color: #cccccc; -fx-padding: 5;");
        titleBar.getChildren().addAll(title, closeBtn);

        closeBtn.setOnAction(e -> {
            this.isRunning = false;
            contentPane.getChildren().remove(this);
        });

        imageView = new ImageView();
        imageView.setFitWidth(300); // Tamaño más pequeño para la vista previa
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 5;");

        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        contenido.getChildren().addAll(imageView);

        this.setTop(titleBar);
        this.setCenter(contenido);

        contentPane.getChildren().add(this);
    }

    private void requestImages(String accion, String roomNumber, String hotelNumber) {
        this.client.getSend().println(accion+"|||"+roomNumber+"|||"+hotelNumber);
    }

    @Override
    public void run() {
        while (this.isRunning) {
            if (this.client.isImageReceived()) {
                byte[] imageData = this.client.getImage();
                if (imageData != null && imageData.length > 0) {
                    javafx.application.Platform.runLater(() -> {
                        imageView.setImage(new javafx.scene.image.Image(new java.io.ByteArrayInputStream(imageData)));
                    });
                } else {
                    System.out.println("⚠ Imagen recibida es nula o vacía");
                }
                this.client.setImage(null);
                this.client.setImageReceived(false);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
