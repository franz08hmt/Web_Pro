package vn.edu.webpro.robotlab.business;

import java.util.Date;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Kiểm luật hỗ trợ độc lập với luật chấm nối dây. */
class WiringSupportTest {
    @Test
    void conversationChangesStateWithoutClaimingWiringPassed() {
        WiringSupportRequest r = new WiringSupportRequest();
        assertTrue(r.isOpen());
        r.applyMessage(true, 1);
        assertTrue(r.isAnswered());
        r.applyMessage(false, 2);
        assertTrue(r.isOpen());
        r.close(3);
        assertTrue(r.isClosed());
        assertFalse(r.getStateLabel().contains("đúng toàn bộ"));
        assertThrows(IllegalArgumentException.class, () -> r.applyMessage(false, 4));
    }

    @Test
    void staleFormsCannotReplyOrClose() {
        WiringSupportRequest r = new WiringSupportRequest();
        r.applyMessage(false, 1);
        assertThrows(IllegalArgumentException.class, () -> r.applyMessage(true, 1));
        assertThrows(IllegalArgumentException.class, () -> r.close(1));
        assertEquals(2, r.getVersion());
    }

    @Test
    void questionAndReplyTrimAndValidateTheirOwnLengths() {
        WiringSupportMessage m = new WiringSupportMessage();
        m.setContent("  câu hỏi quá ngắn  ");
        assertThrows(IllegalArgumentException.class, () -> m.validate(false));
        m.setContent("  Tôi cần kiểm tra lại dây nối này <b>giúp tôi</b>  ");
        m.validate(false);
        assertFalse(m.getContent().startsWith(" "));
        m.setContent("Xin kiểm tra lại đầu nối theo bản chụp.");
        m.validate(true);
        m.setContent("x".repeat(2001));
        assertThrows(IllegalArgumentException.class, () -> m.validate(true));
    }

    @Test
    void unicodeLengthsMatchMysqlCharacterCounts() {
        WiringSupportMessage m = new WiringSupportMessage();
        m.setContent("🤖".repeat(19));
        assertThrows(IllegalArgumentException.class, () -> m.validate(false));
        m.setContent("🤖".repeat(20));
        m.validate(false);
        m.setContent("🤖".repeat(2000));
        m.validate(false);
        m.setContent("🤖".repeat(2001));
        assertThrows(IllegalArgumentException.class, () -> m.validate(false));
    }

    @Test
    void savedSnapshotRemainsAfterSourceDraftChanges() {
        WiringAttempt a = attempt();
        WiringSupportMessage m = new WiringSupportMessage();
        m.capture(a, new Date(1000));
        String before = m.getSnapshotText();
        a.setVersion(9);
        a.getExercise().setTitle("Tên đã thay đổi ở bản nguồn giả lập");
        a.getConnections().clear();
        assertEquals(before, m.getSnapshotText());
        assertEquals(1, m.getSnapshotVersion());
        assertNull(m.getSnapshotScore());
        WiringSupportMessage next = new WiringSupportMessage();
        next.capture(a, new Date(2000));
        assertNotEquals(before, next.getSnapshotText());
        assertEquals(9, next.getSnapshotVersion());
    }

    @Test
    void submittedSnapshotKeepsStoredScore() {
        WiringAttempt a = attempt();
        a.setState("SUBMITTED");
        WiringGrade g = new WiringGrade();
        g.setScore(new java.math.BigDecimal("92.9"));
        a.setGrade(g);
        WiringSupportMessage m = new WiringSupportMessage();
        m.capture(a, new Date());
        assertEquals(g.getScore(), m.getSnapshotScore());
        assertTrue(m.getSnapshotText().contains("92,9"));
        assertEquals("SUBMITTED", a.getState());
    }

    private WiringAttempt attempt() {
        WiringAttempt a = new WiringAttempt();
        WiringExercise e = new WiringExercise();
        e.setTitle("Bài nối dây thử");
        e.setRobotName("Robot thử");
        a.setExercise(e);
        return a;
    }

    @Test
    void snapshotSupportsMaximumTerminalsAndWiresWithUtf8Labels() {
        WiringAttempt a = attempt();
        for (int i = 1; i <= 40; i++) {
            WiringTerminal t = new WiringTerminal();
            t.setId(i);
            t.setCode("pin-" + i);
            t.setDeviceLabel("ấ".repeat(100));
            t.setPinLabel("ệ".repeat(50));
            a.getExercise().getTerminals().add(t);
        }
        for (int i = 2; i <= 40; i++) {
            WiringConnection c = new WiringConnection();
            c.setTerminalA(1);
            c.setTerminalB(i);
            a.getConnections().add(c);
        }
        for (int i = 3; i <= 40; i++) {
            WiringConnection c = new WiringConnection();
            c.setTerminalA(2);
            c.setTerminalB(i);
            a.getConnections().add(c);
        }
        for (int i = 4; i <= 6; i++) {
            WiringConnection c = new WiringConnection();
            c.setTerminalA(3);
            c.setTerminalB(i);
            a.getConnections().add(c);
        }
        a.setConnections(a.getExercise().normalize(a.getConnections()));
        WiringSupportMessage m = new WiringSupportMessage();
        m.capture(a, new Date());
        assertEquals(80, a.getConnections().size());
        assertTrue(m.getSnapshotText().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 65535);
        assertTrue(m.getSnapshotPairs().length() < 4000);
    }
}
