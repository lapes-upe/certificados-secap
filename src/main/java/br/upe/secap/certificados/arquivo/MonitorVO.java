package br.upe.secap.certificados.arquivo;

import org.joda.time.LocalDateTime;

import com.opencsv.bean.CsvBindByPosition;

import lombok.Data;

@Data
public class MonitorVO {

	@CsvBindByPosition(position = 0)
	private String comissao;

	@CsvBindByPosition(position = 1)
	private String nome;

	@CsvBindByPosition(position = 2)
	private String email;

	@CsvBindByPosition(position = 3)
	private Integer cargaHoraria;

	public Monitor getMonitor(Long idArquivo) {
		return Monitor.builder().comissao(comissao.toUpperCase()).nome(nome.toUpperCase()).email(email).cargaHoraria(cargaHoraria)
				.certificadoEnviado(false).exportado(false).arquivo(Arquivo.builder().id(idArquivo).build())
				.dataInclusao(LocalDateTime.now()).build();
	}
}
