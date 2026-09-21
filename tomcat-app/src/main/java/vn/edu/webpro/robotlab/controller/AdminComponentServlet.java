package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.dao.ComponentDao;
import vn.edu.webpro.robotlab.dao.DatabaseConnectionFactory;
import vn.edu.webpro.robotlab.model.Component;
import vn.edu.webpro.robotlab.web.AdminAccess;
import vn.edu.webpro.robotlab.web.ApiResponses;
import vn.edu.webpro.robotlab.web.Json;

@WebServlet("/api/admin/components/*")
public final class AdminComponentServlet extends HttpServlet {
    private final ComponentDao components = new ComponentDao(new DatabaseConnectionFactory(System.getenv()));
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (AdminAccess.requireAdmin(request, response) == null) return;
        try {
            var page = components.list(1, 100);
            ApiResponses.json(response, 200, page.toJson());
        } catch (SQLException exception) {
            ApiResponses.error(response, 503, "DEPENDENCY_NOT_READY", "Cơ sở dữ liệu chưa được cấu hình.");
        }
    }
    @Override protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (AdminAccess.requireAdmin(request, response) == null || !AdminAccess.requireCsrf(request, response)) return;
        String path = request.getPathInfo();
        String id = path == null ? "" : path.substring(1);
        if (!id.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) {
            ApiResponses.error(response, 422, "VALIDATION_ERROR", "ID không hợp lệ.");
            return;
        }
        try {
            if (!components.remove(id)) {
                ApiResponses.error(response, 404, "NOT_FOUND", "Không tìm thấy nội dung.");
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException exception) {
            ApiResponses.error(response, 409, "RELATION_CONFLICT", "Không thể xóa nội dung đang được sử dụng.");
        }
    }
    @Override protected void doPost(HttpServletRequest request,HttpServletResponse response)throws IOException{write(request,response,null,true);}
    @Override protected void service(HttpServletRequest request,HttpServletResponse response)throws javax.servlet.ServletException,IOException{if("PATCH".equals(request.getMethod())){write(request,response,request.getPathInfo()==null?null:request.getPathInfo().substring(1),false);return;}super.service(request,response);}
    private void write(HttpServletRequest request,HttpServletResponse response,String existingId,boolean creating)throws IOException{if(AdminAccess.requireAdmin(request,response)==null||!AdminAccess.requireCsrf(request,response))return;try{String body=request.getReader().lines().reduce("",(a,b)->a+b);Component value=input(body,creating?null:existingId);Component result=creating?components.create(value):components.update(existingId,value);if(result==null){ApiResponses.error(response,404,"NOT_FOUND","Không tìm thấy nội dung.");return;}ApiResponses.json(response,creating?201:200,"{\"data\":"+result.toJson()+"}");}catch(IllegalArgumentException e){ApiResponses.error(response,422,"VALIDATION_ERROR","Dữ liệu linh kiện không hợp lệ.");}catch(SQLException e){ApiResponses.error(response,409,"CONFLICT","Không thể lưu linh kiện.");}}
    private Component input(String body,String id){String value=id==null?Json.stringField(body,"id").trim():id;if(!value.matches("[a-z0-9]+(?:-[a-z0-9]+)*"))throw new IllegalArgumentException();String name=Json.stringField(body,"name").trim(),category=Json.stringField(body,"category").trim(),image=Json.stringField(body,"image").trim(),description=Json.stringField(body,"description").trim(),specs=Json.objectField(body,"specs");if(name.isEmpty()||name.length()>150||category.isEmpty()||category.length()>100||image.isEmpty()||description.isEmpty())throw new IllegalArgumentException();return new Component(value,name,category,image,description,specs);}
}
