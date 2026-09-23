package vn.edu.webpro.robotlab.view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vn.edu.webpro.robotlab.business.Component;
import vn.edu.webpro.robotlab.business.Robot;
import vn.edu.webpro.robotlab.business.User;

/**
 * Hợp đồng giữa JSP và JavaBean (Chapter 6: "Use EL to display properties of JavaBeans").
 *
 * EL ${user.fullName} gọi getFullName(). Nếu JSP dùng một thuộc tính mà bean
 * không có getter, trang trả HTTP 500 lúc chạy; mvn package không phát hiện được
 * vì JSP chỉ biên dịch khi có request. Test này quét thẳng file JSP để bắt sớm.
 */
final class JspExpressionContractTest {
    private static final Path VIEWS = Path.of("src", "main", "webapp", "WEB-INF", "views");

    /** Tên biến Servlet đặt bằng setAttribute (hoặc var của c:forEach) → lớp JavaBean. */
    private static final Map<String, Class<?>> BEANS = Map.of(
            "user", User.class,
            "robot", Robot.class,
            "component", Component.class
    );

    private static final Pattern EXPRESSION = Pattern.compile("\\$\\{\\s*([a-zA-Z]\\w*)\\.(\\w+)");

    @Test
    void everyBeanPropertyUsedInJspHasAGetter() throws IOException {
        List<String> problems = new ArrayList<>();
        Set<String> checked = new LinkedHashSet<>();

        for (Path view : jspFiles()) {
            String source = Files.readString(view, StandardCharsets.UTF_8);
            Matcher matcher = EXPRESSION.matcher(source);

            while (matcher.find()) {
                Class<?> bean = BEANS.get(matcher.group(1));
                if (bean == null) continue;

                String property = matcher.group(2);
                checked.add(bean.getSimpleName() + "." + property);
                if (!hasGetter(bean, property)) {
                    problems.add(view.getFileName() + ": ${" + matcher.group(1) + "." + property
                            + "} cần " + bean.getSimpleName() + ".get"
                            + Character.toUpperCase(property.charAt(0)) + property.substring(1) + "()");
                }
            }
        }

        assertFalse(checked.isEmpty(), "Không tìm thấy biểu thức EL nào để kiểm tra.");
        if (!problems.isEmpty()) fail(String.join("\n", problems));
    }

    /** JSP phải nằm dưới WEB-INF để không ai mở thẳng mà bỏ qua servlet. */
    @Test
    void everyJspStaysUnderWebInf() throws IOException {
        Path webapp = Path.of("src", "main", "webapp");
        try (Stream<Path> files = Files.walk(webapp)) {
            List<Path> exposed = files
                    .filter(path -> path.toString().endsWith(".jsp"))
                    .filter(path -> !webapp.relativize(path).startsWith("WEB-INF"))
                    .toList();
            assertTrue(exposed.isEmpty(), "JSP nằm ngoài WEB-INF: " + exposed);
        }
    }

    private boolean hasGetter(Class<?> bean, String property) {
        String suffix = Character.toUpperCase(property.charAt(0)) + property.substring(1);
        for (String prefix : new String[] {"get", "is"}) {
            try {
                bean.getMethod(prefix + suffix);
                return true;
            } catch (NoSuchMethodException ignored) {
                // Thử tiền tố còn lại.
            }
        }
        return false;
    }

    private List<Path> jspFiles() throws IOException {
        try (Stream<Path> files = Files.walk(VIEWS)) {
            return files.filter(path -> path.toString().endsWith(".jsp")).toList();
        }
    }
}
