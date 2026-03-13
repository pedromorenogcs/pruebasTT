/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.timestenconnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Clase para conectarse a Oracle TimesTen 22 y ejecutar consultas SQL.
 * 
 * Dependencia requerida en pom.xml o classpath:
 *   <dependency> ttjdbc8.jar o ttjdbc17.jar (según versión de JDK) </dependency>
 *   Ubicación típica: $TIMESTEN_HOME/lib/ttjdbc17.jar
 */
public class TimesTenConnection {

    // ── Configuración de conexión ──────────────────────────────────────────────
    private static final String DRIVER   = "com.timesten.jdbc.TimesTenDriver";
    private static final String URL      = "jdbc:timesten:client:DSN=DEG_APP_DB_CS"; 
    // Para conexión client/server: "jdbc:timesten:client:DSN=miDSN;TTC_SERVER=host;TTC_PORT=17000"
    private static final String USER     = "adm";
    private static final String PASSWORD = "admin";

    // ── Método principal de ejemplo ────────────────────────────────────────────
    public static void main(String[] args) {
        String sql = "SELECT col1 FROM IUM.t";
        //String sql = "select HOSTNAME from SYS.V$HOST_NAME";

        try {
            Class.forName(DRIVER);
            System.out.println("Driver cargado correctamente.");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver TimesTen no encontrado. Verifica el classpath.");
            System.err.println("Jar requerido: $TIMESTEN_HOME/lib/ttjdbc17.jar");
            return;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            System.out.println("Conexión establecida con TimesTen 22.");
            conn.setAutoCommit(false); // TimesTen funciona mejor con autocommit explícito

            ejecutarConsulta(conn, sql, "ACTIVO");
            // ejecutarUpdate(conn, "UPDATE mi_tabla SET estado = ? WHERE id = ?", "INACTIVO", 1);

        } catch (SQLException e) {
            System.err.println("Error SQL: " + e.getMessage());
            System.err.println("SQLState: "  + e.getSQLState());
            System.err.println("Código:   "  + e.getErrorCode());
        }
    }

    // ── Ejecutar SELECT ────────────────────────────────────────────────────────
    public static void ejecutarConsulta(Connection conn, String sql, Object... params)
            throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            // Bind de parámetros
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();

                // Cabecera
                for (int i = 1; i <= cols; i++) {
                    System.out.printf("%-20s", meta.getColumnName(i));
                }
                System.out.println();
                System.out.println("-".repeat(20 * cols));

                // Filas
                int filas = 0;
                while (rs.next()) {
                    for (int i = 1; i <= cols; i++) {
                        System.out.printf("%-20s", rs.getString(i));
                    }
                    System.out.println();
                    filas++;
                }
                System.out.println("\nTotal filas: " + filas);
            }
        }
    }

    // ── Ejecutar INSERT / UPDATE / DELETE ─────────────────────────────────────
/*    public static int ejecutarUpdate(Connection conn, String sql, Object... params)
            throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            int afectadas = ps.executeUpdate();
            conn.commit();
            System.out.println("Filas afectadas: " + afectadas);
            return afectadas;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        }
    }*/
}