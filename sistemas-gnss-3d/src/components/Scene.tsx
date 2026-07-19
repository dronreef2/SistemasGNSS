import { useFrame } from '@react-three/fiber'
import { Canvas } from '@react-three/fiber'
import { OrbitControls, Stars } from '@react-three/drei'
import { useEffect, useMemo } from 'react'
import { useStore } from '../lib/store'

function Earth() {
  return (
    <mesh>
      <sphereGeometry args={[1.8, 48, 48]} />
      <meshStandardMaterial color="#1a4b8f" roughness={0.8} metalness={0.1} />
    </mesh>
  )
}

function SatelliteLayer() {
  const satellites = useStore((s) => s.satellites)
  const activeGroups = useStore((s) => s.activeGroups)
  const simulationTime = useStore((s) => s.simulationTime)
  const selectedSatellite = useStore((s) => s.selectedSatellite)
  const setSelectedPosition = useStore((s) => s.setSelectedPosition)

  const visible = useMemo(
    () => satellites.filter((sat) => activeGroups.has(sat.group)),
    [satellites, activeGroups],
  )

  useEffect(() => {
    if (!selectedSatellite) {
      setSelectedPosition(null)
      return
    }
    const t = simulationTime.getTime() / 1000
    setSelectedPosition({
      lat: Math.sin(t / 2000) * 45,
      lon: ((t / 50) % 360) - 180,
      alt: 550,
      velocity: 7.66,
      inclination: 53.2,
      period: 95.2,
    })
  }, [selectedSatellite, simulationTime, setSelectedPosition])

  useFrame(() => {
    if (!selectedSatellite) return
    const t = Date.now() / 1000
    setSelectedPosition({
      lat: Math.sin(t / 2000) * 45,
      lon: ((t / 50) % 360) - 180,
      alt: 550,
      velocity: 7.66,
      inclination: 53.2,
      period: 95.2,
    })
  })

  return (
    <group>
      {visible.slice(0, 500).map((sat, idx) => {
        const angle = (idx / Math.max(visible.length, 1)) * Math.PI * 2
        const radius = 2.3 + (idx % 9) * 0.03
        return (
          <mesh key={sat.noradId} position={[Math.cos(angle) * radius, Math.sin(angle * 0.7) * 0.9, Math.sin(angle) * radius]}>
            <sphereGeometry args={[0.02, 8, 8]} />
            <meshBasicMaterial color={sat.group === 'iss' ? '#ffffff' : '#00e5ff'} />
          </mesh>
        )
      })}
    </group>
  )
}

function TimeDriver() {
  const isPlaying = useStore((s) => s.isPlaying)
  const speedMultiplier = useStore((s) => s.speedMultiplier)
  const setSimulationTime = useStore((s) => s.setSimulationTime)

  useFrame((_, delta) => {
    if (!isPlaying) return
    setSimulationTime(new Date(Date.now() + delta * speedMultiplier * 1000))
  })

  return null
}

export function Scene() {
  return (
    <Canvas camera={{ position: [0, 3.5, 5], fov: 50 }}>
      <color attach="background" args={['#050a14']} />
      <ambientLight intensity={0.7} />
      <directionalLight position={[5, 6, 3]} intensity={1.1} />
      <Earth />
      <SatelliteLayer />
      <Stars radius={120} depth={60} count={4000} factor={4} saturation={0} fade speed={0.4} />
      <OrbitControls enablePan={false} minDistance={2.2} maxDistance={12} />
      <TimeDriver />
    </Canvas>
  )
}
