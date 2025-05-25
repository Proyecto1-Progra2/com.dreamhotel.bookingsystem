package view.hotelView;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import view.imagesView.RoomImagesView;

public class HotelRoomsView extends BorderPane implements Runnable {

    private Client client;
    private Pane contentPane;
    private String hotelNumber;

    private TableView<RoomTableModel> tableView;
    private ObservableList<RoomTableModel> data;

    private volatile boolean isRunning =true;

    public HotelRoomsView(Client client, Pane contentPane, String hotelNumber) {
        this.setStyle("-fx-border-color: black; -fx-background-color: white;");
        this.setPrefSize(680, 530);
        this.setLayoutX(70);
        this.setLayoutY(100);
        this.contentPane = contentPane;

        this.initComponents();
        this.client = client;

        this.roomList(Action.HOTEL_ROOMS, hotelNumber);

        Thread thread = new Thread(this);
        thread.start();
    }

    private void initComponents() {
        HBox titleBar = new HBox();
        Label title = new Label("Room List");
        Button closeBtn = new Button("X");

        tableView = new TableView<>();
        data = FXCollections.observableArrayList();
        tableView.setItems(data);

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

        TableColumn<RoomTableModel, Void> column6 = new TableColumn<>("Images");
        column6.setCellFactory(col -> new TableCell<RoomTableModel, Void>() {
            private final Button btnImage = new Button("View");
            {
                btnImage.setStyle("-fx-background-color: #eff748;");

                btnImage.setOnMouseEntered(ev -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(150), btnImage);
                    st.setToX(1.03);
                    st.setToY(1.03);
                    st.play();
                });

                btnImage.setOnMouseExited(ev -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(150), btnImage);
                    st.setToX(1.0);
                    st.setToY(1.0);
                    st.play();
                });

                btnImage.setOnAction(e -> {
                    RoomTableModel room = getTableView().getItems().get(getIndex());
                    String roomNumber = room.getRoomNumber();
                    new RoomImagesView(client, contentPane, roomNumber);
                });
            }
            private final HBox hbox = new HBox(3, btnImage);

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

        tableView.getColumns().addAll(column1, column2, column3, column4, column5, column6);

        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setSpacing(10);
        titleBar.setStyle("-fx-background-color: #cccccc; -fx-padding: 5;");
        titleBar.getChildren().addAll(title, closeBtn);

        closeBtn.setOnAction(e -> {
            this.isRunning = false;
            contentPane.getChildren().remove(this);
        });

        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        contenido.getChildren().addAll(tableView);

        double widthPercent = 1.0 / tableView.getColumns().size();
        for (TableColumn<?, ?> column : tableView.getColumns()) {
            column.prefWidthProperty().bind(tableView.widthProperty().multiply(widthPercent));
        }

        this.setTop(titleBar);
        this.setCenter(contenido);

        contentPane.getChildren().add(this);
    }

    private void roomList(String accion, String hotelNumber) {
        this.client.getSend().println(accion + "-" + hotelNumber);
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
                                String[] parts = row.split("-");
                                if (parts.length == 5) {
                                    RoomTableModel room = new RoomTableModel(
                                            parts[0].trim(),  // roomNumber
                                            parts[1].trim(),  // roomStatus
                                            parts[2].trim(),  // roomStyle
                                            Double.parseDouble(parts[3].trim()), // roomPrice
                                            parts[4].trim() // roomNumber
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
