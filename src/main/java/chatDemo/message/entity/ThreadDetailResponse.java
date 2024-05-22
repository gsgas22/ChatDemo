package chatDemo.message.entity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class ThreadDetailResponse {
    private List<Map<String, Object>> data;

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }
}

