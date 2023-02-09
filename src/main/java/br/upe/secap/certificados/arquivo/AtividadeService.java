package br.upe.secap.certificados.arquivo;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AtividadeService {

	@Autowired
	private IAtividadeDAO dao;

	public List<Atividade> listar() {
		return this.dao.findAllByOrderByNome();
	}

	public List<Atividade> listar(Long idArquivo) {
		if (idArquivo == null) {
			throw new RuntimeException("Informe o identificador do arquivo para listar as atividades");
		}

		return this.dao.findAllByArquivoIdOrderByNome(idArquivo);
	}

	public Atividade procurar(Long idAtividade) {
		if (idAtividade == null) {
			throw new RuntimeException("Informe o identificador da atividade");
		}

		Optional<Atividade> atividade = this.dao.findById(idAtividade);

		if (!atividade.isPresent()) {
			throw new RuntimeException("Não existe atividade com o identificador:" + idAtividade);
		}

		return atividade.get();
	}

}
