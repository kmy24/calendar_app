package app;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Event {
    public int id;
    public String title;
    public String description;
    public LocalDateTime startDateTime;
    public LocalDateTime endDateTime;
    public LocalDate date;
    public String location;
    public String priority;

    // Main Constructor (Used for creating full events)
    public Event(int id, String title, String description, LocalDateTime start, LocalDateTime end, String location, String priority) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDateTime = start;
        this.endDateTime = end;
        this.date = start.toLocalDate();
        this.location = location;
        this.priority = priority;
    }

    // Simple Constructor (Optional - used for quick tasks)
    public Event(int id, String title, LocalDate date) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.description = "No description";
        this.startDateTime = date.atStartOfDay();
        this.endDateTime = date.atTime(23, 59);
        this.location = "N/A";
        this.priority = "Normal";
    }

    @Override
    public String toString() {
        // Updated this so you can actually see the Location and Priority in the list!
        return String.format("ID: %d | [%s] %s | Date: %s | Loc: %s | Desc: %s", 
                              id, priority.toUpperCase(), title, date, location, description);
    }
}