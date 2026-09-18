# 🔐 Invisible Authentication System

<p align="center">
  <b>Behavior-Based Authentication & Secure Document Locker</b>
</p>

<p align="center">
  A security-focused authentication system that combines traditional authentication with invisible behavioral analysis to detect suspicious users and protect confidential documents.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?style=for-the-badge" alt="Android">
  <img src="https://img.shields.io/badge/Language-Kotlin-purple?style=for-the-badge" alt="Kotlin">
  <img src="https://img.shields.io/badge/UI-XML-blue?style=for-the-badge" alt="XML">
  <img src="https://img.shields.io/badge/Database-SQLite-orange?style=for-the-badge" alt="SQLite">
</p>

---

## 📌 Abstract

The **Invisible Authentication System** is an advanced security solution designed to improve user authentication by combining traditional authentication methods with behavioral analysis.

Instead of relying only on username, password, and OTP, the system analyzes user interaction patterns such as:

* ⌨️ Typing speed
* ⌨️ Keystroke dynamics
* 🔄 Interaction behavior
* 🔢 OTP entry behavior
* ⚠️ Suspicious activity patterns

The system works as a **secure document locker**, allowing authenticated users to store and access important documents. Behavioral analysis is performed during authentication to identify whether the current interaction is consistent with the user's normal behavior.

When suspicious behavior is detected, additional verification can be required before access is granted.

This approach provides an additional security layer while reducing unnecessary authentication steps for legitimate users.

---

## 🎯 Project Objectives

The main objectives of the project are:

* 🔐 Provide secure user authentication
* 🧠 Analyze behavioral patterns during authentication
* 🚨 Detect suspicious login activity
* 📱 Provide a simple and user-friendly Android interface
* 📁 Protect confidential documents
* 🔑 Provide security-key-based document access
* 📊 Generate a behavioral/risk score
* 🔒 Reduce the risk of unauthorized access

---

## ✨ Key Features

### 👤 User Registration

Users can create an account by providing required personal and authentication details.

### 🔑 Secure Login

Users authenticate using their registered credentials.

### 🧠 Invisible Authentication

The system observes interaction behavior during authentication without requiring a separate visible authentication step for every normal interaction.

### 📊 Behavioral Risk Analysis

The system analyzes behavioral characteristics and generates a risk/security score.

### 📩 OTP Verification

Additional OTP verification can be triggered when suspicious behavior is detected.

### 📁 Secure Document Locker

Users can upload and manage important documents from the application.

### 🔐 Security Key

Documents can be accessed using a unique security key associated with the user's account.

### 🚨 Suspicious Activity Detection

Abnormal behavioral patterns can trigger additional verification or restrict access.

### 👤 Secure Profile

User information can be displayed with sensitive information partially hidden.

### 🚪 Logout

Users can securely exit their authenticated session.

---

# 🔄 System Workflow

```text
                    ┌─────────────────┐
                    │     Register    │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │ Create Security │
                    │      Profile    │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │      Login      │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │ Behavior        │
                    │ Analysis        │
                    └────────┬────────┘
                             │
                  ┌──────────┴──────────┐
                  │                     │
             Normal Behavior      Suspicious Behavior
                  │                     │
                  ▼                     ▼
          ┌───────────────┐      ┌───────────────┐
          │ Authentication│      │ OTP / Extra   │
          │   Successful  │      │ Verification  │
          └───────┬───────┘      └───────┬───────┘
                  │                      │
                  └──────────┬───────────┘
                             ▼
                    ┌─────────────────┐
                    │    Dashboard    │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │ Document Locker │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │ Security Key    │
                    │ Verification    │
                    └─────────────────┘
```

---

# 🧩 Main Modules

## 1. Registration Module

Responsible for creating a new user account.

**Functions:**

* User registration
* User information collection
* Authentication details
* Security-key generation
* Initial behavioral information

---

## 2. Login Module

Provides the primary authentication mechanism.

**Functions:**

* Credential verification
* Login validation
* Behavioral data collection
* Authentication status checking

