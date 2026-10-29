package projetolpa2v.logica;

public final class RedeLpa2v {
    public static final double CONTROLE = 0.5;

    private RedeLpa2v() { }

    public static ResultadoLpa2v consolidar(double tabagismo, double sedentarismo,
                                             double pressao, double imc) {
        NoLpa2v nap1 = NoLpa2v.calcular("NAP1", tabagismo, 1.0 - sedentarismo);
        NoLpa2v nap2 = NoLpa2v.calcular("NAP2", pressao, 1.0 - imc);
        NoLpa2v nap3 = NoLpa2v.calcular("NAP3", nap1.evidenciaResultante(),
                1.0 - nap2.evidenciaResultante());
        EstadoLogico estado = classificar(nap3.grauCerteza(), nap3.grauContradicao());
        return new ResultadoLpa2v(nap1, nap2, nap3, estado, classificacao(estado, nap3.grauCerteza()));
    }

    public static EstadoLogico classificar(double gc, double gct) {
        if (gc >= CONTROLE) return EstadoLogico.VERDADEIRO;
        if (gc <= -CONTROLE) return EstadoLogico.FALSO;
        if (gct >= CONTROLE) return EstadoLogico.INCONSISTENTE;
        if (gct <= -CONTROLE) return EstadoLogico.PARACOMPLETO;
        return EstadoLogico.NAO_EXTREMO;
    }

    private static String classificacao(EstadoLogico estado, double gc) {
        if (estado != EstadoLogico.NAO_EXTREMO) return estado.classificacao();
        if (gc > 0) return "Risco indefinido, com tendência a elevado";
        if (gc < 0) return "Risco indefinido, com tendência a baixo";
        return "Risco indefinido, sem tendência";
    }
}
