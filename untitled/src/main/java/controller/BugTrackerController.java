package main.java.controller;

import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.system.common.exception.BusinessException;
import com.ruoyi.system.common.result.ControllerResponse;
import com.ruoyi.system.domain.aireview.dto.PublicTrackAttachment;
import com.ruoyi.system.domain.bug.BugTracker;
import com.ruoyi.system.domain.file.FileUploadInfo;
import com.ruoyi.system.domain.file.ReviewAttachment;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.bug.BugTrackerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @Author liangsw
 * @Date 2024/12/9 14:40
 * @PackageName:com.ai.req.gsmb.rest.batch
 * @ClassName: BugTrackerController
 * @Description: 缺陷bug单管理
 * @Version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/gsmb/bug")
public class BugTrackerController {

    @Resource
    private BugTrackerService bugTrackerService;
    @Resource
    private ISysUserService userService;


    /**
     * bug单-列表页面分页数据接口
     * @Author liangsw
     * @param bugTracker
     * @return
     */
    @PostMapping("/queryList")
    public ControllerResponse queryList(@RequestBody BugTracker bugTracker) {
        log.error("查询bug单-BugTrackerController.queryList()请求参数：{}", bugTracker.toString());
        SysUser sysUser =  userService.selectUserById(Long.parseLong(bugTracker.getCurrentUserId()));
        String currentUserId = bugTracker.getCurrentUserId();
        List<Long> userIdList = new ArrayList<>();
        userIdList.add(Long.parseLong(currentUserId));
        //如果当前登陆人员是组长
        if(sysUser.getRoleId()==101){
            userIdList = bugTrackerService.queryTeamIdByUserId(Long.parseLong(currentUserId));
            if(!userIdList.contains(Long.parseLong(currentUserId))){
                userIdList.add(Long.parseLong(currentUserId));
            }
        }else if(sysUser.getRoleId()==102 || sysUser.getRoleId()==103 || sysUser.getRoleId()==1){ //如果当前登陆人员是大组长、超级管理员或者是局方时，查看全部
            userIdList = null;
        }
        return ControllerResponse.success(bugTrackerService.queryList(bugTracker,sysUser,userIdList));
    }

    /**
     * 新增缺陷bug单
     * @Author liangsw
     * @param bugTracker
     * @return
     */
    @PostMapping("/createBugTracker")
    public ControllerResponse createBugTracker(@RequestBody BugTracker bugTracker) {
        try{
            BugTracker tracker =  bugTrackerService.createBugTracker(bugTracker);
            return ControllerResponse.success(tracker);
        }catch (Exception e){
            log.error("新增缺陷bug单保存失败！"+e);
            return ControllerResponse.fail("新增缺陷bug单保存失败！");
        }
    }

    /**
     * 新增缺陷bug单
     * @Author liangsw
     * @param bugTracker
     * @return
     */
    @PostMapping("/updateBugTracker")
    public ControllerResponse updateBugTracker(@RequestBody BugTracker bugTracker) {
        int row =  bugTrackerService.updateBugTracker(bugTracker);
        if (row > 0) {
            return ControllerResponse.success();
        } else {
            return ControllerResponse.fail();
        }
    }

    /**
     * bug单-导出功能接口
     * @param
     * @return
     */
    @PostMapping("/export")
    public ControllerResponse export(@RequestBody BugTracker bugTracker, HttpServletResponse response) {
        log.error("导出bug单-BugTrackerController.export()请求参数：{}", bugTracker.toString());
        try {
            SysUser sysUser =  userService.selectUserById(Long.parseLong(bugTracker.getCurrentUserId()));
            bugTrackerService.export(bugTracker,response,sysUser);
            return ControllerResponse.success();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ControllerResponse.fail(e.getMessage());
        }
    }

