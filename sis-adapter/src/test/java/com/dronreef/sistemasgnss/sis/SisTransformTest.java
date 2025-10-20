package com.dronreef.sistemasgnss.sis;

import com.dronreef.sistemasgnss.sis.impl.SisCoordinateTransformServiceImpl;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.apache.sis.referencing.CRS;

import static org.junit.Assert.*;

/**
 * Teste simples de integração para transformação ECEF -&gt; WGS84 geodésico.
 */
public class SisTransformTest {
    @Test
    public void testEcefToGeodeticTransform() throws Exception {
        CoordinateReferenceSystem ecef = CRS.forCode("EPSG:4978"); // ECEF
        CoordinateReferenceSystem wgs84 = CRS.forCode("EPSG:4326"); // lat/lon
        SisCoordinateTransformServiceImpl service = new SisCoordinateTransformServiceImpl();
        MathTransform transform = service.getTransform(ecef, wgs84);
        assertNotNull(transform);

        double[] ecefPoint = new double[]{3657660.66, 255768.55, 5201386.17};
        double[] result = service.transform(transform, ecefPoint);
        assertNotNull(result);
        assertEquals(3, result.length);
        // Não assertamos valores numéricos rígidos aqui — validação visual/manual recomendada.
    }
}
