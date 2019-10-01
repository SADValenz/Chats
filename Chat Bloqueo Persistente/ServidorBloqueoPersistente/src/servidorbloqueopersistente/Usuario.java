package servidorbloqueopersistente;

import java.io.Serializable;
import java.util.ArrayList;

public class Usuario implements Serializable {
    private static final long serialVersionUID = -7038486863587832452L;
    private String Nombre;
    private ArrayList<Usuario> Bloqueados;

    public Usuario(String Nombre) {
        this.Nombre = Nombre;
        this.Bloqueados = new ArrayList<>();
    }

    public ArrayList<Usuario> getBloqueados() {
        return Bloqueados;
    }
    
    public String getNombre() {
        return Nombre;
    }

}
