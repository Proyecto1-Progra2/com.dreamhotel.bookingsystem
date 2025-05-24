package view.hotelView;

import domain.Hotel;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import sockets.Client;
import utils.Action;
import utils.FXUtility;

import javax.swing.*;
import java.util.ArrayList;

public class RegisterHotelView extends BorderPane implements Runnable {

    private Client client;
    private Pane contentPane;

    private TextField tNumber, tName, tAddress;

    private volatile boolean isRunning = true;

    private Alert alert = FXUtility.alert("Hotel", "Register Hotel");//acomodar el alert


    public RegisterHotelView(Client client, Pane contentPane) {
        this.setStyle("-fx-border-color: black; -fx-background-color: white;");
        this.setPrefSize(530, 530);
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
        Label title = new Label("Register Hotel");
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
        tNumber = new TextField();
        tName = new TextField();
        tAddress = new TextField();
        Button btnRegister = new Button("Register");

        // Contenido del formulario
        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        contenido.getChildren().addAll(
                new Label("Phone Number:"),
                tNumber,
                new Label("Hotel Name:"),
                tName,
                new Label("Hotel Address:"),
                tAddress,
                btnRegister
        );

        btnRegister.setOnAction(e -> this.hotelRegister(new Hotel(tNumber.getText(), tName.getText(),
                tAddress.getText(), new ArrayList<>())));

        this.setTop(titleBar);
        this.setCenter(contenido);

        //Cabiossss
        contentPane.getChildren().add(this);
    }

    private void hotelRegister(Hotel hotel) {
        this.client.getSend().println(Action.HOTEL_REGISTER + hotel.toString());
    }

    @Override
    public void run() {
        while (this.isRunning) {
            try {
                // se enciclaba pero ya no:))
                int estado = this.client.getRegistered();

                if (estado == 1 || estado == 2) {
                    // Se pone en 0 inmediatamente para evitar múltiples ejecuciones
                    this.client.setRegistered(0);

                    Platform.runLater(() -> {
                        //Alert currentAlert = FXUtility.alert("Register Hotel", "Hotel");

                        if (estado == 1) {
                            alert.setContentText("Hotel registered successfully!");
                        } else if(estado==2){
                            alert.setContentText("The hotel already exists!");
                        }
                        //confirmar la alert
                        alert.setAlertType(Alert.AlertType.CONFIRMATION);
                        alert.showAndWait();

                        this.tNumber.setText("");
                        this.tName.setText("");
                        this.tAddress.setText("");
                    });
                }

                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Hilo interrumpido durante la espera", e);
            }
        }
    }
}

