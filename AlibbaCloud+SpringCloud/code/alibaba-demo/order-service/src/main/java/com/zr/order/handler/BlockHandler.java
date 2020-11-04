package com.zr.order.handler;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Description:
 *
 * @author zhangr
 * 2020/10/30 17:18
 */
@Slf4j
@Component
public class BlockHandler implements BlockExceptionHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws Exception {
        log.info("-----------------------handler exception{}----------------------", e);
        httpServletResponse.setStatus(200);
        httpServletResponse.setHeader("content_Type","application/json;charset=UTF-8");
        httpServletResponse.getWriter().write("handler exception");
    }
}
