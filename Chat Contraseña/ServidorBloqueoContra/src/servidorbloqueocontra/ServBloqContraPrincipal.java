
package servidorbloqueocontra;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServBloqContraPrincipal {

    private static Map<String, Usuario> Usuarios = new HashMap<>();
    private static Map<String, PrintWriter> Escritores = new HashMap<>();
    private static final long serialVersionUID = 42L;

    public static void main(String[] args) {
        Serializacion Ser = new Serializacion();
        if (Ser.CargarDatos() != null) {
            Usuarios = Ser.CargarDatos();
        }
        System.out.println("El servidor de chat está funcionando...");
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(59001)) {
            while (true) {
                pool.execute(new Handler(listener.accept(), Usuarios, Escritores, Ser));
            }
        } catch (IOException ex) {
            System.out.println("Error al crear socket" + ex.toString());
            System.exit(0);
        }
    }
    
}
