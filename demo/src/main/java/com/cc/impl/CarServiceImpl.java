package com.cc.impl;

import com.cc.dao.Car;
import com.cc.mapper.CarMapper;
import com.cc.service.CarService;
import org.springframework.stereotype.Service;

@Service
public class CarServiceImpl implements CarService {
    private CarMapper carMapper;

    @Override
    public void updateCaR(Car car) {
        System.out.println("测试修改");
        carMapper.deleteByPrimaryKey(car.getId());
    }

    @Override
    public void insertCar(Car car) {

        System.out.println("测试新增");
        System.out.println("测试提交");


    }
}
