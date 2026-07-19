import { useRef, useMemo, useCallback } from 'react';
import { useFrame } from '@react-three/fiber';
import * as THREE from 'three';
import { useStore } from '../lib/store';
import { propagateSatellite, getGeodetic } from '../lib/tle';
import { GROUP_CONFIG_MAP, EARTH_RADIUS_KM } from '../types';
import type { SatelliteData } from '../types';
const SAT_SIZE = 0.04;
const TRAIL_LENGTH = 60;
const TRAIL_INTERVAL = 60; // segundos simulados entre pontos

export function Satellites() {
  const satellites = useStore((s) => s.satellites);
  const activeGroups = useStore((s) => s.activeGroups);
  const searchQuery = useStore((s) => s.searchQuery);
  const simulationTime = useStore((s) => s.simulationTime);
  const selectedSatellite = useStore((s) => s.selectedSatellite);
  const selectSatellite = useStore((s) => s.selectSatellite);
  const setSelectedPosition = useStore((s) => s.setSelectedPosition);

  const meshRef = useRef<THREE.InstancedMesh>(null);
  const footprintRef = useRef<THREE.Mesh>(null);
  const selectedMarkerRef = useRef<THREE.Mesh>(null);

  // Filtrar satélites visíveis
  const visibleSats = useMemo(() => {
    const q = searchQuery.toLowerCase();
    return satellites.filter((s) => {
      if (!activeGroups.has(s.group)) return false;
      if (q && !s.name.toLowerCase().includes(q) && s.noradId !== q) return false;
      return true;
    });
  }, [satellites, activeGroups, searchQuery]);

  // Mapa de índice para satélite
  const satIndexMap = useMemo(() => {
    const map = new Map<number, SatelliteData>();
    visibleSats.forEach((sat, i) => map.set(i, sat));
    return map;
  }, [visibleSats]);

  // Dummy para instanced mesh
  const dummy = useMemo(() => new THREE.Object3D(), []);
  const color = useMemo(() => new THREE.Color(), []);

  // Geometria compartilhada
  const satGeometry = useMemo(() => new THREE.SphereGeometry(SAT_SIZE, 8, 8), []);
  const satMaterial = useMemo(
    () =>
      new THREE.MeshBasicMaterial({
        color: 0xffffff,
        transparent: true,
        opacity: 0.9,
      }),
    []
  );

  // Posições atuais dos satélites (para raycasting)
  const positionsRef = useRef<Float32Array>(new Float32Array(0));

  // Atualizar posições frame a frame
  useFrame(() => {
    if (!meshRef.current) return;

    const now = simulationTime;
    const posArray = new Float32Array(visibleSats.length * 3);

    visibleSats.forEach((sat, i) => {
      const result = propagateSatellite(sat, now);
      if (!result) {
        dummy.position.set(0, -1000, 0); // esconde
        dummy.updateMatrix();
        meshRef.current!.setMatrixAt(i, dummy.matrix);
        return;
      }

      const { x, y, z } = result.position;
      posArray[i * 3] = x;
      posArray[i * 3 + 1] = y;
      posArray[i * 3 + 2] = z;

      dummy.position.set(x, y, z);
      dummy.updateMatrix();
      meshRef.current!.setMatrixAt(i, dummy.matrix);

      const config = GROUP_CONFIG_MAP[sat.group];
      color.set(config.color);
      meshRef.current!.setColorAt(i, color);
    });

    meshRef.current.instanceMatrix.needsUpdate = true;
    if (meshRef.current.instanceColor) {
      meshRef.current.instanceColor.needsUpdate = true;
    }

    positionsRef.current = posArray;

    // Atualizar posição do satélite selecionado
    if (selectedSatellite) {
      const result = propagateSatellite(selectedSatellite, now);
      if (result) {
        const geo = getGeodetic(selectedSatellite, now);
        if (geo) {
          setSelectedPosition({
            x: result.position.x,
            y: result.position.y,
            z: result.position.z,
            lat: geo.lat,
            lon: geo.lon,
            alt: geo.alt,
            velocity: result.velocity,
            inclination: geo.inclination,
            period: geo.period,
          });
        }

        // Marker do satélite selecionado
        if (selectedMarkerRef.current) {
          selectedMarkerRef.current.position.set(
            result.position.x,
            result.position.y,
            result.position.z
          );
        }
      }
    }
  });

  // Raycasting para seleção
  const handlePointerDown = useCallback(
    (event: THREE.Event) => {
      const e = event as unknown as { instanceId?: number };
      if (e.instanceId === undefined) return;
      const sat = satIndexMap.get(e.instanceId);
      if (sat) {
        selectSatellite(sat);
      }
    },
    [satIndexMap, selectSatellite]
  );

  // Gerar trilha para satélite selecionado
  const selectedTrail = useMemo(() => {
    if (!selectedSatellite) return null;

    const points: THREE.Vector3[] = [];
    const baseTime = simulationTime.getTime();

    for (let i = 0; i < TRAIL_LENGTH; i++) {
      const t = new Date(baseTime - i * TRAIL_INTERVAL * 1000);
      const result = propagateSatellite(selectedSatellite, t);
      if (result) {
        points.push(new THREE.Vector3(result.position.x, result.position.y, result.position.z));
      }
    }

    if (points.length < 2) return null;

    const geometry = new THREE.BufferGeometry().setFromPoints(points);
    const config = GROUP_CONFIG_MAP[selectedSatellite.group];

    return (
      <line>
        <bufferGeometry attach="geometry" {...geometry} />
        <lineBasicMaterial
          attach="material"
          color={config.color}
          transparent
          opacity={0.5}
          linewidth={1}
        />
      </line>
    );
  }, [selectedSatellite, simulationTime]);

  // Footprint do satélite selecionado
  const footprint = useMemo(() => {
    if (!selectedSatellite || !selectedSatellite.satrec) return null;

    // Raio do footprint aproximado ~67 graus de ângulo de elevação mínimo
    const altKm = 20000; // aproximação média
    const earthRadius = EARTH_RADIUS_KM;
    const footprintRadius = earthRadius * Math.acos(earthRadius / (earthRadius + altKm));
    const sceneRadius = footprintRadius / 1000;

    return (
      <mesh ref={footprintRef} rotation={[-Math.PI / 2, 0, 0]}>
        <ringGeometry args={[sceneRadius * 0.95, sceneRadius, 64]} />
        <meshBasicMaterial
          color={GROUP_CONFIG_MAP[selectedSatellite.group].color}
          transparent
          opacity={0.15}
          side={THREE.DoubleSide}
          depthWrite={false}
        />
      </mesh>
    );
  }, [selectedSatellite]);

  return (
    <group>
      {/* Satélites instanciados */}
      <instancedMesh
        ref={meshRef}
        args={[satGeometry, satMaterial, Math.max(visibleSats.length, 1)]}
        onPointerDown={handlePointerDown}
        frustumCulled={false}
      />

      {/* Glow dos satélites (pontos maiores via sprite) */}
      {visibleSats.map((sat) => (
        <SatelliteGlow key={sat.noradId} satellite={sat} simulationTime={simulationTime} />
      ))}

      {/* Trilha do selecionado */}
      {selectedTrail}

      {/* Footprint do selecionado */}
      {selectedSatellite && (
        <group>
          {footprint}
          <mesh ref={selectedMarkerRef}>
            <sphereGeometry args={[SAT_SIZE * 3, 16, 16]} />
            <meshBasicMaterial
              color={GROUP_CONFIG_MAP[selectedSatellite.group].color}
              transparent
              opacity={0.3}
            />
          </mesh>
        </group>
      )}
    </group>
  );
}

