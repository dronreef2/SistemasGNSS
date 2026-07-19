import { useRef, useMemo } from 'react';
import { useFrame } from '@react-three/fiber';
import * as THREE from 'three';

const EARTH_RADIUS = 6.371; // km * SCALE (1/1000)

// Textura procedural de dia — gradiente oceano/continente
function createDayTexture(): THREE.CanvasTexture {
  const canvas = document.createElement('canvas');
  canvas.width = 2048;
  canvas.height = 1024;
  const ctx = canvas.getContext('2d')!;

  // Oceano base
  const oceanGrad = ctx.createLinearGradient(0, 0, 0, canvas.height);
  oceanGrad.addColorStop(0, '#1a3a5c');
  oceanGrad.addColorStop(0.5, '#0d2137');
  oceanGrad.addColorStop(1, '#1a3a5c');
  ctx.fillStyle = oceanGrad;
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  // Continentes simplificados (silhuetas aproximadas)
  ctx.fillStyle = '#2d5a27';
  // América do Sul
  ctx.beginPath();
  ctx.ellipse(600, 650, 180, 280, 0.2, 0, Math.PI * 2);
  ctx.fill();
  // América do Norte
  ctx.beginPath();
  ctx.ellipse(500, 350, 220, 180, -0.3, 0, Math.PI * 2);
  ctx.fill();
  // Europa/África
  ctx.beginPath();
  ctx.ellipse(1100, 450, 160, 280, 0, 0, Math.PI * 2);
  ctx.fill();
  // Ásia
  ctx.beginPath();
  ctx.ellipse(1500, 350, 300, 200, 0.1, 0, Math.PI * 2);
  ctx.fill();
  // Austrália
  ctx.beginPath();
  ctx.ellipse(1700, 750, 120, 90, 0, 0, Math.PI * 2);
  ctx.fill();

  // Ice caps
  ctx.fillStyle = '#e8f4f8';
  ctx.beginPath();
  ctx.ellipse(1000, 50, 400, 60, 0, 0, Math.PI * 2);
  ctx.fill();
  ctx.beginPath();
  ctx.ellipse(1000, 970, 350, 50, 0, 0, Math.PI * 2);
  ctx.fill();

  const tex = new THREE.CanvasTexture(canvas);
  tex.colorSpace = THREE.SRGBColorSpace;
  return tex;
}

// Textura de noite — city lights
function createNightTexture(): THREE.CanvasTexture {
  const canvas = document.createElement('canvas');
  canvas.width = 2048;
  canvas.height = 1024;
  const ctx = canvas.getContext('2d')!;

  ctx.fillStyle = '#000000';
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  // Luzes de cidades (pontos aleatórios concentrados em latitudes médias)
  for (let i = 0; i < 3000; i++) {
    const x = Math.random() * canvas.width;
    const y = 200 + Math.random() * 600;
    const intensity = Math.random();
    const r = Math.floor(255 * (0.6 + 0.4 * intensity));
    const g = Math.floor(200 * (0.5 + 0.5 * intensity));
    const b = Math.floor(100 * intensity);
    ctx.fillStyle = `rgba(${r},${g},${b},${0.3 + 0.7 * intensity})`;
    ctx.beginPath();
    ctx.arc(x, y, 0.5 + Math.random() * 1.5, 0, Math.PI * 2);
    ctx.fill();
  }

  const tex = new THREE.CanvasTexture(canvas);
  tex.colorSpace = THREE.SRGBColorSpace;
  return tex;
}

