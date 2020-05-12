package com.lcjian.spunsugar.jersey.exception;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.lcjian.spunsugar.entity.ErrorMsg;

@Provider
public class SpunSugarExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException exception) {
        if (exception instanceof BadRequestException
                || exception instanceof NotFoundException
                || exception instanceof ForbiddenException) {
            return Response.fromResponse(exception.getResponse()).entity(new ErrorMsg(1, exception.getMessage()))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        return Response.fromResponse(exception.getResponse()).entity(new ErrorMsg(1, "内部服务器出错,请稍后再试!"))
                .type(MediaType.APPLICATION_JSON).build();
    }
}
