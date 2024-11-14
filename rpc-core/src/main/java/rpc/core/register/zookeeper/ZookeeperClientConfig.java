package rpc.core.register.zookeeper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ZookeeperClientConfig {
    public final static int DEFAULT_BASE_SLEEP_TIMES = 1000;
    public final static int DEFAULT_MAX_RETRIES = 5;
    private String address;
    private int baseSleepTimes = DEFAULT_BASE_SLEEP_TIMES;
    private int maxRetries = DEFAULT_MAX_RETRIES;

    public ZookeeperClientConfig(String address) {
        this.address = address;
    }
}
