package dev.stockflow.core.practice;

import dev.stockflow.core.exceprion.ProductException;
import dev.stockflow.core.state.StatusTask;

public class FakeTask {
    private String taskId;
    private StatusTask statusTask = StatusTask.OPEN;

    public FakeTask(String taskId) {
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new ProductException("Не указан или неверный идентификатор обращения");
        }

        this.taskId = taskId;
    }

    public StatusTask getStatusTask() {
        return statusTask;
    }

    public void close() {
        if (this.getStatusTask() == StatusTask.ARCHIVED) {
            throw new IllegalStateException("Нельзя закрыть архивное обращение");
        }
        if (this.getStatusTask() == StatusTask.OPEN) {
            this.statusTask = StatusTask.CLOSED;
        }
    }

    public void reopen() {
        if (this.getStatusTask() == StatusTask.ARCHIVED) {
            throw new IllegalStateException("Нельзя открыть архивное обращение");
        }
        if (this.getStatusTask() == StatusTask.CLOSED) {
            this.statusTask = StatusTask.OPEN;
        }

    }

    public void archive() {
        if (this.getStatusTask() == StatusTask.OPEN) {
            throw new IllegalStateException("Нельзя архивировать открытое обращение");
        }
        if (this.getStatusTask() == StatusTask.CLOSED) {
            this.statusTask = StatusTask.ARCHIVED;
        }
    }
}
