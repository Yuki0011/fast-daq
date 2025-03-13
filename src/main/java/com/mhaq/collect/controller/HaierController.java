package com.mhaq.collect.controller;

import com.mhaq.collect.common.LocalCacheUtil;
import com.mhaq.collect.entity.LabMonitorData;
import com.mhaq.collect.handler.TXZSerialHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;

import static com.mhaq.collect.common.BiteUtil.*;
import static com.mhaq.collect.common.LocalCacheUtil.*;

/**
 * @author xutao
 * @date 2021/4/11 9:41 下午
 * @description haier
 */
@RestController
@RequestMapping("/haier")
public class HaierController {

    @RequestMapping("/success")
    public String success() {
        return "启动成功";
    }


    @GetMapping("/txt/{content}")
    public void txt(@PathVariable("content") String content) {
        txt.append(content+"\n");
        txt.setCaretPosition(txt.getDocument().getLength());
    }






}
