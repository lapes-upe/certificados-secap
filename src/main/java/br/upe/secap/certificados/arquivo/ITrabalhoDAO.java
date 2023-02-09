package br.upe.secap.certificados.arquivo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITrabalhoDAO extends CrudRepository<Trabalho, Long> {

	List<Trabalho> findAllByArquivoIdOrderByApresentador(Long idArquivo);

	List<Trabalho> findAllByOrderByApresentador();

	long countByArquivoId(Long idArquivo);

	Trabalho findByEmail(String email);
}
