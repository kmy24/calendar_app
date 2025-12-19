import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

class Event {
    int id;
    String title;
    LocalDate date;

    Event(int id, String title, LocalDate date) {
        this.id = id;
        this.title = title;
        this.date = date;
    }
}

public class CalendarApp_Draft {
    static List<Event> events = new ArrayList<>();
    static Scanner sc = new Scanner(System.in);
    static int idCounter = 1;

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n1. Create  2. View  3. Update  4. Delete  5. Exit");
            System.out.print("Enter your choice: ");
            
            try {
                int choice = sc.nextInt();
                sc.nextLine(); // Consume newline

                switch (choice) {
                    case 1 -> createEvent();
                    case 2 -> viewEvents();
                    case 3 -> updateEvent();
                    case 4 -> deleteEvent();
                    case 5 -> {
                        System.out.println("Exiting program. Goodbye!");
                        sc.close();
                        System.exit(0);
                    }
                    default -> System.out.println("Invalid choice. Please enter 1-5.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number 1-5.");
                sc.nextLine(); // Clear invalid input
            }
        }
    }

    // CREATE
    static void createEvent() {
        try {
            System.out.print("Title: ");
            String title = sc.nextLine();
            System.out.print("Date (YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(sc.nextLine());

            events.add(new Event(idCounter++, title, date));
            System.out.println("Event added successfully (ID: " + (idCounter - 1) + ")");
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD format.");
        }
    }

    // VIEW
    static void viewEvents() {
        if (events.isEmpty()) {
        System.out.println("No events found.");
        return;
    }

    System.out.println("View Options:");
    System.out.println("1. Weekly View");
    System.out.println("2. Monthly Calendar View");
    System.out.print("Enter choice: ");

    try {
        int choice = sc.nextInt();
        sc.nextLine(); // consume newline

        switch (choice) {
            case 1 -> viewWeek();
            case 2 -> viewMonth();
            default -> System.out.println("Invalid choice.");
        }
    } catch (InputMismatchException e) {
        System.out.println("Invalid input. Please enter a number.");
        sc.nextLine(); // clear invalid input
    }
    }

    // UPDATE
    static void updateEvent() {
        System.out.print("Enter Event ID to update: ");
        try {
            int id = sc.nextInt();
            sc.nextLine(); // Consume newline

            for (Event e : events) {
                if (e.id == id) {
                    System.out.print("New Title (press Enter to keep current: '" + e.title + "'): ");
                    String newTitle = sc.nextLine();
                    if (!newTitle.trim().isEmpty()) {
                        e.title = newTitle;
                    }

                    System.out.print("New Date (YYYY-MM-DD, press Enter to keep current: '" + e.date + "'): ");
                    String dateInput = sc.nextLine();
                    if (!dateInput.trim().isEmpty()) {
                        try {
                            e.date = LocalDate.parse(dateInput);
                        } catch (DateTimeParseException ex) {
                            System.out.println("Invalid date format. Date not updated.");
                        }
                    }
                    
                    System.out.println("Event updated successfully.");
                    return;
                }
            }
            System.out.println("Event with ID " + id + " not found.");
        } catch (InputMismatchException e) {
            System.out.println("Invalid ID format. Please enter a number.");
            sc.nextLine(); // Clear invalid input
        }
    }

    // DELETE
    static void deleteEvent() {
        System.out.print("Enter Event ID to delete: ");
        try {
            int id = sc.nextInt();
            sc.nextLine(); // Consume newline
            
            boolean removed = events.removeIf(e -> e.id == id);
            if (removed) {
                System.out.println("Event deleted successfully.");
            } else {
                System.out.println("Event with ID " + id + " not found.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid ID format. Please enter a number.");
            sc.nextLine(); // Clear invalid input
        }
    }
    
    static void viewWeek() {
    System.out.print("Enter a date in YYYY-MM-DD format to start the week: ");
    try {
        LocalDate start = LocalDate.parse(sc.nextLine());

        // Adjust to Sunday (start of the week)
        LocalDate weekStart = start.minusDays(start.getDayOfWeek().getValue() % 7);

        System.out.println("\n=== Week of " + weekStart + " ===");

        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            String dayName = day.getDayOfWeek().toString().substring(0, 3); // SUN, MON, etc.

            List<Event> eventsToday = new ArrayList<>();
            for (Event e : events) {
                if (e.date.equals(day)) {
                    eventsToday.add(e);
                }
            }

            System.out.print(dayName + " " + String.format("%02d", day.getDayOfMonth()) + ": ");
            if (eventsToday.isEmpty()) {
                System.out.println("No events");
            } else {
                for (int j = 0; j < eventsToday.size(); j++) {
                    System.out.print(eventsToday.get(j).title);
                    if (j < eventsToday.size() - 1) System.out.print(", ");
                }
                System.out.println();
            }
        }

    } catch (DateTimeParseException e) {
        System.out.println("Invalid date format. Use YYYY-MM-DD.");
    }
}
    
    static void viewMonth() {
    try {
        System.out.print("Enter month and year (MM YYYY): ");
        int month = sc.nextInt();
        int year = sc.nextInt();
        sc.nextLine(); // consume newline

        LocalDate firstDay = LocalDate.of(year, month, 1);
        int lengthOfMonth = firstDay.lengthOfMonth();
        int startDay = firstDay.getDayOfWeek().getValue() % 7; // Sunday = 0

        System.out.println("\n" + firstDay.getMonth() + " " + year);
        System.out.println("Su Mo Tu We Th Fr Sa");

        for (int i = 0; i < startDay; i++) {
            System.out.print("   ");
        }

        for (int day = 1; day <= lengthOfMonth; day++) {
            LocalDate current = LocalDate.of(year, month, day);
            boolean hasEvent = events.stream().anyMatch(e -> e.date.equals(current));
            System.out.printf("%2d%s ", day, hasEvent ? "*" : " ");

            if ((day + startDay) % 7 == 0) System.out.println();
        }
        System.out.println();

        // List events below calendar
        for (Event e : events) {
            if (e.date.getMonthValue() == month && e.date.getYear() == year) {
                System.out.println("* " + e.date.getDayOfMonth() + ": " + e.title);
            }
        }

    } catch (InputMismatchException e) {
        System.out.println("Invalid input. Use numbers like 10 2025.");
        sc.nextLine();
    }
}
}