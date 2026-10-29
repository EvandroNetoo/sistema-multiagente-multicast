package projetolpa2v.rede;

import projetolpa2v.config.Configuracao;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/** Socket multicast único para entrada e saída; TTL 1 e interface explícita. */
public final class CanalMulticast implements AutoCloseable {
    private static final int TAMANHO_BUFFER = 8_192;
    private final MulticastSocket socket;
    private final InetSocketAddress enderecoGrupo;
    private final NetworkInterface interfaceRede;
    private final Registro log;
    private volatile boolean fechado;

    public CanalMulticast(Configuracao config, Registro log) throws IOException {
        this.log = log;
        InetAddress grupo = InetAddress.getByName(config.enderecoGrupo());
        if (!grupo.isMulticastAddress()) throw new IOException("Endereço não é multicast: " + grupo);
        enderecoGrupo = new InetSocketAddress(grupo, config.porta());
        interfaceRede = localizarInterface(config.interfaceRede());
        validarInterface(interfaceRede);

        socket = new MulticastSocket(null);
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(config.porta()));
        socket.setNetworkInterface(interfaceRede);
        socket.setTimeToLive(1);
        socket.joinGroup(enderecoGrupo, interfaceRede);
        log.info("Ingressou em " + enderecoGrupo + " pela interface " + interfaceRede.getName() + ", TTL=1");
    }

    private static NetworkInterface localizarInterface(String nomeOuIp) throws IOException {
        NetworkInterface nic = NetworkInterface.getByName(nomeOuIp);
        if (nic != null) return nic;
        for (NetworkInterface candidata : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (nomeOuIp.equalsIgnoreCase(candidata.getDisplayName())) return candidata;
        }
        try {
            InetAddress ip = InetAddress.getByName(nomeOuIp);
            nic = NetworkInterface.getByInetAddress(ip);
            if (nic != null) return nic;
        } catch (java.net.UnknownHostException ignored) {
            // O valor não era nome DNS/IP; a exceção final abaixo é mais explicativa.
        }
        throw new IOException("Interface não encontrada por nome, nome de exibição ou IP: " + nomeOuIp);
    }

    private static void validarInterface(NetworkInterface nic) throws IOException {
        if (!nic.isUp()) throw new IOException("Interface está inativa: " + nic.getName());
        if (!nic.supportsMulticast()) throw new IOException("Interface não suporta multicast: " + nic.getName());
        boolean ipv4 = nic.inetAddresses().anyMatch(a -> a instanceof Inet4Address);
        if (!ipv4) throw new IOException("Interface não possui IPv4: " + nic.getName());
    }

    public synchronized void enviar(Mensagem mensagem) throws IOException {
        byte[] bytes = mensagem.serializar().getBytes(StandardCharsets.UTF_8);
        DatagramPacket pacote = new DatagramPacket(bytes, bytes.length, enderecoGrupo);
        socket.send(pacote);
        log.enviada(mensagem);
    }

    public String receber() throws IOException {
        byte[] buffer = new byte[TAMANHO_BUFFER];
        DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
        socket.receive(pacote);
        String texto = new String(pacote.getData(), pacote.getOffset(), pacote.getLength(), StandardCharsets.UTF_8);
        log.recebida(texto);
        return texto;
    }

    public boolean fechado() { return fechado; }

    @Override public synchronized void close() throws IOException {
        if (fechado) return;
        fechado = true;
        try {
            socket.leaveGroup(enderecoGrupo, interfaceRede);
            log.info("Saiu do grupo " + enderecoGrupo);
        } finally {
            socket.close();
        }
    }
}