---

## 3. Invisible Authentication Module

This is the main security component of the system.

It analyzes user interaction patterns such as:

* Typing speed
* Keystroke behavior
* Backspace usage
* Interaction patterns
* OTP entry behavior

The collected behavior is compared with expected user behavior to identify unusual activity.

---

## 4. OTP Verification Module

OTP provides an additional authentication layer when required.

**Process:**

```text
Login
  ↓
Behavior Analysis
  ↓
Suspicious Activity
  ↓
OTP Verification
  ↓
Successful Verification
  ↓
Access Granted
```

---

## 5. Behavioral Analysis Module

The behavioral analysis component evaluates user interaction and produces a security/risk score.

Example:

```text
Behavior Data
     ↓
Analysis
     ↓
Risk Score
     ↓
Authentication Decision
```

---

## 6. Document Locker Module

The document locker allows authenticated users to manage important documents.

**Functions:**

* Upload documents
* View document list
* Select documents
* Secure document access
* Security-key verification

---

## 7. Profile Module

Displays user account information.

Sensitive information can be partially hidden to improve privacy.

---

## 8. Dashboard Module

The dashboard provides a central view of the authenticated user's:

* Profile information
* Behavioral/risk score
* Documents
* Security status
* Logout option

---

# 🔐 Authentication Logic

The system uses behavioral analysis as an additional security layer.

A simplified decision flow is:

```text
                User Login
                    │
                    ▼
          Verify Login Credentials
                    │
                    ▼
           Analyze User Behavior
                    │
          ┌─────────┴─────────┐
          │                   │
          ▼                   ▼
     Normal Pattern      Suspicious Pattern
          │                   │
          ▼                   ▼
    Access / Continue       OTP Required
                              │
                              ▼
                       Verify OTP
                              │
                       ┌──────┴──────┐
                       │             │
                       ▼             ▼
                    Success        Failure
                       │             │
                       ▼             ▼
                  Grant Access     Deny Access
```

---

# 🗄️ Database Design

The system uses database tables to store user-related information and document records.

## User Profile

| Field         | Description                 |
| ------------- | --------------------------- |
| User ID       | Unique user identifier      |
| Email         | Registered email            |
| Risk Score    | Calculated behavioral score |
| Last Login IP | Last recorded login IP      |
| Created Date  | Account creation date       |

## User Documents

| Field       | Description                |
| ----------- | -------------------------- |
| Document ID | Unique document identifier |
| User ID     | Associated user            |
| File Name   | Uploaded document name     |

---

# 🛠️ Technology Stack

| Technology          | Purpose                      |
| ------------------- | ---------------------------- |
| Kotlin              | Application development      |
| Android Studio      | Development environment      |
| XML                 | User interface design        |
| SQLite              | Local database               |
| Android SDK         | Android application platform |
| OTP                 | Additional authentication    |
| Behavioral Analysis | Invisible authentication     |

---

# 💻 Hardware Requirements

* Android smartphone or compatible development device
* Minimum 2 GB RAM
* Recommended 4 GB RAM
* Minimum 200 MB available storage
* Internet connection for services requiring online communication

---

# 💻 Software Requirements

* Windows / compatible development environment
* Android Studio
* Android SDK
* Kotlin
* SQLite
* Compatible Android device or emulator

---

# 📱 Application Screens

The application contains screens such as:

```text
┌─────────────────────────┐
│       App Opening       │
└────────────┬────────────┘
             ▼
┌─────────────────────────┐
│      Registration       │
└────────────┬────────────┘
             ▼
┌─────────────────────────┐
│          Login          │
└────────────┬────────────┘
             ▼
┌─────────────────────────┐
│      OTP Verification   │
└────────────┬────────────┘
             ▼
┌─────────────────────────┐
│        Dashboard        │
├─────────────────────────┤
│ Behavioral/Risk Score   │
│ User Profile            │
│ My Documents            │
│ Logout                  │
└─────────────────────────┘
```

---

# 🧪 Testing

