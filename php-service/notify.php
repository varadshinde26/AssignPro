<?php
header('Content-Type: application/json');
$c=require __DIR__.'/config.php';
if($_SERVER['REQUEST_METHOD']!=='POST'){http_response_code(405);echo json_encode(['ok'=>false,'message'=>'POST required']);exit;}
$email=filter_var($_POST['email']??'',FILTER_VALIDATE_EMAIL);$name=trim($_POST['name']??'');$message=trim($_POST['message']??'');
if(!$email||$message===''){http_response_code(422);echo json_encode(['ok'=>false,'message'=>'Valid email and message required']);exit;}
$subject=trim($_POST['subject']??'Hospital duty notification');
$from=$c['mail_from'] ?: $c['smtp_username'];
$fromName=$c['mail_from_name'] ?: 'Smart Hospital Operations';
$sent=false;$method='none';$error='';

function smtp_read($fp){$data='';while(($line=fgets($fp,515))!==false){$data.=$line;if(strlen($line)>=4 && $line[3]===' ')break;}return $data;}
function smtp_expect($fp,$codes){$r=smtp_read($fp);$code=(int)substr($r,0,3);if(!in_array($code,$codes,true))throw new Exception('SMTP '.$r);return $r;}
function smtp_send($c,$to,$toName,$subject,$body,$from,$fromName){
  if(!$c['smtp_host']||!$c['smtp_username']||!$c['smtp_password']||!$from) throw new Exception('SMTP is not configured. Create php-service/config.local.php from config.local.example.php.');
  $host=$c['smtp_host'];$port=(int)$c['smtp_port'];$timeout=20;
  $fp=@stream_socket_client('tcp://'.$host.':'.$port,$errno,$errstr,$timeout,STREAM_CLIENT_CONNECT);
  if(!$fp)throw new Exception('SMTP connection failed: '.$errstr.' ('.$errno.')');stream_set_timeout($fp,$timeout);
  try{
    smtp_expect($fp,[220]);fwrite($fp,"EHLO localhost\r\n");smtp_expect($fp,[250]);
    if(($c['smtp_encryption']??'tls')==='tls'){fwrite($fp,"STARTTLS\r\n");smtp_expect($fp,[220]);if(!stream_socket_enable_crypto($fp,true,STREAM_CRYPTO_METHOD_TLS_CLIENT))throw new Exception('Could not enable TLS.');fwrite($fp,"EHLO localhost\r\n");smtp_expect($fp,[250]);}
    fwrite($fp,"AUTH LOGIN\r\n");smtp_expect($fp,[334]);fwrite($fp,base64_encode($c['smtp_username'])."\r\n");smtp_expect($fp,[334]);fwrite($fp,base64_encode($c['smtp_password'])."\r\n");smtp_expect($fp,[235]);
    fwrite($fp,'MAIL FROM:<'. $from .">\r\n");smtp_expect($fp,[250]);fwrite($fp,'RCPT TO:<'. $to .">\r\n");smtp_expect($fp,[250,251]);fwrite($fp,"DATA\r\n");smtp_expect($fp,[354]);
    $safeName=str_replace(["\r","\n"],' ',$fromName);$safeToName=str_replace(["\r","\n"],' ',$toName);$safeSubject=str_replace(["\r","\n"],' ',$subject);
    $headers="From: ".$safeName." <".$from.">\r\n"."To: ".$safeToName." <".$to.">\r\n"."Subject: ".$safeSubject."\r\n"."MIME-Version: 1.0\r\n"."Content-Type: text/plain; charset=UTF-8\r\n"."Content-Transfer-Encoding: 8bit\r\n";
    $data=$headers."\r\n".str_replace(["\r\n","\r","\n"],["\n","\n","\n"],$body);$data=str_replace("\n","\r\n",$data);$data=preg_replace('/\r\n\.\r\n/','\r\n..\r\n',$data);fwrite($fp,$data."\r\n.\r\n");smtp_expect($fp,[250]);fwrite($fp,"QUIT\r\n");smtp_read($fp);return true;
  }finally{fclose($fp);}
}
try{
  if($c['smtp_host']){$sent=smtp_send($c,$email,$name,$subject,$message,$from,$fromName);$method='smtp';}
  else {$headers="MIME-Version: 1.0\r\nContent-Type: text/plain; charset=UTF-8\r\nFrom: ".$fromName." <".$from.">\r\n";$sent=@mail($email,$subject,$message,$headers);$method='php-mail';if(!$sent)$error='PHP mail() returned false. Configure SMTP in config.local.php.';}
}catch(Throwable $e){$error=$e->getMessage();}
try{require __DIR__.'/db.php';$stmt=$pdo->prepare("INSERT INTO audit_logs(user_id,action,description) VALUES(NULL,'PHP_EMAIL_SERVICE',?)");$stmt->execute(["Notification to $name <$email>: ".($sent?'SENT':'FAILED')." via $method".($error?" - $error":"")]);}catch(Throwable $e){}
if(!$sent){http_response_code(502);echo json_encode(['ok'=>false,'mail_sent'=>false,'method'=>$method,'message'=>$error]);exit;}
echo json_encode(['ok'=>true,'mail_sent'=>true,'method'=>$method]);
