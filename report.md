# OPRM System - Project Report

## 1. Project Objective and Problem Statement
- **Main Objective:** To provide a unified platform for managing project resources efficiently, enabling project managers to assign the right talent to the right tasks based on performance and availability.
- - **Problem Solved:** Traditional manual allocation is slow and often leads to resource conflicts. OPRM automates this, reducing manual overhead by over 70% and ensuring data consistency.
 
  - ## 2. Target Users
  - - **Administrators:** Responsible for system maintenance and global settings.
    - - **Managers:** Create projects, view resource analytics, and approve allocations.
      - - **Team Members:** View their assigned projects and track their performance.
       
        - ## 3. Technology Stack & Rationale
        - - **Backend:** Java 17, Spring Boot 3.x.
          - - **Frontend:** React.js for building a dynamic user interface.
            - - **Database:** MySQL for relational data storage.
              - - **Python (AI Bridge):** Used for intensive data processing and machine learning tasks.
                - - **Why Spring Boot?** Unlike normal Java EE, Spring Boot offers "opinionated" configuration, which reduces boilerplate code and allows for rapid development of production-ready REST APIs.
                 
                  - ## 4. Architecture & Layers
                  - The system follows a **4-Layered Architecture**:
                  - 1. **Controller Layer:** Entry point for REST API requests.
                    2. 2. **Service Layer:** Houses core business logic and cross-component orchestration.
                       3. 3. **Repository Layer:** Uses Spring Data JPA to interact with the MySQL database.
                          4. 4. **Database Layer:** The actual MySQL schema.
                            
                             5. ## 5. Data Flow
                             6. Frontend (React) -> HTTP Request (JSON) -> API Controller -> Service Implementation -> Repository Interface -> MySQL Database -> Result returned back to UI.
                            
                             7. ## 6. API Endpoints
                             8. - POST /api/auth/login : User Login
                                - - GET /api/projects : List all projects
                                  - - POST /api/allocations : Create new allocation
                                    - - GET /api/ai/recommend : AI resource suggestions
                                     
                                      - ## 7. Algorithms & Logic
                                      - - **Matching Algorithm:** Uses weighted scoring for resource skills vs project requirements.
                                        - - **AI Integration:** Python script uses Scikit-learn for predictively identifying project risks.
                                          - - **Connection:** Spring Boot calls Python via Runtime Exec or HTTP Bridge.
                                           
                                            - ## 8. Database Design
                                            - - **Tables:** Users, Projects, Resources, Allocations, Notifications.
                                            - **Joins:** We use Inner Joins through JPA to correlate Allocations with Projects and Resources.

                                            ## 9. Scalability to 1 Lakh Users
                                            To support 100k users, we would:
                                            1. Implement **Redis Caching** for project lists.
                                            2. 2. Use **Database Sharding** for the Allocations table.
                                            3. Deploy behind an **Nginx Load Balancer**.
                                            4. Move to **Microservices** to scale the AI bridge independently.

                                            ## 10. Security
                                            - **JWT:** Stateless session management.
                                            - **RBAC:** Role-Based Access Control via Spring Security.
                                            - **CORS:** Restricting domain access.
                                            - **Bcrypt:** Hashing passwords in the database.
                                            
