# sis-adapter

Módulo adaptador para integrar Apache SIS ao repositório SistemasGNSS.

Objetivo
- Fornecer adaptadores para transformações de CRS, mapeamento de observações/soluções GNSS para Features SIS, I/O (GeoTIFF/NetCDF/CSV) e metadados ISO.
- Ser modular, leve e incremental — comece com transformações CRS e depois adicione I/O e metadados.
- Permitir testes e benchmarks comparativos de memória/desempenho.

Estrutura do módulo
- build.gradle / pom.xml
- src/main/java/com/dronreef/sistemasgnss/sis/...
  - adapter (interfaces de serviço)
  - impl (implementações baseadas em SIS)
  - model (GNSSObservation, GNSSSolution)
  - util (funções utilitárias, e.g. ECEF ↔ geodetic helpers)
- src/test/java/... (testes de integração unitários)

Requisitos
- Java 11+
- Gradle (ou Maven) e acesso ao Maven Central
- Dependências principais: org.apache.sis.core:sis-referencing, sis-io, sis-utility (versão `1.5` nas amostras; ajustar se necessário)

Como compilar (Gradle)
```
cd sis-adapter
./gradlew assemble
```

Como compilar (Maven)
```
cd sis-adapter
mvn -U test package
```

Exemplo de uso rápido (transformação)
- Use a implementação `SisCoordinateTransformServiceImpl` para obter um `MathTransform` entre EPSG:4978 (ECEF) e EPSG:4326 (WGS84 geodetic) e transformar vetores (x,y,z).

Boas práticas de integração
1. Integre incrementalmente (crs -&gt; feature -&gt; io -&gt; metadata).
2. Escreva testes de regressão para transformação de coordenadas e para exportadores I/O.
3. Em cargas grandes, prefira manipulação em streaming e mapeamento "lazy" das Features para evitar criar milhões de objetos simultaneamente.

Licença
- Este módulo é um adaptador. Ao incluir Apache SIS, você deve observar a licença Apache 2.0 e verificar as dependências opcionais (NOTICE).
