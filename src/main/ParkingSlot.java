package main;

public class ParkingSlot {
    private final String slotId;
    private final String location;
    private final int floor;


    public ParkingSlot(String slotId, String location, int floor) {
       if (slotId == null || slotId.isEmpty() || location == null || location == null) {
           throw new IllegalArgumentException("Invalid slot vlaues");
       }

        this.slotId = slotId;
        this.location = location;
        this.floor = floor;
    }

    public String getSlotId() {
        return slotId;
    }
    public String getLocation() {
        return location;
    }
    public int getFloor() {
        return floor;
    }
}