// Textura de nuvens
function createCloudTexture(): THREE.CanvasTexture {
  const canvas = document.createElement('canvas');
  canvas.width = 2048;
  canvas.height = 1024;
  const ctx = canvas.getContext('2d')!;

  ctx.fillStyle = 'rgba(0,0,0,0)';
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  for (let i = 0; i < 200; i++) {
    const x = Math.random() * canvas.width;
    const y = Math.random() * canvas.height;
    const w = 50 + Math.random() * 200;
    const h = 20 + Math.random() * 60;
    const alpha = 0.1 + Math.random() * 0.25;

    const grad = ctx.createRadialGradient(x, y, 0, x, y, Math.max(w, h));
    grad.addColorStop(0, `rgba(255,255,255,${alpha})`);
    grad.addColorStop(1, 'rgba(255,255,255,0)');
    ctx.fillStyle = grad;
    ctx.beginPath();
    ctx.ellipse(x, y, w, h, Math.random() * Math.PI, 0, Math.PI * 2);
    ctx.fill();
  }

  const tex = new THREE.CanvasTexture(canvas);
  tex.colorSpace = THREE.SRGBColorSpace;
  return tex;
}

// Atmosfera glow shader
const atmosphereVertexShader = `
  varying vec3 vNormal;
  varying vec3 vPosition;
  void main() {
    vNormal = normalize(normalMatrix * normal);
    vPosition = (modelViewMatrix * vec4(position, 1.0)).xyz;
    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
  }
`;

const atmosphereFragmentShader = `
  varying vec3 vNormal;
  varying vec3 vPosition;
  uniform vec3 glowColor;
  uniform float intensity;
  void main() {
    float viewDot = 1.0 - dot(normalize(vNormal), vec3(0.0, 0.0, 1.0));
    viewDot = pow(viewDot, 3.0);
    float dist = length(vPosition);
    float fade = smoothstep(15.0, 5.0, dist);
    gl_FragColor = vec4(glowColor, viewDot * intensity * fade);
  }
`;

export function Earth() {
  const earthRef = useRef<THREE.Mesh>(null);
  const cloudsRef = useRef<THREE.Mesh>(null);
  const nightRef = useRef<THREE.Mesh>(null);

  const dayTex = useMemo(() => createDayTexture(), []);
  const nightTex = useMemo(() => createNightTexture(), []);
  const cloudTex = useMemo(() => createCloudTexture(), []);

  useFrame((_, delta) => {
    if (cloudsRef.current) {
      cloudsRef.current.rotation.y += delta * 0.005;
    }
  });

  return (
    <group>
      {/* Terra base (dia) */}
      <mesh ref={earthRef}>
        <sphereGeometry args={[EARTH_RADIUS, 128, 64]} />
        <meshStandardMaterial
          map={dayTex}
          roughness={0.8}
          metalness={0.1}
        />
      </mesh>

      {/* Camada de noite */}
      <mesh ref={nightRef} scale={[1.005, 1.005, 1.005]}>
        <sphereGeometry args={[EARTH_RADIUS, 128, 64]} />
        <meshBasicMaterial
          map={nightTex}
          transparent
          opacity={0.6}
          blending={THREE.AdditiveBlending}
          depthWrite={false}
        />
      </mesh>

      {/* Nuvens */}
      <mesh ref={cloudsRef} scale={[1.02, 1.02, 1.02]}>
        <sphereGeometry args={[EARTH_RADIUS, 128, 64]} />
        <meshStandardMaterial
          map={cloudTex}
          transparent
          opacity={0.4}
          depthWrite={false}
          blending={THREE.NormalBlending}
        />
      </mesh>

      {/* Atmosfera glow */}
      <mesh scale={[1.15, 1.15, 1.15]}>
        <sphereGeometry args={[EARTH_RADIUS, 64, 32]} />
        <shaderMaterial
          vertexShader={atmosphereVertexShader}
          fragmentShader={atmosphereFragmentShader}
          uniforms={{
            glowColor: { value: new THREE.Color('#4da6ff') },
            intensity: { value: 0.6 },
          }}
          transparent
          side={THREE.BackSide}
          depthWrite={false}
          blending={THREE.AdditiveBlending}
        />
      </mesh>

      {/* Luz ambiente sutil */}
      <ambientLight intensity={0.15} />

      {/* Sol */}
      <directionalLight
        position={[50, 20, 30]}
        intensity={2.0}
        color="#fff8e7"
        castShadow={false}
      />

      {/* Luz de preenchimento */}
      <directionalLight
        position={[-30, -10, -20]}
        intensity={0.3}
        color="#4466aa"
      />
    </group>
  );
}
