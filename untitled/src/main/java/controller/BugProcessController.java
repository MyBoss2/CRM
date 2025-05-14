package main.java.controller;

import com.ruoyi.system.common.exception.BusinessException;
import com.ruoyi.system.common.result.ControllerResponse;
import com.ruoyi.system.domain.bug.BugProcess;
import com.ruoyi.system.domain.bug.BugTracker;
import com.ruoyi.system.service.bug.BugProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author lsw
 * @Date 2024/12/12 14:40
 * @PackageName: com.ai.req.gsmb.rest.bug
 * @ClassName: BugProcessService
 * @Description: 缺陷bug单流程管理
 * @Version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/gsmb/bugProcess")
public class BugProcessController {
    @Resource
    private BugProcessService bugProcessService;

    /**
     * 查询bug单流程
     *
     * @param bugTracker
     * @return
     */
    @PostMapping("/queryBugProcessList")
    public ControllerResponse queryList(@RequestBody BugTracker bugTracker) {
        log.info("查询bug单流程-BugProcessController.queryList()请求参数：{}", bugTracker.toString());
        List<BugProcess> list = bugProcessService.queryBugProcessList(bugTracker.getId());
        log.info("查询bug单流程-BugProcessController.queryList()返回结果：{}", list);
        return ControllerResponse.success(list);
    }

    /**
     * 保存bug单流程
     *
     * @param bugProcess
     * @return
     */
    @PostMapping("/saveBugProcessList")
    public ControllerResponse saveBugProcessList(@RequestBody BugProcess bugProcess) {
        log.info("保存bug单流程-BugProcessController.saveBugProcessList()接收参数：{}", bugProcess);
        int row = 0;
        try {
            row = bugProcessService.saveBugProcessList(bugProcess);
        } catch (Exception e) {
            throw new BusinessException(e.toString());
        }
        if(row > 0){
            return ControllerResponse.success();
        }else {
            return ControllerResponse.fail();
        }

    }
}
