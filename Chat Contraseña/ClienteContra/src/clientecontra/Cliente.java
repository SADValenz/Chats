package clientecontra;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Cliente {

    static final long serialVersionUID = 42L;
    private String server;
    private Scanner Entrada;
    private PrintWriter Salida;
    private JTextField textField = new JTextField(50);
    private JTextArea Mensaje = new JTextArea(16, 50);
    private Socket socket;
    JFrame Frame = new JFrame("Chat");

    public Cliente(String serverAddress) {
        this.server = serverAddress;
        textField.setEditable(false);
        Mensaje.setEditable(false);
        Frame.getContentPane().add(textField, BorderLayout.SOUTH);
        Frame.getContentPane().add(new JScrollPane(Mensaje));
        Frame.pack();

        textField.addActionListener((ActionEvent e) -> {
            Salida.println(textField.getText());
            textField.setText("");
        });
    }

    private String getNombre() {
        return JOptionPane.showInputDialog(Frame,
                "Pon tu nombre",
                "Elegir nombre",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private String getPass() {
        return JOptionPane.showInputDialog(Frame,
                "Contraseña de usuario",
                "Contraseña",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private void Incorrecto() {
        JOptionPane.showMessageDialog(Frame,
                "Usuario o Contraseña incorrectos",
                "Error al proporcionar datos",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    public void run() {
        try {
            socket = new Socket(server, 59001);
        } catch (IOException ex) {
            System.out.println("Error al crear socket de conexión" + ex.toString());
            System.exit(0);
        }
        try {
            Entrada = new Scanner(socket.getInputStream());
        } catch (IOException ex) {
            System.out.println("Error al crear entrada de datos" + ex.toString());
            System.exit(1);
        }

        try {
            Salida = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException ex) {
            System.out.println("Error al crear salida de datos" + ex.toString());
            System.exit(2);
        }
        try {
            while (Entrada.hasNextLine()) {
                String line = Entrada.nextLine();
                if (line.startsWith("SUBMITNAME")) {
                    Salida.println(getNombre());
                } else if (line.startsWith("SUBMITPASS")) {
                    Salida.println(getPass());
                } else if (line.startsWith("INCORRECTDATA")) {
                    Incorrecto();
                } else if (line.startsWith("NAMEACCEPTED")) {
                    this.Frame.setTitle("Chatter - " + line.substring(13));
                    textField.setEditable(true);
                } else if (line.startsWith("MESSAGE")) {
                    Mensaje.append("• " + line.substring(8) + "\n");
                }
            }
        } finally {
            Frame.setVisible(false);
            Frame.dispose();
        }
    }
}
