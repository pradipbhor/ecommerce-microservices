# Product Service - Complete Step-by-Step Implementation

## Step 1: Create Spring Boot Project

### Method 1: Using Spring Initializr (Recommended)
1. Go to https://start.spring.io/
2. Fill in the following:
   - **Project**: Maven
   - **Language**: Java
   - **Spring Boot**: 3.2.0
   - **Project Metadata**:
     - Group: `com.ecommerce`
     - Artifact: `product-service`
     - Name: `product-service`
     - Package name: `com.ecommerce.product`
     - Packaging: Jar
     - Java: 17

3. Add Dependencies:
   - Spring Web
   - Spring Data JPA
   - MySQL Driver
   - Lombok
   - Spring Boot DevTools
   - Validation

4. Click **Generate** and extract the ZIP file

### Method 2: Create Manually

Create the following folder structure:
```
product-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── ecommerce/
│   │   │           └── product/
│   │   │               ├── ProductServiceApplication.java
│   │   │               ├── controller/
│   │   │               ├── service/
│   │   │               ├── repository/
│   │   │               ├── entity/
│   │   │               ├── dto/
│   │   │               └── exception/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   └── test/
│       └── java/
│           └── com/
│               └── ecommerce/
│                   └── product/
└── pom.xml
```

### Install MySQL (if not installed)
```bash
# For Ubuntu/Debian
sudo apt-get update
sudo apt-get install mysql-server

# For Mac
brew install mysql
brew services start mysql

# For Windows
# Download from https://dev.mysql.com/downloads/installer/
```

### Create Database
```sql
# Login to MySQL
mysql -u root -p

# Create database
CREATE DATABASE ecommerce_product_db;

# Create user (optional)
CREATE USER 'ecommerce_user'@'localhost' IDENTIFIED BY 'password123';
GRANT ALL PRIVILEGES ON ecommerce_product_db.* TO 'ecommerce_user'@'localhost';
FLUSH PRIVILEGES;

# Use the database
USE ecommerce_product_db;
```

## Step 13: Run and Test the Application

### 1. Start the Application
```bash
# Navigate to project directory
cd product-service

# Run with Maven
mvn spring-boot:run

# Or build and run JAR
mvn clean package
java -jar target/product-service-1.0.0.jar
```

### 2. Test APIs with cURL Commands

#### Create a Product
```bash
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "PROD001",
    "name": "iPhone 15 Pro",
    "description": "Latest iPhone with A17 Pro chip",
    "price": 999.99,
    "quantity": 50,
    "category": "Electronics",
    "brand": "Apple",
    "imageUrl": "https://example.com/iphone15.jpg"
  }'
```

#### Get All Products
```bash
curl http://localhost:8081/api/products?page=0&size=10
```

#### Get Product by ID
```bash
curl http://localhost:8081/api/products/1
```

#### Search Products
```bash
curl "http://localhost:8081/api/products/search?query=iPhone&page=0&size=10"
```

#### Update Product
```bash
curl -X PUT http://localhost:8081/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "price": 899.99,
    "quantity": 45
  }'
```

#### Update Stock
```bash
curl -X PUT http://localhost:8081/api/products/1/stock \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 10,
    "operationType": "ADD"
  }'
```

#### Check Stock Availability
```bash
curl "http://localhost:8081/api/products/1/check-stock?quantity=5"
```

#### Get Products by Category
```bash
curl http://localhost:8081/api/products/category/Electronics
```

#### Get Low Stock Products
```bash
curl http://localhost:8081/api/products/low-stock?threshold=10
```

#### Delete Product (Soft Delete)
```bash
curl -X DELETE http://localhost:8081/api/products/1
```

## Step 14: Test with Postman Collection

Create a Postman collection with these requests:

### Environment Variables
```json
{
  "baseUrl": "http://localhost:8081",
  "productId": ""
}
```

### Sample Requests for Postman

1. **Create Product**
   - Method: POST
   - URL: `{{baseUrl}}/api/products`
   - Body: (raw JSON)
   ```json
   {
     "sku": "LAPTOP001",
     "name": "MacBook Pro 16-inch",
     "description": "M3 Max chip with 16-core CPU",
     "price": 2499.00,
     "quantity": 25,
     "category": "Computers",
     "brand": "Apple"
   }
   ```

2. **Get All Products**
   - Method: GET
   - URL: `{{baseUrl}}/api/products?page=0&size=5&sortBy=price&sortDirection=DESC`

3. **Update Stock**
   - Method: PUT
   - URL: `{{baseUrl}}/api/products/{{productId}}/stock`
   - Body: (raw JSON)
   ```json
   {
     "quantity": 5,
     "operationType": "REDUCE"
   }
   ```

## Step 15: Verify Database

Connect to MySQL and verify the data:

```sql
-- Check if table was created
USE ecommerce_product_db;
SHOW TABLES;

-- View table structure
DESCRIBE products;

-- View all products
SELECT * FROM products;

-- Check specific product
SELECT * FROM products WHERE sku = 'PROD001';

-- Check low stock products
SELECT * FROM products WHERE quantity < 10;
```

## Troubleshooting Common Issues

### Issue 1: Cannot connect to MySQL
**Solution:**
```yaml
# Check MySQL is running
sudo service mysql status

# Update application.yml with correct credentials
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ecommerce_product_db?useSSL=false&allowPublicKeyRetrieval=true
    username: your_username
    password: your_password
```

### Issue 2: Table not created
**Solution:**
```yaml
# Ensure hibernate ddl-auto is set correctly
spring:
  jpa:
    hibernate:
      ddl-auto: update  # or create-drop for development
```

### Issue 3: Port already in use
**Solution:**
```yaml
# Change port in application.yml
server:
  port: 8082  # or any available port
```

## Next Steps

Once you've verified all APIs are working:

1. ✅ All CRUD operations working
2. ✅ Stock management functioning
3. ✅ Search and filter capabilities tested
4. ✅ Exception handling verified
5. ✅ Database persistence confirmed

You're ready to move to the next service! Would you like to proceed with:
- **Order Service** (with Saga pattern)
- **Cart Service** (with Redis)
- **Eureka Server** (Service Discovery)
- **API Gateway** (with rate limiting)

Let me know which service you'd like to implement next!


API Endpoints Summary:

POST /api/products - Create product
GET /api/products/{id} - Get by ID
GET /api/products - Get all (paginated)
PUT /api/products/{id} - Update product
DELETE /api/products/{id} - Soft delete
PUT /api/products/{id}/stock - Update stock
GET /api/products/search?query= - Search products
GET /api/products/category/{category} - Filter by category
GET /api/products/low-stock - Get low stock items
GET /api/products/{id}/check-stock - Check availability