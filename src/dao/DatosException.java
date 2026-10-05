package dao;

// Excepción no chequeada que envuelve los SQLException de los DAO,
// para que la interfaz pueda mostrar un mensaje claro con JOptionPane.
public class DatosException extends RuntimeException {

    public DatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
