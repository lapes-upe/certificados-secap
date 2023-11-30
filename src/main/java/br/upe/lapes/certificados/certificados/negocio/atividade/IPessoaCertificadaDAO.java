package br.upe.lapes.certificados.certificados.negocio.atividade;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import br.upe.lapes.certificados.certificados.negocio.certificado.TipoCertificadoEnum;

public interface IPessoaCertificadaDAO extends CrudRepository<PessoaCertificada, Long> {

	@Query("FROM PessoaCertificada p WHERE p.arquivo.id = :idArquivo AND LENGTH(TRIM(p.email)) > 0 AND p.certificadoEnviado = false ORDER BY p.nome")
	List<PessoaCertificada> findAllNotSendedByArquivoId(Long idArquivo, Pageable pageable);

	@Query("FROM PessoaCertificada p WHERE p.arquivo.id = :idArquivo ORDER BY p.nome")
	List<PessoaCertificada> findAllByArquivoIdOrderByNome(Long idArquivo);
	
	List<PessoaCertificada> findByTipoOrderByNome(TipoCertificadoEnum tipo);

	long countByArquivoId(Long idArquivo);

	PessoaCertificada findByEmail(String email);
	
	List<PessoaCertificada> findAll(Pageable pageable);

}
