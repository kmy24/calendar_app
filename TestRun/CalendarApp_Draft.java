import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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
    
    // Helper to format for CSV
    public String toCSV() {
        return id + "," + title + "," + description + "," + startDateTime + "," + endDateTime;
    }
    
    @Override
    public String toString() {
        return "ID: " + id + " | " + title + " | " + startDateTime + " - " + endDateTime.toLocalTime();
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
    
    public String toCSV() {
        return eventId + "," + recurrentInterval + "," + recurrentTimes + "," + recurrentEndDate;
    }
}

public class CalendarApp_Draft {

    static List<Event> events = new ArrayList<>();
    static Map<Integer, Recurrence> recurrenceMap = new HashMap<>();
    static Scanner sc = new Scanner(System.in);
    
    // File constants
    static final String EVENT_FILE = "event.csv";
    static final String RECUR_FILE = "recurrent.csv";
    static final String EVENT_BACKUP = "event_backup.csv";
    static final String RECUR_BACKUP = "recurrent_backup.csv";

    static int idCounter = 1;

    public static void main(String[] args) {
        // 1. LOAD DATA ON STARTUP
        loadEventsFromFile();
        loadRecurrencesFromFile();
        calculateNextId();

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
                sc.nextLine(); // consume newline

                switch (choice) {
                    case 1 -> createEvent();
                    case 2 -> viewEvents();
                    case 3 -> updateEvent();
                    case 4 -> deleteEvent();
                    case 5 -> searchMenu();
                    case 6 -> backupEvents();
                    case 7 -> restoreEvents();
                    case 8 -> {
                        System.out.println("Exiting...");
                        System.exit(0);
                    }
                    default -> System.out.println("Invalid choice.");
                }

            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine();
            }
        }
    }

    // --- FILE HANDLING METHODS ---

    static void calculateNextId() {
        int max = 0;
        for(Event e : events) {
            if(e.id > max) max = e.id;
        }
        idCounter = max + 1;
    }

    static void loadEventsFromFile() {
        events.clear();
        File file = new File(EVENT_FILE);
        if (!file.exists()) return;

        try (Scanner fs = new Scanner(file)) {
            if (fs.hasNextLine()) fs.nextLine(); // Skip header
            while (fs.hasNextLine()) {
                String line = fs.nextLine();
                if(line.trim().isEmpty()) continue;
                
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    try {
                        int id = Integer.parseInt(parts[0]);
                        String title = parts[1];
                        String desc = parts[2];
                        LocalDateTime start = LocalDateTime.parse(parts[3]);
                        LocalDateTime end = LocalDateTime.parse(parts[4]);
                        events.add(new Event(id, title, desc, start, end));
                    } catch (Exception e) {
                        // Skip corrupted lines
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading events: " + e.getMessage());
        }
    }

    static void loadRecurrencesFromFile() {
        recurrenceMap.clear();
        File file = new File(RECUR_FILE);
        if (!file.exists()) return;

        try (Scanner fs = new Scanner(file)) {
            if (fs.hasNextLine()) fs.nextLine(); // Skip header
            while (fs.hasNextLine()) {
                String line = fs.nextLine();
                if(line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    try {
                        int eventId = Integer.parseInt(parts[0]);
                        String interval = parts[1];
                        int times = Integer.parseInt(parts[2]);
                        String endDate = parts[3];
                        recurrenceMap.put(eventId, new Recurrence(eventId, interval, times, endDate));
                    } catch (Exception e) {
                        // Skip corrupted lines
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading recurrences: " + e.getMessage());
        }
    }

    static void saveEventsToFile() {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(EVENT_FILE))) {
            pw.println("eventId,title,description,startDateTime,endDateTime");
            for (Event e : events) {
                pw.println(e.toCSV());
            }
        } catch (IOException e) {
            System.out.println("Error saving events: " + e.getMessage());
        }
    }

    static void saveRecurrencesToFile() {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(RECUR_FILE))) {
            pw.println("eventId,recurrentInterval,recurrentTimes,recurrentEndDate");
            for (Recurrence r : recurrenceMap.values()) {
                pw.println(r.toCSV());
            }
        } catch (IOException e) {
            System.out.println("Error saving recurrences: " + e.getMessage());
        }
    }

    // --- LOGIC FIX HERE ---

    static boolean checkRecurrence(LocalDate start, LocalDate current, Recurrence r) {
        if (!current.isAfter(start)) {
            return false;
        }

        String intervalStr = r.recurrentInterval;
        // Parse "1w" -> value=1, unit="w"
        int value = Integer.parseInt(intervalStr.substring(0, intervalStr.length() - 1));
        String unit = intervalStr.substring(intervalStr.length() - 1);

        // 1. Check End Date / Count conditions first
        if (!r.recurrentEndDate.equals("0")) {
            LocalDate endDate = LocalDate.parse(r.recurrentEndDate);
            if (current.isAfter(endDate)) return false;
        }

        long unitsDiff = 0;

        switch (unit) {
            case "d" -> {
                long daysDiff = ChronoUnit.DAYS.between(start, current);
                if (daysDiff % value != 0) return false;
                unitsDiff = daysDiff / value;
            }
            case "w" -> {
                long daysDiff = ChronoUnit.DAYS.between(start, current);
                // A week is 7 days. We check if daysDiff is a multiple of (value * 7).
                // Example: 1w -> must be multiple of 7. 2w -> multiple of 14.
                if (daysDiff % (value * 7) != 0) return false;
                
                unitsDiff = daysDiff / (value * 7);
            }
            case "m" -> {
                // For months, we must ensure the Day of Month matches exactly.
                if (start.getDayOfMonth() != current.getDayOfMonth()) return false;
                
                long monthsDiff = ChronoUnit.MONTHS.between(start, current);
                if (monthsDiff % value != 0) return false;
                
                unitsDiff = monthsDiff / value;
            }
            default -> { return false; }
        }

        // Check occurrence count limit
        if (r.recurrentTimes > 0 && unitsDiff > r.recurrentTimes) {
            return false;
        }

        return true;
    }

    // --- APPLICATION FEATURES ---

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
            
            // Handle Recurrence
            System.out.print("Recurring? (y/n): ");
            if (sc.nextLine().equalsIgnoreCase("y")) {
                System.out.print("Interval (1d/1w/1m): ");
                String interval = sc.nextLine();

                System.out.println("1. Times  2. Until Date");
                int type = sc.nextInt();
                sc.nextLine();

                int times = 0;
                String endDate = "0";

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
            }

            saveEventsToFile();
            saveRecurrencesToFile();

            System.out.println("Event created with ID: " + id);

        } catch (Exception e) {
            System.out.println("Invalid input or date format. (Use YYYY-MM-DD HH:MM)");
        }
    }

    static void deleteEvent() {
        System.out.print("Enter Event ID to delete: ");
        int id = sc.nextInt();
        sc.nextLine();

        boolean removed = events.removeIf(e -> e.id == id);

        if (removed) {
            recurrenceMap.remove(id);
            saveEventsToFile();
            saveRecurrencesToFile();
            System.out.println("Event deleted.");
        } else {
            System.out.println("ID not found.");
        }
    }

    static void updateEvent() {
        System.out.println("\n--- Update Event ---");
        System.out.print("Enter Event ID to update: ");

        try {
            int id = sc.nextInt();
            sc.nextLine(); 

            Event target = null;
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

            System.out.print("New Title (Enter to keep '" + target.title + "'): ");
            String newTitle = sc.nextLine();
            if (!newTitle.trim().isEmpty()) target.title = newTitle.trim();

            System.out.print("New Description (Enter to keep): ");
            String newDesc = sc.nextLine();
            if (!newDesc.trim().isEmpty()) target.description = newDesc.trim();

            System.out.print("New Start (YYYY-MM-DD HH:MM, Enter to keep " + target.startDateTime + "): ");
            String newStart = sc.nextLine().trim();
            if (!newStart.isEmpty()) {
                try {
                    target.startDateTime = LocalDateTime.parse(newStart.replace(" ", "T"));
                    target.date = target.startDateTime.toLocalDate();
                } catch (Exception e) { System.out.println("Invalid date ignored."); }
            }

            System.out.print("New End (YYYY-MM-DD HH:MM, Enter to keep " + target.endDateTime + "): ");
            String newEnd = sc.nextLine().trim();
            if (!newEnd.isEmpty()) {
                try {
                    target.endDateTime = LocalDateTime.parse(newEnd.replace(" ", "T"));
                } catch (Exception e) { System.out.println("Invalid date ignored."); }
            }
            
            saveEventsToFile();
            System.out.println("✅ Event updated.");

        } catch (InputMismatchException e) {
            System.out.println("❌ Invalid ID.");
            sc.nextLine();
        }
    }

    // --- SEARCH ---
    static void searchMenu() {
        System.out.println("\n--- Search Menu ---");
        System.out.println("1. Search by ID");
        System.out.println("2. Search by Title");
        System.out.println("3. Search by Date");
        System.out.print("Enter choice: ");

        int sChoice = sc.nextInt();
        sc.nextLine();

        switch (sChoice) {
            case 1 -> searchEventById();
            case 2 -> searchByTitle();
            case 3 -> searchByDate();
            default -> System.out.println("Invalid choice.");
        }
    }

    static void searchEventById() {
        System.out.print("Enter Event ID: ");
        int id = sc.nextInt();
        sc.nextLine();
        boolean found = false;
        for (Event e : events) {
            if (e.id == id) {
                System.out.println("Found: " + e);
                found = true;
                break;
            }
        }
        if (!found) System.out.println("Not found.");
    }

    static void searchByTitle() {
        System.out.print("Enter keyword: ");
        String keyword = sc.nextLine().toLowerCase();
        boolean found = false;
        for (Event e : events) {
            if (e.title.toLowerCase().contains(keyword)) {
                System.out.println(e);
                found = true;
            }
        }
        if (!found) System.out.println("No matches.");
    }

    static void searchByDate() {
        System.out.print("Enter date (YYYY-MM-DD): ");
        try {
            LocalDate target = LocalDate.parse(sc.nextLine());
            List<String> titles = getEventTitlesForDate(target);
            if(titles.isEmpty()) System.out.println("No events on this date.");
            else titles.forEach(System.out::println);
        } catch (Exception e) {
            System.out.println("Invalid date.");
        }
    }

    // --- VIEW LOGIC ---
    static void viewEvents() {
        System.out.println("1. Weekly View  2. Monthly View");
        int choice = sc.nextInt();
        sc.nextLine();

        if (choice == 1) viewWeek();
        else viewMonth();
    }

    static void viewWeek() {
        System.out.print("Start date (YYYY-MM-DD): ");
        try {
            LocalDate start = LocalDate.parse(sc.nextLine());
            LocalDate weekStart = start.minusDays(start.getDayOfWeek().getValue() % 7);

            System.out.println("\n--- Week of " + weekStart + " ---");
            for (int i = 0; i < 7; i++) {
                LocalDate day = weekStart.plusDays(i);
                List<String> titles = getEventTitlesForDate(day);
                System.out.print(day + " (" + day.getDayOfWeek().toString().substring(0, 3) + "): ");
                if (titles.isEmpty()) System.out.println("-");
                else System.out.println(String.join(", ", titles));
            }
        } catch(Exception e) { System.out.println("Invalid date."); }
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

        for (int i = 0; i < startDay; i++) System.out.print("   ");
        
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate current = LocalDate.of(year, month, day);
            List<String> titles = getEventTitlesForDate(current);
            if (!titles.isEmpty()) System.out.printf("%3d*", day);
            else System.out.printf("%3d ", day);

            if ((day + startDay) % 7 == 0) System.out.println();
        }
        System.out.println("\n(List below)");
        for (int i = 1; i <= daysInMonth; i++) {
            List<String> titles = getEventTitlesForDate(LocalDate.of(year, month, i));
            if (!titles.isEmpty()) System.out.println(String.format("%02d", i) + ": " + String.join(", ", titles));
        }
    }

    static List<String> getEventTitlesForDate(LocalDate targetDate) {
        List<String> titles = new ArrayList<>();
        for (Event e : events) {
            boolean isMatch = false;
            // Direct Match
            if (e.date.equals(targetDate)) {
                isMatch = true;
            } 
            // Recurrence Match
            else if (recurrenceMap.containsKey(e.id)) {
                Recurrence r = recurrenceMap.get(e.id);
                isMatch = checkRecurrence(e.date, targetDate, r);
            }
            if (isMatch) titles.add(e.title);
        }
        return titles;
    }

    // --- BACKUP AND RESTORE ---
    
    static void backupEvents() {
        try {
            File srcEvent = new File(EVENT_FILE);
            File srcRecur = new File(RECUR_FILE);
            
            if(srcEvent.exists()) {
                Files.copy(srcEvent.toPath(), new File(EVENT_BACKUP).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            if(srcRecur.exists()) {
                Files.copy(srcRecur.toPath(), new File(RECUR_BACKUP).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("✅ Backup successful.");
        } catch (IOException e) {
            System.out.println("❌ Backup failed: " + e.getMessage());
        }
    }

    static void restoreEvents() {
        System.out.print("⚠️ Overwrite current data? (y/n): ");
        if(!sc.nextLine().equalsIgnoreCase("y")) return;

        try {
            File backupEvent = new File(EVENT_BACKUP);
            File backupRecur = new File(RECUR_BACKUP);

            if(backupEvent.exists()) {
                Files.copy(backupEvent.toPath(), new File(EVENT_FILE).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            if(backupRecur.exists()) {
                Files.copy(backupRecur.toPath(), new File(RECUR_FILE).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            loadEventsFromFile();
            loadRecurrencesFromFile();
            calculateNextId();
            
            System.out.println("✅ Restore successful.");
        } catch (IOException e) {
            System.out.println("❌ Restore failed: " + e.getMessage());
        }
    }
}