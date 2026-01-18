package app;

import java.util.Scanner;
import java.util.InputMismatchException;

public class CalendarApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        EventService service = new EventService();

        while (true) {
            System.out.println("\n=== CALENDAR APP ===");
            System.out.println("1. Create Event");
            System.out.println("2. View Events (All/Weekly/Monthly)");
            System.out.println("3. Update Event");
            System.out.println("4. Delete Event");
            System.out.println("5. Search Menu (ID/Title/Date)");
            System.out.println("6. Advanced Search (Priority/Range/Category)"); 
            System.out.println("7. Show Statistics");                
            System.out.println("8. Backup Events");
            System.out.println("9. Restore Events");
            System.out.println("10. Exit");
            System.out.print("Enter choice: ");

            try {
                int choice = sc.nextInt(); 
                sc.nextLine(); // Clear buffer

                switch (choice) {
                    case 1 -> service.createEvent();
                    case 2 -> service.viewEvents();
                    case 3 -> service.updateEvent();
                    case 4 -> service.deleteEvent();
                    case 5 -> service.searchMenu();
                    case 6 -> service.advancedSearch(); 
                    case 7 -> service.showStatistics();
                    case 8 -> service.backupEvents();
                    case 9 -> service.restoreEvents();
                    case 10 -> { System.out.println("Exiting..."); return; }
                    default -> System.out.println("Invalid choice. Please pick 1-10.");
                }
            } catch (InputMismatchException e) {
                System.out.println("❌ Error: Please enter a valid number.");
                sc.nextLine(); 
            }
        }
    }
}