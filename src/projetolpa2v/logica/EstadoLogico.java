package projetolpa2v.logica;

public enum EstadoLogico {
    VERDADEIRO("Verdadeiro (V)", "Risco cardiológico elevado"),
    FALSO("Falso (F)", "Risco cardiológico baixo"),
    INCONSISTENTE("Inconsistente (T)", "Inconclusivo: evidências contraditórias, reavaliar o paciente"),
    PARACOMPLETO("Paracompleto (⊥)", "Inconclusivo: evidências contraditórias, reavaliar o paciente"),
    NAO_EXTREMO("Não extremo", "");

    private final String descricao;
    private final String classificacao;

    EstadoLogico(String descricao, String classificacao) {
        this.descricao = descricao;
        this.classificacao = classificacao;
    }

    public String descricao() { return descricao; }
    public String classificacao() { return classificacao; }
}
