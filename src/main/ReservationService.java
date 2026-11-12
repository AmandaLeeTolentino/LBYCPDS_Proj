package main;


import java.time.LocalDateTime;
import java.util.*;

public class ReservationService {
    //easier to access reservation info by id (key: slot Id; val: reservation in slot)
    private HashMap<String, List<Reservation>> allReservations = new HashMap<>();
    private int idCount = 0;

    /**
     * @param slotId id of slot
     * @param start start time of reservation
     * @param end end time of reservation
     * @return if given slot is free during specified time
     */
    public boolean isSlotFree(String slotId, LocalDateTime start, LocalDateTime end) {
        List<Reservation> resInSlot = allReservations.get(slotId);

        if (resInSlot == null){
            return true;
        }

        for (Reservation res: resInSlot) {
            if(res.getStatus() == ReservationStatus.CANCELED){//ignore canceled ones
                continue;
            }
            if (res.overlaps(start, end)) { //if finds an overlap with existing reservation
                return false;
            }
        }
        return true;
    }


    /**
     * @param loc location of parking slot
     * @param start start time of reservation
     * @param end end time of reservation
     * @return ArrayList of all free slots in the location
     */
    public List<ParkingSlot> findFree(ParkingLocation loc, LocalDateTime start, LocalDateTime end){
        List<ParkingSlot> free = new ArrayList<>(); //hold list of free slots found

        for (ParkingSlot slot: loc.getSlots()){ //for each slot in location
            if(isSlotFree(slot.getSlotId(), start, end)){// check if free during specified time
                free.add(slot);
            }
        }
        return free;
    }


    /**
     * creates new reservation
     * @return reservation obj; will return null instead if invalid reservation
     */
    public Reservation reserve(String username, ParkingSlot slot, ParkingLocation loc, LocalDateTime start, double hours){

        LocalDateTime end = start.plusMinutes(Math.round(hours * 60));

        if(!isSlotFree(slot.getSlotId(), start, end)) return null;

        double fee = loc.getRate().computeFee(hours);
        String id = "R" + (++idCount);

        Reservation reservation  = new Reservation(id,username,slot.getSlotId(),
                loc.getName(),ReservationStatus.CONFIRMED, hours, fee, start);

        List<Reservation> list = allReservations.get(slot.getSlotId());//get list of allReservations of a slot

        if (list == null) {//if no list yet(first reservation)
            list = new ArrayList<>();
            allReservations.put(slot.getSlotId(), list);//add reservation to map of all reservations
        }
        list.add(reservation);//add reservation to slot's list

        return reservation;
    }

    //test
    public static void main(String[] args){
        //rate of test loc
        Rate rate = new Rate(RateType.HOURLY, 3, 70, 20);

        //test loc: egi
        ParkingLocation egi = new ParkingLocation("EGI Taft Tower", 15, rate, null, ParkingLocation.generateSlots("EGI Taft Tower", "EGI", new int[]{10}));

        ReservationService service = new ReservationService();
        ParkingSlot slot1 = egi.getSlot("EGI-F1-1");

        //test times
        LocalDateTime twoPm = LocalDateTime.of(2026, 11, 10, 14, 0);
        LocalDateTime threePm = LocalDateTime.of(2026, 11, 10, 15, 0);
        LocalDateTime fourPm = LocalDateTime.of(2026, 11, 10, 16, 0);
        LocalDateTime fivePm = LocalDateTime.of(2026, 11, 10, 17, 0);


        //first reservation for slot 1 from 2pm to 5pm
        IO.println("\nAttempting to reserve...");
        Reservation r = service.reserve("mandy", slot1, egi, twoPm, 3);
        IO.println("Reserved: " + r.getReservationId() + " From "+ r.getStart()+ " until " + r.getEnd());

        //try 3pm to 4pm on the same slot, inside the booking
        IO.println("\nAttempting to reserve...");
        Reservation second = service.reserve("kurt", slot1, egi, threePm, 1);
        if (second == null) {
            IO.println("3pm-4pm: rejected");
        } else {
            IO.println("3pm-4pm: accepted " + second.getReservationId());
        }


        //try 5pm to 6pm on the same slot, right after the booking
        IO.println("\nAttempting to reserve...");
        Reservation third = service.reserve("kurt", slot1, egi, fivePm, 1);
        if (third == null) {
            IO.println("5pm-6pm: rejected");
        } else {
            IO.println("5pm-6pm: accepted " + third.getReservationId() + " From " + third.getStart() + " until " + third.getEnd());
        }


        //free slots at 3pm-4pm: slot 1 is taken, the other 9 are free
        List<ParkingSlot> freeAt3 = service.findFree(egi, threePm, fourPm);//find list of free parking slots during time
    }

}
