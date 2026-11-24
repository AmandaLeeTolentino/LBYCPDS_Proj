package main;

import java.util.*;
import java.io.*;

/**
 * Student side of the program: the student dashboard, reserve parking menu, filter menu, and transaction history.
 */
public class StudentMenu {
    private static final List<ParkingLocation> locations = LoginRegistration.locations;
    private static final ReservationService reservationService = LoginRegistration.reservationService;

    // ==========================================
    // STUDENT MENU & SUB-MENUS
    // ==========================================
    public static void showStudentMenu(String username) {
        Scanner scanner = new Scanner(System.in);
        boolean studentSession = true;
        String name = LoginRegistration.getDisplayName(username);

        while (studentSession) {
            System.out.println(Main.Colors.YELLOW +"\n╔══════════ ≪ STUDENT DASHBOARD ≫ ══════════╗");
            System.out.println("\n   ⋅  ⋅  ⋅  ⋅  Welcome, " + name +"! ⋅  ⋅  ⋅  ⋅"+ Main.Colors.RESET);
            System.out.println("\n\t\t1. DLSU Parking Status");
            System.out.println("\t\t2. Reservation & Cancellation");
            System.out.println("\t\t3. Transaction History");
            System.out.println("\t\t4. Logout");
            System.out.println(Main.Colors.paint(Main.Colors.YELLOW,"\n╚═══════════════════════════════════════════╝"));

            System.out.print(Main.Colors.paint(Main.Colors.CYAN,"\nEnter choice: "));
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" : {
                    new ParkingStatMenu(locations, reservationService).start();
                    break;
                }
                case "2":
                    showReserveParkingMenu(scanner, username);
                    break;
                case "3":
                    showTransactionHistory(scanner, username);
                    System.out.println(Main.Colors.paint(Main.Colors.GREEN,"\nPress Enter to continue..."));
                    scanner.nextLine();
                    break;
                case "4":
                    System.out.println(Main.Colors.paint(Main.Colors.GREEN,"\nLogging out..."));
                    studentSession = false;
                    break;
                default:
                    System.out.println(Main.Colors.paint(Main.Colors.RED,"\nInvalid choice! Press Enter to try again...\n"));
                    scanner.nextLine();
                    break;
            }
        }
    }

    public static void showTransactionHistory(Scanner scanner, String username) { // change to name
        java.time.format.DateTimeFormatter dateFmt = java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy");
        java.time.format.DateTimeFormatter timeFmt = java.time.format.DateTimeFormatter.ofPattern("h:mm a");
        String name = LoginRegistration.getDisplayName(username);

        System.out.println(Main.Colors.paint(Main.Colors.CYAN,"\n╭── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ TRANSACTION HISTORY for " + name + " ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ──╮")); // change to name

        int counter = 0;
        double totalSpent = 0;

        for (Reservation r : ReservationHistoryWriter.loadCsv()) {
            if (!r.getUsername().equals(username)) continue;   // not this user's row, skip

            if (counter == 0) {   // print the table header once, before the first row
                System.out.println(Main.Colors.paint(Main.Colors.CYAN,String.format("\n%-4s %-6s %-22s %-11s %-13s %-19s %-9s %-12s %s",
                        "#", "ID", "Building", "Slot", "Date", "Time", "Duration", "Fee", "Status")));
                System.out.println(Main.Colors.paint(Main.Colors.CYAN,"⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ "));
            }

            counter++;
            String time = r.getStart().format(timeFmt) + " - " + r.getEnd().format(timeFmt);

            System.out.println(String.format("%-4d %-6s %-22s %-11s %-13s %-19s %-9s %-12s %s",
                    counter,
                    r.getReservationId(),
                    r.getLocationName(),
                    r.getSlotId(),
                    r.getStart().format(dateFmt),
                    time,
                    r.getDurationHours() + " hrs",
                    String.format("Php %.2f", r.getTotalAmount()),
                    r.getStatus()));

            if (r.getStatus() != ReservationStatus.CANCELED) {
                totalSpent += r.getTotalAmount();
            }
        }
        if (counter == 0) {
            System.out.println("You have no reservations yet.");
        } else {
            System.out.println(Main.Colors.paint(Main.Colors.CYAN,"⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ "));
            System.out.println("\nTotal reservations:\t\t" + counter);
            System.out.println(String.format("Total spent:\t\t\tPhp %.2f", totalSpent));
        }
    }

    public static void showReserveParkingMenu(Scanner scanner, String username) {
        boolean reserveSession = true;
        while (reserveSession) {

            System.out.println(Main.Colors.paint(Main.Colors.YELLOW, """
            
            ╭─╮╭─╴╭─╮╭─╴╭─╮╷ ╷╭─╴   ╭─╮╭─╮╭─╮╷╭ ╷╭╮╷╭─╴
     ╭── ⋅ ⋅ ├┬╯├╴ ╰─╮├╴ ├┬╯│╭╯├╴    ├─╯├─┤├┬╯├┴╮││╰┤│╶╮ ⋅ ⋅ ──╮
            ╵╰╴╰─╴╰─╯╰─╴╵╰╴╰╯ ╰─╴   ╵  ╵ ╵╵╰╴╵ ╵╵╵ ╵╰─╯
            
                            """));
            System.out.println("""
                    1.  Filter Category (rate/distance)
                    2.  Reserve Slot Directly
                    3.  Reschedule Reservation
                    4.  Cancel Reservation
                    5.  Back to Student Menu
                                                        
       """);
            System.out.println(Main.Colors.paint(Main.Colors.YELLOW,"╰─ ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅  ✩  ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ─╯"));
            System.out.print(Main.Colors.paint(Main.Colors.CYAN,"\nEnter choice (1/3):  "));
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    showFilterCategoryMenu(scanner, username);
                    break;
                case "2":
                    ReservationMenu reservationMenu = new ReservationMenu(locations, reservationService, username);
                    reservationMenu.start();
                    break;
                case "3":
                    new ReservationMenu(locations, reservationService, username).rescheduleFlow();
                    break;
                case "4":
                    new ReservationMenu(locations, reservationService, username).cancelFlow();
                    break;
                case "5":
                    reserveSession = false;
                    break;
                default:
                    System.out.println(Main.Colors.paint(Main.Colors.RED,"\nInvalid choice! Press Enter to try again...\n"));
                    scanner.nextLine();
                    break;
            }
        }
    }

    public static void showFilterCategoryMenu(Scanner scanner, String username) {
        LocationFilter filter = new LocationFilter(locations, reservationService, username, scanner);
        boolean filterSession = true;
        while (filterSession) {
            System.out.println(Main.Colors.YELLOW +"\n╔═════════ ≪ FILTER CATEGORY ≫ ═════════╗" + Main.Colors.RESET);
            System.out.println("\n\t\t1. Filter by Distance");
            System.out.println("\t\t2. Filter by Rate");
            System.out.println("\t\t3. Back to Reserve Menu");
            System.out.println(Main.Colors.paint(Main.Colors.YELLOW,"\n╚═══════════════════════════════════════╝"));

            System.out.print(Main.Colors.paint(Main.Colors.CYAN,"\nEnter choice (1/3): "));
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    filter.filterByDistance();
                    break;
                case "2":
                    filter.filterByRate();
                    break;
                case "3":
                    filterSession = false;
                    break;
                default:
                    System.out.println(Main.Colors.paint(Main.Colors.RED,"\nInvalid choice! Press Enter to try again...\n"));
                    scanner.nextLine();
                    break;
            }
        }
    }
}