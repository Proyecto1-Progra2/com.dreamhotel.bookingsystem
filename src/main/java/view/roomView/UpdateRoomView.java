package view.roomView;

import domain.Hotel;
import domain.Room;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import sockets.Client;
import utils.Action;
import utils.FXUtility;

import javax.swing.*;

public class UpdateRoomView extends BorderPane implements Runnable {

    private final Client client;
    private final Pane contentPane;

    private ComboBox<String> cbStatus, cbStyle;
    private TextField tRoomNumber, tRoomPrice;

    private final String roomNumber;
    private final String hotelNumber;

    private volatile boolean isRunning = true;

    public UpdateRoomView(Client client, Pane contentPane, String roomNumber, String hotelNumber) {
        this.setStyle("-fx-border-color: black; -fx-background-color: white;");
        this.setPrefSize(530, 530);
        this.setLayoutX(100);
        this.setLayoutY(100);

        this.contentPane = contentPane;

        this.roomNumber = roomNumber;
        this.hotelNumber = hotelNumber;

        this.initComponents();
        this.client = client;

        this.requestRoom(roomNumber, hotelNumber);

        Thread thread = new Thread(this);
        thread.start();
    }

    private void initComponents() {
        // Título con botón cerrar
        HBox titleBar = new HBox();
        Label title = new Label("Room Update");
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
        this.tRoomNumber = new TextField();
        tRoomNumber.setEditable(false);
        this.tRoomPrice = new TextField();
        this.cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("Available", "Maintenance", "Booked");
        this.cbStyle = new ComboBox<>();
        cbStyle.getItems().addAll("Standar", "Deluxe", "Suite", "Family");
        Button btnUpdate = new Button("Update Room");

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
                new Label("Room Price"),
                tRoomPrice,
                //revisar si tambien hay que actualizar imagenes
                btnUpdate
        );

        btnUpdate.setOnAction(e -> this.updateRoom(new Room(tRoomNumber.getText(), cbStatus.getValue(),
                cbStyle.getValue(), Double.parseDouble(this.tRoomPrice.getText()), null, this.hotelNumber)));

        this.setTop(titleBar);
        this.setCenter(contenido);

        contentPane.getChildren().add(this);

    }

    private void requestRoom(String numberRoomRequest, String hotelNumberRequest) {
        this.client.getSend().println(Action.ROOM_SEARCH + "-" + numberRoomRequest + "-" + hotelNumberRequest);
    }

    private void updateRoom(Room room) {
        this.client.getSend().println(Action.ROOM_UPDATE + room.toString());
    }

    @Override
    public void run() {
        while (this.isRunning) {
            try {
                Platform.runLater(() -> {
                    if (this.client.isMostrarHabitacionSolicitado()) {
                        this.tRoomNumber.setText(this.client.getRoomSolicitado().getRoomNumber());
                        this.cbStatus.setValue(this.client.getRoomSolicitado().getStatus());
                        this.cbStyle.setValue(this.client.getRoomSolicitado().getStyle());
                        this.tRoomPrice.setText(String.valueOf(this.client.getRoomSolicitado().getPrice()));//maybe es aca

                        this.client.setRoomSolicitado(null);
                        this.client.setMostrarHabitacionSolicitado(false);
                    } else if (this.client.getUpdated() == 1) {
                        Alert alert = FXUtility.alert("Room", "Update Room");
                        alert.setContentText("Room update successfully!");
                        alert.setAlertType(Alert.AlertType.CONFIRMATION);
                        alert.showAndWait();

                        this.tRoomNumber.setText("");
                        this.tRoomPrice.setText("");

                        this.client.setUpdated(0);
                    }
                });
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restaura el estado de interrupción
                throw new RuntimeException("Hilo interrumpido durante la espera", e);
            }
        }
    }
}
