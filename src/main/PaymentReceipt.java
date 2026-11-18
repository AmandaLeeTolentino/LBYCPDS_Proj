package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;

/**
 * Simulated payment screen + receipt (.txt) writer.
 */
public class PaymentReceipt {

    // Folder where receipts are saved.
    private static final String RECEIPT_DIR = "receipts";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    private PaymentReceipt() {}

    /**
     * Shows the payment screen and asks for the amount to pay.
     * @return the amount paid, or 0 if the user backed out by entering 0
     */
    public static double collectPayment(Rate rate, double hours, double total) {
        System.out.println("\n========================================");
        System.out.println("                PAYMENT");
        System.out.println("========================================");
        System.out.println();
        System.out.println("Rate: " + describeRate(rate));
        System.out.println("Duration: " + hours + " hours");
        System.out.println();
        System.out.println(String.format("TOTAL: Php %.2f", total));
        System.out.println("========================================");

        long totalCents = Math.round(total * 100);

        while (true) {
            double paid = Input.readNonNegativeDouble("\nEnter amount to pay (0 to go back): ");

            if (paid == 0) {
                return 0; // user backed out
            }
            if (Math.round(paid * 100) < totalCents) {
                System.out.println(String.format("Insufficient amount. You need to pay at least Php %.2f.", total));
                continue;
            }

            double change = paid - total;
            System.out.println();
            System.out.println(String.format("TOTAL:  Php %.2f", total));
            System.out.println(String.format("Paid :  Php %.2f", paid));
            System.out.println(String.format("Change: Php %.2f", change));
            return paid;
        }
    }

    /**
     * Creates the receipt file "[Username]_[ReservationID].txt".
     */
    public static void writeReceipt(Reservation r, ParkingSlot slot, Rate rate) {
        if (r == null) return;

        String safeUser = r.getUsername().replaceAll("[^A-Za-z0-9._-]", "_");
        String fileName = safeUser + "_" + r.getReservationId() + ".txt";

        File dir = new File(RECEIPT_DIR);
        if (!RECEIPT_DIR.isEmpty() && !dir.exists()) {
            dir.mkdirs();
        }
        File file = RECEIPT_DIR.isEmpty() ? new File(fileName) : new File(dir, fileName);

        String floorSlot = (slot != null)
                ? "Floor " + slot.getFloor() + " / Slot " + slot.getSlotId()
                : "Slot " + r.getSlotId();

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("PARKING RESERVATION RECEIPT");
            pw.println();
            pw.println("Reservation ID: " + r.getReservationId());
            pw.println("Username: " + r.getUsername());
            pw.println();
            pw.println("Building: " + r.getLocationName());
            pw.println("Floor/Slot: " + floorSlot);
            pw.println();
            pw.println(String.format("%-10s %-15s %s", "Entry:",
                    r.getStart().format(DATE_FMT), r.getStart().format(TIME_FMT)));
            pw.println(String.format("%-10s %-15s %s", "Exit:",
                    r.getEnd().format(DATE_FMT), r.getEnd().format(TIME_FMT)));
            pw.println("Duration: " + r.getDurationHours() + " hours");
            pw.println();
            pw.println("Rate: " + describeRate(rate));
            pw.println(String.format("TOTAL: Php %.2f", r.getTotalAmount()));
        } catch (IOException e) {
            System.out.println("Warning: could not create receipt file (" + e.getMessage() + ")");
        }
    }

    // helper
    private static String describeRate(Rate rate) {
        if (rate.getType() == RateType.FIXED) {
            return String.format("FIXED - Php %.2f flat fee", rate.getBaseFee());
        }
        return String.format("HOURLY - Php %.2f for the first %d hours + Php %.2f per succeeding hour",
                rate.getBaseFee(), rate.getBaseHours(), rate.getExtraFee());
    }
}