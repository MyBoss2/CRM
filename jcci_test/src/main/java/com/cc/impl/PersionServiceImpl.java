package com.cc.impl;

import com.cc.dao.Persion;
import com.cc.service.PersionService;
import org.springframework.stereotype.Service;


@Service
public class PersionServiceImpl implements PersionService {
    @Override
    public void deleteFile(Long id) {
        System.out.println("测试删除");

    }

    @Override
    public int updateBugTracker(Long id) {
        Persion persion = new Persion();
        persion.setId(id);
        persion.setAge("25");
        System.out.println("测试修改");
        return 0;
    }
}
