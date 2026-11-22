package main;

import java.util.*;
import java.io.*;

public class LoginRegistration {
    static final List<ParkingLocation> locations = LocationData.createLocations();
    static final ReservationService reservationService = new ReservationService();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // MAIN MENU !!!
        boolean menu = true;
        while (menu) {
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, """
                            ╷ ╷╭─╴╷  ╭─╴╭─╮╭┬╮╭─╴
                ╭── ⋅ ⋅ ──   │╷│├╴ │  │  │ ││││├╴  ── ⋅ ⋅ ──╮
                            ╰┴╯╰─╴╰─╴╰─╴╰─╯╵ ╵╰─╴                                
                """));

        System.out.println("""
                            
                    1.  Login
                    2.  Register
                    3.  Exit
                                                        
       """);

            System.out.println(Main.Colors.paint(Main.Colors.YELLOW,"╰─ ── ⋅ ⋅ ── ── ⋅ ⋅ ── ✩ ── ⋅ ⋅ ── ── ⋅ ⋅ ── ─╯"));

            System.out.print(Main.Colors.paint(Main.Colors.CYAN,"\nEnter choice (1/3):  "));
            String Choice = scanner.nextLine().trim();

            switch (Choice) {
                case "1":
                    String[] loginResult = Login(); // returns [Username, Role]
                    if (loginResult != null) {
                        String username = loginResult[0];
                        String role = loginResult[1];

                        // Route user to the correct menu based on their role
                        if (role.equalsIgnoreCase("STUDENT")) {
                            StudentMenu.showStudentMenu(username);
                        } else if (role.equalsIgnoreCase("ADMIN")) {
                            AdminMenu.showAdminMenu(username);
                        }
                    }
                    break;
                case "2":
                    Register();
                    break;
                case "3":
                    System.out.println(Main.Colors.paint(Main.Colors.GREEN,"\nExiting program. Goodbye!"));
                    menu = false;
                    break;
                default:
                    System.out.println(Main.Colors.paint(Main.Colors.RED,"\nInvalid choice! Press Enter to try again...\n"));
                    scanner.nextLine();
                    break;
            }
        }
        scanner.close();
    }

    // ==========================================
    // LOGIN & REGISTER METHODS
    // ==========================================
    public static String[] Login() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(Main.Colors.paint(Main.Colors.YELLOW, """
  
      ╷  ╭─╮╭─╴╷╭╮╷   ╭─╮╭─╴╭─╴╭─╮╷ ╷╭╮╷╶┬╴
   ══ │  │ ││╶╮││╰┤   ├─┤│  │  │ ││ ││╰┤ │  ══
      ╰─╴╰─╯╰─╯╵╵ ╵   ╵ ╵╰─╴╰─╴╰─╯╰─╯╵ ╵ ╵
   """));

        System.out.print(Main.Colors.paint(Main.Colors.CYAN,"Enter username:\t\t"));
        String Username = scanner.nextLine().trim();

        System.out.print(Main.Colors.paint(Main.Colors.CYAN,"Enter password:\t\t"));
        String Password = scanner.nextLine().trim();

        if (Username.isEmpty() || Password.isEmpty()) {
            System.out.println(Main.Colors.paint(Main.Colors.RED,"\nFields cannot be blank! Press Enter to go back..."));
            scanner.nextLine();
            return null;
        }

        String usersPath = "src/resources/Users.csv";
        File file = new File(usersPath);
        boolean accountExists = false;
        String foundRole = "";
        boolean passwordMatch = false;

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] userDetails = line.split(",", -1);
                    if (userDetails.length >= 3) {
                        String storedUser = userDetails[0].trim();
                        String storedPass = userDetails[1].trim();
                        String storedRole = userDetails[2].trim();

                        if (storedUser.equals(Username)) {
                            accountExists = true;
                            if (storedPass.equals(Password)) {
                                passwordMatch = true;
                                foundRole = storedRole;
                            }
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(Main.Colors.paint(Main.Colors.RED,"Error reading user database: " + e.getMessage()));
            }
        }

        if (!accountExists) {
            System.out.println(Main.Colors.paint(Main.Colors.RED,"\nNo account yet! Please register first. Press Enter to continue..."));
            scanner.nextLine();
            return null;
        } else if (!passwordMatch) {
            System.out.println(Main.Colors.paint(Main.Colors.RED,"\nIncorrect password! Press Enter to continue..."));
            scanner.nextLine();
            return null;
        }

        System.out.println(Main.Colors.paint(Main.Colors.GREEN,"\nLogin Successful! Role: " + foundRole + ". Press Enter to continue. "));
        scanner.nextLine();

        return new String[] { Username, foundRole };
    }

    public static void Register() {
        Scanner scanner = new Scanner(System.in);
        String usersPath = "src/resources/Users.csv";
        String username = "";
        String password = "";
        String confirmation = "";
        String role = "";
        boolean registration = true;

        while (registration) {
            System.out.println(Main.Colors.paint(Main.Colors.YELLOW,"""

   ╭─╮╭─╴╭─╴╷╭─╮╶┬╴╭─╴╭─╮   ╭─╮╭─╴╭─╴╭─╮╷ ╷╭╮╷╶┬╴
══ ├┬╯├╴ │╶╮│╰─╮ │ ├╴ ├┬╯   ├─┤│  │  │ ││ ││╰┤ │  ══
   ╵╰╴╰─╴╰─╯╵╰─╯ ╵ ╰─╴╵╰╴   ╵ ╵╰─╴╰─╴╰─╯╰─╯╵ ╵ ╵
   """));


            boolean validation = true;

            while (validation) {
                System.out.print(Main.Colors.paint(Main.Colors.CYAN,"Enter username:\t\t"));
                username = scanner.nextLine().trim();

                System.out.print(Main.Colors.paint(Main.Colors.CYAN,"Enter password:\t\t"));
                password = scanner.nextLine().trim();

                System.out.print(Main.Colors.paint(Main.Colors.CYAN,"Confirm password:\t"));
                confirmation = scanner.nextLine().trim();

                if (username.isEmpty() || password.isEmpty() || confirmation.isEmpty()) {
                    System.out.println(Main.Colors.paint(Main.Colors.RED,"\nInvalid input! Fields cannot be empty.\n"));
                }
                else if (!password.equals(confirmation)) {
                    System.out.println(Main.Colors.paint(Main.Colors.RED,"\nError! Passwords do not match.\n"));
                }
                else {
                    boolean usernameExists = false;
                    File file = new File(usersPath);

                    if (file.exists()) {
                        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                            String line;
                            while ((line = br.readLine()) != null) {
                                if (line.trim().isEmpty()) continue;
                                String[] userDetails = line.split(",", -1);
                                if (userDetails.length > 0 && userDetails[0].trim().equals(username)) {
                                    usernameExists = true;
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            System.out.println(Main.Colors.paint(Main.Colors.RED,"Error reading user database: " + e.getMessage()));
                        }
                    }

                    if (usernameExists) {
                        System.out.println(Main.Colors.paint(Main.Colors.RED,"\nError! Username already taken. Please choose another.\n"));
                    } else {
                        validation = false;
                    }
                }
            }

            boolean roleChoice = true;
            while (roleChoice) {
                System.out.println("""
       
        Choose Account Role       
            1.  Student
            2.  Administrator              
""");

                System.out.print(Main.Colors.paint(Main.Colors.CYAN,"\nEnter Role: "));
                role = scanner.nextLine().trim();

                switch (role) {
                    case "1":
                        role = "STUDENT";
                        roleChoice = false;
                        break;
                    case "2":
                        role = "ADMIN";
                        roleChoice = false;
                        break;
                    default:
                        System.out.println(Main.Colors.paint(Main.Colors.RED,"\nInvalid choice! Press Enter to try again...\n"));
                        scanner.nextLine();
                        break;
                }
            }

            try (PrintWriter pw = new PrintWriter(new FileWriter(usersPath, true))) {
                pw.println(username + "," + password + "," + role);

                System.out.println(Main.Colors.paint(Main.Colors.GREEN,"\n" + username + " (" + role + ") successfully registered! Press Enter to continue..."));
                scanner.nextLine();

                registration = false;
            } catch (IOException e) {
                System.out.println(Main.Colors.paint(Main.Colors.RED,"\nError saving user credentials: " + e.getMessage()));
                return;
            }
        }
    }
}