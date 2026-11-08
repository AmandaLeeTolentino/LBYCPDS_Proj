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

    //test
    public static void main(String[] args){
        //make reservation for 2pm 3 hrs
        LocalDateTime twoPm = LocalDateTime.of(2026, 11, 10, 14, 0); //time of reservation
        Reservation r = new Reservation("R1", "mandy", "EGI-F1-5", "EGI Taft Tower",
                ReservationStatus.CONFIRMED, 3, 110, twoPm);//details of reservation
        IO.println("Reservation for " + r.username + " at " + r.getStart() + " up to " + r.getEnd());

        IO.println("End: " + r.getEnd());

        IO.println("\ntrying to make new reservation...");
        LocalDateTime threePm = LocalDateTime.of(2026, 11, 10, 15, 0); //start time
        LocalDateTime fourPm = LocalDateTime.of(2026, 11, 10, 16, 0);//end time
        IO.println("3pm-4pm overlaps: " + r.overlaps(threePm, fourPm));

        IO.println("\ntrying to make new reservation...");
        LocalDateTime fivePm = LocalDateTime.of(2026, 11, 10, 17, 0);
        LocalDateTime sixPm = LocalDateTime.of(2026, 11, 10, 18, 0);
        IO.println("5pm-6pm overlaps: " + r.overlaps(fivePm, sixPm));
    }

}
