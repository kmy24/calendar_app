package app;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.io.*;


public class EventService {
    private static List<Event> events = new ArrayList<>();
    private Map<Integer, Recurrence> recurrenceMap = new HashMap<>();
    static private Map<Integer, AdditionalInfo> additionalMap = new HashMap<>();
    private int idCounter = 1;
    private Scanner sc = new Scanner(System.in);
    
    public EventService() {
        loadDataFromFiles();
    }
    
    private boolean isConflicting(LocalDateTime start, LocalDateTime end, int ignoreId) {
        for (Event e : events) {
            // Skip the event itself if we are updating it
            if (e.id == ignoreId) continue;

            // Logic: (StartA < EndB) AND (EndA > StartB)
            if (start.isBefore(e.endDateTime) && end.isAfter(e.startDateTime)) {
                System.out.println("⚠ CONFLICT DETECTED: Clashes with '" + e.title + 
                                   "' [" + e.startDateTime + " to " + e.endDateTime + "]");
                return true;
            }
        }
        return false;
    }

    private void loadDataFromFiles() {
        File eventFile = new File("event.csv");
        if (eventFile.exists()) {
            try (Scanner reader = new Scanner(eventFile)) {
                if (reader.hasNextLine()) reader.nextLine(); // Skip CSV Header
                while (reader.hasNextLine()) {
                    String[] p = reader.nextLine().split(",");
                    if (p.length >= 7){
                        Event e = new Event(Integer.parseInt(p[0]), p[1], p[2], 
                                LocalDateTime.parse(p[3]), LocalDateTime.parse(p[4]), p[5], p[6]);
                        events.add(e);
                        if (e.id >= idCounter) idCounter = e.id + 1; // Keep ID counter synced
                    }
                }
            } catch (Exception e) { System.out.println("Error loading event.csv"); }
        }

        File recurFile = new File("recurrent.csv");
        if (recurFile.exists()) {
            try (Scanner reader = new Scanner(recurFile)) {
                if (reader.hasNextLine()) reader.nextLine(); // Skip header
                while (reader.hasNextLine()) {
                    String[] p = reader.nextLine().split(",");
                    if (p.length == 4) {
                        int id = Integer.parseInt(p[0]);
                        Recurrence r = new Recurrence(id, p[1], Integer.parseInt(p[2]), p[3]);
                        recurrenceMap.put(id, r);
                    }
                }
            } catch (Exception e) { System.out.println("Error loading recurrent.csv"); }
        }
        
        File addFile = new File("additional.csv");
        if (addFile.exists()) {
            try (Scanner reader = new Scanner(addFile)) {
                if (reader.hasNextLine()) reader.nextLine(); // Skip header
                while (reader.hasNextLine()) {
                    String[] p = reader.nextLine().split(",");
                    if (p.length == 3) {
                        int id = Integer.parseInt(p[0]);
                        AdditionalInfo info = new AdditionalInfo(id, p[1], p[2]);
                        additionalMap.put(id, info);
            }
        }
    } catch (Exception e) { System.out.println("Error loading additional.csv"); }
}
    }
    
