package ClienteChatNormal;

import javax.swing.JFrame;


public class ClienteNormPrincipal {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Error al definir argumentos de conexión");
            return;
        }
        Cliente cliente = new Cliente(args[0]);
        cliente.Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cliente.Frame.setVisible(true);
        cliente.run();
    }

}
