package main.java.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.common.util.DateUtil;
import com.ruoyi.system.domain.aireview.dto.*;
import com.ruoyi.system.domain.bug.BugProcess;
import com.ruoyi.system.domain.bug.BugTracker;
import com.ruoyi.system.domain.export.ExcelSheetBugTracker;
import com.ruoyi.system.domain.file.FileInfo;
import com.ruoyi.system.domain.file.FileUploadInfo;
import com.ruoyi.system.domain.regist.RegistEntity;
import com.ruoyi.system.mapper.*;
import com.ruoyi.system.service.bug.BugTrackerService;
import com.ruoyi.system.service.file.FileAttachmentService;
import com.ruoyi.system.utils.CommonUtils;
import com.ruoyi.system.utils.ExcelUtils;
import com.ruoyi.system.utils.SubListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * bug单管理实现层
 * @Date 20241209
 */
@Service
public class BugTrackerServiceImpl implements BugTrackerService {

    //日志
    private static final Logger log = LoggerFactory.getLogger(BugTrackerServiceImpl.class);

    //当前页
    public static final Integer CURPAGE = 1;

    //当前也最大显示数量
    public static final Integer PAGESIZE = 10;

    //附件类型（2为缺陷bug单）
    private static final Integer ATTACHEMENT_TYPE = 2;

    //新版-版本冲突
    private static final String REGIST_BUG_TYPE = "17";

    @Resource
    private FileAttachmentService fileAttachmentService;

    @Resource
    private BugTrackerMapper bugTrackerMapper;

    @Resource
    private ReqPublicTrackAttachmentMapper reqPublicTrackAttachmentMapper;

    @Resource
    private BugProcessMapper bugProcessMapper;

    @Resource
    private RegistMapper registMapper;

    //待处理
    private static final String REGIST_PENDING  = "2";

    //待审核
    private static final String REGIST_STATUS = "0";

    /**
     * 查询缺陷bug单
     * @param bugTracker
     * @return
     */
    @Override
    public PageInfo<BugTracker> queryList(BugTracker bugTracker, SysUser sysUser,List<Long> userIdList) {
        if (null == bugTracker.getCurPage()) {
            bugTracker.setCurPage(CURPAGE);
        }
        if (null == bugTracker.getPageSize()) {
            bugTracker.setPageSize(PAGESIZE);
        }
        PageHelper.startPage(bugTracker.getCurPage(), bugTracker.getPageSize());

        List<BugTracker> trackInfoList = bugTrackerMapper.queryList(bugTracker,userIdList);
        int handlerCount=0;//缺陷修复次数
        if(!trackInfoList.isEmpty()){
            List<BugProcess> bugProcesses = new ArrayList<>();
            Date processCreateTime = new Date();
            Date processHandlerTime = new Date();
            Date processTestTime = new Date();
            for (BugTracker bug : trackInfoList) {
                //查询附件信息
                List<PublicTrackAttachment> publicAttachment = reqPublicTrackAttachmentMapper.queryAttachmentByReviewId(bug.getId());
                if(!publicAttachment.isEmpty()){
                    List<FileUploadInfo> fileUploadInfoList = new ArrayList<>();
                    for (PublicTrackAttachment attachment : publicAttachment) {
                        fileUploadInfoList.add(new FileUploadInfo(attachment.getFileName(),attachment.getFileUri(),attachment.getId()));
                    }
                    bug.setFileList(fileUploadInfoList);
                }
                //查询缺陷修复次数,缺陷修复时间,缺陷验证时间
                bugProcesses = bugProcessMapper.selectBugProcessList(bug.getId());
                bugProcesses.sort((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()));//把时间最大的放在最前面
              //  log.info("queryList,按照时间倒序后的bugProcesses：{}",bugProcesses);
                for (BugProcess bugProcess : bugProcesses) {
                    if("提出待处理".equals(bugProcess.getNodeName())){
                        processCreateTime = bugProcess.getCreateTime();
                    }
                }

                bugProcesses.sort((o1, o2) -> o1.getCreateTime().compareTo(o2.getCreateTime()));//把时间最小的放在最前面
               // log.info("queryList,按照时间正序后的bugProcesses：{}",bugProcesses);
                handlerCount=0;
                for (BugProcess bugProcess : bugProcesses) {
                    if("缺陷修复".equals(bugProcess.getNodeName())){
                        handlerCount++;
                        processHandlerTime = bugProcess.getCreateTime();
                    }
                    if("缺陷已验证".equals(bugProcess.getNodeName())){
                        processTestTime = bugProcess.getCreateTime();
                    }
                }

                //处理时间
                double handlerTime = processHandlerTime.getTime() - processCreateTime.getTime();
                double handlerTimeDouble = handlerTime/1000/60/60;
                String time = String.format("%.2f", handlerTimeDouble); //结果为处理的小时数
                if(handlerCount==0){
                    bug.setBugTestTime("0");//结果为验证的小时数
                }else {
                    //验证时间
                    double testTime = processTestTime.getTime() - processHandlerTime.getTime();
                    double testTimeDouble = testTime / 1000 / 60 / 60;
                    bug.setBugTestTime(String.format("%.2f", testTimeDouble));//结果为验证的小时数
                }
                bug.setBugHandlerCount(handlerCount);
                bug.setBugHandlerTime(time);
            }
        }
        PageInfo<BugTracker> pageInfo = new PageInfo<>(trackInfoList);
        return pageInfo;
    }


