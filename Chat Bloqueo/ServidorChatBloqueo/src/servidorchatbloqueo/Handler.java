package servidorchatbloqueo;

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
    private Usuario YO;
    private Map<String, Usuario> Usuarios;

    public Handler(Socket socket, Map<String, Usuario> Usuarios) {
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
                Salida.println("SUBMITNAME ");
                Nombre = Entrada.nextLine();
                if (Nombre == null || Nombre.isEmpty() || Nombre.equalsIgnoreCase("salir")|| Nombre.equals("null")) {
                    continue;
                }
                if (AgregarUsuario()) {
                    break;
                }
            }
            while (true) {
                String Input = Entrada.nextLine();
                if (Input.startsWith("/")) {
                    if (Input.toLowerCase().startsWith("/salir")) {
                        return;
                    } else if (Input.toLowerCase().startsWith("/bloquear")) {
                        BloquearUsuario(Input);
                    } else if (Input.toLowerCase().startsWith("/desbloquear")) {
                        DesbloquearUsuario(Input);
                    } else {
                        EnviarSusurro(Input);
                    }
                } else {
                    Broadcast(Input);
                }
            }
        } catch (Exception e) {
            System.out.println("Error al conectar con el cliente" + e.toString());
        } finally {
            CerrarSesión();
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error al cerrar socket" + e.toString());
                System.exit(3);
            }
        }
    }

    public void Broadcast(String Input) {
        Usuarios.values().forEach(US -> {
            if (!US.getBloqueados().contains(YO)) {
                String Mensaje = "MESSAGE ";
                if (US.getEscritor() == Salida) {
                    Mensaje += "Yo";
                } else {
                    Mensaje += Nombre;
                }
                US.getEscritor().println(Mensaje + ": " + Input);
            }
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
            if (!Existe(Nombre)&& Nombre != null && !Nombre.equals("null")) {
                Usuarios.put(Nombre, new Usuario(Salida));
                Salida.println("NAMEACCEPTED " + Nombre);
                YO = Usuarios.get(Nombre);
                Usuarios.values().forEach(US -> {
                    US.getEscritor().println("MESSAGE " + Nombre + " ha iniciado sesión");
                });
                agregado = true;
            }
        }
        return agregado;
    }

    public void EnviarSusurro(String Input) {
        try {
            int ESPACIO = Input.indexOf(' ');
            String Dest = Input.substring(1, ESPACIO);
            String Mensaje = Input.substring(ESPACIO);
            Usuario Destinatario = Usuarios.get(Dest);
            Salida.println("MESSAGE Mensaje privado para " + Dest + ": " + Mensaje);
            if (Usuarios.containsValue(Destinatario) && !Destinatario.getBloqueados().contains(YO)) {
                Destinatario.getEscritor().println("MESSAGE Mensaje privado de " + Nombre + ": " + Mensaje);
            }
        } catch (Exception e) {
            Salida.println("MESSAGE Error: escribe mensaje");
        }
    }

    public void BloquearUsuario(String Input) {
        try {
            int ESPACIO = Input.indexOf(' ');
            String UsuarioBloqueado = Input.substring(ESPACIO + 1);
            if (Usuarios.containsKey(UsuarioBloqueado) && !UsuarioBloqueado.equals(Nombre)
                    && !YO.getBloqueados().contains(Usuarios.get(UsuarioBloqueado))) {
                YO.getBloqueados().add(Usuarios.get(UsuarioBloqueado));
                Salida.println("MESSAGE Has bloqueado a " + UsuarioBloqueado);
            }
        } catch (Exception e) {
            Salida.println("MESSAGE Error en la definición de parámetros");
        }
    }

    public void DesbloquearUsuario(String Input) {
        try {
            int ESPACIO = Input.indexOf(' ');
            String UsuarioBloqueado = Input.substring(ESPACIO + 1);
            if (Usuarios.containsKey(UsuarioBloqueado) && !UsuarioBloqueado.equals(Nombre)
                    && YO.getBloqueados().contains(Usuarios.get(UsuarioBloqueado))) {
                YO.getBloqueados().remove(Usuarios.get(UsuarioBloqueado));
                Salida.println("MESSAGE Has desbloqueado a " + UsuarioBloqueado);
            }
        } catch (Exception e) {
            Salida.println("MESSAGE Error en la definición de parámetros");
        }
    }

    public void CerrarSesión() {
        if (Salida != null || Nombre != null) {
            Usuarios.remove(Nombre);
            Usuarios.values().forEach(US -> {
                US.getEscritor().println("MESSAGE " + Nombre + " ha cerrado sesión");
            });
        }
    }
}
