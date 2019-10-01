package servidorchatbloqueo;

import java.io.PrintWriter;
import java.util.ArrayList;

public class Usuario {

    private PrintWriter Escritor;
    private ArrayList<Usuario> Bloqueados;

    public Usuario(PrintWriter Escritor) {
        this.Escritor = Escritor;
        this.Bloqueados = new ArrayList<>();
    }

    public PrintWriter getEscritor() {
        return Escritor;
    }

    public ArrayList<Usuario> getBloqueados() {
        return Bloqueados;
    }
}

