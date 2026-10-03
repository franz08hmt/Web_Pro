package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Dữ liệu và quy tắc nghiệp vụ của tài liệu hồ sơ học tập có thể in. */
public class LearningProfile implements Serializable {
    private static final String COMPLETED = "COMPLETED";
    private static final String PREPARING = "PREPARING";
    private static final String READY = "READY";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String ABANDONED = "ABANDONED";
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter VIET_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter VIET_DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String NO_DATA = "Chưa có dữ liệu";

    private String fullName;
    private String generatedAtUtc;
    private String generatedAtDisplay;
    private List<Robot> catalogRobots;
    private List<ProfileSessionStat> sessionStats;
    private List<ProfileQuizAttempt> quizAttempts;
    private List<String> completedComponentNames;
    private List<ProfileRobotEntry> robotEntries;
    private List<ProfileSkill> skillLines;
    private int completedRobotCount;
    private int robotCount;
    private int quizAttemptCount;
    private int quizRobotCount;
    private boolean quizDataAvailable;
    private int averageBestScorePercent;
    private String averageBestScoreLabel;
    private String completionSummary;
    private String overallStatusLabel;
    private String skillEmptyMessage;

    public LearningProfile() {
        fullName = "";
        generatedAtUtc = "";
        generatedAtDisplay = NO_DATA;
        catalogRobots = new ArrayList<>();
        sessionStats = new ArrayList<>();
        quizAttempts = new ArrayList<>();
        completedComponentNames = new ArrayList<>();
        robotEntries = new ArrayList<>();
        skillLines = new ArrayList<>();
        averageBestScoreLabel = NO_DATA;
        completionSummary = "Đã hoàn thành 0/0 mẫu robot";
        overallStatusLabel = NO_DATA;
        skillEmptyMessage = "Chưa có dữ liệu — hoàn thành ít nhất một mẫu robot để có căn cứ ghi nhận.";
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGeneratedAtUtc() {
        return generatedAtUtc;
    }

    public void setGeneratedAtUtc(String generatedAtUtc) {
        this.generatedAtUtc = generatedAtUtc;
    }

    public String getGeneratedAtDisplay() {
        return generatedAtDisplay;
    }

    public void setGeneratedAtDisplay(String generatedAtDisplay) {
        this.generatedAtDisplay = generatedAtDisplay;
    }

    public List<Robot> getCatalogRobots() {
        return catalogRobots;
    }

    public void setCatalogRobots(List<Robot> catalogRobots) {
        this.catalogRobots = catalogRobots == null ? new ArrayList<>() : catalogRobots;
    }

    public List<ProfileSessionStat> getSessionStats() {
        return sessionStats;
    }

    public void setSessionStats(List<ProfileSessionStat> sessionStats) {
        this.sessionStats = sessionStats == null ? new ArrayList<>() : sessionStats;
    }

    public List<ProfileQuizAttempt> getQuizAttempts() {
        return quizAttempts;
    }

    public void setQuizAttempts(List<ProfileQuizAttempt> quizAttempts) {
        this.quizAttempts = quizAttempts == null ? new ArrayList<>() : quizAttempts;
    }

    public List<String> getCompletedComponentNames() {
        return completedComponentNames;
    }

    public void setCompletedComponentNames(List<String> completedComponentNames) {
        this.completedComponentNames = completedComponentNames == null ? new ArrayList<>() : completedComponentNames;
    }

    public List<ProfileRobotEntry> getRobotEntries() {
        return robotEntries;
    }

    public void setRobotEntries(List<ProfileRobotEntry> robotEntries) {
        this.robotEntries = robotEntries == null ? new ArrayList<>() : robotEntries;
    }

    public List<ProfileSkill> getSkillLines() {
        return skillLines;
    }

    public void setSkillLines(List<ProfileSkill> skillLines) {
        this.skillLines = skillLines == null ? new ArrayList<>() : skillLines;
    }

    public int getCompletedRobotCount() {
        return completedRobotCount;
    }

    public void setCompletedRobotCount(int completedRobotCount) {
        this.completedRobotCount = completedRobotCount;
    }

    public int getRobotCount() {
        return robotCount;
    }

    public void setRobotCount(int robotCount) {
        this.robotCount = robotCount;
    }

    public int getQuizAttemptCount() {
        return quizAttemptCount;
    }

    public void setQuizAttemptCount(int quizAttemptCount) {
        this.quizAttemptCount = quizAttemptCount;
    }

    public int getQuizRobotCount() {
        return quizRobotCount;
    }

    public void setQuizRobotCount(int quizRobotCount) {
        this.quizRobotCount = quizRobotCount;
    }

    public boolean isQuizDataAvailable() {
        return quizDataAvailable;
    }

    public void setQuizDataAvailable(boolean quizDataAvailable) {
        this.quizDataAvailable = quizDataAvailable;
    }

    public int getAverageBestScorePercent() {
        return averageBestScorePercent;
    }

    public void setAverageBestScorePercent(int averageBestScorePercent) {
        this.averageBestScorePercent = averageBestScorePercent;
    }

    public String getAverageBestScoreLabel() {
        return averageBestScoreLabel;
    }

    public void setAverageBestScoreLabel(String averageBestScoreLabel) {
        this.averageBestScoreLabel = averageBestScoreLabel;
    }

    public String getCompletionSummary() {
        return completionSummary;
    }

    public void setCompletionSummary(String completionSummary) {
        this.completionSummary = completionSummary;
    }

    public String getOverallStatusLabel() {
        return overallStatusLabel;
    }

    public void setOverallStatusLabel(String overallStatusLabel) {
        this.overallStatusLabel = overallStatusLabel;
    }

    public String getSkillEmptyMessage() {
        return skillEmptyMessage;
    }

    public void setSkillEmptyMessage(String skillEmptyMessage) {
        this.skillEmptyMessage = skillEmptyMessage;
    }

    /** Gom dữ kiện từ DB vào các khối robot và áp dụng thống nhất các luật hồ sơ. */
    public void buildProfile() {
        generatedAtDisplay = formatUtcDateTime(generatedAtUtc);
        robotEntries = new ArrayList<>();

        Map<String, List<ProfileSessionStat>> sessionsByRobot = new HashMap<>();
        for (ProfileSessionStat stat : sessionStats) {
            sessionsByRobot.computeIfAbsent(stat.getRobotId(), key -> new ArrayList<>()).add(stat);
        }
        Map<String, List<ProfileQuizAttempt>> attemptsByRobot = new HashMap<>();
        for (ProfileQuizAttempt attempt : quizAttempts) {
            attemptsByRobot.computeIfAbsent(attempt.getRobotId(), key -> new ArrayList<>()).add(attempt);
        }

        for (Robot robot : catalogRobots) {
            ProfileRobotEntry entry = new ProfileRobotEntry();
            entry.setRobot(robot);
            applySessionFacts(entry, sessionsByRobot.getOrDefault(robot.getId(), List.of()));
            applyQuizFacts(entry, attemptsByRobot.getOrDefault(robot.getId(), List.of()));
            robotEntries.add(entry);
        }

        robotCount = robotEntries.size();
        completedRobotCount = (int) robotEntries.stream()
                .filter(entry -> COMPLETED.equals(entry.getStatusKey())).count();
        quizAttemptCount = quizAttempts.size();
        quizRobotCount = (int) robotEntries.stream().filter(ProfileRobotEntry::isQuizDataAvailable).count();
        quizDataAvailable = quizRobotCount > 0;
        completionSummary = "Đã hoàn thành " + completedRobotCount + "/" + robotCount + " mẫu robot";
        updateOverallStatus();
        updateAverageBestScore();
        buildSkillLines();
    }

    public static String formatUtcDate(String utcTimestamp) {
        return formatUtc(utcTimestamp, VIET_DATE);
    }

    public static String formatUtcDateTime(String utcTimestamp) {
        return formatUtc(utcTimestamp, VIET_DATE_TIME);
    }

    private void applySessionFacts(ProfileRobotEntry entry, List<ProfileSessionStat> facts) {
        long completedCount = 0;
        ProfileSessionStat latestCompletion = null;
        ProfileSessionStat latestInProgress = null;
        ProfileSessionStat latestAbandoned = null;
        boolean hasOpenSession = false;

        for (ProfileSessionStat fact : facts) {
            if (COMPLETED.equals(fact.getStatus())) {
                completedCount += fact.getSessionCount();
                if (isLater(fact, latestCompletion)) latestCompletion = fact;
            } else if (isOpenStatus(fact.getStatus())) {
                hasOpenSession = true;
                if (isLater(fact, latestInProgress)) latestInProgress = fact;
            } else if (ABANDONED.equals(fact.getStatus())) {
                if (isLater(fact, latestAbandoned)) latestAbandoned = fact;
            }
        }

        entry.setCompletedCount((int) completedCount);
        if (completedCount > 0) {
            entry.setStatusKey(COMPLETED);
            entry.setStatusLabel("Đã hoàn thành");
            if (latestCompletion != null) {
                entry.setCompletionDate(formatUtcDate(latestCompletion.getLatestEventAtUtc()));
                entry.setCompletionSessionId(latestCompletion.getLatestSessionId());
            }
        } else if (hasOpenSession) {
            entry.setStatusKey(IN_PROGRESS);
            entry.setStatusLabel("Đang thực hiện");
            setLatestSessionDisplay(entry, latestInProgress);
        } else if (latestAbandoned != null) {
            entry.setStatusKey(ABANDONED);
            entry.setStatusLabel("Đã dừng");
            setLatestSessionDisplay(entry, latestAbandoned);
        }
    }

    private void setLatestSessionDisplay(ProfileRobotEntry entry, ProfileSessionStat fact) {
        if (fact == null) return;
        entry.setLatestSessionStatusLabel(statusLabel(fact.getStatus()));
        entry.setLatestSessionDate(formatUtcDate(fact.getLatestEventAtUtc()));
    }

    private void applyQuizFacts(ProfileRobotEntry entry, List<ProfileQuizAttempt> attempts) {
        if (attempts.isEmpty()) return;

        ProfileQuizAttempt best = attempts.stream().max(this::compareBestAttempts).orElseThrow();
        ProfileQuizAttempt latest = attempts.stream().max(this::compareLatestAttempts).orElseThrow();
        entry.setQuizDataAvailable(true);
        entry.setQuizAttemptCount(attempts.size());
        entry.setBestQuizScore(best.getScore());
        entry.setBestQuizTotalQuestions(best.getTotalQuestions());
        entry.setBestQuizDate(formatUtcDate(best.getSubmittedAtUtc()));
        entry.setLatestQuizScore(latest.getScore());
        entry.setLatestQuizTotalQuestions(latest.getTotalQuestions());
        entry.setLatestQuizDate(formatUtcDate(latest.getSubmittedAtUtc()));
    }

    private int compareBestAttempts(ProfileQuizAttempt left, ProfileQuizAttempt right) {
        long leftRatio = (long) left.getScore() * Math.max(1, right.getTotalQuestions());
        long rightRatio = (long) right.getScore() * Math.max(1, left.getTotalQuestions());
        int byRatio = Long.compare(leftRatio, rightRatio);
        if (byRatio != 0) return byRatio;
        return compareLatestAttempts(left, right);
    }

    private int compareLatestAttempts(ProfileQuizAttempt left, ProfileQuizAttempt right) {
        int byDate = compareInstants(left.getSubmittedAtUtc(), right.getSubmittedAtUtc());
        return byDate != 0 ? byDate : Long.compare(left.getAttemptId(), right.getAttemptId());
    }

    private void updateOverallStatus() {
        if (completedRobotCount > 0) {
            overallStatusLabel = "Đã hoàn thành " + completedRobotCount + " mẫu";
            return;
        }
        boolean anyOpenSession = robotEntries.stream().anyMatch(entry -> IN_PROGRESS.equals(entry.getStatusKey()));
        overallStatusLabel = anyOpenSession ? "Đang thực hiện" : NO_DATA;
    }

    private void updateAverageBestScore() {
        List<BigDecimal> bestPercentages = new ArrayList<>();
        for (ProfileRobotEntry entry : robotEntries) {
            if (!entry.isQuizDataAvailable() || entry.getBestQuizTotalQuestions() <= 0) continue;
            BigDecimal percentage = BigDecimal.valueOf(entry.getBestQuizScore())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(entry.getBestQuizTotalQuestions()), 16, RoundingMode.HALF_UP);
            bestPercentages.add(percentage);
        }

        if (bestPercentages.isEmpty()) {
            quizDataAvailable = false;
            averageBestScoreLabel = NO_DATA;
            averageBestScorePercent = 0;
            return;
        }

        BigDecimal total = bestPercentages.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        averageBestScorePercent = total
                .divide(BigDecimal.valueOf(bestPercentages.size()), 0, RoundingMode.HALF_UP)
                .intValue();
        averageBestScoreLabel = averageBestScorePercent + "%";
    }

