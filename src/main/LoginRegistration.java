package main;

import java.util.*;
import java.io.*;

public class LoginRegistration {
    private static final List<ParkingLocation> locations = LocationData.createLocations();
    private static final ReservationService reservationService = new ReservationService();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // MAIN MENU !!!
        boolean menu = true;
        while (menu) {
            System.out.println("""

                ╷ ╷╭─╴╷  ╭─╴╭─╮╭┬╮╭─╴
                │╷│├╴ │  │  │ ││││├╴
                ╰┴╯╰─╴╰─╴╰─╴╰─╯╵ ╵╰─╴

    1. Login
    2. Register
    3. Exit
            """);

            System.out.print("Enter choice (1/3):  ");
            String Choice = scanner.nextLine();

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
                            showAdminMenu(username);
                        }
                    }
                    break;
                case "2":
                    Register();
                    break;
                case "3":
                    System.out.println("\nExiting program. Goodbye!");
                    menu = false;
                    break;
                default:
                    System.out.print("\nInvalid choice! Press Enter to continue.");
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
            System.out.println("\n========================================");
            System.out.println("   STUDENT DASHBOARD - Welcome, " + username);
            System.out.println("========================================");
            System.out.println("1. Reserve Parking");
            System.out.println("2. Print Receipt");
            System.out.println("3. Transaction History");
            System.out.println("4. Logout");
            System.out.print("\nEnter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    showReserveParkingMenu(scanner, username);
                    break;
                case "2":
                    // 24-hour receipt logic simulation
                    boolean hasRecentTransaction = false; // Change to true if within 24 hours
                    if (!hasRecentTransaction) {
                        System.out.println("\nNo recent transaction to print receipt.");
                    } else {
                        System.out.println("\n[Placeholder] Printing receipt for your recent transaction...");
                    }
                    System.out.print("Press Enter to continue...");
                    scanner.nextLine();
                    break;
                case "3":
                    System.out.println("\n[Placeholder] Transaction History selected.");
                    System.out.print("Press Enter to continue...");
                    scanner.nextLine();
                    break;
                case "4":
                    System.out.println("\nLogging out...");
                    studentSession = false;
                    break;
                default:
                    System.out.println("\nInvalid choice! Press Enter to continue.");
                    scanner.nextLine();
                    break;
            }
        }
    }

    public static void showReserveParkingMenu(Scanner scanner, String username) {
        boolean reserveSession = true;
        while (reserveSession) {
            System.out.println("\n--- RESERVE PARKING ---");
            System.out.println("1. Filter Category (Narrow down options)");
            System.out.println("2. Reserve Slot Directly (Select building, floor, lot)");
            System.out.println("3. Back to Student Menu");
            System.out.print("\nEnter choice: ");
            String choice = scanner.nextLine();

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
                    System.out.println("\nInvalid choice! Press Enter to continue.");
                    scanner.nextLine();
                    break;
            }
        }
    }

    public static void showFilterCategoryMenu(Scanner scanner, String username) {
        LocationFilter filter = new LocationFilter(locations, reservationService, username, scanner);
        boolean filterSession = true;
        while (filterSession) {
            System.out.println("\n--- FILTER CATEGORY ---");
            System.out.println("1. Filter by Distance");
            System.out.println("2. Filter by Rate");
            System.out.println("3. Filter by Promo");
            System.out.println("4. Back to Reserve Menu");
            System.out.print("\nEnter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    filter.filterByDistance();
                    break;
                case "2":
                    filter.filterByRate();
                    break;
                case "3":
                    filter.filterByPromo();
                    break;
                case "4":
                    filterSession = false;
                    break;
                default:
                    System.out.println("\nInvalid choice! Press Enter to continue.");
                    scanner.nextLine();
                    break;
            }
        }
    }

    // ==========================================
    // ADMIN MENU & SUB-MENUS
    // ==========================================
    public static void showAdminMenu(String username) {
        Scanner scanner = new Scanner(System.in);
        boolean adminSession = true;

        while (adminSession) {
            System.out.println("\n========================================");
            System.out.println("   ADMIN DASHBOARD - Welcome, " + username);
            System.out.println("========================================");
            System.out.println("1. View Reserved Slots (Today)");
            System.out.println("2. View Statistics");
            System.out.println("2. Logout");
            System.out.print("\nEnter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    showCurrentSlots(scanner);
                    break;
                case "2":
                    showStatisticsMenu(scanner);
                    break;
                case "3":
                    System.out.println("\nLogging out...");
                    adminSession = false;
                    break;
                default:
                    System.out.println("\nInvalid choice! Press Enter to continue.");
                    scanner.nextLine();
                    break;
            }
        }
    }

    public static void showStatisticsMenu(Scanner scanner) {
        boolean statsEntry = true;
        while (statsEntry) {
            System.out.println("\n--- VIEW STATISTICS ---");
            System.out.println("1. Most Used Parking (Required drill-down: Area -> Floor -> Lot)");
            System.out.println("2. Back to Admin Menu");
            System.out.print("\nEnter choice: ");
            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                boolean completedDrillDown = showAdminAreaSelection(scanner);
                if (completedDrillDown) {
                    showAdvancedStatisticsDashboard(scanner);
                }
            } else if (choice.equals("2")) {
                statsEntry = false;
            } else {
                System.out.println("\nInvalid choice! Press Enter to continue.");
                scanner.nextLine();
            }
        }
    }

    public static void showAdvancedStatisticsDashboard(Scanner scanner) {
        boolean dashboardActive = true;
        while (dashboardActive) {
            System.out.println("\n--- DETAILED STATISTICS DASHBOARD ---");
            System.out.println("1. Most Used Parking Summary");
            System.out.println("2. Average Time of Parking");
            System.out.println("3. Peak Hours");
            System.out.println("4. Available Slots");
            System.out.println("5. Occupied Slots");
            System.out.println("6. Other Analytics / Reports");
            System.out.println("7. Back to Main Statistics Menu");
            System.out.print("\nEnter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("\n[Placeholder] Most Used Parking metrics displayed.");
                    System.out.print("Press Enter to continue...");
                    scanner.nextLine();
                    break;
                case "2":
                    System.out.println("\n[Placeholder] Average time of parking displayed.");
                    System.out.print("Press Enter to continue...");
                    scanner.nextLine();
                    break;
                case "3":
                    System.out.println("\n[Placeholder] Peak hours analytics displayed.");
                    System.out.print("Press Enter to continue...");
                    scanner.nextLine();
                    break;
                case "4":
                    System.out.println("\n[Placeholder] Available slots count displayed.");
                    System.out.print("Press Enter to continue...");
                    scanner.nextLine();
                    break;
                case "5":
                    System.out.println("\n[Placeholder] Occupied slots count displayed.");
                    System.out.print("Press Enter to continue...");
                    scanner.nextLine();
                    break;
                case "6":
                    System.out.println("\n[Placeholder] Other supplementary reports displayed.");
                    System.out.print("Press Enter to continue...");
                    scanner.nextLine();
                    break;
                case "7":
                    dashboardActive = false;
                    break;
                default:
                    System.out.println("\nInvalid choice! Press Enter to continue.");
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
        System.out.println("""

            ╷  ╭─╮╭─╴╷╭╮╷   ╭─╮╭─╴╭─╴╭─╮╷ ╷╭╮╷╶┬╴
            │  │ ││╶╮││╰┤   ├─┤│  │  │ ││ ││╰┤ │
            ╰─╴╰─╯╰─╯╵╵ ╵   ╵ ╵╰─╴╰─╴╰─╯╰─╯╵ ╵ ╵

            """);

        System.out.print("Enter username: ");
        String Username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String Password = scanner.nextLine();

        if (Username.isEmpty() || Password.isEmpty()) {
            System.out.println("\nFields cannot be blank! Press Enter to go back...");
            scanner.nextLine();
            return null;
        }

        String usersPath = "Users.csv";
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

                        if (storedUser.equalsIgnoreCase(Username)) {
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
                System.out.println("Error reading user database: " + e.getMessage());
            }
        }

        if (!accountExists) {
            System.out.println("\nNo account yet! Please register first. Press Enter to continue...");
            scanner.nextLine();
            return null;
        } else if (!passwordMatch) {
            System.out.println("\nIncorrect password! Press Enter to continue...");
            scanner.nextLine();
            return null;
        }

        System.out.println("\nLogin Successful! Role: " + foundRole + ". Press Enter to continue. ");
        scanner.nextLine();

        return new String[] { Username, foundRole };
    }

    public static void Register() {
        Scanner scanner = new Scanner(System.in);
        String usersPath = "Users.csv";
        String username = "";
        String password = "";
        String confirmation = "";
        String role = "";
        boolean registration = true;

        while (registration) {
            System.out.println("""

                ╭─╮╭─╴╭─╴╷╭─╮╶┬╴╭─╴╭─╮   ╭─╮╭─╴╭─╴╭─╮╷ ╷╭╮╷╶┬╴
                ├┬╯├╴ │╶╮│╰─╮ │ ├╴ ├┬╯   ├─┤│  │  │ ││ ││╰┤ │
                ╵╰╴╰─╴╰─╯╵╰─╯ ╵ ╰─╴╵╰╴   ╵ ╵╰─╴╰─╴╰─╯╰─╯╵ ╵ ╵

                """);

            boolean validation = true;

            while (validation) {
                System.out.print("Enter username: ");
                username = scanner.nextLine().trim();

                System.out.print("Enter password: ");
                password = scanner.nextLine();

                System.out.print("Confirm password: ");
                confirmation = scanner.nextLine();

                if (username.isEmpty() || password.isEmpty() || confirmation.isEmpty()) {
                    System.out.println("\nInvalid input! Fields cannot be empty.\n");
                }
                else if (!password.equals(confirmation)) {
                    System.out.println("\nError! Passwords do not match.\n");
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
                                if (userDetails.length > 0 && userDetails[0].trim().equalsIgnoreCase(username)) {
                                    usernameExists = true;
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("Error reading user database: " + e.getMessage());
                        }
                    }

                    if (usernameExists) {
                        System.out.println("\nError! Username already taken. Please choose another.\n");
                    } else {
                        validation = false;
                    }
                }
            }

            boolean roleChoice = true;
            while (roleChoice) {
                System.out.println("\n\t\t\t\tROLE");
                System.out.println("\t\t\t\t1. Student");
                System.out.println("\t\t\t\t2. Administrator");
                System.out.print("\nEnter Role: ");
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
                        System.out.println("\nInvalid choice!");
                        System.out.print("Press Enter to try again...");
                        scanner.nextLine();
                        break;
                }
            }

            try (PrintWriter pw = new PrintWriter(new FileWriter(usersPath, true))) {
                pw.println(username + "," + password + "," + role);

                System.out.println("\n" + username + " (" + role + ") successfully registered!");
                System.out.print("Press Enter to continue...");
                scanner.nextLine();

                registration = false;
            } catch (IOException e) {
                System.out.println("\nError saving user credentials: " + e.getMessage());
                return;
            }
        }
    }

    public static void showCurrentSlots(Scanner scanner) {
        java.time.format.DateTimeFormatter dateFmt = java.time.format.DateTimeFormatter
                .ofPattern("M-d-uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT);
        java.time.format.DateTimeFormatter showFmt = java.time.format.DateTimeFormatter.ofPattern("MMM d, h:mm a");

        // ask date
        java.time.LocalDate date;
        while (true) {
            System.out.print("\nDate (mm-dd-yyyy): ");
            try {
                date = java.time.LocalDate.parse(scanner.nextLine().trim(), dateFmt);
                break;
            } catch (java.time.format.DateTimeParseException e) {
                System.out.println("Use MM-dd-yyyy, for example 05-20-2026.");
            }
        }

        // ask time (Enter = whole day)
        java.time.LocalTime time = java.time.LocalTime.MIDNIGHT;
        while (true) {
            System.out.print("Time (HH:mm, 24-hour) or press Enter for the whole day: ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) break;
            try {
                time = java.time.LocalTime.parse(line);
                break;
            } catch (java.time.format.DateTimeParseException e) {
                System.out.println("Use 24-hour HH:mm, for example 14:30.");
            }
        }

        java.time.LocalDateTime from = java.time.LocalDateTime.of(date, time);
        java.time.LocalDateTime to = date.plusDays(1).atStartOfDay(); // end of that day

        java.util.List<Reservation> taken = reservationService.getReservationsBetween(from, to);

        System.out.println("\n--- RESERVED SLOTS from " + from.format(showFmt) + " to end of day ---");
        if (taken.isEmpty()) {
            System.out.println("No reserved slots for that period.");
        }
        String lastBuilding = "";
        for (Reservation r : taken) {
            if (!r.getLocationName().equals(lastBuilding)) {
                System.out.println("\n" + r.getLocationName());
                lastBuilding = r.getLocationName();
            }
            int floor = 0;
            for (ParkingLocation loc : locations) {
                if (loc.getName().equals(r.getLocationName()) && loc.getSlot(r.getSlotId()) != null) {
                    floor = loc.getSlot(r.getSlotId()).getFloor();
                }
            }
            System.out.println("  Slot " + r.getSlotId() + " | Floor " + floor + " | "
                    + r.getStart().format(showFmt) + " - " + r.getEnd().format(showFmt)
                    + " (" + r.getDurationHours() + " hrs)");
        }
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}

