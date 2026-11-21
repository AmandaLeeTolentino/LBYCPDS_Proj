package main;

import java.util.*;

/**
 * Filter Category feature: narrows down / sorts the parking locations
 * by distance, rate, or promo, then lets the user pick one to reserve.
 */
public class LocationFilter {
    private final List<ParkingLocation> locations;
    private final ReservationService res;
    private final String username;
    private final Scanner scanner;
    private final String banner = " ⋅".repeat(75);

    public LocationFilter(List<ParkingLocation> locations, ReservationService res,
                          String username, Scanner scanner) {
        this.locations = locations;
        this.res = res;
        this.username = username;
        this.scanner = scanner;
    }

    // ---------------------------------------------------------------- DISTANCE

    /** Shows every location from nearest to farthest from the main campus. */
    public void filterByDistance() {
        List<ParkingLocation> sorted = new ArrayList<>(locations);
        sorted.sort(Comparator.comparingDouble(ParkingLocation::getDistance));

        System.out.println(Main.Colors.paint(Main.Colors.YELLOW,"\n╭─ ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── PARKING AREAS BY DISTANCE (nearest to farthest) ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ─╮"));
        printLocations(sorted, loc -> "\t\tDistance to Campus:\t" + loc.getDistance());
        pickAndReserve(sorted);
    }

    // -------------------------------------------------------------------- RATE

    /** Asks for FIXED or HOURLY, then shows the matching locations with base hours and price. */
    public void filterByRate() {
        while (true) {
            System.out.println(Main.Colors.YELLOW +"\n╔═════════ ≪ FILTER BY RATE ≫ ═════════╗" + Main.Colors.RESET);
            System.out.println("\n\t\t1. Fixed rate");
            System.out.println("\t\t2. Hourly rate");
            System.out.println("\t\t3. Back to Filter Menu");
            System.out.println(Main.Colors.paint(Main.Colors.YELLOW,"\n╚══════════════════════════════════════╝"));

            System.out.print(Main.Colors.paint(Main.Colors.CYAN,"\nEnter choice (1/3): "));
            String choice = scanner.nextLine().trim();

            RateType type;
            switch (choice) {
                case "1": type = RateType.FIXED; break;
                case "2": type = RateType.HOURLY; break;
                case "3": return;
                default:
                    System.out.println(Main.Colors.paint(Main.Colors.RED,"\nInvalid input! Enter a number from 1 to 3."));
                    continue;
            }

            List<ParkingLocation> matches = new ArrayList<>();
            for (ParkingLocation loc : locations) {
                if (loc.getRate().getType() == type) matches.add(loc);
            }
            matches.sort(Comparator.comparingDouble(l -> l.getRate().getBaseFee())); // cheapest first

            if (matches.isEmpty()) {
                System.out.println("\nNo parking areas with a " + type + " rate.");
                continue;
            }

            System.out.println(Main.Colors.paint(Main.Colors.YELLOW,"\n╭─ ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ " + type + " RATE PARKING AREAS (cheapest first) ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ─╮"));
            printLocations(matches, loc -> {
                Rate r = loc.getRate();
                if (r.getType() == RateType.FIXED) {
                    return String.format("\t\tFixed fee:\t\t\tPhp %.2f", r.getBaseFee());
                }
                return String.format("\t\tBase fee:\t\t\tPhp %.2f for the first %d hour(s)", r.getBaseFee(), r.getBaseHours());
            });
            pickAndReserve(matches);
        }
    }

    /** Prints the numbered list; 'detail' supplies the one line that depends on the filter. */
    private void printLocations(List<ParkingLocation> list,
                                java.util.function.Function<ParkingLocation, String> detail) {
        int n = 0;
        for (ParkingLocation loc : list) {
            System.out.println(Main.Colors.paint(Main.Colors.YELLOW,banner));
            System.out.println("\t"+(++n) + ". " + loc.getName());
            System.out.println(detail.apply(loc));
            System.out.println("\t\tTotal Slot Count:\t" + loc.getCapacity());
            if (loc.getNotes() != null) System.out.println("\t\tNotes:\t\t\t\t" + loc.getNotes());
        }
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW,banner));
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW,"╰─ ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅   ✩   ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ─╯"));
    }

    /** Lets the user choose one of the listed locations and continue to the reservation steps. */
    private void pickAndReserve(List<ParkingLocation> list) {
        while (true) {
            System.out.print(Main.Colors.paint(Main.Colors.CYAN,"\nChoose a parking area to reserve (0 to go back): "));
            String line = scanner.nextLine().trim();
            try {
                int pick = Integer.parseInt(line);
                if (pick == 0) return;
                if (pick >= 1 && pick <= list.size()) {
                    new ReservationMenu(locations, res, username).reserveAt(list.get(pick - 1));
                    return;
                }
            } catch (NumberFormatException e) {
                // fall through to the message
            }
            System.out.println(Main.Colors.paint(Main.Colors.RED,"\nInvalid input! Enter a number from 0 to " + list.size() + "."));
        }
    }
}