package com.alibaba.cloud.imports.examples.controller;

import com.alibaba.cloud.imports.examples.model.UserConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 *
 * @author freeman
 */
@RestController
public class UserController {

    @Autowired
    private UserConfig userConfig;

    @GetMapping
    public String get() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(userConfig);
    }

}
