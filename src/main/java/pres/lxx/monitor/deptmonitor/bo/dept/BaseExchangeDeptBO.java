package pres.lxx.monitor.deptmonitor.bo.dept;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public abstract class BaseExchangeDeptBO {

    private String symbol;
    private long threshold;

}
