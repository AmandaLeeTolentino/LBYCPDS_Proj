package main;


import java.time.LocalDateTime;
import java.util.*;

public class ReservationService {
    //easier to access reservation info by id (key: res Id; val: reservation info)
    private HashMap<String, List<Reservation>> reservations;

    public boolean isSlotFree(String id, LocalDateTime start, LocalDateTime end) {
        if (reservations.get(id) == null){
            return true;
        }else{return false;}
    }

    public List<ParkingSlot> findFree(ParkingLocation loc, LocalDateTime start, LocalDateTime end){
        List<ParkingSlot> free = new ArrayList<>(); //hold list of free slots found

        for (ParkingSlot slot: loc.getSlots()){ //for each slot in location
            if(isSlotFree(slot.getSlotId(), start, end)){// check if free during specified time
                free.add(slot);
            }
        }
        return free;
    }

    //todo: make reserve method

}
