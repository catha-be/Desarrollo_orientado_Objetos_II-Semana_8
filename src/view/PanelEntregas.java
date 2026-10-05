package view;

import dao.DatosException;
import dao.EntregaDAO;
import dao.PedidoDAO;
import dao.RepartidorDAO;
import model.Entrega;
import model.Pedido;
import model.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// Pestaña de gestión de entregas: combos de Pedido/Repartidor cargados desde la BD.
public class PanelEntregas extends JPanel {

    private static final String TODOS = "Todos";
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final EntregaDAO dao = new EntregaDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final RepartidorDAO repartidorDAO = new RepartidorDAO();
    private final Runnable alCambiar;

    private final JComboBox<Pedido> cmbPedido = new JComboBox<>();
    private final JComboBox<Repartidor> cmbRepartidor = new JComboBox<>();
    private final JTextField txtFecha = new JTextField(9);
    private final JTextField txtHora = new JTextField(6);
    private final JComboBox<Object> filtroPedido = new JComboBox<>();
    private final JComboBox<Object> filtroRepartidor = new JComboBox<>();
    private boolean cargandoCombos = false; // evita refrescar la tabla mientras se rellenan los filtros

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Pedido", "Repartidor", "Fecha", "Hora"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    // Entregas mostradas actualmente (misma posición que las filas de la tabla)
    private java.util.List<Entrega> entregasVisibles = new java.util.ArrayList<>();

