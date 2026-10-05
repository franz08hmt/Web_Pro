package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

/** Bảo vệ luật cặp trực tiếp và điểm của phòng nối dây. */
class WiringTest {
    private WiringExercise exercise() {
        WiringExercise e = new WiringExercise();
        e.setCode("test-board");
        e.setTitle("Bài kiểm tra nối dây");
        e.setRobotId("line-follower");
        e.setObjective("Nối đúng các cặp đã khai báo.");
        e.setScopeText("Chỉ mô phỏng các cặp nối trực tiếp.");
        for (int i = 1; i <= 5; i++) {
            WiringTerminal t = new WiringTerminal();
            t.setId(i);
            t.setCode("pin-" + i);
            t.setDeviceCode("board");
            t.setDeviceLabel("Mạch mẫu");
            t.setPinLabel("Chân " + i);
            t.setX(200);
            t.setY(i * 100);
            e.getTerminals().add(t);
        }
        rule(e, 1, 2, "REQUIRED");
        rule(e, 1, 3, "REQUIRED");
        rule(e, 4, 5, "FORBIDDEN");
        return e;
    }

    private void rule(WiringExercise e, long a, long b, String kind) {
        WiringRule r = new WiringRule();
        r.setTerminalA(a);
        r.setTerminalB(b);
        r.setKind(kind);
        r.setExplanation("Giải thích cặp nối trong phạm vi bài.");
        e.getRules().add(r);
    }

    private ArrayList<WiringConnection> wires(long... ids) {
        ArrayList<WiringConnection> values = new ArrayList<>();
        for (int i = 0; i < ids.length; i += 2) {
            WiringConnection w = new WiringConnection();
            w.setTerminalA(ids[i]);
            w.setTerminalB(ids[i + 1]);
            values.add(w);
        }
        return values;
    }

    @Test void reverseAndDuplicateAreOnePair() {
        assertEquals(1, exercise().normalize(wires(1, 2, 2, 1)).size());
        assertEquals("1:2", exercise().normalize(wires(2, 1)).get(0).getPairKey());
    }

    @Test void commonTerminalSupportsMultipleWires() {
        WiringGrade g = exercise().grade(wires(1, 2, 1, 3));
        assertEquals(new BigDecimal("100.0"), g.getScore());
        assertTrue(g.isEntirelyCorrect());
    }

    @Test void emptySubmissionHasOnlyMissingConnections() {
        WiringGrade g = exercise().grade(wires());
        assertEquals(new BigDecimal("0.0"), g.getScore());
        assertEquals(2, g.getMissingCount());
    }

    @Test void missingConnectionReducesScore() {
        assertEquals(new BigDecimal("50.0"), exercise().grade(wires(1, 2)).getScore());
    }

    @Test void correctPlusWrongCannotBeOneHundred() {
        WiringGrade g = exercise().grade(wires(1, 2, 1, 3, 2, 3));
        assertEquals(new BigDecimal("50.0"), g.getScore());
        assertFalse(g.isEntirelyCorrect());
    }

    @Test void forbiddenPairCountsOnceAndHasItsExplanation() {
        WiringGrade g = exercise().grade(wires(4, 5, 5, 4));
        assertEquals(1, g.getWrongCount());
        assertEquals(new BigDecimal("0.0"), g.getScore());
        assertTrue(g.getRows().get(0).isForbidden());
        assertFalse(g.getRows().get(0).getExplanation().isEmpty());
    }

    @Test void invalidEndpointSelfConnectionAndTooManyAbortValidation() {
        WiringExercise e = exercise();
        assertThrows(IllegalArgumentException.class, () -> e.grade(wires(1, 9)));
        assertThrows(IllegalArgumentException.class, () -> e.grade(wires(1, 1)));
        ArrayList<WiringConnection> many = wires();
        for (int i = 0; i < 81; i++) {
            many.add(wires(1, 2).get(0));
        }
        assertThrows(IllegalArgumentException.class, () -> e.grade(many));
    }

    @Test void halfUpAndZeroDenominator() {
        WiringGrade tie = new WiringGrade();
        tie.calculate(16, 1, 0);
        assertEquals(new BigDecimal("6.3"), tie.getScore());
        WiringExercise e = exercise();
        rule(e, 1, 4, "REQUIRED");
        assertEquals(new BigDecimal("33.3"), e.grade(wires(1, 2)).getScore());
        rule(e, 1, 5, "REQUIRED");
        rule(e, 2, 3, "REQUIRED");
        rule(e, 2, 4, "REQUIRED");
        rule(e, 2, 5, "REQUIRED");
        rule(e, 3, 4, "REQUIRED");
        assertEquals(new BigDecimal("12.5"), e.grade(wires(1, 2)).getScore());
        e.getRules().clear();
        assertThrows(IllegalArgumentException.class, () -> e.grade(wires()));
    }

    @Test void publicationRejectsDuplicateConflictingAndForeignRules() {
        WiringExercise e = exercise();
        e.validatePublication(true);
        rule(e, 2, 1, "FORBIDDEN");
        assertThrows(IllegalArgumentException.class, () -> e.validatePublication(true));
        WiringExercise foreign = exercise();
        rule(foreign, 1, 9, "REQUIRED");
        assertThrows(IllegalArgumentException.class, () -> foreign.validatePublication(true));
    }

    @Test void distinctSensorsAndCoordinatesAreValidated() {
        WiringExercise e = exercise();
        e.getTerminals().get(0).setCode("line-left.OUT");
        e.getTerminals().get(1).setCode("line-right.OUT");
        e.validatePublication(true);
        e.getTerminals().get(0).setX(1001);
        assertThrows(IllegalArgumentException.class, () -> e.validatePublication(true));
    }

    @Test void codeUniquenessMatchesDatabaseCollation() {
        WiringExercise e = exercise();
        e.getTerminals().get(0).setCode("pin-A");
        e.getTerminals().get(1).setCode("pin-a");
        assertThrows(IllegalArgumentException.class, () -> e.validateDraft(true));
    }

    @Test void publicationArchiveAndClonePreserveOriginal() {
        WiringExercise e = exercise();
        e.publish(true);
        assertThrows(IllegalArgumentException.class, e::requireDraft);
        WiringExercise copy = e.copyDraft();
        assertTrue(copy.isDraft());
        assertNotEquals(e.getCode(), copy.getCode());
        e.archive();
        assertFalse(e.isPublished());
        assertTrue(copy.isDraft());
    }

    @Test void draftHasNoScoreAndSubmittedCannotMutate() {
        WiringAttempt a = new WiringAttempt();
        assertNull(a.getGrade());
        a.requireEditable(1);
        assertThrows(IllegalArgumentException.class, () -> a.requireEditable(0));
        a.setState("SUBMITTED");
        assertThrows(IllegalArgumentException.class, () -> a.requireEditable(1));
    }
}
