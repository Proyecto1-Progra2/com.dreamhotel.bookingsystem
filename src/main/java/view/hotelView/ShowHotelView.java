package view.hotelView;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import sockets.Client;
import utils.Action;

// cambios
public class ShowHotelView extends BorderPane implements Runnable {

    private Client client;
    private Pane contentPane;

    // private TextArea taView;

    private TableView<ObservableList<String>> tableView;
    private ObservableList<ObservableList<String>> data;

    private volatile boolean isRunning = true;


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

        tableView = new TableView<>();
        data = FXCollections.observableArrayList();
        tableView.setItems(data);

        TableColumn<ObservableList<String>, String> column1 = new TableColumn<>("ID");
        column1.setCellValueFactory(param ->
                new javafx.beans.property.SimpleStringProperty(param.getValue().get(0)));
        column1.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<ObservableList<String>, String> column2 = new TableColumn<>("Number");
        column2.setCellValueFactory(param ->
                new javafx.beans.property.SimpleStringProperty(param.getValue().get(1)));
        column2.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<ObservableList<String>, String> column3 = new TableColumn<>("Hotel Name");
        column3.setCellValueFactory(param ->
                new javafx.beans.property.SimpleStringProperty(param.getValue().get(2)));
        column3.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<ObservableList<String>, String> column4 = new TableColumn<>("Address");
        column4.setCellValueFactory(param ->
                new javafx.beans.property.SimpleStringProperty(param.getValue().get(3)));
        column4.setCellFactory(TextFieldTableCell.forTableColumn());

        tableView.getColumns().addAll(column1, column2, column3, column4);

        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setSpacing(10);
        titleBar.setStyle("-fx-background-color: #cccccc; -fx-padding: 5;");
        titleBar.getChildren().addAll(title, closeBtn);

        closeBtn.setOnAction(e -> {
            //this.isRunning = false;
            this.setVisible(false);
          contentPane.getChildren().remove(this);
        });

        // Hacer movible la ventana dentro del contentPane
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
//        this.taView = new taView<>();
//        this.taView.setEditable(false);


        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        contenido.getChildren().addAll(tableView);

        this.setTop(titleBar);
        this.setCenter(contenido);

        contentPane.getChildren().add(this);
    }
    // para encontrar la accion
    private void HotelList(String accion) {
        this.client.getSend().println(accion + "-");
    }


    @Override
    public void run() {
        while (this.isRunning) {
            try {
                if (this.client.isHotelesMostrado()) {
                    String resultHotels = this.client.getMostrarHoteles();

                    Platform.runLater(() -> {
                        if(data.isEmpty()) {  // solo carga si no hay datos aún
                            String[] rows = resultHotels.split("\n");

                            for (String row : rows) {
                                String[] parts = row.split(" Numero: | Nombre: | Direccion:"); //   | separa las columns
                                if (parts.length == 4) {

                                    ObservableList<String> rowData = FXCollections.observableArrayList(
                                            parts[0].replace(".", "").trim(), // separar lo que entra desde client linea 73
                                            parts[1].trim(),
                                            parts[2].trim(),
                                            parts[3].trim()
                                            // el indice segun el valor de las columns definidas arriba
                                    );
                                    data.add(rowData);
                                }
                            }
                        }

                    });

                    this.client.setMostrarHoteles("");
                    this.client.setHotelesMostrado(false);
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
