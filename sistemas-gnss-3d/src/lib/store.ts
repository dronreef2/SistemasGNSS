import { create } from 'zustand';
import type { SatelliteData, SatelliteGroup, SatellitePosition } from '../types';

interface AppState {
  // Dados
  satellites: SatelliteData[];
  loading: boolean;
  error: string | null;
  lastUpdated: Date | null;

  // Filtros
  activeGroups: Set<SatelliteGroup>;
  searchQuery: string;

  // Tempo
  simulationTime: Date;
  isPlaying: boolean;
  speedMultiplier: number;

  // Seleção
  selectedSatellite: SatelliteData | null;
  selectedPosition: SatellitePosition | null;

  // Ações
  setSatellites: (sats: SatelliteData[]) => void;
  setLoading: (v: boolean) => void;
  setError: (e: string | null) => void;
  setLastUpdated: (d: Date) => void;
  toggleGroup: (g: SatelliteGroup) => void;
  setSearchQuery: (q: string) => void;
  setSimulationTime: (d: Date) => void;
  setIsPlaying: (v: boolean) => void;
  setSpeedMultiplier: (s: number) => void;
  selectSatellite: (sat: SatelliteData | null) => void;
  setSelectedPosition: (pos: SatellitePosition | null) => void;
  advanceTime: (deltaMs: number) => void;
}

export const useStore = create<AppState>((set) => ({
  satellites: [],
  loading: true,
  error: null,
  lastUpdated: null,

  activeGroups: new Set(['GPS', 'GLONASS', 'Galileo', 'BeiDou', 'Starlink', 'ISS']),
  searchQuery: '',

  simulationTime: new Date(),
  isPlaying: true,
  speedMultiplier: 1,

  selectedSatellite: null,
  selectedPosition: null,

  setSatellites: (sats) => set({ satellites: sats }),
  setLoading: (v) => set({ loading: v }),
  setError: (e) => set({ error: e }),
  setLastUpdated: (d) => set({ lastUpdated: d }),

  toggleGroup: (g) =>
    set((state) => {
      const next = new Set(state.activeGroups);
      if (next.has(g)) next.delete(g);
      else next.add(g);
      return { activeGroups: next };
    }),

  setSearchQuery: (q) => set({ searchQuery: q }),

  setSimulationTime: (d) => set({ simulationTime: d }),
  setIsPlaying: (v) => set({ isPlaying: v }),
  setSpeedMultiplier: (s) => set({ speedMultiplier: s }),

  selectSatellite: (sat) => set({ selectedSatellite: sat, selectedPosition: null }),
  setSelectedPosition: (pos) => set({ selectedPosition: pos }),

  advanceTime: (deltaMs) =>
    set((state) => ({
      simulationTime: new Date(
        state.simulationTime.getTime() + deltaMs * state.speedMultiplier
      ),
    })),
}));
