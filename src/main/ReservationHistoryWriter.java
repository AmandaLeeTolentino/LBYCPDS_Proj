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

    /** Appends one reservation as a new row (creates the file + header if it doesn't exist yet). */
    public static void save(Reservation r) {
        if (r == null) return;

        boolean needsHeader = true;
        File file = new File(HISTORY_PATH);
        if (file.exists() && file.length() > 0) needsHeader = false;

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            if (needsHeader) pw.println(HEADER);

            pw.println(String.join(",",
                    escape(r.getUsername()),
                    escape(findRole(r.getUsername())),
                    escape(r.getReservationId()),
                    escape(r.getLocationName()),
                    escape(r.getSlotId()),
                    r.getStart().format(DATE_FMT),
                    r.getStart().format(TIME_FMT),
                    r.getEnd().format(TIME_FMT),
                    String.valueOf(r.getDurationHours()),
                    String.format("%.2f", r.getTotalAmount()),
                    r.getStatus().toString()));
        } catch (IOException e) {
            System.out.println("Warning: could not save to " + HISTORY_PATH + " (" + e.getMessage() + ")");
        }
    }

    /** Looks up the user's role (student/admin) from Users.csv (format: username,password,role). */
    private static String findRole(String username) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(USERS_PATH));
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].trim().equals(username)) {
                    return parts[2].trim().toLowerCase();
                }
            }
        } catch (IOException e) {
        }
        return "unknown";
    }

    /** Wraps a value in quotes if it contains a comma, quote, or newline. */
    private static String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

}