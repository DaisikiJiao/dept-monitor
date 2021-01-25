package pres.lxx.monitor.deptmonitor.dto.dept;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@ToString
public abstract class BaseDeptDTO {

    private String exchange;
    private String symbol;
    private List<List<String>> bids;
    private List<List<String>> asks;
    private Timestamp timestamp;

}
