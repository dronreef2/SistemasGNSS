import { useEffect } from 'react'
import { Scene } from './components/Scene'
import { Sidebar } from './components/Sidebar'
import { Legend } from './components/Legend'
import { FpsCounter } from './components/FpsCounter'
import { useStore } from './lib/store'
import { fetchAllTle } from './lib/tle'

function App() {
  const setSatellites = useStore((s) => s.setSatellites)
  const setLoading = useStore((s) => s.setLoading)
  const setError = useStore((s) => s.setError)
  const setLastUpdated = useStore((s) => s.setLastUpdated)

  useEffect(() => {
    const load = async () => {
      setLoading(true)
      try {
        const sats = await fetchAllTle()
        setSatellites(sats)
        setLastUpdated(new Date())
        setError(null)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Erro ao carregar TLE')
      } finally {
        setLoading(false)
      }
    }

    load()

    const interval = setInterval(load, 2 * 60 * 60 * 1000)
    return () => clearInterval(interval)
  }, [setSatellites, setLoading, setError, setLastUpdated])

  return (
    <div className="flex h-screen w-screen bg-[#050a14] overflow-hidden font-['Inter',sans-serif]">
      <div className="w-[380px] shrink-0 h-full">
        <Sidebar />
      </div>
      <div className="flex-1 relative">
        <Scene />
        <Legend />
        <FpsCounter />
      </div>
    </div>
  )
}

export default App
