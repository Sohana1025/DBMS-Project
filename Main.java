import java.util.Scanner;
public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        CarService service = new CarService();
        while (true) {

            // ---- Print the menu ----
            System.out.println("\n===== Car Rental System =====");
            System.out.println("1. Add Car");
            System.out.println("2. Book Car");
            System.out.println("3. Return Car");
            System.out.println("4. Cars Currently On Rent");
            System.out.println("5. Exit");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine();
            switch (choice) {

                case 1: // ADD CAR
                    System.out.print("Enter car model: ");
                    String model = sc.nextLine().trim();  // .trim() removes accidental spaces

                    System.out.print("Enter registration number: ");
                    String regNo = sc.nextLine().trim();

                    System.out.print("Enter rate per day (Rs): ");
                    double rate = sc.nextDouble();
                    sc.nextLine();  // consume newline after double input

                    service.addCar(model, regNo, rate);
                    break;
                case 2: // BOOK CAR
                    System.out.print("Enter Car ID to book: ");
                    int carId = sc.nextInt();
                    sc.nextLine();  // consume newline

                    System.out.print("Enter customer name: ");
                    String customer = sc.nextLine().trim();

                    // Dates must be in YYYY-MM-DD format for MySQL DATE columns
                    System.out.print("Enter from date (YYYY-MM-DD): ");
                    String fromDate = sc.nextLine().trim();

                    System.out.print("Enter to date (YYYY-MM-DD): ");
                    String toDate = sc.nextLine().trim();

                    service.bookCar(carId, customer, fromDate, toDate);
                    break;
                case 3: // RETURN CAR
                    System.out.print("Enter Rental ID: ");
                    int rentId = sc.nextInt();
                    sc.nextLine();  // consume newline

                    service.returnCar(rentId);
                    break;

                case 4: // LIST CARS ON RENT
                    service.listCarsOnRent();
                    break;
                case 5: // EXIT
                    System.out.println("Exiting... Thank you!");
                    sc.close();    // close Scanner to release resources
                    System.exit(0); // terminate JVM with success code 0
                    break;
                default: // INVALID CHOICE
                    System.out.println("Invalid choice. Please enter 1–5.");
            }
        }
    }
}
