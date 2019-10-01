package ServidorChatMP;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

public class Handler implements Runnable {

    private String Nombre;
    private Socket socket;
    private Scanner Entrada;
    private PrintWriter Salida;
    private Map<String, PrintWriter> Usuarios;

    public Handler(Socket socket, Map<String, PrintWriter> Usuarios) {
        this.socket = socket;
        this.Usuarios = Usuarios;
    }

    @Override
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
                //Pedir nombre
                Salida.println("SUBMITNAME ");
                Nombre = Entrada.nextLine();
                if (Nombre == null || Nombre.isEmpty() || Nombre.equalsIgnoreCase("Salir") || Nombre.equals("null")) {
                    continue;
                }
                if (AgregarUsuario()) {
                    break;
                }
            }
            while (true) {
                String Input = Entrada.nextLine();
                if (Input.startsWith("/") && !Input.toLowerCase().startsWith("/Salir")) {
                    EnviarSusurro(Input);
                } else if (Input.toLowerCase().startsWith("/Salir")) {
                    return;
                } else {
                    Broadcast(Input);
                }
            }
        } catch (Exception e) {
            System.out.println("Error al conectar con el cliente " + Nombre + e.toString());
        } finally {
            CerrarSesión();
            try {
                socket.close();
            } catch (IOException ex) {
                System.out.println("Error al cerrar socket" + ex);
                System.exit(3);
            }
        }
    }

    public void Broadcast(String Input) {
        Usuarios.values().forEach(Escritor -> {
            String Mensaje = "MESSAGE ";
            if (Escritor == Salida) {
                Mensaje += "Yo";
            } else {
                Mensaje += Nombre;
            }
            Escritor.println(Mensaje + ": " + Input + "");
        });
    }

    public boolean Existe(String Nombre) {
        boolean existe = false;
        if (Usuarios.containsKey(Nombre)) {
            existe = true;
        }
        return existe;
    }

    public boolean AgregarUsuario() {
        boolean agregado = false;
        synchronized (Usuarios) {
            if (!Existe(Nombre) && Nombre != null && !Nombre.equals("null")) {
                Usuarios.put(Nombre, Salida);
                Salida.println("NAMEACCEPTED " + Nombre);
                Usuarios.values().forEach(Escritor -> {
                    Escritor.println("MESSAGE " + Nombre + " ha iniciado sesión");
                });
                agregado = true;
            }
        }
        return agregado;
    }

    public void EnviarSusurro(String Input) {
        try {
            int ESPACIO = Input.indexOf(' ');
            String Destinatario = Input.substring(1, ESPACIO);
            String Mensaje = Input.substring(ESPACIO, Input.length());
            if (Existe(Destinatario)) {
                Usuarios.get(Nombre).println("MESSAGE [Susurro para " + Destinatario + "]: " + Mensaje);
                Usuarios.get(Destinatario).println("MESSAGE [Susurro de " + Nombre + "]: " + Mensaje);
            } else {
                Salida.println("MESSAGE No existe tal usuario");
            }
        } catch (Exception e) {
            Salida.println("MESSAGE Error: escribe mensaje");
        }
    }

    public void CerrarSesión() {
        if (Salida != null || Nombre != null) {
            Usuarios.remove(Nombre);
            Usuarios.values().forEach(Escritor -> {
                Escritor.println("MESSAGE " + Nombre + " ha cerrado sesión");
            });
        }
    }

}
