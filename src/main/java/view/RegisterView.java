package view;

import domain.Receptionist;
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

public class RegisterView extends BorderPane implements Runnable{

    private Client client;

    private Pane contentPane;

    private TextField tName, tLastName, tPhoneNumber, tEmployedNumber, tUsername, tPassword;
    private Label titleRegister, titleName, titleLastName, titlePhoneNumber, titleEmployedNumber, titleUsername, titlePassword;

    private volatile boolean isRunning = true;

    //private ReceptionistData receptionistRegister;

    private Alert alert = FXUtility.alert("Receptionist", "Register Receptionist");//acomodar el alert

    //dentro del constructor va Client client
    public RegisterView(Client client, Pane contentPane) {
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
        Label title = new Label("Register Receptionist");
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
        tName = new TextField();
        tLastName = new TextField();
        tPhoneNumber = new TextField();
        tEmployedNumber = new TextField();
        tUsername = new TextField();
        tUsername.setText("Your name will automatically be (name.lastname)");
        tPassword = new TextField();
        Button btnRegister = new Button("Register");

        // Contenido del registro
        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        contenido.getChildren().addAll(
                titleRegister = new Label("Register New Receptionist"),
                titleName = new Label("Name: Your exact name will be used for your username."),
                tName,
                titleLastName = new Label("Last Name: Your exact last name will be used for your username."),
                tLastName,
                titlePhoneNumber = new Label("Phone Number:"),
                tPhoneNumber,
                titleEmployedNumber = new Label("Employed Number:"),
                tEmployedNumber,
                titleUsername = new Label("UserName:"),
                tUsername,
                titlePassword = new Label("PassWord:"),
                tPassword,
                btnRegister
        );


//        btnRegister.setOnAction(e -> this.receptionistRegister(new Receptionist(tName.getText(), tLastName.getText(),
//                phoneNumber,tEmployedNumber.getText(),tUsername.getText(),tPassword.getText())));
        btnRegister.setOnAction(e -> {
            //valida de que ningún espacio quede vacio
            if (tEmployedNumber.getText().isEmpty() || tName.getText().isEmpty() || tLastName.getText().isEmpty()
                    || tPhoneNumber.getText().isEmpty() || tUsername.getText().isEmpty() || tPassword.getText().isEmpty()) {
                alert = FXUtility.alert("Register", "Register Receptionist");
                alert.setAlertType(Alert.AlertType.WARNING);

                if (tEmployedNumber.getText().isEmpty()) {
                    alert.setContentText("¡El Employed Number no puede estar vacío!");
                } else if(tName.getText().isEmpty()) {
                    alert.setContentText("¡El nombre no puede estar vacío!");
                }else if(tLastName.getText().isEmpty()) {
                    alert.setContentText("¡El apellido no puede estar vacíao!");
                }else if(tPhoneNumber.getText().isEmpty()) {
                    alert.setContentText("¡El número de telefono no puede estar vacío!");
                }else if(tUsername.getText().isEmpty()) {
                    alert.setContentText("¡El nombre de usuarui no puede estar vacío!");
                }else if(tPassword.getText().isEmpty()) {
                    alert.setContentText("¡La contraseña del usuario no puede estar vacío!");
                }

                alert.showAndWait();
            }


            receptionistRegister(new Receptionist(tEmployedNumber.getText(),tName.getText(), tLastName.getText(),
                    Integer.parseInt(tPhoneNumber.getText()) ,tName.getText()+"."+tLastName.getText(),tPassword.getText()));

//                alert = FXUtility.alert("Register Receptionist", "Receptionist");
//                alert.setContentText("Receptionist registered successfully!");
//                alert.setAlertType(Alert.AlertType.CONFIRMATION);
//                alert.showAndWait();
//
//                //limpiar los campos
//                this.tName.setText("");
//                this.tLastName.setText("");
//                this.tPhoneNumber.setText("");
//                this.tEmployedNumber.setText("");
//                this.tUsername.setText("");
//                this.tPassword.setText("");

                //cierra la pestaña
                contentPane.getChildren().remove(this);

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

//    private void receptionistRegister(Receptionist receptionist) {
//        this.client.getSend().println(Action.RECEPTIONIST_REGISTER + receptionist.toString());
//    }

    private void receptionistRegister(Receptionist receptionist) {
        // Aquí es donde construyes el mensaje para el servidor.
        // Necesitas enviar la acción primero, seguida de los datos de la recepcionista,
        // todos separados por "|||".

        //revisar
        this.client.getSend().println(Action.RECEPTIONIST_REGISTER + "|||" +
                receptionist.getEmployedNumber() + "|||" +
                receptionist.getName() + "|||" +
                receptionist.getLastName() + "|||" +
                receptionist.getPhoneNumber() + "|||" +
                receptionist.getUsername() + "|||" +
                receptionist.getPassword());
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
                        alert = FXUtility.alert("Register Receptionist", "Receptionist");

                        if (estado == 1) {
                            alert.setContentText("Receptionist registered successfully!");
                        } else if(estado==2){
                            alert.setContentText("The Receptionist already exists!");
                        }
                        //confirmar la alert
                        alert.setAlertType(Alert.AlertType.CONFIRMATION);
                        alert.showAndWait();

                        this.tName.setText("");
                        this.tLastName.setText("");
                        this.tPhoneNumber.setText("");
                        this.tEmployedNumber.setText("");
                        this.tUsername.setText("");
                        this.tPassword.setText("");

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

