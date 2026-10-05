package dao;

import model.Repartidor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// CRUD de la tabla repartidores (PreparedStatement + ResultSet).
public class RepartidorDAO {

    private static final String SQL_INSERT = "INSERT INTO repartidores (nombre) VALUES (?)";
    private static final String SQL_SELECT = "SELECT id, nombre FROM repartidores ORDER BY id";
    private static final String SQL_UPDATE = "UPDATE repartidores SET nombre = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM repartidores WHERE id = ?";

    /** Inserta el repartidor y le asigna el ID generado por la BD. */
    public void create(Repartidor r) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet claves = null;
        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, r.getNombre());
            ps.executeUpdate();
            claves = ps.getGeneratedKeys();
            if (claves.next()) r.setId(claves.getInt(1));
        } catch (SQLException e) {
            throw new DatosException("No se pudo registrar el repartidor: " + e.getMessage(), e);
        } finally {
            ConexionDB.cerrar(claves);
            ConexionDB.cerrar(ps);
            ConexionDB.cerrar(con);
        }
    }

    /** Devuelve todos los repartidores. */
    public List<Repartidor> readAll() {
        List<Repartidor> lista = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(SQL_SELECT);
            rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new Repartidor(rs.getInt("id"), rs.getString("nombre")));
            }
            return lista;
        } catch (SQLException e) {
            throw new DatosException("No se pudieron listar los repartidores: " + e.getMessage(), e);
        } finally {
            ConexionDB.cerrar(rs);
            ConexionDB.cerrar(ps);
            ConexionDB.cerrar(con);
        }
    }

    /** Actualiza el nombre del repartidor indicado. */
    public void update(Repartidor r) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(SQL_UPDATE);
            ps.setString(1, r.getNombre());
            ps.setInt(2, r.getId());
            if (ps.executeUpdate() == 0) {
                throw new DatosException("El repartidor " + r.getId() + " ya no existe.", null);
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo actualizar el repartidor: " + e.getMessage(), e);
        } finally {
            ConexionDB.cerrar(ps);
            ConexionDB.cerrar(con);
        }
    }

    /** Elimina el repartidor; falla con mensaje claro si tiene entregas asociadas. */
    public void delete(int id) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(SQL_DELETE);
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new DatosException("El repartidor " + id + " ya no existe.", null);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DatosException("No se puede eliminar: el repartidor tiene entregas asociadas.", e);
        } catch (SQLException e) {
            throw new DatosException("No se pudo eliminar el repartidor: " + e.getMessage(), e);
        } finally {
            ConexionDB.cerrar(ps);
            ConexionDB.cerrar(con);
        }
    }
}
