# Cafe Billing System — Java + JDBC + MySQL

## Setup (3 steps)

### 1. Database
Run `sql/schema.sql` in MySQL Workbench or terminal:
```sql
source /path/to/sql/schema.sql
```

### 2. JDBC Driver
- Download `mysql-connector-j-8.x.x.jar` from https://dev.mysql.com/downloads/connector/j/
- Rename it to `mysql-connector-j.jar`
- Place it in the `/lib` folder

### 3. Update credentials
Open `src/BillingSystem.java`, update line 12:
```java
static final String PASS = "your_mysql_password";
```

## Run

**Windows:** Double-click `run.bat`

**Linux/Mac:**
```bash
chmod +x run.sh && ./run.sh
```

**VS Code:** Open folder → press `F5` (requires Extension Pack for Java)

## Features
- New bill with item selection from menu
- Auto GST calculation (5%)
- Stock deduction on each sale
- Bill history with detail view

## Project Structure
```
RestaurantBilling/
├── src/BillingSystem.java   ← all code in one file
├── sql/schema.sql           ← run this first
├── lib/                     ← put mysql-connector-j.jar here
├── run.bat / run.sh
└── .vscode/
```
