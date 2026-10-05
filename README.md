# Semana 8 — CRUD completo con JDBC y Swing (SpeedFast)

**Curso:** Desarrollo Orientado a Objetos II · **Autor:** Catherine Bernardín

Aplicación que completa el ciclo funcional de SpeedFast: gestiona **repartidores, pedidos y entregas**
con operaciones CRUD sobre MySQL (JDBC + `PreparedStatement` + `ResultSet`) desde una interfaz Swing.

## Estructura (capas modelo / dao / vista)

```
Semana_8/
├── sql/speedfast_db.sql        esquema del enunciado (repartidores, pedidos, entregas)
├── lib/mysql-connector-j-*.jar driver JDBC
└── src/
    ├── model/  TipoPedido, EstadoPedido, Pedido, Repartidor, Entrega
    ├── dao/    ConexionDB, DatosException, RepartidorDAO, PedidoDAO, EntregaDAO
    ├── view/   VentanaPrincipal, PanelRepartidores, PanelPedidos, PanelEntregas, Validador
    └── main/   Main
```

## Qué hace cada paso

1. **BD y conexión:** `ConexionDB` (con `probarConexion()` al iniciar; muestra un aviso si MySQL no responde).
2. **DAO:** `RepartidorDAO`, `PedidoDAO` y `EntregaDAO` con `create()`, `readAll()`, `update()` y `delete()`.
   (El enunciado menciona `ClienteDAO`, pero las entidades del caso son repartidores; se implementó `RepartidorDAO`.)
   - `PedidoDAO.readFiltrado(estado, tipo)` y `EntregaDAO.readFiltrado(idPedido, idRepartidor)` implementan los
     filtros opcionales con `(? IS NULL OR col = ?)`.
   - `EntregaDAO` usa `JOIN` para mostrar dirección y nombre en la tabla.
3. **Interfaz:** pestañas con formulario + `JTable` que se recarga tras cada operación. En **Entregas**, los
   `JComboBox` de Pedido y Repartidor se cargan desde la BD, muestran `id - texto` y conservan el objeto (con su id);
   se refrescan cuando se crean/editan/eliminan pedidos o repartidores. Al seleccionar una fila se carga el formulario.
4. **Validaciones y errores:** campos obligatorios y largo máximo, formato de fecha (`AAAA-MM-DD`) y hora (`HH:mm`),
   selección de fila antes de editar/eliminar y confirmación al eliminar. Los `SQLException` se envuelven en
   `DatosException` y se muestran con `JOptionPane` (incluye mensaje claro cuando la FK impide eliminar un
   pedido/repartidor con entregas). Todos los recursos se cierran en `finally`.

## Cómo ejecutar

1. Ejecutar el script: `mysql -u root -p < sql/speedfast_db.sql`
2. Definir la contraseña en `dao/ConexionDB.java` o en la variable de entorno `SPEEDFAST_DB_PASSWORD`.
3. Abrir el proyecto en IntelliJ (el `.iml` ya incluye `lib/`) y ejecutar `main/Main.java`, o por consola:
   ```bash
   javac -d out $(find src -name "*.java")
   java -cp "out:lib/*" main.Main
   ```
