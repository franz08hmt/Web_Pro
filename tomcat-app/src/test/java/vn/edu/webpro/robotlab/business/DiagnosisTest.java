package vn.edu.webpro.robotlab.business;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Kiểm chứng chẩn đoán và mẫu B bằng dữ kiện phía server. */
class DiagnosisTest {
    private DiagnosisAttempt attempt(int done, int total, boolean cause, boolean action) {
        DiagnosisAttempt value = new DiagnosisAttempt();
        value.setRequiredDone(done);
        value.setRequiredTotal(total);
        value.setCauseCorrect(cause);
        value.setActionCorrect(action);
        return value;
    }

    @Test
    void diagnosisUsesFourThreeThreeAndDistinctRequiredChecks() {
        assertEquals("0,0", attempt(0, 1, false, false).getScoreDisplay());
        for (int total = 1; total <= 3; total++) {
            assertEquals(0, attempt(total, total, true, true).getScoreExact().compareTo(BigDecimal.TEN));
        }
        assertEquals("8,0", attempt(1, 2, true, true).getScoreDisplay());
        assertEquals("7,3", attempt(1, 3, true, true).getScoreDisplay());
        try {
            attempt(0, 0, true, true).getScoreExact();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // Expected validation rejection.
        }
    }

    @Test
    void rubricBCombinesPreciseContributionsAndRoundsOnlyFinalAutomaticTotal() {
        TaskRubric rubric = new TaskRubric();
        rubric.setTemplate("B");
        assertEquals(new BigDecimal("71.4"), rubric.automaticPoints(6, 7, new BigDecimal("8")));
        assertEquals(new BigDecimal("80.0"), rubric.automaticPoints(7, 7, BigDecimal.TEN));
        assertEquals(new BigDecimal("30.0"), rubric.automaticPoints(0, 7, BigDecimal.ZERO));
        assertEquals(new BigDecimal("76.7"), rubric.automaticPoints(7, 7, attempt(2, 3, true, true).getScoreExact()));
        assertEquals(new BigDecimal("70.0"), rubric.roundAutomatic(new BigDecimal("69.95")));
        assertEquals(new BigDecimal("69.9"), rubric.roundAutomatic(new BigDecimal("69.94")));
        assertTrue(rubric.canPass(rubric.roundAutomatic(new BigDecimal("69.95")), 70, true));
        assertFalse(rubric.canPass(rubric.roundAutomatic(new BigDecimal("69.94")), 70, true));
        assertEquals(new BigDecimal("74.3"), new TaskRubric().automaticPoints(6, 7));
    }

    @Test
    void observationRequiresRecordedChoiceAndTaskEvidenceRejectsPractice() {
        DiagnosisCheck check = new DiagnosisCheck();
        check.setObservationText("Quan sát chỉ hiện khi đã ghi nhận lựa chọn.");
        assertEquals("", check.getObservationDisplay());
        check.setChosenAt(new java.util.Date());
        assertEquals(check.getObservationText(), check.getObservationDisplay());
        DiagnosisAttempt value = attempt(1, 1, true, true);
        value.setUserId(13);
        value.setScenarioId(6101);
        value.setState("SUBMITTED");
        assertFalse(value.validTaskEvidence(13, 6101));
        value.setMode("TASK");
        assertTrue(value.validTaskEvidence(13, 6101));
        assertFalse(value.validTaskEvidence(14, 6101));
        assertFalse(value.validTaskEvidence(13, 6102));
        value.setState("IN_PROGRESS");
        assertFalse(value.validTaskEvidence(13, 6101));
    }

    @Test
    void diagnosisRoundSelectionNeverDiscardsAnUnfinishedOwnAttempt() {
        TaskRubric rubric = new TaskRubric();
        assertEquals(0, rubric.chooseDiagnosisAttempt(1, 0, 99));
        assertEquals(99, rubric.chooseDiagnosisAttempt(2, 0, 99));
        assertEquals(100, rubric.chooseDiagnosisAttempt(2, 100, 99));
        TaskRound round = new TaskRound();
        assertTrue(rubric.canEditReview(round));
        round.setDiagnosisAttemptId(100);
        assertFalse(rubric.canEditReview(round));
    }

