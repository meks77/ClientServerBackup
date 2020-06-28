package at.meks.backupclientserver.client.filechangehandler;

import at.meks.backupclientserver.client.backupmanager.TodoEntry;

import javax.validation.constraints.NotNull;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

class DelayedFileChange implements Delayed {

    static final long DELAY_IN_MILLISECONDS = 1000;
    private final TodoEntry todoEntry;
    private long readStartTime;


    DelayedFileChange(TodoEntry todoEntry) {
        this.todoEntry = todoEntry;
        readStartTime = System.currentTimeMillis() + DELAY_IN_MILLISECONDS;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = readStartTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(@NotNull Delayed o) {
        return (int) (readStartTime - ((DelayedFileChange) o).readStartTime);
    }

    TodoEntry getTodoEntry() {
        return todoEntry;
    }

    long getDelayInMilliseconds() {
        return DELAY_IN_MILLISECONDS;
    }
}
