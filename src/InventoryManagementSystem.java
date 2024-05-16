import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.sql.*;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class InventoryManagementSystem {
    private JFrame frame;
    private JTextField nameField;
    private JTextField priceField;
    private JTextField stockField;
    private JList<String> itemList;
    private DefaultListModel<String> listModel;
    private JTextField stockUpdateField;
    private JTextField saleUnitsField;
    private Connection conn;

    public InventoryManagementSystem() {
        initialize();
        connectDatabase();
        createTable();
        refreshItemList();
    }

    private void initialize() {
        frame = new JFrame("POS & Inventory Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Item Name
        JLabel nameLabel = new JLabel("Item Name:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        frame.add(nameLabel, gbc);

        nameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        frame.add(nameField, gbc);

        // Item Price
        JLabel priceLabel = new JLabel("Item Price:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        frame.add(priceLabel, gbc);

        priceField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        frame.add(priceField, gbc);

        // Initial Stock Level
        JLabel stockLabel = new JLabel("Initial Stock Level:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        frame.add(stockLabel, gbc);

        stockField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        frame.add(stockField, gbc);

        // Add Item Button
        JButton addButton = new JButton("Add Item");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        frame.add(addButton, gbc);

        // Item List
        listModel = new DefaultListModel<>();
        itemList = new JList<>(listModel);
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(itemList);
        listScrollPane.setPreferredSize(new Dimension(400, 150));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        frame.add(listScrollPane, gbc);

        // Units to Add/Subtract
        JLabel stockUpdateLabel = new JLabel("Units to Add/Subtract:");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        frame.add(stockUpdateLabel, gbc);

        stockUpdateField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 5;
        frame.add(stockUpdateField, gbc);

        // Update Stock Button
        JButton updateStockButton = new JButton("Update Stock");
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        frame.add(updateStockButton, gbc);

        // Units Being Sold
        JLabel saleUnitsLabel = new JLabel("Units Being Sold:");
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        frame.add(saleUnitsLabel, gbc);

        saleUnitsField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 7;
        frame.add(saleUnitsField, gbc);

        // Make Sale Button
        JButton makeSaleButton = new JButton("Make Sale");
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        frame.add(makeSaleButton, gbc);

        // Add Action Listeners
        addButton.addActionListener(new AddItemListener());
        updateStockButton.addActionListener(new UpdateStockListener());
        makeSaleButton.addActionListener(new MakeSaleListener());

        frame.pack();
        frame.setVisible(true);
    }

    private void connectDatabase() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:data.db");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createTable() {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS items (" +
                    "id INTEGER PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "price REAL NOT NULL," +
                    "stock INTEGER NOT NULL)");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Failed to create table: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshItemList() {
        listModel.clear();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM items");
            while (rs.next()) {
                listModel.addElement(rs.getString("name") + " - Price: " + rs.getDouble("price") + ", Stock: " + rs.getInt("stock"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Failed to refresh item list: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class AddItemListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = nameField.getText();
            String priceStr = priceField.getText();
            String stockStr = stockField.getText();

            if (!name.isEmpty() && !priceStr.isEmpty() && !stockStr.isEmpty()) {
                try {
                    double price = Double.parseDouble(priceStr);
                    int stock = Integer.parseInt(stockStr);
                    try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO items (name, price, stock) VALUES (?, ?, ?)")) {
                        pstmt.setString(1, name);
                        pstmt.setDouble(2, price);
                        pstmt.setInt(3, stock);
                        pstmt.executeUpdate();
                        JOptionPane.showMessageDialog(frame, "The item has been added to the menu!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshItemList();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(frame, "Failed to add item: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter valid price and stock values.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class UpdateStockListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedIndex = itemList.getSelectedIndex();
            if (selectedIndex != -1) {
                try {
                    int units = Integer.parseInt(stockUpdateField.getText());
                    try (PreparedStatement pstmt = conn.prepareStatement("UPDATE items SET stock = stock + ? WHERE id = ?")) {
                        pstmt.setInt(1, units);
                        pstmt.setInt(2, selectedIndex + 1);
                        pstmt.executeUpdate();
                        JOptionPane.showMessageDialog(frame, "Stock level updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshItemList();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(frame, "Failed to update stock: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid number of units.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select an item from the list.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class MakeSaleListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedIndex = itemList.getSelectedIndex();
            if (selectedIndex != -1) {
                try {
                    int units = Integer.parseInt(saleUnitsField.getText());
                    try (PreparedStatement pstmt = conn.prepareStatement("SELECT stock, price, name FROM items WHERE id = ?")) {
                        pstmt.setInt(1, selectedIndex + 1);
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) {
                            int stock = rs.getInt("stock");
                            double price = rs.getDouble("price");
                            String name = rs.getString("name");
                            if (stock >= units) {
                                try (PreparedStatement updatePstmt = conn.prepareStatement("UPDATE items SET stock = stock - ? WHERE id = ?")) {
                                    updatePstmt.setInt(1, units);
                                    updatePstmt.setInt(2, selectedIndex + 1);
                                    updatePstmt.executeUpdate();
                                    double totalCost = units * price;
                                    JOptionPane.showMessageDialog(frame, "Sale successful! Total cost: " + totalCost, "Success", JOptionPane.INFORMATION_MESSAGE);
                                    refreshItemList();

                                    // Create invoice PDF
                                    createInvoice(units, totalCost, name);
                                } catch (SQLException ex) {
                                    JOptionPane.showMessageDialog(frame, "Failed to update stock: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            } else {
                                JOptionPane.showMessageDialog(frame, "Insufficient stock!", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(frame, "Failed to retrieve item data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid number of units.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select an item from the list.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void createInvoice(int units, double totalCost, String itemName) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream("invoice.pdf"));
            document.open();
            document.add(new Paragraph("Item: " + itemName));
            document.add(new Paragraph("Units Sold: " + units));
            document.add(new Paragraph("Total Cost: $" + totalCost));
            document.close();
            JOptionPane.showMessageDialog(frame, "Invoice created successfully! Filename: invoice.pdf", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Failed to create invoice: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InventoryManagementSystem());
    }
}
