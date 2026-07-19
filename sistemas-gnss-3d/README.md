# 🛰️ Sistemas GNSS — Visualização Orbital 3D

Aplicação web **frontend-only** para visualização interativa em tempo real de satélites GNSS e mega-constelações orbitando a Terra em 3D.

> 🌐 **Live Demo**: [sistemas-gnss-3d.vercel.app](https://sistemas-gnss-3d.vercel.app) *(exemplo)*

---

## 🚀 Como rodar localmente

```bash
# Clonar o repositório
git clone https://github.com/dronreef2/SistemasGNSS.git
cd SistemasGNSS/sistemas-gnss-3d

# Instalar dependências
npm install

# Iniciar servidor de desenvolvimento
npm run dev
```

Acesse `http://localhost:5173`

---

## 🏗️ Stack Tecnológica

| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| **React** | 19.x | UI declarativa |
| **TypeScript** | 5.8 | Tipagem estática |
| **Vite** | 6.x | Build tool |
| **Three.js** | 0.175 | Renderização 3D |
| **React Three Fiber** | 9.x | Three.js declarativo |
| **@react-three/drei** | 10.x | Helpers 3D |
| **satellite.js** | 5.x | Propagação SGP4 |
| **Zustand** | 5.x | Estado global |
| **Tailwind CSS** | 4.x | Estilização |
| **Lucide React** | 0.475 | Ícones |

---

## 📡 Fonte de Dados Orbitais

TLEs (Two-Line Elements) consumidos diretamente da **CelesTrak** (sem API key):

| Grupo | URL CelesTrak |
|-------|---------------|
| GPS (NAVSTAR) | `gp.php?GROUP=gps-ops` |
| GLONASS | `gp.php?GROUP=glo-ops` |
| Galileo | `gp.php?GROUP=galileo` |
| BeiDou | `gp.php?GROUP=beidou` |
| Starlink | `gp.php?GROUP=starlink` |
| ISS | `gp.php?CATNR=25544` |

### Estratégia de Cache

- **Cache**: `localStorage` com TTL de **2 horas**
- **Fallback**: Se offline/falha, usa cache expirado
- **Refetch**: Automático a cada 2h em background

---

## 🎯 Funcionalidades

- ✅ **Terra 3D** — texturas diurna/noturna procedurais, nuvens rotativas, glow atmosférico via shader customizado
- ✅ **Satélites** — `InstancedMesh` para performance + sprites glow por constelação
- ✅ **Trilhas Orbitais** — 60 pontos propagados para satélite selecionado
- ✅ **Footprint** — Anel aproximado no solo (satélite selecionado)
- ✅ **Estrelas** — 4000 pontos com variação de tamanho
- ✅ **Controles de Tempo** — play/pause, reset, 1×/10×/60×/600×
- ✅ **Filtros** — por constelação + busca por nome/NORAD
- ✅ **Detalhes** — altitude, velocidade, inclinação, período, posição geodésica
- ✅ **FPS** — contador em tempo real
- ✅ **Design** — tema escuro futurista com acentos cyan

---

## ⚡ Otimizações de Performance

| Técnica | Impacto |
|---------|---------|
| `InstancedMesh` | Todos os satélites em 1 draw call |
| Sprites glow | Bloom via canvas texturizado (sem post-processing pesado) |
| Propagação frame-a-frame | Sem polling, cálculo SGP4 no cliente |
| Frustum culling | Objetos fora da câmera não processados |
| `useMemo` | Evita recálculos desnecessários |
| Zustand selectors | Re-render seletiva |

---

## 🏛️ Arquitetura

```
sistemas-gnss-3d/
├── .github/workflows/deploy.yml   # CI/CD GitHub Pages
├── public/
├── src/
│   ├── components/
│   │   ├── Earth.tsx              # Terra 3D + atmosfera + shader glow
│   │   ├── Satellites.tsx         # InstancedMesh + sprites + trilhas + footprint
│   │   ├── Stars.tsx              # Background estrelado (4000 pontos)
│   │   ├── Scene.tsx              # Canvas R3F + loop de simulação temporal
│   │   ├── Sidebar.tsx            # Painel lateral completo (filtros, busca, lista, detalhes)
│   │   ├── Legend.tsx             # Legenda flutuante de cores
│   │   └── FpsCounter.tsx         # Contador de FPS
│   ├── lib/
│   │   ├── tle.ts                 # Fetch TLE CelesTrak + parse + propagação SGP4 + cache
│   │   └── store.ts               # Estado global Zustand (tempo, filtros, seleção)
│   ├── types/
│   │   └── index.ts               # Tipos TypeScript + configurações de grupo
│   ├── App.tsx                    # Composição principal + inicialização TLE
│   ├── main.tsx                   # Entry point React
│   └── index.css                  # Tailwind + estilos globais
├── package.json
├── vite.config.ts
├── tsconfig.json
├── vercel.json                    # Config deploy Vercel
├── netlify.toml                   # Config deploy Netlify
└── README.md
```

---

## 🌐 Deploy Gratuito

### Opção 1: Vercel (Recomendado)

1. Fork este repositório
2. Acesse [vercel.com](https://vercel.com) → "Add New Project"
3. Importe o repo → selecione a pasta `sistemas-gnss-3d`
4. Framework preset: **Vite**
5. Deploy!

> URL será: `https://<seu-projeto>.vercel.app`

### Opção 2: Netlify

1. Fork este repositório
2. Acesse [netlify.com](https://netlify.com) → "Add new site" → "Import from Git"
3. Selecione o repo → configure:
   - **Build command**: `npm run build`
   - **Publish directory**: `dist`
4. Deploy!

> URL será: `https://<seu-site>.netlify.app`

### Opção 3: GitHub Pages

1. Fork este repositório
2. Vá em **Settings → Pages**
3. Source: **GitHub Actions**
4. O workflow `.github/workflows/deploy.yml` já está configurado
5. Faça push para `main` → deploy automático

> URL será: `https://<seu-usuario>.github.io/SistemasGNSS/`

---

## ⚠️ Limitações Conhecidas

| Limitação | Detalhe |
|-----------|---------|
| Texturas | Procedurais (canvas) — não são fotos reais da NASA |
| Footprint | Aproximação baseada em altitude média orbital |
| Trilhas | Amostras discretas (60 pts) — não curvas contínuas |
| Geostacionárias | Sem precisão de sub-ponto |
| CORS | CelesTrak pode bloquear em alguns ambientes |
| Mobile | Layout otimizado para desktop (sidebar fixa) |

---

## 🔮 Próximos Passos

- [ ] Texturas reais NASA (Blue Marble)
- [ ] Post-processing bloom real (@react-three/postprocessing)
- [ ] Órbitas 3D contínuas (Catmull-Rom splines)
- [ ] Modo "seguir satélite" (câmera orbitando)
- [ ] Predição de passagens visíveis do solo
- [ ] Exportação RINEX
- [ ] Integração com backend SistemasGNSS
- [ ] Layout responsivo para mobile

---

## 📝 Licença

MIT — Projeto educacional.

---

<p align="center">
  <sub>Feito com 🛰️ para a comunidade GNSS</sub>
</p>
