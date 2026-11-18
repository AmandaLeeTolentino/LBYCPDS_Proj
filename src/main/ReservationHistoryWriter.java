package main;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves every confirmed reservation to ReservationHistory.csv.
 * Columns: username, role, reservationId, building, slot, date, startTime, endTime,
 *          durationHours, fee, status
 */
public class ReservationHistoryWriter {

    private static final String HISTORY_PATH = "src/resources/ReservationHistory.csv";
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

    public static List<Reservation> loadCsv() {
        List<Reservation> loaded = new ArrayList<>();
        File file = new File(HISTORY_PATH);

        if (!file.exists()) return loaded;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] info = line.split(",");
                if (info.length < 11) {
                    System.out.println("Skipping improperly formatted reservation: " + line);
                    continue;
                }

                try {
                    String username = info[0].trim();
                    String id = info[2].trim();
                    String locationName = info[3].trim();
                    String slotId = info[4].trim();
                    LocalDate date = LocalDate.parse(info[5].trim(), DATE_FMT);
                    LocalTime time = LocalTime.parse(info[6].trim(), TIME_FMT);
                    double hours = Double.parseDouble(info[8].trim());
                    double fee = Double.parseDouble(info[9].trim());
                    ReservationStatus status = ReservationStatus.valueOf(info[10].trim());

                    LocalDateTime start = LocalDateTime.of(date, time);

                    // recreating new reservation objs
                    Reservation reservation = new Reservation(id, username, slotId, locationName, status, hours, fee, start);
                    loaded.add(reservation);

                } catch (Exception e) {
                    System.out.println("Skipping corrupted reservation: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: could not load " + HISTORY_PATH + " (" + e.getMessage() + ")");
        }

        return loaded;
    }

}