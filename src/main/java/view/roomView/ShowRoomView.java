package view.roomView;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import sockets.Client;
import table.HotelTableModel;
import table.RoomTableModel;
import utils.Action;

public class ShowRoomView extends BorderPane implements Runnable {

    private final Client client;
    private final Pane contentPane;

    private TableView<RoomTableModel> tableView;
    private ObservableList<RoomTableModel> data;

    private final boolean isRunning = true;

    public ShowRoomView(Client client, Pane contentPane) {
        this.setStyle("-fx-border-color: black; -fx-background-color: white;");
        this.setPrefSize(530, 530);
        this.setLayoutX(100);
        this.setLayoutY(100);
        this.contentPane = contentPane;

        this.initComponents();
        this.client = client;
        this.RoomList(Action.ROOM_LIST);

        Thread thread = new Thread(this);
        thread.start();
    }

    private void initComponents() {
        // Título con botón cerrar
        HBox titleBar = new HBox();
        Label title = new Label("Room List");
        Button closeBtn = new Button("X");

        tableView = new TableView<>();
        data = FXCollections.observableArrayList();

        FilteredList<RoomTableModel> dataFiltered = new FilteredList<>(data, data -> true);

        Label searchLabel = new Label("Search for a room:");
        TextField searchRoom = new TextField();
        searchRoom.setPromptText("Room number:");

        // Nuevo TextArea a la par del TextField
        TextArea textArea = new TextArea();
        textArea.setPromptText("Additional info or notes...");
        textArea.setPrefRowCount(3);
        textArea.setPrefColumnCount(15);
        textArea.setPrefWidth(200);
        textArea.setMinWidth(150);

        // Filtra de una con el texto que ingresa desde dataFiltered
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

        tableView.getColumns().addAll(column1, column2, column3, column4, column5);

        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setSpacing(10);
        titleBar.setStyle("-fx-background-color: #cccccc; -fx-padding: 5;");
        titleBar.getChildren().addAll(title, closeBtn);

        closeBtn.setOnAction(e -> {
            this.setVisible(false);
            contentPane.getChildren().remove(this);
        });

        final double[] dragOffset = new double[2];
        titleBar.setOnMousePressed(e -> {
            dragOffset[0] = e.getSceneX() - this.getLayoutX();
            dragOffset[1] = e.getSceneY() - this.getLayoutY();
        });

        titleBar.setOnMouseDragged(e -> {
            double newX = e.getSceneX() - dragOffset[0];
            double newY = e.getSceneY() - dragOffset[1];

            newX = Math.max(0, Math.min(newX, contentPane.getWidth() - this.getWidth()));
            newY = Math.max(0, Math.min(newY, contentPane.getHeight() - this.getHeight()));

            this.setLayoutX(newX);
            this.setLayoutY(newY);
        });

        // Contenedor horizontal para el searchLabel, searchRoom y el TextArea
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.getChildren().addAll(searchLabel, searchRoom, textArea);

        // Que el TextArea crezca si hay espacio
        HBox.setHgrow(textArea, Priority.ALWAYS);

        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        contenido.getChildren().addAll(searchBox, tableView);

        // Para que la tabla crezca verticalmente y ocupe espacio
        VBox.setVgrow(tableView, Priority.ALWAYS);

        this.setTop(titleBar);
        this.setCenter(contenido);

        contentPane.getChildren().add(this);
    }


    // para encontrar la accion
    private void RoomList(String accion) {
        this.client.getSend().println(accion + "-");
    }

    @Override
    public void run() {
        while (this.isRunning) {
            try {
                if (this.client.isHabitacionesMostrado()) {
                    String resultRooms = this.client.getMostrarRooms();
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
                                            parts[4].trim() // hotelNumber
                                    );
                                    data.add(room);
                                }
                            }
                        }
                    });
                    this.client.setMostrarRooms("");
                    this.client.setHabitacionesMostrado(false);
                }
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}