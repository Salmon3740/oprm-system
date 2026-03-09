# OPRM - Online Project Resource Management System
## Full Project Report

### 1. Idea & Abstract
**Idea**: To create a streamlined, AI-integrated platform for academic project management, connecting students with the most relevant mentors and resources.

**Abstract**:
The Online Project Resource Management (OPRM) System is a comprehensive web application designed to facilitate collaboration between students and professors. By integrating machine learning models for domain classification and mentor matching, the system ensures that students find guidance tailored to their project's technical requirements. OPRM automates administrative tasks such as mentorship requests, progress tracking, and resource sharing, while providing a real-time notification system that acts as a logbook for all user activities.

---

### 2. Problem Statement
Academic project management often suffers from:
- **Inefficient Mentorship**: Students struggle to identify professors whose expertise aligns with their project's specific domain.
- **Fragmented Communication**: Project discussions and file shares are often lost in disorganized email threads or external messaging apps.
- **Opaque Progress**: Mentors often lack a clear, real-time view of a student's project milestones and overall progress.
- **Resource Scarcity**: Lack of a centralized repository for specialized research papers, tools, and datasets.

---

### 3. Project Flow
1. **User Onboarding**:
   - Students register with their Registration Numbers and Semester details.
   - Professors register with Faculty IDs and specify their Expertise Domains.
   - Secure ID-based authentication ensures users log in with their unique identifiers.
2. **Project Initiation**:
   - Students submit project titles and descriptions.
   - The AI engine automatically classifies the project into specific IT domains (e.g., Machine Learning, Cyber Security).
3. **Mentor Matching & Request**:
   - The system calculates a similarity score between the project domain and professor expertise.
   - Students receive a ranked list of suggested mentors and send mentorship requests.
4. **Mentorship Management**:
   - Professors review requests on their dashboard and accept/reject them.
   - Upon acceptance, a dedicated communication channel (Chat) is opened.
5. **Execution & Tracking**:
   - Students define milestones and update progress percentages.
   - Mentors receive notifications for every update, allowing for timely feedback.
6. **Communication & Resources**:
   - Integrated chat supports text, file sharing, and resource linking.
   - A Resource Library allows professors to upload materials and students to request access.

---

### 4. Algorithms & AI Integration
- **TF-IDF (Term Frequency-Inverse Document Frequency)**: Used to vectorize text data from project descriptions and professor expertise.
- **Cosine Similarity**: Calculates the cosine of the angle between two vectors to determine the mathematical "closeness" of a project's domain to a professor's expertise.
- **Domain Classification (Random Forest/SVM)**: A pre-trained model classifies project descriptions into 20+ IT sub-domains with high accuracy.
- **Python-Java Bridge**: A custom `AIService` in Spring Boot communicates via a Python script (`ai_bridge.py`) using standardized JSON payloads.

---

### 5. Important Methods
- `UserService.registerUser(user, roleSpecificId)`: Normalizes roles and generates unique secret keys for account recovery.
- `ProjectService.createProject(request)`: Triggers AI classification and initializes the project lifecycle.
- `AIService.suggestMentors(domains)`: Orchestrates the AI matching logic via the Python bridge.
- `ProfessorService.acceptMentorship(profId, projectId)`: Updates project status and triggers dual-role notifications.
- `ChatService.notifyRecipient(project, sender, content)`: Ensures real-time awareness of communication updates.
- `NotificationService.createNotification(user, message, type)`: Powers the "logbook" feature by persisting all critical actions.

---

### 6. Design & Architecture
- **Framework**: Spring Boot (Backend), Thymeleaf (Frontend).
- **Security**: Spring Security with custom ID-based Authentication Provider.
- **Styling**: Bootstrap 5 with custom CSS for a premium, responsive UI.
- **Data Persistence**: Spring Data JPA with support for H2 (dev) and MySQL (prod).
- **Architecture Pattern**: MVC (Model-View-Controller) with a dedicated Service layer for business logic.

---

### 7. Key Features
- **AI-Driven Mentor Suggestions**: Recommends mentors based on technical compatibility scores.
- **Real-Time Notification Log**: Tracks project submissions, status changes, and chat activity.
- **Integrated Chat**: Seamless communication with file attachment and resource sharing capabilities.
- **Resource Management**: Role-based access to a library of academic resources.
- **Milestone Tracking**: Visual progress monitoring for students and mentors.
- **ID-Based Login**: Simplified authentication using university-issued IDs.
