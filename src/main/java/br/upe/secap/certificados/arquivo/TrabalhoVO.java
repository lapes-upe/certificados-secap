package br.upe.secap.certificados.arquivo;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import com.opencsv.bean.CsvBindByPosition;

import lombok.Data;

@Data
public class TrabalhoVO {

	@CsvBindByPosition(position = 0)
	private Long identificador;

	@CsvBindByPosition(position = 1)
	private String titulo;

	@CsvBindByPosition(position = 2)
	private String apresentador;

	@CsvBindByPosition(position = 3)
	private String coautores;

	@CsvBindByPosition(position = 4)
	private String email;

	public Trabalho getTrabalho(Long idArquivo) {
		return Trabalho.builder().titulo(StringUtils.upperCase(titulo)).arquivo(Arquivo.builder().id(idArquivo).build())
				.dataInclusao(LocalDateTime.now()).identificador(identificador).titulo(StringUtils.upperCase(titulo))
				.apresentador(StringUtils.upperCase(apresentador)).coautores(StringUtils.upperCase(coautores))
				.email(email).exportado(false).build();
	}
}
