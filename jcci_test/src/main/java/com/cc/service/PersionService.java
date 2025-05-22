package com.cc.service;

public interface PersionService {

    /**
     * 删除文件
     * @param id
     */
    void deleteFile(Long id);

    /**
     * 缺陷bug单修改方法
     * @param id
     * @return
     */
    int updateBugTracker(Long id);

}
