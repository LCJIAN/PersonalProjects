package com.lcjian.spunsugar.jersey.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("parser")
public class ParserResource {

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public File downloadParser(@Context ServletContext application) {
        String realPath = application.getRealPath("/parser");
        File file = new File(realPath, "parser.jar");
        if (!file.exists()) {
            throw new NotFoundException();
        }
        return file;
    }

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadParser(@Context ServletContext application,
            @FormDataParam("parser_file") InputStream uploadedInputStream,
            @FormDataParam("parser_file") FormDataContentDisposition fileDetail) {
        String uploadedFileLocation = application.getRealPath("/parser");
        writeToFile(uploadedInputStream, uploadedFileLocation, fileDetail.getFileName());
        String output = "File uploaded to : " + uploadedFileLocation;
        return Response.status(200).entity(output).build();
    }

    private void writeToFile(InputStream is, String uploadedFileLocation, String fileName) {
        OutputStream os = null;
        try {
            File file = new File(uploadedFileLocation, fileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            os = new FileOutputStream(file);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = is.read(bytes)) != -1) {
                os.write(bytes, 0, read);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    is.close();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
