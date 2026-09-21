package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import vn.edu.webpro.robotlab.dao.DatabaseConnectionFactory;
import vn.edu.webpro.robotlab.dao.UserDao;
import vn.edu.webpro.robotlab.model.User;
import vn.edu.webpro.robotlab.service.AuthService;
import vn.edu.webpro.robotlab.service.PasswordService;
import vn.edu.webpro.robotlab.web.ApiResponses;
import vn.edu.webpro.robotlab.web.Json;

@WebServlet("/api/auth/*")
public final class AuthServlet extends HttpServlet {
    private final AuthService service=new AuthService(new UserDao(new DatabaseConnectionFactory(System.getenv())),new PasswordService());
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws IOException{if(!"/me".equals(req.getPathInfo())){ApiResponses.error(res,404,"NOT_FOUND","Không tìm thấy tài nguyên yêu cầu.");return;}Object user=req.getSession(false)==null?null:req.getSession(false).getAttribute("user");if(!(user instanceof User value)){ApiResponses.error(res,401,"AUTH_REQUIRED","Bạn cần đăng nhập để tiếp tục.");return;}ApiResponses.json(res,200,"{\"data\":{\"user\":"+value.toJson()+"}}");}
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException{try{String body=req.getReader().lines().reduce("",(a,b)->a+b);String path=req.getPathInfo();if("/logout".equals(path)){HttpSession s=req.getSession(false);if(s!=null)s.invalidate();res.setStatus(204);return;}String email=Json.stringField(body,"email").trim().toLowerCase(),password=Json.stringField(body,"password");User user="/register".equals(path)?service.register(Json.stringField(body,"fullName"),email,password):"/login".equals(path)?service.login(email,password):null;if(user==null){ApiResponses.error(res,404,"NOT_FOUND","Không tìm thấy tài nguyên yêu cầu.");return;}req.getSession(true).setAttribute("user",user);ApiResponses.json(res,"/register".equals(path)?201:200,"{\"data\":{\"user\":"+user.toJson()+"}}");}catch(IllegalStateException e){ApiResponses.error(res,409,"EMAIL_ALREADY_EXISTS","Email đã được sử dụng.");}catch(IllegalArgumentException e){ApiResponses.error(res,"credentials".equals(e.getMessage())?401:422,"credentials".equals(e.getMessage())?"INVALID_CREDENTIALS":"VALIDATION_ERROR","credentials".equals(e.getMessage())?"Email hoặc mật khẩu không đúng.":"Dữ liệu không hợp lệ.");}catch(SQLException e){ApiResponses.error(res,503,"DEPENDENCY_NOT_READY","Cơ sở dữ liệu chưa được cấu hình.");}}
}
