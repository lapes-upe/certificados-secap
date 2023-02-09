package br.upe.secap.certificados.arquivo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IOuvinteDAO extends CrudRepository<Ouvinte, Long> {

	List<Ouvinte> findAllByArquivoIdOrderByNome(Long idArquivo);
	List<Ouvinte> findAllByOrderByNome();
	
	long countByArquivoId(Long idArquivo);
	
	Ouvinte findByEmail(String email);
}
