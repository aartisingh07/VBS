package com.vbs.demo.controller;

import com.vbs.demo.repositories.HistoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")

public class HistoryController {
    @Autowired
    private HistoryRepo historyRepo;

//    @GetMapping("/histories")
//    public String history(@PathVariable String username) {
//
//    }
}
