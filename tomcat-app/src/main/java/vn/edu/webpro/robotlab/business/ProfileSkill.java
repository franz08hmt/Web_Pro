package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Một dòng kỹ năng/kiến thức trong hồ sơ, luôn đi kèm căn cứ dữ liệu. */
public class ProfileSkill implements Serializable {
    private String title;
    private String content;
    private String evidence;

    public ProfileSkill() {
        title = "";
        content = "";
        evidence = "";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }
}
