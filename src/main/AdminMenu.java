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
    // STATISTICS MENU DASHBOARD
    //==========================================
    public static void showStatisticsMenu(Scanner scanner) {
        boolean statsEntry = true;
        while (statsEntry) {
            printMenu("VIEW STATISTICS",
                    "1. Most Used Parking Summary",
                    "2. Average Parking Hours per Day",
                    "3. Peak Hours",
                    "4. Total Earnings of the Day",
                    "5. Back to Admin Menu");
            System.out.print(Main.Colors.paint(Main.Colors.CYAN, "\nEnter choice: "));
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": showMostUsedMenu(scanner); break;
                case "2": showAverageHours(scanner); break;
                case "3": showPeakHours(scanner); break;
                case "4": showDailyEarnings(scanner); break;
                case "5": statsEntry = false; break;
                default: invalid("Enter a number from 1 to 5.");
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
            printMenu("MOST USED PARKING SUMMARY",
                    "1. Most reserved area",
                    "2. Most reserved floor (multi-floor areas only)",
                    "3. Most reserved slot (all areas)",
                    "4. Back to Statistics");
            System.out.print(Main.Colors.paint(Main.Colors.CYAN, "\nEnter choice: "));
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": {
                    Map<String, Integer> counts = new LinkedHashMap<>();
                    for (ParkingLocation loc : locations) counts.put(loc.getName(), 0);
                    for (Reservation r : all) counts.merge(r.getLocationName(), 1, Integer::sum);
                    printRanking("RESERVATIONS PER AREA", prettyDate(date), counts, "area", Integer.MAX_VALUE);
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
                        printRanking(loc.getName().toUpperCase() + ": RESERVATIONS PER FLOOR", prettyDate(date), counts, "floor", Integer.MAX_VALUE);
                    }
                    break;
                case "3": {
                    Map<String, Integer> counts = new LinkedHashMap<>();
                    for (Reservation r : all) {
                        counts.merge(r.getLocationName() + " - " + ParkingSlot.displayId(r.getSlotId()) + " (Floor " + floorOf(r) + ")",
                                1, Integer::sum);
                    }
                    printRanking("MOST RESERVED SLOTS (ALL AREAS)", prettyDate(date), counts, "slot", 5);
                    break;
                }
                case "4": inMenu = false; break;
                default: invalid("Enter a number from 1 to 4.");
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
        printHeader("AVERAGE PARKING HOURS", prettyDate(date));
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, DIVIDER));
        if (count == 0) {
            System.out.println("\t\tNo reservations on that day.");
        } else {
            System.out.println("\t\tTotal Reservations:\t" + count);
            System.out.println("\t\tTotal Hours:\t\t" + total);
            System.out.println("\t\tAverage Hours:\t\t" + Main.Colors.paint(Main.Colors.GREEN, String.format("%.2f hours per parking", total / count)));
        }
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, DIVIDER));
        printFooter();
        pressEnter(scanner);
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
        printHeader("PEAK HOURS (by reservation start time)", prettyDate(date));
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, DIVIDER));
        if (total == 0) {
            System.out.println("\t\tNo reservations on that day.");
        } else {
            int max = 0;
            for (int c : perHour) max = Math.max(max, c);
            for (int h = 0; h < 24; h++) {
                if (perHour[h] == 0) continue;
                String line = String.format("\t\t%02d:00 - %02d:59\t%d reservation(s)", h, h, perHour[h]);
                if (perHour[h] == max) line += Main.Colors.paint(Main.Colors.GREEN, "   <-- PEAK");
                System.out.println(line);
            }
        }
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, DIVIDER));
        printFooter();
        pressEnter(scanner);
    }

    // ---------- 4. EARNINGS ----------
    private static void showDailyEarnings(Scanner scanner) {
        java.time.LocalDate date = askDate(scanner);
        while (true) {
            List<String> options = new ArrayList<>();
            int n = 0;
            for (ParkingLocation loc : locations) {
                options.add((++n) + ". " + loc.getName());
            }
            options.add((++n) + ". All areas");
            options.add((++n) + ". Back to Statistics");
            printMenu("TOTAL EARNINGS - " + prettyDate(date), options.toArray(new String[0]));
            System.out.print(Main.Colors.paint(Main.Colors.CYAN, "\nEnter choice: "));
            int pick;
            try {
                pick = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                pick = -1;
            }
            if (pick == n) return;
            if (pick < 1 || pick > n) {
                invalid("Enter a number from 1 to " + n + ".");
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
            printHeader("EARNINGS: " + name.toUpperCase(), prettyDate(date));
            System.out.println(Main.Colors.paint(Main.Colors.YELLOW, DIVIDER));
            System.out.println("\t\tReservations:\t\t" + count);
            System.out.println("\t\tTotal Paid:\t\t\t" + Main.Colors.paint(Main.Colors.GREEN, String.format("Php %.2f", sum)));
            System.out.println(Main.Colors.paint(Main.Colors.YELLOW, DIVIDER));
            printFooter();
        }
    }

    // ---------- helpers ----------
    private static java.time.LocalDate askDate(Scanner scanner) {
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter
                .ofPattern("M-d-uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT);
        while (true) {
            System.out.print(Main.Colors.paint(Main.Colors.CYAN, "\nDate (mm-dd-yyyy): "));
            try {
                return java.time.LocalDate.parse(scanner.nextLine().trim(), fmt);
            } catch (java.time.format.DateTimeParseException e) {
                invalid("Use MM-dd-yyyy, for example 05-20-2026.");
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

    /** prints highest first and marks the top one(s); shows at most 'limit' rows */
    private static void printRanking(String title, String subtitle, Map<String, Integer> counts, String what, int limit) {
        List<Map.Entry<String, Integer>> rows = new ArrayList<>(counts.entrySet());
        rows.sort((a, b) -> b.getValue() - a.getValue());

        printHeader(title, subtitle);
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, DIVIDER));
        if (rows.isEmpty() || rows.get(0).getValue() == 0) {
            System.out.println("\t\tNo reservations on that day.");
        } else {
            int max = rows.get(0).getValue();
            for (int i = 0; i < rows.size() && i < limit; i++) {
                Map.Entry<String, Integer> e = rows.get(i);
                String line = String.format("\t%d. %-42s %d reservation(s)", i + 1, e.getKey(), e.getValue());
                if (e.getValue() == max) line += Main.Colors.paint(Main.Colors.GREEN, "   <-- MOST RESERVED " + what.toUpperCase());
                System.out.println(line);
            }
        }
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, DIVIDER));
        printFooter();
    }

    public static void showCurrentSlots(Scanner scanner) {
        java.time.format.DateTimeFormatter dateFmt = java.time.format.DateTimeFormatter
                .ofPattern("M-d-uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT);
        java.time.format.DateTimeFormatter showFmt = java.time.format.DateTimeFormatter.ofPattern("MMM d, h:mm a");

        // ask date
        java.time.LocalDate date;
        while (true) {
            System.out.print(Main.Colors.paint(Main.Colors.CYAN, "\nDate (mm-dd-yyyy): "));
            try {
                date = java.time.LocalDate.parse(scanner.nextLine().trim(), dateFmt);
                break;
            } catch (java.time.format.DateTimeParseException e) {
                invalid("Use MM-dd-yyyy, for example 05-20-2026.");
            }
        }

        // ask time (Enter = whole day)
        java.time.LocalTime time = java.time.LocalTime.MIDNIGHT;
        while (true) {
            System.out.print(Main.Colors.paint(Main.Colors.CYAN, "Time (HH:mm, 24-hour) or press Enter for the whole day: "));
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) break;
            try {
                time = java.time.LocalTime.parse(line);
                break;
            } catch (java.time.format.DateTimeParseException e) {
                invalid("Use 24-hour HH:mm, for example 14:30.");
            }
        }

        java.time.LocalDateTime from = java.time.LocalDateTime.of(date, time);
        java.time.LocalDateTime to = date.plusDays(1).atStartOfDay(); // end of that day

        java.util.List<Reservation> taken = reservationService.getReservationsBetween(from, to);

        printHeader("CURRENT RESERVED SLOTS", "\t\t\t\t\t(from " + from.format(showFmt) + " to end of day)");
        if (taken.isEmpty()) {
            System.out.println(Main.Colors.paint(Main.Colors.YELLOW, DIVIDER));
            System.out.println("\t\tNo reserved slots for that period.");
        }
        String lastBuilding = "";
        for (Reservation r : taken) {
            if (!r.getLocationName().equals(lastBuilding)) {
                System.out.println(Main.Colors.paint(Main.Colors.YELLOW, DIVIDER));
                System.out.println("\t" + r.getLocationName());
                lastBuilding = r.getLocationName();
            }
            int floor = floorOf(r);
            System.out.println(String.format("\t\tSlot %-7s Floor %-3d %s - %s  (%s hrs)",
                    ParkingSlot.displayId(r.getSlotId()), floor,
                    r.getStart().format(showFmt), r.getEnd().format(showFmt), r.getDurationHours()));
        }
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, DIVIDER));
        printFooter();
        pressEnter(scanner);
    }

    private static final String DIVIDER = " ⋅".repeat(75);
    private static final String FOOTER = "╰─ ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅   ✩   ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ─╯";

    /** yellow title bar */
    private static void printHeader(String title, String subtitle) {
        String pattern = "── ⋅ ⋅ ".repeat(30);
        int side = Math.max(4, (FOOTER.length() - title.length() - 4) / 2);   // decoration on each side of the title
        String left = "╭─ " + pattern.substring(0, side - 3).replaceAll(" $", "─");   // don't end on a space
        int rightLength = Math.max(0, FOOTER.length() - left.length() - title.length() - 5);
        String right = pattern.substring(0, rightLength).replaceAll(" $", "─");   // don't end on a space
        String bar = left + " " + title + " " + right + " ─╮";
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, "\n" + bar));
        if (subtitle != null) System.out.println(Main.Colors.paint(Main.Colors.CYAN, "\t\t\t\t" + subtitle));
    }

    private static void printFooter() {
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, FOOTER));
    }

    /** yellow boxed menu with tab-indented options */
    private static void printMenu(String title, String... options) {
        String top = "╔══════════ ≪ " + title + " ≫ ══════════╗";
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, "\n" + top));
        System.out.println();
        for (String option : options) System.out.println("\t\t" + option);
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, "\n╚" + "═".repeat(top.length() - 2) + "╝"));
    }

    private static void invalid(String message) {
        System.out.println(Main.Colors.paint(Main.Colors.RED, "\nInvalid input! " + message));
    }

    private static void pressEnter(Scanner scanner) {
        System.out.print(Main.Colors.paint(Main.Colors.GREEN, "\nPress Enter to continue..."));
        scanner.nextLine();
    }

    private static String prettyDate(java.time.LocalDate date) {
        return "(" + date.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy")) + ")";
    }

}