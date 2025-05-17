package view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import sockets.Client;
import utils.Action;

public class ShowHotelView extends BorderPane implements Runnable {

    private Client client;
    private Pane contentPane;

    private TextArea taView;

    public ShowHotelView(Client client, Pane contentPane) {
        this.setStyle("-fx-border-color: black; -fx-background-color: white;");
        this.setPrefSize(400, 300);
        this.setLayoutX(100);
        this.setLayoutY(100);
        this.contentPane = contentPane;

        this.initComponents();
        this.client = client;

        this.HotelList(Action.HOTEL_LIST);

        Thread thread = new Thread(this);
        thread.start();
    }

    private void initComponents() {
        // Título con botón cerrar
        HBox titleBar = new HBox();
        Label title = new Label("Hotel List");
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
        this.taView = new TextArea();
        this.taView.setEditable(false);

        // Contenido del formulario (simulado)
        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        contenido.getChildren().addAll(
                this.taView
        );

        this.setTop(titleBar);
        this.setCenter(contenido);

        contentPane.getChildren().add(this);
    }

    private void HotelList(String accion) {
        this.client.getSend().println(accion+"-");
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (this.client.isHotelesMostrado()) {
                    this.taView.setText(this.client.getMostrarHoteles());
                    this.client.setMostrarHoteles("");
                    this.client.setHotelesMostrado(false);
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
