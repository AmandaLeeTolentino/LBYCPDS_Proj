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
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, "\n╔════════════════════════════════════ ≪ PAYMENT ≫ ════════════════════════════════════╗"));
        System.out.println("\nRate: " + describeRate(rate));
        System.out.println("Duration: " + hours + " hours");
        System.out.println(String.format("\nTOTAL: Php %.2f", total));
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW,"\n╚═════════════════════════════════════════════════════════════════════════════════════╝"));
        long totalCents = Math.round(total * 100);

        while (true) {
            double paid = Input.readNonNegativeDouble(Main.Colors.paint(Main.Colors.CYAN,"\nEnter amount to pay (0 to go back): "));

            if (paid == 0) {
                return 0; // user backed out
            }
            if (Math.round(paid * 100) < totalCents) {
                System.out.println(Main.Colors.paint(Main.Colors.RED,String.format("Insufficient amount. You need to pay at least Php %.2f.", total)));
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
     * Payment step after EDITING a reservation.
     * Compares the new total with what was already paid, then:
     *  - new total higher  -> asks the user to pay the extra amount
     *  - new total lower   -> shows the refund and asks to confirm
     *  - same total        -> nothing to pay
     * @return true if the edit may be saved, false if the user backed out
     */
    public static boolean settleAdjustment(Rate rate, double hours, double previousTotal, double newTotal) {
        long diffCents = Math.round(newTotal * 100) - Math.round(previousTotal * 100);
        double diff = Math.abs(diffCents) / 100.0;

        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, "\n╔═════════════════════════════════ ≪ UPDATED PAYMENT ≫ ═════════════════════════════════╗"));
        System.out.println("\nRate: " + describeRate(rate));
        System.out.println("New Duration: " + hours + " hours");
        System.out.println(String.format("\nPrevious TOTAL (already paid): Php %.2f", previousTotal));
        System.out.println(String.format("New TOTAL:                     Php %.2f", newTotal));

        if (diffCents == 0) {
            System.out.println("\nThe fee did not change. No payment or refund is needed.");
            System.out.println(Main.Colors.paint(Main.Colors.YELLOW, "\n╚═════════════════════════════════════════════════════════════════════════════════════╝"));
            return true;
        }

        if (diffCents < 0) {
            // if user will be refunded
            System.out.println(String.format("\nREFUND DUE: Php %.2f", diff));
            System.out.println(Main.Colors.paint(Main.Colors.YELLOW, "\n╚═════════════════════════════════════════════════════════════════════════════════════╝"));

            if (!Input.readYesNo(Main.Colors.paint(Main.Colors.CYAN, "\nConfirm changes and receive your refund"))) {
                return false;
            }

            System.out.println(String.format("\nPrevious TOTAL: Php %.2f", previousTotal));
            System.out.println(String.format("New TOTAL:      Php %.2f", newTotal));
            System.out.println(Main.Colors.paint(Main.Colors.GREEN, String.format("\nREFUNDED:       Php %.2f", diff)));
            return true;
        }

        // if user will be charged extra
        System.out.println(String.format("\nADDITIONAL PAYMENT DUE: Php %.2f", diff));
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, "\n╚═════════════════════════════════════════════════════════════════════════════════════╝"));

        while (true) {
            double paid = Input.readNonNegativeDouble(Main.Colors.paint(Main.Colors.CYAN, "\nEnter amount to pay (0 to go back): "));

            if (paid == 0) {
                return false; // user backed out
            }
            if (Math.round(paid * 100) < diffCents) {
                System.out.println(Main.Colors.paint(Main.Colors.RED,
                        String.format("Insufficient amount. You need to pay at least Php %.2f.", diff)));
                continue;
            }

            System.out.println();
            System.out.println(String.format("Amount due: Php %.2f", diff));
            System.out.println(String.format("Paid :      Php %.2f", paid));
            System.out.println(String.format("Change:     Php %.2f", paid - diff));
            return true;
        }
    }

    /**
     * Creates the receipt file "[Username]_[ReservationID].txt".
     */
    public static void writeReceipt(Reservation r, ParkingSlot slot, Rate rate) {
        writeReceipt(r, slot, rate, -1);
    }

    /**
     * Same as above, but for a reservation that was edited: the old receipt file is
     * overwritten and shows the previous total and the adjustment.
     * @param previousTotal total before the edit (pass a negative number for a new reservation)
     */
    public static void writeReceipt(Reservation r, ParkingSlot slot, Rate rate, double previousTotal) {
        if (r == null) return;
        boolean edited = previousTotal >= 0;

        String safeUser = r.getUsername().replaceAll("[^A-Za-z0-9._-]", "_");
        String fileName = safeUser + "_" + r.getReservationId() + ".txt";

        File dir = new File(RECEIPT_DIR);
        if (!RECEIPT_DIR.isEmpty() && !dir.exists()) {
            dir.mkdirs();
        }
        File file = RECEIPT_DIR.isEmpty() ? new File(fileName) : new File(dir, fileName);

        String floorSlot = (slot != null)
                ? "Floor " + slot.getFloor() + " / Slot " + slot.getDisplayId()
                : "Slot " + r.getSlotId();

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println(edited ? "PARKING RESERVATION RECEIPT (UPDATED)" : "PARKING RESERVATION RECEIPT");
            pw.println();
            pw.println("Reservation ID: " + r.getReservationId());
            pw.println("Name: " + LoginRegistration.getDisplayName(r.getUsername()));
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

            if (edited) {
                long diffCents = Math.round(r.getTotalAmount() * 100) - Math.round(previousTotal * 100);
                pw.println();
                pw.println(String.format("Previous TOTAL: Php %.2f", previousTotal));

                if (diffCents > 0) {
                    pw.println(String.format("Additional payment: Php %.2f", diffCents / 100.0));
                } else if (diffCents < 0) {
                    pw.println(String.format("Refund: Php %.2f", -diffCents / 100.0));
                } else {
                    pw.println("Fee unchanged: no payment or refund");
                }
            }
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