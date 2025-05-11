package com.langqi.xxt_reminder;

public class HomeworkItem {
    public String content;
    public boolean submitted; // true=已提交，false=未提交

    public HomeworkItem(String content, boolean submitted) {
        this.content = content;
        this.submitted = submitted;
    }
}