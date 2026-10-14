package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AssemblyReviewTest {
    @Test
    void technicalRejectionsAreTraceableButNotLearnerMistakes() {
        AssemblyReview review = new AssemblyReview();
        String[] codes = {"LAYOUT_CONFLICT", "NOTHING_TO_UNDO", "SESSION_LOCKED", "GROUP_MODE", "VALIDATION_ERROR",
            "ORIENTATION_NOT_ALLOWED", "ORIENTATION_NOT_ALLOWED"};
        for (String code : codes) {
            AssemblyEvent event = new AssemblyEvent();
            event.setOutcome("REJECTED");
            event.setReasonCode(code);
            review.getEvents().add(event);
        }
        review.summarizeEvents();
        assertEquals(7, review.getEvents().size());
        assertEquals(1, review.getRejectionSummary().size());
        assertEquals("ORIENTATION_NOT_ALLOWED", review.getRejectionSummary().get(0).getReasonCode());
        assertEquals(2, review.getRejectionSummary().get(0).getRepeatCount());
        review.getEvents().remove(6);
        review.getEvents().remove(5);
        review.summarizeEvents();
        assertTrue(review.getRejectionSummary().isEmpty());
        String[] learningCodes = {"WRONG_COMPONENT", "SLOT_OCCUPIED", "ORIENTATION_NOT_ALLOWED",
            "PARENT_MISSING", "ALREADY_PLACED", "HAS_DEPENDENTS"};
        for (String code : learningCodes) {
            assertTrue(review.isLearningError(code));
        }
        assertFalse(review.isLearningError(null));
        assertFalse(review.isLearningError("UNKNOWN_CODE"));
    }

    @Test
    void summaryKeepsOnlyActiveAcceptedStepsAndCountsRejections() {
        AssemblyReview r = new AssemblyReview();
        AssemblyEvent a = new AssemblyEvent();
        a.setAction("ATTACH");
        a.setOutcome("ACCEPTED");
        a.setUndone(true);
        r.getEvents().add(a);
        AssemblyEvent b = new AssemblyEvent();
        b.setOutcome("REJECTED");
        b.setReasonCode("WRONG_COMPONENT");
        r.getEvents().add(b);
        r.getEvents().add(b);
        r.summarizeEvents();
        assertTrue(r.getActiveEvents().isEmpty());
        assertEquals(1, r.getRejectionSummary().size());
        assertEquals(2, r.getRejectionSummary().get(0).getRepeatCount());
        assertEquals("Sai loại linh kiện", r.getRejectionSummary().get(0).getReasonLabel());
    }

    @Test
    void quizZeroIsRealAndImagePathNeverCreatesAnExternalUrl() {
        AssemblyReview r = new AssemblyReview();
        r.setBestQuizTotal(7);
        r.setHasQuiz(true);
        assertEquals("0/7", r.getBestQuizLabel());
        r.getRobot().setImage("images/robots/robot-tranh-vat-can.png");
        assertEquals("images/robots/robot-tranh-vat-can.png", r.getImagePath());
        r.getRobot().setImage("https://external.invalid/x\"onerror=x");
        assertEquals("", r.getImagePath());
    }
}
