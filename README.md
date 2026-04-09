CAR RENTAL SYSTEM (JAVA + MYSQL)

---

## PROJECT OVERVIEW

This project is a console-based Car Rental System developed using Java and MySQL.
It allows users to manage car rentals through a menu-driven interface.

The system supports adding cars, booking cars, returning cars, and viewing cars currently on rent.

---

## FEATURES

1. Add Car

   * Add new cars with model, registration number, and daily rate

2. Book Car

   * Book a car for a customer with date range
   * Prevents double booking using overlap logic

3. Return Car

   * Marks car as returned
   * Calculates total rental cost based on number of days

4. View Cars on Rent

   * Displays all cars currently rented (not yet returned)

---

## TECHNOLOGIES USED

* Java (JDK 8 or above)
* MySQL Database
* JDBC (MySQL Connector/J)

---

## PROJECT STRUCTURE

Main.java
→ Entry point (menu-driven interface)

CarService.java
→ Contains all business logic and database operations

DBConnection.java
→ Handles database connection using JDBC

lib/
→ Contains MySQL Connector JAR file

schema.sql
→ SQL file to create database and tables

---

## DATABASE SETUP

1. Open MySQL Workbench or Command Line
2. Run the schema.sql file

OR manually run:
CREATE DATABASE car_rental_db;
USE car_rental_db;

(Create tables as defined in schema.sql)

---

## HOW TO RUN THE PROJECT

1. Place all Java files in the same folder

2. Ensure MySQL is running

3. Compile:
   javac -cp ".;lib/mysql-connector-j-9.6.0.jar" *.java

4. Run:
   java -cp ".;lib/mysql-connector-j-9.6.0.jar" Main

---

## CONFIGURATION

Update DB credentials in DBConnection.java:

URL: jdbc:mysql://localhost:3306/car_rental_db
USER: root
PASSWORD: your_password

---

## ASSUMPTIONS

* Dates must be entered in YYYY-MM-DD format
* Registration number is unique
* Minimum billing is 1 day

---

## FUTURE ENHANCEMENTS

* GUI using Java Swing / JavaFX
* User login system
* Search and filter cars
* Generate invoices (PDF)
* Online booking integration

---

## AUTHOR

Name: Sohana Singh
Course: MCA
Project: CAR RENTAL SYSTEM

---

## END OF FILE
