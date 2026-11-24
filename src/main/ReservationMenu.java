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
        String divider = " ⋅".repeat(75);

        IO.println(Main.Colors.paint(Main.Colors.YELLOW,
                "\n╭─ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅─ RESERVE SLOT DIRECTLY ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅ ── ⋅ ⋅─ ─╮"));

        //printing parking locations and their info
        for(ParkingLocation location : locations) {
            IO.println(Main.Colors.paint(Main.Colors.YELLOW, divider));
            IO.println("\t" + (++counter) + ". " + location.getName());
            IO.println("\t\tDistance to Campus:\t" + location.getDistance());
            IO.println("\t\tTotal Slot Count:\t" + location.getCapacity());
            if (location.getRate().getType() == RateType.FIXED){
                IO.println("\t\tRate Type:\t\t\tFIXED");
                IO.println("\t\tFixed Fee:\t\t\tPhp " + location.getRate().getBaseFee());
            }else{
                IO.println("\t\tRate Type:\t\t\tHOURLY");
                IO.println("\t\tBase Fee:\t\t\tPhp " + location.getRate().getBaseFee() + " for " + location.getRate().getBaseHours() + " hours");
                IO.println("\t\tSucceeding Hr Fee:\tPhp " + location.getRate().getExtraFee());
            }
            if (location.getNotes() != null) {
                IO.println("\t\tNotes:\t\t\t\t" + location.getNotes());
            }
        }
        IO.println(Main.Colors.paint(Main.Colors.YELLOW, divider));
        IO.println(Main.Colors.paint(Main.Colors.YELLOW,
                "╰─ ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅   ✩   ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ─╯"));

        selection = Input.readInt(Main.Colors.paint(Main.Colors.CYAN, "\nChoose Parking Location (0 to go back): "), 0, locations.size());
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

    private static final String FOOTER = "╰─ ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅   ✩   ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ─╯";

    private static void printHeader(String title, String subtitle) {
        String pattern = "── ⋅ ⋅ ".repeat(30);
        int side = Math.max(4, (FOOTER.length() - title.length() - 4) / 2);
        String left = "╭─ " + pattern.substring(0, side - 3).replaceAll(" $", "─");
        int rightLength = Math.max(0, FOOTER.length() - left.length() - title.length() - 5);
        String right = pattern.substring(0, rightLength).replaceAll(" $", "─");
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, "\n" + left + " " + title + " " + right + " ─╮"));
        if (subtitle != null) System.out.println(Main.Colors.paint(Main.Colors.CYAN, "\t\t\t\t" + subtitle));
    }

    private static void printFooter() {
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, FOOTER));
    }

    private static void printMenu(String title, String... options) {
        String top = "╔══════════ ≪ " + title + " ≫ ══════════╗";
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, "\n" + top));
        System.out.println();
        for (String option : options) System.out.println("\t\t" + option);
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, "\n╚" + "═".repeat(top.length() - 2) + "╝"));
    }

    /** one reservation as a block of aligned lines (used by cancel and reschedule) */
    private void printReservationBlock(Reservation r, boolean showFee) {
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, banner));
        System.out.println("\t" + r.getReservationId());
        System.out.println(String.format("\t\t%-14s%s", "Location:", r.getLocationName()));
        System.out.println(String.format("\t\t%-14s%s", "Slot:", ParkingSlot.displayId(r.getSlotId())));
        System.out.println(String.format("\t\t%-14s%s", "From:", r.getStart().format(FORMAT)));
        System.out.println(String.format("\t\t%-14s%s", "Until:", r.getEnd().format(FORMAT)));
        if (showFee) System.out.println(String.format("\t\t%-14sPhp %.2f", "Total Fee:", r.getTotalAmount()));
    }

    private static void noActive(String action) {
        System.out.println(Main.Colors.paint(Main.Colors.RED, "\nYou have no active reservations to " + action + "."));
    }

    public void cancelFlow(){
        List<Reservation> userReservations = res.getResUser(username);
        List<Reservation> active = new ArrayList<>(); //active resrvations

        //go through each res. by user and get the active ones only
        for (Reservation r: userReservations) {
            if (r.getStatus() == ReservationStatus.CONFIRMED && r.getStart().isAfter(LocalDateTime.now())) active.add(r);
        }

        if (active.isEmpty()) {
            noActive("cancel");
            return;
        }

        printHeader("CANCEL RESERVATION", "Your active reservations");
        for (Reservation r : active) printReservationBlock(r, true);
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, banner));
        printFooter();

        //get user input the reservation they wanna cancel
        String id = Input.readResId(
                Main.Colors.paint(Main.Colors.CYAN, "\nEnter Reservation ID to cancel (0 to go back): "), active);
        if (id == null) return;

        //find reservation obj by given id
        Reservation target = res.byId(id);

        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, "\n" + banner));
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, "SELECTED RESERVATION:"));
        System.out.println(String.format("\n\t\t%-14s%s", "ID:", target.getReservationId()));
        System.out.println(String.format("\t\t%-14s%s", "Location:", target.getLocationName()));
        System.out.println(String.format("\t\t%-14s%s", "Slot:", ParkingSlot.displayId(target.getSlotId())));
        System.out.println(String.format("\t\t%-14s%s", "From:", target.getStart().format(FORMAT)));
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, banner));

        //if user doesnt want to cancel
        if (!Input.readYesNo(Main.Colors.paint(Main.Colors.CYAN, "\nAre you sure you want to cancel this reservation"))) {
            System.out.println(Main.Colors.paint(Main.Colors.YELLOW, "\nReservation remains."));
            return;
        }

        //continues if user wants to go through with cancellation
        if (res.cancelReservation(target.getReservationId())) {
            System.out.println(Main.Colors.paint(Main.Colors.GREEN,
                    "\nReservation " + target.getReservationId() + " has been CANCELED and saved."));
        }
    }

    public void rescheduleFlow() {
        List<Reservation> userReservations = res.getResUser(username);
        List<Reservation> active = new ArrayList<>();

        //list of all user's active reservations
        for (Reservation r : userReservations) {
            if (r.getStatus() == ReservationStatus.CONFIRMED && r.getStart().isAfter(LocalDateTime.now())) {
                active.add(r);
            }
        }

        if (active.isEmpty()) {
            noActive("reschedule");
            return;
        }

        printHeader("RESCHEDULE RESERVATION", "Your active reservations");
        for (Reservation r : active) printReservationBlock(r, true);
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, banner));
        printFooter();

        String id = Input.readResId(
                Main.Colors.paint(Main.Colors.CYAN, "\nEnter Reservation ID to reschedule (0 to go back): "), active);
        if (id == null) return;

        Reservation target = res.byId(id);
        ParkingLocation loc = findLocationByName(target.getLocationName());
        if (loc == null) {
            System.out.println(Main.Colors.paint(Main.Colors.RED, "\nCould not find location details for this reservation."));
            return;
        }

        ParkingSlot slot = loc.getSlot(target.getSlotId());
        LocalDateTime newStart = target.getStart();
        double newHours = target.getDurationHours();

        while (true) {
            double newFee = loc.getRate().computeFee(newHours);
            LocalDateTime newEnd = newStart.plusMinutes(Math.round(newHours * 60));

            printHeader("RESCHEDULING " + target.getReservationId(), null);
            System.out.println(String.format("\n\t\t%-16s%s", "Location:", loc.getName()));
            System.out.println(String.format("\t\t%-16s%s (Floor %d)", "Slot:", slot.getDisplayId(), slot.getFloor()));
            System.out.println(String.format("\t\t%-16s%s", "New From:", newStart.format(FORMAT)));
            System.out.println(String.format("\t\t%-16s%s", "New Until:", newEnd.format(FORMAT)));
            System.out.println(String.format("\t\t%-16s%s hours", "Duration:", newHours));
            System.out.println(String.format("\t\t%-16sPhp %.2f (Previous: Php %.2f)\n", "Updated Fee:", newFee, target.getTotalAmount()));
            printFooter();

            printMenu("RESCHEDULE OPTIONS",
                    "1. Save Rescheduled Reservation",
                    "2. Change Date & Time",
                    "3. Change Duration",
                    "4. Change Slot",
                    "5. Change Location",
                    "0. Cancel Reschedule");

            int choice = Input.readInt(Main.Colors.paint(Main.Colors.CYAN, "\nEnter choice (0-5): "), 0, 5);
            switch (choice) {
                case 0 ->{
                    System.out.println(Main.Colors.paint(Main.Colors.YELLOW, "\nReschedule canceled. Original reservation unchanged."));
                    return;
                }
                case 1 ->{

                    Reservation updated = res.rescheduleReservation(
                            target.getReservationId(), slot, loc, newStart, newHours);
                    if (updated == null) {
                        System.out.println(Main.Colors.paint(Main.Colors.RED, "\nSlot is no longer available for that time."));
                        continue;
                    }

                    PaymentReceipt.writeReceipt(updated, slot, loc.getRate());
                    System.out.println(Main.Colors.paint(Main.Colors.GREEN,
                            "\nReservation " + updated.getReservationId() + " has been RESCHEDULED and saved!"));
                    return;
                }
                case 2 ->{
                    LocalDateTime pickedStart = askStartTime();
                    if (pickedStart == null) continue;

                    //free and valid slot
                    if (res.isSlotFreeIgnore(slot.getSlotId(), pickedStart, newHours, target.getReservationId())) {
                        newStart = pickedStart;

                    } else {
                        //list of free slots
                        List<ParkingSlot> free = res.findFreeIgnore(loc, pickedStart, newHours, target.getReservationId());

                        //no free found
                        if (free.isEmpty()) {
                            System.out.println(Main.Colors.paint(Main.Colors.RED, "\nNo slots free at that time."));
                            continue;
                        }

                        //it's a valid slot but alrd taken at date and time
                        System.out.println(Main.Colors.paint(Main.Colors.YELLOW,
                                slot.getDisplayId() + " is occupied at the new time. Pick another slot:"));
                        ParkingSlot pickedSlot = chooseSlot(loc.getSlots(), free, pickedStart, newHours);//ask for replacement

                        if (pickedSlot != null) {
                            newStart = pickedStart;
                            slot = pickedSlot;
                        }
                    }
                }
                case 3 ->{

                    double pickedHours = askHours();
                    if (pickedHours == 0) continue;

                    if (res.isSlotFreeIgnore(slot.getSlotId(), newStart, pickedHours, target.getReservationId())) {
                        newHours = pickedHours;
                    } else {
                        List<ParkingSlot> free = res.findFreeIgnore(loc, newStart, pickedHours, target.getReservationId());

                        if (free.isEmpty()) {
                            System.out.println(Main.Colors.paint(Main.Colors.RED, "\nNo slots free for that duration."));
                            continue;
                        }

                        System.out.println(Main.Colors.paint(Main.Colors.YELLOW,
                                slot.getDisplayId() + " conflicts with that duration. Pick another slot:"));
                        ParkingSlot pickedSlot = chooseSlot(loc.getSlots(), free, newStart, pickedHours);

                        if (pickedSlot != null) {
                            newHours = pickedHours;
                            slot = pickedSlot;
                        }
                    }
                }
                case 4 ->{

                    List<ParkingSlot> free = res.findFreeIgnore(loc, newStart, newHours, target.getReservationId());
                    ParkingSlot pickedSlot = chooseSlot(loc.getSlots(), free, newStart, newHours);
                    if (pickedSlot != null) slot = pickedSlot;
                }
                case 5 ->{
                    ParkingLocation pickedLoc = chooseLocation();
                    if (pickedLoc == null) continue;
                    List<ParkingSlot> free = res.findFreeIgnore(pickedLoc, newStart, newHours, target.getReservationId());
                    if (free.isEmpty()) {
                        System.out.println(Main.Colors.paint(Main.Colors.RED, "No slots free at " + pickedLoc.getName() + "."));
                        continue;
                    }
                    ParkingSlot pickedSlot = chooseSlot(pickedLoc.getSlots(), free, newStart, newHours);
                    if (pickedSlot != null) {
                        loc = pickedLoc;
                        slot = pickedSlot;
                    }

                }
            }
        }
    }

    private ParkingLocation findLocationByName(String name) {
        for (ParkingLocation loc : locations) {
            if (loc.getName().equalsIgnoreCase(name)) return loc;
        }
        return null;
    }

}