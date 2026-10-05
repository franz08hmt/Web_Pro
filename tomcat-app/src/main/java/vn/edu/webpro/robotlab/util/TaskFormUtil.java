package vn.edu.webpro.robotlab.util;
import java.util.Date;
import java.util.TimeZone;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.servlet.http.HttpServletRequest;
import vn.edu.webpro.robotlab.business.PracticeTask;
import vn.edu.webpro.robotlab.business.TaskSubmission;
public final class TaskFormUtil {
    private TaskFormUtil() { }
    public static String text(HttpServletRequest request,String name) {
        String value=request.getParameter(name); if(value==null) return ""; return value.trim();
    }
    public static long number(HttpServletRequest request,String name) {
        try { long value=Long.parseLong(text(request,name)); if(value<0) throw new NumberFormatException(); return value; }
        catch(NumberFormatException e) { throw new IllegalArgumentException("Mã hoặc số không hợp lệ: "+name); }
    }
    public static Date due(HttpServletRequest request) {
        String value=text(request,"dueAt");
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        format.setLenient(false); format.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        try {
            Date date=format.parse(value);
            if(!value.equals(format.format(date))) throw new ParseException("invalid",0);
            return date;
        } catch(ParseException e) { throw new IllegalArgumentException("Hạn nộp không hợp lệ (giờ Việt Nam)."); }
    }
    public static int integer(HttpServletRequest request,String name) {
        long value=number(request,name);
        if(value>Integer.MAX_VALUE) throw new IllegalArgumentException("Số nằm ngoài phạm vi: "+name);
        return (int)value;
    }
    public static PracticeTask task(HttpServletRequest request) {
        PracticeTask task=new PracticeTask(); task.setId(number(request,"id"));
        task.setTitle(text(request,"title")); task.setDescription(text(request,"description"));
        task.setRobotId(text(request,"robotId")); task.setDueAt(due(request)); task.setLatePolicy(text(request,"latePolicy"));
        task.setMaxSubmissions(integer(request,"maxSubmissions")); task.setPassThreshold(integer(request,"passThreshold"));
        task.setAllowPriorEvidence("true".equals(text(request,"allowPriorEvidence")));
        if(!"A".equals(text(request,"rubricTemplate"))) throw new IllegalArgumentException("Chặng 1 chỉ dùng mẫu A.");
        return task;
    }
    public static TaskSubmission submission(HttpServletRequest request) {
        TaskSubmission input=new TaskSubmission(); input.setSessionId(number(request,"sessionId")); input.setRoundId(number(request,"roundId"));
        input.setProblem(text(request,"problem")); input.setReasoning(text(request,"reasoning")); input.setImprovement(text(request,"improvement"));
        return input;
    }
}
