package br.upe.secap.certificados.arquivo;

import java.io.Serializable;

import org.joda.time.LocalDateTime;

import br.upe.secap.certificados.certificado.TipoCertificadoEnum;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public class ArquivoEnvelope implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String nome;
	private Long tamanho;
	private TipoCertificadoEnum tipo;
	private LocalDateTime dataInclusao;
	private LocalDateTime dataUltimaAlteracao;
	
	public static ArquivoEnvelope get(Arquivo arquivo) {
		return ArquivoEnvelope.builder().id(arquivo.getId()).nome(arquivo.getNome())
		.tamanho(arquivo.getTamanho()).tipo(arquivo.getTipo()).dataInclusao(arquivo.getDataInclusao())
		.dataUltimaAlteracao(arquivo.getDataUltimaAlteracao()).build();
	}
}
