package br.upe.secap.certificados.arquivo;

import java.text.Normalizer;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import com.opencsv.bean.CsvBindByPosition;

import lombok.Data;

@Data
public class OuvinteVO {

	@CsvBindByPosition(position = 0)
	private String nome;

	@CsvBindByPosition(position = 1)
	private String cargaHoraria;

	@CsvBindByPosition(position = 2)
	private String email;

	@CsvBindByPosition(position = 3)
	private Integer qtdComentarios;

	@CsvBindByPosition(position = 4)
	private String permanencia;

	@CsvBindByPosition(position = 5)
	private String credenciado;

	@CsvBindByPosition(position = 6)
	private String primeiroAcesso;

	@CsvBindByPosition(position = 7)
	private String primeiroAcessoTransmissao;

	public Ouvinte getParticipante(Long idArquivo) {

		String credenciado = Normalizer.normalize(this.credenciado, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]",
				"");

		return Ouvinte.builder().nome(nome).email(email).qtdComentarios(qtdComentarios)
				.cargaHoraria(Integer.valueOf(cargaHoraria.replace("h", "")))
				.permanencia(LocalTime.parse(permanencia, DateTimeFormat.forPattern("mm:ss:SS")))
				.credenciado("SIM".equalsIgnoreCase(credenciado) ? Boolean.TRUE : Boolean.FALSE)
				.primeiroAcesso("Não registrado".equalsIgnoreCase(primeiroAcesso) ? null
						: LocalDateTime.parse(primeiroAcesso, DateTimeFormat.forPattern("dd/MM/yyyy - HH:mm")))
				.primeiroAcessoTransmissao("Não registrado".equalsIgnoreCase(primeiroAcessoTransmissao) ? null
						: LocalDateTime.parse(primeiroAcessoTransmissao,
								DateTimeFormat.forPattern("dd/MM/yyyy - HH:mm")))
				.exportado(false).arquivo(Arquivo.builder().id(idArquivo).build()).build();
	}
}
