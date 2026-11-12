package main;

import java.util.*;

public class ParkingLocation {

    private final String name;
    private final double distance;
    private final Rate rate;
    private final String notes;
    private final List<ParkingSlot> slots;
    private final Map<String, ParkingSlot> slotById = new HashMap<>();

    //constructr
    public ParkingLocation(String name, double distance, Rate rate, String notes,
                           List<ParkingSlot> slots) {
        if (name == null || distance < 0 || name.isBlank() || rate == null||slots == null) {
            throw new IllegalArgumentException("Invalid location values");
        }

        this.name = name;
        this.distance = distance;
        this.rate = rate;
        this.notes = notes;
        this.slots = new ArrayList<>(slots);

        //add the id and slot obj to the hashmap
        for(ParkingSlot slot: this.slots) {
            slotById.put(slot.getSlotId(), slot);
        }
    }

    //getters
    public int getCapacity() {
        return slots.size();
    }
    public ParkingSlot getSlot(String slotId) {
        return slotById.get(slotId);
    }
    public String getName() {
        return name;
    }
    public double getDistance() {
        return distance;
    }
    public Rate getRate() {
        return rate;
    }
    public String getNotes() {
        return notes;
    }
    public List<ParkingSlot> getSlots() {
        return new ArrayList<>(slots); //send copy of it only so cant accidentally change
    }

    /**
     *
     * @param name parking area name
     * @param code code for specific location to avoid duplicate ids accross locations
     * @param slotsPerFlr each index represents a floor, value in index is amnt of slots (ex: [3,2] 3 slots in F1, 2 slots in F2)
     * @return create slots in parking area
     */
    public static List<ParkingSlot> generateSlots(String name,String code, int[] slotsPerFlr){
        List<ParkingSlot> slots = new ArrayList<>();

        for (int f = 0; f < slotsPerFlr.length; f++ ) {//for each floor

            for(int i = 1; i <= slotsPerFlr[f]; i++){//each slot
                //creating id (we can change it or add if u guys want)
                String floor = String.valueOf(f + 1);
                String slotNo = String.valueOf(i);

                String id = code + "-F" +floor + "-" + slotNo;
                slots.add(new ParkingSlot(id, name, f + 1)); //add new slot obj to arraylist
            }

        }

        return slots;
    }

    //test
    public static void main(String[] args){
        Rate egiRate = new Rate(RateType.HOURLY, 3,70,20);
        List<ParkingSlot> egiSlots = ParkingLocation.generateSlots("EGI Taft Tower", "EGI", new int[]{10, 4, 3});

        ParkingLocation egi = new ParkingLocation("EGI Taft Tower", 45.0, egiRate,"You may be asked to leave your keys", egiSlots);


        IO.println(egi.getName());
        IO.println("Capacity: " + egi.getCapacity());

        IO.println(egi.getSlot("EGI-F1-5").getFloor());
        IO.println(egiRate.computeFee(6));

        Rate greenRate = new Rate(RateType.HOURLY, 3,60,20);
        List<ParkingSlot> greenSlots  = ParkingLocation.generateSlots("Green Mall", "Green", new int[]{3});
        ParkingLocation greenMall = new ParkingLocation("Green Mall", 50, greenRate,null, greenSlots);

        IO.println();
        IO.println(greenMall.getName());
        IO.println("Capacity: " + greenMall.getCapacity());

        IO.println("\nSlots: ");
        for (ParkingSlot slot: greenSlots) {
            IO.println(slot.getSlotId());
        }
        IO.println(greenRate.computeFee(10));
    }

}
