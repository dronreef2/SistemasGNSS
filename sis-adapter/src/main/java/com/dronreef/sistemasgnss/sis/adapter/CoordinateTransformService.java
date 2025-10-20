package com.dronreef.sistemasgnss.sis.adapter;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

/**
 * Serviço de transformações de coordenadas usando Apache SIS / GeoAPI.
 */
public interface CoordinateTransformService {
    MathTransform getTransform(CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS) throws Exception;
    double[] transform(MathTransform transform, double[] src) throws Exception;
}
