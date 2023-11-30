package br.upe.lapes.certificados.certificados.negocio.arquivo.vo;

import static org.springframework.util.StringUtils.trimAllWhitespace;
import static org.springframework.util.StringUtils.trimWhitespace;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import com.opencsv.bean.CsvBindByPosition;

import br.upe.lapes.certificados.certificados.negocio.arquivo.Arquivo;
import br.upe.lapes.certificados.certificados.negocio.atividade.PessoaCertificada;
import br.upe.lapes.certificados.certificados.negocio.atividade.TipoAtividadeEnum;
import br.upe.lapes.certificados.certificados.negocio.atividade.TipoParticipacaoAtividadeEnum;
import br.upe.lapes.certificados.certificados.negocio.certificado.TipoCertificadoEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

/**
 * Representa a estrutura de dados dos registros que são enviados no arquivo que
 * registra a participaćão dos ouvintes nas atividades com as respectivas cargas
 * horárias
 * 
 * Ex:[COLUNAS]: ch; titulo; participante; email; tipo_participacao; 3; A
 * PROBLEMÁTICA SOCIOAMBIENTAL URBANA; ALINA; ALINA@UPE.BR; PALESTRANTE 5; A
 * PROBLEMÁTICA SOCIOAMBIENTAL URBANA; FELIPE; PELIPE@UPE.BR; MEDIADOR
 * 
 * @author helainelins
 */
@Data
public class RegistroArquivoMinistranteMediadorVO {

	@CsvBindByPosition(position = 0)
	private String tipoAtividade;

	@CsvBindByPosition(position = 1)
	private Double cargaHoraria;

	@CsvBindByPosition(position = 2)
	private String titulo;

	@CsvBindByPosition(position = 3)
	private String tituloTratado;

	@Getter(AccessLevel.NONE)
	@CsvBindByPosition(position = 4)
	private String nome;

	@CsvBindByPosition(position = 5)
	private String nomeTratado;

	@CsvBindByPosition(position = 6)
	private String email;

	@CsvBindByPosition(position = 7)
	private String tipoParticipacao;

	public PessoaCertificada getParticipante(Long idArquivo) {
		return PessoaCertificada.builder().arquivo(Arquivo.builder().id(idArquivo).build()).dataInclusao(LocalDateTime.now())
				.atividades(tituloTratado).nome(StringUtils.upperCase(nome))
				.cargaHoraria(Double.valueOf(cargaHoraria))
				.nomeTratado(StringUtils.upperCase(nomeTratado)).email(StringUtils.trim(email))
				.tipo(TipoCertificadoEnum.MINISTRANTE)
				.tipoAtividade(TipoAtividadeEnum.getEnum(StringUtils.upperCase(tipoAtividade)))
				.tipoParticipacao(TipoParticipacaoAtividadeEnum.getEnum(StringUtils.upperCase(tipoParticipacao)))
				.exportado(false).certificadoEnviado(false).build();
	}

	public String getEmail() {
		return this.email == null ? "" : trimWhitespace(this.email.toLowerCase());
	}

	public String getNome() {
		return trimWhitespace(this.nome.toUpperCase());
	}

	public String getNomeTratado() {
		return trimAllWhitespace(this.nomeTratado.toUpperCase());
	}

}
