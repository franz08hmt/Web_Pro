package vn.edu.webpro.robotlab.model;

/** Fixed-shape response model for the health endpoint. */
public record HealthPayload(String service, String status, String database) {
    public String toJson() {
        return "{\"data\":{\"service\":\"" + service
                + "\",\"status\":\"" + status
                + "\",\"database\":\"" + database + "\"}}";
    }
}
