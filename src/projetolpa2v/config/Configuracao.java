package projetolpa2v.config;

import java.util.HashMap;
import java.util.Map;

/** Configuração comum, alterável por linha de comando sem recompilar. */
public final class Configuracao {
    public static final int GRUPO_PADRAO = 4;
    public static final int TIMEOUT_PADRAO_MS = 3_000;

    private final String enderecoGrupo;
    private final int porta;
    private final String interfaceRede;
    private final int timeoutMs;
    private final int threads;

    private Configuracao(String enderecoGrupo, int porta, String interfaceRede,
                         int timeoutMs, int threads) {
        this.enderecoGrupo = enderecoGrupo;
        this.porta = porta;
        this.interfaceRede = interfaceRede;
        this.timeoutMs = timeoutMs;
        this.threads = threads;
    }

    public static Configuracao ler(String[] args) {
        Map<String, String> opcoes = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (!args[i].startsWith("--") || i + 1 >= args.length) {
                throw new IllegalArgumentException("Parâmetro inválido: " + args[i]);
            }
            opcoes.put(args[i].substring(2), args[++i]);
        }
        int numero = inteiro(opcoes, "grupo-numero", GRUPO_PADRAO, 1, 254);
        String grupo = opcoes.getOrDefault("grupo", "239.0.0." + numero);
        int porta = inteiro(opcoes, "porta", 5000 + numero, 1, 65_535);
        int timeout = inteiro(opcoes, "timeout", TIMEOUT_PADRAO_MS, 1, 120_000);
        int threads = inteiro(opcoes, "threads", 4, 1, 64);
        String iface = opcoes.get("iface");
        if (iface == null || iface.isBlank()) {
            throw new IllegalArgumentException("Informe explicitamente --iface NOME_OU_IP");
        }
        return new Configuracao(grupo, porta, iface, timeout, threads);
    }

    private static int inteiro(Map<String, String> opcoes, String nome, int padrao,
                               int minimo, int maximo) {
        int valor;
        try {
            valor = Integer.parseInt(opcoes.getOrDefault(nome, String.valueOf(padrao)));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor inválido para --" + nome, e);
        }
        if (valor < minimo || valor > maximo) {
            throw new IllegalArgumentException("--" + nome + " deve estar entre " + minimo + " e " + maximo);
        }
        return valor;
    }

    public String enderecoGrupo() { return enderecoGrupo; }
    public int porta() { return porta; }
    public String interfaceRede() { return interfaceRede; }
    public int timeoutMs() { return timeoutMs; }
    public int threads() { return threads; }

    public static String uso() {
        return "--iface NOME_OU_IP [--grupo-numero G] [--grupo 239.0.0.G] "
                + "[--porta 5000+G] [--timeout 3000] [--threads 4]";
    }
}
