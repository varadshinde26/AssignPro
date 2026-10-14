package com.smarthospital.controller;

import com.smarthospital.model.User;
import com.smarthospital.util.DB;
import com.smarthospital.util.PasswordUtil;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

/** Administrator-only self-service profile and credential management. */
@WebServlet("/admin-profile")
public class AdminProfileServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user=(User)req.getSession(false).getAttribute("user");
        if(user==null){resp.sendRedirect(req.getContextPath()+"/login.jsp");return;}
        if(!"ADMIN".equals(user.role())){resp.sendError(HttpServletResponse.SC_FORBIDDEN,"Administrator access only.");return;}
        try(Connection c=DB.getConnection(); PreparedStatement p=c.prepareStatement("SELECT username,full_name,email,role,status,created_at FROM users WHERE user_id=?")){
            p.setInt(1,user.id());
            try(ResultSet r=p.executeQuery()){
                if(!r.next()){resp.sendError(404,"Administrator account not found.");return;}
                req.setAttribute("profileUsername",r.getString("username"));
                req.setAttribute("profileName",r.getString("full_name"));
                req.setAttribute("profileEmail",r.getString("email"));
                req.setAttribute("profileRole",r.getString("role"));
                req.setAttribute("profileStatus",r.getString("status"));
                req.setAttribute("profileCreated",r.getTimestamp("created_at"));
            }
            req.setAttribute("pageTitle","Admin Profile");
            req.getRequestDispatcher("/admin-profile.jsp").forward(req,resp);
        }catch(SQLException e){throw new ServletException("Unable to load administrator profile.",e);}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User user=(User)req.getSession(false).getAttribute("user");
        if(user==null){resp.sendRedirect(req.getContextPath()+"/login.jsp");return;}
        if(!"ADMIN".equals(user.role())){resp.sendError(HttpServletResponse.SC_FORBIDDEN,"Administrator access only.");return;}
        req.setCharacterEncoding("UTF-8");
        String name=clean(req.getParameter("full_name")),email=clean(req.getParameter("email"));
        String current=val(req.getParameter("current_password")),next=val(req.getParameter("new_password")),confirm=val(req.getParameter("confirm_password"));
        if(name.isBlank()||name.length()>120){fail(req,resp,"Enter a name between 1 and 120 characters.");return;}
        if(!email.isBlank()&&(email.length()>150||!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))){fail(req,resp,"Enter a valid email address.");return;}
        boolean changing=!next.isBlank()||!confirm.isBlank()||!current.isBlank();
        if(changing&&(current.isBlank()||next.length()<8||!next.equals(confirm))){fail(req,resp,"To change your password, enter your current password, and make sure the new password is at least 8 characters and both new-password fields match.");return;}
        try(Connection c=DB.getConnection()){
            c.setAutoCommit(false);
            try(PreparedStatement p=c.prepareStatement("SELECT password_hash FROM users WHERE user_id=? AND role='ADMIN' FOR UPDATE")){
                p.setInt(1,user.id());String hash;
                try(ResultSet r=p.executeQuery()){if(!r.next())throw new SecurityException("Administrator account was not found.");hash=r.getString(1);}
                if(changing&&!PasswordUtil.verify(current,hash))throw new SecurityException("Current password is incorrect.");
                String sql=changing?"UPDATE users SET full_name=?,email=?,password_hash=? WHERE user_id=? AND role='ADMIN'":"UPDATE users SET full_name=?,email=? WHERE user_id=? AND role='ADMIN'";
                try(PreparedStatement u=c.prepareStatement(sql)){
                    u.setString(1,name);u.setString(2,email.isBlank()?null:email);
                    if(changing){u.setString(3,PasswordUtil.hash(next));u.setInt(4,user.id());}else u.setInt(3,user.id());
                    if(u.executeUpdate()!=1)throw new SQLException("Profile update did not complete.");
                }
                c.commit();
            }catch(Exception e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
            req.getSession().setAttribute("profileFlash","Profile updated successfully.");
            resp.sendRedirect(req.getContextPath()+"/admin-profile");
        }catch(SecurityException e){fail(req,resp,e.getMessage());}catch(Exception e){throw new ServletException("Unable to update administrator profile.",e);}
    }
    private void fail(HttpServletRequest req,HttpServletResponse resp,String msg)throws ServletException,IOException{req.setAttribute("profileError",msg);doGet(req,resp);}
    private String clean(String s){return s==null?"":s.trim();}
    private String val(String s){return s==null?"":s;}
}
