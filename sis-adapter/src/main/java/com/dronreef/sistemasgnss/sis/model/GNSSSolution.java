package com.dronreef.sistemasgnss.sis.model;

import java.time.Instant;

/**
 * Modelo leve para solução GNSS (posição processada).
 * Placeholder para expansão futura.
 */
public final class GNSSSolution {
    public final Instant epoch;
    public final Double latitude;
    public final Double longitude;
    public final Double altitude;
    public final String coordinateSystem;

    public GNSSSolution(Instant epoch, Double latitude, Double longitude, 
                        Double altitude, String coordinateSystem) {
        this.epoch = epoch;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.coordinateSystem = coordinateSystem;
    }
}
