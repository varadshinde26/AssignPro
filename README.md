# AssignPro — Smart Hospital Ground Staff Management and Workload-Aware Duty Allocation System

A role-based hospital workforce web application for managing ground-level staff, duties, department-scoped supervision, workload-aware allocation and staff-submitted work proof.

## Stack
- JSP + HTML/CSS/JavaScript/AJAX
- React 18 (small progressive enhancement for live department search; not a replacement for JSP/Servlet)
- Java Servlets
- JDBC
- MySQL
- PHP supporting notification/email service
- Apache Tomcat 9 (`javax.servlet`)

## Core role rules
| Feature | Admin | Supervisor | Staff |
|---|---|---|---|
| Dashboard | Hospital-wide | Assigned departments | Personal |
| Add/manage staff | Yes | No | No |
| View staff | All | Assigned departments only | No |
| Supervisor management | Yes | No | No |
| Create duties | All departments | Assigned departments only | No |
| Smart allocation | All | Assigned departments only | No |
| Leave approval | All | Assigned departments only | No |
| Attendance | All | Assigned departments only | Own |
| Upload work proof | **No** | **No** | **Yes** |
| Review work proof | Yes | Assigned departments only | No |
| Mark work done | Yes | Assigned departments only | No |
| Reports | Hospital-wide | Assigned departments | No |

## Work proof rule
A staff member uploads evidence only against an assigned duty. The file is stored in `documents` with a verification state. Admin/Supervisor can inspect the file and choose **Work done** (VERIFY) or **Reject**. Verification closes the assignment and, when all active assignments are complete, closes the duty.

## Supervisor management
Admin creates supervisor accounts from **Supervisor Management**, assigns one or more departments, and can activate/deactivate/reset password. The mapping is stored in `supervisor_departments`.

## Demo ICU workforce
The bootstrap seeds the demonstration ICU duty with an eligible additional staff member:
- Ramesh Patil — `STF-104` — ICU Attendant — 4.0 years experience
- Skills: Patient Handling, Basic First Aid, ICU Equipment Assistance, Infection Control
- Availability: full-day availability for the next 31 days

For the demo duty **ICU Ground Support Demonstration** (tomorrow, 09:00–13:00), Ramesh is eligible and should appear in Smart Allocation. Because he starts with no assigned workload and has 4 years of experience, the configured score will normally place him ahead of Rahul Patil for this demo duty.

## Smart allocation
Hard eligibility checks happen before scoring:
- active staff
- department match
- role match
- all required skills
- approved leave exclusion
- availability window
- overlapping duty exclusion

Suitability score uses the configured weights:
- Skill Match 30%
- Availability 25%
- Workload 20%
- Experience 15%
- Fairness 10%

## Deployment
See `START_HERE.txt` and `docs/IMPLEMENTATION_MAP.md`.


Ramesh Patil demo staff account:
Username: ramesh101
Password: ramesh@123
Work area: ICU
Role: ICU Attendant
Skills seeded for Smart Allocation: Patient Handling, Basic First Aid, ICU Equipment Assistance, Infection Control.

Staff creation: Admin can optionally create a staff login and must select verified skills in the Add Staff form. Required skills are enforced by Smart Allocation. After a supervisor assigns a duty, the assigned staff dashboard and My Duties page read the assignment directly from duty_assignments.

## Corrected staff management and Smart Allocation
- Administrator has Add/Manage/Edit/Deactivate/Reactivate staff controls.
- Staff management includes verified skills and optional staff login.
- Editing replaces the staff member's skill set instead of leaving stale skills.
- Smart Allocation performs explicit hard eligibility checks for department, role, required verified skills, approved leave, availability, and overlapping duties.
- Allocation errors are returned to the UI instead of silently appearing as an empty recommendation list.
- The ICU demonstration duty is repaired on application startup if an older copy exists.
- Ramesh Patil (STF-104) is seeded as an ICU Attendant with Patient Handling and other ICU skills and a staff login.

## Email notifications

The PHP supporting service now supports direct SMTP without a third-party PHP package. Copy `php-service/config.local.example.php` to `php-service/config.local.php` and configure your SMTP provider. For Gmail use an App Password with `smtp.gmail.com:587` and TLS. Never commit `config.local.php`.

Deploy the `php-service` folder as `C:\xampp\htdocs\smart-hospital-php` so the Java app can call `http://localhost/smart-hospital-php/notify.php`.

## Duty change requests

Staff can request a change from My Duties before starting a duty. The request includes a reason and creates a `duty_change_requests` record. Admin and department-assigned supervisors have a `Change Requests` page where they can approve or reject with a response note. Approval cancels the current assignment and reopens the duty for reassignment; rejection restores the previous assignment state and notifies the staff member.


## AssignPro additions
- **Department Management**: Admin-only menu option to create, edit and safely delete unused departments. Departments referenced by staff, duties or supervisor assignments are protected from deletion.
- **Mobile camera proof**: Staff can choose an existing proof file or use the phone camera through the `capture="environment"` input. The same existing Servlet upload validation, 5 MB limit, database record and review workflow are retained. Camera support depends on the mobile browser/device and generally requires HTTPS outside localhost.
- **React enhancement**: React 18 powers live search and visible-result counts on Department Management. JSP, Servlets, JDBC, MySQL and the PHP supporting service remain the core architecture.
- **Branding/UI**: The application is branded AssignPro, with responsive navigation, improved visual hierarchy and mobile-friendly layouts.
