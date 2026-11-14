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

    public ReservationMenu(List<ParkingLocation> locations,
                           ReservationService res, String username) {
        this.locations = locations;
        this.res = res;
        this.username = username;
    }

    //todo: run flow order
    public void start() {

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
        selection = Input.readInt("Choose Parking Location: ", 1, locations.size()) - 1;

        return locations.get(selection);
    }
/*
    //todo: print free slots, get user input
    private ParkingSlot chooseSlot(List<ParkingSlot> freeSlots){



    }


    //todo: show summary of choices, return if confirmed
    private boolean isConfirmed(){

    }

     */

    private LocalDateTime askStartTime() {
        while (true) {
            LocalDate date = Input.readDate("\nDate (mm-dd-yyyy): ");
            LocalTime time = Input.readTime("Start time (HH:mm, 24-hour): ");
            LocalDateTime start = LocalDateTime.of(date, time);

            if (start.isAfter(LocalDateTime.now())) return start;
            IO.println("That time has already passed. Enter a future date and time.");
        }
    }
}
