package vn.edu.webpro.robotlab.controller;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import vn.edu.webpro.robotlab.business.*;
import vn.edu.webpro.robotlab.data.*;
import vn.edu.webpro.robotlab.util.*;

@WebServlet("/admin-tasks")
public class AdminTaskServlet extends HttpServlet {
    private User user(HttpServletRequest request,HttpServletResponse response) throws SQLException,IOException {
        response.setHeader("Cache-Control","no-store");
        User user=SessionUtil.getCurrentUser(request);
        if(user==null) { response.sendRedirect(request.getContextPath() + "/pages/tai-khoan.html"); return null; }
        if(!user.isAdmin()) {response.sendError(403);return null;}
        return user;
    }
    private void forward(HttpServletRequest request,HttpServletResponse response,String view) throws ServletException,IOException {
        String url="/WEB-INF/views/"+view+".jsp";
        getServletContext().getRequestDispatcher(url).forward(request,response);
    }
    private void invalid(HttpServletRequest request,HttpServletResponse response,IllegalArgumentException error) throws ServletException,IOException {
        response.setStatus(422); request.setAttribute("taskError",error.getMessage()); forward(request,response,"task-error");
    }

    private long[] recipientIds(HttpServletRequest request) {
        String[] values=request.getParameterValues("recipientId");
        if(values==null)return new long[0];
        long[] ids=new long[values.length];
        for(int i=0;i<values.length;i++) {
            try {ids[i]=Long.parseLong(values[i]);if(ids[i]<=0)throw new NumberFormatException();}
            catch(NumberFormatException e){throw new IllegalArgumentException("Người được giao không hợp lệ.");}
        }
        return ids;
    }
    protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        try {
            User user=user(request,response);if(user==null)return;
            request.setAttribute("adminView",true);
            String action=TaskFormUtil.text(request,"action");
            if(action.isEmpty()) {
                request.setAttribute("tasks",PracticeTaskDB.selectTasks(user.getId(),true,TaskFormUtil.text(request,"state")));
                forward(request,response,"admin-task-list");return;
            }
            PracticeTask task;
            if("new".equals(action)) task=new PracticeTask();
            else {task=PracticeTaskDB.selectTask(TaskFormUtil.number(request,"id"),user.getId(),true);if(task==null){response.sendError(404);return;}}
            request.setAttribute("task",task);
            List<TaskRecipient> recipients=PracticeTaskDB.selectRecipients(task.getId(),user.getId(),true);
            request.setAttribute("recipients",recipients);
            request.setAttribute("submissions",TaskSubmissionDB.selectSubmissions(task.getId(),user.getId(),true));
            if("edit".equals(action)||"new".equals(action)) {
                if(!task.isDraft())throw new IllegalArgumentException("Nhiệm vụ OPEN không được sửa nội dung cốt lõi; hãy nhân bản thành nháp.");
                request.setAttribute("robots",RobotDB.selectRobots(100,0));
                learners(request,recipients);
                forward(request,response,"admin-task-form");
            } else if("view".equals(action)||"preview".equals(action)) {
                request.setAttribute("adminPreview", "preview".equals(action));
                learners(request,recipients);
                forward(request,response,"task-view");
            } else response.sendError(404);
        } catch(IllegalArgumentException e){invalid(request,response,e);}
        catch(SQLException e){response.sendError(503);}
    }
    private void learners(HttpServletRequest request,List<TaskRecipient> assigned) throws SQLException {
        String query=TaskFormUtil.text(request,"q");
        int page=1;if(!TaskFormUtil.text(request,"page").isEmpty())page=TaskFormUtil.integer(request,"page");
        if(page<1||page>10000)throw new IllegalArgumentException("Trang không hợp lệ.");
        List<User> candidates=UserDB.selectLearners(query,20,(page-1)*20);
        List<User> available=new ArrayList<>();
        for(User candidate:candidates) {
            boolean exists=false;
            for(TaskRecipient recipient:assigned) if(recipient.getUserId()==candidate.getId())exists=true;
            if(!exists)available.add(candidate);
        }
        request.setAttribute("learners",available);request.setAttribute("searchQuery",query);request.setAttribute("learnerPage",page);
    }
    protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            User user=user(request,response);if(user==null)return;
            if(!SessionUtil.hasValidFormCsrfToken(request,response))return;
            String action=TaskFormUtil.text(request,"action");long id;
            if("saveDraft".equals(action)) id=PracticeTaskDB.saveDraft(TaskFormUtil.task(request),user.getId(),recipientIds(request));
            else {
                java.util.Date due=null;if("extend".equals(action))due=TaskFormUtil.due(request);
                id=PracticeTaskDB.manage(TaskFormUtil.number(request,"id"),action,user.getId(),recipientIds(request),due);
            }
            String url=request.getContextPath()+"/admin-tasks";
            if(id>0)url+="?action=view&id="+id;
            response.sendRedirect(url);
        } catch(IllegalArgumentException e){invalid(request,response,e);}
        catch(SQLException e){response.sendError(503);}
    }
}
