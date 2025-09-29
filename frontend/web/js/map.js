import { state, on, setEstacao } from './state.js';

let map;
let layerGroup;

export function initMap(){
  map = L.map('map').setView([-14.2, -53.2], 4); // Brasil central aproximado
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 12,
    attribution: '&copy; OpenStreetMap'
  }).addTo(map);
  layerGroup = L.layerGroup().addTo(map);

  on('estacoes:loaded', estacoes => {
    layerGroup.clearLayers();
    estacoes.forEach(e => {
      if(!e.latitude || !e.longitude) return;
      const marker = L.marker([e.latitude, e.longitude]);
      marker.bindPopup(`<strong>${e.codigo}</strong><br/>${e.nome||''}`);
      marker.on('click', () => setEstacao(e.codigo));
      marker.addTo(layerGroup);
    });
  });

  on('estacao:changed', codigo => {
    const est = state.estacoes.find(x=>x.codigo===codigo);
    if(est) {
      map.setView([est.latitude, est.longitude], 8);
    }
  });
}
