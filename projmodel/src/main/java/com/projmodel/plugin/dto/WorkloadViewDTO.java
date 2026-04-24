package com.projmodel.plugin.dto;

public class WorkloadViewDTO {
    private final String assignee;
    private final int totalTasks;
    private final int tasksDueWithin7Days;
    private final int overdueTasks;
    private final int tasksWithoutDueDate;
    private final String loadLevel; // low, medium, high, critical

    public WorkloadViewDTO(String assignee, int totalTasks, int tasksDueWithin7Days,
                           int overdueTasks, int tasksWithoutDueDate, String loadLevel) {
        this.assignee = assignee;
        this.totalTasks = totalTasks;
        this.tasksDueWithin7Days = tasksDueWithin7Days;
        this.overdueTasks = overdueTasks;
        this.tasksWithoutDueDate = tasksWithoutDueDate;
        this.loadLevel = loadLevel;
    }
}