    // === Create Event ===
    public void createEvent() {
        try {
            System.out.print("Title: ");
            String title = sc.nextLine();
            System.out.print("Description: ");
            String desc = sc.nextLine();
            System.out.print("Location: ");
            String loc = sc.nextLine();
            System.out.print("Priority (High/Medium/Low): ");
            String prio = sc.nextLine();
            System.out.print("Start (YYYY-MM-DD HH:MM): ");
            LocalDateTime start = LocalDateTime.parse(sc.nextLine().replace(" ", "T"));
            System.out.print("End (YYYY-MM-DD HH:MM): ");
            LocalDateTime end = LocalDateTime.parse(sc.nextLine().replace(" ", "T"));

            if (end.isBefore(start)) {
                System.out.println("End before start!");
                return;
            }
            
            if (isConflicting(start, end, -1)) { // -1 because it's a new event
                System.out.print("Do you still want to schedule this? (y/n): ");
                if (!sc.nextLine().equalsIgnoreCase("y")) return;
            }

            int id = idCounter++;
            Event e = new Event(id, title, desc, start, end, loc, prio);
            events.add(e);
            CSVUtils.rewriteEventCSV(events);
            
            System.out.print("Category (e.g., Work/Personal): ");
            String cat = sc.nextLine();
            System.out.print("Attendees (comma separated): ");
            String att = sc.nextLine();
    
            // Save to Map and CSV
            AdditionalInfo info = new AdditionalInfo(id, cat, att);
            additionalMap.put(id, info);
            CSVUtils.rewriteAdditionalCSV(additionalMap);

            System.out.print("Recurring? (y/n): ");
            if (sc.nextLine().equalsIgnoreCase("y")) {
                System.out.print("Interval (1d/1w/1m): ");
                String interval = sc.nextLine();
                System.out.println("1. Times  2. Until Date");
                int type = sc.nextInt(); sc.nextLine();
                int times = 0; String endDate = "0";
                if (type == 1) {
                    System.out.print("Times: ");
                    times = sc.nextInt(); sc.nextLine();
                } else {
                    System.out.print("Until (YYYY-MM-DD): ");
                    endDate = sc.nextLine();
                }
                Recurrence r = new Recurrence(id, interval, times, endDate);
                recurrenceMap.put(id, r);
                CSVUtils.rewriteRecurrenceCSV(recurrenceMap);
            }
            System.out.println("Event created with ID: " + id);
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }
    
    

    // === Update Event ===
    public void updateEvent() {
        System.out.println("\n--- Update Event ---");
        System.out.print("Enter Event ID to update: ");
        try {
            int id = sc.nextInt();
            sc.nextLine();
            Event target = null;
            for (Event e : events) {
                if (e.id == id) { target = e; break; }
            }
            if (target == null) {
                System.out.println("❌ Event not found.");
                return;
            }

            // --- Standard Field Updates ---
            System.out.print("New Title (Enter to keep \"" + target.title + "\"): ");
            String newTitle = sc.nextLine();
            if (!newTitle.trim().isEmpty()) target.title = newTitle;

            System.out.print("New Description (Enter to keep current): ");
            String newDesc = sc.nextLine();
            if (!newDesc.trim().isEmpty()) target.description = newDesc;

            while (true) {
                System.out.print("New Date (YYYY-MM-DD, Enter to keep " + target.date + "): ");
                String dateInput = sc.nextLine().trim();
                if (dateInput.isEmpty()) break;
                try { target.date = LocalDate.parse(dateInput); break; }
                catch (DateTimeParseException e) { System.out.println("⚠ Invalid date format."); }
            }

            System.out.print("New Start (YYYY-MM-DD HH:MM, Enter to keep): ");
            String newStart = sc.nextLine().trim();
            if (!newStart.isEmpty()) {
                target.startDateTime = LocalDateTime.parse(newStart.replace(" ", "T"));
                target.date = target.startDateTime.toLocalDate();
            }

            System.out.print("New End (YYYY-MM-DD HH:MM, Enter to keep): ");
            String newEnd = sc.nextLine().trim();
            if (!newEnd.isEmpty()) {
                target.endDateTime = LocalDateTime.parse(newEnd.replace(" ", "T"));
            }
            
            // Conflict Check
            if (isConflicting(target.startDateTime, target.endDateTime, target.id)) {
                System.out.print("This update conflicts with another event. Proceed anyway? (y/n): ");
                if (!sc.nextLine().equalsIgnoreCase("y")) return; 
            }

            // Recurrence Update
            if (recurrenceMap.containsKey(id)) {
                System.out.print("Update recurrence? (y/n): ");
                if (sc.nextLine().equalsIgnoreCase("y")) {
                    System.out.print("New interval (e.g., 1d, 2w, 1m): ");
                    String interval = sc.nextLine();
                    System.out.println("End condition: 1. Count  2. Until Date");
                    int type = sc.nextInt(); sc.nextLine();
                    int count = 0; LocalDate endDate = null;
                    if (type == 1) {
                        System.out.print("Repeat how many times: ");
                        count = sc.nextInt(); sc.nextLine();
                    } else {
                        System.out.print("Recur until (YYYY-MM-DD): ");
                        endDate = LocalDate.parse(sc.nextLine());
                    }
                    recurrenceMap.put(id, new Recurrence(id, interval, count, endDate));
                }
            }

            // --- NEW: Additional Info Update ---
            System.out.print("Update Category/Attendees? (y/n): ");
            if (sc.nextLine().equalsIgnoreCase("y")) {
                // Get current values if they exist
                String currentCat = "Work"; // default
                String currentAtt = "";
                if (additionalMap.containsKey(id)) {
                    currentCat = additionalMap.get(id).category;
                    currentAtt = additionalMap.get(id).attendees;
                }

                System.out.print("New Category (Current: " + currentCat + "): ");
                String inputCat = sc.nextLine();
                if (inputCat.isEmpty()) inputCat = currentCat;

                System.out.print("New Attendees (Current: " + currentAtt + "): ");
                String inputAtt = sc.nextLine();
                if (inputAtt.isEmpty()) inputAtt = currentAtt;

                AdditionalInfo newInfo = new AdditionalInfo(id, inputCat, inputAtt);
                additionalMap.put(id, newInfo);
            }

            // Save all changes
            CSVUtils.rewriteEventCSV(events);
            CSVUtils.rewriteRecurrenceCSV(recurrenceMap);
            CSVUtils.rewriteAdditionalCSV(additionalMap); // Don't forget this!
            
            System.out.println("✅ Event updated.");
        } catch (Exception e) {
            System.out.println("❌ Invalid input: " + e.getMessage());
            sc.nextLine();
        }
    }

    // === Delete Event ===
    public void deleteEvent() {
        System.out.print("Enter Event ID to delete: ");
        int id = sc.nextInt(); sc.nextLine();
        Event deletedEvent = null;
        for (Event e : events) {
            if (e.id == id) { deletedEvent = e; break; }
        }
        if (deletedEvent != null) {
            System.out.println("You are about to delete: " + deletedEvent.title + " on " + deletedEvent.date);
            System.out.print("Are you sure? (y/n): ");
            if (!sc.nextLine().equalsIgnoreCase("y")) {
                System.out.println("Deletion cancelled.");
                return;
            }
        }
        boolean removed = events.removeIf(e -> e.id == id);
        recurrenceMap.remove(id);
        additionalMap.remove(id);
        if (removed) {
            System.out.println("Event deleted.");
            CSVUtils.rewriteEventCSV(events);
            CSVUtils.rewriteRecurrenceCSV(recurrenceMap);
            CSVUtils.rewriteAdditionalCSV(additionalMap);
        } else {
            System.out.println("ID not found.");
        }
    }
    // === Show Statistics ===
    public void showStatistics() {
        if (events.isEmpty()) {
            System.out.println("No events to analyze.");
            return;
        }

        Map<LocalDate, Integer> counts = new HashMap<>();
        for (Event e : events) {
            counts.put(e.date, counts.getOrDefault(e.date, 0) + 1);
        }

        LocalDate busiestDay = null;
        int maxEvents = 0;

        for (Map.Entry<LocalDate, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > maxEvents) {
                maxEvents = entry.getValue();
                busiestDay = entry.getKey();
            }
        }

        System.out.println("\n--- Calendar Statistics ---");
        System.out.println("Total Events: " + events.size());
        System.out.println("Busiest Day: " + busiestDay + " (" + maxEvents + " events)");
    }

