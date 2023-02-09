package br.upe.secap.certificados.arquivo;

import java.io.Serializable;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class OuvinteEnvelope implements Serializable {

	private static final long serialVersionUID = 8550809576403182019L;

	private Long id;

	private String nome;
	private String email;
	private Integer cargaHoraria;
	private Integer qtdComentarios;
	private LocalTime permanencia;
	private Boolean credenciado;
	private LocalDateTime primeiroAcesso;
	private LocalDateTime primeiroAcessoTransmissao;

	private LocalDateTime dataInclusao;
	private LocalDateTime dataUltimaAlteracao;

	private Boolean certificadoEnviado;

	private Long arquivo;

	public static OuvinteEnvelope get(Ouvinte ouvinte) {
		return OuvinteEnvelope.builder().id(ouvinte.getId()).nome(ouvinte.getNome()).email(ouvinte.getEmail())
				.cargaHoraria(ouvinte.getCargaHoraria()).qtdComentarios(ouvinte.getQtdComentarios())
				.permanencia(ouvinte.getPermanencia()).credenciado(ouvinte.getCredenciado())
				.primeiroAcesso(ouvinte.getPrimeiroAcesso())
				.primeiroAcessoTransmissao(ouvinte.getPrimeiroAcessoTransmissao())
				.certificadoEnviado(ouvinte.getCertificadoEnviado()).arquivo(ouvinte.getArquivo().getId())
				.dataInclusao(ouvinte.getDataInclusao()).dataUltimaAlteracao(ouvinte.getDataUltimaAlteracao()).build();
	}

}
