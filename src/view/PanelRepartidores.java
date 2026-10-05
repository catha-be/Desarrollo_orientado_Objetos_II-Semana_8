package view;

import dao.DatosException;
import dao.RepartidorDAO;
import model.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// Pestaña de gestión de repartidores: formulario + JTable + CRUD.
public class PanelRepartidores extends JPanel {

    private final RepartidorDAO dao = new RepartidorDAO();
    private final Runnable alCambiar; // avisa a la ventana para refrescar los combos de otros paneles

    private final JTextField txtNombre = new JTextField(25);
    private final DefaultTableModel modelo = new DefaultTableModel(new String[]{"ID", "Nombre"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    public PanelRepartidores(Runnable alCambiar) {
        this.alCambiar = alCambiar;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formulario.add(new JLabel("Nombre:"));
        formulario.add(txtNombre);
        JButton btnRegistrar = new JButton("Registrar");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");
        formulario.add(btnRegistrar);
        formulario.add(btnEditar);
        formulario.add(btnEliminar);
        formulario.add(btnLimpiar);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                txtNombre.setText(String.valueOf(modelo.getValueAt(tabla.getSelectedRow(), 1)));
            }
        });

        add(formulario, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnRegistrar.addActionListener(e -> registrar());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());

        refrescar();
    }

    /** Recarga la tabla desde la BD. */
    public void refrescar() {
        try {
            modelo.setRowCount(0);
            for (Repartidor r : dao.readAll()) {
                modelo.addRow(new Object[]{r.getId(), r.getNombre()});
            }
        } catch (DatosException ex) {
            Validador.error(this, ex.getMessage());
        }
    }

    private void registrar() {
        String nombre = Validador.textoObligatorio(this, txtNombre.getText(), "Nombre", 100);
        if (nombre == null) return;
        try {
            dao.create(new Repartidor(nombre));
            despuesDeCambio("Repartidor registrado correctamente.");
        } catch (DatosException ex) {
            Validador.error(this, ex.getMessage());
        }
    }

    private void editar() {
        int id = idSeleccionado("editar");
        if (id < 0) return;
        String nombre = Validador.textoObligatorio(this, txtNombre.getText(), "Nombre", 100);
        if (nombre == null) return;
        try {
            dao.update(new Repartidor(id, nombre));
            despuesDeCambio("Repartidor actualizado correctamente.");
        } catch (DatosException ex) {
            Validador.error(this, ex.getMessage());
        }
    }

    private void eliminar() {
        int id = idSeleccionado("eliminar");
        if (id < 0) return;
        if (!Validador.confirmar(this, "¿Eliminar el repartidor " + id + "?")) return;
        try {
            dao.delete(id);
            despuesDeCambio("Repartidor eliminado correctamente.");
        } catch (DatosException ex) {
            Validador.error(this, ex.getMessage());
        }
    }

    private int idSeleccionado(String accion) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Validador.aviso(this, "Selecciona un repartidor de la tabla para " + accion + ".");
            return -1;
        }
        return (int) modelo.getValueAt(fila, 0);
    }

    private void despuesDeCambio(String mensaje) {
        limpiar();
        alCambiar.run();
        Validador.exito(this, mensaje);
    }

    private void limpiar() {
        txtNombre.setText("");
        tabla.clearSelection();
    }
}
