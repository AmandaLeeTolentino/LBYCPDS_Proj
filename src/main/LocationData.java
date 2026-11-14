package main;

import java.util.ArrayList;
import java.util.List;

public class LocationData {

    public static List<ParkingLocation> createLocations(){

        List<ParkingLocation> locations = new ArrayList<>();

        //egi taft tower
        locations.add(
                new ParkingLocation("EGI Taft Tower", 15,
                new Rate(RateType.HOURLY, 3, 70, 20), "You may be asked to leave your key behind",
                ParkingLocation.generateSlots("EGI Taft Tower", "EGI", new int[]{60}))
        );

        //razon sports complex
        locations.add(
                new ParkingLocation("Razon Sports Complex", 140,new Rate(RateType.FIXED, 0,60,0),
                "You have to buy a ticket in advanced and present receipt to DLSU sec. office first",
                ParkingLocation.generateSlots("Razon Sports Complex", "RAZ", new int[]{80,80, 82}))
        );

        //green court
        locations.add(
                new ParkingLocation("Green Court Parking", 260, new Rate (RateType.HOURLY,3,50,20),
                null,ParkingLocation.generateSlots("Green Court Parking", "GC",  new int[]{48}))

        );

        //university mall
        locations.add(
                new ParkingLocation("University Mall", 70, new Rate(RateType.HOURLY,2,50,20),
                null, ParkingLocation.generateSlots("University Mall", "UM", new int[]{20}))
        );

        //green mall
        locations.add(
                new ParkingLocation("Green Mall", 126, new Rate(RateType.HOURLY, 2,60,50),
                null, ParkingLocation.generateSlots("Green Mall", "GM", new int[]{20}))
        );

        return locations;
    }


}
