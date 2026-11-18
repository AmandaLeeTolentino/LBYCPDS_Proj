package main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ParkingStatMenu {

    private final String banner = "=".repeat(100);
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("MM-dd, yyyy | h:mm a");
    private ReservationService reservations;
    private List<ParkingLocation> locations;

    public ParkingStatMenu(List<ParkingLocation> locations, ReservationService reservations){
        this.locations = locations;
        this.reservations = reservations;
    }

    public void start(){
        LocalDateTime now = LocalDateTime.now();
        IO.println(banner+ "\n DLSU Parking Status & Guide\n (As of " + now.format(format) + ")\n" + banner);

        //go through each parking location
        for(int i = 0; i < locations.size(); i++){
            ParkingLocation loc = locations.get(i);

            int freeCount = reservations.findFree(loc, now, 1).size();
            int capacity = loc.getCapacity();

            //displaying status for each parking
            IO.println(banner + "\n" + (i + 1) + ". " + loc.getName());
            IO.println(freeCount + " slot/s free (for the next 1hr)");

            int selection = Input.readInt("\nSelect a location for more details (0 to go back): ",0, locations.size());
            if(selection == 0)return;
        }

    }
    //wip

}
