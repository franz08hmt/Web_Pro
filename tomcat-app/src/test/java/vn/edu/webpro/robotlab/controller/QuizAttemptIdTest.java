package vn.edu.webpro.robotlab.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import vn.edu.webpro.robotlab.business.User;

/** ID sai phải dừng trước JDBC và dùng cùng nhánh JSON404. */
class QuizAttemptIdTest {
    private void rejects(String id) throws Exception {
        Method method = QuizServlet.class.getDeclaredMethod("sendAttemptDetail",
                HttpServletResponse.class, User.class, String.class);
        method.setAccessible(true);
        ResponseCapture captured = new ResponseCapture();
        method.invoke(new QuizServlet(), captured.response(), new User(), id);
        assertEquals(404, captured.status());
        assertTrue(captured.body().contains("\"code\":\"NOT_FOUND\""));
    }

    @Test
    void rejectsMalformedIdsWithoutJdbc() throws Exception {
        String[] values = {"", "0", "-1", "word", "1/2", "10000000000000000000"};
        for (String value : values) {
            rejects(value);
        }
    }

    @Test
    void rejectsLongMaxPlusOneWithoutJdbc() throws Exception {
        rejects("9223372036854775808");
    }

    @Test
    void rejectsNineteenNinesWithoutJdbc() throws Exception {
        rejects("9999999999999999999");
    }
}
