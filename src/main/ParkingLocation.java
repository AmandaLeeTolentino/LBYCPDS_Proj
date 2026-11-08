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
    public ParkingLocation(String name, double distance, Rate rate, String notes) {
        if (name == null || distance < 0 || name.isBlank() || rate == null) {
            throw new IllegalArgumentException("Invalid location values");
        }

        this.name = name;
        this.distance = distance;
        this.rate = rate;
        this.notes = notes;
        this.slots = new ArrayList<>();

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

    /**
     *
     * @param name parking area name
     * @param slotsPerFlr each index represents a floor, value in index is amnt of slots (ex: [3,2] 3 slots in F1, 2 slots in F2)
     * @return create slots in parking area
     */
    public static List<ParkingSlot> generateSlots(String name, int[] slotsPerFlr){
        List<ParkingSlot> slots = new ArrayList<>();
        for (int i = 0; i < slotsPerFlr.length; i++ ) {

        }
    }

    //test
    public static void main(String[] args){
        Rate egiRate = new Rate(RateType.HOURLY, 3,70,20);
        ParkingLocation egi = new ParkingLocation("EGI Taft Tower", 45,
                egiRate, "You may be asked to leave your keys");

        IO.println(egi.getName());
        IO.println(egiRate.computeFee(6));
    }

}
