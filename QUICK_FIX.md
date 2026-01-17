# Quick Fix: Backend Database Connection Issue

## Problem
Backend fails to start because MySQL is not running.

## Quick Solution

### Step 1: Start MySQL

**Option A: Using Windows Services**
1. Press `Win + R`, type `services.msc`, press Enter
2. Find "MySQL80" or similar MySQL service
3. Right-click → Start

**Option B: Using Command Line**
```powershell
# Check MySQL service name
Get-Service | Where-Object {$_.DisplayName -like "*MySQL*"}

# Start MySQL (replace MySQL80 with your service name)
net start MySQL80
# OR
Start-Service MySQL80
```

**Option C: Using MySQL Command Line**
```bash
# Navigate to MySQL bin directory (usually C:\Program Files\MySQL\MySQL Server 8.0\bin)
cd "C:\Program Files\MySQL\MySQL Server 8.0\bin"

# Start MySQL
mysqld --console
```

### Step 2: Verify MySQL is Running
```powershell
# Check if port 3306 is listening
netstat -an | Select-String "3306"
```

### Step 3: Create Database (if needed)
```bash
mysql -u root -p
```

Then in MySQL:
```sql
CREATE DATABASE IF NOT EXISTS erp_db;
USE erp_db;
EXIT;
```

### Step 4: Test Connection
```bash
mysql -u root -p -h localhost
```

### Step 5: Start Backend
```bash
cd C:\Users\noble\ERP\SMS
.\mvnw.cmd spring-boot:run
```

---

## Alternative: Use H2 Database (No MySQL Required)

If you don't have MySQL or want quick testing, use H2 in-memory database:

1. **Update `pom.xml`** - Add H2 dependency:
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

2. **Create `application-h2.properties`**:
```properties
spring.datasource.url=jdbc:h2:mem:erpdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
```

3. **Run with H2 profile**:
```bash
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2
```

---

## Still Not Working?

1. **Check MySQL installation:**
   - Verify MySQL is installed: `mysql --version`
   - Check MySQL is in PATH

2. **Update credentials in `application.properties`:**
   - Verify username/password are correct
   - Check if MySQL runs on different port

3. **Check firewall:**
   - Allow MySQL through Windows Firewall

4. **Check MySQL logs:**
   - Usually in: `C:\ProgramData\MySQL\MySQL Server 8.0\Data\`

