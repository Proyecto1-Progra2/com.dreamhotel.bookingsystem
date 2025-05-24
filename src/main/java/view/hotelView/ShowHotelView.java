package view.hotelView;

import domain.Hotel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import sockets.Client;
import table.HotelTableModel;
import utils.Action;

import java.util.ArrayList;

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
        this.hotelList(Action.HOTEL_LIST);


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

        TableColumn<HotelTableModel, Void> columnActions = new TableColumn<>("Actions");
        columnActions.setCellFactory(col -> new TableCell<HotelTableModel, Void>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            {
                btnEdit.setStyle("-fx-background-color: #87CEFA;");
                btnDelete.setStyle("-fx-background-color: #FA8072;");

                btnEdit.setOnAction(e -> {
                    HotelTableModel hotel = getTableView().getItems().get(getIndex());

                    String hotelRequest = hotel.getHotelNumber();

                    TextInputDialog numberDialog = new TextInputDialog(hotel.getHotelName());
                    numberDialog.setTitle("Number edit");
                    numberDialog.setHeaderText("Edit hotel number:");
                    numberDialog.setContentText("Number:");
                    numberDialog.showAndWait().ifPresent(newNumber -> hotel.hotelNumberProperty().set(newNumber));

                    TextInputDialog nameDialog = new TextInputDialog(hotel.getHotelName());
                    nameDialog.setTitle("Name edit");
                    nameDialog.setHeaderText("Edit hotel name:");
                    nameDialog.setContentText("Name:");
                    nameDialog.showAndWait().ifPresent(newName -> hotel.hotelNameProperty().set(newName));

                    TextInputDialog addressDialog = new TextInputDialog(hotel.getHotelAddress());
                    addressDialog.setTitle("Address edit");
                    addressDialog.setHeaderText("Edit hotel address:");
                    addressDialog.setContentText("Address:");
                    addressDialog.showAndWait().ifPresent(newAddress -> hotel.hotelAddressProperty().set(newAddress));

                    updateHotel(new Hotel(hotel.getHotelNumber(), hotel.getHotelName(), hotel.getHotelAddress(), new ArrayList<>()), hotelRequest);
                    refreshTable();
                });

                btnDelete.setOnAction(e -> {
                    HotelTableModel hotel = getTableView().getItems().get(getIndex());
                    String hotelNumber = hotel.getHotelNumber();
                    deleteHotel(hotelNumber);
                    refreshTable();
                });
            }
            private final HBox hbox = new HBox(5, btnEdit, btnDelete);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });

        tableView.getColumns().addAll(column1, column2, column3, columnActions);

        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setSpacing(10);
        titleBar.setStyle("-fx-background-color: #cccccc; -fx-padding: 5;");
        titleBar.getChildren().addAll(title, closeBtn);

        closeBtn.setOnAction(e -> {
            this.isRunning = false;
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
    private void hotelList(String accion) {
        this.client.getSend().println(accion + "-");
    }

    private void deleteHotel(String number) {
        this.client.getSend().println(Action.HOTEL_DELETE+"-"+number);
    }

    private void requestHotel(String numberRequest) {
        this.client.getSend().println(Action.HOTEL_SEARCH+"-"+numberRequest);
    }

    private void updateHotel(Hotel hotel, String numberRequest) {
        this.client.getSend().println(Action.HOTEL_UPDATE+hotel.toString()+"-"+numberRequest);
    }

    private void refreshTable() {
        data.clear();
        this.hotelList(Action.HOTEL_LIST);
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
