package app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.util.List;

public class MainController {

    @FXML private TextField eventInput;
    @FXML private DatePicker dateInput;
    @FXML private ListView<String> eventList;

    private EventManager eventManager = new EventManager();

    @FXML
    public void initialize() {
        eventList.getItems().addAll(eventManager.loadEvents());
    }

    @FXML
    public void addEvent() {
        String name = eventInput.getText();
        LocalDate date = dateInput.getValue();
        if (name.isEmpty() || date == null) return;

        Event event = new Event(name, date.toString());
        eventManager.addEvent(event);

        refreshList();
        clearInputs();
    }

    @FXML
    public void deleteEvent() {
        String selected = eventList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        eventManager.deleteEvent(selected);
        refreshList();
    }

    @FXML
    public void updateEvent() {
        String selected = eventList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String newName = eventInput.getText();
        LocalDate newDate = dateInput.getValue();
        if (newName.isEmpty() || newDate == null) return;

        eventManager.updateEvent(selected, newName + " - " + newDate);
        refreshList();
        clearInputs();
    }

    @FXML
    public void checkReminders() {
        List<String> todayEvents = eventManager.getTodayEvents();
        if (todayEvents.isEmpty()) {
            showAlert("No reminders for today.");
        } else {
            showAlert("Today’s Events:\n\n" + String.join("\n", todayEvents));
        }
    }

    private void refreshList() {
        eventList.getItems().setAll(eventManager.loadEvents());
    }

    private void clearInputs() {
        eventInput.clear();
        dateInput.setValue(null);
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reminder");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
