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

    private Client client;
    private Pane contentPane;

    private ComboBox<String> cbStatus, cbStyle;
    private TextField tRoomNumber, tRoomPrice, tRequestRoom;

    private volatile boolean isRunning = true;

    private Alert alert = FXUtility.alert("Room", "Update Room");

    public UpdateRoomView(Client client, Pane contentPane) {
        this.setStyle("-fx-border-color: black; -fx-background-color: white;");
        this.setPrefSize(300, 200);
        this.setLayoutX(100);
        this.setLayoutY(100);

        this.contentPane = contentPane;

        this.initComponents();
        this.client = client;

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
        this.tRequestRoom = new TextField();
        Button btnRequest = new Button("Request Room");

        this.tRoomNumber = new TextField();
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
                new Label("Room number of hotel you want to update:"),
                tRequestRoom,
                btnRequest,
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

        btnRequest.setOnAction(e -> this.requestRoom(this.tRequestRoom.getText()));
        btnUpdate.setOnAction(e -> this.updateRoom(new Room(tRoomNumber.getText(), cbStatus.getValue(),
                cbStyle.getValue(), Double.parseDouble(this.tRoomPrice.getText()), null)));

        this.setTop(titleBar);
        this.setCenter(contenido);

        contentPane.getChildren().add(this);

    }

    private void requestRoom(String numberRoomRequest) {
        this.client.getSend().println(Action.ROOM_SEARCH+"-"+numberRoomRequest);
    }

    private void updateRoom(Room room) {
        this.client.getSend().println(Action.ROOM_UPDATE+room.toString());
    }

    @Override
    public void run() {
        while (this.isRunning) {
            try {
                if (this.client.isMostrarHabitacionSolicitado()) {
                    this.tRoomNumber.setText(this.client.getRoomSolicitado().getRoomNumber());
                    this.cbStatus.setValue(this.client.getRoomSolicitado().getStatus());
                    this.cbStyle.setValue(this.client.getRoomSolicitado().getStyle());
                    this.tRoomPrice.setText(String.valueOf(this.client.getRoomSolicitado().getPrice()));//maybe es aca

                    this.tRequestRoom.setText("");
                    this.client.setRoomSolicitado(null);
                    this.client.setMostrarHabitacionSolicitado(false);
                } else if (this.client.getUpdated() == 1) {
                    Platform.runLater(() -> { //
//                        alert = FXUtility.alert("Room", "Register Room"); //
                        alert.setContentText("Room update successfully!"); //
                        alert.setAlertType(Alert.AlertType.CONFIRMATION); //
                        alert.showAndWait(); // Muestra la alerta y espera que el usuario la cierre

                        this.tRoomNumber.setText("");
                        this.tRoomPrice.setText("");
                    });
                    this.client.setUpdated(0);
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restaura el estado de interrupción
                throw new RuntimeException("Hilo interrumpido durante la espera", e);
            }
        }
    }
}
