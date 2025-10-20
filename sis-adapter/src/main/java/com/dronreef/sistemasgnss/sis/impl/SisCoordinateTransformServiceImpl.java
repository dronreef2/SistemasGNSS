package com.dronreef.sistemasgnss.sis.impl;

import com.dronreef.sistemasgnss.sis.adapter.CoordinateTransformService;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.apache.sis.referencing.CRS;

/**
 * Implementação que delega ao Apache SIS para obter transformações.
 */
public class SisCoordinateTransformServiceImpl implements CoordinateTransformService {
    @Override
    public MathTransform getTransform(CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS) throws Exception {
        // Usa SIS para construir a operação de transformação entre sistemas
        return CRS.findOperation(sourceCRS, targetCRS, null).getMathTransform();
    }

    @Override
    public double[] transform(MathTransform transform, double[] src) throws Exception {
        double[] dst = new double[src.length];
        transform.transform(src, 0, dst, 0, 1);
        return dst;
    }
}
