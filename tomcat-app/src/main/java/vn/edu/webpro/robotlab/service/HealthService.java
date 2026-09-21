package vn.edu.webpro.robotlab.service;

import vn.edu.webpro.robotlab.model.HealthPayload;

public final class HealthService {
    private final DatabaseHealthService databaseHealthService;

    public HealthService(DatabaseHealthService databaseHealthService) {
        this.databaseHealthService = databaseHealthService;
    }

    public HealthPayload readHealth() {
        return new HealthPayload("robot-assembly-lab-api", "ok", databaseHealthService.check());
    }
}
