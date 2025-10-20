package com.dronreef.sistemasgnss.sis.adapter;

import com.dronreef.sistemasgnss.sis.model.GNSSObservation;
import org.apache.sis.feature.AbstractFeature;

/**
 * Mapeia observações GNSS para o modelo Feature (Apache SIS) e vice-versa.
 */
public interface FeatureMapper {
    AbstractFeature toFeature(GNSSObservation obs) throws Exception;
    GNSSObservation fromFeature(AbstractFeature feature) throws Exception;
}
