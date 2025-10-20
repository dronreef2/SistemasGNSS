package com.dronreef.sistemasgnss.sis.adapter;

import java.io.File;
import java.util.List;
import com.dronreef.sistemasgnss.sis.model.GNSSSolution;

/**
 * Serviço para exportar/importar resultados geoespaciais (GeoTIFF, NetCDF, CSV).
 */
public interface GeoIOService {
    void exportToGeoTiff(File outputFile, double[][] grid, double[] geotransform) throws Exception;
    void exportToNetCDF(File outputFile, GNSSSolution solution) throws Exception;
    void exportToCSV(File outputFile, List<GNSSSolution> solutions) throws Exception;
}
