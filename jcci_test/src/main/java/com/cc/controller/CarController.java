package com.cc.controller;

import com.cc.dao.Car;
import com.cc.service.CarService;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class CarController {

    @Resource
    private CarService carService;

    public void add() {
        carService.insertCar(new Car());
    }

    public void Update(){
        carService.updateCaR(new Car());
    }
}
