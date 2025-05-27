package com.levelgroup;

import org.springframework.stereotype.Service;

@Service
public class ConfigService {

    private final TrialConfigRepository repository;

    public ConfigService(TrialConfigRepository repository) {
        this.repository = repository;
    }

    public int getTrialCalls() {
        return repository.findById(1)
                .map(TrialConfig::getTrialCalls)
                .orElse(5); // fallback, якщо не знайдено
    }

    public void setTrialCalls(int value) {
        TrialConfig config = repository.findById(1).orElse(new TrialConfig());
        config.setTrialCalls(value);
        repository.save(config);
    }
}
