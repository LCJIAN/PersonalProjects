package com.lcjian.spunsugar.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lcjian.spunsugar.entity.Config;
import com.lcjian.spunsugar.repository.ConfigRepository;

@Service
public class RepositoryConfigService implements ConfigService {

    @Autowired
    private ConfigRepository configRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Config> getConfigs() {
        return configRepository.findAll();
    }
}
