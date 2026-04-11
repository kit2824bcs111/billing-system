import java.sql.*;
import java.util.*;


public class BillingSystem {

    // ── DB CONFIG —  ──────────────────────────────
    static final String URL  = "jdbc:mysql://localhost:3306/cafe_billing?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    static final String USER = "root";
    static final String PASS = "sql123"; 
    // ─────────────────────────────────────────────────────────

    static Connection conn;
    static Scanner    sc = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        connect();
        System.out.println("\n╔═══════════════════════════════╗");
        System.out.println("║   Cafe Billing System (JDBC)  ║");
        System.out.println("╚═══════════════════════════════╝");

        boolean running = true;
        while (running) {
            System.out.println("\n[1] New Bill  [2] View Menu  [3] Bill History  [0] Exit");
            System.out.print("Choice: ");
            switch (sc.nextLine().trim()) {
                case "1" -> newBill();
                case "2" -> showMenu();
                case "3" -> billHistory();
                case "0" -> running = false;
                default  -> System.out.println("Invalid choice.");
            }
        }
        conn.close();
        System.out.println("Goodbye!");
    }

    // ── CONNECT ───────────────────────────────────────────────
    static void connect() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = DriverManager.getConnection(URL, USER, PASS);
        System.out.println("✅ Connected to MySQL.");
    }

    // ── NEW BILL ──────────────────────────────────────────────
    static void newBill() throws Exception {
        System.out.print("Customer Name: ");
        String customer = sc.nextLine().trim();
        if (customer.isEmpty()) customer = "Walk-in";

        List<int[]> order = new ArrayList<>(); // [itemId, qty]
        double subtotal   = 0;

        boolean ordering = true;
        while (ordering) {
            showMenu();
            System.out.print("Item ID (0 to finish): ");
            int id = Integer.parseInt(sc.nextLine().trim());
            if (id == 0) { ordering = false; continue; }

            MenuItem item = getMenuItem(id);
            if (item == null) { System.out.println("Item not found."); continue; }

            System.out.print("Quantity: ");
            int qty = Integer.parseInt(sc.nextLine().trim());
            order.add(new int[]{id, qty});
            subtotal += item.price * qty;
            System.out.printf("  ➕ %s x%d = ₹%.2f%n", item.name, qty, item.price * qty);
        }

        if (order.isEmpty()) { System.out.println("No items — bill cancelled."); return; }

        double tax   = subtotal * 0.05;
        double total = subtotal + tax;

        System.out.println("\n─────────── BILL ──────────────");
        System.out.printf("Customer : %s%n", customer);
        System.out.printf("Subtotal : ₹%.2f%n", subtotal);
        System.out.printf("GST (5%%) : ₹%.2f%n", tax);
        System.out.printf("TOTAL    : ₹%.2f%n", total);
        System.out.println("───────────────────────────────");

        System.out.print("Confirm & Save? [Y/N]: ");
        if (!sc.nextLine().trim().equalsIgnoreCase("Y")) {
            System.out.println("Bill discarded."); return;
        }

        // Save bill
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO bills (customer_name, total) VALUES (?,?)",
            Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, customer);
        ps.setDouble(2, total);
        ps.executeUpdate();
        int billId = ps.getGeneratedKeys().getInt(1);

        // Save items
        for (int[] entry : order) {
            MenuItem item = getMenuItem(entry[0]);
            PreparedStatement pi = conn.prepareStatement(
                "INSERT INTO bill_items (bill_id, item_name, quantity, unit_price) VALUES (?,?,?,?)");
            pi.setInt(1, billId);
            pi.setString(2, item.name);
            pi.setInt(3, entry[1]);
            pi.setDouble(4, item.price);
            pi.executeUpdate();

            // Reduce stock
            conn.prepareStatement(
                "UPDATE menu_items SET stock = stock - " + entry[1] + " WHERE id = " + entry[0])
                .executeUpdate();
        }

        System.out.printf("✅ Bill #%d saved! Total: ₹%.2f%n", billId, total);
    }

    // ── SHOW MENU ─────────────────────────────────────────────
    static void showMenu() throws Exception {
        System.out.printf("%n%-4s %-22s %-10s %-6s%n", "ID", "Name", "Price", "Stock");
        System.out.println("─".repeat(44));
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM menu_items ORDER BY id");
        while (rs.next()) {
            System.out.printf("%-4d %-22s ₹%-9.2f %-6d%n",
                rs.getInt("id"), rs.getString("name"),
                rs.getDouble("price"), rs.getInt("stock"));
        }
    }

    // ── BILL HISTORY ──────────────────────────────────────────
    static void billHistory() throws Exception {
        System.out.printf("%n%-5s %-18s %-12s %-10s%n", "ID", "Customer", "Total", "Date");
        System.out.println("─".repeat(50));
        ResultSet rs = conn.createStatement().executeQuery(
            "SELECT * FROM bills ORDER BY created_at DESC LIMIT 20");
        while (rs.next()) {
            System.out.printf("%-5d %-18s ₹%-11.2f %-10s%n",
                rs.getInt("id"),
                rs.getString("customer_name"),
                rs.getDouble("total"),
                rs.getString("created_at").substring(0, 10));
        }

        System.out.print("\nView bill details? Enter Bill ID (0 to skip): ");
        int id = Integer.parseInt(sc.nextLine().trim());
        if (id > 0) printBillDetail(id);
    }

    static void printBillDetail(int billId) throws Exception {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM bill_items WHERE bill_id = ?");
        ps.setInt(1, billId);
        ResultSet rs = ps.executeQuery();
        System.out.println("\n── Bill #" + billId + " Items ──────────────");
        double sub = 0;
        while (rs.next()) {
            double lineTotal = rs.getInt("quantity") * rs.getDouble("unit_price");
            sub += lineTotal;
            System.out.printf("  %-20s x%-3d ₹%.2f%n",
                rs.getString("item_name"), rs.getInt("quantity"), lineTotal);
        }
        System.out.printf("  Subtotal: ₹%.2f | GST: ₹%.2f | Total: ₹%.2f%n",
            sub, sub * 0.05, sub * 1.05);
    }

    // ── HELPER ────────────────────────────────────────────────
    static MenuItem getMenuItem(int id) throws Exception {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM menu_items WHERE id = ?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return new MenuItem(rs.getInt("id"),
            rs.getString("name"), rs.getDouble("price"));
        return null;
    }

    record MenuItem(int id, String name, double price) {}
}
