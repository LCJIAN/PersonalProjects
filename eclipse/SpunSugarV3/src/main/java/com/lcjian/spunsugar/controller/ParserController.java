package com.lcjian.spunsugar.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ParserController {

//    public static final String ROOT = "D://parser";
    public static final String ROOT = "/home/spun_sugar/service/parser";

    private final ResourceLoader resourceLoader;

    @Autowired
    public ParserController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/parser")
    public String provideUploadInfo(Model model) throws IOException {

        model.addAttribute("files", Files.walk(Paths.get(ROOT))
                .filter(path -> !path.equals(Paths.get(ROOT)))
                .map(path -> Paths.get(ROOT).relativize(path).toString())
                .collect(Collectors.toList()));

        return "parser_upload_form";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/parser/{filename:.+}", produces="application/octet-stream")
    @ResponseBody
    public ResponseEntity<?> getFileApi(@PathVariable String filename) {
        try {
            return ResponseEntity.ok(resourceLoader.getResource("file:" + Paths.get(ROOT, filename).toString()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/parser/{filename:.+}", produces="application/octet-stream")
    @ResponseBody
    public ResponseEntity<?> getFile(@PathVariable String filename) {
        try {
            return ResponseEntity.ok(resourceLoader.getResource("file:" + Paths.get(ROOT, filename).toString()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/parser")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

        if (!file.isEmpty()) {
            try {
                Files.copy(file.getInputStream(), Paths.get(ROOT, file.getOriginalFilename()));
                redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");
            } catch (IOException | RuntimeException e) {
                redirectAttributes.addFlashAttribute("message", "Failued to upload " + file.getOriginalFilename() + " => " + e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("message", "Failed to upload " + file.getOriginalFilename() + " because it was empty");
        }

        return "redirect:/parser";
    }
}
