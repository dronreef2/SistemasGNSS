import { state, on, setEstacao, setData } from './state.js';
import { getMetadados, getSnr, getPosicoes } from './apiClient.js';
import { renderCharts } from './charts.js';

const estacaoSelect = document.getElementById('estacaoSelect');
const anoInput = document.getElementById('anoInput');
const diaInput = document.getElementById('diaInput');
const btnHoje = document.getElementById('btnHoje');
const btnOntem = document.getElementById('btnOntem');
const btnRelatorio = document.getElementById('btnRelatorio');
const btnRinex2 = document.getElementById('btnRinex2');
const metadadosEl = document.getElementById('metadados');

function pad(n,l){ return String(n).padStart(l,'0'); }

function updateDownloadButtons(){
  const { estacaoSelecionada, data:{ano,dia} } = state;
  const ok = estacaoSelecionada && ano && dia;
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
  const ano = parseInt(anoInput.value,10);
  const dia = parseInt(diaInput.value,10);
  if(!ano || !dia || dia<1 || dia>366 || ano<1995) return;
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
    const start = new Date(d.getFullYear(),0,1);
    const diff = Math.floor((d - start)/86400000)+1;
    anoInput.value = d.getFullYear();
    diaInput.value = diff;
    validateAndSetDate();
  });
  btnOntem.addEventListener('click', () => {
    const d = new Date(Date.now()-86400000);
    const start = new Date(d.getFullYear(),0,1);
    const diff = Math.floor((d - start)/86400000)+1;
    anoInput.value = d.getFullYear();
    diaInput.value = diff;
    validateAndSetDate();
  });
  on('estacao:changed', () => { loadMetadados(); updateDownloadButtons(); loadSeries(); });
  on('data:changed', () => { updateDownloadButtons(); loadSeries(); });
}
