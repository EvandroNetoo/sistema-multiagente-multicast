package projetolpa2v.coordenador;

import projetolpa2v.agentes.TipoAgente;

import java.util.EnumMap;
import java.util.Map;

public final class DadosAvaliacao {
    private final EnumMap<TipoAgente, String> dados = new EnumMap<>(TipoAgente.class);

    public DadosAvaliacao(String tabagismo, String sedentarismo, String pressao, String imc) {
        dados.put(TipoAgente.TABAGISMO, tabagismo);
        dados.put(TipoAgente.SEDENTARISMO, sedentarismo);
        dados.put(TipoAgente.PRESSAO, pressao);
        dados.put(TipoAgente.IMC, imc);
    }

    public String para(TipoAgente agente) { return dados.get(agente); }
    public Map<TipoAgente, String> todos() { return Map.copyOf(dados); }
}
