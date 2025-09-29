export const state = {
  estacoes: [],
  estacaoSelecionada: null,
  data: { ano: null, dia: null },
  metadados: {},
  snrCache: {},
  posicoesCache: {},
  listeners: {}
};

export function on(event, cb){
  (state.listeners[event] ||= []).push(cb);
}

export function emit(event, payload){
  (state.listeners[event]||[]).forEach(cb => cb(payload));
}

export function setEstacoes(lista){
  state.estacoes = lista;
  emit('estacoes:loaded', lista);
}

export function setEstacao(codigo){
  state.estacaoSelecionada = codigo;
  emit('estacao:changed', codigo);
}

export function setData(ano, dia){
  state.data = { ano, dia };
  emit('data:changed', state.data);
}
