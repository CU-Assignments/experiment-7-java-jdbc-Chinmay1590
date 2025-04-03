
package mvc.model;

public class Student {
    private int studentId;
    private String name;
    private String department;
    private double marks;
    
    // Default constructor
    public Student() {
    }
    
    // Parameterized constructor
    public Student(int studentId, String name, String department, double marks) {
        this.studentId = studentId;
        this.name = name;
        this.department = department;
        this.marks = marks;
    }
    }
    }
    }
    
    // Getters and Setters
    public int getStudentId() {
        return studentId;
    }
    
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public double getMarks() {
        return marks;
    }
    
    public void setMarks(double marks) {
        this.marks = marks;
    }
    
    @Override
    public String toString() {
        return "Student ID: " + studentId + 
               ", Name: " + name + 
               ", Department: " + department + 
               ", Marks: " + marks;
    }
}

// Database Access Object
package mvc.dao;

import mvc.model.Student;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    // JDBC URL, username, and password of MySQL server
    private static final String URL = "jdbc:mysql://localhost:3306/school";
    private static final String USER = "root";
    private static final String PASSWORD = "password";
    
    // JDBC variables
    private Connection connection;
    
    // Constructor
    public StudentDAO() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establish the connection
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            
            // Create table if not exists
            createTableIfNotExists();
            
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }
    
    // Create table if it doesn't exist
    private void createTableIfNotExists() throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS Students (" +
                               "StudentID INT PRIMARY KEY AUTO_INCREMENT, " +
                               "Name VARCHAR(100) NOT NULL, " +
                               "Department VARCHAR(50) NOT NULL, " +
                               "Marks DOUBLE NOT NULL)";
        
        try (Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
        }
    }
    
    // Create a new student record
    public boolean createStudent(Student student) {
        String sql = "INSERT INTO Students (Name, Department, Marks) VALUES (?, ?, ?)";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, student.getName());
            preparedStatement.setString(2, student.getDepartment());
            preparedStatement.setDouble(3, student.getMarks());
            
            int affectedRows = preparedStatement.executeUpdate();
            
            if (affectedRows > 0) {
                // Get the generated ID and set it to the student object
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        student.setStudentId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error creating student: " + e.getMessage());
        }
        
        return false;
    }
    
    // Read all students
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM Students";
        
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            
            while (resultSet.next()) {
                Student student = new Student();
                student.setStudentId(resultSet.getInt("StudentID"));
                student.setName(resultSet.getString("Name"));
                student.setDepartment(resultSet.getString("Department"));
                student.setMarks(resultSet.getDouble("Marks"));
                
                students.add(student);
            }
            
        } catch (SQLException e) {
            System.out.println("Error retrieving students: " + e.getMessage());
        }
        
        return students;
    }
    
    // Read a specific student by ID
    public Student getStudentById(int studentId) {
        String sql = "SELECT * FROM Students WHERE StudentID = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, studentId);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Student student = new Student();
                    student.setStudentId(resultSet.getInt("StudentID"));
                    student.setName(resultSet.getString("Name"));
                    student.setDepartment(resultSet.getString("Department"));
                    student.setMarks(resultSet.getDouble("Marks"));
                    
                    return student;
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Error retrieving student: " + e.getMessage());
        }
        
        return null;
    }
    
    // Update an existing student
    public boolean updateStudent(Student student) {
        String sql = "UPDATE Students SET Name = ?, Department = ?, Marks = ? WHERE StudentID = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, student.getName());
            preparedStatement.setString(2, student.getDepartment());
            preparedStatement.setDouble(3, student.getMarks());
            preparedStatement.setInt(4, student.getStudentId());
            
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.out.println("Error updating student: " + e.getMessage());
        }
        
        return false;
    }
    
    // Delete a student
    public boolean deleteStudent(int studentId) {
        String sql = "DELETE FROM Students WHERE StudentID = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, studentId);
            
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.out.println("Error deleting student: " + e.getMessage());
        }
        
        return false;
    }
    
    // Close the database connection
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
}

// Controller class
package mvc.controller;

import mvc.model.Student;
import mvc.dao.StudentDAO;
import java.util.List;

public class StudentController {
    private StudentDAO studentDAO;
    
    public StudentController() {
        this.studentDAO = new StudentDAO();
    }
    
    // Add a new student
    public boolean addStudent(String name, String department, double marks) {
        Student newStudent = new Student(0, name, department, marks);
        return studentDAO.createStudent(newStudent);
    }
    
    // Get all students
    public List<Student> getAllStudents() {
        return studentDAO.getAllStudents();
    }
    
    // Get a specific student by ID
    public Student getStudent(int studentId) {
        return studentDAO.getStudentById(studentId);
    }
    
    // Update an existing student
    public boolean updateStudent(int studentId, String name, String department, double marks) {
        Student student = new Student(studentId, name, department, marks);
        return studentDAO.updateStudent(student);
    }
    
    // Delete a student
    public boolean deleteStudent(int studentId) {
        return studentDAO.deleteStudent(studentId);
    }
    
    // Close resources
    public void closeResources() {
        studentDAO.closeConnection();
    }
}

// View class
package mvc.view;

import mvc.controller.StudentController;
import mvc.model.Student;
import java.util.List;
import java.util.Scanner;
import java.util.InputMismatchException;

public class StudentView {
    private StudentController controller;
    private Scanner scanner;
    
    public StudentView() {
        controller = new StudentController();
        scanner = new Scanner(System.in);
    }
    
