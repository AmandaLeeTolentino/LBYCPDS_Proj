package main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ParkingStatMenu {
    private final String divider = " ⋅".repeat(75);
    private static final String FOOTER = "╰─ ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅   ✩   ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ── ⋅ ⋅ ── ─╯";
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("MMM dd, yyyy | h:mm a");
    private ReservationService reservations;
    private List<ParkingLocation> locations;

    public ParkingStatMenu(List<ParkingLocation> locations, ReservationService reservations){
        this.locations = locations;
        this.reservations = reservations;
    }

    public void start(){
        while(true){

            LocalDateTime now = LocalDateTime.now();
            System.out.println(Main.Colors.paint(Main.Colors.YELLOW, "\n" + header("DLSU PARKING STATUS & GUIDE")));
            System.out.println(Main.Colors.paint(Main.Colors.CYAN, "\t\t\t\t\t\t\t\t\t\t(As of " + now.format(format) + ")"));

            //go through each parking location
            for(int i = 0; i < locations.size(); i++){
                ParkingLocation loc = locations.get(i);

                int freeCount = reservations.findFree(loc, now, 1).size();

                //displaying status for each parking
                System.out.println(Main.Colors.paint(Main.Colors.YELLOW, divider));
                System.out.println("\t" + (i + 1) + ". " + loc.getName());

                String free = freeCount + " slot/s free (for the next 1hr)";
                System.out.println("\t\tFree Slots:\t\t\t" + Main.Colors.paint(freeCount > 0 ? Main.Colors.GREEN : Main.Colors.RED, free));
                showGuide(loc, now);
            }

            System.out.println(Main.Colors.paint(Main.Colors.YELLOW, divider));
            System.out.println(Main.Colors.paint(Main.Colors.YELLOW, FOOTER));

            int selection = Input.readInt(Main.Colors.paint(Main.Colors.CYAN, "\nPress 1 to refresh the page (0 to go back): "), 0, 1);
            if(selection == 0)return;
            if(selection == 1)continue;
        }

    }

    private void showGuide(ParkingLocation loc, LocalDateTime now){

        System.out.println("\t\tDistance to Campus:\t" + loc.getDistance());
        Rate rate = loc.getRate();
        if (rate.getType() == RateType.FIXED){
            System.out.println("\t\tRate Type:\t\t\t" + rate.getType());
            System.out.println("\t\tFixed Fee:\t\t\tPhp " + rate.getBaseFee());
        }else{
            System.out.println("\t\tRate Type:\t\t\t" + rate.getType());
            System.out.println("\t\tBase Fee:\t\t\tPhp " + rate.getBaseFee() + " for the first " + rate.getBaseHours() + " hour(s)");
            System.out.println("\t\tSucceeding Hr Fee:\tPhp " + rate.getExtraFee());
        }
        if (loc.getNotes() != null) {
            System.out.println("\t\tNotes:\t\t\t\t" + loc.getNotes());
        }

    }

    /** yellow title bar */
    private static String header(String title) {
        String pattern = "── ⋅ ⋅ ".repeat(30);
        int side = (FOOTER.length() - title.length() - 4) / 2;   // decoration on each side of the title
        String left = "╭─ " + pattern.substring(0, Math.max(0, side - 3));
        int rightLength = FOOTER.length() - left.length() - title.length() - 5;
        String right = pattern.substring(0, Math.max(0, rightLength));
        return left + " " + title + " " + right + " ─╮";
    }

}
