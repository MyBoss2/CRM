package com.cc.controller;


import com.cc.service.PersionService;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 控制层
 */
@RestController
public class PersonController {

    @Resource
    private PersionService persionService;

    public void delete(Long id){
        persionService.deleteFile(id);
    }

    public void update(Long id){
        persionService.updateBugTracker(id);
    }
}
