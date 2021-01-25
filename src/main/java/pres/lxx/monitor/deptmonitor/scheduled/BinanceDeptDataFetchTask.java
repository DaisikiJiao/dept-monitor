package pres.lxx.monitor.deptmonitor.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pres.lxx.monitor.deptmonitor.bo.dept.BinanceExchangeDeptBO;
import pres.lxx.monitor.deptmonitor.service.DeptDataService;

@Slf4j
@Component
public class BinanceDeptDataFetchTask {

    @Value("${binance.dept.realtime.symbol}")
    private String[] realtimeSymbol;
    @Value("${binance.dept.realtime.limit}")
    private String[] realtimeLimit;
    @Value("${binance.dept.realtime.threshold}")
    private String[] realtimeThreshold;

    @Value("${binance.dept.snapshot.symbol}")
    private String[] snapshotSymbol;
    @Value("${binance.dept.snapshot.limit}")
    private String[] snapshotLimit;
    @Value("${binance.dept.snapshot.threshold}")
    private String[] snapshotThreshold;

    @Autowired
    private DeptDataService deptDataService;

    @Scheduled(cron = "${binance.dept.realtime.scheduled.cron}")
    public void fetchBinanceRealtimeDeptData() {
        for (int i = 0; i < realtimeSymbol.length; i++) {
            String symbol = realtimeSymbol[i];
            int limit = Integer.parseInt(realtimeLimit[i]);
            long threshold = Long.parseLong(realtimeThreshold[i]);

            BinanceExchangeDeptBO binanceExchangeDept = new BinanceExchangeDeptBO(limit);
            binanceExchangeDept.setSymbol(symbol);
            binanceExchangeDept.setThreshold(threshold);
            deptDataService.invokeRealtimeDeptData(binanceExchangeDept);
        }
    }

    @Scheduled(cron = "${binance.dept.snapshot.scheduled.cron}")
    public void fetchBinanceSnapshotDeptData(){
        for (int i = 0; i < snapshotSymbol.length; i++) {
            String symbol = snapshotSymbol[i];
            int limit = Integer.parseInt(snapshotLimit[i]);
            long threshold = Long.parseLong(snapshotThreshold[i]);

            BinanceExchangeDeptBO binanceExchangeDept = new BinanceExchangeDeptBO(limit);
            binanceExchangeDept.setSymbol(symbol);
            binanceExchangeDept.setThreshold(threshold);
            deptDataService.invokeSnapshotDeptData(binanceExchangeDept);
        }
    }

}
