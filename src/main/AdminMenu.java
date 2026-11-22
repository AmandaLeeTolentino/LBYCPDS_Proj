package main;

import java.util.*;
import java.io.*;

/**
 * Admin side of the program: the admin dashboard, the reserved-slots view, and the statistics menus.
 */
public class AdminMenu {
    private static final List<ParkingLocation> locations = LoginRegistration.locations;
    private static final ReservationService reservationService = LoginRegistration.reservationService;

    // ==========================================
    // ADMIN MENU & SUB-MENUS
    // ==========================================
    public static void showAdminMenu(String username) {
        Scanner scanner = new Scanner(System.in);
        boolean adminSession = true;
        String name = LoginRegistration.getDisplayName(username);

        while (adminSession) {
            System.out.println(Main.Colors.YELLOW +"\n╔══════════ ≪ ADMIN DASHBOARD ≫ ══════════╗");
            System.out.println("\n  ⋅  ⋅  ⋅  ⋅  Welcome, " + name +"! ⋅  ⋅  ⋅  ⋅"+ Main.Colors.RESET);
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
                        counts.merge(r.getLocationName() + " - " + ParkingSlot.displayId(r.getSlotId()) + " (Floor " + floorOf(r) + ")",
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
            System.out.println("  Slot " + ParkingSlot.displayId(r.getSlotId()) + " | Floor " + floor + " | "
                    + r.getStart().format(showFmt) + " - " + r.getEnd().format(showFmt)
                    + " (" + r.getDurationHours() + " hrs)");
        }
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}