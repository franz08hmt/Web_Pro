package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AssemblyReviewTest {
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
