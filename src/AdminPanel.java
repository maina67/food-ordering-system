import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AdminPanel {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminPanel::new);
    }

    private JFrame frame;
    private JTable foodTable;
    private DefaultTableModel foodTableModel;
    private JTextField foodNameField, foodPriceField, foodQuantityField;
    private JButton addButton, editButton, removeButton;

    public AdminPanel() {
        frame = new JFrame("Admin - Food Ordering System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Table Panel
        foodTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Availability", "Quantity"}, 0);
        foodTable = new JTable(foodTableModel);
        loadFoodItems(); // Load data from the database

        JScrollPane scrollPane = new JScrollPane(foodTable);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Admin Control Panel (Buttons)
        JPanel controlPanel = new JPanel(new FlowLayout());
        addButton = new JButton("Add Item");
        editButton = new JButton("Edit Item");
        removeButton = new JButton("Remove Item");

        controlPanel.add(addButton);
        controlPanel.add(editButton);
        controlPanel.add(removeButton);
        frame.add(controlPanel, BorderLayout.SOUTH);

        // Action Listeners
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addItem();
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editItem();
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeItem();
            }
        });

        frame.setVisible(true);
    }

    private void loadFoodItems() {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM FoodItems");

            foodTableModel.setRowCount(0); // Clear existing rows
            while (rs.next()) {
                foodTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getBoolean("availability") ? "Yes" : "No",
                        rs.getInt("quantity") // Display quantity
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading food items", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addItem() {
        foodNameField = new JTextField();
        foodPriceField = new JTextField();
        foodQuantityField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Food Name:"));
        panel.add(foodNameField);
        panel.add(new JLabel("Food Price:"));
        panel.add(foodPriceField);
        panel.add(new JLabel("Food Quantity:"));
        panel.add(foodQuantityField);

        int option = JOptionPane.showConfirmDialog(frame, panel, "Add New Food Item", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String name = foodNameField.getText();
            String priceText = foodPriceField.getText();
            String quantityText = foodQuantityField.getText();

            try {
                double price = Double.parseDouble(priceText);
                int quantity = Integer.parseInt(quantityText);
                addFoodItemToDatabase(name, price, quantity);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid price or quantity entered!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addFoodItemToDatabase(String name, double price, int quantity) {
        try (Connection conn = getConnection()) {
            String query = "INSERT INTO FoodItems (name, price, quantity, availability) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setInt(3, quantity);
            pstmt.setBoolean(4, true); // By default, mark the item as available
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(frame, "New item added successfully!");
            loadFoodItems();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error adding item to database!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editItem() {
        int selectedRow = foodTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select an item to edit!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int foodId = (int) foodTableModel.getValueAt(selectedRow, 0);
        String currentName = (String) foodTableModel.getValueAt(selectedRow, 1);
        double currentPrice = (double) foodTableModel.getValueAt(selectedRow, 2);
        int currentQuantity = (int) foodTableModel.getValueAt(selectedRow, 4);

        foodNameField = new JTextField(currentName);
        foodPriceField = new JTextField(String.valueOf(currentPrice));
        foodQuantityField = new JTextField(String.valueOf(currentQuantity));

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Food Name:"));
        panel.add(foodNameField);
        panel.add(new JLabel("Food Price:"));
        panel.add(foodPriceField);
        panel.add(new JLabel("Food Quantity:"));
        panel.add(foodQuantityField);

        int option = JOptionPane.showConfirmDialog(frame, panel, "Edit Food Item", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String newName = foodNameField.getText();
            String newPriceText = foodPriceField.getText();
            String newQuantityText = foodQuantityField.getText();
            try {
                double newPrice = Double.parseDouble(newPriceText);
                int newQuantity = Integer.parseInt(newQuantityText);
                updateFoodItemInDatabase(foodId, newName, newPrice, newQuantity);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid price or quantity entered!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateFoodItemInDatabase(int foodId, String newName, double newPrice, int newQuantity) {
        try (Connection conn = getConnection()) {
            String query = "UPDATE FoodItems SET name = ?, price = ?, quantity = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, newName);
            pstmt.setDouble(2, newPrice);
            pstmt.setInt(3, newQuantity);
            pstmt.setInt(4, foodId);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Item updated successfully!");
            loadFoodItems();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error updating item in database!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeItem() {
        int selectedRow = foodTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select an item to remove!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int foodId = (int) foodTableModel.getValueAt(selectedRow, 0);
        int option = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this item?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            deleteFoodItemFromDatabase(foodId);
        }
    }

    private void deleteFoodItemFromDatabase(int foodId) {
        try (Connection conn = getConnection()) {
            String query = "DELETE FROM FoodItems WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, foodId);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Item deleted successfully!");
            loadFoodItems();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error deleting item from database!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/FoodOrderingSystem";
        String user = "root";
        String password = "@Migelbanky00"; 
        return DriverManager.getConnection(url, user, password);
    }
}
