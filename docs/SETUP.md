# Development Setup Guide

## Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **MySQL 8.0+**
- **Git**
- **IDE**: IntelliJ IDEA (recommended) or VS Code

---

## Step 1: Clone the Repository

```bash
git clone <repository-url>
cd SMS
```

---

## Step 2: Database Setup

### Install MySQL
Download and install MySQL 8.0+ from [mysql.com](https://dev.mysql.com/downloads/)

### Create Database
```sql
CREATE DATABASE erp_db;
```

### Configure Credentials
Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/erp_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

---

## Step 3: Email Configuration (Optional for Development)

For testing email features, update in `application.properties`:
```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

> **Note**: For Gmail, you need to generate an App Password from Google Account settings.

---

## Step 4: Build & Run

### Build the project
```bash
./mvnw clean install
```
Or on Windows:
```bash
mvnw.cmd clean install
```

### Run the application
```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

---

## Step 5: Verify Installation

### Test the API
```bash
curl http://localhost:8080/api/auth/register -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com","password":"password123","firstName":"Test","lastName":"User","role":"ADMIN"}'
```

Or use Postman/Insomnia to test endpoints.

---

## IDE Setup

### IntelliJ IDEA
1. Open project folder
2. Import as Maven project
3. Enable annotation processing: `Settings > Build > Compiler > Annotation Processors > Enable`
4. Install Lombok plugin if prompted

### VS Code
1. Install extensions:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Lombok Annotations Support
2. Open project folder
3. Let VS Code import Maven project

---

## Common Issues

### Lombok not working
- Ensure annotation processing is enabled
- Rebuild the project: `mvnw clean install`

### Database connection failed
- Verify MySQL is running: `mysql -u root -p`
- Check credentials in `application.properties`
- Ensure database `erp_db` exists

### Port 8080 already in use
- Change port in `application.properties`: `server.port=8081`
- Or kill the process using port 8080

---

## Next Steps

1. Read [CONTRIBUTING.md](./CONTRIBUTING.md) for your assigned tasks
2. Create your feature branch
3. Start coding!
