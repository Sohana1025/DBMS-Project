import java.sql.*;
import java.time.LocalDate;

public class CarService {

    // =====================================================================
    // FEATURE 1: ADD CAR
    // =====================================================================

    /**
     * addCar()
     * --------
     * Inserts a new car record into the `cars` table.
     *
     * @param model   – car model name  e.g. "Maruti Swift"
     * @param regNo   – registration number  e.g. "WB01AB1234"
     * @param rateDay – rental rate per day in Rs  e.g. 1500.00
     */
    public void addCar(String model, String regNo, double rateDay) {
        // SQL with ? placeholders → PreparedStatement fills them safely
        // PreparedStatement prevents SQL Injection attacks
        String sql = "INSERT INTO cars (model, reg_no, rate_day) VALUES (?, ?, ?)";

        // try-with-resources → automatically closes Connection & PreparedStatement
        // even if an exception occurs (no need for finally block)
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Bind values to the ? placeholders (index starts at 1, not 0)
            ps.setString(1, model);    // 1st ?  = model
            ps.setString(2, regNo);    // 2nd ?  = reg_no
            ps.setDouble(3, rateDay);  // 3rd ?  = rate_day

            // executeUpdate() runs INSERT/UPDATE/DELETE; returns rows affected
            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Car added successfully!");
            } else {
                System.out.println("Failed to add car.");
            }

        } catch (SQLException e) {
            // SQLIntegrityConstraintViolationException is a subclass of SQLException
            // It fires if reg_no is not UNIQUE (duplicate registration number)
            System.out.println("Error adding car: " + e.getMessage());
        }
    }

    // =====================================================================
    // FEATURE 2: BOOK CAR
    // =====================================================================

    /**
     * bookCar()
     * ---------
     * Books a car for a customer for a given date range.
     * Before booking, checks if the car is available (no overlapping rentals).
     *
     * Overlap logic:
     *   An overlap exists when:
     *   existing.from_date <= requested.to_date
     *   AND
     *   existing.to_date >= requested.from_date
     *
     * @param carId      – ID of the car to book
     * @param customer   – customer's name
     * @param fromDate   – start date (YYYY-MM-DD)
     * @param toDate     – end date   (YYYY-MM-DD)
     */
    public void bookCar(int carId, String customer, String fromDate, String toDate) {

        // ---- Step 1: Check if the requested car exists ----
        String checkCarSql = "SELECT car_id FROM cars WHERE car_id = ?";

        // ---- Step 2: Check for overlapping active rentals ----
        // A rental is "active" if returned_on IS NULL (car not yet returned)
        // We look for any rental of this car where dates overlap with our request
        String overlapSql =
            "SELECT COUNT(*) FROM rentals " +
            "WHERE car_id = ? " +
            "AND returned_on IS NULL " +
            "AND from_date <= ? " +        // existing rental starts before our end date
            "AND to_date   >= ?";          // existing rental ends after our start date

        // ---- Step 3: Insert the new rental ----
        String insertSql =
            "INSERT INTO rentals (car_id, customer, from_date, to_date) " +
            "VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            // --- Check car exists ---
            try (PreparedStatement ps = con.prepareStatement(checkCarSql)) {
                ps.setInt(1, carId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    System.out.println("Car ID " + carId + " not found.");
                    return;  // exit method early
                }
            }

            // --- Check overlap ---
            try (PreparedStatement ps = con.prepareStatement(overlapSql)) {
                ps.setInt(1, carId);
                ps.setString(2, toDate);    // our toDate must be < existing fromDate
                ps.setString(3, fromDate);  // our fromDate must be > existing toDate
                ResultSet rs = ps.executeQuery();
                rs.next();  // COUNT(*) always returns exactly one row
                int count = rs.getInt(1);  // get the count value

                if (count > 0) {
                    // Car is already booked during the requested period
                    System.out.println("Car is NOT available for the selected dates.");
                    return;  // exit method early, don't book
                }
            }

            // --- Insert rental ---
            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                ps.setInt(1, carId);
                ps.setString(2, customer);
                ps.setString(3, fromDate);
                ps.setString(4, toDate);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("Car booked successfully for " + customer + "!");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error booking car: " + e.getMessage());
        }
    }

    // =====================================================================
    // FEATURE 3: RETURN CAR
    // =====================================================================

    /**
     * returnCar()
     * -----------
     * Marks a rental as returned (sets returned_on = TODAY),
     * then calculates and displays the total charge.
     *
     * Charge formula:
     *   days  = DATEDIFF(returned_on, from_date)   [MySQL function]
     *   total = days * rate_day
     *
     * @param rentId – the ID of the rental record to close
     */
    public void returnCar(int rentId) {

        // Get today's date from Java (alternatively could use MySQL's CURDATE())
        String today = LocalDate.now().toString();  // format: "YYYY-MM-DD"

        // ---- Step 1: Update returned_on to today ----
        String updateSql =
            "UPDATE rentals SET returned_on = ? WHERE rent_id = ? AND returned_on IS NULL";
        //  ^--- Only update if returned_on is still NULL (not already returned)

        // ---- Step 2: Fetch rental + car rate to calculate charge ----
        String fetchSql =
            "SELECT r.from_date, r.returned_on, c.rate_day, r.customer " +
            "FROM rentals r " +
            "JOIN cars c ON r.car_id = c.car_id " +  // JOIN to get rate_day from cars table
            "WHERE r.rent_id = ?";

        try (Connection con = DBConnection.getConnection()) {

            // --- Update the return date ---
            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setString(1, today);
                ps.setInt(2, rentId);
                int rows = ps.executeUpdate();

                if (rows == 0) {
                    // Either rentId doesn't exist OR car already returned
                    System.out.println("Rental ID not found or already returned.");
                    return;
                }
            }

            try (PreparedStatement ps = con.prepareStatement(fetchSql)) {
                ps.setInt(1, rentId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String fromDate    = rs.getString("from_date");
                    String returnedOn  = rs.getString("returned_on");
                    double ratePerDay  = rs.getDouble("rate_day");
                    String customerName = rs.getString("customer");
                    LocalDate start  = LocalDate.parse(fromDate);
                    LocalDate end    = LocalDate.parse(returnedOn);
                    long days        = end.toEpochDay() - start.toEpochDay();  // difference in days
                    if (days <= 0) days = 1;

                    double totalCharge = days * ratePerDay;

                    // Print the receipt
                    System.out.println("\n===== Return Receipt =====");
                    System.out.println("Customer   : " + customerName);
                    System.out.println("Rental ID  : " + rentId);
                    System.out.println("From       : " + fromDate);
                    System.out.println("Returned   : " + returnedOn);
                    System.out.printf ("Days       : %d%n", days);
                    System.out.printf ("Rate       : Rs %.2f/day%n", ratePerDay);
                    System.out.printf ("Total      : Rs %.2f%n", totalCharge);
                    System.out.println("==========================\n");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error returning car: " + e.getMessage());
        }
    }
    public void listCarsOnRent() {

        String sql =
            "SELECT r.rent_id, c.model, c.reg_no, r.customer, " +
            "       r.from_date, r.to_date " +
            "FROM rentals r " +
            "JOIN cars c ON r.car_id = c.car_id " +
            "WHERE r.returned_on IS NULL " +
            "ORDER BY r.to_date ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {  // executeQuery() for SELECT statements

            System.out.println("\n====== Cars Currently On Rent ======");
            System.out.printf("%-10s %-20s %-15s %-20s %-12s %-12s%n",
                              "RentID", "Model", "Reg No", "Customer", "From", "Due Back");
            System.out.println("-------------------------------------------------------------------------------------");

            boolean found = false;

            // rs.next() moves the cursor to the next row; returns false when no more rows
            while (rs.next()) {
                found = true;
                int    rentId   = rs.getInt("rent_id");
                String model    = rs.getString("model");
                String regNo    = rs.getString("reg_no");
                String customer = rs.getString("customer");
                String fromDate = rs.getString("from_date");
                String toDate   = rs.getString("to_date");

                // printf with %-Ns = left-aligned string in N-char wide column
                System.out.printf("%-10d %-20s %-15s %-20s %-12s %-12s%n",
                                  rentId, model, regNo, customer, fromDate, toDate);
            }

            if (!found) {
                System.out.println("No cars are currently on rent.");
            }

            System.out.println("=====================================\n");

        } catch (SQLException e) {
            System.out.println("Error fetching rentals: " + e.getMessage());
        }
    }
}
