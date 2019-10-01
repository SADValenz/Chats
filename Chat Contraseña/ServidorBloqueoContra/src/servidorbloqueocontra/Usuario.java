
package servidorbloqueocontra;

import java.io.Serializable;
import java.util.ArrayList;

public class Usuario implements Serializable {
    private static final long serialVersionUID = -7038486863587832452L;
    private String Nombre;
    private ArrayList<Usuario> Bloqueados;
    private String Contra;

    public Usuario(String Nombre, String Contra) {
        this.Nombre = Nombre;
        this.Bloqueados = new ArrayList<>();
        this.Contra=Contra;
    }

    public ArrayList<Usuario> getBloqueados() {
        return Bloqueados;
    }
    
    public String getNombre() {
        return Nombre;
    }

    public String getContra() {
        return Contra;
    }
    
}