The system can be validated using multiple levels of testing.

### Unit Testing

Individual components are tested independently.

Examples:

* Login validation
* OTP verification
* Behavioral score calculation
* Document operations

### Integration Testing

Multiple modules are tested together.

Example:

```text
Login + Behavior Analysis + OTP
```

### System Testing

The complete application is tested as an integrated system.

### Security Testing

Authentication and unauthorized-access scenarios are tested.

### Performance Testing

The responsiveness of authentication, dashboard loading, and document operations is evaluated.

---

# 📋 Sample Test Cases

| Test Case           | Input                | Expected Result          |
| ------------------- | -------------------- | ------------------------ |
| Registration        | Valid user details   | Account created          |
| Login               | Correct credentials  | Authentication continues |
| Normal Behavior     | Expected interaction | User proceeds            |
| Suspicious Behavior | Abnormal interaction | Additional verification  |
| OTP                 | Correct OTP          | Access granted           |
| OTP                 | Incorrect OTP        | Access denied            |
| Document Upload     | Valid file           | File added to locker     |
| Document Access     | Correct security key | Document accessible      |
| Document Access     | Incorrect key        | Access restricted        |

---

# 📊 Expected Benefits

The Invisible Authentication System provides:

* 🔐 Additional authentication security
* 🧠 Behavior-based user verification
* 🚨 Suspicious activity detection
* 📁 Protected document management
* 👤 Improved user identity verification
* ⚡ Reduced unnecessary authentication steps
* 🛡️ Additional protection against unauthorized access

---

# 🌍 Applications

The concept can be applied to:

* 🏦 Banking applications
* ☁️ Cloud storage platforms
* 📁 Confidential document management
* 🏢 Enterprise applications
* 🎓 Educational document systems
* 🔐 Personal data storage
* 💳 Financial applications

---

# 🚀 Future Scope

The system can be enhanced with advanced technologies.

### 🤖 Artificial Intelligence & Machine Learning

* Improve behavioral analysis
* Train personalized user behavior models
* Improve anomaly detection
* Adaptive risk scoring

### 👆 Biometric Authentication

Future versions can integrate:

* Fingerprint recognition
* Face recognition
* Voice recognition

### ⛓️ Blockchain

Blockchain technology could be explored for:

* Data integrity
* Tamper detection
* Decentralized security records

### ☁️ Cloud Integration

Cloud support could provide:

* Secure remote document storage
* Multi-device access
* Cloud backup

### 📡 IoT Integration

Authentication could be extended across connected devices.

### 🚨 Real-Time Threat Detection

Future versions could provide:

* Real-time suspicious activity detection
* Security alerts
* Continuous authentication
* Automated threat responses

---

# 📂 Project Structure

A simplified project structure:

```text
Invisible-Authentication-System/
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       ├── res/
│   │       │   ├── drawable/
│   │       │   ├── layout/
│   │       │   ├── mipmap/
│   │       │   └── values/
│   │       └── AndroidManifest.xml
│   │
│   └── build.gradle
│
├── gradle/
├── .gitignore
├── build.gradle
├── settings.gradle
└── README.md
```

---

# 🎓 Academic Project

**Project:** Invisible Authentication System

**Institution:** Tapi Diploma Engineering College

**Domain:** Android Application Development & Cyber Security

**Project Type:** Secure Authentication & Document Locker

---

# 🏢 Organization

**Briskera Infotech Private Limited**

The project was developed in the context of mobile application development and security-focused software engineering.

---

# 📚 References

* Invisible Authentication concepts
* Android Development documentation
* Kotlin documentation
* SQLite documentation
* diagrams.net
* W3Schools
* GeeksforGeeks
* Edraw
* Napkin AI

---

# 👩‍💻 Author

**Roshni Undhad**

Computer Engineering Student • Full-Stack Developer • ML Enthusiast

---

## ⭐ Project

If you find this project useful or interesting, consider giving the repository a ⭐.

**Invisible Authentication System — Secure Authentication Through Behavioral Analysis.**
