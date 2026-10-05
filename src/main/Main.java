package main;

import dao.ConexionDB;
import view.VentanaPrincipal;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        // Paso 1: comprobar la conexión antes de abrir la interfaz
        if (!ConexionDB.probarConexion()) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar a MySQL (speedfast_db).\n"
                            + "Verifica que el servidor esté iniciado, que ejecutaste sql/speedfast_db.sql\n"
                            + "y que el usuario/contraseña de dao/ConexionDB sean correctos.",
                    "SpeedFast - Error de conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
