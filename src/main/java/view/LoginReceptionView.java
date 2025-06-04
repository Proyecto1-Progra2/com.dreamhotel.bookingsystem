package view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sockets.Client;
import utils.Action;
import utils.FXUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class LoginReceptionView extends BorderPane implements Runnable {
    //inicia el programa
    private Pane contentPane;
    private final Client client;
    private final Stage stage;

    private Label titleLogin, titlePassword;
    private TextField tUser, tPassword;
    private Button btnLogin;
    private Alert alert;

    public LoginReceptionView(Stage stage) {
        this.setPrefSize(1000, 700);
        this.stage = stage;
        Scene scene = new Scene(this);
        stage.setTitle("Login Receptionist");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

        this.initComponents();
        try {
            this.client = new Client("localhost", 5025);
            this.client.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Thread thread = new Thread(this);
        thread.start();
    }

    private void initComponents() {
        // Login del recepcionista
        Collection<Node> nodes = new ArrayList<>();

        // Cambios de color en etiquetas
        titleLogin = new Label("Login");
        titleLogin.setStyle("-fx-font-family: 'Elephant'; -fx-font-size: 15px; -fx-text-fill: #2b2b2b;");

        tUser = new TextField();
        tUser.setPrefWidth(200);

        titlePassword = new Label("Password");
        titlePassword.setStyle("-fx-font-family: 'Elephant'; -fx-font-size: 15px; -fx-text-fill: #2b2b2b;");

        tPassword = new TextField();
        tPassword.setPrefWidth(200);

        this.btnLogin = new Button("Login");
        btnLogin.setStyle("-fx-background-color: #596fa8; -fx-text-fill: white;");

        Button btnRegister = new Button("Register");
        btnRegister.setStyle("-fx-background-color: #596fa8; -fx-text-fill: white;");

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(200));
        vbox.getChildren().addAll(titleLogin, tUser, titlePassword, tPassword, btnLogin, btnRegister);

        this.setCenter(vbox);

        // Cambia el fondo general de la interfaz principal
        this.setStyle("-fx-background-color: #f2f2f2;");

        btnLogin.setOnAction(e -> {
            if (tUser.getText().isEmpty() || tPassword.getText().isEmpty()) {
                alert = FXUtility.alert("Login", "Login Receptionist");
                alert.setAlertType(Alert.AlertType.WARNING);
                if (tUser.getText().isEmpty()) {
                    alert.setContentText("¡El nombre de usuario no puede estar vacío!");
                } else {
                    alert.setContentText("¡La contraseña no puede estar vacía!");
                }
                alert.showAndWait();
            } else {
                receptionistLogin(tUser.getText(), tPassword.getText());
            }
        });

        btnRegister.setOnAction(e -> new RegisterView(this.client, this.contentPane));

        // Banner superior y contenido gráfico
        contentPane = new Pane();
        contentPane.setPrefSize(1000, 1000);
        contentPane.setStyle("-fx-background-color:#f2f2f2;");


        Image image = new Image(getClass().getResource("/image3.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);
        imageView.setLayoutX(400);
        imageView.setLayoutY(90);


        Label bienvenida = new Label("Join us today and enjoy exclusive features!");
        bienvenida.setStyle("-fx-font-family: 'Elephant'; -fx-font-size: 26px; -fx-text-fill: #596fa8;");
        bienvenida.setLayoutX(160); // centrado aproximado
        bienvenida.setLayoutY(40);

        contentPane.getChildren().addAll(bienvenida, imageView);
        nodes.add(contentPane);
        this.getChildren().addAll(nodes);
    }

    private void receptionistLogin(String username, String password) {
        this.client.getSend().println(Action.RECEPTIONIST_SEARCH + "|||" + username + "|||" + password);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void run() {
        while (true) {
            if (this.client.getLoged() == 1) {
                Platform.runLater(() -> {
                    new MainView(client, stage);
                    alert = FXUtility.alert("Welcome", "Login Receptionist");
                    alert.setAlertType(Alert.AlertType.CONFIRMATION);
                    alert.setContentText("Your username and password are correct!");
                    alert.showAndWait();
                });
                this.client.setLoged(0);
            } else if (this.client.getLoged() == 2) {
                Platform.runLater(() -> {
                    alert = FXUtility.alert("Login", "Login Receptionist");
                    alert.setAlertType(Alert.AlertType.INFORMATION);
                    alert.setContentText("Your username or password aren´t correct!");
                    alert.showAndWait();
                });
                this.client.setLoged(0);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}