package main;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReservationMenu {
    private final List<ParkingLocation> locations;
    private ReservationService res;
    private final String username;
    private final String banner = " ⋅".repeat(75);
    private static final double MIN_HOURS = 0.25; //15mins
    private static final double MAX_HOURS = 24;
    private static final int GRID_COLUMNS = 4;   //slots per row in the slot grid
    private static final int CELL_WIDTH = 23;    //width of one cell (number + slot id + [RESERVED] tag)
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    public ReservationMenu(List<ParkingLocation> locations,
                           ReservationService res, String username) {
        this.locations = locations;
        this.res = res;
        this.username = username;
    }

    public void start() {
        ParkingLocation loc = chooseLocation();
        reserveAt(loc);
    }

    /** Runs the reservation steps for an already-chosen location (used by the filters). */
    public void reserveAt(ParkingLocation loc) {
        if (loc == null) {
            return;
        }

        LocalDateTime startTime = askStartTime();
        if (startTime == null) return;      // user backed out

        double hours = askHours();
        if (hours == 0) return;             // user backed out

        List<ParkingSlot> free = res.findFree(loc, startTime, hours);
        if (free.isEmpty()) {
            System.out.println("\nNo free slot at " + loc.getName() + " for that time.");
            return;
        }

        ParkingSlot slot = chooseSlot(loc.getSlots(), free, startTime, hours);
        if (slot == null) return;

        double fee = loc.getRate().computeFee(hours);
        if (!isConfirmed(loc, slot, startTime, hours, fee)) {
            System.out.println("\nReservation cancelled.");
            return;
        }else{
            double paid = PaymentReceipt.collectPayment(loc.getRate(), hours, fee);
            if (paid <= 0) {
                System.out.println("\nPayment cancelled. Reservation not made.");
                return;
            }

            Reservation made = res.reserve(username, slot, loc, startTime, hours);
            if (made == null) {
                System.out.println("\nSorry, that slot was just taken. You have not been charged.");
                return;
            }

            ReservationHistoryWriter.save(made);                        // CSV history
            PaymentReceipt.writeReceipt(made, slot, loc.getRate());     // receipt .txt
            System.out.println("\nYour parking reservation has been CONFIRMED! Details can be found in the receipt file.");
        }
    }


    private ParkingLocation chooseLocation() {
        int counter = 0;
        int selection;

        //printing parking locations and their info
        for(ParkingLocation location : locations) {
            System.out.println(Main.Colors.paint(Main.Colors.YELLOW,banner));
            System.out.println("\n" + (++counter) + ". " + location.getName());
            System.out.println("Total Slot Count: "+location.getCapacity());
            System.out.println("Distance from Campus: " + location.getDistance());
            if (location.getRate().getType() == RateType.FIXED){
                System.out.println("Rate Information: " + location.getRate().getType() + " | Fee: " + location.getRate().getBaseFee());
            }else{
                System.out.println("Rate Information: " + location.getRate().getType() + " | Base fee and hours: Php " +
                        location.getRate().getBaseFee() + " for " + location.getRate().getBaseHours() +
                        " hours | Succeeding hr fee: " + location.getRate().getExtraFee());
            }
            if (location.getNotes() != null) {
                System.out.println("Notes: " + location.getNotes());
            }
        }

        System.out.print("\n\n");
        selection = Input.readInt("Choose Parking Location (0 to go back): ", 0, locations.size());
        if (selection == 0) return null;

        return locations.get(selection - 1);
    }

    /**
     * shows ALL slots of the location in a grid per floor; the ones not free for the chosen time
     * are marked [RESERVED] and can't be picked. Slot numbers stay the same.
     */
    private ParkingSlot chooseSlot(List<ParkingSlot> allSlots, List<ParkingSlot> freeSlots,
                                   LocalDateTime start, double hours){
        Set<String> freeIds = new HashSet<>();
        for (ParkingSlot s : freeSlots) freeIds.add(s.getSlotId());

        System.out.println("\nSlots (" + freeSlots.size() + " free):");

        String line = "+" + ("-".repeat(CELL_WIDTH + 2) + "+").repeat(GRID_COLUMNS); // horizontal grid line
        int i = 0;

        while (i < allSlots.size()) {
            int floor = allSlots.get(i).getFloor();
            System.out.println("\nFloor " + floor + ":");
            System.out.println(line);

            StringBuilder row = new StringBuilder("|");
            int col = 0;

            while (i < allSlots.size() && allSlots.get(i).getFloor() == floor) {
                ParkingSlot slot = allSlots.get(i);
                boolean isFree = freeIds.contains(slot.getSlotId());

                String tag = String.format("%-10s", isFree ? "" : "[RESERVED]");
                if (!isFree) tag = Main.Colors.paint(Main.Colors.RED, tag);

                row.append(String.format(" %3d. %-7s %s |", i + 1, slot.getDisplayId(), tag));                col++;
                i++;

                if (col == GRID_COLUMNS) {//row is full: print it with a line under it
                    System.out.println(row);
                    System.out.println(line);
                    row = new StringBuilder("|");
                    col = 0;
                }
            }

            if (col != 0) {//last row of the floor isn't full: fill with empty cells so the grid stays aligned
                while (col < GRID_COLUMNS) {
                    row.append(" ".repeat(CELL_WIDTH + 2)).append("|");
                    col++;
                }
                System.out.println(row);
                System.out.println(line);
            }
        }

        while (true) {
            int choice = Input.readInt("\nChoose a slot (0 to go back): ", 0, allSlots.size());
            if (choice == 0) return null;

            ParkingSlot picked = allSlots.get(choice - 1);
            if (freeIds.contains(picked.getSlotId())) return picked;

            System.out.println(Main.Colors.paint(Main.Colors.RED, "That slot is already reserved for that time:"));
            for (Reservation r : res.getConflicts(picked.getSlotId(), start, hours)) {
                System.out.println(Main.Colors.paint(Main.Colors.RED, "  " + picked.getDisplayId() + " is reserved from "
                        + r.getStart().format(FORMAT) + " until " + r.getEnd().format(FORMAT)));
            }
            System.out.println(Main.Colors.paint(Main.Colors.RED, "Choose another slot."));
        }
    }

    private boolean isConfirmed(ParkingLocation loc, ParkingSlot slot, LocalDateTime start,
                                double hours, double fee){
        LocalDateTime end = start.plusMinutes(Math.round(hours * 60));

        System.out.println(Main.Colors.paint(Main.Colors.YELLOW,"\n" + banner));
        System.out.println(Main.Colors.YELLOW + "\nRESERVATION DETAILS:");

        System.out.println("\nLocation:\t" + loc.getName());
        System.out.println("Slot:\t\t" + slot.getDisplayId() + " (Floor " + slot.getFloor() + ")");
        System.out.println("\nFrom:\t\t" + start.format(FORMAT));
        System.out.println("Until:\t\t" + end.format(FORMAT));
        System.out.println("Duration:\t" + hours + " hours");
        System.out.println(String.format("\nFee:\t\tPhp %.2f\n"+ Main.Colors.RESET, fee));
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW,banner));

        return Input.readYesNo(Main.Colors.paint(Main.Colors.CYAN,"\nConfirm reservation"));
    }

    private LocalDateTime askStartTime() {
        while (true) {
            LocalDate date = Input.readDate(Main.Colors.paint(Main.Colors.CYAN,"\nDate (mm-dd-yyyy, 0 to go back): "));
            if (date == null) return null;
            LocalTime time = Input.readTime(Main.Colors.paint(Main.Colors.CYAN,"Start time (HH:mm, 24-hour, 0 to go back): "));
            if (time == null) return null;
            LocalDateTime start = LocalDateTime.of(date, time);

            if (start.isAfter(LocalDateTime.now())) return start;
            System.out.println(Main.Colors.paint(Main.Colors.RED,"That time has already passed. Enter a future date and time."));
        }
    }

    private double askHours() {
        while (true) {
            double hours = Input.readNonNegativeDouble(Main.Colors.paint(Main.Colors.CYAN,"Duration in hours (0 to go back): "));
            if (hours == 0) return 0;
            if (hours >= MIN_HOURS && hours <= MAX_HOURS) {
                return hours;
            }else{
                System.out.println(Main.Colors.paint(Main.Colors.RED,"Duration must be from " + MIN_HOURS + " to " + MAX_HOURS + " hours."));
            }
        }
    }

    public void cancelFlow(){
        List<Reservation> userReservations = res.getResUser(username);
        List<Reservation> active = new ArrayList<>(); //active resrvations

        //go through each res. by user and get the active ones only
        for (Reservation r: userReservations) {
            if (r.getStatus() == ReservationStatus.CONFIRMED && r.getStart().isAfter(LocalDateTime.now())) active.add(r);
        }

        if (active.isEmpty()) {
            System.out.println(Main.Colors.paint(Main.Colors.YELLOW, "\nYou have no active reservations to cancel."));
            return;
        }

        System.out.println("\n" + banner);
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, "YOUR ACTIVE RESERVATIONS:"));
        for (Reservation r : active) {
            System.out.println(String.format("  %-5s | %-22s | Slot: %-9s | %s - %s",
                    r.getReservationId(),
                    r.getLocationName(),
                    r.getSlotId(),
                    r.getStart().format(FORMAT),
                    r.getEnd().format(DateTimeFormatter.ofPattern("h:mm a"))));
        }
        System.out.println(banner);

        //get user input the reservation they wanna cancel
        String id = Input.readResId(
                Main.Colors.paint(Main.Colors.CYAN, "\nEnter Reservation ID to cancel (0 to go back): "), active);


        //find reservation obj by given id
        Reservation target = res.byId(id);

        System.out.println("\nSelected: " + target.getReservationId() + " at " + target.getLocationName()
                + " (" + target.getSlotId() + ") on " + target.getStart().format(FORMAT));

        //if user doesnt want to cancel
        if (!Input.readYesNo(Main.Colors.paint(Main.Colors.CYAN, "Are you sure you want to cancel this reservation"))) {
            System.out.println(Main.Colors.paint(Main.Colors.YELLOW, "Reservation remains"));
            return;
        }

        //continues if user wants to go through with cancellation
        if (res.cancelReservation(target.getReservationId())) {
            System.out.println(Main.Colors.paint(Main.Colors.GREEN,
                    "\nReservation " + target.getReservationId() + " has been CANCELED and saved."));
        }
    }

}
