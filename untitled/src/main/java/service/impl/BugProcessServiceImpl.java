package main.java.service.impl;

import com.ruoyi.system.domain.bug.BugProcess;
import com.ruoyi.system.mapper.BugProcessMapper;
import com.ruoyi.system.service.bug.BugProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

@Service
public class BugProcessServiceImpl implements BugProcessService {
    private static final Logger log = LoggerFactory.getLogger(BugProcessServiceImpl.class);
    @Resource
    private BugProcessMapper bugProcessMapper;
    /**
     * 查询bug流程节点列表
     * @param id
     * @return List<BugProcess> 该bug单的所有流程节点
     */
    @Override
    public List<BugProcess> queryBugProcessList(String id) {
        return bugProcessMapper.selectBugProcessList(id);
    }

    /**
     * 新增bub流程节点
     * @param bugProcess
     * @return
     */
    @Override
    public Integer saveBugProcessList(BugProcess bugProcess) throws Exception{
        log.info("新增bug流程节点-BugProcessServiceImpl.saveBugProcessList()接收参数：{}",bugProcess);
        if (bugProcess == null || "".equals(bugProcess.getBugId()) ) {
            return 0;
        }

       /* List<BugProcess> newBugProcessList = new ArrayList<>();
        for (int i = 0; i < bugProcessList.size(); i++) {
            BugProcess item = bugProcessList.get(i);
            if (StringUtils.isEmpty(item.getId())) {
                newBugProcessList.add(item);
            }
        }*/
        SimpleDateFormat simpleFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        simpleFormat.setTimeZone(TimeZone.getTimeZone("CST")); // 设置时区

        /*bugProcess.setPlanStartTime(simpleFormat.parse(bugProcess.getPlanStartTime().toString()));
        bugProcess.setPlanEndTime(simpleFormat.parse(bugProcess.getPlanEndTime().toString()));
        bugProcess.setActualStartTime(simpleFormat.parse(bugProcess.getActualStartTime().toString()));
        bugProcess.setActualEndTime(simpleFormat.parse(bugProcess.getActualEndTime().toString()));
        bugProcess.setActualSubmitTime(simpleFormat.parse(bugProcess.getActualSubmitTime().toString()));*/

        log.info("新增的bug流程节点准备要保存的数据：{}", bugProcess);
        return bugProcessMapper.insertBugProcess(bugProcess);
    }

}
