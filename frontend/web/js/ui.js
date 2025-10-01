import { state, on, setEstacao, setData } from './state.js';
import { getMetadados, getSnr, getPosicoes } from './apiClient.js';
import { renderCharts } from './charts.js';
import { DateUtils, UrlUtils, debounce } from './utils.js';

const estacaoSelect = document.getElementById('estacaoSelect');
const anoInput = document.getElementById('anoInput');
const diaInput = document.getElementById('diaInput');
const btnHoje = document.getElementById('btnHoje');
const btnOntem = document.getElementById('btnOntem');
const btnRelatorio = document.getElementById('btnRelatorio');
const btnRinex2 = document.getElementById('btnRinex2');
const metadadosEl = document.getElementById('metadados');
const dateValidationEl = document.getElementById('dateValidation');

function validateAndUpdateDate() {
  const year = anoInput.value;
  const day = diaInput.value;
  
  if (!year || !day) {
    dateValidationEl.textContent = '';
    dateValidationEl.className = 'validation-message';
    updateDownloadButtons();
    return false;
  }
  
  const validation = DateUtils.validateDateInput(year, day);
  if (validation.valid) {
    dateValidationEl.textContent = '✓ Data válida';
    dateValidationEl.className = 'validation-message valid';
    setData({ ano: parseInt(year), dia: parseInt(day) });
    updateDownloadButtons();
    return true;
  } else {
    dateValidationEl.textContent = validation.errors[0];
    dateValidationEl.className = 'validation-message';
    updateDownloadButtons();
    return false;
  }
}

function updateDownloadButtons(){
  const { estacaoSelecionada, data:{ano,dia} } = state;
  const dateValid = ano && dia && DateUtils.validateDateInput(ano, dia).valid;
  
  btnRelatorio.disabled = !estacaoSelecionada;
  btnRinex2.disabled = !(estacaoSelecionada && dateValid);
  
  if(estacaoSelecionada){
    btnRelatorio.onclick = () => UrlUtils.openInNewTab(UrlUtils.buildReportUrl(estacaoSelecionada));
  }
  if(estacaoSelecionada && dateValid){
    btnRinex2.onclick = () => UrlUtils.openInNewTab(UrlUtils.buildRinex2Url(estacaoSelecionada, ano, dia));
  }
}

function renderMetadados(md){
  metadadosEl.innerHTML = '';
  if(!md){
    metadadosEl.innerHTML = '<div class="placeholder">Carregando...</div>';
    return;
  }
  const html = [
    `<div class="item"><strong>Receptor:</strong> ${md.receptor||'-'}</div>`,
    `<div class="item"><strong>Antena:</strong> ${md.antena||'-'}</div>`,
    `<div class="item"><strong>Altura (m):</strong> ${md.altura_m??'-'}</div>`,
    `<div class="item"><strong>Última Obs:</strong> ${md.ultimaObservacao||'-'}</div>`
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
  if(!(estacaoSelecionada && ano && dia)) return;
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
  return validateAndUpdateDate();
}

export function initUI(){
  on('estacoes:loaded', list => {
    estacaoSelect.innerHTML = '<option value="" disabled selected>Selecione...</option>' +
      list.map(e=>`<option value='${e.codigo}'>${e.codigo} - ${e.nome||''}</option>`).join('');
  });
  estacaoSelect.addEventListener('change', e => setEstacao(e.target.value));
  
  // Usar debounce para otimizar validação de entrada
  const debouncedValidation = debounce(validateAndUpdateDate, 300);
  anoInput.addEventListener('input', debouncedValidation);
  diaInput.addEventListener('input', debouncedValidation);
  
  btnHoje.addEventListener('click', () => {
    const today = DateUtils.getTodayJulian();
    anoInput.value = today.year;
    diaInput.value = today.day;
    validateAndUpdateDate();
  });
  
  btnOntem.addEventListener('click', () => {
    const yesterday = DateUtils.getYesterdayJulian();
    anoInput.value = yesterday.year;
    diaInput.value = yesterday.day;
    validateAndUpdateDate();
  });
  
  on('estacao:changed', () => { loadMetadados(); updateDownloadButtons(); loadSeries(); updateMetadata(); });
  on('data:changed', () => { updateDownloadButtons(); loadSeries(); updateMetadata(); });
}

// Função para atualizar painel de metadados (Fase 4)
async function updateMetadata() {
  const metadataDiv = document.getElementById('metadata');
  if (!metadataDiv || !state.estacao) return;

  try {
    // Encontrar coordenadas da estação
    const coords = state.estacoes.find(e => e.name === state.estacao);
    if (coords && state.ano && state.diaAno) {
      const selectedDate = DateUtils.dayOfYearToDate(parseInt(state.ano), parseInt(state.diaAno));
      
      metadataDiv.innerHTML = `
        <div class="metadata-content">
          <div class="metadata-item">
            <span class="metadata-label">Estação:</span>
            <span class="metadata-value">${state.estacao}</span>
          </div>
          <div class="metadata-item">
            <span class="metadata-label">Coordenadas:</span>
            <span class="metadata-value">${coords.lat.toFixed(6)}°, ${coords.lon.toFixed(6)}°</span>
          </div>
          <div class="metadata-item">
            <span class="metadata-label">Data:</span>
            <span class="metadata-value">${selectedDate.toLocaleDateString('pt-BR')}</span>
          </div>
          <div class="metadata-item">
            <span class="metadata-label">Dia do Ano:</span>
            <span class="metadata-value">${state.diaAno.toString().padStart(3, '0')}</span>
          </div>
          <div class="metadata-item">
            <span class="metadata-label">Status:</span>
            <span class="metadata-value status-online">🟢 Online</span>
          </div>
        </div>
      `;
    }
  } catch (error) {
    console.error('Erro ao atualizar metadados:', error);
    if (metadataDiv) {
      metadataDiv.innerHTML = '<div class="error">❌ Erro ao carregar metadados</div>';
    }
  }
}
