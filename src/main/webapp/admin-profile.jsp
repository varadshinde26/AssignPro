<%@ page import="java.text.SimpleDateFormat" %>
<%@ include file="/WEB-INF/header.jsp" %>
<% if(current==null || !"ADMIN".equals(current.role())) { response.sendError(403,"Administrator access only."); return; }
String pn=(String)request.getAttribute("profileName"); String pe=(String)request.getAttribute("profileEmail"); String pu=(String)request.getAttribute("profileUsername");
Object created=request.getAttribute("profileCreated"); String createdText=created==null?"Not available":new SimpleDateFormat("dd MMM yyyy").format((java.util.Date)created);
String flash=(String)session.getAttribute("profileFlash"); session.removeAttribute("profileFlash");
%>
<div class="profile-hero cardx pad">
  <div class="profile-hero-main"><div class="profile-avatar"><i class="fa-solid fa-user-shield"></i></div><div><div class="eyebrow">ACCOUNT SETTINGS</div><h2><%=pn==null||pn.isBlank()?pu:pn%></h2><p>Manage your administrator identity and account security.</p><span class="profile-status"><span></span> Active administrator</span></div></div>
  <div class="profile-hero-side"><span class="profile-role"><i class="fa-solid fa-shield-halved"></i> ADMINISTRATOR</span><small>AssignPro · Hospital Workforce</small></div>
</div>
<% if(flash!=null){ %><div class="alert alert-success mt-3"><i class="fa-solid fa-circle-check me-2"></i><%=flash%></div><% } %>
<% if(request.getAttribute("profileError")!=null){ %><div class="alert alert-danger mt-3"><i class="fa-solid fa-triangle-exclamation me-2"></i><%=request.getAttribute("profileError")%></div><% } %>
<div class="profile-layout mt-4">
 <section class="cardx profile-form-card"><div class="profile-card-heading"><div class="intro-icon"><i class="fa-solid fa-id-card"></i></div><div><h3>Personal information</h3><p>Update the details associated with your account.</p></div></div>
  <form method="post" action="<%=request.getContextPath()%>/admin-profile" class="profile-form">
   <div class="profile-field"><label for="full_name">Full name</label><input class="form-control" id="full_name" name="full_name" maxlength="120" required value="<%=esc(pn)%>" placeholder="Enter your full name"></div>
   <div class="profile-field"><label for="username">Username</label><input class="form-control readonly-field" id="username" value="<%=esc(pu)%>" readonly><small>Your login username cannot be changed here.</small></div>
   <div class="profile-field"><label for="email">Email address</label><input class="form-control" type="email" id="email" name="email" maxlength="150" value="<%=esc(pe)%>" placeholder="admin@hospital.com"></div>
   <div class="profile-section-title"><span class="profile-section-icon"><i class="fa-solid fa-lock"></i></span><div><strong>Change password</strong><small>Leave these fields blank if you do not want to change it.</small></div></div>
   <div class="profile-field"><label for="current_password">Current password</label><input class="form-control" type="password" id="current_password" name="current_password" autocomplete="current-password" placeholder="Enter current password"></div>
   <div class="profile-two-fields"><div class="profile-field"><label for="new_password">New password</label><input class="form-control" type="password" id="new_password" name="new_password" minlength="8" autocomplete="new-password" placeholder="At least 8 characters"></div><div class="profile-field"><label for="confirm_password">Confirm new password</label><input class="form-control" type="password" id="confirm_password" name="confirm_password" minlength="8" autocomplete="new-password" placeholder="Repeat new password"></div></div>
   <div class="profile-form-actions"><span><i class="fa-solid fa-shield-halved me-1"></i> Passwords are stored as secure hashes.</span><button class="btn-main" type="submit"><i class="fa-solid fa-floppy-disk"></i> Save changes</button></div>
  </form>
 </section>
 <aside class="profile-aside"><section class="cardx pad"><div class="profile-card-heading compact"><div class="intro-icon"><i class="fa-solid fa-shield-halved"></i></div><div><h3>Account security</h3><p>Administrator access overview</p></div></div><div class="security-row"><span class="security-icon"><i class="fa-solid fa-user-lock"></i></span><div><strong>Role permissions</strong><small>Full system administration</small></div><span class="security-check"><i class="fa-solid fa-check"></i></span></div><div class="security-row"><span class="security-icon"><i class="fa-solid fa-key"></i></span><div><strong>Password protection</strong><small>Secure password hashing enabled</small></div><span class="security-check"><i class="fa-solid fa-check"></i></span></div><div class="security-row"><span class="security-icon"><i class="fa-solid fa-clock"></i></span><div><strong>Session timeout</strong><small>Automatic timeout after inactivity</small></div></div></section>
 <section class="cardx pad account-details"><h3>Account details</h3><div class="detail-line"><span>Account role</span><strong>Administrator</strong></div><div class="detail-line"><span>Account created</span><strong><%=createdText%></strong></div><div class="detail-line"><span>Account status</span><strong class="text-success">Active</strong></div><div class="detail-line"><span>Application</span><strong>AssignPro</strong></div><a class="btn-soft w-100 mt-3" href="<%=request.getContextPath()%>/dashboard"><i class="fa-solid fa-arrow-left"></i> Back to dashboard</a></section></aside>
</div>
<%!
private String esc(String v){if(v==null)return "";return v.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");}
%>
<%@ include file="/WEB-INF/footer.jsp" %>
