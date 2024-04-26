package ex1.synchronizers.service;

import ex1.synchronizers.worker.master.MasterWorker;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandService {
    private final MasterWorker masterWorker;
    private int total;

    public CommandService(final MasterWorker masterWorker) {
        this.masterWorker = masterWorker;
    }

    public void setup(final int total) {
        this.total = total;
    }


    public void runTask(final List<? extends Future<?>> futures) {
        final AtomicInteger count = new AtomicInteger(0);
        futures.forEach(future -> {
            try {
                future.get();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }

            if (this.masterWorker.hasCommands() && count.incrementAndGet() >= this.total) {
                this.runTask(this.masterWorker.callNextTaskCommand());
            } else {
                this.masterWorker.startSimulation();
            }
        });
    }


}