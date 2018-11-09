package at.meks.backupclientserver.client.filechangehandler;

import at.meks.backupclientserver.client.backupmanager.TodoEntry;
import com.google.common.primitives.Ints;

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
    public int compareTo(Delayed o) {
        return Ints.saturatedCast(readStartTime - ((DelayedFileChange) o).readStartTime);
    }

    TodoEntry getTodoEntry() {
        return todoEntry;
    }

    long getDelayInMilliseconds() {
        return DELAY_IN_MILLISECONDS;
    }
}
