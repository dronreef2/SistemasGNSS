import * as satellite from 'satellite.js';
import type { SatelliteData, SatelliteGroup } from '../types';
import { GROUP_CONFIGS } from '../types';

const CACHE_KEY = 'gnss_tle_cache_v1';
const CACHE_TTL_MS = 2 * 60 * 60 * 1000; // 2 horas

interface TleCacheEntry {
  group: SatelliteGroup;
  rawTle: string;
  timestamp: number;
  satellites: SatelliteData[];
}

interface TleCache {
  entries: Record<string, TleCacheEntry>;
  lastFetch: number;
}

function loadCache(): TleCache | null {
  try {
    const raw = localStorage.getItem(CACHE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as TleCache;
    if (Date.now() - parsed.lastFetch > CACHE_TTL_MS) return null;
    return parsed;
  } catch {
    return null;
  }
}

function saveCache(cache: TleCache) {
  try {
    localStorage.setItem(CACHE_KEY, JSON.stringify(cache));
  } catch {
    // Storage pode estar cheio — silencioso
  }
}

function parseTle(raw: string, group: SatelliteGroup): SatelliteData[] {
  const lines = raw.trim().split('\n').map((l) => l.trim()).filter(Boolean);
  const sats: SatelliteData[] = [];

  for (let i = 0; i < lines.length - 2; i += 3) {
    const name = lines[i].trim();
    const tle1 = lines[i + 1];
    const tle2 = lines[i + 2];

    if (!tle1 || !tle2 || !tle1.startsWith('1 ') || !tle2.startsWith('2 ')) continue;

    let satrec: satellite.SatRec | null = null;
    try {
      satrec = satellite.twoline2satrec(tle1, tle2);
    } catch {
      continue;
    }

    const noradId = tle1.substring(2, 7).trim();

    sats.push({
      name,
      noradId,
      tleLine1: tle1,
      tleLine2: tle2,
      group,
      satrec,
    });
  }

  return sats;
}

export async function fetchTleGroup(group: SatelliteGroup): Promise<SatelliteData[]> {
  const config = GROUP_CONFIGS.find((g) => g.key === group);
  if (!config) throw new Error(`Grupo desconhecido: ${group}`);

  const cache = loadCache();
  if (cache && cache.entries[group] && Date.now() - cache.entries[group].timestamp < CACHE_TTL_MS) {
    return cache.entries[group].satellites;
  }

  try {
    const response = await fetch(config.celestrakUrl, { cache: 'no-store' });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const raw = await response.text();

    const satellites = parseTle(raw, group);

    const newCache: TleCache = cache || { entries: {}, lastFetch: Date.now() };
    newCache.entries[group] = { group, rawTle: raw, timestamp: Date.now(), satellites };
    newCache.lastFetch = Date.now();
    saveCache(newCache);

    return satellites;
  } catch (err) {
    // Fallback para cache antigo mesmo expirado
    if (cache && cache.entries[group]) {
      console.warn(`[TLE] Fetch falhou para ${group}, usando cache antigo.`);
      return cache.entries[group].satellites;
    }
    throw err;
  }
}

export async function fetchAllTle(): Promise<SatelliteData[]> {
  const results = await Promise.allSettled(
    GROUP_CONFIGS.map((g) => fetchTleGroup(g.key))
  );

  const all: SatelliteData[] = [];
  results.forEach((r, i) => {
    if (r.status === 'fulfilled') {
      all.push(...r.value);
    } else {
      console.error(`[TLE] Falha ao carregar ${GROUP_CONFIGS[i].key}:`, r.reason);
    }
  });

  return all;
}

export function propagateSatellite(
  sat: SatelliteData,
  date: Date
): { position: { x: number; y: number; z: number }; velocity: number; gmst: number } | null {
  if (!sat.satrec) return null;

  try {
    const positionAndVelocity = satellite.propagate(sat.satrec, date);
    if (
      !positionAndVelocity.position ||
      typeof positionAndVelocity.position === 'boolean'
    )
      return null;

    const gmst = satellite.gstime(date);
    const positionEci = positionAndVelocity.position as satellite.EciVec3<number>;
    const velocityEci = positionAndVelocity.velocity as satellite.EciVec3<number>;

    // Converter ECI para ECEF
    const positionEcf = satellite.eciToEcf(positionEci, gmst);

    const x = positionEcf.x * SCALE;
    const y = positionEcf.z * SCALE; // Three.js: Y é up
    const z = -positionEcf.y * SCALE; // Three.js: Z é para frente (inverte)

    const vx = velocityEci.x;
    const vy = velocityEci.y;
    const vz = velocityEci.z;
    const velocity = Math.sqrt(vx * vx + vy * vy + vz * vz);

    return { position: { x, y, z }, velocity, gmst };
  } catch {
    return null;
  }
}

export function getGeodetic(
  sat: SatelliteData,
  date: Date
): { lat: number; lon: number; alt: number; inclination: number; period: number } | null {
  if (!sat.satrec) return null;

  try {
    const positionAndVelocity = satellite.propagate(sat.satrec, date);
    if (
      !positionAndVelocity.position ||
      typeof positionAndVelocity.position === 'boolean'
    )
      return null;

    const gmst = satellite.gstime(date);
    const positionEci = positionAndVelocity.position as satellite.EciVec3<number>;
    const positionGd = satellite.eciToGeodetic(positionEci, gmst);

    const lat = satellite.degreesLat(positionGd.latitude);
    const lon = satellite.degreesLong(positionGd.longitude);
    const alt = positionGd.height;

    // Inclinação e período orbitais
    const inclination = sat.satrec.inclo ? satellite.degreesLat(sat.satrec.inclo) : 0;
    const meanMotion = sat.satrec.no;
    const period = meanMotion > 0 ? (2 * Math.PI) / meanMotion / 60 : 0; // minutos

    return { lat, lon, alt, inclination, period };
  } catch {
    return null;
  }
}

const SCALE = 1 / 1000;
