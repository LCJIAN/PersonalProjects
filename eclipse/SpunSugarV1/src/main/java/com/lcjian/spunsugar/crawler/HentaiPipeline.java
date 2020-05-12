package com.lcjian.spunsugar.crawler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import com.lcjian.spunsugar.dao.HentaiAnimeEpisodeDAOImpl;
import com.lcjian.spunsugar.dao.HentaiAnimeGenreDAOImpl;
import com.lcjian.spunsugar.dao.HentaiAnimeMakerDAOImpl;
import com.lcjian.spunsugar.dao.HentaiAnimeSeriesDAOImpl;
import com.lcjian.spunsugar.entity.HentaiAnimeEpisode;
import com.lcjian.spunsugar.entity.HentaiAnimeGenre;
import com.lcjian.spunsugar.entity.HentaiAnimeMaker;
import com.lcjian.spunsugar.entity.HentaiAnimeSeries;
import com.lcjian.spunsugar.util.HibernateUtil;

public class HentaiPipeline implements Pipeline {

    private HentaiAnimeGenreDAOImpl genreDao;
    
    private HentaiAnimeMakerDAOImpl makerDao;
    
    private HentaiAnimeSeriesDAOImpl seriesDao;
    
    private HentaiAnimeEpisodeDAOImpl episodeDao;

    private SessionFactory mSessionFactory;

    public HentaiPipeline() {
        mSessionFactory = HibernateUtil.getSessionFactory();
        genreDao = new HentaiAnimeGenreDAOImpl();
        genreDao.setSessionFactory(mSessionFactory);
        makerDao = new HentaiAnimeMakerDAOImpl();
        makerDao.setSessionFactory(mSessionFactory);
        seriesDao = new HentaiAnimeSeriesDAOImpl();
        seriesDao.setSessionFactory(mSessionFactory);
        episodeDao = new HentaiAnimeEpisodeDAOImpl();
        episodeDao.setSessionFactory(mSessionFactory);
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        String url = resultItems.getRequest().getUrl();
        if (url.equals("http://www.dmm.co.jp/digital/anime/-/genre/")) {
            List<HentaiAnimeGenre> hentaiAnimeGenres = resultItems.get("hentai_anime_genres");
            Session session = mSessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            for (HentaiAnimeGenre genre : hentaiAnimeGenres) {
                HentaiAnimeGenre genreNew = genreDao.find(genre.getId());
                if (genreNew == null) {
                    genreDao.save(genre);
                } else {
                    genreNew.setType(genre.getType());
                    genreDao.save(genreNew);
                }
            }
            transaction.commit();
        } else if (url.equals("http://www.dmm.co.jp/digital/anime/-/maker/")) {
            List<HentaiAnimeMaker> hentaiAnimeMakers = resultItems.get("hentai_anime_makers");
            Session session = mSessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            for (HentaiAnimeMaker maker : hentaiAnimeMakers) {
                HentaiAnimeMaker makerNew = makerDao.find(maker.getId());
                if (makerNew == null) {
                    makerDao.save(maker);
                }
            }
            transaction.commit();
        } else if (url.startsWith("http://www.dmm.co.jp/digital/anime/-/series/=/sort=ranking/")) {
            List<HentaiAnimeSeries> hentaiAnimeSeries = resultItems.get("hentai_anime_series");
            Session session = mSessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            for (HentaiAnimeSeries series : hentaiAnimeSeries) {
                HentaiAnimeSeries seriesNew = seriesDao.find(series.getId());
                if (seriesNew == null) {
                    seriesDao.save(series);
                }
            }
            transaction.commit();
        } else if (url.startsWith("http://www.dmm.co.jp/digital/anime/-/list/=/limit=120/sort=date/")) {
            List<HentaiAnimeEpisode> hentaiAnimeEpisodes = resultItems.get("hentai_anime_episodes");
            Session session = mSessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            for (HentaiAnimeEpisode episode : hentaiAnimeEpisodes) {
                HentaiAnimeEpisode episodeNew = episodeDao.find(episode.getId());
                if (episodeNew == null) {
                    episodeDao.save(episode);
                }
            }
            transaction.commit();
        } else {
            HentaiAnimeEpisode episodeDetail = resultItems.get("hentai_anime_episode_detail");
            Session session = mSessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            HentaiAnimeEpisode episode = episodeDao.find(episodeDetail.getId());
            episode.setDeliveryStartDate(episodeDetail.getDeliveryStartDate());
            episode.setDuration(episodeDetail.getDuration());
            
            HentaiAnimeSeries series = episodeDetail.getHentaiAnimeSeries();
            if (series != null) {
                series = seriesDao.find(series.getId());
                if (series != null) {
                    episode.setHentaiAnimeSeries(series);
                }
            }
           
            HentaiAnimeMaker maker = episodeDetail.getHentaiAnimeMaker();
            if (maker != null) {
                maker = makerDao.find(maker.getId());
                if (maker != null) {
                    episode.setHentaiAnimeMaker(maker);
                }
            }
            
            Set<HentaiAnimeGenre> genres = episodeDetail.getHentaiAnimeGenres();
            if (genres != null && !genres.isEmpty()) {
                Set<HentaiAnimeGenre> genresNew = new HashSet<HentaiAnimeGenre>();
                for (HentaiAnimeGenre genre : genres) {
                    genre = genreDao.find(genre.getId());
                    if (genre != null) {
                        genresNew.add(genre);
                    }
                }
                episode.setHentaiAnimeGenres(genresNew);
            }
            episode.setPosterLarge(episodeDetail.getPosterLarge());
            episode.setPosterMedium(episodeDetail.getPosterMedium());
            episode.setOverview(episodeDetail.getOverview());
            episode.setSampleImage(episodeDetail.getSampleImage());
            transaction.commit();
        }
    }
}
