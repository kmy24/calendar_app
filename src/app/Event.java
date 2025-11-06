package app;

public class Event {
    private String name;
    private String date;

    public Event(String name, String date) {
        this.name = name;
        this.date = date;
    }

    @Override
    public String toString() {
        return name + " - " + date;
    }
}
