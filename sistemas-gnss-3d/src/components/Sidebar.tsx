import { useMemo } from 'react'
import { useStore } from '../lib/store'
import { GROUP_CONFIGS, GROUP_CONFIG_MAP } from '../types'
import type { SatelliteGroup } from '../types'
import {
  Play,
  Pause,
  RotateCcw,
  Search,
  Satellite,
  ChevronRight,
  Clock,
  Zap,
  Globe,
  X,
} from 'lucide-react'

const SPEED_OPTIONS = [
  { label: '1×', value: 1 },
  { label: '10×', value: 10 },
  { label: '60×', value: 60 },
  { label: '600×', value: 600 },
]

export function Sidebar() {
  const {
    satellites,
    loading,
    error,
    lastUpdated,
    activeGroups,
    searchQuery,
    isPlaying,
    speedMultiplier,
    simulationTime,
    selectedSatellite,
    selectedPosition,
    toggleGroup,
    setSearchQuery,
    setIsPlaying,
    setSpeedMultiplier,
    setSimulationTime,
    selectSatellite,
  } = useStore()

  const filteredSats = useMemo(() => {
    const q = searchQuery.toLowerCase()
    return satellites.filter((s) => {
      if (!activeGroups.has(s.group)) return false
      if (q && !s.name.toLowerCase().includes(q) && s.noradId !== q) return false
      return true
    })
  }, [satellites, activeGroups, searchQuery])

  const groupCounts = useMemo(() => {
    const counts = new Map<SatelliteGroup, number>()
    satellites.forEach((s) => {
      counts.set(s.group, (counts.get(s.group) || 0) + 1)
    })
    return counts
  }, [satellites])

  const formatTime = (d: Date) => {
    return d.toLocaleTimeString('pt-BR', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    })
  }

  const formatDate = (d: Date) => {
    return d.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    })
  }

  return (
    <div className="flex flex-col h-full w-full max-w-[380px] bg-[#0a0f1a]/90 backdrop-blur-xl border-r border-white/5 text-white overflow-hidden">
      <div className="p-5 border-b border-white/5">
        <div className="flex items-center gap-3 mb-1">
          <Globe className="w-6 h-6 text-cyan-400" />
          <h1 className="text-xl font-bold tracking-tight bg-gradient-to-r from-cyan-400 to-blue-500 bg-clip-text text-transparent">
            Sistemas GNSS
          </h1>
        </div>
        <p className="text-xs text-white/40 font-mono">Visualização Orbital 3D em Tempo Real</p>
      </div>

      <div className="p-4 border-b border-white/5 space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2 text-sm text-white/60">
            <Clock className="w-4 h-4" />
            <span className="font-mono">{formatTime(simulationTime)}</span>
          </div>
          <div className="flex items-center gap-1">
            <button
              onClick={() => setIsPlaying(!isPlaying)}
              className="p-2 rounded-lg bg-white/5 hover:bg-white/10 transition-colors"
            >
              {isPlaying ? <Pause className="w-4 h-4 text-cyan-400" /> : <Play className="w-4 h-4 text-cyan-400" />}
            </button>
            <button
              onClick={() => setSimulationTime(new Date())}
              className="p-2 rounded-lg bg-white/5 hover:bg-white/10 transition-colors"
            >
              <RotateCcw className="w-4 h-4 text-white/60" />
            </button>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Zap className="w-3 h-3 text-white/30" />
          <div className="flex gap-1">
            {SPEED_OPTIONS.map((opt) => (
              <button
                key={opt.value}
                onClick={() => setSpeedMultiplier(opt.value)}
                className={`px-2.5 py-1 rounded text-xs font-mono transition-all ${
                  speedMultiplier === opt.value
                    ? 'bg-cyan-500/20 text-cyan-400 border border-cyan-500/30'
                    : 'bg-white/5 text-white/40 hover:bg-white/10'
                }`}
              >
                {opt.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className="p-4 border-b border-white/5">
        <h3 className="text-xs font-semibold text-white/30 uppercase tracking-wider mb-3">Constelações</h3>
        <div className="space-y-1.5">
          {GROUP_CONFIGS.map((group) => {
            const isActive = activeGroups.has(group.key)
            const count = groupCounts.get(group.key) || 0
            return (
              <button
                key={group.key}
                onClick={() => toggleGroup(group.key)}
                className={`w-full flex items-center justify-between px-3 py-2 rounded-lg transition-all ${
                  isActive ? 'bg-white/5' : 'opacity-40'
                } hover:bg-white/10`}
              >
                <div className="flex items-center gap-2.5">
                  <div className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: group.color }} />
                  <span className="text-sm text-white/80">{group.label}</span>
                </div>
                <span className="text-xs font-mono text-white/30">{count}</span>
              </button>
            )
          })}
        </div>
      </div>

      <div className="p-4 border-b border-white/5">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-white/30" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Buscar satélite ou NORAD..."
            className="w-full pl-9 pr-3 py-2 bg-white/5 border border-white/10 rounded-lg text-sm text-white placeholder:text-white/20 focus:outline-none focus:border-cyan-500/30 transition-colors font-mono"
          />
        </div>
        <div className="mt-2 text-xs text-white/30 font-mono">{filteredSats.length} satélites visíveis</div>
      </div>

      <div className="flex-1 overflow-y-auto p-2 space-y-0.5">
        {loading && (
          <div className="flex items-center justify-center py-8">
            <div className="w-5 h-5 border-2 border-cyan-400/30 border-t-cyan-400 rounded-full animate-spin" />
          </div>
        )}

        {error && (
          <div className="p-3 mx-2 rounded-lg bg-red-500/10 border border-red-500/20 text-xs text-red-400">{error}</div>
        )}

        {filteredSats.map((sat) => {
          const config = GROUP_CONFIG_MAP[sat.group]
          const isSelected = selectedSatellite?.noradId === sat.noradId
          return (
            <button
              key={sat.noradId}
              onClick={() => selectSatellite(isSelected ? null : sat)}
              className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all text-left ${
                isSelected
                  ? 'bg-cyan-500/10 border border-cyan-500/20'
                  : 'hover:bg-white/5 border border-transparent'
              }`}
            >
              <Satellite className="w-4 h-4 shrink-0" style={{ color: config.color }} />
              <div className="flex-1 min-w-0">
                <div className="text-sm text-white/80 truncate">{sat.name}</div>
                <div className="text-xs text-white/30 font-mono">NORAD {sat.noradId}</div>
              </div>
              <ChevronRight
                className={`w-4 h-4 text-white/20 shrink-0 transition-transform ${
                  isSelected ? 'rotate-90 text-cyan-400' : ''
                }`}
              />
            </button>
          )
        })}
      </div>

      {selectedSatellite && selectedPosition && (
        <div className="border-t border-white/5 p-4 bg-white/[0.02]">
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 rounded-full" style={{ backgroundColor: GROUP_CONFIG_MAP[selectedSatellite.group].color }} />
              <span className="text-sm font-semibold text-white/90 truncate max-w-[200px]">{selectedSatellite.name}</span>
            </div>
            <button onClick={() => selectSatellite(null)} className="p-1 rounded hover:bg-white/10 transition-colors">
              <X className="w-4 h-4 text-white/40" />
            </button>
          </div>

          <div className="grid grid-cols-2 gap-2 text-xs font-mono">
            <div className="bg-white/5 rounded-lg p-2.5">
              <div className="text-white/30 mb-1">Altitude</div>
              <div className="text-cyan-400">
                {selectedPosition.alt.toFixed(1)} <span className="text-white/30">km</span>
              </div>
            </div>
            <div className="bg-white/5 rounded-lg p-2.5">
              <div className="text-white/30 mb-1">Velocidade</div>
              <div className="text-cyan-400">
                {selectedPosition.velocity.toFixed(2)} <span className="text-white/30">km/s</span>
              </div>
            </div>
            <div className="bg-white/5 rounded-lg p-2.5">
              <div className="text-white/30 mb-1">Inclinação</div>
              <div className="text-cyan-400">{selectedPosition.inclination.toFixed(2)}°</div>
            </div>
            <div className="bg-white/5 rounded-lg p-2.5">
              <div className="text-white/30 mb-1">Período</div>
              <div className="text-cyan-400">
                {selectedPosition.period.toFixed(1)} <span className="text-white/30">min</span>
              </div>
            </div>
          </div>

          <div className="mt-2 bg-white/5 rounded-lg p-2.5 text-xs font-mono">
            <div className="text-white/30 mb-1">Posição</div>
            <div className="text-white/60">
              {selectedPosition.lat.toFixed(4)}°, {selectedPosition.lon.toFixed(4)}°
            </div>
          </div>

          <div className="mt-2 text-[10px] text-white/20 font-mono">
            NORAD ID: {selectedSatellite.noradId} · {selectedSatellite.group}
          </div>
        </div>
      )}

      {lastUpdated && (
        <div className="p-3 border-t border-white/5 text-[10px] text-white/20 font-mono text-center">
          TLE atualizado: {formatDate(lastUpdated)} {formatTime(lastUpdated)}
        </div>
      )}
    </div>
  )
}
