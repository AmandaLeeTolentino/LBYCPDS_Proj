package main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ParkingStatMenu {

    private final String banner = "=".repeat(100);
    private final String banner2 = "-".repeat(100);
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("MMM dd, yyyy | h:mm a");
    private ReservationService reservations;
    private List<ParkingLocation> locations;

    public ParkingStatMenu(List<ParkingLocation> locations, ReservationService reservations){
        this.locations = locations;
        this.reservations = reservations;
    }

    public void start(){
        while(true){

            LocalDateTime now = LocalDateTime.now();
            IO.println("\n"+banner+ "\nDLSU Parking Status & Guide\n(As of " + now.format(format) + ")\n" + banner + "\n");

            //go through each parking location
            for(int i = 0; i < locations.size(); i++){
                ParkingLocation loc = locations.get(i);

                int freeCount = reservations.findFree(loc, now, 1).size();
                int capacity = loc.getCapacity();

                //displaying status for each parking
                IO.println((i + 1) + ". " + loc.getName());
                IO.println(freeCount + " slot/s free (for the next 1hr)\n");
                showGuide(loc,now);

            }

            int selection = Input.readInt("\n1 - Refresh; 0 - Previous Page: ",0, 1);
            if(selection == 0)return;
            if(selection == 1)continue;
        }

    }

    private void showGuide(ParkingLocation loc, LocalDateTime now){

        IO.println("Distance from Campus: " + loc.getDistance());
        if (loc.getRate().getType() == RateType.FIXED){
            IO.println("Rate Information: " + loc.getRate().getType() + " | Fee: " + loc.getRate().getBaseFee());
        }else{
            IO.println("Rate Information: " + loc.getRate().getType() + " | Base fee and hours: Php " +
                    loc.getRate().getBaseFee() + " for " + loc.getRate().getBaseHours() +
                    " hours | Succeeding hr fee: " + loc.getRate().getExtraFee());
        }
        if (loc.getNotes() != null) {
            IO.println("Notes: " + loc.getNotes());
        }
        IO.println(banner2);

    }

}
