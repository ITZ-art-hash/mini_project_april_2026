package com.library.management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseSetup {
    
    // CHANGE THIS TO YOUR PASSWORD
    private static final String DB_URL = "jdbc:mysql://localhost:3306/?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "root"; 
    
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            // Load driver explicitly
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Connect to MySQL server
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
            
            // Create Database
            String sql = "CREATE DATABASE IF NOT EXISTS LibraryDB";
            stmt.executeUpdate(sql);
            System.out.println("Database 'LibraryDB' created successfully!");
            
            // Use the database
            stmt.executeUpdate("USE LibraryDB");
            
            // Create Books table
            sql = "CREATE TABLE IF NOT EXISTS Books (" +
                  "book_id INT PRIMARY KEY AUTO_INCREMENT, " +
                  "title VARCHAR(200) NOT NULL, " +
                  "author VARCHAR(100) NOT NULL, " +
                  "publisher VARCHAR(100), " +
                  "year INT, " +
                  "isbn VARCHAR(20) UNIQUE, " +
                  "copies_available INT DEFAULT 1, " +
                  "total_copies INT DEFAULT 1)";
            stmt.executeUpdate(sql);
            System.out.println("Table 'Books' created!");
            
            // Create Members table
            sql = "CREATE TABLE IF NOT EXISTS Members (" +
                  "member_id INT PRIMARY KEY AUTO_INCREMENT, " +
                  "name VARCHAR(100) NOT NULL, " +
                  "email VARCHAR(100) UNIQUE, " +
                  "phone VARCHAR(15), " +
                  "join_date DATE)";
            stmt.executeUpdate(sql);
            System.out.println("Table 'Members' created!");
            
            // Create IssuedBooks table
            sql = "CREATE TABLE IF NOT EXISTS IssuedBooks (" +
                  "issue_id INT PRIMARY KEY AUTO_INCREMENT, " +
                  "book_id INT NOT NULL, " +
                  "member_id INT NOT NULL, " +
                  "issue_date DATE, " +
                  "due_date DATE, " +
                  "return_date DATE, " +
                  "fine_amount DECIMAL(10,2) DEFAULT 0.00, " +
                  "FOREIGN KEY (book_id) REFERENCES Books(book_id) ON DELETE CASCADE, " +
                  "FOREIGN KEY (member_id) REFERENCES Members(member_id) ON DELETE CASCADE)";
            stmt.executeUpdate(sql);
            System.out.println("Table 'IssuedBooks' created!");
            
            // Try to create triggers (additional)
            createTriggerIfPossible(conn);
            
            // Insert sample data
            insertSampleData(conn);
            
            System.out.println("\n✅ Database setup complete!");
            System.out.println("Now run LibraryManagementSystem.java");
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL Driver not found!");
        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void insertSampleData(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        
        // Check if data already exists
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Books");
        rs.next();
        if (rs.getInt(1) > 0) {
            System.out.println("Sample data already exists, skipping...");
            return;
        }
        
        // Insert sample books
        String[] books = {
            "INSERT INTO Books (title, author, publisher, year, isbn, copies_available, total_copies) " +
            "VALUES ('The Great Gatsby', 'F. Scott Fitzgerald', 'Scribner', 1925, '9780743273565', 3, 3)",
            "INSERT INTO Books (title, author, publisher, year, isbn, copies_available, total_copies) " +
            "VALUES ('To Kill a Mockingbird', 'Harper Lee', 'J.B. Lippincott', 1960, '9780061120084', 2, 2)",
            "INSERT INTO Books (title, author, publisher, year, isbn, copies_available, total_copies) " +
            "VALUES ('1984', 'George Orwell', 'Secker & Warburg', 1949, '9780451524935', 4, 4)",
            "INSERT INTO Books (title, author, publisher, year, isbn, copies_available, total_copies) " +
            "VALUES ('Pride and Prejudice', 'Jane Austen', 'T. Egerton', 1813, '9780141439518', 2, 2)"
        };
        
        for (String sql : books) {
            stmt.executeUpdate(sql);
        }
        System.out.println("✅ Sample books added!");
        
        // Insert sample members
        String[] members = {
            "INSERT INTO Members (name, email, phone, join_date) VALUES ('John Doe', 'john@email.com', '9876543210', CURDATE())",
            "INSERT INTO Members (name, email, phone, join_date) VALUES ('Jane Smith', 'jane@email.com', '9876543211', CURDATE())",
            "INSERT INTO Members (name, email, phone, join_date) VALUES ('Bob Johnson', 'bob@email.com', '9876543212', CURDATE())"
        };
        
        for (String sql : members) {
            stmt.executeUpdate(sql);
        }
        System.out.println("✅ Sample members added!");
    }
    
    private static void createTriggerIfPossible(Connection conn) {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            
            // Try to create the decrease trigger
            try {
                stmt.executeUpdate("DROP TRIGGER IF EXISTS decrease_copies");
                String sql = "CREATE TRIGGER decrease_copies " +
                            "AFTER INSERT ON IssuedBooks " +
                            "FOR EACH ROW " +
                            "UPDATE Books SET copies_available = copies_available - 1 " +
                            "WHERE book_id = NEW.book_id";
                stmt.executeUpdate(sql);
                System.out.println("✅ Trigger 'decrease_copies' created!");
            } catch (SQLException e) {
                System.out.println("⚠️ Trigger creation skipped - using manual updates instead");
            }
            
            // Try to create the increase trigger
            try {
                stmt.executeUpdate("DROP TRIGGER IF EXISTS increase_copies");
                String sql = "CREATE TRIGGER increase_copies " +
                            "AFTER UPDATE ON IssuedBooks " +
                            "FOR EACH ROW " +
                            "BEGIN " +
                            "  IF NEW.return_date IS NOT NULL AND OLD.return_date IS NULL THEN " +
                            "    UPDATE Books SET copies_available = copies_available + 1 " +
                            "    WHERE book_id = NEW.book_id; " +
                            "  END IF; " +
                            "END";
                stmt.executeUpdate(sql);
                System.out.println("✅ Trigger 'increase_copies' created!");
            } catch (SQLException e) {
                System.out.println("⚠️ Increase trigger skipped - using manual updates instead");
            }
            
        } catch (SQLException e) {
            System.out.println("Trigger setup skipped");
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
} 