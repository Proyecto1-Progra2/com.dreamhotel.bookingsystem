package view;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import sockets.Client;

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
        stage.setTitle("Ventana Principal");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

        this.initComponents();
    }

    private void initComponents() {
        Collection<Node> nodes = new ArrayList<>();

        MenuBar menuBar = new MenuBar();
        Menu menuHuesped = new Menu("Huéspedes");
        Menu menuHotel = new Menu("Hoteles");

        MenuItem registrarHuesped = new MenuItem("Registrar");
        registrarHuesped.setOnAction(e -> new VentanaRegistrarHuesped(this.client, this.contentPane));

        MenuItem mostrarHuespedes = new MenuItem("Mostrar");
        mostrarHuespedes.setOnAction(e -> new VentanaMostrarHuesped(this.client, this.contentPane));

        MenuItem registrarHotel = new MenuItem("Registrar");
        registrarHotel.setOnAction(e -> new RegisterHotelView(this.client, this.contentPane));

        MenuItem mostrarHotel = new MenuItem("Mostrar");
        mostrarHotel.setOnAction(e -> new ShowHotelView(this.client, this.contentPane));

        MenuItem modificarHotel = new MenuItem("Modificar");
        modificarHotel.setOnAction(e -> new UpdateHotelView(this.client, this.contentPane));

        MenuItem eliminarHotel = new MenuItem("Eliminar");
        eliminarHotel.setOnAction(e -> new DeleteHotelView(this.client, this.contentPane));

        menuHuesped.getItems().addAll(registrarHuesped, mostrarHuespedes);
        menuHotel.getItems().addAll(registrarHotel, mostrarHotel, modificarHotel, eliminarHotel);

        menuBar.getMenus().addAll(menuHuesped, menuHotel);
        nodes.add(menuBar);

        // Panel de contenido donde estarán las ventanas internas
        contentPane = new Pane();
        contentPane.setPrefSize(600, 550);
        contentPane.setStyle("-fx-background-color: #f0f0f0;");
        nodes.add(contentPane);

        this.getChildren().addAll(nodes);
    }

    private void mostrarVentanaInterna() {
        BorderPane internalFrame = new BorderPane();
        internalFrame.setStyle("-fx-border-color: black; -fx-background-color: white;");
        internalFrame.setPrefSize(300, 200);
        internalFrame.setLayoutX(100);
        internalFrame.setLayoutY(100);

        // Título con botón cerrar
        HBox titleBar = new HBox();
        Label title = new Label("Registrar Huésped");
        Button closeBtn = new Button("X");

        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setSpacing(10);
        titleBar.setStyle("-fx-background-color: #cccccc; -fx-padding: 5;");
        titleBar.getChildren().addAll(title, closeBtn);

        closeBtn.setOnAction(e -> contentPane.getChildren().remove(internalFrame));

        // Hacer movible la ventana y limitarla al contentPane
        final double[] dragOffset = new double[2];
        titleBar.setOnMousePressed(e -> {
            dragOffset[0] = e.getSceneX() - internalFrame.getLayoutX();
            dragOffset[1] = e.getSceneY() - internalFrame.getLayoutY();
        });

        titleBar.setOnMouseDragged(e -> {
            double newX = e.getSceneX() - dragOffset[0];
            double newY = e.getSceneY() - dragOffset[1];

            // Restringir al área visible del contentPane
            newX = Math.max(0, Math.min(newX, contentPane.getWidth() - internalFrame.getWidth()));
            newY = Math.max(0, Math.min(newY, contentPane.getHeight() - internalFrame.getHeight()));

            internalFrame.setLayoutX(newX);
            internalFrame.setLayoutY(newY);
        });

        // Contenido del formulario (simulado)
        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        contenido.getChildren().addAll(
                new Label("Nombre:"),
                new TextField(),
                new Label("Cédula:"),
                new TextField(),
                new Button("Registrar")
        );

        internalFrame.setTop(titleBar);
        internalFrame.setCenter(contenido);

        contentPane.getChildren().add(internalFrame);
    }
}
