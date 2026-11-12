package main;


import java.time.LocalDateTime;
import java.util.*;

public class ReservationService {
    //easier to access reservation info by id (key: res Id; val: reservation info)
    private HashMap<String, List<Reservation>> reservations;
    private int idCount = 0;

    /**
     * @param id reservation id
     * @param start start time of reservation
     * @param end end time of reservation
     * @return if given slot is free during specified time
     */
    public boolean isSlotFree(String id, LocalDateTime start, LocalDateTime end) {
        if (reservations.get(id) == null){
            return true;
        }else{return false;}
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
     * @return reservation obj
     */
    public Reservation reserve(String username, ParkingSlot slot, LocalDateTime start,
                               ParkingLocation loc, LocalDateTime Start, double hours){
        LocalDateTime end = start.plusMinutes(Math.round(hours * 60));

        if(!isSlotFree(slot.getSlotId(), start, end)) return null;

        double fee = loc.getRate().computeFee(hours);
        String id = "R" + idCount++;

        Reservation reservation  = new Reservation(id,username,slot.getSlotId(),
                loc.getName(),ReservationStatus.CONFIRMED, hours, fee, start);

        return reservation;
    }

}