    public PanelEntregas(Runnable alCambiar) {
        this.alCambiar = alCambiar;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formulario.add(new JLabel("Pedido:"));
        formulario.add(cmbPedido);
        formulario.add(new JLabel("Repartidor:"));
        formulario.add(cmbRepartidor);
        formulario.add(new JLabel("Fecha (AAAA-MM-DD):"));
        formulario.add(txtFecha);
        formulario.add(new JLabel("Hora (HH:mm):"));
        formulario.add(txtHora);
        JButton btnRegistrar = new JButton("Registrar");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");
        formulario.add(btnRegistrar);
        formulario.add(btnEditar);
        formulario.add(btnEliminar);
        formulario.add(btnLimpiar);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Filtrar por pedido:"));
        filtros.add(filtroPedido);
        filtros.add(new JLabel("por repartidor:"));
        filtros.add(filtroRepartidor);

        JPanel norte = new JPanel(new GridLayout(2, 1));
        norte.add(formulario);
        norte.add(filtros);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) cargarFormulario(tabla.getSelectedRow());
        });

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnRegistrar.addActionListener(e -> registrar());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        filtroPedido.addActionListener(e -> { if (!cargandoCombos) refrescarTabla(); });
        filtroRepartidor.addActionListener(e -> { if (!cargandoCombos) refrescarTabla(); });

        limpiar();
        refrescar();
    }

    /** Recarga combos (pedidos y repartidores) y tabla desde la BD. */
    public void refrescar() {
        cargarCombos();
        refrescarTabla();
    }

    // Los combos muestran "id - texto" (toString del modelo) y conservan el objeto con su id
    private void cargarCombos() {
        cargandoCombos = true;
        try {
            Object pedidoSel = cmbPedido.getSelectedItem();
            Object repartidorSel = cmbRepartidor.getSelectedItem();
            Object filtroPedSel = filtroPedido.getSelectedItem();
            Object filtroRepSel = filtroRepartidor.getSelectedItem();

            cmbPedido.removeAllItems();
            filtroPedido.removeAllItems();
            filtroPedido.addItem(TODOS);
            for (Pedido p : pedidoDAO.readAll()) {
                cmbPedido.addItem(p);
                filtroPedido.addItem(p);
            }
            cmbRepartidor.removeAllItems();
            filtroRepartidor.removeAllItems();
            filtroRepartidor.addItem(TODOS);
            for (Repartidor r : repartidorDAO.readAll()) {
                cmbRepartidor.addItem(r);
                filtroRepartidor.addItem(r);
            }
            restaurarSeleccion(cmbPedido, pedidoSel);
            restaurarSeleccion(cmbRepartidor, repartidorSel);
            restaurarSeleccion(filtroPedido, filtroPedSel);
            restaurarSeleccion(filtroRepartidor, filtroRepSel);
        } catch (DatosException ex) {
            Validador.error(this, ex.getMessage());
        } finally {
            cargandoCombos = false;
        }
    }

    // Reselecciona el elemento previo (comparando por id) tras recargar un combo
    private void restaurarSeleccion(JComboBox<?> combo, Object anterior) {
        if (anterior == null) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (mismoElemento(item, anterior)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private boolean mismoElemento(Object a, Object b) {
        if (a instanceof Pedido pa && b instanceof Pedido pb) return pa.getId() == pb.getId();
        if (a instanceof Repartidor ra && b instanceof Repartidor rb) return ra.getId() == rb.getId();
        return a.equals(b);
    }

    private void refrescarTabla() {
        try {
            Integer idPedido = filtroPedido.getSelectedItem() instanceof Pedido p ? p.getId() : null;
            Integer idRepartidor = filtroRepartidor.getSelectedItem() instanceof Repartidor r ? r.getId() : null;
            entregasVisibles = dao.readFiltrado(idPedido, idRepartidor);
            modelo.setRowCount(0);
            for (Entrega en : entregasVisibles) {
                modelo.addRow(new Object[]{en.getId(),
                        en.getIdPedido() + " - " + en.getDireccionPedido(),
                        en.getIdRepartidor() + " - " + en.getNombreRepartidor(),
                        en.getFecha(), en.getHora().format(FORMATO_HORA)});
            }
        } catch (DatosException ex) {
            Validador.error(this, ex.getMessage());
        }
    }

    private void cargarFormulario(int fila) {
        Entrega en = entregasVisibles.get(fila);
        for (int i = 0; i < cmbPedido.getItemCount(); i++) {
            if (cmbPedido.getItemAt(i).getId() == en.getIdPedido()) cmbPedido.setSelectedIndex(i);
        }
        for (int i = 0; i < cmbRepartidor.getItemCount(); i++) {
            if (cmbRepartidor.getItemAt(i).getId() == en.getIdRepartidor()) cmbRepartidor.setSelectedIndex(i);
        }
        txtFecha.setText(en.getFecha().toString());
        txtHora.setText(en.getHora().format(FORMATO_HORA));
    }

    // Valida el formulario y arma la entrega; devuelve null si algo es inválido
    private Entrega leerFormulario(int id) {
        Pedido pedido = (Pedido) cmbPedido.getSelectedItem();
        Repartidor repartidor = (Repartidor) cmbRepartidor.getSelectedItem();
        if (pedido == null) {
            Validador.aviso(this, "Debes registrar y seleccionar un pedido.");
            return null;
        }
        if (repartidor == null) {
            Validador.aviso(this, "Debes registrar y seleccionar un repartidor.");
            return null;
        }
        LocalDate fecha = Validador.fecha(this, txtFecha.getText());
        if (fecha == null) return null;
        LocalTime hora = Validador.hora(this, txtHora.getText());
        if (hora == null) return null;
        return new Entrega(id, pedido.getId(), repartidor.getId(), fecha, hora);
    }

    private void registrar() {
        Entrega en = leerFormulario(0);
        if (en == null) return;
        try {
            dao.create(en);
            despuesDeCambio("Entrega registrada correctamente.");
        } catch (DatosException ex) {
            Validador.error(this, ex.getMessage());
        }
    }

    private void editar() {
        int id = idSeleccionado("editar");
        if (id < 0) return;
        Entrega en = leerFormulario(id);
        if (en == null) return;
        try {
            dao.update(en);
            despuesDeCambio("Entrega actualizada correctamente.");
        } catch (DatosException ex) {
            Validador.error(this, ex.getMessage());
        }
    }

    private void eliminar() {
        int id = idSeleccionado("eliminar");
        if (id < 0) return;
        if (!Validador.confirmar(this, "¿Eliminar la entrega " + id + "?")) return;
        try {
            dao.delete(id);
            despuesDeCambio("Entrega eliminada correctamente.");
        } catch (DatosException ex) {
            Validador.error(this, ex.getMessage());
        }
    }

    private int idSeleccionado(String accion) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Validador.aviso(this, "Selecciona una entrega de la tabla para " + accion + ".");
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
        tabla.clearSelection();
        LocalDateTime ahora = LocalDateTime.now();
        txtFecha.setText(ahora.toLocalDate().toString());
        txtHora.setText(ahora.toLocalTime().format(FORMATO_HORA));
    }
}
