import { state, on, setEstacao, setData } from './state.js';
import { getMetadados, getSnr, getPosicoes } from './apiClient.js';
import { renderCharts } from './charts.js';
import { pad, validateDate, dateToJulianDay } from './utils.js';

const estacaoSelect = document.getElementById('estacaoSelect');
const anoInput = document.getElementById('anoInput');
const diaInput = document.getElementById('diaInput');
const btnHoje = document.getElementById('btnHoje');
const btnOntem = document.getElementById('btnOntem');
const btnRelatorio = document.getElementById('btnRelatorio');
const btnRinex2 = document.getElementById('btnRinex2');
const metadadosEl = document.getElementById('metadados');

let validationMessageEl = null;

function showValidationMessage(message) {
  if (!validationMessageEl) {
    validationMessageEl = document.createElement('div');
    validationMessageEl.className = 'validation-message';
    validationMessageEl.style.color = '#dc2626';
    validationMessageEl.style.fontSize = '0.875rem';
    validationMessageEl.style.marginTop = '0.5rem';
    const datePickerSection = document.querySelector('.date-picker');
    const quickButtons = datePickerSection.querySelector('.quick-buttons');
    datePickerSection.insertBefore(validationMessageEl, quickButtons);
  }
  validationMessageEl.textContent = message;
  validationMessageEl.style.display = message ? 'block' : 'none';
}

function updateDownloadButtons(){
  const { estacaoSelecionada, data:{ano,dia} } = state;
  const validation = validateDate(ano, dia);
  const ok = estacaoSelecionada && validation.valid;
  
  btnRelatorio.disabled = !estacaoSelecionada;
  btnRinex2.disabled = !ok;
  
  if(estacaoSelecionada){
    btnRelatorio.onclick = () => window.open(`/api/v1/rbmc/${estacaoSelecionada}/relatorio`, '_blank');
  }
  if(ok){
    btnRinex2.onclick = () => window.open(`/api/v1/rbmc/${estacaoSelecionada}/rinex2/${ano}/${pad(dia,3)}`, '_blank');
  }
}

function renderMetadados(md){
  metadadosEl.innerHTML = '';
  if(!md){
    metadadosEl.innerHTML = '<div class="placeholder">Carregando...</div>';
    return;
  }
  
  // Derive status from ultimaObservacao
  let statusText = '-';
  if (md.ultimaObservacao) {
    try {
      const lastObs = new Date(md.ultimaObservacao);
      const now = new Date();
      const diffHours = (now - lastObs) / (1000 * 60 * 60);
      if (diffHours < 2) {
        statusText = '🟢 ONLINE (< 2h)';
      } else if (diffHours < 24) {
        statusText = '🟡 RECENTE (< 24h)';
      } else {
        statusText = '🔴 OFFLINE (> 24h)';
      }
    } catch(e) {
      statusText = '-';
    }
  }
  
  const html = [
    `<div class="item"><strong>Receptor:</strong> ${md.receptor||'-'}</div>`,
    `<div class="item"><strong>Antena:</strong> ${md.antena||'-'}</div>`,
    `<div class="item"><strong>Altura (m):</strong> ${md.altura_m??'-'}</div>`,
    `<div class="item"><strong>Última Obs:</strong> ${md.ultimaObservacao||'-'}</div>`,
    `<div class="item"><strong>Status:</strong> ${statusText}</div>`
  ].join('');
  metadadosEl.innerHTML = html;
}

async function loadMetadados(){
  const cod = state.estacaoSelecionada;
  if(!cod) return;
  renderMetadados(null);
  try {
    if(!state.metadados[cod]){
      state.metadados[cod] = await getMetadados(cod);
    }
    renderMetadados(state.metadados[cod]);
  } catch(e){
    metadadosEl.innerHTML = `<div class='placeholder'>Erro metadados</div>`;
  }
}

async function loadSeries(){
  const { estacaoSelecionada, data:{ano,dia} } = state;
  const validation = validateDate(ano, dia);
  if(!(estacaoSelecionada && validation.valid)) {
    // Clear charts if data is invalid
    const snrCanvas = document.getElementById('snrChart');
    const posCanvas = document.getElementById('posChart');
    if (snrCanvas && posCanvas) {
      renderCharts({ samples: [] }, { samples: [] });
    }
    return;
  }
  
  const key = `${estacaoSelecionada}-${ano}-${dia}`;
  document.getElementById('snrChart').classList.add('loading');
  document.getElementById('posChart').classList.add('loading');
  if(!state.snrCache[key]){
    try { state.snrCache[key] = await getSnr(estacaoSelecionada, ano, dia, 300); } catch { state.snrCache[key] = { samples: [] }; }
  }
  if(!state.posicoesCache[key]){
    try { state.posicoesCache[key] = await getPosicoes(estacaoSelecionada, ano, dia, 300); } catch { state.posicoesCache[key] = { samples: [] }; }
  }
  renderCharts(state.snrCache[key], state.posicoesCache[key]);
  document.getElementById('snrChart').classList.remove('loading');
  document.getElementById('posChart').classList.remove('loading');
}

function validateAndSetDate(){
  const ano = parseInt(anoInput.value,10);
  const dia = parseInt(diaInput.value,10);
  
  const validation = validateDate(ano, dia);
  
  if (!validation.valid) {
    showValidationMessage(validation.message);
    // Clear state if invalid to prevent downloads
    state.data = { ano: null, dia: null };
    updateDownloadButtons();
    return;
  }
  
  showValidationMessage('');
  setData(ano, dia);
}

export function initUI(){
  on('estacoes:loaded', list => {
    estacaoSelect.innerHTML = '<option value="" disabled selected>Selecione...</option>' +
      list.map(e=>`<option value='${e.codigo}'>${e.codigo} - ${e.nome||''}</option>`).join('');
  });
  estacaoSelect.addEventListener('change', e => setEstacao(e.target.value));
  anoInput.addEventListener('change', validateAndSetDate);
  diaInput.addEventListener('change', validateAndSetDate);
  btnHoje.addEventListener('click', () => {
    const d = new Date();
    const julianDay = dateToJulianDay(d);
    anoInput.value = d.getFullYear();
    diaInput.value = julianDay;
    validateAndSetDate();
  });
  btnOntem.addEventListener('click', () => {
    const d = new Date(Date.now()-86400000);
    const julianDay = dateToJulianDay(d);
    anoInput.value = d.getFullYear();
    diaInput.value = julianDay;
    validateAndSetDate();
  });
  on('estacao:changed', () => { loadMetadados(); updateDownloadButtons(); loadSeries(); });
  on('data:changed', () => { updateDownloadButtons(); loadSeries(); });
}
