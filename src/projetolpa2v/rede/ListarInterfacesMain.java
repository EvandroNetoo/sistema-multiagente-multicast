package projetolpa2v.rede;

import java.net.NetworkInterface;
import java.util.Collections;

/** Lista os identificadores que podem ser usados em --iface. */
public final class ListarInterfacesMain {
    private ListarInterfacesMain() { }

    public static void main(String[] args) throws Exception {
        for (NetworkInterface nic : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            System.out.printf("nome=%s | exibição=%s | ativa=%s | multicast=%s | endereços=%s%n",
                    nic.getName(), nic.getDisplayName(), nic.isUp(), nic.supportsMulticast(),
                    Collections.list(nic.getInetAddresses()));
        }
    }
}
