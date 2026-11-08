import java.util.Scanner;
import java.io.FileReader;
import java.io.IOException;

public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    // MAIN MENU !!!
    boolean menu = true;
    while (menu) {
        // clear console if possible
        System.out.println( "\n\t\t\t╷ ╷╭─╴╷  ╭─╴╭─╮╭┬╮╭─╴\n" +
                              "\t\t\t│╷│├╴ │  │  │ ││││├╴ \n" +
                              "\t\t\t╰┴╯╰─╴╰─╴╰─╴╰─╯╵ ╵╰─╴\n\n"+
                            "\t\t\t\t1. Login\n\t\t\t\t2. Register\n\n");

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
    System.out.print(   "\n╷  ╭─╮╭─╴╷╭╮╷   ╭─╮╭─╴╭─╴╭─╮╷ ╷╭╮╷╶┬╴\n" +
                          "│  │ ││╶╮││╰┤   ├─┤│  │  │ ││ ││╰┤ │ \n" +
                          "╰─╴╰─╯╰─╯╵╵ ╵   ╵ ╵╰─╴╰─╴╰─╯╰─╯╵ ╵ ╵ \n\n");

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

    return Username;//, Role; // PANO BA TO SA JAVA NA MULTIPLE ARGUMENTS IRERETURN?
}

public static void Register() {
    Scanner scanner = new Scanner(System.in);
    String usersPath = "Users.csv";
    boolean registration = true;
    String Role = "";

    while (registration) {
        // clear console if possible
        System.out.print(   "\n╭─╮╭─╴╭─╴╷╭─╮╶┬╴╭─╴╭─╮   ╭─╮╭─╴╭─╴╭─╮╷ ╷╭╮╷╶┬╴\n" +
                              "├┬╯├╴ │╶╮│╰─╮ │ ├╴ ├┬╯   ├─┤│  │  │ ││ ││╰┤ │ \n" +
                              "╵╰╴╰─╴╰─╯╵╰─╯ ╵ ╰─╴╵╰╴   ╵ ╵╰─╴╰─╴╰─╯╰─╯╵ ╵ ╵ \n\n");

        System.out.print("Enter username: ");
        String Username = scanner.nextLine(); // validate pa field cannot be blank

        System.out.print("Enter password: ");
        String Password = scanner.nextLine(); // validate pa field cannot be blank

        System.out.print("Confirm password: ");
        String Confirmation = scanner.nextLine(); // validate pa field cannot be blank

        if (Objects.equals(Password, Confirmation)) {
            registration = false;
            boolean roleChoice = true;

            while (roleChoice) {
                System.out.print("\n\t\t\t\tROLE\n\t\t\t\t1. Student\n\t\t\t\t2. Administrator\n\nEnter Role: ");
                Role = scanner.nextLine();

                switch (Role) {
                    case "1":
                        Role = "STUDENT";
                        roleChoice = false;
                        break;
                    case "2":
                        Role = "ADMIN";
                        roleChoice = false;
                        break;
                    default:
                        System.out.print("\nInvalid choice!");
                        System.out.print("Press any key to continue...");
                        scanner.nextLine();
                        break;
                }
            }
            System.out.println(Username + " (" + Role + ") successfully registered!");
            System.out.println("Press any key to continue...");
            scanner.nextLine();

            // Opens user database and saves credentials
            try (BufferedReader br = new BufferedReader(new FileReader(usersPath))) {
                /** TO DO !!!!!!!!!!!!!!
                 // for loop sa csv to find empty row
                 // save: column1 = Username, column2 = Password, column3 = Role
                 */

                System.out.println(Username + " (" + Role + ") successfully registered!");
                System.out.println("Press any key to continue...");
                scanner.nextLine();

            } catch (IOException e) {
                System.out.println("\nError reading file: " + e.getMessage());
                return;
            }
        }
        else { System.out.println("\nError! Passwords do not match."); }

    }
}

