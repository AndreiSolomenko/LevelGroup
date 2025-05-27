package com.levelgroup;

import jakarta.persistence.*;

@Entity
@Table(name = "trial_config")
public class TrialConfig {

    @Id
    @Column(name = "id")
    private Integer id = 1;

    @Column(name = "trial_calls", nullable = false)
    private int trialCalls;

    // Гетери і сетери
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getTrialCalls() {
        return trialCalls;
    }

    public void setTrialCalls(int trialCalls) {
        this.trialCalls = trialCalls;
    }
}
