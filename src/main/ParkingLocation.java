package main;

import java.util.*;

public class ParkingLocation {

    private final String name;
    private final double distance;
    private final Rate rate;
    private final String notes;
    private final List<ParkingSlot> slots;
    private final Map<String, ParkingSlot> slotById = new HashMap<>();

    //constructor
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
}
