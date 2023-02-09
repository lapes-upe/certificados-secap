package br.upe.secap.certificados.arquivo;

import java.io.Serializable;

import org.joda.time.LocalDateTime;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class MonitorEnvelope implements Serializable {

	private static final long serialVersionUID = 8550809576403182019L;

	private Long id;

	private String nome;
	private String email;
	private Integer cargaHoraria;
	private String comissao;

	private LocalDateTime dataInclusao;
	private LocalDateTime dataUltimaAlteracao;

	private Boolean certificadoEnviado;
	private Boolean certificadoExportado;

	private Long arquivo;

	public static MonitorEnvelope get(Monitor monitor) {
		return MonitorEnvelope.builder().id(monitor.getId()).nome(monitor.getNome()).email(monitor.getEmail())
				.cargaHoraria(monitor.getCargaHoraria()).comissao(monitor.getComissao())
				.certificadoEnviado(monitor.getCertificadoEnviado()).arquivo(monitor.getArquivo().getId())
				.dataInclusao(monitor.getDataInclusao())
				.dataUltimaAlteracao(monitor.getDataUltimaAlteracao()).build();
	}

}
