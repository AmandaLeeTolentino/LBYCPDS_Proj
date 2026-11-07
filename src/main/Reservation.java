package main;

import java.time.LocalDateTime;

public class Reservation {

    private final String reservationId;
    private final String username;
    private final String slotId;
    private final String locationName;
    private final double durationHours;
    private final double totalAmount;
    private final ReservationStatus status;
    private final LocalDateTime start;

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

    public LocalDateTime getEnd(){
        //add duration of stay to start time after converting hrs to min
        return start.plusMinutes(Math.round(durationHours * 60));
    }



}
