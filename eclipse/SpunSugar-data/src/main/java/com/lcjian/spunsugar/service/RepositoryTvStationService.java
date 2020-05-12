package com.lcjian.spunsugar.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lcjian.spunsugar.entity.TvLiveSource;
import com.lcjian.spunsugar.entity.TvStation;
import com.lcjian.spunsugar.repository.TvLiveSourceRepository;
import com.lcjian.spunsugar.repository.TvStationRepository;

@Service
public class RepositoryTvStationService implements TvStationService {

    @Autowired
    private TvStationRepository tvStationRepository;
    
    @Autowired
    private TvLiveSourceRepository tvLiveSourceRepository;

    @Override
    @Transactional
    public TvStation create(TvStation outterTvStation) {
        Set<TvLiveSource> outterSources = outterTvStation.getTvLiveSources();
        TvStation innerTvStation = tvStationRepository.findOneByChannel(outterTvStation.getChannel());
        if (innerTvStation == null) {
            innerTvStation = tvStationRepository.save(outterTvStation);
            
            if (outterSources != null) {
                for (TvLiveSource source : outterSources) {
                    source.setTvStation(innerTvStation);
                    tvLiveSourceRepository.save(source);
                }
            }
        } else {
            if (outterSources != null) {
                final TvStation finalInnerTvStation = innerTvStation;
                Set<TvLiveSource> innerSources = finalInnerTvStation.getTvLiveSources();
                
                Set<TvLiveSource> deleteSources = innerSources.stream()
                        .filter(v -> outterSources.stream()
                                .allMatch(t -> !StringUtils.equals(t.getUrl(), v.getUrl())))
                        .collect(Collectors.toSet());
                
                Set<TvLiveSource> updateSources = innerSources.stream()
                        .filter(v -> outterSources.stream()
                                .anyMatch(t -> {
                                    boolean update = StringUtils.equals(t.getUrl(), v.getUrl())
                                            && !StringUtils.equals(v.getSite(), t.getSite());
                                    if (update) {
                                        v.setSite(t.getSite());
                                    }
                                    return update;
                                }))
                        .collect(Collectors.toSet());
                
                Set<TvLiveSource> insertSources = outterSources.stream()
                        .filter(v -> innerSources.stream()
                                .allMatch(t -> !StringUtils.equals(t.getUrl(), v.getUrl())))
                        .map(v -> {v.setTvStation(finalInnerTvStation);return v;})
                        .collect(Collectors.toSet());
                
                // http://blog.csdn.net/yuzhenyuan1/article/details/45078243
                finalInnerTvStation.getTvLiveSources().clear();
                
                tvLiveSourceRepository.delete(deleteSources);
                tvLiveSourceRepository.save(updateSources);
                tvLiveSourceRepository.save(insertSources);
            }
            boolean updateTvStation = false;
            if (!Objects.equals(innerTvStation.getName(), outterTvStation.getName())) {
                innerTvStation.setName(outterTvStation.getName());
                updateTvStation = true;
            }
            if (!Objects.equals(innerTvStation.getLogo(), outterTvStation.getLogo())) {
                innerTvStation.setLogo(outterTvStation.getLogo());
                updateTvStation = true;
            }
            if (!Objects.equals(innerTvStation.getType(), outterTvStation.getType())) {
                innerTvStation.setType(outterTvStation.getType());
                updateTvStation = true;
            }
            if (updateTvStation) {
                innerTvStation = tvStationRepository.save(innerTvStation);
            }
        }
        return outterTvStation;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TvStation> getTvStations() {
        return tvStationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TvLiveSource> getTvLiveSourcesByChannel(String channel) {
        return new ArrayList<>(tvStationRepository.findOneByChannel(channel).getTvLiveSources());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TvStation> getTvStationsByIds(String ids) {
        return tvStationRepository.findAll(
                Arrays.asList(ids.split(","))
                .stream().mapToInt(s -> Integer.parseInt(s))
                .boxed()
                .collect(Collectors.toList()));
    }
}
