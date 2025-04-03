import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class EasyLevelDBConnection {
    public static void main(String[] args) {
        // JDBC URL, username, and password of MySQL server
        String url = "jdbc:mysql://localhost:3306/company";
        String user = "root";
        String password = "password";
        
        // SQL query to fetch data from Employee table
        String query = "SELECT EmpID, Name, Salary FROM Employee";
        
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establish a connection to the database
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("Database connected successfully!");
            
            // Create a statement
            Statement statement = connection.createStatement();
            
            // Execute the query
            ResultSet resultSet = statement.executeQuery(query);
            
            // Process the result set
            System.out.println("\nEmployee Details:");
            System.out.println("----------------------------------------------------");
            System.out.printf("%-10s %-20s %-10s\n", "EmpID", "Name", "Salary");
            System.out.println("----------------------------------------------------");
            
            while (resultSet.next()) {
                int empId = resultSet.getInt("EmpID");
                String name = resultSet.getString("Name");
                double salary = resultSet.getDouble("Salary");
                
                System.out.printf("%-10d %-20s $%-10.2f\n", empId, name, salary);
            }
            System.out.println("----------------------------------------------------");
            
            // Close the resources
            resultSet.close();
            statement.close();
            connection.close();
            System.out.println("\nDatabase connection closed.");
            
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
