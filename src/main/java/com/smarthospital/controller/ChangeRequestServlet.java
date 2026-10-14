package com.smarthospital.controller;

import com.smarthospital.model.User;
import com.smarthospital.service.PhpBridge;
import com.smarthospital.util.Access;
import com.smarthospital.util.Audit;
import com.smarthospital.util.DB;
import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@WebServlet("/change-requests")
public class ChangeRequestServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u=(User)req.getSession().getAttribute("user");
        if(Access.isStaff(u)){resp.sendError(403,"Manager access required");return;}
        try(Connection c=DB.getConnection()){
            List<Map<String,Object>> rows=new ArrayList<>();
            String pred=Access.departmentPredicate(u,"d.department_id",new ArrayList<>(),c);
            List<Object> ps=new ArrayList<>(); pred=Access.departmentPredicate(u,"d.department_id",ps,c);
            String q="SELECT cr.request_id,cr.assignment_id,s.name,s.staff_code,d.duty_name,d.duty_date,d.start_time,d.end_time,dp.department_name,cr.reason,cr.previous_assignment_status,cr.status,cr.response_note,cr.created_at,COALESCE(ru.full_name,ru.username,'') FROM duty_change_requests cr JOIN duty_assignments da ON cr.assignment_id=da.assignment_id JOIN staff s ON cr.staff_id=s.staff_id JOIN duties d ON da.duty_id=d.duty_id JOIN departments dp ON d.department_id=dp.department_id LEFT JOIN users ru ON cr.responded_by=ru.user_id WHERE "+pred+" ORDER BY CASE cr.status WHEN 'PENDING' THEN 0 ELSE 1 END,cr.created_at DESC";
            try(PreparedStatement p=c.prepareStatement(q)){for(int i=0;i<ps.size();i++)p.setObject(i+1,ps.get(i));try(ResultSet r=p.executeQuery()){while(r.next()){Map<String,Object> m=new LinkedHashMap<>();for(int i=1;i<=15;i++)m.put("c"+i,r.getObject(i));rows.add(m);}}}
            req.setAttribute("rows",rows); req.getRequestDispatcher("/change-requests.jsp").forward(req,resp);
        }catch(Exception e){throw new ServletException(e);}
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User u=(User)req.getSession().getAttribute("user");
        if(Access.isStaff(u)){resp.sendError(403,"Manager access required");return;}
        String action=req.getParameter("action");
        try(Connection c=DB.getConnection()){
            int id=Integer.parseInt(req.getParameter("request_id"));
            String note=req.getParameter("response_note"); if(note==null) note=""; note=note.trim();
            if(note.length()>1000) throw new IllegalArgumentException("Response note is too long.");
            c.setAutoCommit(false);
            int assignmentId, staffId, dutyId, deptId; String prev, current;
            String staffName="", staffEmail="", dutyName="";
            try(PreparedStatement p=c.prepareStatement("SELECT cr.assignment_id,cr.staff_id,cr.previous_assignment_status,cr.status,da.duty_id,d.department_id,s.name,COALESCE(s.email,''),d.duty_name FROM duty_change_requests cr JOIN duty_assignments da ON cr.assignment_id=da.assignment_id JOIN duties d ON da.duty_id=d.duty_id JOIN staff s ON cr.staff_id=s.staff_id WHERE cr.request_id=? FOR UPDATE")){
                p.setInt(1,id); try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Change request not found."); assignmentId=r.getInt(1);staffId=r.getInt(2);prev=r.getString(3);current=r.getString(4);dutyId=r.getInt(5);deptId=r.getInt(6);staffName=r.getString(7);staffEmail=r.getString(8);dutyName=r.getString(9);}}
            if(!Access.isAdmin(u)&&!Access.supervisorHasDepartment(c,u.id(),deptId)) throw new SecurityException("You can respond only to requests from your assigned department(s).");
            if(!"PENDING".equals(current)) throw new IllegalArgumentException("This request has already been responded to.");
            boolean approve="approve".equals(action); boolean reject="reject".equals(action); if(!approve&&!reject)throw new IllegalArgumentException("Invalid response action.");
            String newReq=approve?"APPROVED":"REJECTED";
            try(PreparedStatement p=c.prepareStatement("UPDATE duty_change_requests SET status=?,responded_by=?,response_note=?,responded_at=NOW() WHERE request_id=?")){p.setString(1,newReq);p.setInt(2,u.id());p.setString(3,note);p.setInt(4,id);p.executeUpdate();}
            if(approve){
                try(PreparedStatement p=c.prepareStatement("UPDATE duty_assignments SET status='CANCELLED' WHERE assignment_id=?")){p.setInt(1,assignmentId);p.executeUpdate();}
                try(PreparedStatement p=c.prepareStatement("UPDATE duties SET status='OPEN' WHERE duty_id=? AND NOT EXISTS (SELECT 1 FROM duty_assignments WHERE duty_id=? AND status NOT IN ('CANCELLED'))")){p.setInt(1,dutyId);p.setInt(2,dutyId);p.executeUpdate();}
            } else {
                try(PreparedStatement p=c.prepareStatement("UPDATE duty_assignments SET status=? WHERE assignment_id=?")){p.setString(1,prev);p.setInt(2,assignmentId);p.executeUpdate();}
            }
            String msg="Change request for '"+dutyName+"' was "+(approve?"APPROVED":"REJECTED")+"."+(note.isEmpty()?"":" Response: "+note);
            Integer staffUser=null; try(PreparedStatement p=c.prepareStatement("SELECT user_id FROM staff WHERE staff_id=?")){p.setInt(1,staffId);try(ResultSet r=p.executeQuery()){if(r.next()&&r.getObject(1)!=null)staffUser=r.getInt(1);}}
            if(staffUser!=null){try(PreparedStatement p=c.prepareStatement("INSERT INTO notifications(user_id,message,type) VALUES(?,?,?)")){p.setInt(1,staffUser);p.setString(2,msg);p.setString(3,"CHANGE_REQUEST_RESPONSE");p.executeUpdate();}}
            c.commit();
            Audit.log(u.id(),"CHANGE_REQUEST_RESPONSE",newReq+" request #"+id);
            if(staffEmail!=null&&!staffEmail.isBlank()) PhpBridge.sendDutyMail(staffEmail,staffName,msg);
            resp.sendRedirect(req.getContextPath()+"/change-requests");
        }catch(SecurityException e){resp.sendError(403,e.getMessage());}catch(Exception e){resp.sendError(400,e.getMessage());}
    }
}
