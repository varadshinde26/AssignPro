# PHP Supporting Service

- `notify.php` — email notification service used by the Java application.
- `upload.php` — supporting upload endpoint.
- `health.php` — service/database health check.

## Real email on Windows/XAMPP

PHP `mail()` is NOT automatically configured on a normal XAMPP Windows installation. This project therefore supports direct SMTP with PHP streams and no external PHP package.

1. Copy `config.local.example.php` to `config.local.php`.
2. For Gmail use:
   - SMTP host: `smtp.gmail.com`
   - Port: `587`
   - Encryption: `tls`
   - SMTP username: your Gmail address
   - SMTP password: a Gmail **App Password**, not your normal Gmail password
   - mail_from: the same Gmail address
3. Enable 2-Step Verification on the Gmail account and create an App Password.
4. Start Apache in XAMPP.
5. Open `http://localhost/smart-hospital-php/health.php` if this folder is deployed as `htdocs/smart-hospital-php`.
6. Do not commit `config.local.php` to GitHub.

For another provider, replace host/port/encryption according to its SMTP settings.

The Java application continues the duty workflow even if email delivery fails; the failure is recorded in `audit_logs` and the notification remains in-app.
