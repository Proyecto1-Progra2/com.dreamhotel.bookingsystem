package view.hotelView;

import domain.Hotel;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import sockets.Client;
import table.HotelTableModel;
import utils.Action;

import java.util.ArrayList;
import java.util.Optional;

// cambios
public class ShowHotelView extends BorderPane implements Runnable {

    private Client client;
    private Pane contentPane;

    private Button btnAddHotel;

    private TableView<HotelTableModel> tableView;
    private ObservableList<HotelTableModel> data;

    private volatile boolean isRunning =true;

    public ShowHotelView(Client client, Pane contentPane) {
        this.setStyle("-fx-border-color: black; -fx-background-color: white;");
        this.setPrefSize(680, 530);
        this.setLayoutX(70);
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

        // Configuración del titleBar
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setSpacing(10);
        titleBar.setStyle("-fx-background-color: #cccccc; -fx-padding: 5;");
        titleBar.getChildren().addAll(title, closeBtn);

        // imagen
        Image image = new Image(getClass().getResource("/image.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(160);
        imageView.setPreserveRatio(true);

        HBox imageBox = new HBox();
        imageBox.setAlignment(Pos.BASELINE_CENTER);
        imageBox.getChildren().add(imageView);



        VBox topBox = new VBox(3);
        topBox.getChildren().addAll(titleBar, imageBox);

        tableView = new TableView<>();
        data = FXCollections.observableArrayList();

        // para buscar
        // ahora si funciona la flechita de los table view

        FilteredList<HotelTableModel> dataFiltered = new FilteredList<>(data, data -> true);

        Label searchLabel = new Label("Search for hotel number:");
        TextField searchHotel = new TextField();
        searchHotel.setPromptText("Hotel number:");

        // Filtra de una con el texto que ingresa desde dataFiltered
        searchHotel.textProperty().addListener((observable, info, enterInfo) -> {

            dataFiltered.setPredicate(hotel -> {
                if (enterInfo == null || enterInfo.isEmpty()) {// si no hay filtros debemos de mostrar todos
                    return true;
                }
                String enterInfoLowerCase = enterInfo.toLowerCase();// POR AQUELLO que sea lower case
                return hotel.getHotelNumber().toLowerCase().contains(enterInfoLowerCase);
            });
        });

        // aqui ordenamos primero la info que llega de hotel table
        SortedList<HotelTableModel> sortedInfo = new SortedList<>(dataFiltered);
        sortedInfo.comparatorProperty().bind(tableView.comparatorProperty());

        //agregamos con set la info a la table view
        tableView.setItems(sortedInfo);

        // Columnas
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
            private final Button btnViewRooms = new Button("Rooms");
            {
                btnEdit.setStyle("-fx-background-color: #87CEFA;");
                btnDelete.setStyle("-fx-background-color: #FA8072;");
                btnViewRooms.setStyle("-fx-background-color: #eff748;");

                btnViewRooms.setOnMouseEntered(ev -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(150), btnViewRooms);
                    st.setToX(1.03);
                    st.setToY(1.03);
                    st.play();
                });

                btnViewRooms.setOnMouseExited(ev -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(150), btnViewRooms);
                    st.setToX(1.0);
                    st.setToY(1.0);
                    st.play();
                });

