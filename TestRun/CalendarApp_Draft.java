
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

class Event {

    int id;
    String title;
    String description;
    LocalDate date;

    LocalDateTime startDateTime;
    LocalDateTime endDateTime;

    Event(int id, String title, LocalDate date, LocalDateTime startDateTime,
            LocalDateTime endDateTime) {

        this.id = id;
        this.title = title;
        this.date = date;
        this.description = "No description";
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;

    }
}

class Recurrence {

    int eventId;
    String interval;
    int count;
    LocalDate endDate;

    public Recurrence(int eventId, String interval, int count, LocalDate endDate) {
        this.eventId = eventId;
        this.interval = interval;
        this.count = count;
        this.endDate = endDate;
    }
}

public class CalendarApp_Draft {

    static List<Event> events = new ArrayList<>();

    static Map<Integer, Recurrence> recurrenceMap = new HashMap<>();

    static Scanner sc = new Scanner(System.in);
    static int idCounter = 1;

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
        long diff = 0;
        long unitAmount = 0;

        String unit = r.interval.substring(r.interval.length() - 1);
        int value = Integer.parseInt(r.interval.substring(0, r.interval.length() - 1));

        if (unit.equalsIgnoreCase("d")) {
            diff = ChronoUnit.DAYS.between(start, current);
            unitAmount = value;
        } else if (unit.equalsIgnoreCase("w")) {
            diff = ChronoUnit.WEEKS.between(start, current);

            if (start.getDayOfWeek() != current.getDayOfWeek()) {
                return false;
            }
            unitAmount = value;
        } else if (unit.equalsIgnoreCase("m")) {
            diff = ChronoUnit.MONTHS.between(start, current);

            if (start.getDayOfMonth() != current.getDayOfMonth()) {
                return false;
            }
            unitAmount = value;
        }

        if (diff > 0 && diff % unitAmount == 0) {
            long occurrencesSoFar = diff / unitAmount;

            if (r.count > 0 && occurrencesSoFar > r.count) {
                return false;
            }

            if (r.endDate != null && current.isAfter(r.endDate)) {
                return false;
            }

            return true;
        }

        return false;
    }

    static void createEvent() {
        try {
            System.out.print("Title: ");
            String title = sc.nextLine();

            System.out.print("Start DateTime (YYYY-MM-DD HH:MM): ");
            String startInput = sc.nextLine();

            System.out.print("End DateTime (YYYY-MM-DD HH:MM): ");
            String endInput = sc.nextLine();

            LocalDateTime startDT
                    = LocalDateTime.parse(startInput.replace(" ", "T"));
            LocalDateTime endDT
                    = LocalDateTime.parse(endInput.replace(" ", "T"));

            if (endDT.isBefore(startDT)) {
                System.out.println("❌ End time cannot be before start time.");
                return;
            }

            // IMPORTANT: keep LocalDate for recurrence logic
            LocalDate date = startDT.toLocalDate();

            int newId = idCounter++;

            events.add(new Event(
                    newId,
                    title,
                    date,
                    startDT,
                    endDT
            ));

            System.out.print("Is this a recurring event? (y/n): ");
            String isRecur = sc.nextLine();

            if (isRecur.equalsIgnoreCase("y")) {
                System.out.print("Interval (e.g., 1d, 2w, 1m): ");
                String interval = sc.nextLine();

                System.out.println("End condition type: 1. Count (Times)  2. Until Date");
                int type = sc.nextInt();
                sc.nextLine();

                int times = 0;
                LocalDate endDate = null;

                if (type == 1) {
                    System.out.print("How many times to repeat: ");
                    times = sc.nextInt();
                    sc.nextLine();
                } else {
                    System.out.print("Recur until (YYYY-MM-DD): ");
                    endDate = LocalDate.parse(sc.nextLine());
                }

                recurrenceMap.put(newId,
                        new Recurrence(newId, interval, times, endDate));
            }

            System.out.println("Event created with ID: " + newId);

        } catch (DateTimeParseException e) {
            System.out.println("Invalid date-time format.");
        } catch (Exception e) {
            System.out.println("Error creating event: " + e.getMessage());
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

<<<<<<< HEAD
<<<<<<< HEAD
            System.out.print(day + " (" + day.getDayOfWeek().toString().substring(0,3) + "): ");
            if (titles.isEmpty()) System.out.println("-");
            else System.out.println(String.join(", ", titles));
=======
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

>>>>>>> f521882 (fixing miss alignment (viewmonth))
=======
            System.out.print(day + " (" + day.getDayOfWeek().toString().substring(0, 3) + "): ");
            if (titles.isEmpty()) {
                System.out.println("-");
            } else {
                System.out.println(String.join(", ", titles));
            }
>>>>>>> 57dac70 (Adding new variable startDateTime and endDateTime and fix some issues with createEvent)
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

<<<<<<< HEAD
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
=======
        System.out.println("\n" + firstDay.getMonth() + " " + year);
        System.out.println("Sun Mon Tue Wed Thu Fri Sat ");

        for (int i = 0; i < startDay; i++) {
            System.out.print("    ");
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
>>>>>>> f521882 (fixing miss alignment (viewmonth))
            }
        }
    }

    static void updateEvent() {
        System.out.print("Enter the CSV filename to update from (e.g., update_events.csv): ");
        String fileName = sc.nextLine();
        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println("❌ File not found.");
            return;
        }

        int updatedCount = 0;
        int notFoundCount = 0;

        try (Scanner fileScanner = new Scanner(file)) {
            // Skip header if it exists
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine(); 
            }

            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                // split by comma, assuming no commas are inside the text fields
                String[] parts = line.split(",");

                if (parts.length >= 5) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String newTitle = parts[1].trim();
                        String newDesc = parts[2].trim();
                        LocalDateTime newStart = LocalDateTime.parse(parts[3].trim().replace(" ", "T"));
                        LocalDateTime newEnd = LocalDateTime.parse(parts[4].trim().replace(" ", "T"));

                        // Find the event in memory
                        Event target = null;
                        for (Event e : events) {
                            if (e.id == id) {
                                target = e;
                                break;
                            }
                        }

                        if (target != null) {
                            target.title = newTitle;
                            target.description = newDesc;
                            target.startDateTime = newStart;
                            target.endDateTime = newEnd;
                            target.date = newStart.toLocalDate(); // keep date in sync
                            updatedCount++;
                        } else {
                            notFoundCount++;
                        }
                    } catch (Exception parseError) {
                        System.out.println("⚠ Skipping malformed row: " + line);
                    }
                }
            }
            System.out.println("✅ Batch Update Complete.");
            System.out.println("Successfully updated: " + updatedCount);
            if (notFoundCount > 0) {
                System.out.println("IDs not found in system: " + notFoundCount);
            }

        } catch (FileNotFoundException e) {
            System.out.println("❌ Error reading file: " + e.getMessage());
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
}
