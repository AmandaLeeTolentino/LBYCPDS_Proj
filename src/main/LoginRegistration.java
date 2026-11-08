package main;

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LoginRegistration {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // MAIN MENU !!!
        boolean menu = true;
        while (menu) {
            System.out.println("""

                ╷ ╷╭─╴╷  ╭─╴╭─╮╭┬╮╭─╴
                │╷│├╴ │  │  │ ││││├╴ 
                ╰┴╯╰─╴╰─╴╰─╴╰─╯╵ ╵╰─╴

    1. Login
    2. Register
    3. Exit
            """);

            System.out.print("Enter choice (1/3):  ");
            String Choice = scanner.nextLine();

            switch (Choice) {
                case "1":
                    String[] loginResult = Login(); // returns [Username, Role]
                    if (loginResult != null) {
                        String username = loginResult[0];
                        String role = loginResult[1];
                        // Call Home Page Interface based on role
                        menu = false;
                    }
                    break;
                case "2":
                    Register();
                    break;
                case "3":
                    System.out.println("\nExiting program. Goodbye!");
                    menu = false;
                    break;
                default:
                    System.out.print("\nInvalid choice! Press Enter to continue.");
                    scanner.nextLine();
                    break;
            }
        }
        scanner.close();
    }

    public static String[] Login() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("""

            ╷  ╭─╮╭─╴╷╭╮╷   ╭─╮╭─╴╭─╴╭─╮╷ ╷╭╮╷╶┬╴
            │  │ ││╶╮││╰┤   ├─┤│  │  │ ││ ││╰┤ │
            ╰─╴╰─╯╰─╯╵╵ ╵   ╵ ╵╰─╴╰─╴╰─╯╰─╯╵ ╵ ╵ 

            """);

        System.out.print("Enter username: ");
        String Username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String Password = scanner.nextLine();

        if (Username.isEmpty() || Password.isEmpty()) {
            System.out.println("\nFields cannot be blank! Press Enter to go back...");
            scanner.nextLine();
            return null;
        }

        String usersPath = "Users.csv";
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

                        if (storedUser.equalsIgnoreCase(Username)) {
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
                System.out.println("Error reading user database: " + e.getMessage());
            }
        }

        if (!accountExists) {
            System.out.println("\nNo account yet! Please register first. Press Enter to continue...");
            scanner.nextLine();
            return null;
        } else if (!passwordMatch) {
            System.out.println("\nIncorrect password! Press Enter to continue...");
            scanner.nextLine();
            return null;
        }

        System.out.println("\nLogin Successful! Role: " + foundRole + ". Press Enter to continue. ");
        scanner.nextLine();

        return new String[] { Username, foundRole };
    }

    public static void Register() {
        Scanner scanner = new Scanner(System.in);
        String usersPath = "Users.csv";
        String username = "";
        String password = "";
        String confirmation = "";
        String role = "";
        boolean registration = true;

        while (registration) {
            System.out.println("""

                ╭─╮╭─╴╭─╴╷╭─╮╶┬╴╭─╴╭─╮   ╭─╮╭─╴╭─╴╭─╮╷ ╷╭╮╷╶┬╴
                ├┬╯├╴ │╶╮│╰─╮ │ ├╴ ├┬╯   ├─┤│  │  │ ││ ││╰┤ │
                ╵╰╴╰─╴╰─╯╵╰─╯ ╵ ╰─╴╵╰╴   ╵ ╵╰─╴╰─╴╰─╯╰─╯╵ ╵ ╵

                """);

            boolean validation = true;

            while (validation) {
                System.out.print("Enter username: ");
                username = scanner.nextLine().trim();

                System.out.print("Enter password: ");
                password = scanner.nextLine();

                System.out.print("Confirm password: ");
                confirmation = scanner.nextLine();

                if (username.isEmpty() || password.isEmpty() || confirmation.isEmpty()) {
                    System.out.println("\nInvalid input! Fields cannot be empty.\n");
                }
                else if (!password.equals(confirmation)) {
                    System.out.println("\nError! Passwords do not match.\n");
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
                                if (userDetails.length > 0 && userDetails[0].trim().equalsIgnoreCase(username)) {
                                    usernameExists = true;
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("Error reading user database: " + e.getMessage());
                        }
                    }

                    if (usernameExists) {
                        System.out.println("\nError! Username already taken. Please choose another.\n");
                    } else {
                        validation = false;
                    }
                }
            }

            boolean roleChoice = true;
            while (roleChoice) {
                System.out.println("\n\t\t\t\tROLE");
                System.out.println("\t\t\t\t1. Student");
                System.out.println("\t\t\t\t2. Administrator");
                System.out.print("\nEnter Role: ");
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
                        System.out.println("\nInvalid choice!");
                        System.out.print("Press Enter to try again...");
                        scanner.nextLine();
                        break;
                }
            }

            try (PrintWriter pw = new PrintWriter(new FileWriter(usersPath, true))) {
                pw.println(username + "," + password + "," + role);

                System.out.println("\n" + username + " (" + role + ") successfully registered!");
                System.out.print("Press Enter to continue...");
                scanner.nextLine();

                registration = false;
            } catch (IOException e) {
                System.out.println("\nError saving user credentials: " + e.getMessage());
                return;
            }
        }
    }
}