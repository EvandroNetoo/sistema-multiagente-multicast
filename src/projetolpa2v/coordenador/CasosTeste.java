package projetolpa2v.coordenador;

import java.util.List;

public final class CasosTeste {
    private static final List<DadosAvaliacao> CASOS = List.of(
            new DadosAvaliacao("0,0,0,0,0", "0,0,0,0,0", "115,75", "65,1.70"),
            new DadosAvaliacao("4,3,3,1,2", "3,2,1,2,1", "185,115", "125,1.70"),
            new DadosAvaliacao("4,2,2,0,0", "2,2,1,2,0", "150,95", "92,1.75"),
            new DadosAvaliacao("4,3,3,1,2", "3,2,1,2,1", "115,75", "65,1.70"),
            new DadosAvaliacao("0,0,0,0,0", "0,0,0,0,0", "185,115", "125,1.70"),
            new DadosAvaliacao("2,1,1,0,0", "1,1,0,1,0", "135,85", "80,1.70"));

    private CasosTeste() { }

    public static DadosAvaliacao obter(int numero) {
        if (numero < 1 || numero > CASOS.size()) throw new IllegalArgumentException("Caso deve estar entre 1 e 6");
        return CASOS.get(numero - 1);
    }
}