    // Display main menu and handle user input
    public void showMenu() {
        boolean exit = false;
        
        while (!exit) {
            System.out.println("\n=== Student Management System ===");
            System.out.println("1. Add new student");
            System.out.println("2. View all students");
            System.out.println("3. View student by ID");
            System.out.println("4. Update student");
            System.out.println("5. Delete student");
            System.out.println("6. Exit");
            System.out.println("===============================");
            
            try {
                System.out.print("Enter your choice: ");
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1:
                        addStudent();
                        break;
                    case 2:
                        viewAllStudents();
                        break;
                    case 3:
                        viewStudentById();
                        break;
                    case 4:
                        updateStudent();
                        break;
                    case 5:
                        deleteStudent();
                        break;
                    case 6:
                        exit = true;
                        System.out.println("Exiting the program...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        // Close resources before exiting
        controller.closeResources();
        scanner.close();
    }
    
    // Add a new student
    private void addStudent() {
        try {
            System.out.println("\n=== Add New Student ===");
            
            System.out.print("Enter student name: ");
            String name = scanner.nextLine().trim();
            
            System.out.print("Enter department: ");
            String department = scanner.nextLine().trim();
            
            System.out.print("Enter marks: ");
            double marks = Double.parseDouble(scanner.nextLine().trim());
            
            if (controller.addStudent(name, department, marks)) {
                System.out.println("Student added successfully!");
            } else {
                System.out.println("Failed to add student.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid marks format. Please enter a valid number.");
        }
    }
    
    // View all students
    private void viewAllStudents() {
        System.out.println("\n=== All Students ===");
        List<Student> students = controller.getAllStudents();
        
        if (students.isEmpty()) {
            System.out.println("No students found in the database.");
        } else {
            System.out.println("----------------------------------------------------------------------");
            System.out.printf("%-5s %-20s %-15s %-10s\n", "ID", "Name", "Department", "Marks");
            System.out.println("----------------------------------------------------------------------");
            
            for (Student student : students) {
                System.out.printf("%-5d %-20s %-15s %-10.2f\n", 
                        student.getStudentId(), 
                        student.getName(), 
                        student.getDepartment(),
                        student.getMarks());
            }
            System.out.println("----------------------------------------------------------------------");
        }
    }
    
    // View student by ID
    private void viewStudentById() {
        try {
            System.out.print("\nEnter student ID: ");
            int studentId = Integer.parseInt(scanner.nextLine().trim());
            
            Student student = controller.getStudent(studentId);
            
            if (student != null) {
                System.out.println("\n=== Student Details ===");
                System.out.println("ID: " + student.getStudentId());
                System.out.println("Name: " + student.getName());
                System.out.println("Department: " + student.getDepartment());
                System.out.println("Marks: " + student.getMarks());
            } else {
                System.out.println("Student with ID " + studentId + " not found.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Please enter a valid number.");
        }
    }
    
    // Update student
    private void updateStudent() {
        try {
            viewAllStudents();
            
            System.out.print("\nEnter student ID to update: ");
            int studentId = Integer.parseInt(scanner.nextLine().trim());
            
            Student existingStudent = controller.getStudent(studentId);
            
            if (existingStudent != null) {
                System.out.println("\n=== Update Student ===");
                System.out.println("Current Details: " + existingStudent);
                System.out.println("Enter new details (leave blank to keep current value):");
                
                System.out.print("Enter name [" + existingStudent.getName() + "]: ");
                String nameInput = scanner.nextLine().trim();
                String name = nameInput.isEmpty() ? existingStudent.getName() : nameInput;
                
                System.out.print("Enter department [" + existingStudent.getDepartment() + "]: ");
                String deptInput = scanner.nextLine().trim();
                String department = deptInput.isEmpty() ? existingStudent.getDepartment() : deptInput;
                
                System.out.print("Enter marks [" + existingStudent.getMarks() + "]: ");
                String marksInput = scanner.nextLine().trim();
                double marks = marksInput.isEmpty() ? existingStudent.getMarks() : Double.parseDouble(marksInput);
                
                if (controller.updateStudent(studentId, name, department, marks)) {
                    System.out.println("Student updated successfully!");
                } else {
                    System.out.println("Failed to update student.");
                }
                
            } else {
                System.out.println("Student with ID " + studentId + " not found.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format. Update canceled.");
        }
    }
    
    // Delete student
    private void deleteStudent() {
        try {
            viewAllStudents();
            
            System.out.print("\nEnter student ID to delete: ");
            int studentId = Integer.parseInt(scanner.nextLine().trim());
            
            Student existingStudent = controller.getStudent(studentId);
            
            if (existingStudent != null) {
                System.out.print("Are you sure you want to delete this student? (y/n): ");
                String confirm = scanner.nextLine().trim().toLowerCase();
                
                if (confirm.equals("y")) {
                    if (controller.deleteStudent(studentId)) {
                        System.out.println("Student deleted successfully!");
                    } else {
                        System.out.println("Failed to delete student.");
                    }
                } else {
                    System.out.println("Delete operation canceled.");
                }
                
            } else {
                System.out.println("Student with ID " + studentId + " not found.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Please enter a valid number.");
        }
    }
}

// Main Application class
package mvc;

import mvc.view.StudentView;

public class StudentManagementApp {
    public static void main(String[] args) {
        StudentView view = new StudentView();
        view.showMenu();
    }
}