    private void buildSkillLines() {
        skillLines = new ArrayList<>();
        List<ProfileRobotEntry> completed = robotEntries.stream()
                .filter(entry -> COMPLETED.equals(entry.getStatusKey())).toList();
        if (completed.isEmpty()) return;

        List<String> completedNames = new ArrayList<>();
        for (ProfileRobotEntry entry : completed) {
            Robot robot = entry.getRobot();
            String robotName = safe(robot.getName());
            completedNames.add(robotName);
            ProfileSkill skill = new ProfileSkill();
            skill.setTitle(robotName);
            skill.setContent(safe(robot.getSkills()));
            String evidence = "Hoàn thành mẫu " + robotName + " ngày " + entry.getCompletionDate()
                    + ", phiên #" + entry.getCompletionSessionId();
            if (entry.isQuizDataAvailable()) {
                evidence += ", Kiểm tra: " + entry.getBestQuizDisplay();
            }
            skill.setEvidence(evidence);
            skillLines.add(skill);
        }

        Set<String> distinctNames = new LinkedHashSet<>(completedComponentNames);
        ProfileSkill components = new ProfileSkill();
        components.setTitle("Nhận biết và chuẩn bị linh kiện");
        components.setContent(distinctNames.isEmpty() ? NO_DATA : String.join(", ", distinctNames));
        components.setEvidence(String.join(", ", completedNames));
        skillLines.add(components);
    }

