package com.lcjian.spunsugar.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lcjian.spunsugar.UnknownResourceException;

/**
 * Default controller that exists to return a proper REST response for unmapped requests.
 */
@RestController
public class DefaultController {

    @RequestMapping("/api/**")
    public void unmappedRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        throw new UnknownResourceException("There is no resource for path " + uri);
    }
}
