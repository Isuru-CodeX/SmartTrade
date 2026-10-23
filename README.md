# SmartTrade - Web Programming II (HF2L)

Phone marketplace web application - **Java 17 / Apache Tomcat 9 / Servlet 4.0 (javax.\*) / Jersey 2 (JAX-RS) / Hibernate 5.6 / MySQL 8**.
Front end: custom dark design system (colours sampled from the SmartTrade logo), HTML5, CSS3, vanilla JavaScript, Fetch API + JSON.

## 1. Requirements
| Tool | Version |
|---|---|
| JDK | 17 |
| Maven | 3.8+ |
| Apache Tomcat | **9.0.x** (not 10 - this project uses the `javax.*` namespace) |
| MySQL | 8.x |

## 2. Database
```bash
mysql -u root -p < database/smarttrade.sql            # schema + required lookup data
mysql -u root -p < database/smarttrade_demo_data.sql  # OPTIONAL demo seller + 2 demo phones
```
Demo login (only if you ran the demo script): `demo@smarttrade.lk` / `Demo@1234` (verified, approved seller).

Then check the connection details in
* `src/main/resources/hibernate.cfg.xml` - JDBC url, user, password (`hbm2ddl.auto=update` keeps the schema in sync)
* `src/main/resources/app.properties` - SMTP (mail) settings and `app.url` (must match the URL you deploy to)

## 3. Build & deploy on Tomcat 9
```bash
mvn clean package
cp target/smarttrade.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/startup.sh        # (startup.bat on Windows)
```
Open **http://localhost:8080/smarttrade/**

IntelliJ IDEA: *Run > Edit configurations > Tomcat Server (Local)* with a Tomcat **9** home, deployment artifact
`SmartTrade:war exploded`, application context `/smarttrade`.
(Optional: `lk.jiat.smarttrade.Main` starts an embedded Tomcat 9 - enable *"Add dependencies with provided scope to classpath"* in the run configuration.)

## 4. What changed in this version
**Front end (complete rework)**
* New dark theme built from the logo palette (`#10171c` ink, `#18a953` brand green, `#1bdb7b` highlight); Poppins bundled locally.
* Every page redesigned: home, sign in / sign up / verify, my account, shop (advanced search, filters, sorting, pagination), product details, cart, checkout, error page.
* Responsive (mobile menu, filter drawer, cart drawer), live search overlay (`/` or `Ctrl+K`), wishlist and cart (browser storage).
* Old Axil theme CSS/JS (jQuery, slick, ...) and the trial-licensed RichTextEditor were removed. The sell form now uses a small built-in editor (`assets/js/editor.js`).
* Seller HTML descriptions are sanitised before display (XSS-safe); all dynamic text is escaped.

**Back end (Tomcat 9 / JDK 17 compatibility)**
* `jakarta.*` -> `javax.*`; Hibernate 6.1 -> 5.6.15, Jersey 3 -> 2.41, Jakarta Mail -> JavaMail 1.6.2, Tomcat embed 10 -> 9.
* Jersey servlet, listener and filters are declared in `WEB-INF/web.xml` (works on a standalone Tomcat, not only the embedded launcher).
* `SnakeCaseNamingStrategy` keeps the existing lower_snake_case table / column names, so an existing database keeps working on Windows, Linux and macOS.
* New endpoints: `GET api/data/products` (catalogue with brand/model details), `GET api/users/session` (header sign-in state).

**Bugs fixed**
* Account verification was impossible: `VerificationController` required a logged-in user.
* Uploaded image URLs contained the absolute disk path (broken images) - they are now `http://.../uploads/product/{id}/{file}`; file names are unique even for simultaneous uploads.
* `ProfileService` null pointer exceptions when an address had no line 2 / postal code; the first saved address now becomes the primary address.
* `AuthAccessFilter` set cache headers after the response was committed.
* The cart icon called an undefined `viewCart()`; the debug `TestController` and scratch test class were removed.

## 5. Known limitations (be ready to explain them in the viva)
* Shopping cart and wishlist live in the browser (`localStorage`); there is no order / payment-gateway back end yet - the checkout page validates the form and explains this.
* A user can only sell if a row exists in `seller` with status `APPROVED` (the demo script creates one). There is no seller-registration screen.
* Passwords are stored in plain text and returned by `api/profiles/user-profile`; hash them (e.g. BCrypt) before any real use.
* Database and SMTP credentials are in plain text config files - move them to environment variables for deployment.
