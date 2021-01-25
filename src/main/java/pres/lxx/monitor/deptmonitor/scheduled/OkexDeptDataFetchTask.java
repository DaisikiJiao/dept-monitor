package pres.lxx.monitor.deptmonitor.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pres.lxx.monitor.deptmonitor.bo.dept.OkexExchangeDeptBO;
import pres.lxx.monitor.deptmonitor.service.DeptDataService;

@Slf4j
@Component
public class OkexDeptDataFetchTask {

    @Value("${okex.dept.realtime.symbol}")
    private String[] realtimeSymbol;
    @Value("${okex.dept.realtime.size}")
    private String[] realtimeSize;
    @Value("${okex.dept.realtime.depth}")
    private String[] realtimeDepth;
    @Value("${okex.dept.realtime.threshold}")
    private String[] realtimeThreshold;

    @Value("${okex.dept.snapshot.symbol}")
    private String[] snapshotSymbol;
    @Value("${okex.dept.snapshot.size}")
    private String[] snapshotSize;
    @Value("${okex.dept.snapshot.depth}")
    private String[] snapshotDepth;
    @Value("${okex.dept.snapshot.threshold}")
    private String[] snapshotThreshold;

    @Autowired
    private DeptDataService deptDataService;

    @Scheduled(cron = "${okex.dept.realtime.scheduled.cron}")
    public void fetchOkexRealtimeDeptData() {
        for (int i = 0; i < realtimeSymbol.length; i++) {
            String symbol = realtimeSymbol[i];
            String size = realtimeSize[i];
            String depth = realtimeDepth[i];

            long threshold = Long.parseLong(realtimeThreshold[i]);

            OkexExchangeDeptBO okexExchangeDept = new OkexExchangeDeptBO(size,depth);
            okexExchangeDept.setSymbol(symbol);
            okexExchangeDept.setThreshold(threshold);
            deptDataService.invokeRealtimeDeptData(okexExchangeDept);
        }
    }

    @Scheduled(cron = "${okex.dept.snapshot.scheduled.cron}")
    public void fetchOkexSnapshotDeptData(){
        for (int i = 0; i < snapshotSymbol.length; i++) {
            String symbol = snapshotSymbol[i];
            String size = snapshotSize[i];
            String depth = snapshotDepth[i];

            long threshold = Long.parseLong(snapshotThreshold[i]);

            OkexExchangeDeptBO okexExchangeDept = new OkexExchangeDeptBO(size,depth);
            okexExchangeDept.setSymbol(symbol);
            okexExchangeDept.setThreshold(threshold);
            deptDataService.invokeSnapshotDeptData(okexExchangeDept);
        }
    }

}
