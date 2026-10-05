package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Gestiona la conexión JDBC a MySQL y el cierre seguro de recursos.
public class ConexionDB {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/speedfast_db";
    private static final String USER = "root";
    // Cambia el valor por defecto o define la variable de entorno SPEEDFAST_DB_PASSWORD
    private static final String PASSWORD = valorEntorno("SPEEDFAST_DB_PASSWORD", "tu_contraseña");

    private ConexionDB() {
    }

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("No se encontró el driver JDBC (" + DRIVER + "). Agrega el .jar de lib/ al proyecto.");
        }
    }

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Verifica la conexión al iniciar la aplicación (Paso 1)
    public static boolean probarConexion() {
        Connection conexion = null;
        try {
            conexion = conectar();
            return conexion.isValid(2);
        } catch (SQLException e) {
            System.err.println("No se pudo conectar a la BD: " + e.getMessage());
            return false;
        } finally {
            cerrar(conexion);
        }
    }

    public static void cerrar(Connection conexion) {
        try {
            if (conexion != null) conexion.close();
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }

    public static void cerrar(Statement sentencia) {
        try {
            if (sentencia != null) sentencia.close();
        } catch (SQLException e) {
            System.err.println("Error al cerrar la sentencia: " + e.getMessage());
        }
    }

    public static void cerrar(ResultSet resultado) {
        try {
            if (resultado != null) resultado.close();
        } catch (SQLException e) {
            System.err.println("Error al cerrar el ResultSet: " + e.getMessage());
        }
    }

    private static String valorEntorno(String variable, String porDefecto) {
        String valor = System.getenv(variable);
        return (valor == null || valor.isBlank()) ? porDefecto : valor;
    }
}
