package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import vn.edu.webpro.robotlab.dao.*;
import vn.edu.webpro.robotlab.model.User;
import vn.edu.webpro.robotlab.service.AssemblySessionService;
import vn.edu.webpro.robotlab.web.*;

@WebServlet("/api/assembly-sessions/*")
public final class AssemblySessionServlet extends HttpServlet {
    private final AssemblySessionService service=new AssemblySessionService(new AssemblySessionDao(new DatabaseConnectionFactory(System.getenv())),new RobotDao(new DatabaseConnectionFactory(System.getenv())));
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException{User user=user(req,res);if(user==null)return;try{String body=req.getReader().lines().reduce("",(a,b)->a+b);var session=service.create(user.id(),Json.stringField(body,"robotId"));ApiResponses.json(res,201,"{\"data\":"+session.toJson()+"}");}catch(IllegalArgumentException e){ApiResponses.error(res,422,"VALIDATION_ERROR","Robot không tồn tại hoặc dữ liệu không hợp lệ.");}catch(SQLException e){ApiResponses.error(res,503,"DEPENDENCY_NOT_READY","Cơ sở dữ liệu chưa được cấu hình.");}}
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws IOException{User user=user(req,res);if(user==null)return;try{String path=req.getPathInfo();if(path==null||"/".equals(path)){ApiResponses.error(res,501,"NOT_IMPLEMENTED","Danh sách phiên sẽ được bổ sung ở lát cắt kế tiếp.");return;}long id=Long.parseLong(path.substring(1));ApiResponses.json(res,200,"{\"data\":"+service.get(user.id(),id).toJson()+"}");}catch(IllegalArgumentException e){ApiResponses.error(res,404,"NOT_FOUND","Không tìm thấy phiên lắp ráp.");}catch(SQLException e){ApiResponses.error(res,503,"DEPENDENCY_NOT_READY","Cơ sở dữ liệu chưa được cấu hình.");}}
    @Override protected void doPut(HttpServletRequest req,HttpServletResponse res)throws IOException{User user=user(req,res);if(user==null)return;if(!csrf(req,res))return;try{String[] p=req.getPathInfo().substring(1).split("/");long id=Long.parseLong(p[0]);String body=req.getReader().lines().reduce("",(a,b)->a+b);var session="components".equals(p[1])?service.component(user.id(),id,p[2],Boolean.parseBoolean(Json.stringField(body,"isPrepared"))):"steps".equals(p[1])?service.step(user.id(),id,p[2],Json.stringField(body,"status")):"visual-parts".equals(p[1])?service.visual(user.id(),id,p[2],Boolean.parseBoolean(Json.stringField(body,"isAssembled"))):null;if(session==null){ApiResponses.error(res,404,"NOT_FOUND","Không tìm thấy tài nguyên yêu cầu.");return;}ApiResponses.json(res,200,"{\"data\":"+session.toJson()+"}");}catch(Exception e){ApiResponses.error(res,422,"VALIDATION_ERROR","Dữ liệu phiên lắp ráp không hợp lệ.");}}
    private User user(HttpServletRequest req,HttpServletResponse res)throws IOException{HttpSession s=req.getSession(false);Object value=s==null?null:s.getAttribute("user");if(value instanceof User user)return user;ApiResponses.error(res,401,"AUTH_REQUIRED","Bạn cần đăng nhập để tiếp tục.");return null;}
    private boolean csrf(HttpServletRequest req,HttpServletResponse res)throws IOException{HttpSession s=req.getSession(false);Object token=s==null?null:s.getAttribute("csrfToken");if(token instanceof String expected&&expected.equals(req.getHeader("X-CSRF-Token")))return true;ApiResponses.error(res,403,"CSRF_REQUIRED","Yêu cầu cần mã CSRF hợp lệ.");return false;}
}
