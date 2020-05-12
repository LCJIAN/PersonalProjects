package com.lcjian.spunsugar.jersey.resource;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.googlecode.genericdao.search.Search;
import com.googlecode.genericdao.search.SearchResult;
import com.lcjian.spunsugar.entity.HentaiAnimeEpisode;
import com.lcjian.spunsugar.entity.PageResult;
import com.lcjian.spunsugar.service.HentaiServiceImpl;

@Path("hentais")
public class HentaiResource {

    HentaiServiceImpl hentaiService;

    public HentaiResource() {
        hentaiService = new HentaiServiceImpl();
    }

    @GET
    @Path("episodes")
    @Produces(MediaType.APPLICATION_JSON)
    public PageResult<HentaiAnimeEpisode> episodes(
            @DefaultValue("1") @QueryParam("page") Integer page,
            @DefaultValue("20") @QueryParam("page_size") Integer pageSize) {
        if (page < 1) {
            throw new BadRequestException("页数必须大于0");
        }
        if (pageSize < 1) {
            throw new BadRequestException("每页大小必须大于0");
        }
        Search search = new Search();
        search.setPage(page - 1);
        search.setMaxResults(pageSize);
        SearchResult<HentaiAnimeEpisode> searchResult = hentaiService.searchAndCountEpisode(search);
        PageResult<HentaiAnimeEpisode> pageResult = new PageResult<HentaiAnimeEpisode>();
        pageResult.total_results = searchResult.getTotalCount();
        pageResult.results = searchResult.getResult();
        pageResult.page = page;
        return pageResult;
    }
}
