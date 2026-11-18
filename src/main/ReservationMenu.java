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

        ParkingSlot slot = chooseSlot(free);
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

    private ParkingSlot chooseSlot(List<ParkingSlot> freeSlots){
        IO.println("\nFree slots:");
        int lastFloor = -1;
        int count = 0;

        for (int i = 0; i < freeSlots.size(); i++) {
            ParkingSlot slot = freeSlots.get(i);

            if (slot.getFloor() != lastFloor) {//new floor, print a heading
                if (lastFloor != -1 && count % 4 != 0) IO.println();
                IO.println("\nFloor " + slot.getFloor() + ":");
                lastFloor = slot.getFloor();
                count = 0;
            }

            IO.print(String.format("%3d. %-12s", i + 1, slot.getSlotId()));
            count++;
            if (count % 4 == 0) IO.println();//4 slots per row
        }
        if (count % 4 != 0) IO.println();

        int choice = Input.readInt("\nChoose a slot (0 to go back): ", 0, freeSlots.size());
        if (choice == 0) return null;
        return freeSlots.get(choice - 1);


    }

    private boolean isConfirmed(ParkingLocation loc, ParkingSlot slot, LocalDateTime start,
                                double hours, double fee){
        LocalDateTime end = start.plusMinutes(Math.round(hours * 60));

        IO.println("\n" + banner);
        IO.println("Location: " + loc.getName());
        IO.println("Slot: " + slot.getSlotId() + " (Floor " + slot.getFloor() + ")");
        IO.println("From: " + start.format(FORMAT));
        IO.println("Until: " + end.format(FORMAT));
        IO.println("Duration: " + hours + " hours");
        IO.println(String.format("Fee: Php %.2f", fee));
        IO.println(banner);

        return Input.readYesNo("Confirm reservation");
    }

    private LocalDateTime askStartTime() {
        while (true) {
            LocalDate date = Input.readDate("\nDate (mm-dd-yyyy, 0 to go back): ");
            if (date == null) return null;
            LocalTime time = Input.readTime("Start time (HH:mm, 24-hour, 0 to go back): ");
            if (time == null) return null;
            LocalDateTime start = LocalDateTime.of(date, time);

            if (start.isAfter(LocalDateTime.now())) return start;
            IO.println("That time has already passed. Enter a future date and time.");
        }
    }

    private double askHours() {
        while (true) {
            double hours = Input.readNonNegativeDouble("Duration in hours (0 to go back): ");
            if (hours == 0) return 0;
            if (hours >= MIN_HOURS && hours <= MAX_HOURS) {
                return hours;
            }else{
                IO.println("Duration must be from " + MIN_HOURS + " to " + MAX_HOURS + " hours.");
            }
        }
    }
}
