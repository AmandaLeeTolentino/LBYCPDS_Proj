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
     * @param hours total hours for reserving the slot
     * @return if given slot is free during specified time
     */
    public boolean isSlotFree(String slotId, LocalDateTime start, double hours) {
        List<Reservation> resInSlot = allReservations.get(slotId);

        LocalDateTime end = start.plusMinutes(Math.round(hours * 60));

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
     * @param hours hours to reserve slot
     * @return ArrayList of all free slots in the location
     */
    public List<ParkingSlot> findFree(ParkingLocation loc, LocalDateTime start, double hours){
        List<ParkingSlot> free = new ArrayList<>(); //hold list of free slots found

        for (ParkingSlot slot: loc.getSlots()){ //for each slot in location
            if(isSlotFree(slot.getSlotId(), start, hours)){// check if free during specified time
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

        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username is required");
        if (slot == null) throw new IllegalArgumentException("Slot does not exist");
        if (loc == null) throw new IllegalArgumentException("Location does not exist");
        if (start == null) throw new IllegalArgumentException("Start time is required");
        if (hours < 0) throw new IllegalArgumentException("Hours must be positive: " + hours);
        if (loc.getSlot(slot.getSlotId()) == null) {
            throw new IllegalArgumentException("Slot " + slot.getSlotId() + " is not in " + loc.getName());
        }

        if(!isSlotFree(slot.getSlotId(), start, hours)) return null;

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
}
