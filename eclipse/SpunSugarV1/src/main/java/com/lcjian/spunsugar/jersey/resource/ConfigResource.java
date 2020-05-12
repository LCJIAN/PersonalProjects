package com.lcjian.spunsugar.jersey.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.lcjian.spunsugar.entity.Config;
import com.lcjian.spunsugar.service.ConfigServiceImpl;

@Path("configs")
public class ConfigResource {

    ConfigServiceImpl configService;

    public ConfigResource() {
        configService = new ConfigServiceImpl();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Config getConfig() {
        return configService.findAll().get(0);
    }
}
