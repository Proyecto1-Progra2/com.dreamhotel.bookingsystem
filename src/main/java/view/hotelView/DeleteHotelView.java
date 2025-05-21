package view.hotelView;

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

public class DeleteHotelView extends BorderPane implements Runnable{

    private Client client;
    private Pane contentPane;
    private volatile boolean isRunning = true;

    private TextField tNumber;

    private Alert alert = FXUtility.alert("Room", "Delete Room");

    public DeleteHotelView(Client client, Pane contentPane) {
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
        Label title = new Label("Delete Hotel");
        Button closeBtn = new Button("X");

        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setSpacing(10);
        titleBar.setStyle("-fx-background-color: #cccccc; -fx-padding: 5;");
        titleBar.getChildren().addAll(title, closeBtn);

        closeBtn.setOnAction(e -> contentPane.getChildren().remove(this));

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
        Button btnDelete = new Button("Delete");

        // Contenido del formulario
        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        contenido.getChildren().addAll(
                new Label("Phone Number of hotel you want to delete:"),
                tNumber,
                btnDelete
        );

        btnDelete.setOnAction(e -> this.deleteNumber(tNumber.getText()));

        this.setTop(titleBar);
        this.setCenter(contenido);

        contentPane.getChildren().add(this);
    }

    private void deleteNumber(String number) {
        this.client.getSend().println(Action.HOTEL_DELETE+"-"+number);
    }

    @Override
    public void run() {
        while (this.isRunning) {
            try {
                if (this.client.getDeleted() == 1) {
                    //JOptionPane.showMessageDialog(null, "Phone number hotel: " + tNumber.getText()+ " deleted successfully!");
                    alert.setContentText("Hotel deleted successfully!");
                    alert.setAlertType(Alert.AlertType.CONFIRMATION);
                    alert.showAndWait();
                    this.tNumber.setText("");
                    this.client.setDeleted(0);
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
