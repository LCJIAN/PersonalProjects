package com.org.firefighting.data.network.entity;

import java.util.List;

public class TaskQuestion {

    public String id;
    public String taskId;
    public String question;
    public Long createDate;
    public String createByCode;
    public String createByName;
    public String departmentName;
    public String departmentCode;
    public List<TaskAnswer> answers;
    public String refId;

}
