package br.upe.lapes.certificados.certificados.negocio.certificado;

import br.upe.lapes.certificados.certificados.negocio.arquivo.Arquivo;

public class Service {

	public int inserir(Arquivo arquivo) {
		Repositorio repo = new Repositorio();
		return repo.inserir(arquivo);
	}
	
}
