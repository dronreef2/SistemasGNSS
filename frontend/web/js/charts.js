let snrChart, posChart;

export function renderCharts(snrData, posData){
  const ctx1 = document.getElementById('snrChart');
  const ctx2 = document.getElementById('posChart');
  const snrSamples = (snrData?.samples||[]).slice(0,300);
  const posSamples = (posData?.samples||[]).slice(0,300);

  // Se não há dados, mostrar mensagem
  if (snrSamples.length === 0 && posSamples.length === 0) {
    destroyCharts();
    ctx1.style.display = 'none';
    ctx2.style.display = 'none';
    
    const panel = document.querySelector('.panel');
    let noDataMsg = panel.querySelector('.no-data-message');
    if (!noDataMsg) {
      noDataMsg = document.createElement('div');
      noDataMsg.className = 'no-data-message';
      noDataMsg.style.textAlign = 'center';
      noDataMsg.style.padding = '2rem';
      noDataMsg.style.color = '#6b7280';
      panel.appendChild(noDataMsg);
    }
    noDataMsg.textContent = 'Nenhum dado disponível para a data selecionada';
    return;
  }

  // Remover mensagem de "sem dados" se existir
  const noDataMsg = document.querySelector('.no-data-message');
  if (noDataMsg) {
    noDataMsg.remove();
  }
  ctx1.style.display = 'block';
  ctx2.style.display = 'block';

  const snrLabels = snrSamples.map(s=> new Date(s.epoch).toLocaleTimeString());
  const snrValues = snrSamples.map(s=> s.snr || 0);

  const posLabels = posSamples.map(s=> new Date(s.epoch).toLocaleTimeString());
  const latValues = posSamples.map(s=> s.lat||0);
  const lonValues = posSamples.map(s=> s.lon||0);

  if(!snrChart){
    snrChart = new Chart(ctx1, {
      type:'line',
      data:{ labels: snrLabels, datasets:[ { label:'SNR', data: snrValues, borderColor:'#2563eb', tension:0.2, pointRadius:0 } ] },
      options:{ scales:{ x:{display:false} }, animation:false, responsive:true, plugins:{legend:{display:true}} }
    });
  } else {
    snrChart.data.labels = snrLabels; snrChart.data.datasets[0].data = snrValues; snrChart.update();
  }

  if(!posChart){
    posChart = new Chart(ctx2, {
      type:'line',
      data:{ labels: posLabels, datasets:[
        { label:'Lat', data: latValues, borderColor:'#10b981', tension:0.2, pointRadius:0 },
        { label:'Lon', data: lonValues, borderColor:'#f59e0b', tension:0.2, pointRadius:0 }
      ]},
      options:{ scales:{ x:{display:false} }, animation:false, responsive:true, plugins:{legend:{display:true}} }
    });
  } else {
    posChart.data.labels = posLabels;
    posChart.data.datasets[0].data = latValues;
    posChart.data.datasets[1].data = lonValues;
    posChart.update();
  }
}

// Função para reconstruir gráficos (Fase 6)
export function rebuildCharts(estacao, ano, dia) {
  destroyCharts();
  // Esta função será chamada via loadSeries() quando dados mudarem
}

function destroyCharts() {
  if (snrChart) {
    snrChart.destroy();
    snrChart = null;
  }
  if (posChart) {
    posChart.destroy();
    posChart = null;
  }
}
