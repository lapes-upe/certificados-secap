package br.upe.lapes.certificados.certificados.negocio.atividade;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.upe.lapes.certificados.certificados.negocio.certificado.TipoCertificadoEnum;

@Service
public class ParticipanteService {

	@Autowired
	private IPessoaCertificadaDAO dao;

	public List<PessoaCertificada> listar(Long idArquivo) {
		if (idArquivo == null) {
			throw new RuntimeException("Informe o identificador do arquivo para listar os participantes");
		}

		return this.dao.findAllByArquivoIdOrderByNome(idArquivo);
	}
	
	public List<PessoaCertificada> listar(TipoCertificadoEnum tipo) {
		if (tipo == null) {
			throw new RuntimeException("Informe o tipo do arquivo para listar os participantes");
		}

		return this.dao.findByTipoOrderByNome(tipo);
	}
	

	public PessoaCertificada procurar(Long idOuvinte) {
		if (idOuvinte == null) {
			throw new RuntimeException("Informe o identificador do participante");
		}

		Optional<PessoaCertificada> ouvinte = this.dao.findById(idOuvinte);

		if (!ouvinte.isPresent()) {
			throw new RuntimeException("Não existe participante com identificador:" + idOuvinte);
		}

		return ouvinte.get();
	}

	public PessoaCertificada procurar(String email) {
		if (email == null) {
			throw new RuntimeException("Informe o email do participante");
		}

		PessoaCertificada ouvinte = this.dao.findByEmail(email);

		if (ouvinte == null) {
			throw new RuntimeException("Não existe participante com o email:" + email);
		}

		return ouvinte;
	}
}
