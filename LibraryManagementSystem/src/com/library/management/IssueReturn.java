package com.library.management;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class IssueReturn {
    
    // ==================== INNER CLASS: IssuedBookInfo ====================
    public static class IssuedBookInfo {
        private int issueId;
        private String bookTitle;
        private String memberName;
        private Date issueDate;
        private Date dueDate;
        private String status;
        
        // Getters and Setters
        public int getIssueId() { return issueId; }
        public void setIssueId(int issueId) { this.issueId = issueId; }
        public String getBookTitle() { return bookTitle; }
        public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
        public String getMemberName() { return memberName; }
        public void setMemberName(String memberName) { this.memberName = memberName; }
        public Date getIssueDate() { return issueDate; }
        public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }
        public Date getDueDate() { return dueDate; }
        public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    // ==================== INNER CLASS: HistoryRecord ====================
    public static class HistoryRecord {
        private int issueId;
        private String bookTitle;
        private String memberName;
        private Date issueDate;
        private Date dueDate;
        private Date returnDate;
        private double fineAmount;
        private String status;
        
        // Getters and Setters
        public int getIssueId() { return issueId; }
        public void setIssueId(int issueId) { this.issueId = issueId; }
        public String getBookTitle() { return bookTitle; }
        public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
        public String getMemberName() { return memberName; }
        public void setMemberName(String memberName) { this.memberName = memberName; }
        public Date getIssueDate() { return issueDate; }
        public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }
        public Date getDueDate() { return dueDate; }
        public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
        public Date getReturnDate() { return returnDate; }
        public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }
        public double getFineAmount() { return fineAmount; }
        public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    // ==================== METHOD: issueBook ====================
    public static String issueBook(int bookId, int memberId) {
        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement issueCheckStmt = null;
        PreparedStatement issueStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            // First check if copies available
            String checkSql = "SELECT copies_available FROM Books WHERE book_id = ?";
            checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, bookId);
            rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                return "ERROR: Book not found!";
            }
            
            int availableCopies = rs.getInt("copies_available");
            System.out.println("DEBUG: Book ID " + bookId + " has " + availableCopies + " copies available");
            
            if (availableCopies <= 0) {
                return "ERROR: No copies available!";
            }
            
            // Check if member already has this book
            String checkIssueSql = "SELECT * FROM IssuedBooks WHERE book_id = ? AND member_id = ? AND return_date IS NULL";
            issueCheckStmt = conn.prepareStatement(checkIssueSql);
            issueCheckStmt.setInt(1, bookId);
            issueCheckStmt.setInt(2, memberId);
            ResultSet issueRs = issueCheckStmt.executeQuery();
            if (issueRs.next()) {
                return "ERROR: Member already has this book issued!";
            }
            
            // Start transaction
            conn.setAutoCommit(false);
            
            try {
                // Issue the book
                String issueSql = "INSERT INTO IssuedBooks (book_id, member_id, issue_date, due_date) VALUES (?, ?, CURDATE(), ?)";
                issueStmt = conn.prepareStatement(issueSql);
                LocalDate dueDate = LocalDate.now().plusDays(14);
                issueStmt.setInt(1, bookId);
                issueStmt.setInt(2, memberId);
                issueStmt.setDate(3, Date.valueOf(dueDate));
                int result1 = issueStmt.executeUpdate();
                
                // Decrease available copies
                String updateSql = "UPDATE Books SET copies_available = copies_available - 1 WHERE book_id = ? AND copies_available > 0";
                updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, bookId);
                int result2 = updateStmt.executeUpdate();
                
                if (result1 > 0 && result2 > 0) {
                    conn.commit();
                    System.out.println("DEBUG: Book issued successfully. New available copies: " + (availableCopies - 1));
                    return "SUCCESS: Book issued successfully!";
                } else {
                    conn.rollback();
                    return "ERROR: Failed to issue book.";
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        } finally {
            // Close all resources
            try {
                if (rs != null) rs.close();
                if (checkStmt != null) checkStmt.close();
                if (issueCheckStmt != null) issueCheckStmt.close();
                if (issueStmt != null) issueStmt.close();
                if (updateStmt != null) updateStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // ==================== METHOD: returnBook ====================
    public static String returnBook(int issueId) {
        Connection conn = null;
        PreparedStatement getStmt = null;
        PreparedStatement updateStmt = null;
        PreparedStatement bookUpdateStmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            // Get due date and book_id
            String getSql = "SELECT book_id, due_date FROM IssuedBooks WHERE issue_id = ? AND return_date IS NULL";
            getStmt = conn.prepareStatement(getSql);
            getStmt.setInt(1, issueId);
            rs = getStmt.executeQuery();
            
            if (!rs.next()) {
                return "ERROR: Issue record not found or already returned!";
            }
            
            int bookId = rs.getInt("book_id");
            Date dueDate = rs.getDate("due_date");
            LocalDate dueLocal = dueDate.toLocalDate();
            LocalDate returnLocal = LocalDate.now();
            
            long daysLate = ChronoUnit.DAYS.between(dueLocal, returnLocal);
            double fine = daysLate > 0 ? daysLate * 2.0 : 0.0;
            
            // Start transaction
            conn.setAutoCommit(false);
            
            try {
                // Update issue record
                String updateSql = "UPDATE IssuedBooks SET return_date = CURDATE(), fine_amount = ? WHERE issue_id = ?";
                updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setDouble(1, fine);
                updateStmt.setInt(2, issueId);
                int result1 = updateStmt.executeUpdate();
                
                // Increase available copies
                String bookUpdateSql = "UPDATE Books SET copies_available = copies_available + 1 WHERE book_id = ?";
                bookUpdateStmt = conn.prepareStatement(bookUpdateSql);
                bookUpdateStmt.setInt(1, bookId);
                int result2 = bookUpdateStmt.executeUpdate();
                
                if (result1 > 0 && result2 > 0) {
                    conn.commit();
                    if (fine > 0) {
                        return String.format("SUCCESS: Book returned! Fine: ₹%.2f", fine);
                    } else {
                        return "SUCCESS: Book returned on time!";
                    }
                } else {
                    conn.rollback();
                    return "ERROR: Failed to return book.";
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        } finally {
            try {
                if (rs != null) rs.close();
                if (getStmt != null) getStmt.close();
                if (updateStmt != null) updateStmt.close();
                if (bookUpdateStmt != null) bookUpdateStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // ==================== METHOD: getIssuedBooks ====================
    public static List<IssuedBookInfo> getIssuedBooks() {
        List<IssuedBookInfo> issuedBooks = new ArrayList<>();
        String sql = "SELECT ib.issue_id, b.title AS book_title, m.name AS member_name, " +
                     "ib.issue_date, ib.due_date, " +
                     "CASE WHEN ib.due_date < CURDATE() THEN 'OVERDUE' ELSE 'Active' END as status " +
                     "FROM IssuedBooks ib " +
                     "INNER JOIN Books b ON ib.book_id = b.book_id " +
                     "INNER JOIN Members m ON ib.member_id = m.member_id " +
                     "WHERE ib.return_date IS NULL " +
                     "ORDER BY ib.issue_id ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                IssuedBookInfo info = new IssuedBookInfo();
                info.setIssueId(rs.getInt("issue_id"));
                info.setBookTitle(rs.getString("book_title"));
                info.setMemberName(rs.getString("member_name"));
                info.setIssueDate(rs.getDate("issue_date"));
                info.setDueDate(rs.getDate("due_date"));
                info.setStatus(rs.getString("status"));
                issuedBooks.add(info);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return issuedBooks;
    }
    
    // ==================== METHOD: getTransactionHistory ====================
    public static List<HistoryRecord> getTransactionHistory() {
        List<HistoryRecord> history = new ArrayList<>();
        String sql = "SELECT ib.issue_id, b.title AS book_title, m.name AS member_name, " +
                     "ib.issue_date, ib.due_date, ib.return_date, ib.fine_amount, " +
                     "CASE " +
                     "  WHEN ib.return_date IS NOT NULL THEN 'Returned' " +
                     "  WHEN ib.due_date < CURDATE() THEN 'Overdue' " +
                     "  ELSE 'Active' " +
                     "END as status " +
                     "FROM IssuedBooks ib " +
                     "INNER JOIN Books b ON ib.book_id = b.book_id " +
                     "INNER JOIN Members m ON ib.member_id = m.member_id " +
                     "ORDER BY ib.issue_id DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                HistoryRecord record = new HistoryRecord();
                record.setIssueId(rs.getInt("issue_id"));
                record.setBookTitle(rs.getString("book_title"));
                record.setMemberName(rs.getString("member_name"));
                record.setIssueDate(rs.getDate("issue_date"));
                record.setDueDate(rs.getDate("due_date"));
                record.setReturnDate(rs.getDate("return_date"));
                record.setFineAmount(rs.getDouble("fine_amount"));
                record.setStatus(rs.getString("status"));
                history.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
    
    // ==================== METHOD: getHistoryByMember ====================
    public static List<HistoryRecord> getHistoryByMember(int memberId) {
        List<HistoryRecord> history = new ArrayList<>();
        String sql = "SELECT ib.issue_id, b.title AS book_title, m.name AS member_name, " +
                     "ib.issue_date, ib.due_date, ib.return_date, ib.fine_amount, " +
                     "CASE " +
                     "  WHEN ib.return_date IS NOT NULL THEN 'Returned' " +
                     "  WHEN ib.due_date < CURDATE() THEN 'Overdue' " +
                     "  ELSE 'Active' " +
                     "END as status " +
                     "FROM IssuedBooks ib " +
                     "INNER JOIN Books b ON ib.book_id = b.book_id " +
                     "INNER JOIN Members m ON ib.member_id = m.member_id " +
                     "WHERE ib.member_id = ? " +
                     "ORDER BY ib.issue_id DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, memberId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                HistoryRecord record = new HistoryRecord();
                record.setIssueId(rs.getInt("issue_id"));
                record.setBookTitle(rs.getString("book_title"));
                record.setMemberName(rs.getString("member_name"));
                record.setIssueDate(rs.getDate("issue_date"));
                record.setDueDate(rs.getDate("due_date"));
                record.setReturnDate(rs.getDate("return_date"));
                record.setFineAmount(rs.getDouble("fine_amount"));
                record.setStatus(rs.getString("status"));
                history.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
    
    // ==================== COUNT METHODS ====================
    public static int getTotalBooksCount() {
        String sql = "SELECT COUNT(*) FROM Books";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public static int getTotalMembersCount() {
        String sql = "SELECT COUNT(*) FROM Members";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public static int getIssuedBooksCount() {
        String sql = "SELECT COUNT(*) FROM IssuedBooks WHERE return_date IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public static int getOverdueBooksCount() {
        String sql = "SELECT COUNT(*) FROM IssuedBooks WHERE return_date IS NULL AND due_date < CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
}