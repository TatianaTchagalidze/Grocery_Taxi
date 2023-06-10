## Grocery Taxi
Grocery Taxi is a project aimed at connecting grocery consumers with couriers who transport food from stores to consumers. It provides a REST API for managing orders and a basic web UI for user interaction.

## Technologies Used 
### The Grocery Taxi project utilizes the following technologies: 
* Java with Spring Boot framework
* PostgreSQL database
* Docker for containerization
* Flyway for database migration
* Checkstyle for code style checking

## Prerequisites
* Docker installed on your machine
* Java Development Kit (JDK) installed

## SetUp:
1. Clone the repository to your local machine
2. Start the Docker containers for PostgreSQL and Flyway 
3. Build the project using Maven: ./mvnw clean install
4. To run the application, use the following command: mvn spring-boot:run 
5. To run the database migrations using Flyway, use the following command: mvn flyway:migrate
6. To run Checkstyle for code style checking, use the following command: mvn checkstyle:check





