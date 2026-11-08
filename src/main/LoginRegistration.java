import java.util.Scanner;
import java.io.FileReader;
import java.io.IOException;

public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    // MAIN MENU !!!
    boolean menu = true;
    while (menu) {
        // clear console if possible
        System.out.println("""

            ╷ ╷╭─╴╷  ╭─╴╭─╮╭┬╮╭─╴
            │╷│├╴ │  │  │ ││││├╴ 
            ╰┴╯╰─╴╰─╴╰─╴╰─╯╵ ╵╰─╴

                1. Login
                2. Register

        """);

        System.out.print("Enter choice (1/2):  ");
        String Choice = scanner.nextLine();

        switch (Choice) {
            case "1":
                Login(); // should return username and role
                // Call Home Page Interface
                menu = false;
                break;
            case "2":
                Register();
                break;
            // case for exit   JAMES !!!!
            default:
                System.out.print("\nInvalid choice! Press any key to continue.");
                scanner.nextLine();
                break;
        }
    }
    scanner.close();
}

public static String Login(){
    Scanner scanner = new Scanner(System.in);
    // clear console if possible
    System.out.println("""

        ╷  ╭─╮╭─╴╷╭╮╷   ╭─╮╭─╴╭─╴╭─╮╷ ╷╭╮╷╶┬╴
        │  │ ││╶╮││╰┤   ├─┤│  │  │ ││ ││╰┤ │
        ╰─╴╰─╯╰─╯╵╵ ╵   ╵ ╵╰─╴╰─╴╰─╯╰─╯╵ ╵ ╵ 

        """);

    System.out.print("Enter username: ");
    String Username = scanner.nextLine(); // validate pa field cannot be blank

    System.out.print("Enter password: ");
    String Password = scanner.nextLine(); // validate pa field cannot be blank

    /** TO DO !!!!!!!!!!!!!!
     * open Users.csv
     * for loop through first column to find username
     * check if username exists
     * exists -> get row
     * check column 2 of that row, compare entered pw to saved pw
     * if yes, do below*/

    String Role = " "; // place column3 (role column) to string

    System.out.println("Login Successful! Press any key to continue. ");
    scanner.nextLine();
    //LOGIN SUCCESS PARIN KAHIT  HINDI MAAYOS
    return Username;//, Role; // PANO BA TO SA JAVA NA MULTIPLE ARGUMENTS IRERETURN?
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

