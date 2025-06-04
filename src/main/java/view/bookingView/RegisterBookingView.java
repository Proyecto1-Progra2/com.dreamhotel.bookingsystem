package view.bookingView;

import domain.Booking;
import domain.Person;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sockets.Client;
import utils.Action;
import utils.FXUtility;

import java.time.LocalDate;
import java.util.Random;

public class RegisterBookingView extends BorderPane implements Runnable {

    private Client client;
    private Pane contentPane;

    private TextField tBookingNumber, tPrice, tHotelNumber, tRoomNumber, tHostId;

    private volatile boolean isRunning = true;

    private Alert alert = FXUtility.alert("Booking", "Register Booking");

    private String hotelNumber, bookingNumber, roomsNumber;

    private LocalDate startDate;
    private LocalDate endDate;

    public RegisterBookingView(Client client, Pane contentPane, String hotelNumber) {
        this.setStyle("-fx-border-color: black; -fx-background-color: white;");
        this.setPrefSize(530, 530);
        this.setLayoutX(100);
        this.setLayoutY(100);

        this.contentPane = contentPane;
        this.hotelNumber = hotelNumber;
        this.bookingNumber = this.generateRandomBookingNumber();

        this.startDate = null;
        this.endDate = null;

        this.initComponents();
        this.client = client;
        this.bookingNumberRequest(this.bookingNumber);

        Thread thread = new Thread(this);
        thread.start();
    }

    private void initComponents() {
        // Título con botón cerrar
        HBox titleBar = new HBox();
        Label title = new Label("Register Booking");
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

        tBookingNumber = new TextField();
        tBookingNumber.setEditable(false);
        tHostId = new TextField();
        tHotelNumber = new TextField();
        tHotelNumber.setText(this.hotelNumber);
        tHotelNumber.setEditable(false);
        tPrice = new TextField();
        tPrice.setEditable(false);
        Button btnSearchHost = new Button("Search");
        Button btnCalendar = new Button("Open Calendar");
        Button btnAddRoom = new Button("Add Room");
        Button btnHost = new Button("Add Host");
        Button btnRegister = new Button("Register");

        // Contenido del formulario
        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        contenido.getChildren().addAll(
                new Label("Booking Number:"),
                tBookingNumber,
                new Label("Enter Host ID for search"),
                tHostId,
                btnSearchHost,
                new Label("If the host doesn´t exist, register it"),
                btnHost,
                new Label("Hotel Number:"),
                tHotelNumber,
                new Label("Booking Price:"),
                tPrice,
                btnCalendar,
                btnAddRoom,
                btnRegister
        );

        btnSearchHost.setOnAction(e -> {
            this.searchHost(tHostId.getText());
        });

        btnHost.setOnAction(e -> {
            // Se llama la ventana de registrar el huesped
            new RegisterHostView(client, contentPane);
        });

        btnAddRoom.setOnAction(e -> {
            SelectRoomsView selectRoomsView = new SelectRoomsView(client, contentPane, hotelNumber, (roomsNumber -> {
                this.roomsNumber = roomsNumber;
                System.out.println("Rooms number: "+this.roomsNumber);
            }));
        });

        btnRegister.setOnAction(e -> this.bookingRegister(
                new Booking(this.tBookingNumber.getText(), new Person(this.client.getHost().getName(),
                        this.client.getHost().getLastName(), this.client.getHost().getPhoneNumber()), this.startDate,
                        this.endDate, new Person(this.client.getReceptionist().getName(),
                        this.client.getReceptionist().getLastName(), this.client.getReceptionist().getPhoneNumber()),
                        this.roomsNumber, this.hotelNumber)
        ));

        btnCalendar.setOnAction(e -> {
            CalendarView calendarView = new CalendarView(client, contentPane, (start, end) -> {
                this.startDate = start;
                this.endDate = end;
                System.out.println("Fechas recibidas en RegisterBookingView: " + start + " - " + end);

                // (Opcional) Calcula un precio y lo actualiza
                long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
                double price = days * 100; // Suponiendo ₡100 por día
                tPrice.setText(String.valueOf(price));
            });
        });

        this.setTop(titleBar);
        this.setCenter(contenido);

        contentPane.getChildren().add(this);
    }

    private void bookingRegister(Booking booking) {
        this.client.getSend().println(Action.BOOKING_REGISTER + "|||" +
                booking.getBookingNumber() + "|||" +
                booking.getHost().toString() + "|||" +
                booking.getStartDate() + "|||" +
                booking.getDepartureDate() + "|||" +
                booking.getReceptionist().toString() + "|||" +
                booking.getRoomNumber() + "|||" +
                booking.getHotelNumber()
        );
    }

    private void bookingNumberRequest(String bookingNumber) {
        this.client.getSend().println(Action.REQUEST_BOOKING_NUMBER+"|||"+bookingNumber);
    }

    private void searchHost(String id) {
        this.client.getSend().println(Action.HOST_REQUEST+"|||"+id);
    }

    private String generateRandomBookingNumber() {
        Random rand = new Random();
        return String.format("%06d", rand.nextInt(1_000_000)); // Números de 6 dígitos
    }

    @Override
    public void run() {
        while (this.isRunning) {
            try {
                if (this.client.getBookingNumberExiste() == 2) {
                    // manda otra vez la accion
                    this.bookingNumber = this.generateRandomBookingNumber();
                    this.bookingNumberRequest(this.bookingNumber);
                    this.client.setBookingNumberExiste(0);
                } else if (this.client.getBookingNumberExiste() == 1) {
                    // carga el numero en el textfield
                    Platform.runLater(() -> {
                        this.tBookingNumber.setText(this.bookingNumber);
                    });
                    this.client.setBookingNumberExiste(0);
                }
                if (this.client.getRegistered() == 1) {
                    // Todas las actualizaciones de UI deben ir dentro de Platform.runLater
                    Platform.runLater(() -> { //
                        alert.setContentText("Booking registered successfully!"); //
                        alert.setAlertType(Alert.AlertType.CONFIRMATION); //
                        alert.showAndWait(); // Muestra la alerta y espera que el usuario la cierre

                        this.tBookingNumber.setText("");
                        this.tPrice.setText("");
                    });
                    this.client.setRegistered(0);
                }
                if (this.client.getHostExist() == 1) {
                    Platform.runLater(() -> {
                        alert.setContentText("Host found!");
                        alert.setAlertType(Alert.AlertType.CONFIRMATION);
                        alert.showAndWait();
                    });
                    this.client.setHostExist(0);
                } else if (this.client.getHostExist() == 2) {
                    Platform.runLater(() -> {
                        alert.setContentText("Host not found!");
                        alert.setAlertType(Alert.AlertType.CONFIRMATION);
                        alert.showAndWait();
                    });
                    this.client.setHostExist(0);
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restaura el estado de interrupción
                throw new RuntimeException("Hilo interrumpido durante la espera", e);
            }
        }
    }
}
