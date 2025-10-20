package com.dronreef.sistemasgnss.sis.model;

import java.time.Instant;

/**
 * Modelo leve para observação GNSS.
 */
public final class GNSSObservation {
    public final Instant epoch;
    public final String satelliteId;
    public final Double pseudorangeMeters;
    public final Double carrierPhaseCycles;
    public final Double snr;

    public GNSSObservation(Instant epoch, String satelliteId, Double pseudorangeMeters,
                           Double carrierPhaseCycles, Double snr) {
        this.epoch = epoch;
        this.satelliteId = satelliteId;
        this.pseudorangeMeters = pseudorangeMeters;
        this.carrierPhaseCycles = carrierPhaseCycles;
        this.snr = snr;
    }
}
