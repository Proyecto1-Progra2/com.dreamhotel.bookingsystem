package view.bookingView;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import sockets.Client;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

public class CalendarView extends BorderPane {

    private Client client;
    private Pane contentPane;

    private YearMonth currentYearMonth;
    private GridPane calendarGrid;
    private Label monthLabel;

    private LocalDate startDate;
    private LocalDate endDate;

    private DateRangeCallback callback;

    public CalendarView(Client client, Pane contentPane, DateRangeCallback callback) {
        this.setStyle("-fx-border-color: black; -fx-background-color: white;");
        this.setLayoutX(100);
        this.setLayoutY(100);
        this.contentPane = contentPane;
        this.callback = callback;
        this.client = client;
        this.startDate = null;
        this.endDate = null;

        this.initComponents();
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

        // Calendario
        currentYearMonth = YearMonth.now();

        HBox header = new HBox(10);
        Button prevButton = new Button("<");
        Button nextButton = new Button(">");
        monthLabel = new Label();
        monthLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        header.getChildren().addAll(prevButton, monthLabel, nextButton);
        header.setAlignment(Pos.CENTER);

        prevButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });

        nextButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        calendarGrid = new GridPane();
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);
        calendarGrid.setAlignment(Pos.CENTER);

        updateCalendar();

        // Contenido de la ventana
        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 10;");
        // Botón de confirmación
        Button confirmButton = new Button("Confirm Dates");
        confirmButton.setOnAction(e -> {
            if (startDate != null && endDate != null) {
                if (callback != null) {
                    callback.onDateRangeSelected(startDate, endDate);
                }
                contentPane.getChildren().remove(this); // Cierra la vista al confirmar
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Selection incomplete");
                alert.setHeaderText(null);
                alert.setContentText("Please, select a date range valid.");
                alert.showAndWait();
            }
        });

        contenido.getChildren().addAll(header, calendarGrid, confirmButton);

        this.setTop(titleBar);
        this.setCenter(contenido);

        contentPane.getChildren().add(this);
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();

        String monthName = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        monthLabel.setText(monthName + " " + currentYearMonth.getYear());

        String[] days = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        for (int i = 0; i < days.length; i++) {
            Label label = new Label(days[i]);
            label.setStyle("-fx-font-weight: bold;");
            calendarGrid.add(label, i, 0);
        }

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // lunes=1
        int lengthOfMonth = currentYearMonth.lengthOfMonth();

        int dayCounter = 1;
        for (int row = 1; row <= 6; row++) {
            for (int col = 0; col < 7; col++) {
                if ((row == 1 && col + 1 < dayOfWeek) || dayCounter > lengthOfMonth) {
                    continue;
                }

                LocalDate date = currentYearMonth.atDay(dayCounter);
                Label dayLabel = new Label(String.valueOf(dayCounter));
                dayLabel.setMinSize(40, 40);
                dayLabel.setAlignment(Pos.CENTER);
                dayLabel.setStyle("-fx-border-color: lightgray;");

                if (isDateInRange(date)) {
                    dayLabel.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, null, null)));
                }

                LocalDate finalDate = date;
                dayLabel.setOnMouseClicked(e -> {
                    handleDateClick(finalDate);
                    System.out.println(finalDate);
                });

                calendarGrid.add(dayLabel, col, row);
                dayCounter++;
            }
        }
    }

    private void handleDateClick(LocalDate clickedDate) {
        if (startDate == null || (startDate != null && endDate != null)) {
            startDate = clickedDate;
            endDate = null;
        } else if (clickedDate.isBefore(startDate)) {
            // Si el usuario hace clic en una fecha anterior, se reinicia la selección
            startDate = clickedDate;
            endDate = null;
        } else {
            endDate = clickedDate;
        }
        updateCalendar();
    }

    private boolean isDateInRange(LocalDate date) {
        if (startDate == null) return false;
        if (endDate == null) return date.equals(startDate);
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    private void clearSelection() {
        startDate = null;
        endDate = null;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
}
