<?php
// Safe defaults for local development. For real email, create config.local.php
// from config.local.example.php and keep it OUT of GitHub.
$local = __DIR__ . '/config.local.php';
$localCfg = file_exists($local) ? require $local : [];
if (!is_array($localCfg)) $localCfg = [];
return array_merge([
  'db_host' => getenv('HOSPITAL_DB_HOST') ?: '127.0.0.1',
  'db_name' => getenv('HOSPITAL_DB_NAME') ?: 'smart_hospital',
  'db_user' => getenv('HOSPITAL_DB_USER') ?: 'root',
  'db_pass' => getenv('HOSPITAL_DB_PASSWORD') ?: '',
  'service_token' => getenv('HOSPITAL_PHP_TOKEN') ?: 'smart-hospital-local-token',
  'smtp_host' => getenv('HOSPITAL_SMTP_HOST') ?: '',
  'smtp_port' => (int)(getenv('HOSPITAL_SMTP_PORT') ?: 587),
  'smtp_encryption' => getenv('HOSPITAL_SMTP_ENCRYPTION') ?: 'tls',
  'smtp_username' => getenv('HOSPITAL_SMTP_USERNAME') ?: '',
  'smtp_password' => getenv('HOSPITAL_SMTP_PASSWORD') ?: '',
  'mail_from' => getenv('HOSPITAL_MAIL_FROM') ?: '',
  'mail_from_name' => getenv('HOSPITAL_MAIL_FROM_NAME') ?: 'AssignPro Hospital Operations',
], $localCfg);
