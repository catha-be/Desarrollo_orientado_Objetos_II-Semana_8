package dao;

import model.Entrega;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

// CRUD de la tabla entregas (une pedidos y repartidores).
public class EntregaDAO {

    private static final String SQL_INSERT =
            "INSERT INTO entregas (id_pedido, id_repartidor, fecha, hora) VALUES (?, ?, ?, ?)";
    // JOIN para mostrar dirección y nombre; los filtros null se ignoran
    private static final String SQL_SELECT =
            "SELECT e.id, e.id_pedido, e.id_repartidor, e.fecha, e.hora, p.direccion, r.nombre "
                    + "FROM entregas e "
                    + "JOIN pedidos p ON p.id = e.id_pedido "
                    + "JOIN repartidores r ON r.id = e.id_repartidor "
                    + "WHERE (? IS NULL OR e.id_pedido = ?) AND (? IS NULL OR e.id_repartidor = ?) "
                    + "ORDER BY e.id";
    private static final String SQL_UPDATE =
            "UPDATE entregas SET id_pedido = ?, id_repartidor = ?, fecha = ?, hora = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM entregas WHERE id = ?";

    /** Inserta la entrega y le asigna el ID generado por la BD. */
    public void create(Entrega en) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet claves = null;
        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, en.getIdPedido());
            ps.setInt(2, en.getIdRepartidor());
            ps.setDate(3, Date.valueOf(en.getFecha()));
            ps.setTime(4, Time.valueOf(en.getHora()));
            ps.executeUpdate();
            claves = ps.getGeneratedKeys();
            if (claves.next()) en.setId(claves.getInt(1));
        } catch (SQLException e) {
            throw new DatosException("No se pudo registrar la entrega: " + e.getMessage(), e);
        } finally {
            ConexionDB.cerrar(claves);
            ConexionDB.cerrar(ps);
            ConexionDB.cerrar(con);
        }
    }

    /** Lista todas las entregas. */
    public List<Entrega> readAll() {
        return readFiltrado(null, null);
    }

    /** Lista entregas por pedido y/o repartidor (null = sin filtro). */
    public List<Entrega> readFiltrado(Integer idPedido, Integer idRepartidor) {
        List<Entrega> lista = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(SQL_SELECT);
            asignarOpcional(ps, 1, idPedido);
            asignarOpcional(ps, 3, idRepartidor);
            rs = ps.executeQuery();
            while (rs.next()) {
                Entrega en = new Entrega(rs.getInt("id"), rs.getInt("id_pedido"), rs.getInt("id_repartidor"),
                        rs.getDate("fecha").toLocalDate(), rs.getTime("hora").toLocalTime());
                en.setDetalle(rs.getString("direccion"), rs.getString("nombre"));
                lista.add(en);
            }
            return lista;
        } catch (SQLException e) {
            throw new DatosException("No se pudieron listar las entregas: " + e.getMessage(), e);
        } finally {
            ConexionDB.cerrar(rs);
            ConexionDB.cerrar(ps);
            ConexionDB.cerrar(con);
        }
    }

    /** Actualiza pedido, repartidor, fecha y hora de la entrega. */
    public void update(Entrega en) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(SQL_UPDATE);
            ps.setInt(1, en.getIdPedido());
            ps.setInt(2, en.getIdRepartidor());
            ps.setDate(3, Date.valueOf(en.getFecha()));
            ps.setTime(4, Time.valueOf(en.getHora()));
            ps.setInt(5, en.getId());
            if (ps.executeUpdate() == 0) {
                throw new DatosException("La entrega " + en.getId() + " ya no existe.", null);
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo actualizar la entrega: " + e.getMessage(), e);
        } finally {
            ConexionDB.cerrar(ps);
            ConexionDB.cerrar(con);
        }
    }

    /** Elimina la entrega indicada. */
    public void delete(int id) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(SQL_DELETE);
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new DatosException("La entrega " + id + " ya no existe.", null);
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo eliminar la entrega: " + e.getMessage(), e);
        } finally {
            ConexionDB.cerrar(ps);
            ConexionDB.cerrar(con);
        }
    }

    // Asigna el mismo valor a los dos parámetros "(? IS NULL OR col = ?)"
    private void asignarOpcional(PreparedStatement ps, int indice, Integer valor) throws SQLException {
        if (valor == null) {
            ps.setNull(indice, java.sql.Types.INTEGER);
            ps.setNull(indice + 1, java.sql.Types.INTEGER);
        } else {
            ps.setInt(indice, valor);
            ps.setInt(indice + 1, valor);
        }
    }
}
