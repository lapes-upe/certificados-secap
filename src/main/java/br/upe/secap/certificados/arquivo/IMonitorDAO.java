package br.upe.secap.certificados.arquivo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IMonitorDAO extends CrudRepository<Monitor, Long> {

	List<Monitor> findAllByArquivoIdOrderByNome(Long idArquivo);

	List<Monitor> findAllByOrderByNome();

	long countByArquivoId(Long idArquivo);

	Monitor findByEmail(String email);
}
