package main.java.service;

import com.github.pagehelper.PageInfo;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.domain.aireview.dto.PublicTrackAttachment;
import com.ruoyi.system.domain.bug.BugTracker;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 缺陷bug单业务层
 */
public interface BugTrackerService {

    /**
     * bug单联合查询
     * @param bugTracker
     * @return
     */
    PageInfo<BugTracker> queryList(BugTracker bugTracker, SysUser sysUser,List<Long> userIdList);

    /**
     * 根据缺陷bug单id查询bug单信息
     * @param id
     * @return
     */
    BugTracker selectByPrimaryKey(String id);

    /**
     * 缺陷bug单插入方法
     * @param bugTracker
     * @return
     */
    BugTracker createBugTracker(BugTracker bugTracker);

    /**
     * 缺陷bug单修改方法
     * @param bugTracker
     * @return
     */
    int updateBugTracker(BugTracker bugTracker);

    /**
     * 导出bug单
     * @param bugTracker
     * @param response
     */
    void export(BugTracker bugTracker, HttpServletResponse response,SysUser sysUser) throws Exception;

    /**
     * @param file
     * @param bugId
     * @Author maxp
     * @Date 2024/12/12
     * @Description 评审附件信息上传及保存
     * @Return int
     */
    PublicTrackAttachment uploadAttachment(MultipartFile file, String bugId,String userId) throws Exception;

    /**
     * 功能描述: 文件下载
     * @author liangsw
     * @date 2024/12/12
     * @param
     * @param request
     * @param response
     * @return com.ai.req.agile.file.domain.FileInfo
     */
    void download(String fileName, String remoteName, HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
     * 功能描述: 删除附件
     * @author liangsw
     * @date 2024/12/12
     * @param id
     */
    void deleteFile(Long id);

    /**
     * 删除缺陷BUG（真删）
     * @param id
     */
    void realDelReviewByReviewId(String id);

    /**
     * 根据任务id查询版本冲突集合
     * @param bugId
     * @return
     */
    List<BugTracker> getDetailByRegistId(String bugId);

    /**
     * 根据用户id查询团队里面所有的用户id集合
     * @param currentUserId
     * @return
     */
    List<Long> queryTeamIdByUserId(Long currentUserId);

}
