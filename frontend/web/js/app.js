import { initMap } from './map.js';
import { initUI } from './ui.js';
import { getEstacoes } from './apiClient.js';
import { setEstacoes } from './state.js';

async function bootstrap(){
  initMap();
  initUI();
  const lista = await getEstacoes();
  setEstacoes(lista);
  // Pre-fill data inputs com hoje
  document.getElementById('btnHoje').click();
}

bootstrap();
