package br.upe.secap.certificados.arquivo;

import java.io.Serializable;

import org.joda.time.LocalDateTime;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class TrabalhoEnvelope implements Serializable {

	private static final long serialVersionUID = 8550809576403182019L;

	private Long id;

	private String titulo;
	private String email;
	private String apresentador;
	private String coautores;
	private Long identificador;

	private LocalDateTime dataInclusao;
	private LocalDateTime dataUltimaAlteracao;

	private Boolean certificadoEnviado;

	private Long arquivo;

	public static TrabalhoEnvelope get(Trabalho trabalho) {
		return TrabalhoEnvelope.builder().id(trabalho.getId()).titulo(trabalho.getTitulo()).email(trabalho.getEmail())
				.apresentador(trabalho.getApresentador()).coautores(trabalho.getCoautores())
				.identificador(trabalho.getIdentificador()).certificadoEnviado(trabalho.getCertificadoEnviado())
				.arquivo(trabalho.getArquivo().getId()).dataInclusao(trabalho.getDataInclusao())
				.dataUltimaAlteracao(trabalho.getDataUltimaAlteracao()).build();
	}

}
