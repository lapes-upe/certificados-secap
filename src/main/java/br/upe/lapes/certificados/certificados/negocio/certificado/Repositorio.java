package br.upe.lapes.certificados.certificados.negocio.certificado;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import br.upe.lapes.certificados.certificados.negocio.arquivo.Arquivo;

public class Repositorio {

	public int inserir(Arquivo arquivo) {
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			
			Connection conexao = DriverManager.getConnection("jdbc:odbc:sistema");
			
			PreparedStatement stmt = conexao.prepareStatement("INSERT INTO arquivo VALUES(?,?,now())");
			
			stmt.setLong(1, arquivo.getId());
			stmt.setString(2, arquivo.getNome());
			
			return stmt.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Ocorreu um erro ao salvar os dados do arquivo", e);
		}
	}
}


