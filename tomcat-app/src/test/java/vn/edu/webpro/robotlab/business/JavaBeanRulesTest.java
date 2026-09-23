package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Chapter 6 slide 6 — một JavaBean phải:
 *   1. có constructor không tham số,
 *   2. có get/set cho mọi biến private theo quy ước đặt tên Java,
 *   3. implements Serializable.
 *
 * Test quét mọi lớp trong package business, nên thêm business object mới mà
 * quên một quy tắc thì build báo lỗi ngay.
 */
final class JavaBeanRulesTest {
    private static final Path SOURCES =
            Path.of("src", "main", "java", "vn", "edu", "webpro", "robotlab", "business");

    @Test
    void everyBusinessClassFollowsTheThreeJavaBeanRules() throws Exception {
        List<Class<?>> beans = businessClasses();
        assertFalse(beans.isEmpty(), "Không tìm thấy lớp nào trong package business.");

        List<String> problems = new ArrayList<>();
        for (Class<?> bean : beans) {
            String name = bean.getSimpleName();

            if (bean.isRecord()) {
                problems.add(name + " là record, không phải JavaBean");
            }
            try {
                if (!Modifier.isPublic(bean.getConstructor().getModifiers())) {
                    problems.add(name + " có constructor rỗng nhưng không public");
                }
            } catch (NoSuchMethodException e) {
                problems.add(name + " thiếu constructor không tham số");
            }
            if (!Serializable.class.isAssignableFrom(bean)) {
                problems.add(name + " chưa implements Serializable");
            }

            for (Field field : bean.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || !Modifier.isPrivate(modifiers)) continue;

                String suffix = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                boolean isBoolean = field.getType() == boolean.class;
                if (!hasMethod(bean, "get" + suffix) && !(isBoolean && hasMethod(bean, "is" + suffix))) {
                    problems.add(name + "." + field.getName() + " thiếu getter");
                }
                if (!hasMethod(bean, "set" + suffix, field.getType())) {
                    problems.add(name + "." + field.getName() + " thiếu setter");
                }
            }
        }

        if (!problems.isEmpty()) fail(String.join("\n", problems));
    }

    private boolean hasMethod(Class<?> type, String name, Class<?>... parameters) {
        try {
            type.getMethod(name, parameters);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private List<Class<?>> businessClasses() throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        try (Stream<Path> files = Files.list(SOURCES)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                String simpleName = file.getFileName().toString().replace(".java", "");
                classes.add(Class.forName("vn.edu.webpro.robotlab.business." + simpleName));
            }
        }
        return classes;
    }
}
