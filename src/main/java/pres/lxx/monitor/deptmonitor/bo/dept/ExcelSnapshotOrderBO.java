package pres.lxx.monitor.deptmonitor.bo.dept;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@ToString
public class ExcelSnapshotOrderBO {

    private String position;
    private BigDecimal price;
    private BigDecimal amount;
    private String startTime;

}
