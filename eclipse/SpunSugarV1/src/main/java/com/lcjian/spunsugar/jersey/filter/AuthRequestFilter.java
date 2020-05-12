package com.lcjian.spunsugar.jersey.filter;

import java.io.IOException;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import com.lcjian.spunsugar.util.MD5Utils;
import com.lcjian.spunsugar.util.StringUtils;

@Provider
public class AuthRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext)
            throws IOException {
        String authorization = requestContext.getHeaderString("Authorization");
        String date = requestContext.getHeaderString("Date");
        String uri = requestContext.getUriInfo().getRequestUri().toString();
        if (uri.contains("upload")) {
            return;
        }
        if (StringUtils.isEmpty(authorization)
                || StringUtils.isEmpty(date)
                || !MD5Utils.getMD532(uri + date + "spun_sugar")
                        .equals(authorization.substring(authorization.indexOf(" ") + 1))) {
            throw new ForbiddenException();
        }
    }
}
