package pres.lxx.monitor.deptmonitor.service;

import pres.lxx.monitor.deptmonitor.bo.dept.BaseExchangeDeptBO;

public interface DeptDataService {

    void invokeRealtimeDeptData(BaseExchangeDeptBO exchangeDept);

    void invokeSnapshotDeptData(BaseExchangeDeptBO exchangeDept);

}
