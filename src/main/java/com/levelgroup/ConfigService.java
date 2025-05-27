package com.levelgroup;

import org.springframework.stereotype.Service;

@Service
public class ConfigService {

    private int trialCalls = 10; // initial default

    public int getTrialCalls() {
        return trialCalls;
    }

    public void setTrialCalls(int trialCalls) {
        this.trialCalls = trialCalls;
    }
}
