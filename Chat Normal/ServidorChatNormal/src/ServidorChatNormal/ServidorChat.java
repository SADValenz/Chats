package ServidorChatNormal;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorChat {

    private static Set<String> Nombres = new HashSet<>();
    private static Set<PrintWriter> Escritores = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("El servidor de chat está funcionando...");
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(59001)) {
            while (true) {
                pool.execute(new Handler(listener.accept(),Nombres,Escritores));
            }
        }catch(IOException ex){
            System.out.println("Error al establecer socket de servidor" + ex.toString());
            System.exit(0);
        }
    }
}
