package view;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import sockets.Client;
import view.hotelView.DeleteHotelView;
import view.hotelView.RegisterHotelView;
import view.hotelView.ShowHotelView;
import view.hotelView.UpdateHotelView;
import view.roomView.RegisterRoomView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class MainView extends VBox {

    private Pane contentPane;

    private Client client;

    public MainView(Stage stage) {
        this.setPrefSize(600, 600);

        try {
            this.client = new Client("localhost", 5025);
            this.client.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Scene scene = new Scene(this);
        stage.setTitle("Principal Window");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

        this.initComponents();
    }

    private void initComponents() {
        Collection<Node> nodes = new ArrayList<>();

        MenuBar menuBar = new MenuBar();
        Menu menuHotel = new Menu("Hotels");
        Menu menuRoom = new Menu("Rooms");

        // -> Hotels Menu
        MenuItem hotelRegister = new MenuItem("Register");
        hotelRegister.setOnAction(e -> new RegisterHotelView(this.client, this.contentPane));

        MenuItem hotelView = new MenuItem("View all");
        hotelView.setOnAction(e -> new ShowHotelView(this.client, this.contentPane));

        MenuItem hotelUpdate = new MenuItem("Update");
        hotelUpdate.setOnAction(e -> new UpdateHotelView(this.client, this.contentPane));

        MenuItem hotelDelete = new MenuItem("Delete");
        hotelDelete.setOnAction(e -> new DeleteHotelView(this.client, this.contentPane));

        menuHotel.getItems().addAll(hotelRegister, hotelView, hotelUpdate, hotelDelete);

        // -> Rooms Menus
        MenuItem roomRegister = new MenuItem("Register");
        roomRegister.setOnAction(e -> new RegisterRoomView(this.client, this.contentPane));

        MenuItem roomView = new MenuItem("View all");
        roomView.setOnAction(e -> new ShowHotelView(this.client, this.contentPane));

        MenuItem roomUpdate = new MenuItem("Update");
        roomUpdate.setOnAction(e -> new UpdateHotelView(this.client, this.contentPane));

        MenuItem roomDelete = new MenuItem("Delete");
        roomDelete.setOnAction(e -> new DeleteHotelView(this.client, this.contentPane));

        menuRoom.getItems().addAll(roomRegister, roomView, roomUpdate, roomDelete);

        menuBar.getMenus().addAll(menuHotel, menuRoom);
        nodes.add(menuBar);

        // Panel de contenido donde estarán las ventanas internas
        contentPane = new Pane();
        contentPane.setPrefSize(600, 550);
        contentPane.setStyle("-fx-background-color: #f0f0f0;");
        nodes.add(contentPane);

        this.getChildren().addAll(nodes);
    }

}
