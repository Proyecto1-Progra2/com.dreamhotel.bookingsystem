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
import table.HotelTableModel;
import table.RoomTableModel;
import utils.Action;

// cambios
public class ShowHotelView extends BorderPane implements Runnable {

    private Client client;
    private Pane contentPane;

    // private TextArea taView;

    private TableView<HotelTableModel> tableView;
    private ObservableList<HotelTableModel> data;

    private volatile boolean isRunning =true;

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

        TableColumn<HotelTableModel, String> column1 = new TableColumn<>("Hotel Number");
        column1.setCellValueFactory(cellData -> cellData.getValue().hotelNumberProperty());

        TableColumn<HotelTableModel, String> column2 = new TableColumn<>("Hotel Name");
        column2.setCellValueFactory(cellData -> cellData.getValue().hotelNameProperty());

        TableColumn<HotelTableModel, String> column3 = new TableColumn<>("Hotel Address");
        column3.setCellValueFactory(cellData -> cellData.getValue().hotelAddressProperty());

        tableView.getColumns().addAll(column1, column2, column3);

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
                                String[] parts = row.split("-");
                                if (parts.length == 3) {
                                    HotelTableModel hotel = new HotelTableModel(
                                            parts[0].trim(),  // hotelNumber
                                            parts[1].trim(),  // hotelName
                                            parts[2].trim()  // hotelAddress
                                    );
                                    data.add(hotel);
                                }
                            }
                        }

                    });

                    this.client.setMostrarHoteles("");
                    this.client.setHotelesMostrado(false);
                }
               Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
