package view.roomView;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import sockets.Client;
import utils.Action;

public class ShowRoomView extends BorderPane implements Runnable {

    private Client client;
    private Pane contentPane;

    private TableView<ObservableList<String>> tableView;
    private ObservableList<ObservableList<String>> data;

    private volatile boolean isRunning = true;

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

        TableColumn<ObservableList<String>, String> column1 = new TableColumn<>("Room Number");
        column1.setCellValueFactory(param ->
                new javafx.beans.property.SimpleStringProperty(param.getValue().get(0)));
        column1.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<ObservableList<String>, String> column2 = new TableColumn<>("Room Status");
        column2.setCellValueFactory(param ->
                new javafx.beans.property.SimpleStringProperty(param.getValue().get(1)));
        column2.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<ObservableList<String>, String> column3 = new TableColumn<>("Room Style");
        column3.setCellValueFactory(param ->
                new javafx.beans.property.SimpleStringProperty(param.getValue().get(2)));
        column3.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<ObservableList<String>, String> column4 = new TableColumn<>("Room Price");
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
        while(this.isRunning){
            try{
                if(this.client.isHabitacionesMostrado()){
                    String resultRooms = this.client.getMostrarRooms();

                    Platform.runLater(() -> {
                        if(data.isEmpty()) {  // solo carga si no hay datos aún
                            String[] rows = resultRooms.split("\n");

                            for (String row : rows) {
                                String[] parts = row.split(" Room Number: | Room Status: | Room Style: | Room Price:"); //   | separa las columns
                                if (parts.length == 5) {
                                    ObservableList<String> rowData = FXCollections.observableArrayList(
                                            parts[0].replace(".", "").trim(), // separar lo que entra desde client linea 73
                                            parts[1].trim(),
                                            parts[2].trim(),
                                            parts[3].trim(),
                                            parts[4].trim()
                                            // el indice segun el valor de las columns definidas arriba
                                    );
                                    data.add(rowData);
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
