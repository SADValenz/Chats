package servidorbloqueocontra;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

public class Handler implements Runnable {

    private String Nombre;
    private String Contra;
    private Socket socket;
    private Scanner Entrada;
    private PrintWriter Salida;
    private Usuario YO;
    private Map<String, Usuario> Usuarios;
    private Map<String, PrintWriter> Escritores;
    private Serializacion Ser;

    public Handler(Socket socket, Map<String, Usuario> Usuarios,
            Map<String, PrintWriter> Escritores, Serializacion Ser) {
        this.socket = socket;
        this.Escritores = Escritores;
        this.Usuarios = Usuarios;
        this.Ser = Ser;
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
                Salida.println("SUBMITPASS ");
                Contra = Entrada.nextLine();
                if (Nombre == null || Nombre.isEmpty()
                        || Nombre.equalsIgnoreCase("salir") || Nombre.equals("null") 
                        || Escritores.containsKey(Nombre)|| Contra.isEmpty()|| Contra.equals("null")) {
                    continue;
                }
                if (IniciarSesion()) {
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
            System.out.println("Error al conectar con el cliente " + e.toString());
            e.printStackTrace();
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
        Usuarios.keySet().forEach(US -> {
            if (!Usuarios.get(US).getBloqueados().contains(YO)) {
                String Mensaje = "MESSAGE ";
                if (Escritores.get(Usuarios.get(US).getNombre()) == Salida) {
                    Mensaje += "Yo";
                } else {
                    Mensaje += Nombre;
                }
                if (Escritores.containsKey(Usuarios.get(US).getNombre())) {
                    Escritores.get(Usuarios.get(US).getNombre()).println(Mensaje + ": " + Input);
                }
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

    public boolean IniciarSesion() {
        boolean Iniciar = false;
        synchronized (Usuarios) {
            if (!Existe(Nombre) && Nombre != null && !Nombre.equals("null") && Salida != null) {
                Usuarios.put(Nombre, new Usuario(Nombre, Contra));
                Escritores.put(Nombre, Salida);
                Salida.println("NAMEACCEPTED " + Nombre);
                YO = Usuarios.get(Nombre);
                Ser.GuardarDatos(Usuarios);
                Escritores.values().forEach(ESC -> {
                    ESC.println("MESSAGE Bienvenido por primera vez " + Nombre);
                });
                Iniciar = true;
            } else if (Existe(Nombre) && Contra.equals(Usuarios.get(Nombre).getContra())
                    && Nombre != null && !Nombre.equals("null") && Salida != null) {
                Escritores.put(Nombre, Salida);
                Salida.println("NAMEACCEPTED " + Nombre);
                YO = Usuarios.get(Nombre);
                Escritores.values().forEach(ESC -> {
                    ESC.println("MESSAGE " + Nombre + " ha iniciado sesión");
                });
                Iniciar = true;
            }else{
                Salida.println("INCORRECTDATA ");
            }
        }
        return Iniciar;
    }

    public void EnviarSusurro(String Input) {
        try {
            int ESPACIO = Input.indexOf(' ');
            String Dest = Input.substring(1, ESPACIO);
            String Mensaje = Input.substring(ESPACIO);
            Usuario Destinatario = Usuarios.get(Dest);
            Salida.println("MESSAGE [Susurro para " + Dest + "]: " + Mensaje);
            if (Usuarios.containsValue(Destinatario) && !Destinatario.getBloqueados().contains(YO)) {
                Escritores.get(Dest).println("MESSAGE [Susurro de " + Nombre + "]: " + Mensaje);
            } else {
                Salida.println("MESSAGE No existe tal usuario");
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
                Ser.GuardarDatos(Usuarios);
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
                Ser.GuardarDatos(Usuarios);
            }
        } catch (Exception e) {
            Salida.println("MESSAGE Error en la definición de parámetros");
        }
    }

    public void CerrarSesión() {
        if (Salida != null && Nombre != null && !Nombre.equals("null")) {
            Escritores.remove(Nombre);
            Escritores.values().forEach(ESC -> {
                ESC.println("MESSAGE " + Nombre + " ha cerrado sesión");
            });
        }
    }
}
