package view;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import sockets.Client;
import view.hotelView.*;

import java.util.ArrayList;
import java.util.Collection;

public class MainView extends VBox {

    private Pane contentPane;

    private Client client;

    private Label title;


    public MainView(Client client, Stage stage) {
        this.setPrefSize(1000, 700);


        Scene scene = new Scene(this);
        stage.setTitle("Reservation System");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

        this.initComponents();
        this.client = client;
    }

    private void initComponents() {
        Collection<Node> nodes = new ArrayList<>();

        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: #cfd6d8  ;");

        Menu menuHotel = new Menu("Hotels");
        menuHotel.setStyle("-fx-font-size: 16px;");
        menuHotel.setStyle("-fx-background-color: #87c1cc;");

        Menu menuBooking = new Menu("Booking");
        menuBooking.setStyle("-fx-font-size: 16px;");
        menuBooking.setStyle("-fx-background-color: #87c1cc;");

        // aparezca un contexto de lo que vamos a hacer
        menuHotel.setOnShowing(e -> {
            contentPane.getChildren().clear();
            Label label = new Label("Register of hotels");
            label.setStyle("-fx-font-family: 'Elephant'; -fx-font-size: 20px; -fx-text-fill: #59a5b3  ;");
            label.setLayoutX(20);
            label.setLayoutY(20);
            contentPane.getChildren().add(label);
        });

        // -> Hotels Menu
        MenuItem hotelView = new MenuItem("View all");
        hotelView.setOnAction(e -> new ShowHotelView(this.client, this.contentPane));
        hotelView.setStyle("-fx-background-color: #87c1cc ");

        menuHotel.getItems().addAll(hotelView);

        menuBar.getMenus().addAll(menuHotel);
        nodes.add(menuBar);

        contentPane = new Pane();
        contentPane.setPrefSize(1000, 1000);
        contentPane.setStyle("-fx-background-color:#ccd2d3 ;");


        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPrefWidth(1000);


        vbox.layoutYProperty().bind(contentPane.heightProperty().subtract(vbox.heightProperty()).divide(2));


        Image image = new Image(getClass().getResource("/image2.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(400);
        imageView.setPreserveRatio(true);


        Label bienvenida = new Label("Welcome to the Hotel Register!");
        Label inicio = new Label("Let’s Get You Checked In");
        Label mensaje = new Label("Efficiency, comfort, success — all in one stay");
        Label pressOn = new Label("Press on the menuBar!");


        inicio.setStyle("-fx-font-family: 'Elephant'; -fx-font-size: 20px; -fx-text-fill: #3a6972 ;");
        bienvenida.setStyle("-fx-font-family: 'Elephant'; -fx-font-size: 40px; -fx-text-fill: #2a2e2c;");
        mensaje.setStyle("-fx-font-family: 'Elephant'; -fx-font-size: 30px; -fx-text-fill: #59a5b3 ;");
        pressOn.setStyle("-fx-font-family: 'Elephant'; -fx-font-size: 15px; -fx-text-fill: #101111  ;");


        vbox.getChildren().addAll(inicio, bienvenida, mensaje, imageView, pressOn);


        contentPane.getChildren().add(vbox);


        nodes.add(contentPane);


        this.getChildren().addAll(nodes);
    }

}