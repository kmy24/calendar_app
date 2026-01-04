
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.io.*;

class Event {

    int id;
    String title;
    String description;
    LocalDate date;
    LocalDateTime startDateTime;
    LocalDateTime endDateTime;

    Event(int id, String title, String description, LocalDateTime startDateTime,
            LocalDateTime endDateTime) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.date = startDateTime.toLocalDate();

    }

    Event(int id, String title, LocalDate date) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.description = "No description";

        // default times for restored events
        this.startDateTime = date.atStartOfDay();
        this.endDateTime = date.atTime(23, 59);
    }

}

class Recurrence {

    int eventId;
    String recurrentInterval; //e.g 1d,1w
    int recurrentTimes;       // 0 if unused
    String recurrentEndDate; // YYYY-MM-DD or "0"

    public Recurrence(int eventId, String interval, int times, String endDate) {
        this.eventId = eventId;
        this.recurrentInterval = interval;
        this.recurrentTimes = times;
        this.recurrentEndDate = endDate;
    }

    Recurrence(int id, String interval, int count, LocalDate endDate) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}

public class CalendarApp_Draft {

    static List<Event> events = new ArrayList<>();

    static Map<Integer, Recurrence> recurrenceMap = new HashMap<>();

    static Scanner sc = new Scanner(System.in);
    static int idCounter = loadNextEventId();

