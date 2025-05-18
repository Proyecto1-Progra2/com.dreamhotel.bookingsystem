package view.roomView;

import domain.Room;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import sockets.Client;
import utils.Action;

import javax.swing.*;

public class RegisterRoomView extends BorderPane implements Runnable {

    private Client client;
    private Pane contentPane;

    private ComboBox<String> cbStatus, cbStyle;
    private TextField tRoomNumber, tPrice;

    private volatile boolean isRunning = true;

    public RegisterRoomView(Client client, Pane contentPane) {
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
                btnRegister
        );

        btnRegister.setOnAction(e -> this.roomRegister(new Room(this.tRoomNumber.getText(), cbStatus.getValue(),
                cbStyle.getValue(), Double.parseDouble(this.tPrice.getText()))));

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
                    JOptionPane.showMessageDialog(null, "Room registered successfully!");
                    this.tRoomNumber.setText("");
                    this.tPrice.setText("");
                    this.client.setRegistered(0);
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
