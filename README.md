# SmartTrade

SmartTrade is a comprehensive phone marketplace web application built with a modern Java backend and a custom, lightweight, responsive frontend. It allows users to browse phones, manage wishlists and shopping carts, and (for approved sellers) list new phones for sale.

This project was developed for the Web Programming II (HF2L) module, providing a robust solution for buying and selling mobile devices.

## Features

- **User Authentication:** Secure sign-up, sign-in, and account verification flows.
- **Seller Accounts:** Approved sellers can create and manage product listings.
- **Product Catalog:** Advanced search, filtering (by brand, model, price, etc.), sorting, and pagination.
- **Shopping Cart & Wishlist:** Client-side storage (localStorage) for seamless cart and wishlist management.
- **Responsive Design:** Custom dark design system (ink, brand green, highlight) optimized for mobile and desktop, including mobile menus, filter drawers, and cart drawers.
- **Live Search:** Instant search overlay triggered by `/` or `Ctrl+K`.
- **Rich Product Descriptions:** Built-in rich text editor for sellers, with rigorous server-side HTML sanitization (XSS-safe).
- **Automated Emails:** HTML email template builder for verification and password reset emails.
- **Robust File Uploads:** Secure, collision-resistant product and profile image uploads stored locally.

## Technologies Used

- **Frontend:** HTML5, CSS3, Vanilla JavaScript, Fetch API
- **Backend:** Java 17, Jersey 2 (JAX-RS), Servlet 4.0
- **Database:** MySQL 8.x, Hibernate 5.6
- **Tools:** Git, GitHub, Maven 3.8+
- **Application Server:** Apache Tomcat 9.0.x (Requires `javax.*` namespace)

## Screenshots

![Application Screenshot](src/main/webapp/assets/images/og-image.png)

## Prerequisites

Before running the project, install the required software:

- JDK 17
- Maven 3.8+
- Apache Tomcat 9.0.x (must be 9.0.x, not 10+)
- MySQL 8.x
- Git

## Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/your-username/smarttrade.git
   ```

2. Open the project directory:

   ```bash
   cd smarttrade
   ```

3. Build the project using Maven to install dependencies:

   ```bash
   mvn clean package
   ```

4. Configure the database and environment variables as described below.

## Database Setup

1. Start your MySQL database server.
2. Create the required schema and import the lookup data:
   ```bash
   mysql -u root -p < database/smarttrade.sql
   ```
3. *(Optional)* Insert a demo seller and sample products:
   ```bash
   mysql -u root -p < database/smarttrade_demo_data.sql
   ```
   *Demo login (if demo script ran): `demo@smarttrade.lk` / `Demo@1234` (verified, approved seller).*
4. Configure the database connection using your local environment settings by updating `src/main/resources/hibernate.cfg.xml`.
   *Note: `hbm2ddl.auto=update` is set to keep the schema in sync.*

## Configuration

The application configuration prioritizes Environment Variables over the `app.properties` file for sensitive data, managed via a custom `Env.java` utility.

Create the required configuration file `src/main/resources/app.properties` or set environment variables (using exact match or uppercase underscore format, e.g., `APP_URL`).

Example `app.properties`:

```properties
app.url=http://localhost:8080/smarttrade/
mail.smtp.host=smtp.example.com
mail.smtp.port=587
mail.username=your_username
mail.password=your_password
```

**Do not commit real passwords, API keys, or other secrets.**

## Running the Project

### Command Line

1. Build the WAR file:
   ```bash
   mvn clean package
   ```

2. Copy to Tomcat webapps directory:
   ```bash
   cp target/smarttrade.war $CATALINA_HOME/webapps/
   ```

3. Start Tomcat:
   ```bash
   $CATALINA_HOME/bin/startup.sh
   # On Windows use: startup.bat
   ```

4. Open **http://localhost:8080/smarttrade/** in your browser.

### IntelliJ IDEA

1. Go to *Run > Edit configurations > Add New Configuration > Tomcat Server (Local)*.
2. Select your Tomcat **9** home directory.
3. In the Deployment tab, add the artifact `smarttrade:war exploded`.
4. Set the Application context to `/smarttrade`.
*(Optional: Run `lk.jiat.smarttrade.Main` to start an embedded Tomcat 9 server. Ensure "Add dependencies with provided scope to classpath" is enabled in the run configuration).*

## Project Structure

```text
smarttrade/
├── src/
│   ├── main/
│   │   ├── java/lk/jiat/smarttrade/
│   │   │   ├── controller/  # Jersey REST API endpoints
│   │   │   ├── dto/         # Data Transfer Objects
│   │   │   ├── entity/      # Hibernate JPA Entities
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
├── database/                # SQL scripts for schema and demo data
├── target/                  # Compiled artifacts
├── .gitignore
├── pom.xml                  # Maven dependencies and build config
└── README.md
```

## Testing

To verify compilation and ensure no syntax errors were introduced, run:

```bash
mvn test
```

*Note: The project currently has no automated tests defined; running `mvn test` is primarily useful for verifying compilation.*

## Known Issues

- **Storage:** Shopping cart and wishlist live in the browser (`localStorage`). There is currently no backend order processing or payment gateway integration. The checkout page validates the form and explains this limitation.
- **Seller Registration:** A user can only list products if a row exists in the `seller` table with status `APPROVED`. There is currently no self-service seller registration screen.
- **Security:** Passwords are currently stored in plain text and returned by `api/profiles/user-profile`. They should be hashed (e.g., using BCrypt) before production use.
- **Credentials:** Database and SMTP credentials are in plain text config files. It is recommended to use environment variables for deployment.

## Future Improvements

- Integrate a secure password hashing mechanism (e.g., BCrypt).
- Implement a backend order processing system and payment gateway.
- Create a self-service seller registration and approval workflow.
- Move away from plain text credentials to an environment variables-only approach for production deployment.

## Contributing

1. Fork the repository.
2. Create a new branch (`git checkout -b feature-branch`).
3. Make your changes and commit them (`git commit -m 'Add some feature'`).
4. Push to the branch (`git push origin feature-branch`).
5. Open a pull request.

Contributions are welcome!

## License

This project was developed for educational purposes. It is currently not licensed under any specific open-source license.
