package main.java.service;

import com.ruoyi.system.domain.bug.BugProcess;

import java.util.List;

/**
 * 缺陷bug单流程业务接口
 */
public interface BugProcessService {
    /**
     * 查询缺陷bug单流程列表
     * @param id
     * @return
     */
    List<BugProcess> queryBugProcessList(String id);

    /**
     * 保存缺陷bug单流程列表
     * @param bugProcess
     * @return
     */
    Integer saveBugProcessList(BugProcess bugProcess) throws Exception;
}
