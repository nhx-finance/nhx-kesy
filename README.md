# NHX KESY Spring Boot Application

## Project Overview

This is a Spring Boot application designed to manage user authentication, KYC (Know Your Customer) processes, minting requests for a digital asset (KESY tokens), and administrative functionalities. It provides a robust backend for a secure and scalable platform.

## Key Features

*   **User Authentication & Authorization:** Secure user registration, login, OTP verification, and JWT-based authentication with refresh tokens. Includes role-based authorization for administrative functionalities.
*   **KYC Management:** Workflow for user identity verification, including document submission and admin review.
*   **Minting Operations:** Functionality for users to request minting of KESY tokens, with validation and status tracking.
*   **Centralized Exception Handling:** Robust and consistent error management across the application, providing standardized JSON responses with appropriate HTTP status codes for various error scenarios.
*   **Document Storage:** Integration with Azure Blob Storage (production profile) and local file storage (development/local profiles) for document management.
*   **Email Notifications:** Automated email notifications for OTP, welcome messages, KYC status changes, and mint status updates.
*   **Admin Dashboard (API):** Endpoints for administrators to manage KYC submissions and mint requests.

## Technologies Used

*   **Spring Boot:** Framework for building robust, production-ready applications.
*   **Spring Data JPA:** For database interaction and repository management.
*   **Spring Security:** For authentication and authorization.
*   **H2 Database:** In-memory database for development/testing.
*   **Lombok:** Reduces boilerplate code.
*   **Swagger/OpenAPI:** For API documentation.
*   **Azure Blob Storage SDK:** For cloud-based document storage.
*   **Thymeleaf:** Template engine for HTML email generation.
*   **Jackson:** JSON processing library.
*   **SLF4J & Logback:** For logging.
*   **Maven:** Project management and build automation tool.

## Setup and Installation

To get this application running locally, follow these steps:

1.  **Clone the Repository:**
    ```bash
    git clone git@github.com:nhx-finance/nhx-kesy.git
    cd nhxkesy
    ```

2.  **Maven Dependencies:**
    Ensure you have Maven installed. The `pom.xml` file contains all necessary dependencies. They will be automatically downloaded when you build the project.

3.  **Database Configuration:**
    The application uses an H2 in-memory database by default for `dev` and `local` profiles. You can find configuration in `src/main/resources/application-dev.yml`.

    For production, you would configure a persistent database (e.g., PostgreSQL, MySQL) in `application-prod.yml`.

4.  **Environment Variables / `application-dev.yml`:**
    Update `src/main/resources/application-dev.yml` with your local configurations. Key properties include:
    *   `spring.mail.*`: Email server configuration for sending notifications.
    *   `jwt.access-token-expiry`, `jwt.refresh-token-expiry`: JWT token expiry times.
    *   `app.base-url`, `app.name`, `app.support-email`: Application specific details.
    *   `storage.local.base-path`: Local directory for file storage (for `dev`/`local` profiles).
    *   `admin.notification.email`: Email address for admin notifications.

    **Note:** For local file storage, ensure the `storage.local.base-path` directory exists or is creatable by the application.

## Running the Application

You can run the Spring Boot application using Maven:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
Replace `dev` with `prod` or `local` if you have other profiles configured.

## API Endpoints

Once the application is running, you can access the API documentation via Swagger UI (if configured):
`http://localhost:8080/swagger-ui.html` (or your configured port).

Some key endpoints include:

*   `POST /api/auth/signup`: User registration.
*   `POST /api/auth/login`: User login.
*   `POST /api/auth/verify-otp`: Verify user email with OTP.
*   `GET /api/user/profile`: Retrieve authenticated user's profile.
*   `PATCH /api/user/profile`: Update authenticated user's profile.
*   `GET /api/user/mints`: **(New!)** Retrieve all mint requests for the authenticated user.
*   `POST /api/user/mints`: Submit a new mint request.
*   `GET /api/admin/kyc`: Get all KYC submissions (Admin only).
*   `PATCH /api/admin/kyc/{kycId}/status`: Update KYC status (Admin only).

## Exception Handling

The application features a centralized exception handling mechanism implemented using `@ControllerAdvice` and `@ExceptionHandler`. All custom exceptions (e.g., `ResourceNotFoundException`, `InvalidOtpException`, `StorageException`) and common Spring-related exceptions are caught and transformed into a consistent JSON error response:

```json
{
  "timestamp": "2023-10-27T10:30:00.123456",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid OTP",
  "path": "/api/auth/verify-otp"
}
```
This ensures a predictable and user-friendly error experience across the API.

## Contact and Support

For any questions or issues, please contact [email](mailto:dancanian25@gmail.com) or open an issue in the GitHub repository.
