const BASE = '/api/v1'; // ajustar se backend estiver em rota diferente

async function fetchJson(url){
  const r = await fetch(url);
  if(!r.ok) throw new Error('HTTP '+r.status);
  return r.json();
}

export async function getEstacoes(){
  try {
    return await fetchJson(`${BASE}/estacoes`);
  } catch(e){
    console.warn('Falha ao carregar estacoes, usando fallback.', e);
    return [
      { codigo:'ALAR', nome:'Alagoinhas', latitude:-12.135, longitude:-38.423 },
      { codigo:'BRAZ', nome:'Brasília', latitude:-15.793, longitude:-47.882 },
    ];
  }
}

export async function getMetadados(codigo){
  return fetchJson(`${BASE}/estacoes/${codigo}/metadados`);
}

export async function getSnr(codigo, ano, dia, max){
  return fetchJson(`${BASE}/estacoes/${codigo}/snr?ano=${ano}&dia=${dia}&max=${max}`);
}

export async function getPosicoes(codigo, ano, dia, max){
  return fetchJson(`${BASE}/estacoes/${codigo}/posicoes?ano=${ano}&dia=${dia}&max=${max}`);
}
