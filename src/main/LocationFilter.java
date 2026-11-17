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
    private final String banner = "=".repeat(100);

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

        System.out.println("\n--- PARKING AREAS BY DISTANCE (nearest to farthest) ---");
        printLocations(sorted, loc -> "Distance from Campus: " + loc.getDistance());
        pickAndReserve(sorted);
    }

    // -------------------------------------------------------------------- RATE

    /** Asks for FIXED or HOURLY, then shows the matching locations with base hours and price. */
    public void filterByRate() {
        while (true) {
            System.out.println("\n--- FILTER BY RATE ---");
            System.out.println("1. Fixed rate");
            System.out.println("2. Hourly rate");
            System.out.println("3. Back to Filter Menu");
            System.out.print("\nEnter choice: ");
            String choice = scanner.nextLine().trim();

            RateType type;
            switch (choice) {
                case "1": type = RateType.FIXED; break;
                case "2": type = RateType.HOURLY; break;
                case "3": return;
                default:
                    System.out.println("Enter a number from 1 to 3.");
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

            System.out.println("\n--- " + type + " RATE PARKING AREAS (cheapest first) ---");
            printLocations(matches, loc -> {
                Rate r = loc.getRate();
                if (r.getType() == RateType.FIXED) {
                    return String.format("Fixed fee: Php %.2f (whole stay)", r.getBaseFee());
                }
                return String.format("Base: Php %.2f for the first %d hour(s)", r.getBaseFee(), r.getBaseHours());
            });
            pickAndReserve(matches);
        }
    }

    /** Prints the numbered list; 'detail' supplies the one line that depends on the filter. */
    private void printLocations(List<ParkingLocation> list,
                                java.util.function.Function<ParkingLocation, String> detail) {
        int n = 0;
        for (ParkingLocation loc : list) {
            System.out.println(banner);
            System.out.println((++n) + ". " + loc.getName());
            System.out.println(detail.apply(loc));
            System.out.println("Total Slot Count: " + loc.getCapacity());
            if (loc.getNotes() != null) System.out.println("Notes: " + loc.getNotes());
        }
        System.out.println(banner);
    }

    /** Lets the user choose one of the listed locations and continue to the reservation steps. */
    private void pickAndReserve(List<ParkingLocation> list) {
        while (true) {
            System.out.print("\nChoose a parking area to reserve (0 to go back): ");
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
            System.out.println("Enter a number from 0 to " + list.size() + ".");
        }
    }
}