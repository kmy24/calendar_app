package app;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class EventManager {

    private StorageManager storage = new StorageManager();

    public void addEvent(Event event) {
        storage.saveEvent(event.toString());
    }

    public List<String> loadEvents() {
        return storage.loadEvents();
    }

    public void deleteEvent(String eventText) {
        storage.removeEvent(eventText);
    }

    public void updateEvent(String oldEvent, String newEvent) {
        storage.updateEvent(oldEvent, newEvent);
    }

    public List<String> getTodayEvents() {
        String today = LocalDate.now().toString();
        return loadEvents().stream()
                .filter(e -> e.contains(today))
                .collect(Collectors.toList());
    }
}
