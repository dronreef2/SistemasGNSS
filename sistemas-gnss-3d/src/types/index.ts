export interface SatelliteData {
  name: string;
  noradId: string;
  tleLine1: string;
  tleLine2: string;
  group: SatelliteGroup;
  satrec: import('satellite.js').SatRec | null;
}

export type SatelliteGroup =
  | 'GPS'
  | 'GLONASS'
  | 'Galileo'
  | 'BeiDou'
  | 'Starlink'
  | 'ISS';

export interface SatellitePosition {
  x: number;
  y: number;
  z: number;
  lat: number;
  lon: number;
  alt: number;
  velocity: number;
  period: number;
  inclination: number;
}

export interface GroupConfig {
  key: SatelliteGroup;
  label: string;
  color: string;
  celestrakUrl: string;
  glowColor: string;
}

export const GROUP_CONFIGS: GroupConfig[] = [
  {
    key: 'GPS',
    label: 'GPS (NAVSTAR)',
    color: '#00E5FF',
    glowColor: '#00E5FF',
    celestrakUrl: 'https://celestrak.org/NORAD/elements/gp.php?GROUP=gps-ops&FORMAT=TLE',
  },
  {
    key: 'GLONASS',
    label: 'GLONASS',
    color: '#FF5252',
    glowColor: '#FF5252',
    celestrakUrl: 'https://celestrak.org/NORAD/elements/gp.php?GROUP=glo-ops&FORMAT=TLE',
  },
  {
    key: 'Galileo',
    label: 'Galileo',
    color: '#69F0AE',
    glowColor: '#69F0AE',
    celestrakUrl: 'https://celestrak.org/NORAD/elements/gp.php?GROUP=galileo&FORMAT=TLE',
  },
  {
    key: 'BeiDou',
    label: 'BeiDou',
    color: '#FFD740',
    glowColor: '#FFD740',
    celestrakUrl: 'https://celestrak.org/NORAD/elements/gp.php?GROUP=beidou&FORMAT=TLE',
  },
  {
    key: 'Starlink',
    label: 'Starlink',
    color: '#E040FB',
    glowColor: '#E040FB',
    celestrakUrl: 'https://celestrak.org/NORAD/elements/gp.php?GROUP=starlink&FORMAT=TLE',
  },
  {
    key: 'ISS',
    label: 'ISS',
    color: '#FFFFFF',
    glowColor: '#FFFFFF',
    celestrakUrl: 'https://celestrak.org/NORAD/elements/gp.php?CATNR=25544&FORMAT=TLE',
  },
];

export const GROUP_CONFIG_MAP: Record<SatelliteGroup, GroupConfig> =
  GROUP_CONFIGS.reduce((acc, g) => ({ ...acc, [g.key]: g }), {} as Record<SatelliteGroup, GroupConfig>);

export const EARTH_RADIUS_KM = 6371;
export const SCALE = 1 / 1000; // 1 unidade = 1000 km
