package choral.examples.ozone.concurrentsend;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ServerState {
    public ConcurrentHashMap<Integer, Long> startTimes;
    public ConcurrentHashMap<Integer, Long> keyAckTimes;
    public ConcurrentHashMap<Integer, Long> txtAckTimes;

    public ServerState() {
        this.startTimes = new ConcurrentHashMap<>();
        this.keyAckTimes = new ConcurrentHashMap<>();
        this.txtAckTimes = new ConcurrentHashMap<>();
    }

    public void init(Integer input) {
        this.startTimes.put(input, System.nanoTime());
    }

    public void onKeyAck(Integer input) {
        this.keyAckTimes.put(input, System.nanoTime());
    }

    public void onTxtAck(Integer input) {
        this.txtAckTimes.put(input, System.nanoTime());
    }

    public Iterable<Float> getKeyLatencies() {
        ArrayList<Float> latencies = new ArrayList<>();

        for (Integer index : startTimes.keySet()) {
            float latency = ((float) (keyAckTimes.get(index) - startTimes.get(index))) / 1000000.0F;
            latencies.add(latency);
        }
        return latencies;
    }

    public Iterable<Float> getTxtLatencies() {
        ArrayList<Float> latencies = new ArrayList<>();

        for (Integer index : startTimes.keySet()) {
            float latency = ((float) (txtAckTimes.get(index) - startTimes.get(index))) / 1000000.0F;
            latencies.add(latency);
        }
        return latencies;
    }
}
