# 🗂 E-Commerce API Backlog & Sprint Planning

This document outlines the backlog and sprint planning for the E-Commerce API project. Each sprint is planned for a duration of **2 weeks**. Each task includes effort estimation in story points (SP) on a scale of 1-8.

---

## 🏁 Sprint 1: Project Setup & User Management

### Goals:
- Set up the project structure and dependencies.
- Implement user authentication and role-based access control.
- Configure Docker for development environment.

### Tasks:
- [ ] **Project Setup** (5 SP)
  - [x] Initialize the project using Spring Initializr with Java 17 and Spring Boot 3.x.
  - [x] Add dependencies: Web, JPA, Security, OpenAPI, Lombok, MapStruct.
  - [x] Setup layered architecture (controller, service, repository, model, dto, mapper).
  - [ ] Configure application properties for local development.
  - [ ] Setup global exception handling.

- [ ] **User Authentication** (8 SP)
  - [ ] Create User entity with roles and required fields.
  - [ ] Implement user repository with custom queries.
  - [ ] Create DTOs for user registration and login.
  - [ ] Implement user service layer with business logic.
  - [ ] Implement JWT token generation and validation.
  - [ ] Hash passwords using BCrypt with appropriate strength.
  - [ ] Add role-based access control (`ADMIN`, `VENDOR`, `CUSTOMER`).
  - [ ] Create authentication controller with endpoints.

- [ ] **Swagger Integration** (3 SP)
  - [ ] Configure OpenAPI documentation with proper metadata.
  - [ ] Add Swagger (springdoc-openapi) for API documentation.
  - [ ] Secure Swagger UI with Bearer Token authentication.
  - [ ] Add detailed documentation to API endpoints.

- [ ] **Docker Setup** (3 SP)
  - [ ] Create Dockerfile for the application.
  - [ ] Set up docker-compose.yml with PostgreSQL and application services.
  - [ ] Configure environment variables for containerized deployment.
  - [ ] Create Docker volume for persistent database storage.
  - [ ] Document Docker commands for local development.

---

## 🏁 Sprint 2: Product & Category Management

### Goals:
- Build the product and category modules.
- Implement CRUD operations and stock tracking.

### Tasks:
- [ ] **Category Module** (5 SP)
  - [ ] Create category entity with name, description, and parent relationship.
  - [ ] Implement category repository with custom queries.
  - [ ] Create DTOs for category requests and responses.
  - [ ] Implement category service with business logic.
  - [ ] Create category controller with REST endpoints.
  - [ ] Implement category hierarchy (parent-child relationship).

- [ ] **Product Module** (8 SP)
  - [ ] Create product entity with required fields (name, description, price, etc.).
  - [ ] Implement product-category relationship (many-to-one).
  - [ ] Create product variation entity (size, color, etc.).
  - [ ] Implement product repository with filtering queries.
  - [ ] Create DTOs for product requests and responses.
  - [ ] Implement product service with business logic.
  - [ ] Create product controller with REST endpoints.
  - [ ] Implement stock tracking and inventory management.

- [ ] **Validation & Error Handling** (3 SP)
  - [ ] Add input validation for product and category endpoints.
  - [ ] Implement custom validation constraints if needed.
  - [ ] Centralize exception handling for consistent responses.
  - [ ] Add logging for errors and important operations.

---

## 🏁 Sprint 3: Cart & Order Management

### Goals:
- Build the cart and order modules.
- Enable users to manage their carts and place orders.

### Tasks:
- [ ] **Cart Module**
  - [ ] Create cart entity and repository.
  - [ ] Implement add, update, and remove operations for cart items.
  - [ ] Calculate total price dynamically.

- [ ] **Order Module**
  - [ ] Create order entity and repository.
  - [ ] Implement order creation from the cart.
  - [ ] Add order tracking and status updates (e.g., pending, shipped, delivered).

- [ ] **Security**
  - [ ] Ensure cart and order endpoints are secured by user roles.

---

## 🏁 Sprint 4: Payment Simulation & Admin Panel

### Goals:
- Simulate the payment process.
- Build admin panel endpoints for managing users, products, and orders.

### Tasks:
- [ ] **Payment Simulation**
  - [ ] Simulate payment processing (no real gateway).
  - [ ] Mark orders as "paid."
  - [ ] Generate simple invoices (optional PDF).

- [ ] **Admin Panel**
  - [ ] Implement endpoints for managing users, products, and orders.
  - [ ] Add functionality for creating campaigns/discounts.
  - [ ] Manage shipping options (e.g., companies, tracking codes).

---

## 🏁 Sprint 5: Testing, Documentation & Deployment

### Goals:
- Finalize testing and documentation.
- Dockerize the application and prepare for deployment.

### Tasks:
- [ ] **Testing**
  - [ ] Write unit tests using JUnit and Mockito.
  - [ ] Write integration tests for key modules.

- [ ] **Documentation**
  - [ ] Finalize Swagger documentation.
  - [ ] Add a sample Postman collection (optional).

- [ ] **Dockerization**
  - [ ] Create `Dockerfile` and `docker-compose.yml`.
  - [ ] Connect the application to a PostgreSQL/MySQL container.

- [ ] **Deployment**
  - [ ] Deploy the application to a container platform (e.g., Heroku, Render, VPS).

---

## 🏁 Future Backlog (Optional Features)

### Goals:
- Enhance the application with advanced features.

### Tasks:
- [ ] **Performance Optimization**
  - [ ] Add Redis caching for products and categories.
  - [ ] Implement full-text search with Elasticsearch.

- [ ] **Event-Driven Architecture**
  - [ ] Integrate Kafka for event-driven communication.

- [ ] **Multi-Vendor Support**
  - [ ] Extend the application to support multiple vendors.

- [ ] **Mobile-Ready API**
  - [ ] Optimize the API for mobile app integration (e.g., Flutter, React Native).

- [ ] **AWS S3 Integration**
  - [ ] Add support for image uploads to AWS S3.

--- 

## 📌 Notes

- Each sprint will include a sprint review and retrospective to evaluate progress and identify improvements.
- Tasks are prioritized based on core functionality and business value.
