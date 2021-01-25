package pres.lxx.monitor.deptmonitor.dto.dept;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class BinanceDeptDTO extends BaseDeptDTO {

    private long lastUpdateId;


}
