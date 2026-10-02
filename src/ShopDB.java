import java.util.Scanner;

public class ShopDB {
    private DBEngine engine;
    private Scanner sc;

    public ShopDB() {
        engine = new DBEngine("shopdata/");
        sc = new Scanner(System.in);
        initializeTables();
    }

    private void initializeTables() {
        engine.execute(
            "CREATE TABLE products (id,name,category,price,quantity)");
        engine.execute(
            "CREATE TABLE customers (id,name,phone,address)");
        engine.execute(
            "CREATE TABLE sales (id,product_id,customer_id,qty,total,date)");
    }

    public void run() {
        System.out.println("\n Welcome to Shop Manager");
        while (true) {
            printMainMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": productMenu(); break;
                case "2": customerMenu(); break;
                case "3": salesMenu(); break;
                case "4": reportsMenu(); break;
                case "5":
                    System.out.println("Goodbye!"); return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n===========================");
        System.out.println(" MAIN MENU ");
        System.out.println("===========================");
        System.out.println(" 1. Product Management ");
        System.out.println(" 2. Customer Management ");
        System.out.println(" 3. Sales Management ");
        System.out.println(" 4. Reports ");
        System.out.println(" 5. Exit ");
        System.out.println("===========================");
        System.out.print("Enter choice: ");
    }

    // ── PRODUCT MENU ────────────────────────────────
    private void productMenu() {
        while (true) {
            System.out.println("\n PRODUCT MANAGEMENT");
            System.out.println("1. Add Product");
            System.out.println("2. View All Products");
            System.out.println("3. Search Product");
            System.out.println("4. Update Price");
            System.out.println("5. Update Stock");
            System.out.println("6. Delete Product");
            System.out.println("7. Back");
            System.out.print("Choice: ");
            switch (sc.nextLine().trim()) {
                case "1": addProduct(); break;
                case "2": viewProducts(); break;
                case "3": searchProduct(); break;
                case "4": updatePrice(); break;
                case "5": updateStock(); break;
                case "6": deleteProduct(); break;
                case "7": return;
                default: System.out.println("Invalid.");
            }
        }
    }

    private void addProduct() {
        System.out.println("\n ADD NEW PRODUCT");
        System.out.print("ID : "); String id = sc.nextLine().trim();
        System.out.print("Name : "); String name = sc.nextLine().trim();
        System.out.print("Category : "); String cat = sc.nextLine().trim();
        System.out.print("Price : "); String price= sc.nextLine().trim();
        System.out.print("Quantity : "); String qty = sc.nextLine().trim();
        System.out.println(engine.execute(
            "INSERT INTO products VALUES (" +
            id+","+name+","+cat+","+price+","+qty+")"));
    }

    private void viewProducts() {
        System.out.println("\n ALL PRODUCTS");
        engine.execute("SELECT * FROM products");
    }

    private void searchProduct() {
        System.out.print("Product name: ");
        String name = sc.nextLine().trim();
        engine.execute(
            "SELECT * FROM products WHERE name=" + name);
    }

    private void updatePrice() {
        System.out.print("Product ID : "); String id = sc.nextLine().trim();
        System.out.print("New Price : "); String price = sc.nextLine().trim();
        System.out.println(engine.execute(
            "UPDATE products SET price=" + price + " WHERE id=" + id));
    }

    private void updateStock() {
        System.out.print("Product ID : "); String id = sc.nextLine().trim();
        System.out.print("New Quantity : "); String qty = sc.nextLine().trim();
        System.out.println(engine.execute(
            "UPDATE products SET quantity=" + qty + " WHERE id=" + id));
    }

    private void deleteProduct() {
        System.out.print("Product ID: "); String id = sc.nextLine().trim();
        System.out.print("Confirm? (yes/no): ");
        if (sc.nextLine().trim().equalsIgnoreCase("yes"))
            System.out.println(engine.execute(
                "DELETE FROM products WHERE id=" + id));
        else System.out.println("Cancelled.");
    }

    // ── CUSTOMER MENU ───────────────────────────────
    private void customerMenu() {
        while (true) {
            System.out.println("\n CUSTOMER MANAGEMENT");
            System.out.println("1. Add Customer");
            System.out.println("2. View All");
            System.out.println("3. Search");
            System.out.println("4. Delete");
            System.out.println("5. Back");
            System.out.print("Choice: ");
            switch (sc.nextLine().trim()) {
                case "1": addCustomer(); break;
                case "2": viewCustomers(); break;
                case "3": searchCustomer(); break;
                case "4": deleteCustomer(); break;
                case "5": return;
                default: System.out.println("Invalid.");
            }
        }
    }

    private void addCustomer() {
        System.out.print("ID : "); String id = sc.nextLine().trim();
        System.out.print("Name : "); String name = sc.nextLine().trim();
        System.out.print("Phone : "); String ph = sc.nextLine().trim();
        System.out.print("Address : "); String addr = sc.nextLine().trim();
        System.out.println(engine.execute(
            "INSERT INTO customers VALUES (" +
            id+","+name+","+ph+","+addr+")"));
    }

    private void viewCustomers() {
        engine.execute("SELECT * FROM customers");
    }

    private void searchCustomer() {
        System.out.print("Name: ");
        engine.execute("SELECT * FROM customers WHERE name="
            + sc.nextLine().trim());
    }

    private void deleteCustomer() {
        System.out.print("Customer ID: ");
        System.out.println(engine.execute(
            "DELETE FROM customers WHERE id=" + sc.nextLine().trim()));
    }

    // ── SALES MENU ──────────────────────────────────
    private void salesMenu() {
        while (true) {
            System.out.println("\n SALES MANAGEMENT");
            System.out.println("1. Record New Sale");
            System.out.println("2. View All Sales");
            System.out.println("3. Back");
            System.out.print("Choice: ");
            switch (sc.nextLine().trim()) {
                case "1": recordSale(); break;
                case "2": viewSales(); break;
                case "3": return;
            }
        }
    }

    private void recordSale() {
        System.out.print("Sale ID : "); String id = sc.nextLine().trim();
        System.out.print("Product ID : "); String pid = sc.nextLine().trim();
        System.out.print("Customer ID : "); String cid = sc.nextLine().trim();
        System.out.print("Quantity Sold : "); String qty = sc.nextLine().trim();
        System.out.print("Total Amount : "); String total = sc.nextLine().trim();
        System.out.print("Date (YYYY-MM-DD): "); String date = sc.nextLine().trim();
        System.out.println(engine.execute(
            "INSERT INTO sales VALUES (" +
            id+","+pid+","+cid+","+qty+","+total+","+date+")"));
    }

    private void viewSales() {
        engine.execute("SELECT * FROM sales");
    }

    // ── REPORTS MENU ────────────────────────────────
    private void reportsMenu() {
        System.out.println("\n REPORTS");
        System.out.println("1. All Products");
        System.out.println("2. All Customers");
        System.out.println("3. All Sales");
        System.out.print("Choice: ");
        switch (sc.nextLine().trim()) {
            case "1": engine.execute("SELECT * FROM products"); break;
            case "2": engine.execute("SELECT * FROM customers"); break;
            case "3": engine.execute("SELECT * FROM sales"); break;
        }
    }

    public static void main(String[] args) {
        new ShopDB().run();
    }
}
