package com.langqi.xxt_reminder.model;

public class HomeworkInfo {
    public String subject;
    public String homeworkName;
    public String homeworkStatus;
    public String url;
    public String taskrefId;
    public String deadline;
    public boolean submitted;

    public HomeworkInfo(String subject, String homeworkName, String homeworkStatus, String url, String taskrefId,
            String deadline) {
        this.subject = subject;
        this.homeworkName = homeworkName;
        this.homeworkStatus = homeworkStatus;
        this.url = url;
        this.taskrefId = taskrefId;
        this.deadline = deadline;
        this.submitted = homeworkStatus != null && !"未提交".equals(homeworkStatus) && !"未交".equals(homeworkStatus);
    }
}