package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/** Dữ liệu và quy tắc nghiệp vụ của tài liệu hồ sơ học tập có thể in. */
public class LearningProfile implements Serializable {
    private static final String COMPLETED = "COMPLETED";
    private static final String PREPARING = "PREPARING";
    private static final String READY = "READY";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String ABANDONED = "ABANDONED";
    private static final String NO_DATA = "Chưa có dữ liệu";

    private String fullName;
    private Date generatedAt;
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
        generatedAt = null;
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

    public Date getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Date generatedAt) {
        this.generatedAt = generatedAt;
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
        if (catalogRobots == null) {
            this.catalogRobots = new ArrayList<>();
        } else {
            this.catalogRobots = catalogRobots;
        }
    }

    public List<ProfileSessionStat> getSessionStats() {
        return sessionStats;
    }

    public void setSessionStats(List<ProfileSessionStat> sessionStats) {
        if (sessionStats == null) {
            this.sessionStats = new ArrayList<>();
        } else {
            this.sessionStats = sessionStats;
        }
    }

    public List<ProfileQuizAttempt> getQuizAttempts() {
        return quizAttempts;
    }

    public void setQuizAttempts(List<ProfileQuizAttempt> quizAttempts) {
        if (quizAttempts == null) {
            this.quizAttempts = new ArrayList<>();
        } else {
            this.quizAttempts = quizAttempts;
        }
    }

    public List<String> getCompletedComponentNames() {
        return completedComponentNames;
    }

    public void setCompletedComponentNames(List<String> completedComponentNames) {
        if (completedComponentNames == null) {
            this.completedComponentNames = new ArrayList<>();
        } else {
            this.completedComponentNames = completedComponentNames;
        }
    }

    public List<ProfileRobotEntry> getRobotEntries() {
        return robotEntries;
    }

    public void setRobotEntries(List<ProfileRobotEntry> robotEntries) {
        if (robotEntries == null) {
            this.robotEntries = new ArrayList<>();
        } else {
            this.robotEntries = robotEntries;
        }
    }

    public List<ProfileSkill> getSkillLines() {
        return skillLines;
    }

    public void setSkillLines(List<ProfileSkill> skillLines) {
        if (skillLines == null) {
            this.skillLines = new ArrayList<>();
        } else {
            this.skillLines = skillLines;
        }
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
        generatedAtDisplay = formatDateTime(generatedAt);
        robotEntries = new ArrayList<>();

        Map<String, List<ProfileSessionStat>> sessionsByRobot = new HashMap<>();
        for (int i = 0; i < sessionStats.size(); i++) {
            ProfileSessionStat stat = sessionStats.get(i);
            List<ProfileSessionStat> group = sessionsByRobot.get(stat.getRobotId());
            if (group == null) {
                group = new ArrayList<>();
                sessionsByRobot.put(stat.getRobotId(), group);
            }
            group.add(stat);
        }
        Map<String, List<ProfileQuizAttempt>> attemptsByRobot = new HashMap<>();
        for (int i = 0; i < quizAttempts.size(); i++) {
            ProfileQuizAttempt attempt = quizAttempts.get(i);
            List<ProfileQuizAttempt> group = attemptsByRobot.get(attempt.getRobotId());
            if (group == null) {
                group = new ArrayList<>();
                attemptsByRobot.put(attempt.getRobotId(), group);
            }
            group.add(attempt);
        }

        for (int i = 0; i < catalogRobots.size(); i++) {
            Robot robot = catalogRobots.get(i);
            ProfileRobotEntry entry = new ProfileRobotEntry();
            entry.setRobot(robot);
            List<ProfileSessionStat> robotSessions = sessionsByRobot.get(robot.getId());
            if (robotSessions == null) {
                robotSessions = new ArrayList<>();
            }
            List<ProfileQuizAttempt> robotAttempts = attemptsByRobot.get(robot.getId());
            if (robotAttempts == null) {
                robotAttempts = new ArrayList<>();
            }
            applySessionFacts(entry, robotSessions);
            applyQuizFacts(entry, robotAttempts);
            robotEntries.add(entry);
        }

        robotCount = robotEntries.size();
        completedRobotCount = 0;
        quizRobotCount = 0;
        for (int i = 0; i < robotEntries.size(); i++) {
            ProfileRobotEntry entry = robotEntries.get(i);
            if (entry.isCompleted()) {
                completedRobotCount++;
            }
            if (entry.isQuizDataAvailable()) {
                quizRobotCount++;
            }
        }
        quizAttemptCount = quizAttempts.size();
        quizDataAvailable = quizRobotCount > 0;
        completionSummary = "Đã hoàn thành " + completedRobotCount + "/" + robotCount + " mẫu robot";
        updateOverallStatus();
        updateAverageBestScore();
        buildSkillLines();
    }

    public static String formatDate(Date date) {
        return formatDate(date, "dd/MM/yyyy");
    }

    public static String formatDateTime(Date date) {
        return formatDate(date, "dd/MM/yyyy HH:mm");
    }

    private void applySessionFacts(ProfileRobotEntry entry, List<ProfileSessionStat> facts) {
        long completedCount = 0;
        ProfileSessionStat latestCompletion = null;
        ProfileSessionStat latestInProgress = null;
        ProfileSessionStat latestAbandoned = null;
        boolean hasOpenSession = false;

        for (int i = 0; i < facts.size(); i++) {
            ProfileSessionStat fact = facts.get(i);
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
                entry.setCompletionDate(formatDate(latestCompletion.getLatestEventAt()));
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
        entry.setLatestSessionDate(formatDate(fact.getLatestEventAt()));
    }

    private void applyQuizFacts(ProfileRobotEntry entry, List<ProfileQuizAttempt> attempts) {
        if (attempts.isEmpty()) return;

        ProfileQuizAttempt best = attempts.get(0);
        ProfileQuizAttempt latest = attempts.get(0);
        for (int i = 1; i < attempts.size(); i++) {
            ProfileQuizAttempt candidate = attempts.get(i);
            if (compareBestAttempts(candidate, best) > 0) {
                best = candidate;
            }
            if (compareLatestAttempts(candidate, latest) > 0) {
                latest = candidate;
            }
        }
        entry.setQuizDataAvailable(true);
        entry.setQuizAttemptCount(attempts.size());
        entry.setBestQuizScore(best.getScore());
        entry.setBestQuizTotalQuestions(best.getTotalQuestions());
        entry.setBestQuizDate(formatDate(best.getSubmittedAt()));
        entry.setLatestQuizScore(latest.getScore());
        entry.setLatestQuizTotalQuestions(latest.getTotalQuestions());
        entry.setLatestQuizDate(formatDate(latest.getSubmittedAt()));
    }

    private int compareBestAttempts(ProfileQuizAttempt left, ProfileQuizAttempt right) {
        long leftRatio = (long) left.getScore() * Math.max(1, right.getTotalQuestions());
        long rightRatio = (long) right.getScore() * Math.max(1, left.getTotalQuestions());
        int byRatio = Long.compare(leftRatio, rightRatio);
        if (byRatio != 0) return byRatio;
        return compareLatestAttempts(left, right);
    }

    private int compareLatestAttempts(ProfileQuizAttempt left, ProfileQuizAttempt right) {
        int byDate = compareDates(left.getSubmittedAt(), right.getSubmittedAt());
        if (byDate != 0) {
            return byDate;
        }
        return Long.compare(left.getAttemptId(), right.getAttemptId());
    }

    private void updateOverallStatus() {
        if (completedRobotCount > 0) {
            overallStatusLabel = "Đã hoàn thành " + completedRobotCount + " mẫu";
            return;
        }
        boolean anyOpenSession = false;
        boolean anyAbandonedSession = false;
        for (int i = 0; i < robotEntries.size(); i++) {
            ProfileRobotEntry entry = robotEntries.get(i);
            if (entry.isInProgress()) {
                anyOpenSession = true;
            }
            if (entry.isStopped()) {
                anyAbandonedSession = true;
            }
        }
        if (anyOpenSession) {
            overallStatusLabel = "Đang thực hiện";
            return;
        }
        if (anyAbandonedSession) {
            overallStatusLabel = "Đã dừng";
            return;
        }
        if (quizAttemptCount > 0) {
            overallStatusLabel = "Chưa hoàn thành mẫu nào";
        } else {
            overallStatusLabel = NO_DATA;
        }
    }

    private void updateAverageBestScore() {
        // BigDecimal giữ phép tính và làm tròn HALF_UP chính xác, không dùng số thực nhị phân.
        BigDecimal total = BigDecimal.ZERO;
        int bestPercentageCount = 0;
        for (int i = 0; i < robotEntries.size(); i++) {
            ProfileRobotEntry entry = robotEntries.get(i);
            if (!entry.isQuizDataAvailable() || entry.getBestQuizTotalQuestions() <= 0) continue;
            BigDecimal percentage = BigDecimal.valueOf(entry.getBestQuizScore())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(entry.getBestQuizTotalQuestions()), 16, RoundingMode.HALF_UP);
            total = total.add(percentage);
            bestPercentageCount++;
        }

        if (bestPercentageCount == 0) {
            quizDataAvailable = false;
            averageBestScoreLabel = NO_DATA;
            averageBestScorePercent = 0;
            return;
        }

        averageBestScorePercent = total
                .divide(BigDecimal.valueOf(bestPercentageCount), 0, RoundingMode.HALF_UP)
                .intValue();
        averageBestScoreLabel = averageBestScorePercent + "%";
    }

    private void buildSkillLines() {
        skillLines = new ArrayList<>();
        List<ProfileRobotEntry> completed = new ArrayList<>();
        for (int i = 0; i < robotEntries.size(); i++) {
            ProfileRobotEntry entry = robotEntries.get(i);
            if (entry.isCompleted()) {
                completed.add(entry);
            }
        }
        if (completed.isEmpty()) return;

        List<String> completedNames = new ArrayList<>();
        for (int i = 0; i < completed.size(); i++) {
            ProfileRobotEntry entry = completed.get(i);
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

        List<String> distinctNames = new ArrayList<>();
        for (int i = 0; i < completedComponentNames.size(); i++) {
            String name = completedComponentNames.get(i);
            if (!distinctNames.contains(name)) {
                distinctNames.add(name);
            }
        }
        ProfileSkill components = new ProfileSkill();
        components.setTitle("Nhận biết và chuẩn bị linh kiện");
        if (distinctNames.isEmpty()) {
            components.setContent(NO_DATA);
        } else {
            components.setContent(joinNames(distinctNames));
        }
        components.setEvidence(joinNames(completedNames));
        skillLines.add(components);
    }

    private static boolean isOpenStatus(String status) {
        return PREPARING.equals(status) || READY.equals(status) || IN_PROGRESS.equals(status);
    }

    private static String joinNames(List<String> names) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                text.append(", ");
            }
            text.append(names.get(i));
        }
        return text.toString();
    }

    private static boolean isLater(ProfileSessionStat candidate, ProfileSessionStat current) {
        if (current == null) return true;
        int byDate = compareDates(candidate.getLatestEventAt(), current.getLatestEventAt());
        return byDate > 0 || (byDate == 0 && candidate.getLatestSessionId() > current.getLatestSessionId());
    }

    private static int compareDates(Date left, Date right) {
        if (left == null && right == null) {
            return 0;
        } else if (left == null) {
            return -1;
        } else if (right == null) {
            return 1;
        }
        return left.compareTo(right);
    }

    private static String statusLabel(String status) {
        if (PREPARING.equals(status)) {
            return "Đang chuẩn bị";
        } else if (READY.equals(status)) {
            return "Sẵn sàng";
        } else if (IN_PROGRESS.equals(status)) {
            return "Đang lắp ráp";
        } else if (ABANDONED.equals(status)) {
            return "Đã dừng";
        }
        return NO_DATA;
    }

    private static String formatDate(Date date, String pattern) {
        if (date == null) return NO_DATA;
        // Tạo formatter mới mỗi lần như Ch9/29; SimpleDateFormat không an toàn khi dùng chung giữa các luồng.
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return format.format(date);
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}
