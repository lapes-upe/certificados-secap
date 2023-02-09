package br.upe.secap.certificados.arquivo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAtividadeDAO extends CrudRepository<Atividade, Long> {

	List<Atividade> findAllByArquivoIdOrderByNome(Long idArquivo);

	List<Atividade> findAllByOrderByNome();

	long countByArquivoId(Long idArquivo);

	Atividade findByEmail(String email);
}
