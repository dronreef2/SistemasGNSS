export type SatelliteGroup = 'gps' | 'glonass' | 'galileo' | 'beidou' | 'starlink' | 'iss'

export interface SatelliteData {
  name: string
  noradId: string
  tle1: string
  tle2: string
  group: SatelliteGroup
}

export interface SatellitePosition {
  lat: number
  lon: number
  alt: number
  velocity: number
  inclination: number
  period: number
}

export const GROUP_CONFIGS = [
  { key: 'gps', label: 'GPS', color: '#00e5ff' },
  { key: 'glonass', label: 'GLONASS', color: '#ff6b6b' },
  { key: 'galileo', label: 'Galileo', color: '#ffd93d' },
  { key: 'beidou', label: 'BeiDou', color: '#6bcB77' },
  { key: 'starlink', label: 'Starlink', color: '#9d4edd' },
  { key: 'iss', label: 'ISS', color: '#ffffff' },
] as const satisfies ReadonlyArray<{ key: SatelliteGroup; label: string; color: string }>

export const GROUP_CONFIG_MAP = Object.fromEntries(
  GROUP_CONFIGS.map((group) => [group.key, group]),
) as Record<SatelliteGroup, (typeof GROUP_CONFIGS)[number]>
