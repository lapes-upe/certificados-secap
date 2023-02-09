package br.upe.secap.certificados.arquivo;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OuvinteService {

	@Autowired
	private IOuvinteDAO dao;

	public List<Ouvinte> listar(Long idArquivo) {
		if (idArquivo == null) {
			throw new RuntimeException("Informe o identificador do arquivo para listar os ouvintes");
		}

		return this.dao.findAllByArquivoIdOrderByNome(idArquivo);
	}
	

	public List<Ouvinte> listar() {
		return this.dao.findAllByOrderByNome();
	}

	public Ouvinte procurar(Long idOuvinte) {
		if (idOuvinte == null) {
			throw new RuntimeException("Informe o identificador do ouvinte");
		}

		Optional<Ouvinte> ouvinte = this.dao.findById(idOuvinte);

		if (!ouvinte.isPresent()) {
			throw new RuntimeException("Não existe ouvinte com identificador:" + idOuvinte);
		}

		return ouvinte.get();
	}

	public Ouvinte procurar(String email) {
		if (email == null) {
			throw new RuntimeException("Informe o email do ouvinte");
		}

		Ouvinte ouvinte = this.dao.findByEmail(email);

		if (ouvinte == null) {
			throw new RuntimeException("Não existe ouvinte com o email:" + email);
		}

		return ouvinte;
	}
}
