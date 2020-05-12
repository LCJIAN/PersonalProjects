package com.lcjian.spunsugar.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lcjian.spunsugar.dto.EpgNowDTO;
import com.lcjian.spunsugar.dto.ProgramDTO;
import com.lcjian.spunsugar.dto.TvStationDTO;
import com.lcjian.spunsugar.entity.TvLiveSource;
import com.lcjian.spunsugar.entity.TvStation;
import com.lcjian.spunsugar.service.TvStationService;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@RestController
@RequestMapping("/api/tv_stations")
public class TvStationController {

    @Autowired
    private TvStationService tvStationService;

    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public List<TvStationDTO> tvStations(
            @RequestParam(value = "tv_station_type", defaultValue = "cctv", required = false) String tvStationType) {
        List<TvStation> tvStations = tvStationService.getTvStationsByType(tvStationType);
        List<TvStationDTO> result = new ArrayList<>();
        List<EpgNowDTO> epgNows = epgNows();
        for (TvStation tvStation : tvStations) {
            TvStationDTO tvStationDTO = new TvStationDTO();
            tvStationDTO.setChannel(tvStation.getChannel());
            tvStationDTO.setId(tvStation.getId());
            tvStationDTO.setLogo(tvStation.getLogo());
            tvStationDTO.setName(tvStation.getName());
            tvStationDTO.setType(tvStation.getType());
            for (EpgNowDTO epgNow : epgNows) {
                if (tvStation.getChannel().equals(epgNow.getC())) {
                    ProgramDTO program = new ProgramDTO();
                    program.setName(epgNow.getT());
                    program.setTime(epgNow.getS());
                    tvStationDTO.setNow(program);
                    epgNows.remove(program);
                    break;
                }
            }
            result.add(tvStationDTO);
        }
        return result;
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public List<TvStationDTO> tvStationsByIds(@RequestParam("ids") String ids) {
        List<TvStationDTO> result = new ArrayList<>();
        if (!StringUtils.isEmpty(ids)) {
            List<TvStation> tvStations = tvStationService.getTvStationsByIds(ids);
            List<EpgNowDTO> epgNows = epgNows();
            for (TvStation tvStation : tvStations) {
                TvStationDTO tvStationDTO = new TvStationDTO();
                tvStationDTO.setChannel(tvStation.getChannel());
                tvStationDTO.setId(tvStation.getId());
                tvStationDTO.setLogo(tvStation.getLogo());
                tvStationDTO.setName(tvStation.getName());
                tvStationDTO.setType(tvStation.getType());
                for (EpgNowDTO epgNow : epgNows) {
                    if (tvStation.getChannel().equals(epgNow.getC())) {
                        ProgramDTO program = new ProgramDTO();
                        program.setName(epgNow.getT());
                        program.setTime(epgNow.getS());
                        tvStationDTO.setNow(program);
                        epgNows.remove(program);
                        break;
                    }
                }
                result.add(tvStationDTO);
            }
        }
        return result;
    }

    @RequestMapping(path = "/{channel}/sources", method = RequestMethod.GET)
    public List<TvLiveSource> sources(@PathVariable("channel") String channel) {
        return tvStationService.getTvLiveSourcesByChannel(channel);
    }

    private static final String EPG_NOW_URL = "http://tv.cntv.cn/api/epg/now?c=cctv1,cctv2,cctv3,cctv4,cctveurope,"
            + "cctvamerica,cctv5,cctv6,cctv7,cctv8,cctvjilu,cctv10,cctv11,cctv12,cctv13,cctvchild,cctv15,"
            + "anhui,btv1,chongqing,dongfang,"
            + "dongnan,guangdong,guangxi,gansu,guizhou,hebei,henan,heilongjiang,hubei,jilin,jiangxi,liaoning,travel,"
            + "neimenggu,ningxia,qinghai,shandong,sdetv,shenzhen,shan3xi,shan1xi,sichuan,tianjin,xizang,xiamen,"
            + "xinjiang,yanbian,yunnan,cctvfxzl,xinkedongman,zhinan";

    private OkHttpClient mClient = new OkHttpClient();

    private List<EpgNowDTO> epgNows() {
        Request request = new Request.Builder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                .url(EPG_NOW_URL).build();
        Response response;
        try {
            response = mClient.newCall(request).execute();
            if (response.isSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return mapper.readValue(response.body().string(),
                        mapper.getTypeFactory().constructParametricType(ArrayList.class, EpgNowDTO.class));
            } else {
                return Collections.emptyList();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
