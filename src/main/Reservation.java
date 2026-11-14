package main;

import java.time.LocalDateTime;

public class Reservation {

    private final String reservationId;
    private final String username;
    private final String slotId;
    private final String locationName;
    private final double durationHours;
    private final double totalAmount;
    private ReservationStatus status;
    private final LocalDateTime start;
    private final LocalDateTime end;

    //constructor
    public Reservation(String reservationId, String username, String slotId,
                       String locationName, ReservationStatus status, double
                       durationHours, double totalAmount, LocalDateTime start) {

        this.reservationId = reservationId;
        this.username = username;
        this.slotId = slotId;
        this.locationName = locationName;
        this.durationHours = durationHours;
        this.totalAmount = totalAmount;
        this.status = status;
        this.start = start;
        this.end = start.plusMinutes(Math.round(durationHours * 60));
    }

    //getters
    public String getReservationId() {
        return reservationId;
    }
    public String getUsername() {
        return username;
    }
    public String getSlotId() {
        return slotId;
    }
    public String getLocationName() {
        return locationName;
    }
    public double getDurationHours() {
        return durationHours;
    }
    public double getTotalAmount() {
        return totalAmount;
    }
    public ReservationStatus getStatus() {
        return status;
    }
    public LocalDateTime getStart() {
        return start;
    }
    public LocalDateTime getEnd() {
        return end;
    }

    /**
     * tool to check for overlap so no invalid reservation
     * @param st start time of new reservation
     * @param et end time of new reservation
     * @return true if overlap, false if no overlap
     */
    public boolean overlaps(LocalDateTime st, LocalDateTime et){
        return start.isBefore(et) && st.isBefore(end);
    }

    public void cancel(){
        status = ReservationStatus.CANCELED;
    }
}
