package servidorbloqueocontra;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

public class Serializacion {

    public void GuardarDatos(Map<String, Usuario> Usuarios) {
        try {
            FileOutputStream FOS = new FileOutputStream("DatosSVContra.ser");
            ObjectOutputStream OOS = new ObjectOutputStream(FOS);
            OOS.writeObject(Usuarios);
            OOS.close();
            FOS.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public Map<String, Usuario> CargarDatos() {
        Map<String, Usuario> Usuarios = null;
        try {
            File Archivo = new File("DatosSVContra.ser");
            if (!Archivo.exists()) {
                Archivo.createNewFile();
            } else if (Archivo.length() == 0) {
                return Usuarios;
            } else {
                FileInputStream FIS = new FileInputStream("DatosSVContra.ser");
                ObjectInputStream OIS = new ObjectInputStream(FIS);
                Usuarios = (Map) OIS.readObject();
                OIS.close();
                FIS.close();
            }
        } catch (IOException ex) {
            System.out.println("No existe archivo, se creará uno nuevo");
            ex.printStackTrace();
            return Usuarios;
        } catch (ClassNotFoundException c) {
            System.out.println("Clase no encontrada");
            c.printStackTrace();
            return Usuarios;
        }
        return Usuarios;
    }
}

