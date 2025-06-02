package view.bookingView;

import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import sockets.Client;
import table.BookingTableModel;
import table.RoomTableModel;

public class BookingRoomsView extends BorderPane implements Runnable {

    private Client client;
    private Pane contentPane;
    private TableView<RoomTableModel> tableView;
    private String bookingNumber, roomNumber, hotelNumber;

    public BookingRoomsView(Client client, Pane contentPane, String bookingNumber, String roomNumber, String hotelNumber) {
        this.setStyle("-fx-border-color: black; -fx-background-color: white;");
        this.setPrefSize(680, 530);
        this.setLayoutX(70);
        this.setLayoutY(100);
        this.contentPane = contentPane;
        this.bookingNumber = bookingNumber;
        this.roomNumber = roomNumber;
        this.hotelNumber = hotelNumber;

        this.initComponents();
        this.client = client;

        Thread thread = new Thread(this);
        thread.start();
    }

    private void initComponents() {
        // Título con botón cerrar
        HBox titleBar = new HBox();
        Label title = new Label("Show Booking´s rooms");
        Button closeBtn = new Button("X");

        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setSpacing(10);
        titleBar.setStyle("-fx-background-color: #cccccc; -fx-padding: 5;");
        titleBar.getChildren().addAll(title, closeBtn);

        closeBtn.setOnAction(e -> {
            //this.isRunning = false;
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

        // -> Tabla
        this.tableView = new TableView<>();

        //-> Columns


        this.tableView.getColumns().addAll();
        double widthPercent = 1.0 / tableView.getColumns().size();
        for (TableColumn<?, ?> column : tableView.getColumns()) {
            column.prefWidthProperty().bind(tableView.widthProperty().multiply(widthPercent));
        }

        // -> Se añade lo que se quiere mostrar en la interfaz
        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        contenido.getChildren().addAll(this.tableView);

        this.setTop(titleBar);
        this.setCenter(contenido);

        contentPane.getChildren().add(this);
    }

    @Override
    public void run() {

    }
}
