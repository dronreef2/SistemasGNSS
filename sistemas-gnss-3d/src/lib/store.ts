import { create } from 'zustand'
import { GROUP_CONFIGS, type SatelliteData, type SatelliteGroup, type SatellitePosition } from '../types'

interface AppState {
  satellites: SatelliteData[]
  loading: boolean
  error: string | null
  lastUpdated: Date | null
  activeGroups: Set<SatelliteGroup>
  searchQuery: string
  isPlaying: boolean
  speedMultiplier: number
  simulationTime: Date
  selectedSatellite: SatelliteData | null
  selectedPosition: SatellitePosition | null
  setSatellites: (satellites: SatelliteData[]) => void
  setLoading: (loading: boolean) => void
  setError: (error: string | null) => void
  setLastUpdated: (date: Date | null) => void
  toggleGroup: (group: SatelliteGroup) => void
  setSearchQuery: (query: string) => void
  setIsPlaying: (isPlaying: boolean) => void
  setSpeedMultiplier: (speed: number) => void
  setSimulationTime: (time: Date) => void
  selectSatellite: (satellite: SatelliteData | null) => void
  setSelectedPosition: (position: SatellitePosition | null) => void
}

const allGroups = new Set<SatelliteGroup>(GROUP_CONFIGS.map((group) => group.key))

export const useStore = create<AppState>((set) => ({
  satellites: [],
  loading: false,
  error: null,
  lastUpdated: null,
  activeGroups: allGroups,
  searchQuery: '',
  isPlaying: true,
  speedMultiplier: 1,
  simulationTime: new Date(),
  selectedSatellite: null,
  selectedPosition: null,
  setSatellites: (satellites) => set({ satellites }),
  setLoading: (loading) => set({ loading }),
  setError: (error) => set({ error }),
  setLastUpdated: (date) => set({ lastUpdated: date }),
  toggleGroup: (group) =>
    set((state) => {
      const next = new Set(state.activeGroups)
      if (next.has(group)) next.delete(group)
      else next.add(group)
      return { activeGroups: next }
    }),
  setSearchQuery: (query) => set({ searchQuery: query }),
  setIsPlaying: (isPlaying) => set({ isPlaying }),
  setSpeedMultiplier: (speed) => set({ speedMultiplier: speed }),
  setSimulationTime: (time) => set({ simulationTime: time }),
  selectSatellite: (satellite) => set({ selectedSatellite: satellite }),
  setSelectedPosition: (position) => set({ selectedPosition: position }),
}))
