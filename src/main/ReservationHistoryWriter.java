package main;

import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Saves every confirmed reservation to ReservationHistory.csv.
 * Columns: username, role, reservationId, building, slot, date, startTime, endTime,
 *          durationHours, fee, status
 */
public class ReservationHistoryWriter {

    private static final String HISTORY_PATH = "ReservationHistory.csv";
    private static final String USERS_PATH = "Users.csv";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final String HEADER =  "Username,Role,ReservationID,Building,Slot,Date,StartTime,EndTime,DurationHours,Fee,Status";

}