    // === Search Menu ===
    public void searchMenu() {
        System.out.println("1. Search by ID");
        System.out.println("2. Search by Title");
        System.out.println("3. Search by Date");
        int choice = sc.nextInt(); sc.nextLine();
        switch (choice) {
            case 1 -> searchById();
            case 2 -> searchByTitle();
            case 3 -> searchByDate();
            default -> System.out.println("Invalid choice.");
        }
    }

    private void searchById() {
        System.out.print("Enter ID: ");
        int id = sc.nextInt(); sc.nextLine();
        for (Event e : events) {
            if (e.id == id) { System.out.println("Found: " + e); return; }
        }
        System.out.println("Not found.");
    }

    private void searchByTitle() {
        System.out.print("Keyword: ");
        String keyword = sc.nextLine().toLowerCase();
        boolean found = false;
        for (Event e : events) {
            if (e.title.toLowerCase().contains(keyword)) {
                System.out.println("Found: " + e.id + " | " + e.title + " | " + e.date + " | " + e.description);
                found = true;
            }
        }
        if (!found) System.out.println("No events found.");
    }

    private void searchByDate() {
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
    
    public void advancedSearch() {
        System.out.println("1. Search by Priority");
        System.out.println("2. Search by Date Range");
        System.out.println("3. Search by Category");
        int choice = sc.nextInt(); sc.nextLine();

        if (choice == 1) {
            System.out.print("Enter Priority (High/Medium/Low): ");
            String p = sc.nextLine();
            System.out.println("\n--- Results for Priority: " + p.toUpperCase() + " ---");

            for (Event e : events) {
                if (e.priority.equalsIgnoreCase(p)) {
                    // Main line: Date, Time, Title, Location
                    System.out.printf("[%s] %s @ %s | %s (Loc: %s)\n", 
                        e.priority.toUpperCase(), e.date, e.startDateTime.toLocalTime(), e.title, e.location);
                    // Sub-line: Description
                    System.out.println("     └─ Desc: " + e.description);
                    System.out.println("--------------------------------------------------");
                }
            }
        } else if (choice == 2) {
            System.out.print("Start Date (YYYY-MM-DD): ");
            LocalDate start = LocalDate.parse(sc.nextLine());
            System.out.print("End Date (YYYY-MM-DD): ");
            LocalDate end = LocalDate.parse(sc.nextLine());

            System.out.println("\n--- Events from " + start + " to " + end + " ---");
            events.sort(Comparator.comparing(e -> e.startDateTime));

            for (Event e : events) {
                // check if start is after the end
                if (!e.date.isBefore(start) && !e.date.isAfter(end)) {
                    System.out.printf("%s @ %s | %s (Loc: %s)\n", 
                        e.date, e.startDateTime.toLocalTime(), e.title, e.location);
                    System.out.println("     └─ Desc: " + e.description);
                    System.out.println("--------------------------------------------------");
                }
            }
        } else if (choice == 3) {
        System.out.print("Enter Category: ");
        String targetCat = sc.nextLine().toLowerCase();
        System.out.println("\n--- Events in Category: " + targetCat + " ---");
        
        for (AdditionalInfo info : additionalMap.values()) {
            if (info.category.toLowerCase().contains(targetCat)) {
                // We have the ID, now find the matching Event object to display details
                for (Event e : events) {
                    if (e.id == info.eventId) {
                        System.out.printf("[%s] %s | %s\n", 
                            info.category, e.title, e.date);
                         System.out.println("     - Attendees: " + info.attendees);
                    }
                }
            }
        }
        }
    }

    // === View Events ===
    public void viewEvents() {
        System.out.println("1. View All Events  2. Weekly View  3. Monthly View");
        try {
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> viewAllEvents();
                case 2 -> viewWeek();
                case 3 -> viewMonth();
                default -> System.out.println("Invalid choice.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input.");
            sc.nextLine();
      }
    }
    
    static void viewAllEvents() {
        if (events.isEmpty()) {
            System.out.println("No events found.");
            return;
        }

        // Sort events by start date & time
        events.sort(Comparator.comparing(e -> e.startDateTime));
        System.out.println("\n--- All Events ---");
        for (Event e : events) {
            // Fetch additional info safely
            String category = "No Category";
            String attendees = "None";
            
            if (additionalMap.containsKey(e.id)) {
                AdditionalInfo info = additionalMap.get(e.id);
                category = info.category;
                attendees = info.attendees;
            }

            System.out.println(
                "ID: " + e.id +
                " | " + e.title +
                " | " + e.startDateTime +
                " | Cat: " + category + 
                " | Att: " + attendees
            );
        }
    }

    private void viewWeek() {
        System.out.print("Start date (YYYY-MM-DD): ");
        try {
            LocalDate start = LocalDate.parse(sc.nextLine());
            // Calculate previous Sunday
            LocalDate weekStart = start.minusDays(start.getDayOfWeek().getValue() % 7);

            System.out.println("\n--- Week of " + weekStart + " ---");
            for (int i = 0; i < 7; i++) {
                LocalDate day = weekStart.plusDays(i);
                
                // This helper fetches the formatted string with Category included
                List<String> eventsOnDay = getEventTitlesForDate(day); 

                System.out.println(day + " (" + day.getDayOfWeek().toString().substring(0, 3) + "): ");
                
                if (eventsOnDay.isEmpty()) {
                    System.out.println("    -");
                } else {
                    for (String eventStr : eventsOnDay) {
                        // eventStr already contains the Category from the helper method
                        System.out.println("    " + eventStr);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Invalid date input.");
        }
    }

    public void viewMonth() {
        System.out.println("\n--- Monthly View ---");
        System.out.print("Enter month and year (MM YYYY): ");
        int month, year;
        try {
            month = sc.nextInt();
            year = sc.nextInt();
            sc.nextLine(); // Clear buffer
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter numbers (e.g., 10 2025).");
            sc.nextLine();
            return;
        }

        LocalDate firstDay = LocalDate.of(year, month, 1);
        int daysInMonth = firstDay.lengthOfMonth();
        int startDayOffset = firstDay.getDayOfWeek().getValue() % 7;

        System.out.println("\n====================================");
        System.out.println("       " + firstDay.getMonth() + " " + year);
        System.out.println("====================================");
        System.out.println(" SUN  MON  TUE  WED  THU  FRI  SAT");

        // 1. Print leading spaces
        for (int i = 0; i < startDayOffset; i++) System.out.print("     ");

        // 2. Print the days grid
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate current = LocalDate.of(year, month, day);
            // This helper handles the grid markers [ 5] vs  5
            List<String> titles = getEventTitlesForDate(current); 
            
            if (!titles.isEmpty()) System.out.printf("[%2d] ", day);
            else System.out.printf(" %2d  ", day);

            if ((day + startDayOffset) % 7 == 0) System.out.println();
        }
        System.out.println("\n====================================");
        System.out.println("           MONTHLY AGENDA");
        System.out.println("====================================");

        // 3. Detailed Summary (UPDATED for Category)
        boolean anyEvents = false;
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate current = LocalDate.of(year, month, day);
            
            for (Event e : events) {
                boolean isMatch = false;
                if (e.date.equals(current)) {
                    isMatch = true;
                } else if (recurrenceMap.containsKey(e.id)) {
                    if (current.isAfter(e.date)) {
                        isMatch = checkRecurrence(e.date, current, recurrenceMap.get(e.id));
                    }
                }

                if (isMatch) {
                    // --- NEW: Fetch Category ---
                    String category = "No Category";
                    if (additionalMap.containsKey(e.id)) {
                        category = additionalMap.get(e.id).category;
                    }

                    // Display: Date Day: Title (Time) - [Category]
                    System.out.printf("%02d %s: %s (%s - %s) - [%s]\n", 
                        day, 
                        current.getDayOfWeek().toString().substring(0,3), 
                        e.title, 
                        e.startDateTime.toLocalTime(), 
                        e.endDateTime.toLocalTime(),
                        category); // <--- Added this
                    anyEvents = true;
                }
            }
        }

        if (!anyEvents) System.out.println("No events scheduled for this month.");
        System.out.println("====================================");
    }
    // === Helper for recurrence ===
    private List<String> getEventTitlesForDate(LocalDate targetDate) {
        List<String> result = new ArrayList<>();
        for (Event e : events) {
            boolean isMatch = false;
            // 1. Check if it is the specific date (Normal event OR Start of recurring)
            if (e.date.equals(targetDate)) {
                isMatch = true;
            } 
            // 2. Check if it is a recurring instance
            else if (recurrenceMap.containsKey(e.id)) {
                Recurrence r = recurrenceMap.get(e.id);
                // Only check recurrence if we are strictly AFTER the start date
                if (targetDate.isAfter(e.date)) {
                     isMatch = checkRecurrence(e.date, targetDate, r);
                }
            }

            if (isMatch) {
                // Fetch Category for display
                String category = "No Category";
                if (additionalMap.containsKey(e.id)) {
                    category = additionalMap.get(e.id).category;
                }

                // FORMAT: [17:40] Title (Category)
                String formatted = String.format("[%s] %s (%s)", 
                    e.startDateTime.toLocalTime(), e.title, category);
                result.add(formatted);
            }
        }
        return result;
    }
    
    

    private boolean checkRecurrence(LocalDate start, LocalDate current, Recurrence r) {
        String interval = r.recurrentInterval;
        // Parse the number (e.g., "2" from "2w")
        int value = Integer.parseInt(interval.substring(0, interval.length() - 1));
        // Parse the unit (e.g., "w")
        String unit = interval.substring(interval.length() - 1);

        long diff = switch (unit) {
            case "d" -> ChronoUnit.DAYS.between(start, current);
            case "w" -> ChronoUnit.WEEKS.between(start, current);
            case "m" -> ChronoUnit.MONTHS.between(start, current);
            default -> 0;
        };

        if (diff <= 0 || diff % value != 0) return false;
        if (r.recurrentTimes > 0 && diff / value > r.recurrentTimes) return false;
        if (!r.recurrentEndDate.equals("0")) {
            LocalDate endDate = LocalDate.parse(r.recurrentEndDate);
            if (current.isAfter(endDate)) return false;
        }

        // 3. precise Interval Logic
        long occurrencesPassed = 0;

        switch (unit) {
            case "d" -> {
                long days = ChronoUnit.DAYS.between(start, current);
                if (days % value != 0) return false;
                occurrencesPassed = days / value;
            }
            case "w" -> {
                long days = ChronoUnit.DAYS.between(start, current);
                // MUST be a multiple of 7 days to land on the same day of the week
                if (days % 7 != 0) return false; 
                
                long weeks = days / 7;
                if (weeks % value != 0) return false;
                occurrencesPassed = weeks / value;
            }
            case "m" -> {
                // MUST match the exact day of the month (e.g., the 10th)
                if (current.getDayOfMonth() != start.getDayOfMonth()) return false;

                long months = ChronoUnit.MONTHS.between(start, current);
                if (months % value != 0) return false;
                occurrencesPassed = months / value;
            }
            default -> { return false; }
        }

        // 4. Check "Times" Limit (if set)
        if (r.recurrentTimes > 0 && occurrencesPassed > r.recurrentTimes) {
            return false;
        }

        return true;
    }

    // === Backup Events ===
    public void backupEvents() {
        try (PrintWriter pw = new PrintWriter("calendar_backup.txt")) {
            // Section 1: Events
            pw.println("===EVENTS===");
            for (Event e : events) {
                // Store all fields: ID, Title, Desc, Start, End
                pw.println(e.id + "|" + e.title + "|" + e.description + "|" + 
                        e.startDateTime + "|" + e.endDateTime + "|" + e.location + "|" + e.priority);
            }

            // Section 2: Recurrences
            pw.println("===RECURRENCE===");
            for (Recurrence r : recurrenceMap.values()) {
                pw.println(r.eventId + "|" + r.recurrentInterval + "|" + r.recurrentTimes + "|" + r.recurrentEndDate);
            }
            
            pw.println("===ADDITIONAL===");
            for (AdditionalInfo info : additionalMap.values()) {
                pw.println(info.eventId + "|" + info.category + "|" + info.attendees);
            }
            System.out.println("Backup completed to calendar_backup.txt"); // Required output format 
        } catch (IOException e) {
            System.out.println("Backup failed: " + e.getMessage());
        }
    }
    // === Restore Events ===
    public void restoreEvents() {
        File backupFile = new File("calendar_backup.txt");
        if (!backupFile.exists()) {
            System.out.println("No backup file found.");
            return;
        }

        try (Scanner reader = new Scanner(backupFile)) {
            events.clear();
            recurrenceMap.clear();
            additionalMap.clear();
            String currentSection = "";
            int maxId = 0;

            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                if (line.equals("===EVENTS===")) {
                    currentSection = "EVENTS";
                    continue;
                } else if (line.equals("===RECURRENCE===")) {
                    currentSection = "RECURRENCE";
                    continue;
                } else if (line.equals("===ADDITIONAL===")) {
                    currentSection = "ADDITIONAL";
                    continue;
                }

                String[] parts = line.split("\\|");
                if (currentSection.equals("EVENTS") && parts.length == 7) {
                    int id = Integer.parseInt(parts[0]);
                    Event e = new Event(id, parts[1], parts[2], LocalDateTime.parse(parts[3]), 
                            LocalDateTime.parse(parts[4]), parts[5], parts[6]);
                    events.add(e);
                    if (id > maxId) maxId = id;
                } else if (currentSection.equals("RECURRENCE") && parts.length == 4) {
                    int id = Integer.parseInt(parts[0]);
                    Recurrence r = new Recurrence(id, parts[1], Integer.parseInt(parts[2]), parts[3]);
                    recurrenceMap.put(id, r);
                } else if (currentSection.equals("ADDITIONAL") && parts.length == 3) {
                    int id = Integer.parseInt(parts[0]);
                    AdditionalInfo info = new AdditionalInfo(id, parts[1], parts[2]);
                    additionalMap.put(id, info);
                }
            }

            idCounter = maxId + 1; // Sync the ID counter [cite: 17]

            // Persist to local CSV files immediately 
            CSVUtils.rewriteEventCSV(events);
            CSVUtils.rewriteRecurrenceCSV(recurrenceMap);
            CSVUtils.rewriteAdditionalCSV(additionalMap);

            System.out.println("Restore successful! Loaded " + events.size() + " events.");
        } catch (Exception e) {
            System.out.println("Error during restore: " + e.getMessage());
        }
    }
}

