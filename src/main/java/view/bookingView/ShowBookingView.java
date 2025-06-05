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
import table.BookingTableModel;
import utils.Action;

public class ShowBookingView extends BorderPane implements Runnable {

    private final Client client;
    private final Pane contentPane;

    private TableView<BookingTableModel> tableView;
    private ObservableList<BookingTableModel> data;

    private volatile boolean isRunning = true;
    private final String hotelNumber;

    public ShowBookingView(Client client, Pane contentPane, String hotelNumber) {
        this.setStyle("-fx-border-color: black; -fx-background-color: white;");
        this.setPrefSize(850, 530);
        this.setLayoutX(70);
        this.setLayoutY(100);
        this.contentPane = contentPane;
        this.hotelNumber = hotelNumber;

        this.initComponents();
        this.client = client;

        this.bookingList(Action.BOOKING_LIST, hotelNumber);

        Thread thread = new Thread(this);
        thread.start();
    }

    private void initComponents() {
        // Título con botón cerrar
        HBox titleBar = new HBox();
        Label title = new Label("Show Bookings");
        Button closeBtn = new Button("X");

        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setSpacing(10);
        titleBar.setStyle("-fx-background-color: #cccccc; -fx-padding: 5;");
        titleBar.getChildren().addAll(title, closeBtn);

        closeBtn.setOnAction(e -> {
            this.isRunning = false;
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
        data = FXCollections.observableArrayList();

        // para buscar
        // ahora si funciona la flechita de los table view

        FilteredList<BookingTableModel> dataFiltered = new FilteredList<>(data, data -> true);

        Label searchLabel = new Label("Search for booking number:");
        TextField searchHotel = new TextField();
        searchHotel.setPromptText("Booking number:");

        // Filtra de una con el texto que ingresa desde dataFiltered
        searchHotel.textProperty().addListener((observable, info, enterInfo) -> {

            dataFiltered.setPredicate(booking -> {
                if (enterInfo == null || enterInfo.isEmpty()) {// si no hay filtros debemos de mostrar todos
                    return true;
                }
                String enterInfoLowerCase = enterInfo.toLowerCase();// POR AQUELLO que sea lower case
                return booking.getBookingNumber().toLowerCase().contains(enterInfoLowerCase);
            });
        });

        // aqui ordenamos primero la info que llega de hotel table
        SortedList<BookingTableModel> sortedInfo = new SortedList<>(dataFiltered);
        sortedInfo.comparatorProperty().bind(tableView.comparatorProperty());

        //agregamos con set la info a la table view
        tableView.setItems(sortedInfo);

        //-> Columns
        TableColumn<BookingTableModel, String> column1 = new TableColumn<>("Booking Number");
        column1.setCellValueFactory(cellData -> cellData.getValue().bookingNumberProperty());

        TableColumn<BookingTableModel, String> column2 = new TableColumn<>("Host Name");
        column2.setCellValueFactory(cellData -> cellData.getValue().hostNameProperty());

        TableColumn<BookingTableModel, String> column3 = new TableColumn<>("Start Date");
        column3.setCellValueFactory(cellData -> cellData.getValue().startDateProperty());

        TableColumn<BookingTableModel, String> column4 = new TableColumn<>("Departure Date");
        column4.setCellValueFactory(cellData -> cellData.getValue().departureDateProperty());

        TableColumn<BookingTableModel, String> column5 = new TableColumn<>("Receptionist Name");
        column5.setCellValueFactory(cellData -> cellData.getValue().receptionistNameProperty());

        TableColumn<BookingTableModel, String> column6 = new TableColumn<>("Room Number");
        column6.setCellValueFactory(cellData -> cellData.getValue().roomNumberProperty());

        TableColumn<BookingTableModel, String> column7 = new TableColumn<>("Hotel Number");
        column7.setCellValueFactory(cellData -> cellData.getValue().hotelNumberProperty());

        TableColumn<BookingTableModel, Void> columnActions = new TableColumn<>("Actions");
        columnActions.setCellFactory(col -> new TableCell<BookingTableModel, Void>() {
            private final Button btnRooms = new Button("Delete");

            {
                btnRooms.setStyle("-fx-background-color: #FA8072;");

                btnRooms.setOnMouseEntered(ev -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(150), btnRooms);
                    st.setToX(1.03);
                    st.setToY(1.03);
                    st.play();
                });

                btnRooms.setOnMouseExited(ev -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(150), btnRooms);
                    st.setToX(1.0);
                    st.setToY(1.0);
                    st.play();
                });

                btnRooms.setOnAction(e -> {
                    BookingTableModel booking = getTableView().getItems().get(getIndex());
                    String bookingNumber = booking.getBookingNumber();
                    deleteBooking(bookingNumber);
                    refreshTable();
                });
            }

            private final HBox hbox = new HBox(5, btnRooms);

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

        this.tableView.getColumns().addAll(column1, column2, column3, column4, column5, column6, column7, columnActions);
        double widthPercent = 1.0 / tableView.getColumns().size();
        for (TableColumn<?, ?> column : tableView.getColumns()) {
            column.prefWidthProperty().bind(tableView.widthProperty().multiply(widthPercent));
        }


        Button btnRegister = new Button("Add Booking");
        btnRegister.setOnAction(e -> {
            new RegisterBookingView(this.client, this.contentPane, this.hotelNumber);
        });

        HBox searchBox = new HBox(5);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.getChildren().addAll(searchLabel, searchHotel);

        // -> Se añade lo que se quiere mostrar en la interfaz
        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        contenido.getChildren().addAll(btnRegister, searchBox, this.tableView);

        this.setTop(titleBar);
        this.setCenter(contenido);

        contentPane.getChildren().add(this);
    }

    private void bookingList(String accion, String hotelNumber) {
        this.client.getSend().println(accion + "|||" + hotelNumber);
    }

    private void refreshTable() {
        data.clear();
        this.bookingList(Action.BOOKING_LIST, hotelNumber);
    }

    private void deleteBooking(String number) {
        this.client.getSend().println(Action.BOOKING_DELETE+"|||"+number);
    }

    @Override
    public void run() {
        while (this.isRunning) {
            try {
                if (this.client.isBookingMostrado()) {
                    String resultBookings = this.client.getBookings();
                    Platform.runLater(() -> {
                        if (data.isEmpty()) {  // solo carga si no hay datos aún
                            String[] rows = resultBookings.split("\n");
                            for (String row : rows) {
                                String[] parts = row.split("\\|\\|\\|");
                                if (parts.length == 9) {
                                    BookingTableModel booking = new BookingTableModel(
                                            parts[0].trim(),
                                            parts[1].trim() + " " + parts[2].trim(),
                                            parts[3].trim(),
                                            parts[4].trim(),
                                            parts[5].trim() + " " + parts[6].trim(),
                                            parts[7].trim(),
                                            parts[8].trim()
                                    );
                                    data.add(booking);
                                }
                            }
                        }

                    });

                    this.client.setBookings("");
                    this.client.setBookingMostrado(false);
                }
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
