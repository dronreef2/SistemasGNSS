import { Canvas, useFrame } from '@react-three/fiber';
import { OrbitControls } from '@react-three/drei';
import { Earth } from './Earth';
import { Satellites } from './Satellites';
import { Stars } from './Stars';
import { useStore } from '../lib/store';
import * as THREE from 'three';

function SceneContent() {
  const isPlaying = useStore((s) => s.isPlaying);
  const speedMultiplier = useStore((s) => s.speedMultiplier);
  const advanceTime = useStore((s) => s.advanceTime);
  useFrame((_, delta) => {
    if (!isPlaying) return;
    // Avançar tempo de simulação
    const simDeltaMs = delta * 1000 * speedMultiplier;
    advanceTime(simDeltaMs);
  });

  return (
    <>
      <ambientLight intensity={0.05} />
      <Earth />
      <Satellites />
      <Stars />
      <OrbitControls
        enablePan={false}
        minDistance={8}
        maxDistance={60}
        autoRotate
        autoRotateSpeed={0.1}
        enableDamping
        dampingFactor={0.05}
      />
    </>
  );
}

export function Scene() {
  return (
    <Canvas
      camera={{ position: [15, 8, 15], fov: 45, near: 0.1, far: 500 }}
      gl={{
        antialias: true,
        toneMapping: THREE.ACESFilmicToneMapping,
        toneMappingExposure: 1.2,
      }}
      style={{ background: '#050a14' }}
    >
      <SceneContent />
    </Canvas>
  );
}
