package projetolpa2v.agentes;

import projetolpa2v.logica.NoLpa2v;

import java.util.Set;

/** Escalas didáticas obrigatórias dos quatro fatores de risco. */
public final class Calculadores {
    private static final Set<Integer>[] TABAGISMO = conjuntos(
            new int[]{0, 1, 2, 4}, new int[]{0, 1, 2, 3}, new int[]{0, 1, 2, 3},
            new int[]{0, 1}, new int[]{0, 1, 2});
    private static final Set<Integer>[] SEDENTARISMO = conjuntos(
            new int[]{0, 1, 2, 3}, new int[]{0, 1, 2}, new int[]{0, 1},
            new int[]{0, 1, 2}, new int[]{0, 1});

    private Calculadores() { }

    @SafeVarargs
    private static Set<Integer>[] conjuntos(int[]... valores) {
        @SuppressWarnings("unchecked") Set<Integer>[] resultado = new Set[valores.length];
        for (int i = 0; i < valores.length; i++) {
            java.util.HashSet<Integer> conjunto = new java.util.HashSet<>();
            for (int v : valores[i]) conjunto.add(v);
            resultado[i] = Set.copyOf(conjunto);
        }
        return resultado;
    }

    public static double tabagismo(String dados) throws EntradaInvalidaException {
        return somaQuestionario(dados, TABAGISMO, 13.0, "tabagismo");
    }

    public static double sedentarismo(String dados) throws EntradaInvalidaException {
        return somaQuestionario(dados, SEDENTARISMO, 9.0, "sedentarismo");
    }

    private static double somaQuestionario(String dados, Set<Integer>[] permitidos,
                                            double maximo, String nome) throws EntradaInvalidaException {
        String[] partes = separar(dados, permitidos.length, nome);
        int soma = 0;
        for (int i = 0; i < partes.length; i++) {
            int valor;
            try { valor = Integer.parseInt(partes[i].trim()); }
            catch (NumberFormatException e) { throw new EntradaInvalidaException(nome + ": resposta não inteira na posição " + (i + 1)); }
            if (!permitidos[i].contains(valor)) {
                throw new EntradaInvalidaException(nome + ": alternativa inexistente na posição " + (i + 1));
            }
            soma += valor;
        }
        return soma / maximo;
    }

    public static double pressao(String dados) throws EntradaInvalidaException {
        String[] p = separar(dados, 2, "pressão");
        double pas = real(p[0], "pressão sistólica");
        double pad = real(p[1], "pressão diastólica");
        if (pas <= 0 || pad <= 0) throw new EntradaInvalidaException("pressões devem ser positivas");
        double evidenciaPas = (pas - 120.0) / 60.0;
        double evidenciaPad = (pad - 80.0) / 30.0;
        return NoLpa2v.limitar(Math.max(evidenciaPas, evidenciaPad));
    }

    public static double imc(String dados) throws EntradaInvalidaException {
        String[] p = separar(dados, 2, "IMC");
        double peso = real(p[0], "peso");
        double altura = real(p[1], "altura");
        if (peso <= 0) throw new EntradaInvalidaException("peso deve ser positivo");
        if (altura < 0.50 || altura > 2.50) throw new EntradaInvalidaException("altura fora da faixa plausível (0.50 a 2.50 m)");
        double imc = peso / (altura * altura);
        return NoLpa2v.limitar((imc - 25.0) / 15.0);
    }

    private static String[] separar(String dados, int quantidade, String nome) throws EntradaInvalidaException {
        String[] partes = dados.split(",", -1);
        if (partes.length != quantidade) {
            throw new EntradaInvalidaException(nome + ": esperados " + quantidade + " valores, recebidos " + partes.length);
        }
        return partes;
    }

    private static double real(String texto, String nome) throws EntradaInvalidaException {
        try {
            double valor = Double.parseDouble(texto.trim());
            if (!Double.isFinite(valor)) throw new NumberFormatException();
            return valor;
        } catch (NumberFormatException e) {
            throw new EntradaInvalidaException(nome + " inválido");
        }
    }

    public static CalculadorEvidencia para(TipoAgente tipo) {
        return switch (tipo) {
            case TABAGISMO -> Calculadores::tabagismo;
            case SEDENTARISMO -> Calculadores::sedentarismo;
            case PRESSAO -> Calculadores::pressao;
            case IMC -> Calculadores::imc;
        };
    }
}
