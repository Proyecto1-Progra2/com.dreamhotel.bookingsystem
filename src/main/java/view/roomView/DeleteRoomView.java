package view.roomView;

import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import sockets.Client;

public class DeleteRoomView extends BorderPane implements Runnable {

    private Client client;
    private Pane contentPane;
    
    private volatile boolean isRunning = true;

    private TextField tRoomNumber;

    public DeleteRoomView(Client client, Pane contentPane) {
        this.setStyle("-fx-border-color: black; -fx-background-color: white;");
        this.setPrefSize(300, 200);
        this.setLayoutX(100);
        this.setLayoutY(100);

        this.contentPane = contentPane;

        this.initComponents();
        this.client = client;

        Thread thread = new Thread(this);
        thread.start();
    }

    private void initComponents() {
    }

    @Override
    public void run() {

    }
}
