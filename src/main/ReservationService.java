package main;


import java.time.LocalDateTime;
import java.util.*;

public class ReservationService {
    //easier to access reservation info by id (key: slot Id; val: reservation in slot)
    private HashMap<String, List<Reservation>> allReservations = new HashMap<>();
    private int idCount = 0;

    //constructor
    public ReservationService() {
        for (Reservation r : ReservationHistoryWriter.loadCsv()) {
            addToMap(r);
            if (r.getReservationId().startsWith("R")) {
                int num = Integer.parseInt(r.getReservationId().substring(1));
                if (num > idCount) idCount = num;
            }
        }
    }


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
        addToMap(reservation);

        return reservation;
    }

    private void addToMap(Reservation r){

        List<Reservation> list = allReservations.get(r.getSlotId());//get list of allReservations of a slot

        if (list == null) {//if no list yet(first reservation)
            list = new ArrayList<>();
            allReservations.put(r.getSlotId(), list);//add reservation to map of all reservations
        }
        list.add(r);//add reservation to slot's list
    }



    /**
     * @return all non-canceled reservations that overlap [from, to), sorted by building then start time
     */
    public List<Reservation> getReservationsBetween(LocalDateTime from, LocalDateTime to) {
        List<Reservation> found = new ArrayList<>();
        for (List<Reservation> list : allReservations.values()) {
            for (Reservation r : list) {
                if (r.getStatus() != ReservationStatus.CANCELED && r.overlaps(from, to)) {
                    found.add(r);
                }
            }
        }
        found.sort(Comparator.comparing(Reservation::getLocationName)
                .thenComparing(Reservation::getStart));
        return found;
    }

    /** @return every non-canceled reservation */
    public List<Reservation> getAllReservations() {
        List<Reservation> all = new ArrayList<>();
        for (List<Reservation> list : allReservations.values()) {
            for (Reservation r : list) {
                if (r.getStatus() != ReservationStatus.CANCELED) all.add(r);
            }
        }
        return all;
    }

    /**
     * @return the non-canceled reservations of a slot that overlap the given time window
     */
    public List<Reservation> getConflicts(String slotId, LocalDateTime start, double hours) {
        List<Reservation> found = new ArrayList<>();
        List<Reservation> list = allReservations.get(slotId);
        if (list == null) return found;

        LocalDateTime end = start.plusMinutes(Math.round(hours * 60));
        for (Reservation r : list) {
            if (r.getStatus() != ReservationStatus.CANCELED && r.overlaps(start, end)) {
                found.add(r);
            }
        }
        found.sort(Comparator.comparing(Reservation::getStart));
        return found;
    }

    /**
     * finds the reservation given the reservation id and returns the reservation obj
     */
    public Reservation byId(String reservationId){
        if (reservationId == null) return null;

        //for each info grp in all reservation
        for (List<Reservation> list : allReservations.values()) {

            //each specific info in grp
            for (Reservation r : list) {
                if  (r.getReservationId().equals(reservationId)) return r;
            }
        }
        return null;
    }


    /**
     * cancels reservation and returns true if successful
     */
    public boolean cancelReservation(String reservationId){
        Reservation r = byId(reservationId);
        if (r == null || r.getStatus() == ReservationStatus.CANCELED) return false; //if null or alrdy cancelled

        r.cancel();
        ReservationHistoryWriter.saveAll(allReservations.values());
        return true;
    }
}
