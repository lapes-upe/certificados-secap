package br.upe.lapes.certificados.certificados.negocio.arquivo.vo;

import org.joda.time.LocalDateTime;

import com.opencsv.bean.CsvBindByPosition;

import br.upe.lapes.certificados.certificados.negocio.arquivo.Arquivo;
import br.upe.lapes.certificados.certificados.negocio.atividade.PessoaCertificada;
import br.upe.lapes.certificados.certificados.negocio.certificado.TipoCertificadoEnum;
import lombok.Data;

/**
 * Representa a estrutura de dados dos registros que são enviados no arquivo que
 * registra a participaćão dos monitores com as respectivas cargas horárias
 * 
 * Ex:[COLUNAS]: nome; nome_tratado; email; ch; comissao Alisson Vieira de
 * Azevedo ALISSON VIEIRA DE AZEVEDO alisson.vieira@upe.br 40 OPERACIONAL
 * 
 * @author helainelins
 */
@Data
public class RegistroArquivoMonitoresVO {

	@CsvBindByPosition(position = 0)
	private String nome;

	@CsvBindByPosition(position = 1)
	private String nomeTratado;

	@CsvBindByPosition(position = 2)
	private String email;

	@CsvBindByPosition(position = 3)
	private Double cargaHoraria;

	@CsvBindByPosition(position = 4)
	private String comissao;

	public PessoaCertificada getParticipante(Long idArquivo) {
		return PessoaCertificada.builder().comissao(comissao.toUpperCase()).nome(nome.toUpperCase()).nomeTratado(nomeTratado)
				.email(email).cargaHoraria(cargaHoraria).tipo(TipoCertificadoEnum.MONITOR).certificadoEnviado(false).exportado(false)
				.arquivo(Arquivo.builder().id(idArquivo).build()).dataInclusao(LocalDateTime.now()).build();
	}
}
