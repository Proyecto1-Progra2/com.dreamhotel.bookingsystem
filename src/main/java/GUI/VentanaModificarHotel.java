package GUI;

import domain.Hotel;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import domain.Client;

public class VentanaModificarHotel extends BorderPane implements Runnable{

    private Client client;
    private Pane contentPane;

    private TextField tNumero, tNombre, tDireccion, tNumeroSolicitar;

    public VentanaModificarHotel(Client client, Pane contentPane) {
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
        Label title = new Label("Modificar Hotel");
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
        this.tNumeroSolicitar = new TextField();
        Button btnConsultar = new Button("Consultar");

        this.tNumero = new TextField();
        this.tNombre = new TextField();
        this.tDireccion = new TextField();
        Button btnModificar = new Button("Modificar");

        // Contenido del formulario
        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        contenido.getChildren().addAll(
                new Label("Ingrese el numero de telefono del hotel que desee editar:"),
                tNumeroSolicitar,
                btnConsultar,
                new Label("Numero de telefono:"),
                tNumero,
                new Label("Nombre:"),
                tNombre,
                new Label("Direccion:"),
                tDireccion,
                btnModificar
        );

        btnConsultar.setOnAction(e -> this.solicitarHotel(this.tNumeroSolicitar.getText()));
        btnModificar.setOnAction(e -> this.modificarHotel(new Hotel(tNumero.getText(), tNombre.getText(),
                tDireccion.getText())));

        this.setTop(titleBar);
        this.setCenter(contenido);

        contentPane.getChildren().add(this);
    }

    private void solicitarHotel(String numeroSolicitado) {
        this.client.getSend().println("solicitarHotel-"+numeroSolicitado);
    }

    private void modificarHotel(Hotel hotel) {
        this.client.getSend().println("modificarHotel"+hotel.toString());
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (this.client.isMostrarHotelSolicitado()) {
                    this.tNumero.setText(this.client.getHotelSolicitado().getNumero());
                    this.tNombre.setText(this.client.getHotelSolicitado().getNombre());
                    this.tDireccion.setText(this.client.getHotelSolicitado().getDireccion());
                    this.tNumeroSolicitar.setText("");
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
