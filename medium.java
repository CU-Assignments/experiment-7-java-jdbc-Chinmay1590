import java.sql.*;
import java.util.Scanner;

public class MediumLevelCRUDOperations {
    // JDBC URL, username, and password of MySQL server
    private static final String URL = "jdbc:mysql://localhost:3306/store";
    private static final String USER = "root";
    private static final String PASSWORD = "password";
    
    // Connection and Statement objects
    private static Connection connection = null;
    private static Statement statement = null;
    private static PreparedStatement preparedStatement = null;
    private static ResultSet resultSet = null;
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establish a connection to the database
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connected successfully!");
            
            boolean exit = false;
            while (!exit) {
                displayMenu();
                int choice = getIntInput("Enter your choice: ");
                
                switch (choice) {
                    case 1:
                        createProduct();
                        break;
                    case 2:
                        readAllProducts();
                        break;
                    case 3:
                        updateProduct();
                        break;
                    case 4:
                        deleteProduct();
                        break;
                    case 5:
                        exit = true;
                        System.out.println("Exiting the program...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
            
            // Close resources
            if (connection != null) connection.close();
            if (scanner != null) scanner.close();
            
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void displayMenu() {
        System.out.println("\n=== Product Management System ===");
        System.out.println("1. Add new product");
        System.out.println("2. View all products");
        System.out.println("3. Update a product");
        System.out.println("4. Delete a product");
        System.out.println("5. Exit");
        System.out.println("===============================");
    }
    
    private static void createProduct() {
        try {
            String productName = getStringInput("Enter product name: ");
            double price = getDoubleInput("Enter price: ");
            int quantity = getIntInput("Enter quantity: ");
            
            String sql = "INSERT INTO Product (ProductName, Price, Quantity) VALUES (?, ?, ?)";
            
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, productName);
            preparedStatement.setDouble(2, price);
            preparedStatement.setInt(3, quantity);
            
            int rowsAffected = preparedStatement.executeUpdate();
            
            if (rowsAffected > 0) {
                connection.commit();
                System.out.println("Product added successfully!");
            } else {
                connection.rollback();
                System.out.println("Failed to add product.");
            }
            
            connection.setAutoCommit(true);
            preparedStatement.close();
            
        } catch (SQLException e) {
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println("Error creating product: " + e.getMessage());
        }
    }
    
    private static void readAllProducts() {
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM Product");
            
            System.out.println("\nProduct List:");
            System.out.println("-----------------------------------------------------------------------");
            System.out.printf("%-10s %-25s %-10s %-10s\n", "ID", "Name", "Price", "Quantity");
            System.out.println("-----------------------------------------------------------------------");
            
            boolean found = false;
            while (resultSet.next()) {
                found = true;
                int id = resultSet.getInt("ProductID");
                String name = resultSet.getString("ProductName");
                double price = resultSet.getDouble("Price");
                int quantity = resultSet.getInt("Quantity");
                
                System.out.printf("%-10d %-25s $%-10.2f %-10d\n", id, name, price, quantity);
            }
            
            if (!found) {
                System.out.println("No products found in the database.");
            }
            
            System.out.println("-----------------------------------------------------------------------");
            resultSet.close();
            statement.close();
            
        } catch (SQLException e) {
            System.out.println("Error reading products: " + e.getMessage());
        }
    }
    
    private static void updateProduct() {
        try {
            readAllProducts();
            
            int productId = getIntInput("\nEnter product ID to update: ");
            
            // Check if product exists
            preparedStatement = connection.prepareStatement("SELECT * FROM Product WHERE ProductID = ?");
            preparedStatement.setInt(1, productId);
            resultSet = preparedStatement.executeQuery();
            
            if (!resultSet.next()) {
                System.out.println("Product with ID " + productId + " not found.");
                return;
            }
            
            // Product exists, allow update
            System.out.println("\nUpdating Product ID: " + productId);
            System.out.println("Leave field empty to keep current value");
            
            String currentName = resultSet.getString("ProductName");
            double currentPrice = resultSet.getDouble("Price");
            int currentQuantity = resultSet.getInt("Quantity");
            
            System.out.println("Current Name: " + currentName);
            String newName = getStringInputAllowEmpty("Enter new name: ");
            if (newName.isEmpty()) newName = currentName;
            
            System.out.println("Current Price: $" + currentPrice);
            String priceInput = getStringInputAllowEmpty("Enter new price: ");
            double newPrice = priceInput.isEmpty() ? currentPrice : Double.parseDouble(priceInput);
            
            System.out.println("Current Quantity: " + currentQuantity);
            String quantityInput = getStringInputAllowEmpty("Enter new quantity: ");
            int newQuantity = quantityInput.isEmpty() ? currentQuantity : Integer.parseInt(quantityInput);
            
            String sql = "UPDATE Product SET ProductName = ?, Price = ?, Quantity = ? WHERE ProductID = ?";
            
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newName);
            preparedStatement.setDouble(2, newPrice);
            preparedStatement.setInt(3, newQuantity);
            preparedStatement.setInt(4, productId);
            
            int rowsAffected = preparedStatement.executeUpdate();
            
            if (rowsAffected > 0) {
                connection.commit();
                System.out.println("Product updated successfully!");
            } else {
                connection.rollback();
                System.out.println("Failed to update product.");
            }
            
            connection.setAutoCommit(true);
            preparedStatement.close();
            resultSet.close();
            
        } catch (SQLException e) {
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println("Error updating product: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format. Update canceled.");
        }
    }
    
    private static void deleteProduct() {
        try {
            readAllProducts();
            
            int productId = getIntInput("\nEnter product ID to delete: ");
            
            String confirmDelete = getStringInput("Are you sure you want to delete this product? (y/n): ");
            if (!confirmDelete.equalsIgnoreCase("y")) {
                System.out.println("Delete operation canceled.");
                return;
            }
            
            String sql = "DELETE FROM Product WHERE ProductID = ?";
            
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, productId);
            
            int rowsAffected = preparedStatement.executeUpdate();
            
            if (rowsAffected > 0) {
                connection.commit();
                System.out.println("Product deleted successfully!");
            } else {
                connection.rollback();
                System.out.println("Product with ID " + productId + " not found.");
            }
            
            connection.setAutoCommit(true);
            preparedStatement.close();
            
        } catch (SQLException e) {
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println("Error deleting product: " + e.getMessage());
        }
    }
    
    // Helper methods for input handling
    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    private static String getStringInputAllowEmpty(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }
    
    private static double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }
}
