package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

    private Client client;

    private Label titleLogin, titlePassword;

    private TextField tUser, tPassword;

    private Stage primaryStage;
    private Alert alert;

    public LoginReceptionView(Stage stage) {
        this.setPrefSize(1000, 700);
        this.primaryStage = stage;
        try {
            this.client = new Client("localhost", 5025);
            this.client.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        try {
//            // Inicializar ReceptionistData aquí para evitar NullPointerException
//            this.receptionistData = new ReceptionistData(); // Asegúrate de la ruta de tu archivo de datos
//        } catch (IOException e) {
//            System.err.println("Error al inicializar ReceptionistData: " + e.getMessage());
//            showAlert(Alert.AlertType.ERROR, "Error del sistema", "No se pudo iniciar el gestor de datos de recepcionistas.");
//            // Considera Platform.exit(); si el error es crítico y la app no puede continuar.
//        }

        this.contentPane = new Pane();

        Scene scene = new Scene(this);
        stage.setTitle("Login Receptionist");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

        this.initComponents();
    }

    private void initComponents() {
        //Login del recepcionista
        Collection<Node> nodes = new ArrayList<>();

        titleLogin = new Label("Login");
        tUser = new TextField();
        titlePassword = new Label("Password");
        tPassword = new TextField();
        Button btnLogin = new Button("Login");
        Button btnRegister = new Button("Register");

        VBox vbox = new VBox(10); // espacio entre nodos
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(200));
        vbox.getChildren().addAll(titleLogin, tUser, titlePassword, tPassword, btnLogin, btnRegister);

        this.setCenter(vbox); // Coloca el VBox en el centro del BorderPane

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
                return;
            }

            //buscar en el receptionist.data si se encuentra ese recepcionsita, con ese username y password
            //hacer metodo que en un registro de recepcionistas busque el user y pass

//                boolean dato = receptionistData.receptionistLogin(tUser.getText(), tPassword.getText());
//                System.out.println("El usuario esta registrado: "+dato);
//                System.out.println(receptionistData.findAll());

            receptionistLogin(tUser.getText(), tPassword.getText());
            //esto debe devolver un boolean para saber si se puede loguear
            if(true){
                //ir al mainView
//                  MainView(contentPane);
                    alert = FXUtility.alert("Welcome", "Login Receptionist");
                    alert.setAlertType(Alert.AlertType.CONFIRMATION);
                    alert.setContentText("Tu username y password son correctos");
                    alert.showAndWait();

                    // 1. Crear una instancia de la nueva vista (DashboardView)
                    MainView mainView = new MainView(client, primaryStage);

                    // 2. Crear una nueva Scene con la DashboardView
                    Scene dashboardScene = new Scene(mainView);

//                    // 3. Establecer la nueva Scene en el Stage principal
//                    primaryStage.setScene(dashboardScene);
//                    primaryStage.setTitle("Panel de Control del Hotel"); // Cambia el título de la ventana
//                    primaryStage.centerOnScreen(); // Opcional: volver a centrar la ventana
//                    // No necesitas llamar a primaryStage.show() de nuevo, ya está visible.
            }else{
                    alert = FXUtility.alert("Login", "Login Receptionist");
                    alert.setAlertType(Alert.AlertType.INFORMATION);
                    alert.setContentText("Tu username o password son incorrectas");
                    alert.showAndWait();
                    return;
            }



            //si el metodo de receptionist.receptionistLogin(), me devuelve un true, que abra el mainView donde se puede hacer todo
        });

//        btnRegister.setOnAction(e ->e -> new RegisterView(this.client, this.contentPane)});
        btnRegister.setOnAction(e  -> new RegisterView(this.client, this.contentPane));

        contentPane = new Pane();
        contentPane.setPrefSize(1000, 1000);
        contentPane.setStyle("-fx-background-color:#cad1ce;");


        Label bienvenida = new Label("Welcome Receptionist to the system hotel!");
        bienvenida.setStyle("-fx-font-family: 'Elephant'; -fx-font-size: 22px; -fx-text-fill: #2a2e2c;");
        bienvenida.setLayoutX(40);
        bienvenida.setLayoutY(40);
        contentPane.getChildren().add(bienvenida);

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
//textField.setOnKeyTyped(event -> {
//String character = event.getCharacter();
//    if (!character.matches("\\d")) {
//        event.consume(); // Ignora el carácter si no es un dígito
//    }
//            });