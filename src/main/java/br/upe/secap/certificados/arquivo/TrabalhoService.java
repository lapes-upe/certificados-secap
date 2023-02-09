package br.upe.secap.certificados.arquivo;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrabalhoService {

	@Autowired
	private ITrabalhoDAO dao;

	public List<Trabalho> listar() {
		return this.dao.findAllByOrderByApresentador();
	}

	public List<Trabalho> listar(Long idArquivo) {
		if (idArquivo == null) {
			throw new RuntimeException("Informe o identificador do arquivo para listar os apresentadores de trabalho");
		}

		return this.dao.findAllByArquivoIdOrderByApresentador(idArquivo);
	}

	public Trabalho procurar(Long idTrabalho) {
		if (idTrabalho == null) {
			throw new RuntimeException("Informe o identificador do apresentador de trabalho");
		}

		Optional<Trabalho> trabalho = this.dao.findById(idTrabalho);

		if (!trabalho.isPresent()) {
			throw new RuntimeException("Não existe apresentador de trabalho com o identificador:" + idTrabalho);
		}

		return trabalho.get();
	}

}
