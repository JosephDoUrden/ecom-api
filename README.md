# 🛒 E-Commerce API

This project is a fully modular and scalable e-commerce API developed with **Java Spring Boot**. It includes **JWT-based authentication**, **role management (Admin, Vendor, Customer)**, **product and order systems**, and **Swagger/OpenAPI documentation**. 

## 🚀 Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Data JPA (Hibernate)
- Spring Security + JWT
- PostgreSQL / MySQL
- Swagger / OpenAPI (springdoc)
- Lombok
- MapStruct / ModelMapper
- Docker
- (Optional) Redis, Kafka, AWS S3

## 🗂 Project Structure

```
src/
├── config/
├── controller/
├── dto/
├── exception/
├── mapper/
├── model/ (entities)
├── repository/
└── service/
```

---

## 🔌 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user and get JWT token

### Users
- `GET /api/users` - List all users (Admin only)
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user (Admin only)

### Products
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create new product (Vendor/Admin)
- `PUT /api/products/{id}` - Update product (Vendor/Admin)
- `DELETE /api/products/{id}` - Delete product (Vendor/Admin)

### Categories
- `GET /api/categories` - List all categories
- `GET /api/categories/{id}` - Get category by ID
- `POST /api/categories` - Create new category (Admin)
- `PUT /api/categories/{id}` - Update category (Admin)
- `DELETE /api/categories/{id}` - Delete category (Admin)

### Cart
- `GET /api/cart` - View current user's cart
- `POST /api/cart/items` - Add item to cart
- `PUT /api/cart/items/{id}` - Update cart item quantity
- `DELETE /api/cart/items/{id}` - Remove item from cart

### Orders
- `GET /api/orders` - List user's orders
- `GET /api/orders/{id}` - Get order details
- `POST /api/orders` - Create order from cart
- `PUT /api/orders/{id}/status` - Update order status (Admin)

---

## 📅 Development Plan

### 1. Planning & Requirements (Day 1-2)
- Define project scope and core modules.
- Determine user roles: `ADMIN`, `VENDOR`, `CUSTOMER`.

---

### 2. Project Setup (Day 3)
- Initialize project with Spring Initializr.
- Add dependencies (Web, JPA, Security, OpenAPI, Lombok).
- Setup layered architecture.

---

### 3. User Authentication & Roles (Day 4–6)
- Implement JWT-based user registration & login.
- Implement role-based access control.
- Secure endpoints in Swagger.

---

### 4. Product & Category Module (Day 7–9)
- Implement CRUD operations for categories and products.
- Implement stock tracking.

---

### 5. Cart & Order Module (Day 10–12)
- Manage user shopping cart.
- Create and track orders.

---

### 6. Payment Simulation (Day 13–14)
- Simulate payment process.
- Generate simple invoice (optional PDF).

---

### 7. Admin Panel Endpoints (Day 15–16)
- Manage users, products, and orders.
- Manage campaigns/discounts and shipping.

---

### 8. Swagger Integration (Day 17)
- Integrate Swagger (springdoc-openapi).
- Group endpoints and add security scheme.

---

### 9. Testing & Documentation (Day 18–19)
- Finalize Swagger docs.
- Write unit/integration tests.

---

### 10. Dockerization & Deployment (Day 20–21)
- Add `Dockerfile` & `docker-compose.yml`.
- Deploy to a container platform.

---

## ✨ Optional Features

- Full-text search with Elasticsearch
- Redis caching for products/categories
- Kafka integration for events
- Multi-vendor architecture
- Mobile-ready REST API (Flutter or React Native frontend)

---

## 🧪 Sample Module: Product

```java
@Entity
public class Product {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;

    @ManyToOne
    private Category category;
}

```

----------

## 📖 Swagger Example

```java
@Tag(name = "Product", description = "Product management endpoints")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Operation(summary = "Get all products", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public List<ProductDto> getAllProducts() {
        return productService.getAll();
    }
}

```

----------

## 🧠 Tips

-   Use DTOs to avoid exposing entities directly.
    
-   Keep exception handling centralized.
    
-   Comment your Swagger annotations for clarity.
    
-   Use `@PreAuthorize("hasRole('ADMIN')")` for protected endpoints.
    
-   Track development in a project board (Notion, Trello, Jira, etc.)
    

----------

## 📌 License

This project is for educational and personal development purposes. Commercial use requires permission.

