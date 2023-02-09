package br.upe.secap.certificados.certificado;

import br.upe.secap.certificados.arquivo.Arquivo;

public class Service {

	public int inserir(Arquivo arquivo) {
		Repositorio repo = new Repositorio();
		return repo.inserir(arquivo);
	}
	
}