    /**
     * 通过缺陷bug单id查询bug单信息
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BugTracker selectByPrimaryKey(String id) {
        return bugTrackerMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增缺陷bug单
     * @param bugTracker
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BugTracker createBugTracker(BugTracker bugTracker) {
        BugTracker tracker = null;
        String uuid = UUID.randomUUID().toString().replace("-","");
        bugTracker.setId(uuid);
        //生成代办信息
        RegistEntity registEntity = new RegistEntity();
        registEntity.setUid(UUID.randomUUID().toString().replace("-", ""));
        registEntity.setTaskType(REGIST_BUG_TYPE);//缺陷bug单
        try {
            registEntity.setReqId(ExcelUtils.randomLong(10));
        } catch (Exception e) {
            log.error("设置reqId发生错误", e.getMessage());
        }
        registEntity.setStatus(REGIST_STATUS);
        registEntity.setApprovalOpId(bugTracker.getTaskPersonId());
        registEntity.setCreateOpId(String.valueOf(bugTracker.getCreaterId()));
        registEntity.setCreateOpName(bugTracker.getCreater());
        registEntity.setReqName(bugTracker.getBugName() + "_缺陷BUG单");
        registEntity.setBugId(bugTracker.getId());
        registMapper.insertRegist(registEntity);
        // bugTracker.setRegistId(registEntity.getId());
        bugTracker.setBugNode("提出待处理");
        int id = bugTrackerMapper.createBugTracker(bugTracker);
        if(id>0){
            tracker = bugTrackerMapper.selectByPrimaryKey(uuid);
        }
        return tracker;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateBugTracker(BugTracker bugTracker) {
        return  bugTrackerMapper.updateBugTracker(bugTracker);
    }

    /**
     * 导出
     * @param bugTracker
     * @param response
     */
    public void export(BugTracker bugTracker, HttpServletResponse response,SysUser sysUser) throws Exception {
        List<BugTracker> bugTrackerLsit = new ArrayList<>();
        String currentUserId = bugTracker.getCurrentUserId();
        List<Long> userIdList = new ArrayList<>();
        userIdList.add(Long.parseLong(currentUserId));
        //如果当前登陆人员是组长
        if(sysUser.getRoleId()==101){
            userIdList = bugTrackerMapper.queryTeamIdByUserId(Long.parseLong(currentUserId));
            if(!userIdList.contains(Long.parseLong(currentUserId))){
                userIdList.add(Long.parseLong(currentUserId));
            }
        }else if(sysUser.getRoleId()==102 || sysUser.getRoleId()==103 || sysUser.getRoleId()==1){ //如果当前登陆人员是大组长、超级管理员或者是局方时，查看全部
            userIdList = null;
        }
        List<BugTracker> bugTrackers = bugTrackerMapper.queryList(bugTracker,null);
        bugTrackerLsit.addAll(bugTrackers);
       /* if (bugTrackers != null && bugTrackers.size() > 0) {
            for (BugTracker bugT : bugTrackers) {
                List<BugTracker> childList = bugTrackerMapper.getChildrenById(bugT.getId());
                if (childList != null && childList.size() > 0) {
                    bugTrackerLsit.addAll(childList);
                }
            }
        }*/
        Workbook workbook = new HSSFWorkbook();
        List<ExcelSheetBugTracker> excelSheets = new ArrayList<>();
        String[] heads = {"序号", "bug单ID","bug名称", "缺陷描述", "严重程度", "缺陷来源", "缺陷类型", "缺陷测试环境", "缺陷触发类型","期望发布时间","创建时间","完成时间","缺陷状态"};
        if (bugTrackerLsit.size() <= 300) {
            ExcelSheetBugTracker excelSheet = ExcelSheetBugTracker.class.newInstance();
            excelSheet.setHeads(heads);
            excelSheet.setDataList(bugTrackerLsit);
            excelSheet.setSheetName("sheet1");
            excelSheets.add(excelSheet);
            workbook = exportManySheetExcel(excelSheets);
        } else {
            List<List<BugTracker>> subAllLists = SubListUtils.partition(bugTrackerLsit, 300);
            if (subAllLists != null && subAllLists.size() > 0) {
                for (int i = 0; i < subAllLists.size(); i++) {
                    ExcelSheetBugTracker excelSheet = ExcelSheetBugTracker.class.newInstance();
                    excelSheet.setDataList(subAllLists.get(i));
                    excelSheet.setSheetName("sheet" + (i + 1));
                    excelSheet.setHeads(heads);
                    excelSheets.add(excelSheet);
                }
            }
            workbook = exportManySheetExcel(excelSheets);
        }
        // 输出Excel文件
        String fileName = "bugTracker" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xls";
        OutputStream outputStream = response.getOutputStream();
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        response.setContentType("application/octet-stream;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        workbook.write(outputStream);
        outputStream.flush();
        outputStream.close();
    }

    private HSSFWorkbook exportManySheetExcel(List<ExcelSheetBugTracker> mysheets) {
        //创建工作薄
        HSSFWorkbook wb = new HSSFWorkbook();
        //表头样式
        HSSFCellStyle style = wb.createCellStyle();
        // 垂直
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        // 水平
        style.setAlignment(HorizontalAlignment.CENTER);
        //字体样式
        HSSFFont fontStyle = wb.createFont();
        fontStyle.setFontName("微软雅黑");
        fontStyle.setFontHeightInPoints((short) 12);
        style.setFont(fontStyle);
        for (ExcelSheetBugTracker excel : mysheets) {
            //新建一个sheet
            //获取该sheet名称
            HSSFSheet sheet = wb.createSheet(excel.getSheetName());
            //获取sheet的标题名
            String[] heads = excel.getHeads();
            //第一个sheet的第一行为标题
            HSSFRow rowFirst = sheet.createRow(0);
            //写标题
            for (int i = 0; i < heads.length; i++) {
                //获取第一行的每个单元格
                HSSFCell cell = rowFirst.createCell(i);
                //往单元格里写数据
                cell.setCellValue(heads[i]);
                //加样式
                cell.setCellStyle(style);
                //设置每列的列宽
                sheet.setColumnWidth(i, 4000);
            }
            //写数据集
            List<BugTracker> dataList = excel.getDataList();
            // 填充数据行
            int rowNum = 1;
            for (BugTracker item : dataList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(item.getId());
                row.createCell(2).setCellValue(item.getBugName());
                row.createCell(3).setCellValue(item.getDescription());
                //row.createCell(4).setCellValue(item.getDetail());
                row.createCell(4).setCellValue(item.getSeverity());
                row.createCell(5).setCellValue(item.getBugSource());
                row.createCell(6).setCellValue(item.getBugType());
                row.createCell(7).setCellValue(item.getBugTest());
                row.createCell(8).setCellValue(item.getTriggerType());
                row.createCell(9).setCellValue(DateUtil.formatDateToStr2(item.getReleaseDate()));
                row.createCell(10).setCellValue(DateUtil.formatDateToStr2(item.getCreateTime()));
                row.createCell(11).setCellValue(DateUtil.formatDateToStr2(item.getFinishTime()));
                row.createCell(12).setCellValue(item.getBugNode());
                // String[] heads = {"序号", "bug单ID","bug名称", "缺陷描述", "详细信息", "严重程度", "缺陷来源",
                // "缺陷类型", "缺陷测试环境", "期望发布时间","创建时间","完成时间"};
            }
        }
        return wb;
    }

    /**
     * 上传附件
     * @param file
     * @param bugId
     * @return
     * @author liangsw
     * @date 2024/12/12
     */
    @Override
    public PublicTrackAttachment uploadAttachment(MultipartFile file, String bugId,String userId) throws Exception {
        PublicTrackAttachment publicAttachment = new PublicTrackAttachment();
        //判断上传附件名在表中是否存在如果存在则直接返回
        if(StringUtils.isNotBlank(file.getOriginalFilename())){
            PublicTrackAttachment publicAtt =  reqPublicTrackAttachmentMapper.findAttachmentByfileName(file.getOriginalFilename());
            if(null == publicAtt){
                try {
                    FileInfo fileInfo = fileAttachmentService.upload(file);
                    publicAttachment.setPrimaryId(bugId);
                    publicAttachment.setType(ATTACHEMENT_TYPE);
                    publicAttachment.setFileName(fileInfo.getFileName());
                    publicAttachment.setFileUri(fileInfo.getFileUri());
                    publicAttachment.setFileSize((int) fileInfo.getSize());
                    publicAttachment.setUploadTime(new Date());
                   // Long loginUserId = UserThreadLocalUtil.getUserInfo().getUserId();
                    publicAttachment.setUploadUid(Long.parseLong(userId));
                    reqPublicTrackAttachmentMapper.insertPublicAttachment(publicAttachment);
                } catch (Exception e) {
                    log.error("文件上传失败：{}", e);
                    throw new Exception("文件上传失败！");
                }
            }else{
                log.error("文件已存在：请核查！");
                throw new Exception("文件已存在，请核查！");
            }
        }
        return publicAttachment;
    }

    /**
     * 功能描述: 文件下载
     *
     * @param
     * @param request
     * @param response
     * @return com.ai.req.agile.file.domain.FileInfo
     * @author liangsw
     * @date 2024/12/12
     */
    @Override
    public void download(String fileName, String remoteName, HttpServletRequest request, HttpServletResponse response) throws Exception {
        fileAttachmentService.download(fileName,remoteName, request, response);
    }

    /**
     * 删除附件
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long id) {
        reqPublicTrackAttachmentMapper.deleteByreviewId(id);
    }

    /**
     * 通过评审id查询对应的附件信息
     * @param id
     */
    @Override
    public void realDelReviewByReviewId(String id) {
        //删除缺陷bug单表 req.bug_tracker数据
        bugTrackerMapper.deleteByPrimaryKey(id);
        //删除缺陷bug单流程表req.bug_process数据
        bugProcessMapper.deleteBugProcessListById(id);
        //删除关联附件以及存放在sftp上的文件  req.req_public_attachment
        List<PublicTrackAttachment> attachmentList = reqPublicTrackAttachmentMapper.queryAttachmentByReviewId(id);
        if (CollectionUtils.isNotEmpty(attachmentList)) {
            for (PublicTrackAttachment attachment : attachmentList) {
                String fileUrl = attachment.getFileUri();
                if (StringUtils.isNotBlank(fileUrl)) {
                    //这个如果有异常记录下来，不阻塞进程
                    try {
                        //截取远程主机文件名
                        Map<String, String> urlMap = CommonUtils.getUrlParam(fileUrl);
                        String remoteFileName = MapUtils.getString(urlMap, "remoteName", "");
                        if (StringUtils.isBlank(remoteFileName)) {
                            if (log.isErrorEnabled()) {
                                log.error("删除sftp主机文件失败，没有远程文件名，文件信息：" + attachment);
                            }
                        } else {
                            fileAttachmentService.deleteAttachmentFile(remoteFileName);
                        }
                    } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                            log.error("删除sftp主机文件异常，文件信息：" + attachment, e);
                        }
                    }
                } else {
                    if (log.isErrorEnabled()) {
                        log.error("删除sftp主机文件失败，没有文件路径信息，文件信息：" + attachment);
                    }
                }
            }
            //相关表记录全部删除
            // reqPublicAttachmentMapper.deleteByreviewId(id);
        }
    }

    /**
     * 根据任务id查询版本冲突集合
     * @param bugId
     * @return
     */
    @Override
    public List<BugTracker> getDetailByRegistId(String bugId) {
        List<BugTracker> trackInfoList  = bugTrackerMapper.queryByRegistId(bugId);
        if(!trackInfoList.isEmpty()){
            for (BugTracker bug : trackInfoList) {
                //查询附件信息
                List<PublicTrackAttachment> publicAttachment = reqPublicTrackAttachmentMapper.queryAttachmentByReviewId(bug.getId());
                List<FileUploadInfo> fileUploadInfoList = new ArrayList<>();
                for (PublicTrackAttachment attachment : publicAttachment) {
                    if (null != attachment) {
                        fileUploadInfoList.add(new FileUploadInfo(attachment.getFileName(), attachment.getFileUri(), attachment.getId()));
                    }
                }
                bug.setFileList(fileUploadInfoList);
            }
        }
        return trackInfoList;
    }

    /**
     * 根据缺陷bug单id查询bug单信息
     * @param currentUserId
     * @return
     */
    @Override
    public List<Long> queryTeamIdByUserId(Long currentUserId) {
        return bugTrackerMapper.queryTeamIdByUserId(currentUserId);
    }

    public void test(){
        System.out.println("测试版本");
    }

}
