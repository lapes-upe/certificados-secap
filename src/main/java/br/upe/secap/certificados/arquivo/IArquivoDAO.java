package br.upe.secap.certificados.arquivo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IArquivoDAO extends CrudRepository<Arquivo, Long>{

	List<Arquivo> findAllByOrderByNome();
}
