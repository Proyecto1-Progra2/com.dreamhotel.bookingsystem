package view.roomView;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import sockets.Client;
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
        this.setPrefSize(400, 300);
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
        tableView.setItems(data);

        TableColumn<RoomTableModel, String> column1 = new TableColumn<>("Room Number");
        column1.setCellValueFactory(cellData -> cellData.getValue().roomNumberProperty());

        TableColumn<RoomTableModel, String> column2 = new TableColumn<>("Room Status");
        column2.setCellValueFactory(cellData -> cellData.getValue().roomStatusProperty());

        TableColumn<RoomTableModel, String> column3 = new TableColumn<>("Room Style");
        column3.setCellValueFactory(cellData -> cellData.getValue().roomStyleProperty());

        TableColumn<RoomTableModel, Number> column4 = new TableColumn<>("Room Price");
        column4.setCellValueFactory(cellData -> cellData.getValue().roomPriceProperty());

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

        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        contenido.getChildren().addAll(tableView);

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
                                if (parts.length == 4) {
                                    RoomTableModel room = new RoomTableModel(
                                            parts[2].trim(),  // roomNumber
                                            parts[0].trim(),  // roomStatus
                                            parts[1].trim(),  // roomStyle
                                            Double.parseDouble(parts[3].trim()) // roomPrice
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
