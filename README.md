# Food Ordering & Inventory Management System

A desktop-based food ordering and menu management application built with **Java (Swing)** and **MySQL**. The system features a dual-interface architecture: an intuitive customer ordering portal with live cart management, and a secure administrative panel for real-time inventory and catalog control.

---

## 📌 Features

### 🛒 Customer Ordering Portal (`FoodOrderingSystem.java`)
- **Live Menu Browsing:** Displays current food items, pricing, availability, and stock quantities fetched dynamically from the database.
- **Interactive Cart System:** Add items with specific quantities, check live inventory limits, and view running cart sub-totals.
- **Stock Validation:** Real-time checking ensures orders cannot exceed current stock levels.
- **Cart Management:** Inspect items in the cart with the option to remove items before finalizing the order.
- **Automated Checkout & Receipt:** Generates an itemized receipt upon checkout, deducts stock quantities in the database, and clears the cart.

### 🛠️ Admin Control Panel (`AdminPanel.java`)
- **Menu Management (CRUD):**
  - **Add:** Insert new food items with name, unit price, and starting quantity.
  - **Edit:** Update item details directly through pre-filled dialog modals.
  - **Delete:** Remove discontinued items with confirmation prompts.
- **Real-Time Data Refresh:** Automatically reloads the catalog table after every database mutation.
- **Safe SQL Operations:** Employs parameterized queries (`PreparedStatement`) to prevent SQL injection vulnerabilities.

---

## 🧰 Tech Stack

- **Language:** Java (JDK 8+)
- **GUI Framework:** Java Swing (`JFrame`, `JTable`, `DefaultTableModel`, `JOptionPane`)
- **Database:** MySQL 8.x
- **Database Driver:** MySQL Connector/J (`mysql-connector-j-9.1.0`)
- **Architecture:** Client-Database / Event-Driven UI

---

## 📂 Project Structure

```text
foodorderingsystem/
├── .vscode/               # VS Code project configuration
├── bin/                   # Compiled bytecode (.class files)
├── lib/
│   └── mysql-connector-j-9.1.0/   # JDBC MySQL connector library
├── src/
│   ├── AdminPanel.java            # Admin management GUI
│   └── FoodOrderingSystem.java    # Customer ordering & cart GUI
├── .gitignore
└── README.md
```

---

## 🗄️ Database Setup

1. Launch your MySQL server (via MySQL Workbench, XAMPP, or MySQL CLI).
2. Execute the following SQL script to create the database and table:

```sql
CREATE DATABASE IF NOT EXISTS FoodOrderingSystem;
USE FoodOrderingSystem;

CREATE TABLE IF NOT EXISTS FoodItems (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DOUBLE NOT NULL,
    quantity INT NOT NULL,
    availability BOOLEAN DEFAULT TRUE
);

-- Seed sample menu items
INSERT INTO FoodItems (name, price, quantity, availability) VALUES
('Cheeseburger', 5.99, 25, TRUE),
('Pepperoni Pizza', 8.50, 15, TRUE),
('Crispy French Fries', 2.99, 40, TRUE),
('Grilled Chicken Sandwich', 6.75, 20, TRUE),
('Soda / Soft Drink', 1.50, 50, TRUE);
```

---

## ⚙️ Configuration

In both `src/FoodOrderingSystem.java` and `src/AdminPanel.java`, ensure your MySQL credentials match your local setup:

```java
private Connection getConnection() throws SQLException {
    String url = "jdbc:mysql://localhost:3306/FoodOrderingSystem";
    String user = "root";             // Your MySQL username
    String password = "YOUR_PASSWORD"; // Your MySQL password
    return DriverManager.getConnection(url, user, password);
}
```

---

## 🚀 Getting Started

### Prerequisites
- [Java Development Kit (JDK 8 or higher)](https://www.oracle.com/java/technologies/downloads/)
- [MySQL Server](https://dev.mysql.com/downloads/installer/)
- VS Code (with the *Extension Pack for Java*) or IntelliJ IDEA / Eclipse

### Running the Application

#### Option 1: Using VS Code
1. Open the project folder in VS Code.
2. Ensure the Java extension detects the MySQL jar in `lib/mysql-connector-j-9.1.0`.
3. Open either:
   - `src/FoodOrderingSystem.java` (Customer interface)
   - `src/AdminPanel.java` (Admin interface)
4. Click **Run** or press `F5`.

#### Option 2: Using Terminal / Command Line
Compile and run with the MySQL JDBC driver in the classpath:

**Windows (PowerShell):**
```powershell
# Compile
javac -cp ".;lib/mysql-connector-j-9.1.0/mysql-connector-j-9.1.0.jar" -d bin src/*.java

# Run Customer Portal
java -cp "bin;lib/mysql-connector-j-9.1.0/mysql-connector-j-9.1.0.jar" FoodOrderingSystem

# Run Admin Panel
java -cp "bin;lib/mysql-connector-j-9.1.0/mysql-connector-j-9.1.0.jar" AdminPanel
```

---

## 👤 Author

- **GitHub:** [@maina67](https://github.com/maina67)
