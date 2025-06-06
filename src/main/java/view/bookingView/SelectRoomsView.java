package view.bookingView;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import sockets.Client;
import table.RoomTableModel;
import utils.Action;

import java.time.LocalDate;
import java.util.Optional;

public class SelectRoomsView extends BorderPane implements Runnable {

    private Client client;
    private Pane contentPane;
    private String hotelNumber;
    private String hotelName;
    private String hotelAdress;
    private Label hotelLabel;

    private String roomNumbers;

    private TableView<RoomTableModel> tableView;
    private ObservableList<RoomTableModel> data;
    private RoomsNumberCallback roomsNumberCallback;

    private LocalDate startDate;
    private LocalDate endDate;

    private volatile boolean isRunning = true;

    public SelectRoomsView(Client client, Pane contentPane, LocalDate startDate, LocalDate endDate, String hotelNumber, RoomsNumberCallback roomsNumberCallback) {
        this.setStyle("-fx-border-color: black; -fx-background-color: white;");
        this.setPrefSize(800, 530);
        this.setLayoutX(30);
        this.setLayoutY(100);
        this.contentPane = contentPane;
        this.startDate = startDate;
        this.endDate = endDate;
        this.roomsNumberCallback = roomsNumberCallback;
        this.hotelNumber = hotelNumber;
        this.hotelName = hotelName;
        this.hotelAdress = hotelAdress;
        this.roomNumbers = "";

        this.initComponents();
        this.client = client;

        this.roomList(Action.HOTEL_ROOMS_BOOKING, String.valueOf(startDate), String.valueOf(endDate), hotelNumber);
        //this.hotelInfo(Action.HOTEL_SEARCH, hotelName);

        Thread thread = new Thread(this);
        thread.start();
    }

    private void initComponents() {
        HBox titleBar = new HBox();

        Label title = new Label("Room List");
        Button closeBtn = new Button("X");

        tableView = new TableView<>();
        data = FXCollections.observableArrayList();
        FilteredList<RoomTableModel> dataFiltered = new FilteredList<>(data, data -> true);

        Label searchLabel = new Label("Search for a room:");
        TextField searchRoom = new TextField();
        searchRoom.setPromptText("Room number:");

        TextArea infoTextArea = new TextArea();
        infoTextArea.setPromptText("Information of your hotel");
        infoTextArea.setPrefRowCount(1);
        infoTextArea.setPrefColumnCount(20);
        infoTextArea.setEditable(false);
        infoTextArea.setWrapText(true);

        hotelLabel = new Label("Hotel Name: " + this.hotelName);
        hotelLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        infoTextArea.setText("Hotel Name: " + this.hotelName);

        VBox infoBox = new VBox(50);
        infoBox.getChildren().addAll(infoTextArea);

        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.getChildren().addAll(searchLabel, searchRoom, infoTextArea);

        searchRoom.textProperty().addListener((observable, oldValue, newValue) -> {
            dataFiltered.setPredicate(room -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return room.getRoomNumber().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<RoomTableModel> sortedInfo = new SortedList<>(dataFiltered);
        sortedInfo.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedInfo);

        TableColumn<RoomTableModel, String> column1 = new TableColumn<>("Room Number");
        column1.setCellValueFactory(cellData -> cellData.getValue().roomNumberProperty());

        TableColumn<RoomTableModel, String> column2 = new TableColumn<>("Room Status");
        column2.setCellValueFactory(cellData -> cellData.getValue().roomStatusProperty());

        TableColumn<RoomTableModel, String> column3 = new TableColumn<>("Room Style");
        column3.setCellValueFactory(cellData -> cellData.getValue().roomStyleProperty());

        TableColumn<RoomTableModel, Number> column4 = new TableColumn<>("Room Price");
        column4.setCellValueFactory(cellData -> cellData.getValue().roomPriceProperty());

        TableColumn<RoomTableModel, String> column5 = new TableColumn<>("Hotel Number");
        column5.setCellValueFactory(cellData -> cellData.getValue().roomHotelNumberProperty());

        TableColumn<RoomTableModel, Boolean> column6 = new TableColumn<>("Select");
        column6.setCellFactory(col -> new TableCell<RoomTableModel, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(e -> {
                    RoomTableModel room = getTableView().getItems().get(getIndex());
                    roomNumbers += room.getRoomNumber() + ",";
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(checkBox);
                }
            }
        });

        tableView.getColumns().addAll(column1, column2, column3, column4, column5, column6);

        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setSpacing(10);
        titleBar.setStyle("-fx-background-color: #cccccc; -fx-padding: 5;");
        titleBar.getChildren().addAll(title, closeBtn);

        closeBtn.setOnAction(e -> {
            this.isRunning = false;
            contentPane.getChildren().remove(this);
        });

        Button btnAddRoom = new Button("Add Rooms");
        btnAddRoom.setOnAction(e -> {
            if (roomNumbers != null) {
                if (roomsNumberCallback != null) {
                    roomsNumberCallback.roomsNumberSelect(roomNumbers);
                }
                contentPane.getChildren().remove(this); // Cierra la vista al confirmar
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Selection incomplete");
                alert.setHeaderText(null);
                alert.setContentText("Please, select a room valid.");
                alert.showAndWait();
            }
        });

        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        contenido.getChildren().addAll(searchBox, tableView, btnAddRoom);

        double widthPercent = 1.0 / tableView.getColumns().size();
        for (TableColumn<?, ?> column : tableView.getColumns()) {
            column.prefWidthProperty().bind(tableView.widthProperty().multiply(widthPercent));
        }

        this.setTop(titleBar);
        this.setCenter(contenido);

        contentPane.getChildren().add(this);
    }

    private void roomList(String accion, String starDate, String endDate, String hotelNumber) {
        this.client.getSend().println(accion + "|||" + starDate + "|||" + endDate + "|||" + hotelNumber);
    }

    private void hotelInfo(String accion, String hotelName) {
        this.client.getSend().println(accion + "|||" + hotelName);
    }

    private void roomDelete(String accion, String roomNumber, String hotelNumber) {
        this.client.getSend().println(accion + "|||" + roomNumber + "|||" + hotelNumber);
    }

    private void refreshTable() {
        data.clear();
        this.roomList(Action.HOTEL_ROOMS, String.valueOf(this.startDate), String.valueOf(this.endDate), this.hotelNumber);
    }

    @Override
    public void run() {
        while (this.isRunning) {
            try {
                if (this.client.isMostrarRoomHotel()) {
                    String resultRooms = this.client.getHotelRooms();
                    Platform.runLater(() -> {
                        if (data.isEmpty()) {  // solo carga si no hay datos aún
                            String[] rows = resultRooms.split("\n");
                            for (String row : rows) {
                                String[] parts = row.split("\\|\\|\\|");
                                if (parts.length == 5) {
                                    RoomTableModel room = new RoomTableModel(
                                            parts[0].trim(),  // roomNumber
                                            parts[1].trim(),  // roomStatus
                                            parts[2].trim(),  // roomStyle
                                            Double.parseDouble(parts[3].trim()), // roomPrice
                                            parts[4].trim() // hotelNumber
                                    );
                                    data.add(room);
                                }
                            }
                        }
                    });
                    this.client.setHotelRooms("");
                    this.client.setMostrarRoomHotel(false);
                }
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
