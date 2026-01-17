# Backend Troubleshooting Guide

## Issue: Backend Fails to Run

### Problem: Database Connection Error

**Error Message:**
```
Communications link failure
Connection refused: no further information
```

This means the application cannot connect to MySQL database.

---

## Solutions

### Solution 1: Start MySQL Server

1. **Check if MySQL is running:**
   ```powershell
   # On Windows, check MySQL service
   Get-Service -Name MySQL*
   ```

2. **Start MySQL Service:**
   ```powershell
   # Start MySQL service
   net start MySQL80
   # OR
   Start-Service MySQL80
   ```

   If MySQL service name is different, check with:
   ```powershell
   Get-Service | Where-Object {$_.DisplayName -like "*MySQL*"}
   ```

3. **Verify MySQL is listening on port 3306:**
   ```powershell
   netstat -an | findstr 3306
   ```

### Solution 2: Check Database Credentials

Edit `src/main/resources/application.properties`:

```properties
# Verify these settings match your MySQL installation
spring.datasource.url=jdbc:mysql://localhost:3306/erp_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
```

**Update if your MySQL:**
- Runs on a different port (not 3306)
- Uses different username/password
- Database name is different

### Solution 3: Create the Database

If the database `erp_db` doesn't exist:

1. **Connect to MySQL:**
   ```bash
   mysql -u root -p
   ```

2. **Create the database:**
   ```sql
   CREATE DATABASE erp_db;
   USE erp_db;
   ```

3. **Or use the provided SQL script:**
   ```bash
   mysql -u root -p < database_setup.sql
   ```

### Solution 4: Test Database Connection Manually

Test if you can connect to MySQL:

```bash
mysql -u root -p -h localhost -P 3306
```

If this fails, MySQL is not running or credentials are wrong.

---

## Alternative: Use H2 In-Memory Database (For Testing)

If you want to test without MySQL, you can use H2 database:

1. **Add H2 dependency to `pom.xml`:**
   ```xml
   <dependency>
       <groupId>com.h2database</groupId>
       <artifactId>h2</artifactId>
       <scope>runtime</scope>
   </dependency>
   ```

2. **Update `application.properties`:**
   ```properties
   # H2 Database Configuration
   spring.datasource.url=jdbc:h2:mem:testdb
   spring.datasource.driver-class-name=org.h2.Driver
   spring.datasource.username=sa
   spring.datasource.password=
   spring.h2.console.enabled=true
   spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
   ```

---

## Common Issues

### Issue 1: MySQL Port 3306 Already in Use

**Solution:** Change MySQL port or stop conflicting service

### Issue 2: Wrong MySQL Password

**Solution:** Reset MySQL root password or update `application.properties`

### Issue 3: MySQL Not Installed

**Solution:** Install MySQL from https://dev.mysql.com/downloads/mysql/

### Issue 4: Firewall Blocking Connection

**Solution:** Allow MySQL through Windows Firewall

---

## Quick Check Commands

```powershell
# Check if MySQL is running
Get-Service MySQL*

# Check MySQL port
netstat -an | findstr 3306

# Try to connect to MySQL
mysql -u root -p

# Check Java version (should be 17+)
java -version

# Check Maven version
.\mvnw.cmd --version
```

---

## Next Steps After Fixing Database

1. **Verify database connection:**
   ```bash
   .\mvnw.cmd spring-boot:run
   ```

2. **Check application logs** for successful startup

3. **Test API endpoints** using Postman or curl

---

## Still Having Issues?

1. Check MySQL error logs (usually in MySQL data directory)
2. Verify Java version (should be 17+)
3. Ensure port 8080 is not already in use
4. Check application.properties for typos
5. Review full error stack trace

