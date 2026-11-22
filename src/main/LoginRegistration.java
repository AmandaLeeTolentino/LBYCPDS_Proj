package main;

import java.util.*;
import java.io.*;

public class LoginRegistration {
    static final List<ParkingLocation> locations = LocationData.createLocations();
    static final ReservationService reservationService = new ReservationService();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // MAIN MENU !!!
        boolean menu = true;
        while (menu) {
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, """
                            ╷ ╷╭─╴╷  ╭─╴╭─╮╭┬╮╭─╴
                ╭── ⋅ ⋅ ──   │╷│├╴ │  │  │ ││││├╴  ── ⋅ ⋅ ──╮
                            ╰┴╯╰─╴╰─╴╰─╴╰─╯╵ ╵╰─╴                                
                """));

        System.out.println("""
                            
                    1.  Login
                    2.  Register
                    3.  Exit
                                                        
       """);

            System.out.println(Main.Colors.paint(Main.Colors.YELLOW,"╰─ ── ⋅ ⋅ ── ── ⋅ ⋅ ── ✩ ── ⋅ ⋅ ── ── ⋅ ⋅ ── ─╯"));

            System.out.print(Main.Colors.paint(Main.Colors.CYAN,"\nEnter choice (1/3):  "));
            String Choice = scanner.nextLine().trim();

            switch (Choice) {
                case "1":
                    String[] loginResult = Login(); // returns [Username, Role]
                    if (loginResult != null) {
                        String username = loginResult[0];
                        String role = loginResult[1];

                        // Route user to the correct menu based on their role
                        if (role.equalsIgnoreCase("STUDENT")) {
                            showStudentMenu(username);
                        } else if (role.equalsIgnoreCase("ADMIN")) {
                            AdminMenu.showAdminMenu(username);
                        }
                    }
                    break;
                case "2":
                    Register();
                    break;
                case "3":
                    System.out.println(Main.Colors.paint(Main.Colors.GREEN,"\nExiting program. Goodbye!"));
                    menu = false;
                    break;
                default:
                    System.out.println(Main.Colors.paint(Main.Colors.RED,"\nInvalid choice! Press Enter to try again...\n"));
                    scanner.nextLine();
                    break;
            }
        }
        scanner.close();
    }

    // ==========================================
    // STUDENT MENU & SUB-MENUS
    // ==========================================
    public static void showStudentMenu(String username) {
        Scanner scanner = new Scanner(System.in);
        boolean studentSession = true;

        while (studentSession) {
            System.out.println(Main.Colors.YELLOW +"\n╔══════════ ≪ STUDENT DASHBOARD ≫ ══════════╗");
            System.out.println("\n   ⋅  ⋅  ⋅  ⋅  Welcome, " + username +"! ⋅  ⋅  ⋅  ⋅"+ Main.Colors.RESET);
            System.out.println("\n\t\t1. DLSU Parking Status");
            System.out.println("\t\t2. Reserve Parking");
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

    public static void showTransactionHistory(Scanner scanner, String username) {
        java.time.format.DateTimeFormatter dateFmt = java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy");
        java.time.format.DateTimeFormatter timeFmt = java.time.format.DateTimeFormatter.ofPattern("h:mm a");

        System.out.println(Main.Colors.paint(Main.Colors.CYAN,"\n╭── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ TRANSACTION HISTORY for " + username + " ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ──╮"));

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
                    3.  Back to Student Menu
                                                        
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

    // ==========================================
    // LOGIN & REGISTER METHODS
    // ==========================================
    public static String[] Login() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, """
  
      ╷  ╭─╮╭─╴╷╭╮╷   ╭─╮╭─╴╭─╴╭─╮╷ ╷╭╮╷╶┬╴
   ══ │  │ ││╶╮││╰┤   ├─┤│  │  │ ││ ││╰┤ │  ══
      ╰─╴╰─╯╰─╯╵╵ ╵   ╵ ╵╰─╴╰─╴╰─╯╰─╯╵ ╵ ╵
   """));

        System.out.print(Main.Colors.paint(Main.Colors.CYAN,"Enter username:\t\t"));
        String Username = scanner.nextLine().trim();

        System.out.print(Main.Colors.paint(Main.Colors.CYAN,"Enter password:\t\t"));
        String Password = scanner.nextLine().trim();

        if (Username.isEmpty() || Password.isEmpty()) {
            System.out.println(Main.Colors.paint(Main.Colors.RED,"\nFields cannot be blank! Press Enter to go back..."));
            scanner.nextLine();
            return null;
        }

        String usersPath = "src/resources/Users.csv";
        File file = new File(usersPath);
        boolean accountExists = false;
        String foundRole = "";
        boolean passwordMatch = false;

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] userDetails = line.split(",", -1);
                    if (userDetails.length >= 3) {
                        String storedUser = userDetails[0].trim();
                        String storedPass = userDetails[1].trim();
                        String storedRole = userDetails[2].trim();

                        if (storedUser.equals(Username)) {
                            accountExists = true;
                            if (storedPass.equals(Password)) {
                                passwordMatch = true;
                                foundRole = storedRole;
                            }
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(Main.Colors.paint(Main.Colors.RED,"Error reading user database: " + e.getMessage()));
            }
        }

        if (!accountExists) {
            System.out.println(Main.Colors.paint(Main.Colors.RED,"\nNo account yet! Please register first. Press Enter to continue..."));
            scanner.nextLine();
            return null;
        } else if (!passwordMatch) {
            System.out.println(Main.Colors.paint(Main.Colors.RED,"\nIncorrect password! Press Enter to continue..."));
            scanner.nextLine();
            return null;
        }

        System.out.println(Main.Colors.paint(Main.Colors.GREEN,"\nLogin Successful! Role: " + foundRole + ". Press Enter to continue. "));
        scanner.nextLine();

        return new String[] { Username, foundRole };
    }

    public static void Register() {
        Scanner scanner = new Scanner(System.in);
        String usersPath = "src/resources/Users.csv";
        String username = "";
        String password = "";
        String confirmation = "";
        String role = "";
        boolean registration = true;

        while (registration) {
            System.out.println(Main.Colors.paint(Main.Colors.YELLOW,"""

   ╭─╮╭─╴╭─╴╷╭─╮╶┬╴╭─╴╭─╮   ╭─╮╭─╴╭─╴╭─╮╷ ╷╭╮╷╶┬╴
══ ├┬╯├╴ │╶╮│╰─╮ │ ├╴ ├┬╯   ├─┤│  │  │ ││ ││╰┤ │  ══
   ╵╰╴╰─╴╰─╯╵╰─╯ ╵ ╰─╴╵╰╴   ╵ ╵╰─╴╰─╴╰─╯╰─╯╵ ╵ ╵
   """));


            boolean validation = true;

            while (validation) {
                System.out.print(Main.Colors.paint(Main.Colors.CYAN,"Enter username:\t\t"));
                username = scanner.nextLine().trim();

                System.out.print(Main.Colors.paint(Main.Colors.CYAN,"Enter password:\t\t"));
                password = scanner.nextLine().trim();

                System.out.print(Main.Colors.paint(Main.Colors.CYAN,"Confirm password:\t"));
                confirmation = scanner.nextLine().trim();

                if (username.isEmpty() || password.isEmpty() || confirmation.isEmpty()) {
                    System.out.println(Main.Colors.paint(Main.Colors.RED,"\nInvalid input! Fields cannot be empty.\n"));
                }
                else if (!password.equals(confirmation)) {
                    System.out.println(Main.Colors.paint(Main.Colors.RED,"\nError! Passwords do not match.\n"));
                }
                else {
                    boolean usernameExists = false;
                    File file = new File(usersPath);

                    if (file.exists()) {
                        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                            String line;
                            while ((line = br.readLine()) != null) {
                                if (line.trim().isEmpty()) continue;
                                String[] userDetails = line.split(",", -1);
                                if (userDetails.length > 0 && userDetails[0].trim().equals(username)) {
                                    usernameExists = true;
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            System.out.println(Main.Colors.paint(Main.Colors.RED,"Error reading user database: " + e.getMessage()));
                        }
                    }

                    if (usernameExists) {
                        System.out.println(Main.Colors.paint(Main.Colors.RED,"\nError! Username already taken. Please choose another.\n"));
                    } else {
                        validation = false;
                    }
                }
            }

            boolean roleChoice = true;
            while (roleChoice) {
                System.out.println("""
       
        Choose Account Role       
            1.  Student
            2.  Administrator              
""");

                System.out.print(Main.Colors.paint(Main.Colors.CYAN,"\nEnter Role: "));
                role = scanner.nextLine().trim();

                switch (role) {
                    case "1":
                        role = "STUDENT";
                        roleChoice = false;
                        break;
                    case "2":
                        role = "ADMIN";
                        roleChoice = false;
                        break;
                    default:
                        System.out.println(Main.Colors.paint(Main.Colors.RED,"\nInvalid choice! Press Enter to try again...\n"));
                        scanner.nextLine();
                        break;
                }
            }

            try (PrintWriter pw = new PrintWriter(new FileWriter(usersPath, true))) {
                pw.println(username + "," + password + "," + role);

                System.out.println(Main.Colors.paint(Main.Colors.GREEN,"\n" + username + " (" + role + ") successfully registered! Press Enter to continue..."));
                scanner.nextLine();

                registration = false;
            } catch (IOException e) {
                System.out.println(Main.Colors.paint(Main.Colors.RED,"\nError saving user credentials: " + e.getMessage()));
                return;
            }
        }
    }
}

