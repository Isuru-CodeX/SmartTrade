# SmartTrade

SmartTrade is a comprehensive phone marketplace web application built with a modern Java backend and a custom, lightweight, responsive frontend. It allows users to browse phones, manage wishlists and shopping carts, and (for approved sellers) list new phones for sale.

This project was developed for the Web Programming II (HF2L) module.

## 🚀 Key Features

* **User Authentication:** Secure sign-up, sign-in, and account verification flows.
* **Seller Accounts:** Approved sellers can create and manage product listings.
* **Product Catalog:** Advanced search, filtering (by brand, model, price, etc.), sorting, and pagination.
* **Shopping Cart & Wishlist:** Client-side storage (localStorage) for seamless cart and wishlist management.
* **Responsive Design:** Custom dark design system (ink, brand green, highlight) optimized for mobile and desktop, including mobile menus, filter drawers, and cart drawers.
* **Live Search:** Instant search overlay triggered by `/` or `Ctrl+K`.
* **Rich Product Descriptions:** Built-in rich text editor for sellers, with rigorous server-side HTML sanitization (XSS-safe).
* **Automated Emails:** HTML email template builder for verification and password reset emails.
* **Robust File Uploads:** Secure, collision-resistant product and profile image uploads stored locally.

## 🛠️ Tech Stack

### Backend
* **Language:** Java 17
* **Build Tool:** Maven 3.8+
* **Application Server:** Apache Tomcat 9.0.x (Requires `javax.*` namespace, **not Tomcat 10+**)
* **REST API:** Jersey 2 (JAX-RS)
* **ORM:** Hibernate 5.6
* **Database:** MySQL 8.x

### Frontend
* HTML5, CSS3, Vanilla JavaScript
* Fetch API with JSON for asynchronous communication
* Bundled Poppins font
* Zero heavy dependencies (jQuery, Slick, etc. removed)

## 📋 Prerequisites

Ensure you have the following installed on your system:
* JDK 17
* Maven 3.8+
* Apache Tomcat 9.0.x (must be 9.0.x)
* MySQL 8.x

## ⚙️ Configuration & Setup

The application configuration prioritizes Environment Variables over the `app.properties` file for sensitive data, managed via a custom `Env.java` utility.

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd smarttrade
   ```

2. **Database Setup:**
   Import the schema and initial data into your MySQL server:
   ```bash
   # Create schema + required lookup data (brands, models, etc.)
   mysql -u root -p < database/smarttrade.sql

   # OPTIONAL: Insert demo seller and 2 demo phones
   mysql -u root -p < database/smarttrade_demo_data.sql
   ```
   *Demo login (if demo script ran): `demo@smarttrade.lk` / `Demo@1234` (verified, approved seller).*

3. **Configure Database Connection:**
   Update `src/main/resources/hibernate.cfg.xml` with your MySQL connection details (JDBC url, user, password).
   *Note: `hbm2ddl.auto=update` is set to keep the schema in sync.*

4. **Configure Application Properties:**
   Edit `src/main/resources/app.properties` or set the following environment variables (using exact match or uppercase underscore format, e.g., `APP_URL`):
   * `app.url`: Must match the URL you deploy to (e.g., `http://localhost:8080/smarttrade/`).
   * SMTP Settings for emails (e.g., `mail.smtp.host`, `mail.smtp.port`, `mail.username`, `mail.password`).

## 🏗️ Build & Deploy

### Command Line
```bash
# Build the WAR file
mvn clean package

# Copy to Tomcat webapps directory
cp target/smarttrade.war $CATALINA_HOME/webapps/

# Start Tomcat (use startup.bat on Windows)
$CATALINA_HOME/bin/startup.sh
```
Open **http://localhost:8080/smarttrade/** in your browser.

### IntelliJ IDEA
1. Go to *Run > Edit configurations > Add New Configuration > Tomcat Server (Local)*.
2. Select your Tomcat **9** home directory.
3. In the Deployment tab, add the artifact `SmartTrade:war exploded`.
4. Set the Application context to `/smarttrade`.
*(Optional: Run `lk.jiat.smarttrade.Main` to start an embedded Tomcat 9 server. Ensure "Add dependencies with provided scope to classpath" is enabled in the run configuration).*

## 📁 Project Structure

```
smarttrade/
├── database/                # SQL scripts for schema and demo data
├── src/
│   ├── main/
│   │   ├── java/lk/jiat/smarttrade/
│   │   │   ├── controller/  # Jersey REST API endpoints (e.g., ProductController)
│   │   │   ├── dto/         # Data Transfer Objects
│   │   │   ├── entity/      # Hibernate JPA Entities (Product, User, etc.)
│   │   │   ├── service/     # Business logic
│   │   │   └── util/        # Utilities (Env.java, HibernateUtil.java)
│   │   ├── resources/
│   │   │   ├── app.properties      # App config
│   │   │   └── hibernate.cfg.xml   # DB config
│   │   └── webapp/
│   │       ├── WEB-INF/web.xml     # Servlet configurations
│   │       ├── assets/             # CSS, JS, Fonts, Images
│   │       ├── uploads/            # Uploaded product and profile images
│   │       └── *.html              # Frontend views
└── pom.xml                  # Maven dependencies and build config
```

## ⚠️ Known Limitations & Future Improvements

* **Storage:** Shopping cart and wishlist live in the browser (`localStorage`). There is currently no backend order processing or payment gateway integration. The checkout page validates the form and explains this limitation.
* **Seller Registration:** A user can only list products if a row exists in the `seller` table with status `APPROVED`. There is currently no self-service seller registration screen (the demo SQL script creates one).
* **Security:** Passwords are currently stored in plain text and returned by `api/profiles/user-profile`. They should be hashed (e.g., using BCrypt) before production use.
* **Credentials:** Database and SMTP credentials are in plain text config files. It is recommended to use environment variables for deployment.

## 📝 Recent Updates

* **Complete Frontend Rework:** Replaced old Axil theme with a custom dark theme built from the logo palette. Removed heavy dependencies like jQuery and slick.
* **Backend Modernization:** Ensured strict compatibility with Tomcat 9 and JDK 17 (`jakarta.*` reverted to `javax.*`, Hibernate downgraded to 5.6.15, Jersey to 2.41).
* **Database Compatibility:** Implemented `SnakeCaseNamingStrategy` to maintain existing lower_snake_case table/column names across different OS file systems.
* **Bug Fixes:** Resolved account verification issues, absolute image path bugs, and NPEs in address handling.
