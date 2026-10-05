package vn.edu.webpro.robotlab.business;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.Date;
import org.junit.jupiter.api.Test;
class TaskRubricTest {
    private final TaskRubric rubric = new TaskRubric();
    private PracticeTask task() {
        PracticeTask task = new PracticeTask();
        task.setState("OPEN");
        task.setDueAt(new Date(2000));
        task.setMaxSubmissions(2);
        return task;
    }
    @Test void automaticPointsUseTheStoredRoundedValue() {
        assertEquals(new BigDecimal("80.0"), rubric.automaticPoints(7, 7));
        assertEquals(new BigDecimal("74.3"), rubric.automaticPoints(6, 7));
        assertEquals(new BigDecimal("40.0"), rubric.automaticPoints(0, 7));
        assertEquals(new BigDecimal("70.0"), rubric.automaticPoints(1198, 1600));
        assertEquals(new BigDecimal("69.9"), rubric.automaticPoints(1497, 2000));
        BigDecimal upper = rubric.roundAutomatic(new BigDecimal("69.95"));
        BigDecimal lower = rubric.roundAutomatic(new BigDecimal("69.94"));
        assertEquals(new BigDecimal("70.0"), upper);
        assertEquals(new BigDecimal("69.9"), lower);
        assertTrue(rubric.canPass(upper, 70, true));
        assertFalse(rubric.canPass(lower, 70, true));
        assertFalse(rubric.canPass(upper, 70, false));
    }
    @Test void explanationLevelsAreFixed() {
        for (int level = 0; level <= 4; level++) {
            assertEquals(level * 5, rubric.explanationPoints(level));
            assertFalse(rubric.explanationLabel(level).isEmpty());
        }
        assertEquals(new BigDecimal("79.3"), rubric.totalPoints(new BigDecimal("74.3"), 1));
    }
    @Test void revisionChecksLimitDeadlineAndState() {
        PracticeTask task = task();
        assertTrue(rubric.canRequestRevision(task, 1, new Date(1000)));
        assertFalse(rubric.canRequestRevision(task, 2, new Date(1000)));
        assertFalse(rubric.canRequestRevision(task, 1, new Date(3000)));
        task.setLatePolicy("ACCEPT_LATE_FLAGGED");
        assertTrue(rubric.canRequestRevision(task, 1, new Date(3000)));
        task.setState("CLOSED");
        assertFalse(rubric.canRequestRevision(task, 1, new Date(1000)));
    }
    @Test void reviewEditStopsWhenTheNextRoundHasActivity() {
        TaskRound next = new TaskRound();
        assertTrue(rubric.canEditReview(null));
        assertTrue(rubric.canEditReview(next));
        next.setQuizAttemptId(3);
        assertFalse(rubric.canEditReview(next));
        next.setQuizAttemptId(0);
        next.setState("SUBMITTED");
        assertFalse(rubric.canEditReview(next));
    }
    @Test void assemblyEvidenceUsesFirstRoundAndPriorPolicy() {
        assertTrue(rubric.validAssemblyEvidence(true, true, true, new Date(1000), new Date(1000), false));
        assertFalse(rubric.validAssemblyEvidence(true, true, true, new Date(999), new Date(1000), false));
        assertTrue(rubric.validAssemblyEvidence(true, true, true, new Date(999), new Date(1000), true));
        assertFalse(rubric.validAssemblyEvidence(false, true, true, new Date(2000), new Date(1000), true));
        assertFalse(rubric.validAssemblyEvidence(true, false, true, new Date(2000), new Date(1000), true));
        assertFalse(rubric.validAssemblyEvidence(true, true, false, new Date(2000), new Date(1000), true));
    }
    @Test void quizSelectionRequiresRoundOneAndReusesPreviousOnlyAfterIt() {
        assertEquals(7, rubric.chooseQuizAttempt(1, 7, 0));
        assertEquals(8, rubric.chooseQuizAttempt(2, 8, 7));
        assertEquals(7, rubric.chooseQuizAttempt(2, 0, 7));
        assertEquals(0, rubric.chooseQuizAttempt(1, 0, 7));
    }
    @Test void recipientLabelsFollowTheLatestReview() {
        TaskRecipient recipient = new TaskRecipient();
        recipient.setTask(task());
        assertEquals("Chưa nộp", recipient.statusLabel(new Date(1000)));
        assertEquals("Chưa nộp — Quá hạn", recipient.statusLabel(new Date(3000)));
        TaskSubmission submission = new TaskSubmission();
        recipient.setLatestSubmission(submission);
        assertEquals("Chờ chấm", recipient.statusLabel(new Date(1000)));
        submission.setState("REVIEWED");
        TaskReview review = new TaskReview();
        submission.getReviews().add(review);
        for (String conclusion : new String[]{"NEEDS_REVISION", "PASSED", "NOT_PASSED"}) {
            review.setConclusion(conclusion);
            assertEquals(review.getConclusionLabel(), recipient.statusLabel(new Date(1000)));
        }
    }
    @Test void vietnamTimeAndNumberFormattingAreInBeans() {
        TaskSubmission submission = new TaskSubmission();
        submission.setAutomaticPoints(new BigDecimal("74.3"));
        assertEquals("74,3", submission.getAutomaticDisplay());
        assertEquals("01/01/2026 07:00", TaskRubric.formatDate(new Date(1767225600000L)));
        assertEquals("25/09/2026 00:30", TaskRubric.formatDate(new Date(1790271000000L)));
        assertEquals("Chưa có dữ liệu", TaskRubric.formatDate(null));
    }
    @Test void draftValidationAndPublicationRejectMissingEvidence() {
        PracticeTask draft = new PracticeTask();
        draft.setTitle("Nhiệm vụ robot"); draft.setDescription("Thực hành mẫu A");
        draft.setDueAt(new Date(2000));
        draft.validate(true, true);
        for (int count : new int[]{0, 1}) {
            boolean rejected = false;
            try { draft.requirePublish(count, new Date(3000)); }
            catch (IllegalArgumentException e) { rejected = true; }
            assertTrue(rejected);
        }
        draft.requirePublish(1, new Date(1000));
        boolean rejected = false;
        try { draft.validate(true, false); } catch (IllegalArgumentException e) { rejected = true; }
        assertTrue(rejected);
    }
    @Test void openTasksOnlyExtendToALaterFutureDate() {
        PracticeTask task = task();
        task.requireExtension(new Date(4000), new Date(3000));
        for (long time : new long[]{1000, 2000, 3000}) {
            boolean rejected = false;
            try { task.requireExtension(new Date(time), new Date(3000)); }
            catch (IllegalArgumentException e) { rejected = true; }
            assertTrue(rejected);
        }
        task.setState("CLOSED");
        boolean rejected = false;
        try { task.requireOpen(); } catch (IllegalArgumentException e) { rejected = true; }
        assertTrue(rejected);
    }
    @Test void submittedOrCancelledRoundsCannotReceiveAnotherSubmission() {
        TaskRecipient recipient = new TaskRecipient(); recipient.setTask(task());
        TaskRound round = new TaskRound(); round.setId(10); recipient.setCurrentRound(round);
        assertTrue(recipient.ownsRound(10)); assertFalse(recipient.ownsRound(11));
        assertTrue(recipient.canSubmit(new Date(1000)));
        for (String state : new String[]{"SUBMITTED", "CANCELLED"}) {
            round.setState(state); assertFalse(recipient.canSubmit(new Date(1000)));
        }
        round.setState("ACTIVE"); recipient.setSubmissionCount(2);
        assertFalse(recipient.canSubmit(new Date(1000)));
        recipient.setSubmissionCount(1); recipient.setLatestSubmission(new TaskSubmission());
        assertFalse(recipient.canSubmit(new Date(1000)));
    }
    @Test void reviewCannotPassBelowThresholdAndNonPassNeedsSpecificFeedback() {
        PracticeTask task = task();
        TaskSubmission submission = new TaskSubmission(); submission.setSessionId(1);
        submission.setAutomaticPoints(new BigDecimal("40.0"));
        TaskReview review = new TaskReview(); review.setConclusion("PASSED"); review.setExplanationLevel(4);
        boolean rejected = false;
        try { review.validate(task, submission, 1, null, new Date(1000), false); }
        catch (IllegalArgumentException e) { rejected = true; }
        assertTrue(rejected);
        submission.setAutomaticPoints(new BigDecimal("80.0"));
        review.validate(task, submission, 1, null, new Date(1000), false);
        assertEquals(new BigDecimal("100.0"), review.getTotalPoints());
        review.setConclusion("NOT_PASSED");
        rejected = false;
        try { review.validate(task, submission, 1, null, new Date(1000), false); }
        catch (IllegalArgumentException e) { rejected = true; }
        assertTrue(rejected);
        review.setImprovements("Thiếu căn cứ"); review.setRetryGuidance("Thử lại và ghi kết quả");
        review.validate(task, submission, 1, null, new Date(1000), false);
    }
    @Test void reviewCorrectionRequiresReasonAndAnUnusedNextRound() {
        PracticeTask task = task(); TaskSubmission submission = new TaskSubmission();
        submission.setSessionId(1); submission.setAutomaticPoints(new BigDecimal("80.0"));
        TaskReview review = new TaskReview(); review.setConclusion("PASSED");
        TaskRound next = new TaskRound();
        boolean rejected = false;
        try { review.validate(task, submission, 1, next, new Date(1000), true); }
        catch (IllegalArgumentException e) { rejected = true; }
        assertTrue(rejected);
        review.setChangeReason("Đối chiếu lại căn cứ");
        review.validate(task, submission, 1, next, new Date(1000), true);
        next.setQuizAttemptId(1);
        rejected = false;
        try { review.validate(task, submission, 1, next, new Date(1000), true); }
        catch (IllegalArgumentException e) { rejected = true; }
        assertTrue(rejected);
    }
    @Test void allThreeExplanationFieldsMustMeetTheLimits() {
        TaskSubmission submission = new TaskSubmission();
        submission.setProblem("Một vấn đề đủ dài để mô tả rõ.");
        submission.setReasoning("Một căn cứ đủ dài để giải thích.");
        submission.setImprovement("Một điều cải thiện đủ dài để mô tả.");
        submission.validateExplanation();
        submission.setReasoning("Ngắn");
        boolean rejected = false;
        try { submission.validateExplanation(); } catch (IllegalArgumentException e) { rejected = true; }
        assertTrue(rejected);
    }
}
