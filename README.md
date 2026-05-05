#  CampusCare - School Clinic Appointment System

![Status](https://img.shields.io/badge/status-active-brightgreen)
![Version](https://img.shields.io/badge/version-1.0.0-blue)
![License](https://img.shields.io/badge/license-MIT-green)

CampusCare is a comprehensive school clinic appointment management system that streamlines the process of scheduling, managing, and tracking medical appointments for students and administrators.

---

##  Features

###  Student Features
- **User Authentication**: Secure login and registration with email/password
- **Book Appointments**: Schedule appointments with the school clinic
- **View Appointments**: Track upcoming and past appointments
- **Appointment Status**: Real-time status updates (Pending, Approved, Completed, Cancelled)
- **Profile Management**: Update personal information and change password
- **Password Strength Indicator**: Visual feedback on password security

###  Admin Features
- **Admin Dashboard**: Overview of all appointments
- **Approve/Reject**: Manage appointment requests
- **User Management**: View and manage user accounts

###  Security
- **JWT Authentication**: Secure token-based authentication
- **Role-Based Access**: Different views for students and admins
- **Password Encryption**: BCrypt password hashing
- **Protected Routes**: Frontend and backend route protection

---

##  Tech Stack

### Frontend (Web)
| Technology | Description |
|------------|-------------|
| React.js | UI Framework |
| React Router | Client-side routing |
| CSS3 | Custom styling with variables |
| Fetch API | HTTP requests |

### Backend
| Technology | Description |
|------------|-------------|
| Spring Boot | Java framework |
| Spring Security | Authentication & authorization |
| JWT | JSON Web Tokens |
| JPA/Hibernate | ORM & database |
| BCrypt | Password encryption |

### Mobile
| Technology | Description |
|------------|-------------|
| Kotlin | Programming language |
| Android SDK | Mobile framework |
| Retrofit | HTTP client |
| Material Design | UI components |

---

---

## 🚀 Getting Started

### Prerequisites
- **Java 17+**
- **Node.js 18+**
- **Android Studio** (for mobile)
- **MySQL** or **PostgreSQL**

### Backend Setup

1. Navigate to the backend directory
   cd backend
2. Create environment file
  Create a .env file in the backend/ directory:
###
DATABASE_URL=jdbc:postgresql://localhost:5432/campuscare
DATABASE_USERNAME=your_database_username
DATABASE_PASSWORD=your_database_password

DATABASE_CLIENT_ID=your_google_oauth_client_id
DATABASE_CLIENT_SECRET=your_google_oauth_client_secret

MAIL_USERNAME=your_gmail_address@gmail.com
MAIL_PASSWORD=your_gmail_app_password
###
Note: For Gmail, use an App Password instead of your regular password.
###
4. Run the application
  ./mvnw spring-boot:run

### Frontend Setup
1. Navigate to the web directory
   cd web/campuscare-web
3. Create environment file
   Create a .env file in the web/campuscare-web/ directory: REACT_APP_GOOGLE_CLIENT_ID=your_google_client_id
5. Install dependencies
   npm install
7. Start the development server
   npm start
### Mobile Setup
1. Open Android Studio
2. Open the project
   File → Open → Select mobile/ directory
3. Update API base URL
Open app/src/main/java/edu/cit/morre/campuscare/auth/RetrofitClient.kt:
###
   private const val BASE_URL = "http://10.0.2.2:8080/" // For emulator, Use "http://your_ip:8080/" for physical device
###
5. Run on emulator or device
   
