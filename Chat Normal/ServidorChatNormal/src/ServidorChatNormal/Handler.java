package ServidorChatNormal;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Set;

public class Handler implements Runnable {

    private String Nombre;
    private Socket socket;
    private Scanner Entrada;
    private PrintWriter Salida;
    private Set<String> Nombres;
    private Set<PrintWriter> Escritores;

    public Handler(Socket socket, Set<String> Nombres, Set<PrintWriter> Escritores) {
        this.socket = socket;
        this.Nombres = Nombres;
        this.Escritores = Escritores;
    }

    public void run() {
        try {
            Entrada = new Scanner(socket.getInputStream());
        } catch (IOException ex) {
            System.out.println("Error al crear lector de entrada" + ex.toString());
            System.exit(1);
        }
        try {
            Salida = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException ex) {
            System.out.println("Error al crear lector de salida" + ex.toString());
            System.exit(2);
        }
        try {
            while (true) {
                Salida.println("SUBMITNAME ");
                Nombre = Entrada.nextLine();
                if (Nombre == null) {
                    return;
                }
                if(AgregarUsuario()){
                    break;
                }
            }
            while (true) {
                String Input = Entrada.nextLine();
                if (Input.toLowerCase().startsWith("/salir")) {
                    return;
                }
                Broadcast(Input);
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            CerrarSesión();
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error al cerrar socket" + e.toString());
            }
        }
    }

    public boolean Existe(String Nombre) {
        boolean existe = false;
        if (Nombres.contains(Nombre)) {
            existe = true;
        }
        return existe;
    }

    public boolean AgregarUsuario() {
        boolean Agregado = false;
        synchronized (Nombres) {
            if (!Existe(Nombre)) {
                Nombres.add(Nombre);
                Salida.println("NAMEACCEPTED " + Nombre);
                Escritores.add(Salida);
                Escritores.forEach((Escritor) -> {
                    Escritor.println("MESSAGE " + Nombre + " ha iniciado sesión");
                });
                
                Agregado = true;
            }
        }
        return Agregado;
    }

    public void Broadcast(String Input) {
        Escritores.forEach((escritor) -> {
            escritor.println("MESSAGE " + Nombre + ": " + Input);
        });
    }

    public void CerrarSesión() {
        if (Salida != null) {
            Escritores.remove(Salida);
        }
        if (Nombre != null) {
            Nombres.remove(Nombre);
            Escritores.forEach((escritor) -> {
                escritor.println("MESSAGE " + Nombre + " ha cerrado sesión");
            });
        }
    }
}
