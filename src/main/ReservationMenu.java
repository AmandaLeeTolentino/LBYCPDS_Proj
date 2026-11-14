package main;

import java.util.*;

public class ReservationMenu {
    List<ParkingLocation> locations;
    Scanner input = new Scanner(System.in);
    public static ReservationService res;
    private String username;
    private String banner = "=".repeat(100);

    //todo: run flow order
    public void start() {

    }

    //todo:prints the locations, get user input, return location
    private ParkingLocation chooseLocation() {
        List<ParkingLocation> locations = LocationData.createLocations();
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

        IO.print("\n\nChoose Parking Location: ");
        selection = input.nextInt() - 1;

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

    //test
    public void main(String[] args){
        IO.println(chooseLocation().getName());
    }
}
