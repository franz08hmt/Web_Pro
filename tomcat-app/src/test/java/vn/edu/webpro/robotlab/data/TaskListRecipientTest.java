package vn.edu.webpro.robotlab.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashMap;
import vn.edu.webpro.robotlab.business.TaskRecipient;
import org.junit.jupiter.api.Test;

/** Fixture ResultSet trong bộ nhớ; không kết nối hoặc ghi dữ liệu vào MySQL. */
class TaskListRecipientTest {
    private HashMap<String, Object> row(long userId) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("id", 10L);
        values.put("list_recipient_id", userId + 100L);
        values.put("list_user_id", userId);
        values.put("state", "OPEN");
        values.put("due_at", new Timestamp(4102444800000L));
        return values;
    }

    private ResultSet result(final HashMap<String, Object> values) {
        return (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {ResultSet.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        Object value = values.get((String) args[0]);
                        if ("getLong".equals(method.getName())) {
                            if (value == null) {
                                return 0L;
                            }
                            return ((Number) value).longValue();
                        }
                        if ("getInt".equals(method.getName())) {
                            if (value == null) {
                                return 0;
                            }
                            return ((Number) value).intValue();
                        }
                        if ("getBoolean".equals(method.getName())) {
                            return Boolean.TRUE.equals(value);
                        }
                        if ("getString".equals(method.getName()) || "getTimestamp".equals(method.getName())) {
                            return value;
                        }
                        throw new UnsupportedOperationException(method.getName());
                    }
                });
    }

    @Test
    void noSubmissionUsesExistingUnsubmittedRule() throws Exception {
        TaskRecipient recipient = PracticeTaskDB.mapListRecipient(result(row(13)));
        assertEquals("Chưa nộp", recipient.getStatusLabel());
        assertNull(recipient.getLatestSubmission());
        assertEquals(13, recipient.getUserId());
    }

    @Test
    void submittedWithoutReviewIsWaiting() throws Exception {
        HashMap<String, Object> values = row(13);
        values.put("list_submission_id", 100L);
        values.put("list_submission_state", "SUBMITTED");
        TaskRecipient recipient = PracticeTaskDB.mapListRecipient(result(values));
        assertEquals("Chờ chấm", recipient.getStatusLabel());
    }

    @Test
    void storedRevisionConclusionAndLateFlagStaySeparateFromTaskState() throws Exception {
        HashMap<String, Object> values = row(13);
        values.put("list_submission_id", 100L);
        values.put("list_submission_state", "REVIEWED");
        values.put("list_review_id", 200L);
        values.put("list_conclusion", "NEEDS_REVISION");
        values.put("list_is_late", true);
        TaskRecipient recipient = PracticeTaskDB.mapListRecipient(result(values));
        assertEquals("Cần bổ sung", recipient.getStatusLabel());
        assertEquals("Đang mở", recipient.getTask().getStateLabel());
        assertTrue(recipient.getLatestSubmission().isLate());
    }

    @Test
    void missingReviewDoesNotInventAnOutcome() throws Exception {
        HashMap<String, Object> values = row(13);
        values.put("list_submission_id", 100L);
        values.put("list_submission_state", "REVIEWED");
        TaskRecipient recipient = PracticeTaskDB.mapListRecipient(result(values));
        assertEquals("Chờ chấm", recipient.getStatusLabel());
    }

    @Test
    void sameTaskForTwoUsersKeepsDifferentSubmissionsAndLabels() throws Exception {
        HashMap<String, Object> first = row(13);
        first.put("list_submission_id", 100L);
        first.put("list_submission_state", "REVIEWED");
        first.put("list_review_id", 200L);
        first.put("list_conclusion", "NEEDS_REVISION");
        HashMap<String, Object> second = row(14);
        second.put("list_submission_id", 101L);
        second.put("list_submission_state", "SUBMITTED");
        TaskRecipient a = PracticeTaskDB.mapListRecipient(result(first));
        TaskRecipient b = PracticeTaskDB.mapListRecipient(result(second));
        assertEquals(a.getTaskId(), b.getTaskId());
        assertEquals("Cần bổ sung", a.getStatusLabel());
        assertEquals("Chờ chấm", b.getStatusLabel());
        assertEquals(13, a.getUserId());
        assertEquals(14, b.getUserId());
        assertNotSame(a.getLatestSubmission(), b.getLatestSubmission());
    }
}
