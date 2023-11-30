package br.upe.lapes.certificados.certificados.negocio.arquivo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface IArquivoDAO extends CrudRepository<Arquivo, Long> {

	List<Arquivo> findAllByOrderByNome();
}
