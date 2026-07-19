import type { SatelliteData, SatelliteGroup } from '../types'

const SAMPLE_TLE: Record<SatelliteGroup, SatelliteData[]> = {
  gps: [
    {
      name: 'GPS BIIR-2  (PRN 13)',
      noradId: '24876',
      group: 'gps',
      tle1: '1 24876U 97035A   26199.39980556 -.00000063  00000+0  00000+0 0  9998',
      tle2: '2 24876  55.3533 301.6110 0104308  57.9170 303.1135  2.00564202212812',
    },
  ],
  glonass: [
    {
      name: 'COSMOS 2522',
      noradId: '42939',
      group: 'glonass',
      tle1: '1 42939U 17055A   26199.28745565  .00000028  00000+0  00000+0 0  9990',
      tle2: '2 42939  64.8913 341.4439 0016205 220.5286 139.4290  2.13103227 68601',
    },
  ],
  galileo: [
    {
      name: 'GSAT0219 (GALILEO-FM15)',
      noradId: '43566',
      group: 'galileo',
      tle1: '1 43566U 18079A   26199.34787088 -.00000066  00000+0  00000+0 0  9990',
      tle2: '2 43566  56.9985  61.2474 0001075 261.8990  98.1366  1.70475044 48057',
    },
  ],
  beidou: [
    {
      name: 'BEIDOU-3 M20',
      noradId: '43602',
      group: 'beidou',
      tle1: '1 43602U 18093A   26199.54811536  .00000056  00000+0  00000+0 0  9992',
      tle2: '2 43602  55.0623  55.7063 0005507 359.0649   0.9810  1.86231514 52672',
    },
  ],
  starlink: [
    {
      name: 'STARLINK-10985',
      noradId: '60498',
      group: 'starlink',
      tle1: '1 60498U 24140A   26199.48772006  .00002967  00000+0  17726-3 0  9994',
      tle2: '2 60498  53.1526  53.5508 0001327  85.3080 274.8098 15.06496495 51673',
    },
  ],
  iss: [
    {
      name: 'ISS (ZARYA)',
      noradId: '25544',
      group: 'iss',
      tle1: '1 25544U 98067A   26199.61093418  .00009040  00000+0  16606-3 0  9991',
      tle2: '2 25544  51.6389 353.8734 0004383 121.3250 322.0110 15.50120590525592',
    },
  ],
}

export async function fetchAllTle(): Promise<SatelliteData[]> {
  return Object.values(SAMPLE_TLE).flat()
}