    private static boolean isOpenStatus(String status) {
        return PREPARING.equals(status) || READY.equals(status) || IN_PROGRESS.equals(status);
    }

    private static boolean isLater(ProfileSessionStat candidate, ProfileSessionStat current) {
        if (current == null) return true;
        int byDate = compareInstants(candidate.getLatestEventAtUtc(), current.getLatestEventAtUtc());
        return byDate > 0 || (byDate == 0 && candidate.getLatestSessionId() > current.getLatestSessionId());
    }

    private static int compareInstants(String left, String right) {
        Instant leftInstant = parseInstant(left);
        Instant rightInstant = parseInstant(right);
        int comparison = leftInstant.compareTo(rightInstant);
        return comparison != 0 ? comparison : safe(left).compareTo(safe(right));
    }

    private static Instant parseInstant(String value) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException | NullPointerException exception) {
            return Instant.MIN;
        }
    }

    private static String statusLabel(String status) {
        return switch (status) {
            case PREPARING -> "Đang chuẩn bị";
            case READY -> "Sẵn sàng";
            case IN_PROGRESS -> "Đang lắp ráp";
            case ABANDONED -> "Đã dừng";
            default -> NO_DATA;
        };
    }

    private static String formatUtc(String utcTimestamp, DateTimeFormatter formatter) {
        Instant instant = parseInstant(utcTimestamp);
        if (instant.equals(Instant.MIN)) return NO_DATA;
        return formatter.format(instant.atZone(VIETNAM_ZONE));
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
