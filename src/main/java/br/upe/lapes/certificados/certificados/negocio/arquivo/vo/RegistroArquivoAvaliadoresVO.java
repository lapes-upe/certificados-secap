package br.upe.lapes.certificados.certificados.negocio.arquivo.vo;

import static org.springframework.util.StringUtils.trimWhitespace;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import com.opencsv.bean.CsvBindByPosition;

import br.upe.lapes.certificados.certificados.negocio.arquivo.Arquivo;
import br.upe.lapes.certificados.certificados.negocio.atividade.PessoaCertificada;
import br.upe.lapes.certificados.certificados.negocio.certificado.TipoCertificadoEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

/**
 * Representa a estrutura de dados dos registros que são enviados no arquivo que
 * registra a participaćão dos ouvintes nas atividades com as respectivas cargas
 * horárias
 * 
 * Ex:[COLUNAS]: discente; email; id; atividade; ch ADRIA ANGELO DA SILVA;
 * ADRIA@UPE.BR; A PROBLEMÁTICA SOCIOAMBIENTAL URBANA; 1.5
 * 
 * @author helainelins
 */
@Data
public class RegistroArquivoAvaliadoresVO {

	@Getter(AccessLevel.NONE)
	@CsvBindByPosition(position = 0)
	private String nome;

	@CsvBindByPosition(position = 1)
	private Double cargaHoraria;

	@CsvBindByPosition(position = 2)
	private String email;

	public PessoaCertificada getParticipante(Long idArquivo) {
		return PessoaCertificada.builder().arquivo(Arquivo.builder().id(idArquivo).build()).dataInclusao(LocalDateTime.now())
				.nome(StringUtils.upperCase(nome)).cargaHoraria(Double.valueOf(cargaHoraria))
				.email(StringUtils.trim(email)).tipo(TipoCertificadoEnum.AVALIADOR).exportado(false)
				.certificadoEnviado(false).build();
	}

	public String getEmail() {
		return this.email == null ? "" : trimWhitespace(this.email.toLowerCase());
	}

	public String getNome() {
		return trimWhitespace(this.nome.toUpperCase());
	}

}
