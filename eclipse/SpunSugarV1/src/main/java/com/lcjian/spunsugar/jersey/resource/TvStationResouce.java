package com.lcjian.spunsugar.jersey.resource;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.googlecode.genericdao.search.Search;
import com.googlecode.genericdao.search.SearchResult;
import com.lcjian.spunsugar.entity.EpgNow;
import com.lcjian.spunsugar.entity.Program;
import com.lcjian.spunsugar.entity.TvLiveSource;
import com.lcjian.spunsugar.entity.TvStation;
import com.lcjian.spunsugar.parser.TVParser;
import com.lcjian.spunsugar.service.TvStationServiceImpl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

@Path("tv_stations")
public class TvStationResouce {

    TvStationServiceImpl tvStationService;
    
    public TvStationResouce() {
        tvStationService = new TvStationServiceImpl();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<TvStation> tvStations(
            @QueryParam("tv_station_type") String tvStationType) {
        Search search = new Search();
        search.addFilterEqual("type", tvStationType);
        SearchResult<TvStation> searchResult = tvStationService.searchAndCount(search);
        List<TvStation> result = searchResult.getResult();
        List<EpgNow> epgNows = epgNows();
        for (TvStation tvStation : result) {
            for (EpgNow epgNow : epgNows) {
                if (tvStation.getChannel().equals(epgNow.getC())) {
                    Program program = new Program();
                    program.setName(epgNow.getT());
                    program.setTime(epgNow.getS());
                    tvStation.setNow(program);
                    epgNows.remove(program);
                    break;
                }
            }
        }
        return result;
    }
    
    @GET
    @Path("{channel}/sources")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TvLiveSource> sources(@PathParam("channel") String channel) {
        return tvStationService.findTvStationSource(channel);
    }
    
    @POST
    @Path("real_sources")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> realSources(@FormParam("data") String data, @FormParam("type") String type) {
        return (new TVParser()).getLiveUrl2(data, type);
    }

    @GET
    @Path("{channel}/epg-list/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Program> epgList(
            @PathParam("channel") String channel,
            @PathParam("date") String date) {
        List<Program> programs = null;
        try {
            Document document = Jsoup.parse(new URL(
                    "http://tv.cntv.cn/index.php?action=epg-list&date=" + date
                            + "&channel=" + channel + "&mode="), 60 * 1000);
            Elements elements = document.getElementsByClass("p_name_a");
            programs = new ArrayList<Program>();
            for (Element element : elements) {
                Program program = new Program();
                String[] ownText = element.ownText().split(" ");
                program.setName(ownText[1]);
                program.setTime(ownText[0]);
                if (element.nextElementSibling().text().contains("直播中")) {
                    program.setLive(true);
                }
                programs.add(program);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return programs;
    }
    
    private static final String EPG_NOW_URL = "http://tv.cntv.cn/api/epg/now?c=cctv1,cctv2,cctv3,cctv4,cctveurope,cctvamerica,cctv5,cctv6,cctv7,cctv8,cctvjilu,cctvdoc,cctv10,cctv11,cctv12,cctv13,cctvchild,cctv15,cctv9,cctvfrench,cctvxiyu,cctvarabic,cctvrussian,cctv5plus,anhui,btv1,bingtuan,chongqing,dongfang,dongnan,guangdong,guangxi,gansu,guizhou,hebei,henan,heilongjiang,hubei,jilin,jiangxi,liaoning,travel,neimenggu,ningxia,qinghai,shandong,sdetv,shenzhen,shan3xi,shan1xi,sichuan,tianjin,xizang,xiamen,xinjiang,yanbian,yunnan,cctvfxzl,xinkedongman,zhinan";
    
    private OkHttpClient mClient = new OkHttpClient();
    
    private List<EpgNow> epgNows() {
        Request request = new Request.Builder().url(EPG_NOW_URL).build();
        Response response;
        try {
            response = mClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return (new Gson()).fromJson(response.body().string(), new TypeToken<List<EpgNow>>() {}.getType());
             } else {
                 return Collections.emptyList();
             }
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