    /**
     * 缺陷bug单-导出功能接口
     * @param
     * @return
     */
    @PostMapping("/download")
    public ControllerResponse download(@RequestParam(name="fileName") String fileName,
                                       @RequestParam(name="remoteName") String remoteName,
                                       HttpServletRequest request, HttpServletResponse response) {
        log.error("导出评审附件ReqIssueReviewInfoController.export()请求参数：{}", fileName.toString());
        try {
            bugTrackerService.download(fileName,remoteName, request, response);
            return ControllerResponse.success();
        } catch (Exception e) {
            return ControllerResponse.fail(e.getMessage());
        }
    }

    /**
     * 删除附件
     * @param fileUploadInfo
     * @return
     */
    @PostMapping("/deleteConfigAttachment")
    public ControllerResponse deleteAttachment(@RequestBody FileUploadInfo fileUploadInfo) {
        try{
            bugTrackerService.deleteFile(fileUploadInfo.getId());
            return ControllerResponse.success("附件删除成功! ");
        }catch (Exception e){
            log.error("附件删除失败！"+e);
            return ControllerResponse.fail("附件删除失败！");
        }
    }

    /**
     * 根据reviewIdbug单子
     * @param reviewId
     * @return
     */
    @GetMapping("/deleteBud")
    public ControllerResponse deleteReview(@RequestParam(value = "reviewId", required = true) String reviewId){
        try {
            bugTrackerService.realDelReviewByReviewId(reviewId);
            return ControllerResponse.success("删除成功");
        }catch (Exception e){
            log.error("删除bug单失败！"+e);
            return ControllerResponse.fail("删除bug单失败！");
        }
    }

    /**
     * 版本冲突-根据registId获取详情信息接口
     * @param bugId
     * @return
     */
    @GetMapping("/getBugDetailByRegistId")
    public ControllerResponse getBugDetailByRegistId(@RequestParam(value = "bugId") String bugId){
        log.error("BugTrackerController.getBugDetailByRegistId()请求参数：{}", bugId);
        return ControllerResponse.success(bugTrackerService.getDetailByRegistId(bugId));
    }


    /**
     * @param file
     * @param bugId
     * @Author liangsw
     * @Date 2024/12/12
     * @Description 缺陷bug单上传附件
     * @Return com.ai.req.common.result.ControllerResponse
     */
    @PostMapping("/uploadAttachment/{bugId}")
    public ControllerResponse uploadAttachment(@RequestParam("file") MultipartFile file,
                                               @RequestParam("userId") String userId,
                                               @PathVariable String bugId) {
        PublicTrackAttachment publicAttachment = null;
        try {
            publicAttachment = bugTrackerService.uploadAttachment(file, bugId,userId);
        } catch (Exception e) {
            log.error("上传附件excel失败：{}", e);
            return ControllerResponse.fail("上传附件excel失败！"+e.getMessage());
        }
       //ReviewAttachment result = ReflectUtil.copyProperties(publicAttachment,ReviewAttachment.class);
        ReviewAttachment result = new ReviewAttachment();
        BeanUtils.copyProperties(publicAttachment, result);

        return ControllerResponse.success(result);
    }

    /**
     * bug单上传图片处理
     * @param file
     * @return
     */
    @PostMapping("/uploadImage")
    @PreAuthorize("@ss.hasPermi('gsmb:bug:uploadImage')")
    public ControllerResponse uploadAttachment(@RequestParam("file") MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 读取文件内容并转换为字节数组
        byte[] fileContent = null;
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")+1);
        try {
            fileContent = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(e.getMessage());
        }

        // 使用Base64编码器进行编码
        String base64EncodedString = Base64.getEncoder().encodeToString(fileContent);

        Map<String,String> fileMap= new HashMap();
        fileMap.put("file","data:image/"+fileExtension+";base64,"+base64EncodedString);
        Map<String,Map> filesMap= new HashMap();
        filesMap.put("files",fileMap);
        Object json = JSONObject.toJSON(filesMap);
        return ControllerResponse.success(json);
    }
}

