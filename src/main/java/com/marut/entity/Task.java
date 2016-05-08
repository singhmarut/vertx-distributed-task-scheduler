package com.marut.entity;

import java.util.UUID;

/**
 * Created by marutsingh on 4/29/16.
 */
public class Task {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        if (!uuid.equals(task.uuid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    public enum TaskStatus{
        PULL,
        PROCESSED
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    private String uuid = UUID.randomUUID().toString();
    private TaskStatus status = TaskStatus.PULL;
}