    private List<String> sqlValues(String line) {
        List<String> list = new ArrayList<>();
        String values = line.substring(line.indexOf("VALUES (") + 8);
        Matcher matcher = Pattern.compile("'((?:''|[^'])*)'|([0-9]+)|(@seed_admin|UTC_TIMESTAMP\\(\\))")
                .matcher(values);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                list.add(matcher.group(1).replace("''", "'"));
            } else if (matcher.group(2) != null) {
                list.add(matcher.group(2));
            } else {
                list.add(matcher.group(3));
            }
        }
        return list;
    }

    @Test
    void actualFiveSeedScenariosMeetEveryPublicationRule() throws Exception {
        HashMap<Long, DiagnosisScenario> scenarios = new HashMap<>();
        for (String line : Files.readAllLines(Path.of("../database/seed-diagnosis-phase6.sql"))) {
            if (!line.startsWith("INSERT IGNORE")) {
                continue;
            }
            List<String> values = sqlValues(line);
            if (line.startsWith("INSERT IGNORE INTO diagnosis_scenarios")) {
                DiagnosisScenario scenario = new DiagnosisScenario();
                scenario.setId(Long.parseLong(values.get(0)));
                scenario.setRobotId(values.get(1));
                scenario.setTitle(values.get(2));
                scenario.setContextText(values.get(3));
                scenario.setSymptomText(values.get(4));
                scenario.setExplanationText(values.get(5));
                scenario.setState(values.get(6));
                scenarios.put(scenario.getId(), scenario);
            } else if (line.startsWith("INSERT IGNORE INTO diagnosis_checks")) {
                DiagnosisCheck check = new DiagnosisCheck();
                check.setId(Long.parseLong(values.get(0)));
                check.setLabel(values.get(2));
                check.setObservationText(values.get(3));
                check.setRequired("1".equals(values.get(4)));
                check.setDisplayOrder(Integer.parseInt(values.get(5)));
                scenarios.get(Long.parseLong(values.get(1))).getChecks().add(check);
            } else {
                DiagnosisOption option = new DiagnosisOption();
                option.setId(Long.parseLong(values.get(0)));
                option.setKind(values.get(2));
                option.setLabel(values.get(3));
                option.setFeedbackText(values.get(4));
                option.setCorrect("1".equals(values.get(5)));
                option.setDisplayOrder(Integer.parseInt(values.get(6)));
                scenarios.get(Long.parseLong(values.get(1))).getOptions().add(option);
            }
        }
        assertEquals(5, scenarios.size());
        for (Long id : scenarios.keySet()) {
            DiagnosisScenario scenario = scenarios.get(id);
            assertTrue(scenario.isPublished());
            scenario.validatePublication(true, true);
            scenario.setState("DRAFT");
            scenario.requireDraft();
            scenario.setState("PUBLISHED");
            try {
                scenario.requireDraft();
                fail("Expected IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
                // Expected validation rejection.
            }
            try {
                scenario.validatePublication(false, true);
                fail("Expected IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
                // Expected validation rejection.
            }
            try {
                scenario.validatePublication(true, false);
                fail("Expected IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
                // Expected validation rejection.
            }
            scenario.getCauses().get(0).setCorrect(false);
            try {
                scenario.validatePublication(true, true);
                fail("Expected IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
                // Expected validation rejection.
            }
            scenario.getCauses().get(0).setCorrect(true);
            scenario.getChecks().clear();
            try {
                scenario.validatePublication(true, true);
                fail("Expected IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
                // Expected validation rejection.
            }
        }
    }

    @Test
    void draftMayBeIncompleteButRejectsOversizedFieldsBeforeSql() {
        DiagnosisScenario scenario = new DiagnosisScenario();
        scenario.setTitle("Nháp hợp lệ");
        scenario.validateDraft();
        DiagnosisCheck check = new DiagnosisCheck();
        check.setLabel("x".repeat(201));
        scenario.getChecks().add(check);
        try {
            scenario.validateDraft();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // Expected validation rejection.
        }
    }

    @Test
    void concludeUsesChosenLogNotUnchosenObservationsAndRejectsWrongKinds() {
        DiagnosisScenario scenario = new DiagnosisScenario();
        for (int i = 1; i <= 3; i++) {
            DiagnosisCheck check = new DiagnosisCheck();
            check.setId(i);
            check.setRequired(i < 3);
            if (i != 2) {
                check.setChosenAt(new java.util.Date());
            }
            scenario.getChecks().add(check);
        }
        DiagnosisOption cause = new DiagnosisOption();
        cause.setId(10);
        cause.setKind("CAUSE");
        cause.setCorrect(true);
        DiagnosisOption action = new DiagnosisOption();
        action.setId(11);
        action.setKind("ACTION");
        action.setCorrect(false);
        scenario.getOptions().add(cause);
        scenario.getOptions().add(action);
        DiagnosisAttempt value = new DiagnosisAttempt();
        value.setScenario(scenario);
        try {
            value.conclude(11, 10);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // Expected validation rejection.
        }
        try {
            value.conclude(999, 11);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // Expected validation rejection.
        }
        value.conclude(10, 11);
        assertEquals(1, value.getRequiredDone());
        assertEquals(2, value.getRequiredTotal());
        assertEquals("5,0", value.getScoreDisplay());
        assertEquals(1, value.getMissingChecks().size());
        try {
            value.conclude(10, 11);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // Expected validation rejection.
        }
    }
}
