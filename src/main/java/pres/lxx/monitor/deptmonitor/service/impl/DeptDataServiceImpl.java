package pres.lxx.monitor.deptmonitor.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pres.lxx.monitor.deptmonitor.bo.dept.*;
import pres.lxx.monitor.deptmonitor.constant.BinanceApiConstant;
import pres.lxx.monitor.deptmonitor.constant.ExchangeConstant;
import pres.lxx.monitor.deptmonitor.constant.OkexApiConstant;
import pres.lxx.monitor.deptmonitor.dto.dept.*;
import pres.lxx.monitor.deptmonitor.service.DeptDataService;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DeptDataServiceImpl implements DeptDataService {

    private static Map<String, Set<OrderDTO>> REALTIME_BIG_ORDER_MAP = new ConcurrentHashMap<>();

    @Async
    @Override
    public void invokeRealtimeDeptData(BaseExchangeDeptBO exchangeDept) {
        BaseDeptDTO deptData = invokeExchangeDeptData(exchangeDept);
        log.debug("Received " + exchangeDept.getSymbol() + " dept data:{}", exchangeDept);
        if (deptData!=null) exportRealtimeBigOrderData2Excel(deptData, exchangeDept.getThreshold());
    }

    @Async
    @Override
    public void invokeSnapshotDeptData(BaseExchangeDeptBO exchangeDept) {
        BaseDeptDTO deptData = invokeExchangeDeptData(exchangeDept);
        log.debug("Received " + exchangeDept.getSymbol() + " dept data:{}", exchangeDept);
        if (deptData!=null) exportSnapshotBigOrderData2Excel(deptData, exchangeDept.getThreshold());
    }

    private BaseDeptDTO invokeExchangeDeptData(BaseExchangeDeptBO exchangeDept) {
        if (exchangeDept instanceof BinanceExchangeDeptBO) {
            BinanceExchangeDeptBO binanceExchangeDept = (BinanceExchangeDeptBO) exchangeDept;
            return invokeBinanceDeptData(binanceExchangeDept.getSymbol(), binanceExchangeDept.getLimit());
        } else if (exchangeDept instanceof OkexExchangeDeptBO) {
            OkexExchangeDeptBO okexExchangeDept = (OkexExchangeDeptBO) exchangeDept;
            return invokeOkexDeptData(okexExchangeDept.getSymbol(), okexExchangeDept.getSize(), okexExchangeDept.getDepth());
        }
        return null;
    }

    private BinanceDeptDTO invokeBinanceDeptData(String symbol, int limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("symbol", symbol);
        params.put("limit", limit);

        String BINANCE_DEPT_URL = BinanceApiConstant.HOST + BinanceApiConstant.DEPT;
        String result = HttpUtil.get(BINANCE_DEPT_URL, params);
        BinanceDeptDTO binanceDeptData = JSON.parseObject(result, new TypeReference<BinanceDeptDTO>(){});
        binanceDeptData.setExchange(ExchangeConstant.BINANCE);
        binanceDeptData.setSymbol(symbol);
        binanceDeptData.setTimestamp(new Timestamp(System.currentTimeMillis()));
        return binanceDeptData;
    }

    private OkexDeptDTO invokeOkexDeptData(String symbol, String size, String depth) {
        Map<String, Object> params = new HashMap<>();
        params.put("size", size);
        params.put("depth", depth);

        String OKEX_DEPT_URL = OkexApiConstant.HOST + OkexApiConstant.DEPT;
        String url = OKEX_DEPT_URL.replace("<instrument_id>", symbol);
        String result = HttpUtil.get(url, params);
        OkexDeptDTO okexDeptData = JSON.parseObject(result, new TypeReference<OkexDeptDTO>(){});
        okexDeptData.setExchange(ExchangeConstant.OKEX);
        okexDeptData.setSymbol(symbol);
        okexDeptData.setTimestamp(new Timestamp(System.currentTimeMillis()));
        return okexDeptData;
    }

    private void exportRealtimeBigOrderData2Excel(BaseDeptDTO deptData, long threshold) {
        String exchangeName = deptData.getExchange();
        String symbol = deptData.getSymbol();
        String bigOrderMapKey = exchangeName + "-" + symbol;
        Set<OrderDTO> bigOrders = REALTIME_BIG_ORDER_MAP.getOrDefault(bigOrderMapKey, new HashSet<>());

        addNewBigOrder(deptData, bigOrders, threshold);
        Set<ExcelRealtimeOrderBO> exportBigOrders = convert2ExcelRealtimeOrder(deptData, bigOrders);
        if (exportBigOrders.size() > 0) {
            String title = exchangeName + " " + symbol;
            log.info(title + "盘口现存大单:{}", bigOrders);
            log.info(title + "盘口消失大单:{}", exportBigOrders);
            String dateSuffix = LocalDate.now().toString();
            String filePath = System.getProperty("user.dir") + File.separator + "data" + File.separator + "realtime" + File.separator + exchangeName + "_" + symbol + "_" + dateSuffix + ".xlsx";

            ExcelWriter excelWriter = ExcelUtil.getWriter(filePath);
            if (FileUtil.exist(filePath)){
                excelWriter.setCurrentRowToEnd();
                excelWriter.write(exportBigOrders, false);
            } else {
                excelWriter.merge(4, title);
                excelWriter.addHeaderAlias("position", "方向");
                excelWriter.addHeaderAlias("price", "价格");
                excelWriter.addHeaderAlias("amount", "数量");
                excelWriter.addHeaderAlias("startTime", "开始时间");
                excelWriter.addHeaderAlias("duration", "持续时间");
                excelWriter.write(exportBigOrders, true);
            }
            excelWriter.close();
        }
        REALTIME_BIG_ORDER_MAP.putIfAbsent(bigOrderMapKey, bigOrders);
    }

    private void exportSnapshotBigOrderData2Excel(BaseDeptDTO deptData, long threshold) {
        String exchangeName = deptData.getExchange();
        String symbol = deptData.getSymbol();

        Set<OrderDTO> bigOrders = new HashSet<>();
        addNewBigOrder(deptData, bigOrders, threshold);
        Set<ExcelSnapshotOrderBO> exportBigOrders = convert2ExcelSnapshotOrder(deptData, bigOrders);
        if (bigOrders.size() > 0) {
            String title = exchangeName + " " + symbol;
            log.info(title + "盘口存在大单:{}", exportBigOrders);
            String dateSuffix = LocalDate.now().toString();
            String filePath = System.getProperty("user.dir") + File.separator + "data" + File.separator + "snapshot" + File.separator + exchangeName + "_" + symbol + "_" + dateSuffix + ".xlsx";

            ExcelWriter excelWriter = ExcelUtil.getWriter(filePath);
            if (FileUtil.exist(filePath)){
                excelWriter.setCurrentRowToEnd();
                excelWriter.write(exportBigOrders, false);
            } else {
                excelWriter.merge(3, title);
                excelWriter.addHeaderAlias("position", "方向");
                excelWriter.addHeaderAlias("price", "价格");
                excelWriter.addHeaderAlias("amount", "数量");
                excelWriter.addHeaderAlias("startTime", "快照时间");
                excelWriter.write(exportBigOrders, true);
            }
            excelWriter.close();
        }
    }

    private void addNewBigOrder(BaseDeptDTO deptData, Set<OrderDTO> bigOrders, long threshold) {
        List<List<String>> bids = deptData.getBids();
        List<List<String>> asks = deptData.getAsks();

        for (List<String> bid : bids) {
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble(bid.get(0)));
            BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(bid.get(1)));
            long startTime = deptData.getTimestamp().getTime();

            if (amount.compareTo(BigDecimal.valueOf(threshold)) > -1) {
                bigOrders.add(OrderDTO.builder().position(1).price(price).amount(amount).startTime(startTime).build());
            }
        }

        for (List<String> ask : asks) {
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble(ask.get(0)));
            BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(ask.get(1)));
            long startTime = deptData.getTimestamp().getTime();

            if (amount.compareTo(BigDecimal.valueOf(threshold)) > -1) {
                OrderDTO bigOrder = OrderDTO.builder().position(-1).price(price).amount(amount).startTime(startTime).build();
                bigOrders.add(bigOrder);
            }
        }
    }

    private Set<ExcelRealtimeOrderBO> convert2ExcelRealtimeOrder(BaseDeptDTO deptData, Set<OrderDTO> bigOrders) {
        Set<ExcelRealtimeOrderBO> excelOrders = new HashSet<>();
        removeExpireBigOrder(deptData, bigOrders, excelOrders);
        return excelOrders;
    }

    private Set<ExcelSnapshotOrderBO> convert2ExcelSnapshotOrder(BaseDeptDTO deptData, Set<OrderDTO> bigOrders) {
        Set<ExcelSnapshotOrderBO> excelOrders = new HashSet<>();
        for (OrderDTO bigOrder : bigOrders) {
            ExcelSnapshotOrderBO excelOrder = ExcelSnapshotOrderBO.builder()
                    .position(bigOrder.getPosition() > 0 ? "买入" : "卖出")
                    .price(bigOrder.getPrice())
                    .amount(bigOrder.getAmount())
                    .startTime(DateTime.of(bigOrder.getStartTime()).toString())
                    .build();
            excelOrders.add(excelOrder);
        }
        return excelOrders;
    }

    private Set<OrderDTO> removeExpireBigOrder(BaseDeptDTO deptData, Set<OrderDTO> bigOrders, Set<ExcelRealtimeOrderBO> excelOrders) {
        Set<OrderDTO> expireBigOrders = new HashSet<>();
        Set<OrderDTO> orderList = new HashSet<>();
        List<List<String>> bids = deptData.getBids();
        List<List<String>> asks = deptData.getAsks();

        for (List<String> bid : bids) {
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble(bid.get(0)));
            BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(bid.get(1)));
            long startTime = deptData.getTimestamp().getTime();

            OrderDTO order = OrderDTO.builder().position(1).price(price).amount(amount).startTime(startTime).build();
            orderList.add(order);        }

        for (List<String> ask : asks) {
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble(ask.get(0)));
            BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(ask.get(1)));
            long startTime = deptData.getTimestamp().getTime();

            OrderDTO order = OrderDTO.builder().position(-1).price(price).amount(amount).startTime(startTime).build();
            orderList.add(order);
        }

        for (OrderDTO bigOrder : bigOrders) {
            if (!orderList.contains(bigOrder)) {
                bigOrder.setDuration(System.currentTimeMillis() - bigOrder.getStartTime());
                expireBigOrders.add(bigOrder);
                if (excelOrders != null) {
                    ExcelRealtimeOrderBO excelOrder = ExcelRealtimeOrderBO.builder()
                            .position(bigOrder.getPosition() > 0 ? "买入" : "卖出")
                            .price(bigOrder.getPrice())
                            .amount(bigOrder.getAmount())
                            .startTime(DateTime.of(bigOrder.getStartTime()).toString())
                            .duration(String.valueOf(NumberUtil.div(bigOrder.getDuration(), 1000)))
                            .build();
                    excelOrders.add(excelOrder);
                }
            }
        }

        bigOrders.removeAll(expireBigOrders);
        return expireBigOrders;
    }
}
