package view.bookingView;

import domain.Host;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import sockets.Client;
import utils.Action;
import utils.FXUtility;

import java.util.ArrayList;
import java.util.Collection;

public class RegisterHostView extends BorderPane implements Runnable{
    private Client client;

    private Pane contentPane;

    // id, name, last name , phone number, address mail, country
    private TextField id, name, lastName,  phoneNumber, address, email, country;
    private Label titleRegister, LabeltitleId, LabeltitleName, LabeltitleLastName, LabeltitleHostNumber,LabeladressLabel , labelMail,labelCountry, LabeltitleUsername, titlePassword;

    private volatile boolean isRunning = true;

    //private ReceptionistData receptionistRegister;

        private Alert alert = FXUtility.alert("Host", "Register Host");//acomodar el alert

    //dentro del constructor va Client client
    public RegisterHostView(Client client, Pane contentPane) {
        this.setStyle("-fx-border-color: black; -fx-background-color: white;");
        this.setPrefSize(530, 530);
        this.setLayoutX(100);
        this.setLayoutY(100);

        this.contentPane = contentPane;

        this.initComponents();
        this.client = client;

        Thread thread = new Thread(this);
        thread.start();
    }

    private void initComponents() {
        Collection<Node> nodes = new ArrayList<>();

        // Título con botón cerrar
        HBox titleBar = new HBox();
        Label title = new Label("Register Host");
        Button closeBtn = new Button("X");

        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setSpacing(10);
        titleBar.setStyle("-fx-background-color: #cccccc; -fx-padding: 5;");
        titleBar.getChildren().addAll(title, closeBtn);

        closeBtn.setOnAction(e -> {
            this.isRunning = false;
            this.setVisible(false);

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

        // Recuperar datos
        name = new TextField();
        lastName = new TextField();
        phoneNumber = new TextField();
        id = new TextField();
        address = new TextField();
        email = new TextField();
        country = new TextField();

        Button btnRegister = new Button("Register");

        // Contenido del registro
        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        contenido.getChildren().addAll(
                titleRegister = new Label("Register New Host"),
                LabeltitleId= new Label("ID:"),
                id,
                LabeltitleName = new Label("Name: Your exact name will be used for your username."),
                name,
                LabeltitleLastName = new Label("Last Name: Your exact last name will be used for your username."),
                lastName,
                LabeltitleHostNumber = new Label("Phone Number:"),
                phoneNumber,
                LabeladressLabel = new Label("Host Adress:"),
                address,
                labelMail = new Label("Mail"),
                email,
                labelCountry= new Label("Country"),
                country,
                btnRegister
        );


        btnRegister.setOnAction(e -> {
            // Validación de campos vacíos
            if (id.getText().isEmpty() || name.getText().isEmpty() || lastName.getText().isEmpty() ||
                    phoneNumber.getText().isEmpty() || address.getText().isEmpty() ||
                    email.getText().isEmpty() || country.getText().isEmpty()) {

                alert.setAlertType(Alert.AlertType.WARNING);

                if (id.getText().isEmpty()) {
                    alert.setContentText("¡El campo ID no puede estar vacío!");
                } else if (name.getText().isEmpty()) {
                    alert.setContentText("¡El nombre no puede estar vacío!");
                } else if (lastName.getText().isEmpty()) {
                    alert.setContentText("¡El apellido no puede estar vacío!");
                } else if (phoneNumber.getText().isEmpty()) {
                    alert.setContentText("¡El número de teléfono no puede estar vacío!");
                } else if (address.getText().isEmpty()) {
                    alert.setContentText("¡La dirección no puede estar vacía!");
                } else if (email.getText().isEmpty()) {
                    alert.setContentText("¡El correo no puede estar vacío!");
                } else if (country.getText().isEmpty()) {
                    alert.setContentText("¡El país no puede estar vacío!");
                }

                alert.showAndWait();
                return;
            }

            try {
                int phone = Integer.parseInt(phoneNumber.getText());

                Host host = new Host(
                        id.getText(),
                        name.getText(),
                        lastName.getText(),
                        phone,
                        address.getText(),
                        email.getText(),
                        country.getText()
                );

                hostRegister(host);

//                alert.setAlertType(Alert.AlertType.INFORMATION);
//                alert.setContentText("¡Host registrado exitosamente!");
//                alert.showAndWait();

                // Limpiar campos


                contentPane.getChildren().remove(this);

            } catch (NumberFormatException ex) {
                alert.setAlertType(Alert.AlertType.ERROR);
                alert.setContentText("¡El número de teléfono debe ser numérico!");
                alert.showAndWait();
            }
        });


        this.setTop(titleBar);
        this.setCenter(contenido);

        //Cabiossss
        contentPane.getChildren().add(this);

        contentPane = new Pane();
        contentPane.setPrefSize(1000, 1000);
        contentPane.setStyle("-fx-background-color:#cad1ce;");

        nodes.add(contentPane);

        this.getChildren().addAll(nodes);

    }

    private void hostRegister(Host host) {
        this.client.getSend().println(Action.HOST_REGISTER + "|||" +
                host.getId() + "|||" +
                host.getName() + "|||" +
                host.getLastName() + "|||" +
                host.getPhoneNumber() + "|||" +
                host.getAddress() + "|||" +
                host.getEmail() + "|||" +
                host.getCountry());
    }

    @Override
    public void run() {
        while (this.isRunning) {
            try {
                int estado = this.client.getRegistered();

                if (estado == 1 || estado == 2) {
                    // Se pone en 0 inmediatamente para evitar múltiples ejecuciones
                    this.client.setRegistered(0);

                    Platform.runLater(() -> {
                        alert = FXUtility.alert("Host Receptionist", "Host");

                        if (estado == 1) {
                            alert.setContentText("Host registered successfully!");
                        } else if(estado==2){
                            alert.setContentText("The Host already exists!");
                        }
                        //confirmar la alert
                        alert.setAlertType(Alert.AlertType.CONFIRMATION);
                        alert.showAndWait();

                        this.name.setText("");
                        this.lastName.setText("");
                        this.phoneNumber.setText("");
                        this.id.setText("");
                        this.country.setText("");
                        this.address.setText("");
                        this.email.setText("");

                    });
                }

                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Hilo interrumpido durante la espera", e);
            }
        }
    }


}
