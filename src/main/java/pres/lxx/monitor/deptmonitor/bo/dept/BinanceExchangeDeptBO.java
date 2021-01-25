package pres.lxx.monitor.deptmonitor.bo.dept;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@ToString(callSuper = true)
public class BinanceExchangeDeptBO extends BaseExchangeDeptBO {

    private int limit;

}
