package dev.stockflow.core;

import dev.stockflow.core.state.StatusTask;
import dev.stockflow.core.practice.FakeTask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestTask {
    @Test
    public void create_normal_successful() {
        FakeTask task = new FakeTask("A1");
        assertEquals(StatusTask.OPEN, task.getStatusTask());
    }

    @Test
    public void create_closeTask_successful() {
        FakeTask task = new FakeTask("A1");
        assertEquals(StatusTask.OPEN, task.getStatusTask());
        task.close();
        assertEquals(StatusTask.CLOSED, task.getStatusTask());
        task.close();
        assertEquals(StatusTask.CLOSED, task.getStatusTask());
    }

    @Test
    public void create_reopenTask_successful() {
        FakeTask task = new FakeTask("A1");
        assertEquals(StatusTask.OPEN, task.getStatusTask());
        task.close();
        assertEquals(StatusTask.CLOSED, task.getStatusTask());
        task.reopen();
        assertEquals(StatusTask.OPEN, task.getStatusTask());
        task.reopen();
        assertEquals(StatusTask.OPEN, task.getStatusTask());
        task.close();
        assertEquals(StatusTask.CLOSED, task.getStatusTask());
    }

    @Test
    public void create_openArchiveTask_throwException() {
        FakeTask task = new FakeTask("A1");
        assertEquals(StatusTask.OPEN, task.getStatusTask());
        Throwable exception = assertThrows(IllegalStateException.class, task::archive);
        assertEquals("Нельзя архивировать открытое обращение", exception.getMessage());
        assertEquals(StatusTask.OPEN, task.getStatusTask());
    }

    @Test
    public void create_archiveTask_successful() {
        FakeTask task = new FakeTask("A1");
        assertEquals(StatusTask.OPEN, task.getStatusTask());
        task.close();
        assertEquals(StatusTask.CLOSED, task.getStatusTask());
        task.archive();
        assertEquals(StatusTask.ARCHIVED, task.getStatusTask());
        task.archive();
        assertEquals(StatusTask.ARCHIVED, task.getStatusTask());
    }

    @Test
    public void create_archiveTaskToReopen_throwException() {
        FakeTask task = new FakeTask("A1");
        assertEquals(StatusTask.OPEN, task.getStatusTask());
        task.close();
        assertEquals(StatusTask.CLOSED, task.getStatusTask());
        task.archive();
        assertEquals(StatusTask.ARCHIVED, task.getStatusTask());
        Throwable exception = assertThrows(IllegalStateException.class, task::close);
        assertEquals("Нельзя закрыть архивное обращение", exception.getMessage());
        assertEquals(StatusTask.ARCHIVED, task.getStatusTask());

        Throwable exception2 = assertThrows(IllegalStateException.class, task::reopen);
        assertEquals("Нельзя открыть архивное обращение", exception2.getMessage());
        assertEquals(StatusTask.ARCHIVED, task.getStatusTask());
    }

    @Test
    public void create_twoObjects_successful() {
        FakeTask task = new FakeTask("A1");
        FakeTask task2 = new FakeTask("A1");
        assertEquals(StatusTask.OPEN, task.getStatusTask());
        assertEquals(StatusTask.OPEN, task2.getStatusTask());
        task.close();
        assertEquals(StatusTask.CLOSED, task.getStatusTask());
        assertEquals(StatusTask.OPEN, task2.getStatusTask());
        task.archive();
        assertEquals(StatusTask.ARCHIVED, task.getStatusTask());
        assertEquals(StatusTask.OPEN, task2.getStatusTask());
        task2.close();
        assertEquals(StatusTask.ARCHIVED, task.getStatusTask());
        assertEquals(StatusTask.CLOSED, task2.getStatusTask());
        task2.reopen();
        assertEquals(StatusTask.ARCHIVED, task.getStatusTask());
        assertEquals(StatusTask.OPEN, task2.getStatusTask());

    }

}