                btnEdit.setOnMouseEntered(ev -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(150), btnEdit);
                    st.setToX(1.03);
                    st.setToY(1.03);
                    st.play();
                });

                btnEdit.setOnMouseExited(ev -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(150), btnEdit);
                    st.setToX(1.0);
                    st.setToY(1.0);
                    st.play();
                });

                btnDelete.setOnMouseEntered(ev -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(150), btnDelete);
                    st.setToX(1.03);
                    st.setToY(1.03);
                    st.play();
                });

                btnDelete.setOnMouseExited(ev -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(150), btnDelete);
                    st.setToX(1.0);
                    st.setToY(1.0);
                    st.play();
                });

                btnViewRooms.setOnAction(e -> {
                    HotelTableModel hotel = getTableView().getItems().get(getIndex());
                    String hotelRequest = hotel.getHotelNumber();
                    new HotelRoomsView(client, contentPane, hotelRequest);
                });

                btnEdit.setOnAction(e -> {
                    HotelTableModel hotel = getTableView().getItems().get(getIndex());
                    String hotelRequest = hotel.getHotelNumber();
                    // variable que hice porque la llamada de getResource era muy larga
                    String diseñoVnetanas = getClass().getResource("/main.css").toExternalForm();

//                    TextInputDialog numberDialog = new TextInputDialog(hotel.getHotelNumber());
//                    numberDialog.setTitle("Number edit");
//                    numberDialog.setHeaderText("Edit hotel number:");
//                    numberDialog.setContentText("Number:");
//                    numberDialog.getDialogPane().getStylesheets().add(diseñoVnetanas);
//                    numberDialog.showAndWait().ifPresent(newNumber -> hotel.hotelNumberProperty().set(newNumber));

                    TextInputDialog nameDialog = new TextInputDialog(hotel.getHotelName());
                    nameDialog.setTitle("Name edit");
                    nameDialog.setHeaderText("Edit hotel name:");
                    nameDialog.setContentText("Name:");
                    nameDialog.getDialogPane().getStylesheets().add(diseñoVnetanas);
                    nameDialog.showAndWait().ifPresent(newName -> hotel.hotelNameProperty().set(newName));

                    TextInputDialog addressDialog = new TextInputDialog(hotel.getHotelAddress());
                    addressDialog.setTitle("Address edit");
                    addressDialog.setHeaderText("Edit hotel address:");
                    addressDialog.setContentText("Address:");
                    addressDialog.getDialogPane().getStylesheets().add(diseñoVnetanas);
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
            private final HBox hbox = new HBox(5, btnEdit, btnDelete, btnViewRooms);

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

        double widthPercent = 1.0 / tableView.getColumns().size();
        for (TableColumn<?, ?> column : tableView.getColumns()) {
            column.prefWidthProperty().bind(tableView.widthProperty().multiply(widthPercent));
        }


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

        this.btnAddHotel = new Button("Add Hotel");
        this.btnAddHotel.setOnAction(e -> {
            // variable que hice porque la llamada de getResource era muy larga
            String diseñoVnetanas = getClass().getResource("/main.css").toExternalForm();

            TextInputDialog numberDialog = new TextInputDialog();
            numberDialog.setTitle("Number edit");
            numberDialog.setHeaderText("Add hotel number:");
            numberDialog.setContentText("Number:");
            numberDialog.getDialogPane().getStylesheets().add(diseñoVnetanas);
            Optional<String> number = numberDialog.showAndWait();
            if (number.isEmpty()) return;

            TextInputDialog nameDialog = new TextInputDialog("");
            nameDialog.setTitle("Name edit");
            nameDialog.setHeaderText("Add hotel name:");
            nameDialog.setContentText("Name:");
            nameDialog.getDialogPane().getStylesheets().add(diseñoVnetanas);
            Optional<String> name = nameDialog.showAndWait();
            if (name.isEmpty()) return;

            TextInputDialog addressDialog = new TextInputDialog("");
            addressDialog.setTitle("Address edit");
            addressDialog.setHeaderText("Add hotel address:");
            addressDialog.setContentText("Address:");
            addressDialog.getDialogPane().getStylesheets().add(diseñoVnetanas);
            Optional<String> address = addressDialog.showAndWait();
            if (address.isEmpty()) return;

            Hotel newHotel = new Hotel(number.get(), name.get(), address.get(), new ArrayList<>());
            hotelRegister(newHotel);
            refreshTable();
        });


        HBox searchBox = new HBox(5);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.getChildren().addAll(searchLabel, searchHotel);


        VBox contenido = new VBox(10);
        contenido.setPadding(new Insets(10));
        contenido.getChildren().addAll(btnAddHotel, searchBox, tableView);

        this.setTop(topBox);
        this.setCenter(contenido);

        contentPane.getChildren().add(this);
    }

    // para encontrar la accion
    private void hotelList(String accion) {
        this.client.getSend().println(accion + "|||");
    }

    private void hotelRegister(Hotel hotel) {
        this.client.getSend().println(Action.HOTEL_REGISTER + hotel.toString());
    }

    private void deleteHotel(String number) {
        this.client.getSend().println(Action.HOTEL_DELETE+"|||"+number);
    }

    private void updateHotel(Hotel hotel, String numberRequest) {
        this.client.getSend().println(Action.HOTEL_UPDATE+hotel.toString()+"|||"+numberRequest);
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
                                String[] parts = row.split("\\|\\|\\|");
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
