package com.lcjian.spunsugar.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lcjian.spunsugar.entity.TvLiveSource;
import com.lcjian.spunsugar.entity.TvStation;
import com.lcjian.spunsugar.repository.TvStationRepository;

@Service
public class RepositoryTvStationService implements TvStationService {

    @Autowired
    private TvStationRepository tvStationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TvStation> getTvStationsByType(String type) {
        return tvStationRepository.findAllByType(type);
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
