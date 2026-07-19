import { useRef, useState, useEffect } from 'react'

export function FpsCounter() {
  const [fps, setFps] = useState(0)
  const frameCount = useRef(0)
  const lastTime = useRef(performance.now())

  useEffect(() => {
    let raf: number

    const loop = () => {
      frameCount.current++
      const now = performance.now()
      const elapsed = now - lastTime.current

      if (elapsed >= 1000) {
        setFps(Math.round((frameCount.current * 1000) / elapsed))
        frameCount.current = 0
        lastTime.current = now
      }

      raf = requestAnimationFrame(loop)
    }

    raf = requestAnimationFrame(loop)
    return () => cancelAnimationFrame(raf)
  }, [])

  return (
    <div className="absolute top-6 right-6 bg-[#0a0f1a]/80 backdrop-blur-xl border border-white/5 rounded-lg px-3 py-1.5">
      <span className="text-[10px] font-mono text-white/30">
        FPS:{' '}
        <span className={fps >= 55 ? 'text-green-400' : fps >= 30 ? 'text-yellow-400' : 'text-red-400'}>{fps}</span>
      </span>
    </div>
  )
}
