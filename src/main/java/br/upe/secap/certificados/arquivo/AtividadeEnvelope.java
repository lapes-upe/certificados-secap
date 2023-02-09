package br.upe.secap.certificados.arquivo;

import java.io.Serializable;

import org.joda.time.LocalDateTime;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class AtividadeEnvelope implements Serializable {

	private static final long serialVersionUID = 8550809576403182019L;

	private Long id;

	private String nome;
	private String email;
	private Double cargaHoraria;

	private TipoAtividadeEnum tipoAtividade;
	private TipoParticipacaoAtividadeEnum tipoParticipacao;

	private LocalDateTime dataInclusao;
	private LocalDateTime dataUltimaAlteracao;

	private Boolean certificadoEnviado;

	private Long arquivo;

	public static AtividadeEnvelope get(Atividade atividade) {
		return AtividadeEnvelope.builder().id(atividade.getId()).nome(atividade.getNome()).email(atividade.getEmail())
				.cargaHoraria(atividade.getCargaHoraria()).certificadoEnviado(atividade.getCertificadoEnviado())
				.arquivo(atividade.getArquivo().getId()).dataInclusao(atividade.getDataInclusao())
				.tipoAtividade(atividade.getTipoAtividade()).tipoParticipacao(atividade.getTipoParticipacao())
				.dataUltimaAlteracao(atividade.getDataUltimaAlteracao()).build();
	}

}