// Componente separado para glow de cada satélite (otimizado)
function SatelliteGlow({
  satellite,
  simulationTime,
}: {
  satellite: SatelliteData;
  simulationTime: Date;
}) {
  const ref = useRef<THREE.Sprite>(null);
  const config = GROUP_CONFIG_MAP[satellite.group];

  const texture = useMemo(() => {
    const canvas = document.createElement('canvas');
    canvas.width = 64;
    canvas.height = 64;
    const ctx = canvas.getContext('2d')!;

    const grad = ctx.createRadialGradient(32, 32, 0, 32, 32, 32);
    grad.addColorStop(0, config.glowColor + 'FF');
    grad.addColorStop(0.3, config.glowColor + '88');
    grad.addColorStop(1, config.glowColor + '00');
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, 64, 64);

    const tex = new THREE.CanvasTexture(canvas);
    tex.colorSpace = THREE.SRGBColorSpace;
    return tex;
  }, [config.glowColor]);

  useFrame(() => {
    if (!ref.current) return;
    const result = propagateSatellite(satellite, simulationTime);
    if (result) {
      ref.current.position.set(result.position.x, result.position.y, result.position.z);
    }
  });

  return (
    <sprite ref={ref} scale={[0.3, 0.3, 0.3]}>
      <spriteMaterial
        map={texture}
        transparent
        opacity={0.8}
        blending={THREE.AdditiveBlending}
        depthWrite={false}
      />
    </sprite>
  );
}
