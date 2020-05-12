package com.lcjian.spunsugar.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lcjian.spunsugar.dto.RecommendDTO;
import com.lcjian.spunsugar.dto.SubjectDTO;
import com.lcjian.spunsugar.entity.Recommend;
import com.lcjian.spunsugar.entity.Subject;
import com.lcjian.spunsugar.service.RecommendService;
import com.lcjian.spunsugar.service.SubjectService;

@RestController
@RequestMapping("/api/recommends")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(method = RequestMethod.GET)
    public List<RecommendDTO> getConfigs() {
        List<Recommend> recommends = recommendService.findAll();
        return recommends.stream().map(r -> {
            RecommendDTO recommendDTO = new RecommendDTO();
            recommendDTO.setTitle(r.getTitle());
            recommendDTO.setType(r.getType());
            recommendDTO.setExtra(r.getExtra());
            recommendDTO.setExtra1(r.getExtra1());
            Subject subject = subjectService.get(r.getSubjectId());
            String subjectStr = "";
            try {
                subjectStr = objectMapper.writeValueAsString(new SubjectDTO(subject));
            } catch (Exception e) {
                e.printStackTrace();
            }
            recommendDTO.setData(subjectStr);
            return recommendDTO;
        }).collect(Collectors.toList());
    }
}
