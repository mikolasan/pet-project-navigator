package io.github.mikolasan.petprojectnavigator;

/**
 * Created by neupo on 3/25/2017.
 */

public class PetTask {
    public PetTask() {
        this.taskId = 0;
        this.projectId = 0;
        this.name = "";
        this.links = "";
        this.statement = "";
        this.tech = 0;
        this.time = 0;
        this.type = 0;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLinks() {
        return links;
    }

    public void setLinks(String links) {
        this.links = links;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public int getTech() {
        return tech;
    }

    public void setTech(int tech) {
        this.tech = tech;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    int taskId;
    int projectId;
    String name;
    String links;
    String statement;
    int tech;
    int time;
    int type;
}

