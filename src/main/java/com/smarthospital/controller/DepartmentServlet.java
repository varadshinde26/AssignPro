package com.smarthospital.controller;

import com.smarthospital.model.User;
import com.smarthospital.util.Access;
import com.smarthospital.util.Audit;
import com.smarthospital.util.DB;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/** Admin-only department catalogue management. Existing department IDs remain stable when edited. */
@WebServlet("/departments")
public class DepartmentServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user=(User)req.getSession().getAttribute("user");
        if(!Access.isAdmin(user)){resp.sendError(403,"Only administrators can manage departments.");return;}
        try(Connection c=DB.getConnection()){
            List<Map<String,Object>> rows=new ArrayList<>();
            String q="SELECT d.department_id,d.department_name,d.description,(SELECT COUNT(*) FROM staff s WHERE s.department_id=d.department_id) staff_count,(SELECT COUNT(*) FROM duties du WHERE du.department_id=d.department_id AND du.status<>'CANCELLED') duty_count,(SELECT COUNT(*) FROM supervisor_departments sd WHERE sd.department_id=d.department_id AND sd.active=1) supervisor_count FROM departments d ORDER BY d.department_name";
            try(Statement st=c.createStatement();ResultSet rs=st.executeQuery(q)){while(rs.next()){Map<String,Object> m=new LinkedHashMap<>();m.put("id",rs.getInt(1));m.put("name",rs.getString(2));m.put("description",rs.getString(3));m.put("staff",rs.getInt(4));m.put("duties",rs.getInt(5));m.put("supervisors",rs.getInt(6));rows.add(m);}}
            req.setAttribute("departments",rows);
            String edit=req.getParameter("edit");
            if(edit!=null&&!edit.isBlank())try(PreparedStatement p=c.prepareStatement("SELECT department_id,department_name,description FROM departments WHERE department_id=?")){p.setInt(1,Integer.parseInt(edit));try(ResultSet rs=p.executeQuery()){if(rs.next()){Map<String,Object> m=new HashMap<>();m.put("id",rs.getInt(1));m.put("name",rs.getString(2));m.put("description",rs.getString(3));req.setAttribute("editDepartment",m);}}}
            req.setAttribute("pageTitle","Department Management");
            req.getRequestDispatcher("/departments.jsp").forward(req,resp);
        }catch(Exception e){throw new ServletException("Unable to load departments",e);}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        User user=(User)req.getSession().getAttribute("user");if(!Access.isAdmin(user)){resp.sendError(403,"Only administrators can manage departments.");return;}
        req.setCharacterEncoding("UTF-8");String action=clean(req.getParameter("action"));
        try(Connection c=DB.getConnection()){
            if("save".equals(action)){
                String id=clean(req.getParameter("department_id")),name=clean(req.getParameter("department_name")),description=clean(req.getParameter("description"));
                if(name.isBlank())throw new IllegalArgumentException("Department name is required.");
                if(name.length()>100||description.length()>255)throw new IllegalArgumentException("Department name or description is too long.");
                if(id.isBlank()){
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO departments(department_name,description) VALUES(?,?)")){p.setString(1,name);p.setString(2,description.isBlank()?null:description);p.executeUpdate();}
                    Audit.log(user.id(),"DEPARTMENT_CREATED",name);
                }else{
                    try(PreparedStatement p=c.prepareStatement("UPDATE departments SET department_name=?,description=? WHERE department_id=?")){p.setString(1,name);p.setString(2,description.isBlank()?null:description);p.setInt(3,Integer.parseInt(id));if(p.executeUpdate()==0)throw new IllegalArgumentException("Department not found.");}
                    Audit.log(user.id(),"DEPARTMENT_UPDATED",name+" (#"+id+")");
                }
            }else if("delete".equals(action)){
                int id=Integer.parseInt(req.getParameter("department_id"));String name="Department #"+id;
                try(PreparedStatement p=c.prepareStatement("SELECT department_name FROM departments WHERE department_id=?")){p.setInt(1,id);try(ResultSet rs=p.executeQuery()){if(!rs.next())throw new IllegalArgumentException("Department not found.");name=rs.getString(1);}}
                String[] checks={"SELECT COUNT(*) FROM staff WHERE department_id=?","SELECT COUNT(*) FROM duties WHERE department_id=?","SELECT COUNT(*) FROM supervisor_departments WHERE department_id=?"};
                for(String q:checks)try(PreparedStatement p=c.prepareStatement(q)){p.setInt(1,id);try(ResultSet rs=p.executeQuery()){rs.next();if(rs.getInt(1)>0)throw new IllegalArgumentException("This department is in use by staff, duties, or supervisor assignments. Edit it instead of deleting it.");}}
                try(PreparedStatement p=c.prepareStatement("DELETE FROM departments WHERE department_id=?")){p.setInt(1,id);p.executeUpdate();}Audit.log(user.id(),"DEPARTMENT_DELETED",name);
            }else throw new IllegalArgumentException("Unsupported department action.");
            resp.sendRedirect(req.getContextPath()+"/departments?success=1");
        }catch(SQLIntegrityConstraintViolationException e){resp.sendError(400,"A department with that name already exists, or it is still referenced by another record.");}
        catch(Exception e){resp.sendError(400,e.getMessage()==null?"Unable to save department.":e.getMessage());}
    }
    private String clean(String value){return value==null?"":value.trim();}
}
