package pres.lxx.monitor.deptmonitor.dto.dept;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@ToString
public class OrderDTO {

    private int position;
    private BigDecimal price;
    private BigDecimal amount;
    private long startTime;
    private long duration;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDTO orderDTO = (OrderDTO) o;
        return price.toPlainString().equals(orderDTO.price.toPlainString());
    }

    @Override
    public int hashCode() {
        return price.toPlainString().hashCode();
    }
}
