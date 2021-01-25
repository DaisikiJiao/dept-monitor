package pres.lxx.monitor.deptmonitor.bo.dept;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString(callSuper = true)
public class OkexExchangeDeptBO extends BaseExchangeDeptBO {

    private String size;
    private String depth;

}
