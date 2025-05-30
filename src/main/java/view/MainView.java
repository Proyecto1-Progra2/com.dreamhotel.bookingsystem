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
import view.roomView.DeleteRoomView;
import view.roomView.RegisterRoomView;
import view.roomView.ShowRoomView;
import view.roomView.UpdateRoomView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class MainView extends VBox {

    private Pane contentPane;

    private Client client;

    private Label title;


    public MainView(Stage stage) {
        this.setPrefSize(1000, 700);

        try {
            this.client = new Client("localhost", 5025);
            this.client.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Scene scene = new Scene(this);
        stage.setTitle("Reservation System");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

        this.initComponents();
    }

    private void initComponents() {
        Collection<Node> nodes = new ArrayList<>();

        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: #cfd6d8  ;");

        Menu menuHotel = new Menu("Hotels");
        menuHotel.setStyle("-fx-font-size: 16px;");
        menuHotel.setStyle("-fx-background-color: #87c1cc   ;");


//        Menu menuRoom = new Menu("Rooms");
//        menuRoom.setStyle("-fx-font-size: 16px;");
//        menuRoom.setStyle("-fx-background-color:#62c19a    ;");

        // aparezca un contexto de lo que vamos a hacer
        menuHotel.setOnShowing(e -> {
            contentPane.getChildren().clear();
            Label label = new Label("Register of hotels");
            label.setStyle("-fx-font-family: 'Elephant'; -fx-font-size: 20px; -fx-text-fill: #59a5b3  ;");
            label.setLayoutX(20);
            label.setLayoutY(20);
            contentPane.getChildren().add(label);
        });

//        menuRoom.setOnShowing(e -> {
//            contentPane.getChildren().clear();
//            Label label = new Label("Register of Rooms");
//            label.setStyle("-fx-font-family: 'Elephant';-fx-font-size: 20px; -fx-text-fill: #2a2e2c ;");
//            label.setLayoutX(20);
//            label.setLayoutY(20);
//            contentPane.getChildren().add(label);
//        });

        // -> Hotels Menu
        MenuItem hotelRegister = new MenuItem("Register");
        hotelRegister.setOnAction(e -> new RegisterHotelView(this.client, this.contentPane));
        hotelRegister.setStyle("-fx-background-color: #87c1cc ");

        MenuItem hotelView = new MenuItem("View all");
        hotelView.setOnAction(e -> new ShowHotelView(this.client, this.contentPane));
        hotelView.setStyle("-fx-background-color: #87c1cc ");

        MenuItem hotelUpdate = new MenuItem("Update");
        hotelUpdate.setOnAction(e -> new UpdateHotelView(this.client, this.contentPane));
        hotelUpdate.setStyle("-fx-background-color: #87c1cc ");

        MenuItem hotelDelete = new MenuItem("Delete");
        hotelDelete.setOnAction(e -> new DeleteHotelView(this.client, this.contentPane));
        hotelDelete.setStyle("-fx-background-color: #87c1cc ");

        menuHotel.getItems().addAll(hotelView);

        // -> Rooms Menus
//        MenuItem roomRegister = new MenuItem("Register");
//        roomRegister.setOnAction(e -> new RegisterRoomView(this.client, this.contentPane));
//        roomRegister.setStyle("-fx-background-color: #8ec6af ");

        MenuItem roomView = new MenuItem("View all");
        roomView.setOnAction(e -> new ShowRoomView(this.client, this.contentPane));
        roomView.setStyle("-fx-background-color: #8ec6af ");

        MenuItem roomUpdate = new MenuItem("Update");
        roomUpdate.setOnAction(e -> new UpdateRoomView(this.client, this.contentPane, "", ""));
        roomUpdate.setStyle("-fx-background-color: #8ec6af ");

        MenuItem roomDelete = new MenuItem("Delete");
        roomDelete.setOnAction(e -> new DeleteRoomView(this.client, this.contentPane));
        roomDelete.setStyle("-fx-background-color: #8ec6af ");

//        menuRoom.getItems().addAll(/*roomRegister, */roomView, roomUpdate, roomDelete);

        //menuBar.getMenus().addAll(menuHotel */menuRoom);
        menuBar.getMenus().add(menuHotel);
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