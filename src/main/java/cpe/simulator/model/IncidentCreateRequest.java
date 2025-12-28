package cpe.simulator.model;

import java.time.LocalDateTime;

public class IncidentCreateRequest {
    private String createdByOperatorId;
    private String address;
    private String zipcode;
    private String city;
    private double latitude;
    private double longitude;
    private String description;
    private LocalDateTime endedAt;

    public String getCreatedByOperatorId() {
        return createdByOperatorId;
    }

    public void setCreatedByOperatorId(String createdByOperatorId) {
        this.createdByOperatorId = createdByOperatorId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }
}
