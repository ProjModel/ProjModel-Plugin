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

    public String getAssignee() { return assignee; }
    public int getTotalTasks() { return totalTasks; }
    public int getTasksDueWithin7Days() { return tasksDueWithin7Days; }
    public int getOverdueTasks() { return overdueTasks; }
    public int getTasksWithoutDueDate() { return tasksWithoutDueDate; }
    public String getLoadLevel() { return loadLevel; }

    //Для шкалы
    public int getLoadPercent()
    {
        //условная шкала
        switch (loadLevel)
        {
            case "low": return 40;
            case "medium": return 70;
            case "high": return 90;
            case "critical": return 110;
            default: return 0;
        }
    }
}
