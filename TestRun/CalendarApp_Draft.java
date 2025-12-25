import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;


class Event {
    int id;
    String title;
    String description; 
    LocalDate date;     

    Event(int id, String title, LocalDate date) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.description = "No description"; 
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
        
        Event e1 = new Event(idCounter++,"", LocalDate.now());
        events.add(e1);
        
        recurrenceMap.put(e1.id, new Recurrence(e1.id, "1d", 3, null)); 

        while (true) {
            System.out.println("\n=== CALENDAR APP ===");
            System.out.println("1. Create Event");
            System.out.println("2. View Events (Week/Month)");
            System.out.println("3. Update Event");
            System.out.println("4. Delete Event");
            System.out.println("5. Exit");
            System.out.print("Enter choice: ");
            
            try {
                int choice = sc.nextInt();
                sc.nextLine(); 

                switch (choice) {
    case 1 -> createEvent();
    case 2 -> viewEvents();
    case 3 -> updateEvent();   // NEW
    case 4 -> deleteEvent();
    case 5 -> {
        System.out.println("Exiting...");
        System.exit(0);
    }
    default -> System.out.println("Invalid choice.");
}

            } catch (InputMismatchException e) {
                System.out.println("Invalid input.");
                sc.nextLine();
            }
        }
    }

   
    static List<String> getEventTitlesForDate(LocalDate targetDate) {
        List<String> titles = new ArrayList<>();

        for (Event e : events) {
            boolean isMatch = false;

            
            if (e.date.equals(targetDate)) {
                isMatch = true;
            }
         
            else if (recurrenceMap.containsKey(e.id)) {
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
            
            if(start.getDayOfWeek() != current.getDayOfWeek()) return false;
            unitAmount = value;
        } else if (unit.equalsIgnoreCase("m")) {
            diff = ChronoUnit.MONTHS.between(start, current);
          
            if(start.getDayOfMonth() != current.getDayOfMonth()) return false;
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
            System.out.print("Date (YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(sc.nextLine());

            int newId = idCounter++;
            events.add(new Event(newId, title, date));

            System.out.print("Is this a recurring event? (y/n): ");
            String isRecur = sc.nextLine();

            if (isRecur.equalsIgnoreCase("y")) {
                System.out.print("Interval (e.g., 1d, 2w, 1m): ");
                String interval = sc.nextLine();

                System.out.println("End condition type: 1. Count (Times)  2. Until Date");
                int type = sc.nextInt();
                sc.nextLine(); 

                int count = 0;
                LocalDate endDate = null;

                if (type == 1) {
                    System.out.print("How many times to repeat (after first): ");
                    count = sc.nextInt();
                    sc.nextLine();
                } else {
                    System.out.print("Recur until (YYYY-MM-DD): ");
                    endDate = LocalDate.parse(sc.nextLine());
                }

                
                recurrenceMap.put(newId, new Recurrence(newId, interval, count, endDate));
            }

            System.out.println("Event created!");

        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format.");
        } catch (Exception e) {
            System.out.println("Error creating event: " + e.getMessage());
        }
    }

    
    static void viewEvents() {
        System.out.println("1. Weekly View  2. Monthly View");
        int choice = sc.nextInt();
        sc.nextLine();

        if (choice == 1) viewWeek();
        else viewMonth();
    }

    static void viewWeek() {
        System.out.print("Start date (YYYY-MM-DD): ");
        LocalDate start = LocalDate.parse(sc.nextLine());
        LocalDate weekStart = start.minusDays(start.getDayOfWeek().getValue() % 7);

        System.out.println("\n--- Week of " + weekStart + " ---");
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            
            
            List<String> titles = getEventTitlesForDate(day);

            System.out.print(day + " (" + day.getDayOfWeek().toString().substring(0,3) + "): ");
            if (titles.isEmpty()) System.out.println("-");
            else System.out.println(String.join(", ", titles));
        }
    }

    static void viewMonth() {
        System.out.print("Enter month and year (MM YYYY): ");
        int month = sc.nextInt();
        int year = sc.nextInt();
        sc.nextLine();

        LocalDate firstDay = LocalDate.of(year, month, 1);
        int daysInMonth = firstDay.lengthOfMonth();

        System.out.println("\nEvents in " + firstDay.getMonth() + " " + year + ":");
        
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

            if (dateInput.isEmpty()) break;

            try {
                target.date = LocalDate.parse(dateInput);
                break;
            } catch (DateTimeParseException e) {
                System.out.println("⚠ Invalid date format. Try again.");
            }
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
        
       
        boolean removed = events.removeIf(e -> e.id == id);
        
        
        if (recurrenceMap.containsKey(id)) {
            recurrenceMap.remove(id);
            System.out.println("Recurrence rule removed.");
        }

        if (removed) System.out.println("Event deleted.");
        else System.out.println("ID not found.");
    }
}