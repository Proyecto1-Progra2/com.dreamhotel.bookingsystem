package view;

import domain.Hotel;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import sockets.Client;
import utils.Action;

public class UpdateHotelView extends BorderPane implements Runnable{

    private Client client;
    private Pane contentPane;

    private TextField tNumber, tName, tAddress, tRequestHotel;

    public UpdateHotelView(Client client, Pane contentPane) {
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
        Label title = new Label("Hotel Update");
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
        this.tRequestHotel = new TextField();
        Button btnRequest = new Button("Request Hotel");

        this.tNumber = new TextField();
        this.tName = new TextField();
        this.tAddress = new TextField();
        Button btnUpdate = new Button("Update Hotel");

        // Contenido del formulario
        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        contenido.getChildren().addAll(
                new Label("Phone number of hotel you want to update:"),
                tRequestHotel,
                btnRequest,
                new Label("Phone Number:"),
                tNumber,
                new Label("Name:"),
                tName,
                new Label("Address:"),
                tAddress,
                btnUpdate
        );

        btnRequest.setOnAction(e -> this.requestHotel(this.tRequestHotel.getText()));
        btnUpdate.setOnAction(e -> this.updateHotel(new Hotel(tNumber.getText(), tName.getText(),
                tAddress.getText())));

        this.setTop(titleBar);
        this.setCenter(contenido);

        contentPane.getChildren().add(this);
    }

    private void requestHotel(String numberRequest) {
        this.client.getSend().println(Action.HOTEL_SEARCH+"-"+numberRequest);
    }

    private void updateHotel(Hotel hotel) {
        this.client.getSend().println(Action.HOTEL_UPDATE+hotel.toString());
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (this.client.isMostrarHotelSolicitado()) {
                    this.tNumber.setText(this.client.getHotelSolicitado().getNumber());
                    this.tName.setText(this.client.getHotelSolicitado().getName());
                    this.tAddress.setText(this.client.getHotelSolicitado().getAddress());
                    this.tRequestHotel.setText("");
                    this.client.setHotelSolicitado(null);
                    this.client.setMostrarHotelSolicitado(false);
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
