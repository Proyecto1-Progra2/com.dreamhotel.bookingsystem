package view;

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

public class LoginReceptionView extends BorderPane {
    //inicia el programa
    private Pane contentPane;
    private final Client client;
    private final Stage stage;

    private Label titleLogin, titlePassword;
    private TextField tUser, tPassword;
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
    }

    private void initComponents() {
        //Login del recepcionista
        Collection<Node> nodes = new ArrayList<>();

        titleLogin = new Label("Login");
        titleLogin.setStyle("-fx-font-family: 'Elephant'; -fx-font-size: 15px; -fx-text-fill: #596fa8  ;");


        tUser = new TextField();
        tUser.setPrefWidth(200);


        titlePassword = new Label("Password");
        titlePassword.setStyle("-fx-font-family: 'Elephant'; -fx-font-size: 15px; -fx-text-fill: #596fa8 ;");

        tPassword = new TextField();
        tPassword.setPrefWidth(200);

        Button btnLogin = new Button("Login");
        btnLogin.setStyle("-fx-background-color: #596fa8; -fx-text-fill: white;");

        Button btnRegister = new Button("Register");
        btnRegister.setStyle("-fx-background-color: #596fa8; -fx-text-fill: white;");


        VBox vbox = new VBox(10); // espacio entre nodos
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(200));
        vbox.getChildren().addAll(titleLogin, tUser, titlePassword, tPassword, btnLogin, btnRegister);

        this.setCenter(vbox); // Coloca el VBox en el centro del BorderPane

        btnLogin.setOnAction(e -> {
            new MainView(client, stage);

//            if (tUser.getText().isEmpty() || tPassword.getText().isEmpty()) {
//                alert = FXUtility.alert("Login", "Login Receptionist");
//                alert.setAlertType(Alert.AlertType.WARNING);
//
//                if (tUser.getText().isEmpty()) {
//                    alert.setContentText("¡El nombre de usuario no puede estar vacío!");
//                } else {
//                    alert.setContentText("¡La contraseña no puede estar vacía!");
//                }
//
//                alert.showAndWait();
//                return;
//            }
//
//            receptionistLogin(tUser.getText(), tPassword.getText());
//            //esto debe devolver un boolean para saber si se puede loguear
//            if (true) {
//                //ir al mainView
//                new MainView(client, stage);
//                alert = FXUtility.alert("Welcome", "Login Receptionist");
//                alert.setAlertType(Alert.AlertType.CONFIRMATION);
//                alert.setContentText("Your username and password are correct!");
//                alert.showAndWait();
//            } else {
//                alert = FXUtility.alert("Login", "Login Receptionist");
//                alert.setAlertType(Alert.AlertType.INFORMATION);
//                alert.setContentText("Your username and password are correct!");
//                alert.showAndWait();
//            }
        });

        btnRegister.setOnAction(e -> new RegisterView(this.client, this.contentPane));

        contentPane = new Pane();
        contentPane.setPrefSize(1000, 1000);
        contentPane.setStyle("-fx-background-color:#dcdee2;");

        Image image = new Image(getClass().getResource("/image3.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);

// para centrar la imagen
        double paneWidth = 1000;
        double imageWidth = 150;
        double imageX = (paneWidth - imageWidth) / 1.9999;// la mitad osea en el centro
        double imageY = (1000 - (imageWidth * (image.getHeight() / image.getWidth()))) / 11;// calculado con priuebas

        imageView.setLayoutX(imageX);
        imageView.setLayoutY(imageY);


        Label bienvenida = new Label("Register here and let you have a great moment!");
        bienvenida.setStyle("-fx-font-family: 'Elephant'; -fx-font-size: 30px; -fx-text-fill:#566ead ;");
        bienvenida.setLayoutX(80);
        bienvenida.setLayoutY(20);
        contentPane.getChildren().addAll(bienvenida, imageView);

        nodes.add(contentPane);

        this.getChildren().addAll(nodes);
    }

    private void receptionistLogin(String username, String password) {
        this.client.getSend().println(Action.RECEPTIONIST_LOGIN + "|||" + username + "|||" + password);
    }

    // Metodo auxiliar para mostrar alertas
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}