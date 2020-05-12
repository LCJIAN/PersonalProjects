package com.lcjian.spunsugar.jersey.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

@Provider
public class CharsetResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext request,
            ContainerResponseContext response) {
        MediaType type = response.getMediaType();
        if (type != null) {
            String contentType = type.toString();
            if (!contentType.contains("charset")) {
                contentType = contentType + ";charset=utf-8";
                response.getHeaders().putSingle("Content-Type", contentType);
            }
        }
    }
}
