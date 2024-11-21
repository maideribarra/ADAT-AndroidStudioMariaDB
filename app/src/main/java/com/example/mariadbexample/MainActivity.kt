package com.example.mariadbexample


import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nombreInput = findViewById<EditText>(R.id.nombreInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val guardarBtn = findViewById<Button>(R.id.guardarBtn)
        val listarBtn = findViewById<Button>(R.id.listarBtn)
        val resultadosView = findViewById<TextView>(R.id.resultadosView)

        // Botón para guardar datos en la base de datos
        guardarBtn.setOnClickListener {
            val nombre = nombreInput.text.toString()
            val email = emailInput.text.toString()
            print("guardo persona")
            guardarEnMariaDB(nombre, email)
        }

        // Botón para listar datos desde la base de datos
        listarBtn.setOnClickListener {
            val registros = obtenerRegistros()
            resultadosView.text = registros.joinToString("\n") { "${it["id"]}: ${it["nombre"]} - ${it["email"]}" }
        }
    }

    // Conexión a la base de datos
    private fun obtenerConexion(): Connection? {

        val url = "jdbc:mariadb://10.0.2.2:3303/tu_base"
        val user = "root"
        val password = "1234"

        return try {
            DriverManager.getConnection(url, user, password)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Metodo para guardar datos
    private fun guardarEnMariaDB(nombre: String, email: String) {
        val connection=null
        thread {
            val connection = obtenerConexion()

            // Si la conexión es exitosa, actualiza el UI en el hilo principal
            runOnUiThread {
                if (connection != null) {
                    print("Conexión exitosa a la base de datos.")
                    val query = "INSERT INTO registros (nombre, email) VALUES (?, ?)"
                    val statement: PreparedStatement = connection.prepareStatement(query)
                    statement.setString(1, nombre)
                    statement.setString(2, email)
                    statement.executeUpdate()
                    connection.close()
                } else {
                    print("Error al conectar a la base de datos.")
                }
            }
        }
    }

    // Metodo para obtener registros
    private fun obtenerRegistros(): List<Map<String, String>> {
        val conexion = obtenerConexion()
        val registros = mutableListOf<Map<String, String>>()

        if (conexion != null) {
            val query = "SELECT * FROM registros"
            val statement = conexion.createStatement()
            val resultSet: ResultSet = statement.executeQuery(query)

            while (resultSet.next()) {
                registros.add(
                    mapOf(
                        "id" to resultSet.getString("id"),
                        "nombre" to resultSet.getString("nombre"),
                        "email" to resultSet.getString("email")
                    )
                )
            }
            conexion.close()
        }

        return registros
    }
}
