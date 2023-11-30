package br.upe.lapes.certificados.certificados.negocio.certificado.beans;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class CertificadoVO {

	private byte[] certificado;
	private String nomeArquivo;
}
