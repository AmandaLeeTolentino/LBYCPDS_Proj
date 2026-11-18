package main;

import java.util.*;

public class Main {

    public class Colors {
        public static final String RESET  = "\u001B[0m";
        public static final String BOLD   = "\u001B[1m";

        public static final String RED    = "\u001B[31m";
        public static final String GREEN  = "\u001B[32m";
        public static final String YELLOW = "\u001B[33m";
        public static final String BLUE   = "\u001B[34m";
        public static final String PURPLE = "\u001B[35m";
        public static final String CYAN   = "\u001B[36m";
        public static final String WHITE  = "\u001B[37m";

        private Colors() {}

        // wraps text in a color and resets it afterwards
        public static String paint(String color, String text) {
            return color + text + RESET;
        }
    }
}
