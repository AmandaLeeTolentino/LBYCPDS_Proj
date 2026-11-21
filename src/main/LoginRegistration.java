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
                            showAdminMenu(username);
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
            System.out.println("\t\t3. Print Receipt");
            System.out.println("\t\t4. Transaction History");
            System.out.println("\t\t5. Logout");
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
                    // 24-hour receipt logic simulation
                    boolean hasRecentTransaction = false; // Change to true if within 24 hours
                    if (!hasRecentTransaction) {
                        System.out.println("\nNo recent transaction to print receipt.");
                    } else {
                        System.out.println("\n[Placeholder] Printing receipt for your recent transaction...");
                    }
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                    break;
                case "4":
                    showTransactionHistory(scanner, username);
                    System.out.println(Main.Colors.paint(Main.Colors.GREEN,"\nPress Enter to continue..."));
                    scanner.nextLine();
                    break;
                case "5":
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
    // ADMIN MENU & SUB-MENUS
    // ==========================================
    public static void showAdminMenu(String username) {
        Scanner scanner = new Scanner(System.in);
        boolean adminSession = true;

        while (adminSession) {
            System.out.println(Main.Colors.YELLOW +"\n╔══════════ ≪ ADMIN DASHBOARD ≫ ══════════╗");
            System.out.println("\n  ⋅  ⋅  ⋅  ⋅  Welcome, " + username +"! ⋅  ⋅  ⋅  ⋅"+ Main.Colors.RESET);
            System.out.println("\n\t\t1. View Current Reserved Slots");
            System.out.println("\t\t2. View Statistics");
            System.out.println("\t\t3. Logout\n" );
            System.out.println(Main.Colors.paint(Main.Colors.YELLOW,"╚════════════════════════════════════════╝"));

            System.out.print(Main.Colors.paint(Main.Colors.CYAN,"\nEnter choice: "));
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    showCurrentSlots(scanner);
                    break;
                case "2":
                    showStatisticsMenu(scanner);
                    break;
                case "3":
                    System.out.println(Main.Colors.paint(Main.Colors.GREEN,"\nLogging out..."));
                    adminSession = false;
                    break;
                default:
                    System.out.println(Main.Colors.paint(Main.Colors.RED,"\nInvalid choice! Press Enter to try again...\n"));
                    scanner.nextLine();
                    break;
            }
        }
    }

     //==========================================
     //NEW STATISTICS MENU DASHBOARD
     //==========================================
    public static void showStatisticsMenu(Scanner scanner) {
        boolean statsEntry = true;
        while (statsEntry) {
            System.out.println("\n--- VIEW STATISTICS ---");
            System.out.println("1. Most Used Parking Summary");
            System.out.println("2. Average Parking Hours per Day");
            System.out.println("3. Peak Hours");
            System.out.println("4. Total Earnings of the Day");
            System.out.println("5. Back to Admin Menu");
            System.out.print("\nEnter choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": showMostUsedMenu(scanner); break;
                case "2": showAverageHours(scanner); break;
                case "3": showPeakHours(scanner); break;
                case "4": showDailyEarnings(scanner); break;
                case "5": statsEntry = false; break;
                default: System.out.println("Enter a number from 1 to 5.");
            }
        }
    }

    // ---------- 1. MOST USED  ----------
    private static void showMostUsedMenu(Scanner scanner) {
        java.time.LocalDate date = askDate(scanner);

        // load the CSV and keep only this date's non-canceled reservations
        List<Reservation> all = new ArrayList<>();
        for (Reservation r : ReservationHistoryWriter.loadCsv()) {
            if (r.getStatus() != ReservationStatus.CANCELED && r.getStart().toLocalDate().equals(date)) {
                all.add(r);
            }
        }

        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--- MOST USED PARKING SUMMARY (" + date + ") ---");
            System.out.println("1. Most reserved area");
            System.out.println("2. Most reserved floor (multi-floor areas only)");
            System.out.println("3. Most reserved slot (all areas)");
            System.out.println("4. Back to Statistics");
            System.out.print("\nEnter choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": {
                    Map<String, Integer> counts = new LinkedHashMap<>();
                    for (ParkingLocation loc : locations) counts.put(loc.getName(), 0);
                    for (Reservation r : all) counts.merge(r.getLocationName(), 1, Integer::sum);
                    System.out.println("\n--- RESERVATIONS PER AREA ---");
                    printRanking(counts, "area");
                    break;
                }
                case "2":
                    for (ParkingLocation loc : locations) {
                        if (floorCount(loc) < 2) continue;
                        Map<String, Integer> counts = new LinkedHashMap<>();
                        for (ParkingSlot s : loc.getSlots()) counts.putIfAbsent("Floor " + s.getFloor(), 0);
                        for (Reservation r : all) {
                            if (r.getLocationName().equals(loc.getName())) {
                                counts.merge("Floor " + floorOf(r), 1, Integer::sum);
                            }
                        }
                        System.out.println("\n--- " + loc.getName().toUpperCase() + ": RESERVATIONS PER FLOOR ---");
                        printRanking(counts, "floor");
                    }
                    break;
                case "3": {
                    Map<String, Integer> counts = new LinkedHashMap<>();
                    for (Reservation r : all) {
                        counts.merge(r.getLocationName() + " - " + r.getSlotId() + " (Floor " + floorOf(r) + ")",
                                1, Integer::sum);
                    }
                    System.out.println("\n--- MOST RESERVED SLOTS (ALL AREAS) ---");
                    printRanking(counts, "slot", 5);
                    break;
                }
                case "4": inMenu = false; break;
                default: System.out.println("Enter a number from 1 to 4.");
            }
        }
    }

    // ---------- 2. AVERAGE HOURS ----------
    private static void showAverageHours(Scanner scanner) {
        java.time.LocalDate date = askDate(scanner);
        double total = 0;
        int count = 0;
        for (Reservation r : reservationService.getAllReservations()) {
            if (r.getStart().toLocalDate().equals(date)) {
                total += r.getDurationHours();
                count++;
            }
        }
        System.out.println("\n--- AVERAGE PARKING HOURS on " + date + " ---");
        if (count == 0) {
            System.out.println("No reservations on that day.");
        } else {
            System.out.println("Total reservations: " + count);
            System.out.println("Total hours: " + total);
            System.out.println(String.format("Average hours per parking: %.2f", total / count));
        }
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    // ---------- 3. PEAK HOURS ----------
    private static void showPeakHours(Scanner scanner) {
        java.time.LocalDate date = askDate(scanner);
        int[] perHour = new int[24]; // start-time hour -> number of reservations
        int total = 0;
        for (Reservation r : reservationService.getAllReservations()) {
            if (!r.getStart().toLocalDate().equals(date)) continue;
            perHour[r.getStart().getHour()]++;
            total++;
        }
        System.out.println("\n--- PEAK HOURS on " + date + " (by reservation start time) ---");
        if (total == 0) {
            System.out.println("No reservations on that day.");
        } else {
            int max = 0;
            for (int c : perHour) max = Math.max(max, c);
            for (int h = 0; h < 24; h++) {
                if (perHour[h] == 0) continue;
                System.out.println(String.format("%02d:00 - %02d:59 | %d reservation(s)%s",
                        h, h, perHour[h], perHour[h] == max ? "   <-- PEAK" : ""));
            }
        }
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    // ---------- 4. EARNINGS ----------
    private static void showDailyEarnings(Scanner scanner) {
        java.time.LocalDate date = askDate(scanner);
        while (true) {
            System.out.println("\n--- TOTAL EARNINGS on " + date + " ---");
            int n = 0;
            for (ParkingLocation loc : locations) {
                System.out.println((++n) + ". " + loc.getName());
            }
            System.out.println((++n) + ". All areas");
            System.out.println((++n) + ". Back to Statistics");
            System.out.print("\nEnter choice: ");
            int pick;
            try {
                pick = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                pick = -1;
            }
            if (pick == n) return;
            if (pick < 1 || pick > n) {
                System.out.println("Enter a number from 1 to " + n + ".");
                continue;
            }
            boolean allAreas = pick == n - 1;
            String name = allAreas ? "All areas" : locations.get(pick - 1).getName();

            double sum = 0;
            int count = 0;
            for (Reservation r : reservationService.getAllReservations()) {
                if (!r.getStart().toLocalDate().equals(date)) continue;
                if (allAreas || r.getLocationName().equals(name)) {
                    sum += r.getTotalAmount();
                    count++;
                }
            }
            System.out.println("\n" + name + " | " + count + " reservation(s)");
            System.out.println(String.format("Total amount paid: Php %.2f", sum));
        }
    }

    // ---------- helpers ----------
    private static java.time.LocalDate askDate(Scanner scanner) {
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter
                .ofPattern("M-d-uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT);
        while (true) {
            System.out.print("\nDate (mm-dd-yyyy): ");
            try {
                return java.time.LocalDate.parse(scanner.nextLine().trim(), fmt);
            } catch (java.time.format.DateTimeParseException e) {
                System.out.println("Use MM-dd-yyyy, for example 05-20-2026.");
            }
        }
    }

    private static int floorCount(ParkingLocation loc) {
        Set<Integer> floors = new HashSet<>();
        for (ParkingSlot s : loc.getSlots()) floors.add(s.getFloor());
        return floors.size();
    }

    private static int floorOf(Reservation r) {
        for (ParkingLocation loc : locations) {
            if (loc.getName().equals(r.getLocationName()) && loc.getSlot(r.getSlotId()) != null) {
                return loc.getSlot(r.getSlotId()).getFloor();
            }
        }
        return 0;
    }

    private static void printRanking(Map<String, Integer> counts, String what) {
        printRanking(counts, what, Integer.MAX_VALUE);
    }

    /** prints highest first and marks the top one(s); shows at most 'limit' rows */
    private static void printRanking(Map<String, Integer> counts, String what, int limit) {
        List<Map.Entry<String, Integer>> rows = new ArrayList<>(counts.entrySet());
        rows.sort((a, b) -> b.getValue() - a.getValue());
        if (rows.isEmpty() || rows.get(0).getValue() == 0) {
            System.out.println("No reservations yet.");
            return;
        }
        int max = rows.get(0).getValue();
        for (int i = 0; i < rows.size() && i < limit; i++) {
            Map.Entry<String, Integer> e = rows.get(i);
            System.out.println(e.getKey() + " | " + e.getValue() + " reservation(s)"
                    + (e.getValue() == max ? "   <-- MOST RESERVED " + what.toUpperCase() : ""));
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

