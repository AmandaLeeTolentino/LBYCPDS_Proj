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
    private final String banner = "=".repeat(100);
    private static final double MIN_HOURS = 0.25; //15mins
    private static final double MAX_HOURS = 24;
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
            IO.println("\nNo free slot at " + loc.getName() + " for that time.");
            return;
        }

        ParkingSlot slot = chooseSlot(loc.getSlots(), free, startTime, hours);
        if (slot == null) return;

        double fee = loc.getRate().computeFee(hours);
        if (!isConfirmed(loc, slot, startTime, hours, fee)) {
            IO.println("\nReservation cancelled.");
            return;
        }else{
            double paid = PaymentReceipt.collectPayment(loc.getRate(), hours, fee);
            if (paid <= 0) {
                IO.println("\nPayment cancelled. Reservation not made.");
                return;
            }

            Reservation made = res.reserve(username, slot, loc, startTime, hours);
            if (made == null) {
                IO.println("\nSorry, that slot was just taken. You have not been charged.");
                return;
            }

            ReservationHistoryWriter.save(made);                        // CSV history
            PaymentReceipt.writeReceipt(made, slot, loc.getRate());     // receipt .txt
            IO.println("\nYour parking reservation has been CONFIRMED! Details can be found in the receipt file.");
        }
    }


    private ParkingLocation chooseLocation() {
        int counter = 0;
        int selection;

        //printing parking locations and their info
        for(ParkingLocation location : locations) {
            IO.println(banner + "\n" + (++counter) + ". " + location.getName());
            IO.println("Total Slot Count: "+location.getCapacity());
            IO.println("Distance from Campus: " + location.getDistance());
            if (location.getRate().getType() == RateType.FIXED){
                IO.println("Rate Information: " + location.getRate().getType() + " | Fee: " + location.getRate().getBaseFee());
            }else{
                IO.println("Rate Information: " + location.getRate().getType() + " | Base fee and hours: Php " +
                        location.getRate().getBaseFee() + " for " + location.getRate().getBaseHours() +
                        " hours | Succeeding hr fee: " + location.getRate().getExtraFee());
            }
            if (location.getNotes() != null) {
                IO.println("Notes: " + location.getNotes());
            }
        }

        IO.print("\n\n");
        selection = Input.readInt("Choose Parking Location (0 to go back): ", 0, locations.size());
        if (selection == 0) return null;

        return locations.get(selection - 1);
    }

    /**
     * shows ALL slots of the location; the ones not free for the chosen time are marked [RESERVED]
     * and can't be picked. Slot numbers stay the same (they no longer shift up).
     */
    private ParkingSlot chooseSlot(List<ParkingSlot> allSlots, List<ParkingSlot> freeSlots, LocalDateTime start, double hours){
        Set<String> freeIds = new HashSet<>();
        for (ParkingSlot s : freeSlots) freeIds.add(s.getSlotId());

        IO.println("\nSlots (" + freeSlots.size() + " free):");
        int lastFloor = -1;
        int count = 0;

        for (int i = 0; i < allSlots.size(); i++) {
            ParkingSlot slot = allSlots.get(i);

            if (slot.getFloor() != lastFloor) {//new floor, print a heading
                if (lastFloor != -1 && count % 4 != 0) IO.println();
                IO.println("\nFloor " + slot.getFloor() + ":");
                lastFloor = slot.getFloor();
                count = 0;
            }

            String tag = String.format("%-10s", freeIds.contains(slot.getSlotId()) ? "" : "[RESERVED]");
            if (!freeIds.contains(slot.getSlotId())) tag = Main.Colors.paint(Main.Colors.RED, tag);
            IO.print(String.format("%3d. %-9s %s", i + 1, slot.getSlotId(), tag));
            count++;
            if (count % 4 == 0) IO.println();//4 slots per row
        }
        if (count % 4 != 0) IO.println();

        while (true) {
            int choice = Input.readInt("\nChoose a slot (0 to go back): ", 0, allSlots.size());
            if (choice == 0) return null;

            ParkingSlot picked = allSlots.get(choice - 1);
            if (freeIds.contains(picked.getSlotId())) return picked;

            IO.println(Main.Colors.paint(Main.Colors.RED, "That slot is already reserved for that time:"));
            for (Reservation r : res.getConflicts(picked.getSlotId(), start, hours)) {
                IO.println(Main.Colors.paint(Main.Colors.RED, "  " + picked.getSlotId() + " is reserved from "
                        + r.getStart().format(FORMAT) + " until " + r.getEnd().format(FORMAT)));
            }
            IO.println(Main.Colors.paint(Main.Colors.RED, "Choose another slot."));
        }
    }

    private boolean isConfirmed(ParkingLocation loc, ParkingSlot slot, LocalDateTime start,
                                double hours, double fee){
        LocalDateTime end = start.plusMinutes(Math.round(hours * 60));

        IO.println("\n" + banner);
        System.out.println(Main.Colors.YELLOW + "\nRESERVATION DETAILS:");

        IO.println("\nLocation:\t" + loc.getName());
        IO.println("Slot:\t\t" + slot.getSlotId() + " (Floor " + slot.getFloor() + ")");
        IO.println("\nFrom:\t\t" + start.format(FORMAT));
        IO.println("Until:\t\t" + end.format(FORMAT));
        IO.println("Duration:\t" + hours + " hours");
        IO.println(String.format("\nFee:\t\tPhp %.2f\n"+ Main.Colors.RESET, fee));
        IO.println(banner);

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
            IO.println(Main.Colors.paint(Main.Colors.RED,"That time has already passed. Enter a future date and time."));
        }
    }

    private double askHours() {
        while (true) {
            double hours = Input.readNonNegativeDouble(Main.Colors.paint(Main.Colors.CYAN,"Duration in hours (0 to go back): "));
            if (hours == 0) return 0;
            if (hours >= MIN_HOURS && hours <= MAX_HOURS) {
                return hours;
            }else{
                IO.println(Main.Colors.paint(Main.Colors.RED,"Duration must be from " + MIN_HOURS + " to " + MAX_HOURS + " hours."));
            }
        }
    }
}
