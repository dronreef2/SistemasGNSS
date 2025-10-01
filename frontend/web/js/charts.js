let snrChart, posChart;

export function renderCharts(snrData, posData){
  const ctx1 = document.getElementById('snrChart');
  const ctx2 = document.getElementById('posChart');
  const snrSamples = (snrData?.samples||[]).slice(0,300);
  const posSamples = (posData?.samples||[]).slice(0,300);

  // Show message if no data
  const snrContainer = ctx1.parentElement;
  const posContainer = ctx2.parentElement;
  
  let snrMsg = snrContainer.querySelector('.no-data-message');
  let posMsg = posContainer.querySelector('.no-data-message');
  
  if (snrSamples.length === 0) {
    if (!snrMsg) {
      snrMsg = document.createElement('div');
      snrMsg.className = 'no-data-message';
      snrMsg.style.textAlign = 'center';
      snrMsg.style.padding = '1rem';
      snrMsg.style.color = '#6b7280';
      snrMsg.textContent = 'Sem dados de SNR';
      ctx1.parentElement.insertBefore(snrMsg, ctx1);
    }
    snrMsg.style.display = 'block';
    ctx1.style.display = 'none';
  } else {
    if (snrMsg) snrMsg.style.display = 'none';
    ctx1.style.display = 'block';
  }
  
  if (posSamples.length === 0) {
    if (!posMsg) {
      posMsg = document.createElement('div');
      posMsg.className = 'no-data-message';
      posMsg.style.textAlign = 'center';
      posMsg.style.padding = '1rem';
      posMsg.style.color = '#6b7280';
      posMsg.textContent = 'Sem dados de posição';
      ctx2.parentElement.insertBefore(posMsg, ctx2);
    }
    posMsg.style.display = 'block';
    ctx2.style.display = 'none';
  } else {
    if (posMsg) posMsg.style.display = 'none';
    ctx2.style.display = 'block';
  }

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
