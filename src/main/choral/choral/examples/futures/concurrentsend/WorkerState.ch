package choral.examples.futures.concurrentsend;

public class WorkerState@A {
    String@A workerName;

    public WorkerState(String@A workerName) {
        this.workerName = workerName;
    }

    public String@A compute() {
        return "Data from "@A + workerName;
    }
}