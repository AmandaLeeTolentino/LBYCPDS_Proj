package main;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class Input {

    private static final Scanner input = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("M-d-uuuu").withResolverStyle(ResolverStyle.STRICT);

    private Input() {}

    /**
     * gets integer input and makes sure it's a whole number from specified
     * min to max
     */
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            IO.print(prompt);
            try {
                int value = input.nextInt();
                input.nextLine(); //discard enter key (i noticed scanner doesn't work properly sometimes w/o it?)
                if (value >= min && value <= max) return value;
            } catch (InputMismatchException e) {
                input.nextLine();//discard the bad input
            }
            IO.println("Enter a number from " + min + " to " + max + ".");
        }
    }

    /**
     * gets double input and makes sure it's greater than 0
     */
    public static double readPositiveDouble(String prompt) {
        while (true) {
            IO.print(prompt);
            try {
                double value = input.nextDouble();
                input.nextLine();
                if (value > 0 && !Double.isInfinite(value)) return value;
            } catch (InputMismatchException e) {
                input.nextLine(); //get rid of bad input
            }
            IO.println("Enter a number greater than 0.");
        }
    }

    /**
     * gets string input and makes sure it's not empty or just whitespaces
     */
    public static String readNonBlank(String prompt) {
        while (true) {
            IO.print(prompt);
            String line = input.nextLine().trim();
            if (!line.isEmpty()) return line;
            IO.println("This cannot be blank.");
        }
    }

    /**
     * for yes/no or y/n inputs
     * @return True - yes/y; False - no/n
     */
    public static boolean readYesNo(String prompt) {
        while (true) {
            IO.print(prompt + " (Y/N): ");
            String line = input.nextLine().trim().toLowerCase();
            if (line.equals("y") || line.equals("yes")) return true;
            if (line.equals("n") || line.equals("no")) return false;
            IO.println("Enter Y or N.");
        }
    }


    /**
     * makes sure time input is in military time format
     */
    public static LocalTime readTime(String prompt) {
        while (true) {
            IO.print(prompt);
            String line = input.nextLine().trim();
            try {
                return LocalTime.parse(line);
            } catch (DateTimeParseException e) {
                IO.println("Use 24-hour HH:mm, for example 14:30.");
            }
        }
    }

    public static LocalDate readDate(String prompt) {
        while (true) {
            IO.print(prompt);
            String line = input.nextLine().trim();
            try {
                return LocalDate.parse(line, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                IO.println("Use MM-dd-yyyy, for example 05-20-2026.");
            }
        }
    }
}