    public static void main(String[] args) {

        while (true) {
            System.out.println("\n=== CALENDAR APP ===");
            System.out.println("1. Create Event");
            System.out.println("2. View Events (Week/Month)");
            System.out.println("3. Update Event");
            System.out.println("4. Delete Event");
            System.out.println("5. Search Menu");
            System.out.println("6. Backup Events");
            System.out.println("7. Restore Events");
            System.out.println("8. Exit");
            System.out.print("Enter choice: ");

            try {
                int choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1 ->
                        createEvent();
                    case 2 ->
                        viewEvents();
                    case 3 ->
                        updateEvent();   // NEW
                    case 4 ->
                        deleteEvent();
                    case 5 ->
                        searchMenu();
                    case 6 ->
                        backupEvents();
                    case 7 ->
                        restoreEvents();
                    case 8 -> {
                        System.out.println("Exiting...");
                        System.exit(0);
                    }
                    default ->
                        System.out.println("Invalid choice.");
                }

            } catch (InputMismatchException e) {
                System.out.println("Invalid input.");
                sc.nextLine();
            }
        }
    }

    static int loadNextEventId() {
        File file = new File("event.csv");
        int maxId = 0;

        if (!file.exists()) {
            return 1;
        }

        try (Scanner fs = new Scanner(file)) {
            fs.nextLine(); // header
            while (fs.hasNextLine()) {
                String[] parts = fs.nextLine().split(",");
                maxId = Math.max(maxId, Integer.parseInt(parts[0]));
            }
        } catch (Exception ignored) {
        }

        return maxId + 1;
    }

    static void saveEventToCSV(Event e) {
        boolean exists = new File("event.csv").exists();

        try (PrintWriter pw = new PrintWriter(new FileOutputStream("event.csv", true))) {
            if (!exists) {
                pw.println("eventId,title,description,startDateTime,endDateTime");
            }

            pw.println(e.id + "," + e.title + "," + e.description + ","
                    + e.startDateTime + "," + e.endDateTime);
        } catch (IOException e1) {
            System.out.println("Error saving event.csv");
        }
    }

    static void saveRecurrenceToCSV(Recurrence r) {
        boolean exists = new File("recurrent.csv").exists();

        try (PrintWriter pw = new PrintWriter(new FileOutputStream("recurrent.csv", true))) {
            if (!exists) {
                pw.println("eventId,recurrentInterval,recurrentTimes,recurrentEndDate");
            }

            pw.println(r.eventId + "," + r.recurrentInterval + ","
                    + r.recurrentTimes + "," + r.recurrentEndDate);
        } catch (IOException e) {
            System.out.println("Error saving recurrent.csv");
        }
    }

    static void searchMenu() {
        System.out.println("\n--- Search Menu ---");
        System.out.println("1. Search by ID");
        System.out.println("2. Search by Title");
        System.out.println("3. Search by Date");
        System.out.print("Enter choice: ");

        int sChoice = sc.nextInt();
        sc.nextLine();

        switch (sChoice) {
            case 1 ->
                searchEventById();
            case 2 ->
                searchByTitle();
            case 3 ->
                searchByDate();
            default ->
                System.out.println("Invalid choice.");
        }
    }

    static List<String> getEventTitlesForDate(LocalDate targetDate) {
        List<String> titles = new ArrayList<>();

        for (Event e : events) {
            boolean isMatch = false;

            if (e.date.equals(targetDate)) {
                isMatch = true;
            } else if (recurrenceMap.containsKey(e.id)) {
                Recurrence r = recurrenceMap.get(e.id);

                if (targetDate.isAfter(e.date)) {
                    isMatch = checkRecurrence(e.date, targetDate, r);
                }
            }

            if (isMatch) {
                titles.add(e.title);
            }
        }
        return titles;
    }

    static void searchByTitle() {
        System.out.print("Enter keyword in title: ");
        String keyword = sc.nextLine().toLowerCase();

        boolean found = false;
        for (Event e : events) {
            if (e.title.toLowerCase().contains(keyword)) {
                System.out.println("Found: " + e.id + " | " + e.title + " | " + e.date + " | " + e.description);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No events found with keyword: " + keyword);
        }
    }

    static void searchByDate() {
        System.out.print("Enter date (YYYY-MM-DD): ");
        String dateInput = sc.nextLine();

        try {
            LocalDate targetDate = LocalDate.parse(dateInput);
            boolean found = false;
            for (Event e : events) {
                if (e.date.equals(targetDate)) {
                    System.out.println("Found: " + e.id + " | " + e.title + " | " + e.date + " | " + e.description);
                    found = true;
                }
            }
            if (!found) {
                System.out.println("No events found on " + targetDate);
            }
        } catch (Exception e) {
            System.out.println("Invalid date format.");
        }
    }

    // SEARCH by ID
    static void searchEventById() {
        System.out.print("Enter Event ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        boolean found = false;
        for (Event e : events) {
            if (e.id == id) {
                System.out.println("Found: " + e);
                found = true;
                break; // stop once found
            }
        }

        if (!found) {
            System.out.println("No event found with ID: " + id);
        }
    }

    static boolean checkRecurrence(LocalDate start, LocalDate current, Recurrence r) {

        String interval = r.recurrentInterval;
        int value = Integer.parseInt(interval.substring(0, interval.length() - 1));
        String unit = interval.substring(interval.length() - 1);

        long diff = switch (unit) {
            case "d" ->
                ChronoUnit.DAYS.between(start, current);
            case "w" ->
                ChronoUnit.WEEKS.between(start, current);
            case "m" ->
                ChronoUnit.MONTHS.between(start, current);
            default ->
                0;
        };

        if (diff <= 0 || diff % value != 0) {
            return false;
        }

        if (r.recurrentTimes > 0 && diff / value > r.recurrentTimes) {
            return false;
        }

        if (!r.recurrentEndDate.equals("0")) {
            LocalDate endDate = LocalDate.parse(r.recurrentEndDate);
            if (current.isAfter(endDate)) {
                return false;
            }
        }
        return true;
    }

    static void createEvent() {
        try {
            System.out.print("Title: ");
            String title = sc.nextLine();

            System.out.print("Description: ");
            String desc = sc.nextLine();

            System.out.print("Start (YYYY-MM-DD HH:MM): ");
            LocalDateTime start = LocalDateTime.parse(sc.nextLine().replace(" ", "T"));

            System.out.print("End (YYYY-MM-DD HH:MM): ");
            LocalDateTime end = LocalDateTime.parse(sc.nextLine().replace(" ", "T"));

            if (end.isBefore(start)) {
                System.out.println("End before start!");
                return;
            }

            int id = idCounter++;
            Event e = new Event(id, title, desc, start, end);
            events.add(e);
            saveEventToCSV(e);

            System.out.print("Recurring? (y/n): ");
            if (sc.nextLine().equalsIgnoreCase("y")) {
                System.out.print("Interval (1d/1w/1m): ");
                String interval = sc.nextLine();

                System.out.println("1. Times  2. Until Date");
                int type = sc.nextInt();
                sc.nextLine();

                int times = 0;
                String endDate = "0";   // STRING on purpose

                if (type == 1) {
                    System.out.print("Times: ");
                    times = sc.nextInt();
                    sc.nextLine();
                } else {
                    System.out.print("Until (YYYY-MM-DD): ");
                    endDate = sc.nextLine(); 
                }

                Recurrence r = new Recurrence(id, interval, times, endDate);
                recurrenceMap.put(id, r);
                saveRecurrenceToCSV(r);
            }

            System.out.println("Event created with ID: " + id);

        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }

    static void viewEvents() {
        System.out.println("1. Weekly View  2. Monthly View");
        int choice = sc.nextInt();
        sc.nextLine();

        if (choice == 1) {
            viewWeek();
        } else {
            viewMonth();
        }
    }

    static void viewWeek() {
        System.out.print("Start date (YYYY-MM-DD): ");
        LocalDate start = LocalDate.parse(sc.nextLine());
        LocalDate weekStart = start.minusDays(start.getDayOfWeek().getValue() % 7);

        System.out.println("\n--- Week of " + weekStart + " ---");
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);

            List<String> titles = getEventTitlesForDate(day);

            System.out.print(day + " (" + day.getDayOfWeek().toString().substring(0, 3) + "): ");
            if (titles.isEmpty()) {
                System.out.println("-");
            } else {
                System.out.println(String.join(", ", titles));
            }

        }
    }

    static void viewMonth() {
        System.out.print("Enter month and year (MM YYYY): ");
        int month = sc.nextInt();
        int year = sc.nextInt();
        sc.nextLine();

        LocalDate firstDay = LocalDate.of(year, month, 1);
        int daysInMonth = firstDay.lengthOfMonth();
        int startDay = firstDay.getDayOfWeek().getValue() % 7;

        System.out.println("\nEvents in " + firstDay.getMonth() + " " + year + ":");
        System.out.println("SUN MON TUE WED THU FRI SAT");

        for (int i = 0; i < startDay; i++) {
            System.out.print("   ");
        }
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate current = LocalDate.of(year, month, day);
            List<String> titles = getEventTitlesForDate(current);

            if (!titles.isEmpty()) {
                System.out.printf("%3d*", day);
            } else {
                System.out.printf("%3d ", day);
            }

            if ((day + startDay) % 7 == 0) {
                System.out.println();
            }
        }
        System.out.println();
        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate current = LocalDate.of(year, month, i);
            List<String> titles = getEventTitlesForDate(current);

            if (!titles.isEmpty()) {
                System.out.println(String.format("%02d", i) + ": " + String.join(", ", titles));
            }
        }
    }

    static void updateEvent() {
        System.out.println("\n--- Update Event ---");
        System.out.print("Enter Event ID to update: ");

        try {
            int id = sc.nextInt();
            sc.nextLine(); // consume newline

            Event target = null;

            // Find event
            for (Event e : events) {
                if (e.id == id) {
                    target = e;
                    break;
                }
            }

            if (target == null) {
                System.out.println("❌ Event with ID " + id + " not found.");
                return;
            }

            // Update title
            System.out.print(
                    "New Title (press Enter to keep current: \"" + target.title + "\"): "
            );
            String newTitle = sc.nextLine();
            if (!newTitle.trim().isEmpty()) {
                target.title = newTitle.trim();
            }

            // Update description
            System.out.print(
                    "New Description (press Enter to keep current): "
            );
            String newDesc = sc.nextLine();
            if (!newDesc.trim().isEmpty()) {
                target.description = newDesc.trim();
            }

            // Update date (with validation)
            while (true) {
                System.out.print(
                        "New Date (YYYY-MM-DD, press Enter to keep current: "
                        + target.date + "): "
                );
                String dateInput = sc.nextLine().trim();

                if (dateInput.isEmpty()) {
                    break;
                }

                try {
                    target.date = LocalDate.parse(dateInput);
                    break;
                } catch (DateTimeParseException e) {
                    System.out.println("⚠ Invalid date format. Try again.");
                }
            }
            // Update startDateTime
            System.out.print(
                    "New Start DateTime (YYYY-MM-DD HH:MM, press Enter to keep current: "
                    + target.startDateTime + "): "
            );
            String newStart = sc.nextLine().trim();

            if (!newStart.isEmpty()) {
                target.startDateTime
                        = LocalDateTime.parse(newStart.replace(" ", "T"));

                // keep LocalDate in sync for recurrence logic
                target.date = target.startDateTime.toLocalDate();
            }

            // Update endDateTime
            System.out.print(
                    "New End DateTime (YYYY-MM-DD HH:MM, press Enter to keep current: "
                    + target.endDateTime + "): "
            );
            String newEnd = sc.nextLine().trim();

            if (!newEnd.isEmpty()) {
                target.endDateTime
                        = LocalDateTime.parse(newEnd.replace(" ", "T"));
            }

            // Update recurrence if exists
            if (recurrenceMap.containsKey(id)) {
                System.out.print("Update recurrence? (y/n): ");
                String choice = sc.nextLine();

                if (choice.equalsIgnoreCase("y")) {
                    System.out.print("New interval (e.g., 1d, 2w, 1m): ");
                    String interval = sc.nextLine();

                    System.out.println("End condition: 1. Count  2. Until Date");
                    int type = sc.nextInt();
                    sc.nextLine();

                    int count = 0;
                    LocalDate endDate = null;

                    if (type == 1) {
                        System.out.print("Repeat how many times: ");
                        count = sc.nextInt();
                        sc.nextLine();
                    } else {
                        System.out.print("Recur until (YYYY-MM-DD): ");
                        endDate = LocalDate.parse(sc.nextLine());
                    }

                    recurrenceMap.put(id, new Recurrence(id, interval, count, endDate));
                }
            }

            System.out.println("✅ Event updated successfully.");

        } catch (InputMismatchException e) {
            System.out.println("❌ Invalid ID. Please enter a number.");
            sc.nextLine();
        }
    }

    static void deleteEvent() {
        System.out.print("Enter Event ID to delete: ");
        int id = sc.nextInt();
        sc.nextLine();

        Event deletedEvent = null;
        for (Event e : events) {
            if (e.id == id) {
                deletedEvent = e;
                break;
            }
        }

        if (deletedEvent != null) {
            System.out.println("You are about to delete: " + deletedEvent.title + " on " + deletedEvent.date);
            System.out.print("Are you sure? (y/n): ");
            String confirm = sc.nextLine();
            if (!confirm.equalsIgnoreCase("y")) {
                System.out.println("Deletion cancelled.");
                return;
            }
        }

        boolean removed = events.removeIf(e -> e.id == id);

        if (recurrenceMap.containsKey(id)) {
            recurrenceMap.remove(id);
            System.out.println("Recurrence rule removed.");
        }

        if (removed) {
            System.out.println("Event deleted.");
        } else {
            System.out.println("ID not found.");
        }
    }

    static void backupEvents() {
        try (PrintWriter pw = new PrintWriter("events_backup.txt")) {
            for (Event e : events) {
                pw.println(e.id + "," + e.title + "," + e.date);
            }
            System.out.println("Backup successful!");
        } catch (IOException e) {
            System.out.println("Error while backing up: " + e.getMessage());
        }
    }

    static void restoreEvents() {
        try (Scanner fileScanner = new Scanner(new File("events_backup.txt"))) {
            events.clear(); // clear current memory
            idCounter = 1;

            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split(",", 3);
                if (parts.length == 3) {
                    int id = Integer.parseInt(parts[0]);
                    String title = parts[1];
                    LocalDate date = LocalDate.parse(parts[2]);

                    events.add(new Event(id, title, date));
                    if (id >= idCounter) {
                        idCounter = id + 1;
                    }
                }
            }
            System.out.println("Restore successful!");
        } catch (FileNotFoundException e) {
            System.out.println("No backup file found.");
        } catch (Exception e) {
            System.out.println("Error while restoring: " + e.getMessage());
        }
    }
}
