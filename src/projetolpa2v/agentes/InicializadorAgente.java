package projetolpa2v.agentes;

import projetolpa2v.config.Configuracao;

import java.util.Scanner;

final class InicializadorAgente {
    private InicializadorAgente() { }

    static void iniciar(TipoAgente tipo, String[] args) throws Exception {
        try {
            Configuracao config = Configuracao.ler(args);
            AgenteMulticast agente = new AgenteMulticast(tipo, config);
            Runtime.getRuntime().addShutdownHook(new Thread(agente::close, "encerramento-" + tipo));
            Thread console = new Thread(() -> aguardarSaida(agente), "console-" + tipo);
            console.setDaemon(true);
            console.start();
            try { agente.executar(); } finally { agente.close(); }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println("Uso: " + Configuracao.uso());
        }
    }

    private static void aguardarSaida(AgenteMulticast agente) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            if (scanner.nextLine().trim().equalsIgnoreCase("sair")) {
                agente.close();
                return;
            }
        }
    }
}
