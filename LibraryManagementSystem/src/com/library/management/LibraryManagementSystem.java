package com.library.management;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class LibraryManagementSystem extends JFrame {
    private JTabbedPane tabbedPane;
    private JTable booksTable;
    private JTable membersTable;
    private JTable issuedBooksTable;
    private DefaultTableModel booksTableModel;
    private DefaultTableModel membersTableModel;
    private DefaultTableModel issuedBooksTableModel;
    
    // Dashboard labels
    private JLabel totalBooksLabel;
    private JLabel totalMembersLabel;
    private JLabel booksIssuedLabel;
    private JLabel overdueBooksLabel;
    
    public LibraryManagementSystem() {
        setTitle("Library Management System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Add tabs
        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("Manage Books", createBooksPanel());
        tabbedPane.addTab("Manage Members", createMembersPanel());
        tabbedPane.addTab("Issue/Return", createIssueReturnPanel());
        
        add(tabbedPane);
        
        // Refresh data when tab changes
        tabbedPane.addChangeListener(e -> refreshCurrentTab());
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setPreferredSize(new Dimension(800, 200));
        
        totalBooksLabel = createStatCard("Total Books", "0", new Color(52, 152, 219));
        totalMembersLabel = createStatCard("Total Members", "0", new Color(46, 204, 113));
        booksIssuedLabel = createStatCard("Books Issued", "0", new Color(155, 89, 182));
        overdueBooksLabel = createStatCard("Overdue Books", "0", new Color(231, 76, 60));
        
        statsPanel.add(totalBooksLabel.getParent());
        statsPanel.add(totalMembersLabel.getParent());
        statsPanel.add(booksIssuedLabel.getParent());
        statsPanel.add(overdueBooksLabel.getParent());
        
        panel.add(statsPanel, BorderLayout.NORTH);
        
        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome to Library Management System", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(welcomeLabel, BorderLayout.CENTER);
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh Statistics");
        refreshButton.addActionListener(e -> refreshDashboard());
        panel.add(refreshButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JLabel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        valueLabel.setName(title); // Set name for identification
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return valueLabel;
    }
    
    private JPanel createBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel for add book
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBookButton = new JButton("Add New Book");
        addBookButton.addActionListener(e -> showAddBookDialog());
        
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        
        addPanel.add(new JLabel("Search:"));
        addPanel.add(searchField);
        addPanel.add(searchButton);
        addPanel.add(addBookButton);
        
        // Table for books
        String[] columns = {"ID", "Title", "Author", "Publisher", "Year", "ISBN", "Available", "Total"};
        booksTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        booksTable = new JTable(booksTableModel);
        JScrollPane scrollPane = new JScrollPane(booksTable);
        
        // Bottom panel with actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton deleteButton = new JButton("Delete Selected Book");
        deleteButton.addActionListener(e -> deleteSelectedBook());
        JButton refreshButton = new JButton("Refresh List");
        refreshButton.addActionListener(e -> refreshBooksTable());
        
        actionPanel.add(deleteButton);
        actionPanel.add(refreshButton);
        
        // Search action
        searchButton.addActionListener(e -> searchBooks(searchField.getText()));
        searchField.addActionListener(e -> searchBooks(searchField.getText()));
        
        panel.add(addPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createMembersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel for add member
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addMemberButton = new JButton("Add New Member");
        addMemberButton.addActionListener(e -> showAddMemberDialog());
        addPanel.add(addMemberButton);
        
        // Table for members
        String[] columns = {"ID", "Name", "Email", "Phone", "Join Date"};
        membersTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        membersTable = new JTable(membersTableModel);
        JScrollPane scrollPane = new JScrollPane(membersTable);
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh List");
        refreshButton.addActionListener(e -> refreshMembersTable());
        
        panel.add(addPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(refreshButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createIssueReturnPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Issue Panel
        JPanel issuePanel = new JPanel(new GridBagLayout());
        issuePanel.setBorder(BorderFactory.createTitledBorder("Issue Book"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField bookIdField = new JTextField(10);
        JTextField memberIdField = new JTextField(10);
        JButton issueButton = new JButton("Issue Book");
        
        gbc.gridx = 0; gbc.gridy = 0;
        issuePanel.add(new JLabel("Book ID:"), gbc);
        gbc.gridx = 1;
        issuePanel.add(bookIdField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        issuePanel.add(new JLabel("Member ID:"), gbc);
        gbc.gridx = 1;
        issuePanel.add(memberIdField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        issuePanel.add(issueButton, gbc);
        
        // Issued Books Table
        String[] columns = {"Issue ID", "Book Title", "Member Name", "Issue Date", "Due Date", "Status"};
        issuedBooksTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        issuedBooksTable = new JTable(issuedBooksTableModel);
        JScrollPane scrollPane = new JScrollPane(issuedBooksTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Currently Issued Books"));
        
        // Return Panel
        JPanel returnPanel = new JPanel(new FlowLayout());
        JButton returnButton = new JButton("Return Selected Book");
        JButton refreshButton = new JButton("Refresh");
        
        returnPanel.add(returnButton);
        returnPanel.add(refreshButton);
        
        // Actions
        issueButton.addActionListener(e -> {
            try {
                int bookId = Integer.parseInt(bookIdField.getText());
                int memberId = Integer.parseInt(memberIdField.getText());
                String result = IssueReturn.issueBook(bookId, memberId);
                JOptionPane.showMessageDialog(this, result);
                if (result.startsWith("SUCCESS")) {
                    bookIdField.setText("");
                    memberIdField.setText("");
                    refreshIssuedBooksTable();
                    refreshDashboard();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric IDs!");
            }
        });
        
        returnButton.addActionListener(e -> {
            int selectedRow = issuedBooksTable.getSelectedRow();
            if (selectedRow >= 0) {
                int issueId = (int) issuedBooksTableModel.getValueAt(selectedRow, 0);
                String result = IssueReturn.returnBook(issueId);
                JOptionPane.showMessageDialog(this, result);
                refreshIssuedBooksTable();
                refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Please select a book to return!");
            }
        });
        
        refreshButton.addActionListener(e -> refreshIssuedBooksTable());
        
        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(issuePanel, BorderLayout.NORTH);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(returnPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void showAddBookDialog() {
        JDialog dialog = new JDialog(this, "Add New Book", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField titleField = new JTextField(20);
        JTextField authorField = new JTextField(20);
        JTextField publisherField = new JTextField(20);
        JTextField yearField = new JTextField(20);
        JTextField isbnField = new JTextField(20);
        JTextField copiesField = new JTextField(20);
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        panel.add(titleField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1;
        panel.add(authorField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Publisher:"), gbc);
        gbc.gridx = 1;
        panel.add(publisherField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1;
        panel.add(yearField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("ISBN:"), gbc);
        gbc.gridx = 1;
        panel.add(isbnField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Copies:"), gbc);
        gbc.gridx = 1;
        panel.add(copiesField, gbc);
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        saveButton.addActionListener(e -> {
            try {
                Book book = new Book(
                    titleField.getText(),
                    authorField.getText(),
                    publisherField.getText(),
                    Integer.parseInt(yearField.getText()),
                    isbnField.getText(),
                    Integer.parseInt(copiesField.getText())
                );
                
                if (book.addBook()) {
                    JOptionPane.showMessageDialog(dialog, "Book added successfully!");
                    dialog.dispose();
                    refreshBooksTable();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add book!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for Year and Copies!");
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void showAddMemberDialog() {
        JDialog dialog = new JDialog(this, "Add New Member", true);
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField nameField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        panel.add(phoneField, gbc);
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        saveButton.addActionListener(e -> {
            Member member = new Member(
                nameField.getText(),
                emailField.getText(),
                phoneField.getText()
            );
            
            if (member.addMember()) {
                JOptionPane.showMessageDialog(dialog, "Member added successfully!");
                dialog.dispose();
                refreshMembersTable();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to add member!");
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void refreshBooksTable() {
        booksTableModel.setRowCount(0);
        List<Book> books = Book.getAllBooks();
        for (Book book : books) {
            booksTableModel.addRow(new Object[]{
                book.getBookId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getYear(),
                book.getIsbn(),
                book.getCopiesAvailable(),
                book.getTotalCopies()
            });
        }
    }
    
    private void searchBooks(String searchTerm) {
        booksTableModel.setRowCount(0);
        List<Book> books = Book.searchBooks(searchTerm);
        for (Book book : books) {
            booksTableModel.addRow(new Object[]{
                book.getBookId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getYear(),
                book.getIsbn(),
                book.getCopiesAvailable(),
                book.getTotalCopies()
            });
        }
    }
    
    private void deleteSelectedBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow >= 0) {
            int bookId = (int) booksTableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this book?", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                if (Book.deleteBook(bookId)) {
                    JOptionPane.showMessageDialog(this, "Book deleted successfully!");
                    refreshBooksTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete book!");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a book to delete!");
        }
    }
    
    private void refreshMembersTable() {
        membersTableModel.setRowCount(0);
        List<Member> members = Member.getAllMembers();
        for (Member member : members) {
            membersTableModel.addRow(new Object[]{
                member.getMemberId(),
                member.getName(),
                member.getEmail(),
                member.getPhone(),
                member.getJoinDate()
            });
        }
    }
    
    private void refreshIssuedBooksTable() {
        issuedBooksTableModel.setRowCount(0);
        List<IssueReturn.IssuedBookInfo> issuedBooks = IssueReturn.getIssuedBooks();
        for (IssueReturn.IssuedBookInfo info : issuedBooks) {
            issuedBooksTableModel.addRow(new Object[]{
                info.getIssueId(),
                info.getBookTitle(),
                info.getMemberName(),
                info.getIssueDate(),
                info.getDueDate(),
                info.getStatus()
            });
        }
    }
    
    private void refreshDashboard() {
        totalBooksLabel.setText(String.valueOf(IssueReturn.getTotalBooksCount()));
        totalMembersLabel.setText(String.valueOf(IssueReturn.getTotalMembersCount()));
        booksIssuedLabel.setText(String.valueOf(IssueReturn.getIssuedBooksCount()));
        overdueBooksLabel.setText(String.valueOf(IssueReturn.getOverdueBooksCount()));
    }
    
    private void refreshCurrentTab() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        switch (selectedIndex) {
            case 0: refreshDashboard(); break;
            case 1: refreshBooksTable(); break;
            case 2: refreshMembersTable(); break;
            case 3: refreshIssuedBooksTable(); break;
        }
    }
    
    public static void main(String[] args) {
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            LibraryManagementSystem app = new LibraryManagementSystem();
            app.setVisible(true);
            app.refreshCurrentTab();
        });
    }
}