# Email notifications and duty-change requests

## Real email setup on Windows/XAMPP

PHP `mail()` is normally not configured on Windows XAMPP. The project now sends SMTP directly using PHP streams.

1. Copy `php-service/config.local.example.php` to `php-service/config.local.php`.
2. For Gmail:
   - Enable 2-Step Verification.
   - Create a Google App Password.
   - Set `smtp_host` to `smtp.gmail.com`.
   - Set `smtp_port` to `587`.
   - Set `smtp_encryption` to `tls`.
   - Set `smtp_username` and `mail_from` to the Gmail address.
   - Set `smtp_password` to the App Password (not the normal Gmail password).
3. Copy the `php-service` folder to `C:\xampp\htdocs\smart-hospital-php`.
4. Start Apache and MySQL in XAMPP.
5. Open `http://localhost/smart-hospital-php/health.php`.
6. Make sure the staff/supervisor email stored in the database is a real address.
7. Assign a duty or create a duty-change request. The PHP endpoint returns an error if SMTP fails; the Java workflow remains available and the failure is recorded in `audit_logs`.

Do not commit `config.local.php` to GitHub.

## Staff duty-change workflow

1. Staff opens `My Duties`.
2. For an ASSIGNED or ACKNOWLEDGED duty, click `Request change`.
3. Enter a reason (minimum 5 characters).
4. The assignment becomes `CHANGE_REQUESTED` and a row is created in `duty_change_requests`.
5. The supervisor(s) assigned to that duty's department receive an in-app notification and, if SMTP is configured, an email.
6. Supervisor opens `Change Requests`.
7. Supervisor can `Approve change` or `Reject` and provide a response note.
8. Approve: assignment is cancelled, the duty is reopened, and the staff member is notified.
9. Reject: the assignment returns to its previous status and the staff member is notified.
10. Admin can respond to requests across the hospital; supervisors are limited to their mapped departments.
