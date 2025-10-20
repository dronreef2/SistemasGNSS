package com.dronreef.sistemasgnss.sis.util;

/**
 * Utilitários auxiliares ECEF &lt;-&gt; Geodetic (pontos estáticos / aproximações).
 * Implementações mais precisas podem delegar ao SIS quando apropriado.
 */
public final class EcefGeodeticUtils {
    private EcefGeodeticUtils() {}

    /**
     * Converte vetor ECEF (x,y,z) para array [x,y,z] (pass-through helper).
     * Placeholder: usar SIS para transformações Reais.
     */
    public static double[] pack(double x, double y, double z) {
        return new double[]{x, y, z};
    }
}
