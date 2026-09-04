import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class FoodOrderingSystem {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FoodOrderingSystem::new);
    }

    private JFrame frame;
    private JTable foodTable;
    private DefaultTableModel foodTableModel;
    private JTextField quantityField;
    private JLabel totalPriceLabel;

    // Cart-related fields
    private ArrayList<CartItem> cart = new ArrayList<>();
    private double totalPrice = 0;

    public FoodOrderingSystem() {
        frame = new JFrame("Food Ordering System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Table Panel
        foodTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Availability", "Quantity"}, 0);
        foodTable = new JTable(foodTableModel);
        loadFoodItems(); // Load data from the database

        JScrollPane scrollPane = new JScrollPane(foodTable);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Order Panel (Button to Add to Cart)
        JPanel orderPanel = new JPanel(new FlowLayout());
        quantityField = new JTextField(5); // TextField to enter quantity
        orderPanel.add(new JLabel("Quantity:"));
        orderPanel.add(quantityField);

        JButton addToCartButton = new JButton("Add to Cart");
        orderPanel.add(addToCartButton);

        JButton checkoutButton = new JButton("Checkout");
        orderPanel.add(checkoutButton);

        JButton viewCartButton = new JButton("View Cart");
        orderPanel.add(viewCartButton);

        frame.add(orderPanel, BorderLayout.SOUTH);

        // Total Price Label
        totalPriceLabel = new JLabel("Total Price: $0.00");
        frame.add(totalPriceLabel, BorderLayout.NORTH);

        // Action Listeners
        addToCartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addItemToCart();
            }
        });

        checkoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkout();
            }
        });

        viewCartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewCart();
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

    private void addItemToCart() {
        int selectedRow = foodTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select an item to add to the cart!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int foodId = (int) foodTableModel.getValueAt(selectedRow, 0);
        String foodName = (String) foodTableModel.getValueAt(selectedRow, 1);
        double price = (double) foodTableModel.getValueAt(selectedRow, 2);
        int availableQuantity = (int) foodTableModel.getValueAt(selectedRow, 4);

        // Get the quantity entered by the user
        int quantityToOrder = Integer.parseInt(quantityField.getText());

        if (quantityToOrder > availableQuantity) {
            JOptionPane.showMessageDialog(frame, "Not enough stock available!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (quantityToOrder > 0) {
            CartItem cartItem = new CartItem(foodId, foodName, price, quantityToOrder);
            cart.add(cartItem);

            totalPrice += price * quantityToOrder;
            totalPriceLabel.setText("Total Price: $" + totalPrice);

            // Update the database for the item quantity
            updateFoodQuantity(foodId, availableQuantity - quantityToOrder);

            JOptionPane.showMessageDialog(frame, foodName + " added to cart.");
        } else {
            JOptionPane.showMessageDialog(frame, "Please enter a valid quantity!", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void checkout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Your cart is empty!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Display the cart contents
        StringBuilder receipt = new StringBuilder("Receipt:\n\n");
        for (CartItem item : cart) {
            receipt.append("Item: ").append(item.getFoodName())
                    .append("\nQuantity: ").append(item.getQuantity())
                    .append("\nPrice: $").append(item.getPrice() * item.getQuantity())
                    .append("\n\n");
        }

        receipt.append("Total Price: $").append(totalPrice).append("\n\nThank you for your order!");

        JOptionPane.showMessageDialog(frame, receipt.toString(), "Checkout", JOptionPane.INFORMATION_MESSAGE);

        // Clear the cart and reset the total price
        cart.clear();
        totalPrice = 0;
        totalPriceLabel.setText("Total Price: $0.00");
    }

    private void viewCart() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Your cart is empty!", "Cart", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Display cart contents
        StringBuilder cartDetails = new StringBuilder("Your Cart:\n\n");
        for (CartItem item : cart) {
            cartDetails.append("Item: ").append(item.getFoodName())
                    .append("\nQuantity: ").append(item.getQuantity())
                    .append("\nPrice: $").append(item.getPrice() * item.getQuantity())
                    .append("\n\n");
        }

        cartDetails.append("Total Price: $").append(totalPrice);

        // Allow removing items
        String input = JOptionPane.showInputDialog(frame, cartDetails.toString() + "\n\nEnter the ID of the item to remove, or 'cancel' to exit:");

        if (input != null && !input.equals("cancel")) {
            try {
                int itemIdToRemove = Integer.parseInt(input);
                removeItemFromCart(itemIdToRemove);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input. Please enter a valid item ID.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeItemFromCart(int foodIdToRemove) {
        for (CartItem item : cart) {
            if (item.getfoodId() == foodIdToRemove) {
                cart.remove(item);
                totalPrice -= item.getPrice() * item.getQuantity();
                totalPriceLabel.setText("Total Price: $" + totalPrice);
                JOptionPane.showMessageDialog(frame, "Item removed from the cart.");
                return;
            }
        }
        JOptionPane.showMessageDialog(frame, "Item not found in cart.", "Error", JOptionPane.WARNING_MESSAGE);
    }

    private void updateFoodQuantity(int foodId, int newQuantity) {
        try (Connection conn = getConnection()) {
            String query = "UPDATE FoodItems SET quantity = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, foodId);
            pstmt.executeUpdate();
            loadFoodItems(); // Reload items after quantity update
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error updating item quantity", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/FoodOrderingSystem";
        String user = "root"; // Replace with your MySQL username
        String password = "@Migelbanky00"; // Replace with your MySQL password
        return DriverManager.getConnection(url, user, password);
    }

    // CartItem class to hold items in the cart
    private static class CartItem {
        private int foodId;
        private String foodName;
        private double price;
        private int quantity;

        public CartItem(int foodId, String foodName, double price, int quantity) {
            this.foodId = foodId;
            this.foodName = foodName;
            this.price = price;
            this.quantity = quantity;
        }

        public int getfoodId() {
            return foodId;
        }

        public String getFoodName() {
            return foodName;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}
