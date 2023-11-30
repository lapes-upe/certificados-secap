package br.upe.lapes.certificados.certificados.negocio.arquivo.vo;

import static org.springframework.util.StringUtils.trimAllWhitespace;
import static org.springframework.util.StringUtils.trimWhitespace;

import com.opencsv.bean.CsvBindByPosition;

import br.upe.lapes.certificados.certificados.negocio.atividade.PessoaCertificada;
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
public class RegistroArquivoOuvintesVO {

	@Getter(AccessLevel.NONE)
	@CsvBindByPosition(position = 0)
	private String nome;

	@CsvBindByPosition(position = 1)
	private String nomeTratado;

	@CsvBindByPosition(position = 2)
	private String doitId;

	@CsvBindByPosition(position = 3)
	private String email;

	@CsvBindByPosition(position = 4)
	private String atividade;

	@CsvBindByPosition(position = 5)
	private String cargaHoraria;

	public OuvinteVO getOuvinteBO(Long idArquivo) {
		return OuvinteVO.builder().nomeTratado(getNomeTratado()).nome(getNome()).doitId(getDoitId())
				.email(getEmail()).cargaHoraria(Double.valueOf(getCargaHoraria()))
				.atividade(trimWhitespace(getAtividade())).build();
	}

	public PessoaCertificada getOuvinte(Long idArquivo) {
		return PessoaCertificada.builder().nomeTratado(getNomeTratado())
				.nome(getNome()).doitId(trimWhitespace(getDoitId())).email(getEmail())
				.cargaHoraria(Double.valueOf(getCargaHoraria())).build();
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
		return trimWhitespace(this.atividade.toUpperCase());
	}

	public String getNomeTratado() {
		return trimAllWhitespace(this.nomeTratado.toUpperCase());
	}

	public String getCargaHoraria() {
		return this.cargaHoraria == null ? "" : this.cargaHoraria.toLowerCase().replace("h", "");
	}
}
