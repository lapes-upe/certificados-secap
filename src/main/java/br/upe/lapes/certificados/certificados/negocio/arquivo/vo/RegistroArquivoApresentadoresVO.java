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
public class RegistroArquivoApresentadoresVO {

	@CsvBindByPosition(position = 0)
	private String doitId;

	@CsvBindByPosition(position = 1)
	private String trabalho;

	@Getter(AccessLevel.NONE)
	@CsvBindByPosition(position = 2)
	private String nome;

	@CsvBindByPosition(position = 3)
	private String coautores;

	@CsvBindByPosition(position = 4)
	private String email;

	public PessoaCertificada getParticipante(Long idArquivo) {
		return PessoaCertificada.builder().doitId(doitId).arquivo(Arquivo.builder().id(idArquivo).build())
				.dataInclusao(LocalDateTime.now()).atividades(trabalho).nome(StringUtils.upperCase(nome))
				.coautores(StringUtils.upperCase(coautores)).email(StringUtils.trim(email))
				.tipo(TipoCertificadoEnum.APRESENTADOR).exportado(false).certificadoEnviado(false).build();
	}

	public String getEmail() {
		return this.email == null ? "" : trimWhitespace(this.email.toLowerCase());
	}

	public String getNome() {
		return trimWhitespace(this.nome.toUpperCase());
	}

	public String getDoitId() {
		return trimWhitespace(this.doitId);
	}

	public String getAtividade() {
		return trimWhitespace(this.trabalho.toUpperCase());
	}

}
