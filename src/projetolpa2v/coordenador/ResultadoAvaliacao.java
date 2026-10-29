package projetolpa2v.coordenador;

import projetolpa2v.agentes.TipoAgente;
import projetolpa2v.logica.RedeLpa2v;
import projetolpa2v.logica.ResultadoLpa2v;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ResultadoAvaliacao {
    private final String id;
    private final EnumMap<TipoAgente, Double> respostas;
    private final EnumMap<TipoAgente, String> erros;
    private final EnumSet<TipoAgente> ausentes;

    ResultadoAvaliacao(String id, Map<TipoAgente, Double> respostas,
                       Map<TipoAgente, String> erros, Set<TipoAgente> ausentes) {
        this.id = id;
        this.respostas = new EnumMap<>(respostas);
        this.erros = new EnumMap<>(erros);
        this.ausentes = ausentes.isEmpty() ? EnumSet.noneOf(TipoAgente.class) : EnumSet.copyOf(ausentes);
    }

    public boolean completa() { return respostas.size() == TipoAgente.values().length && erros.isEmpty(); }
    public String id() { return id; }
    public Map<TipoAgente, Double> respostas() { return Map.copyOf(respostas); }
    public Map<TipoAgente, String> erros() { return Map.copyOf(erros); }
    public Set<TipoAgente> ausentes() { return Set.copyOf(ausentes); }

    public String relatorio() {
        StringBuilder s = new StringBuilder("\n=== Avaliação " + id + " ===\n");
        for (TipoAgente agente : TipoAgente.values()) {
            if (respostas.containsKey(agente)) {
                s.append(String.format(Locale.US, "μ%-13s = %.3f%n", agente, respostas.get(agente)));
            } else if (erros.containsKey(agente)) {
                s.append(agente).append(": ERRO - ").append(erros.get(agente)).append('\n');
            } else {
                s.append(agente).append(": NÃO RESPONDEU\n");
            }
        }
        if (!completa()) {
            if (!ausentes.isEmpty()) s.append("Agentes ausentes no timeout: ").append(ausentes).append('\n');
            s.append("AVALIAÇÃO INCOMPLETA. A LPA2v não foi calculada; nenhum valor foi inventado.\n");
            return s.toString();
        }
        ResultadoLpa2v lpa = RedeLpa2v.consolidar(respostas.get(TipoAgente.TABAGISMO),
                respostas.get(TipoAgente.SEDENTARISMO), respostas.get(TipoAgente.PRESSAO),
                respostas.get(TipoAgente.IMC));
        return s.append('\n').append(lpa.relatorio()).toString();
    }
}
