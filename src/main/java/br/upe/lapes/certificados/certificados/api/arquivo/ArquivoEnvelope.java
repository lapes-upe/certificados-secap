package br.upe.lapes.certificados.certificados.api.arquivo;

import java.io.Serializable;
import org.joda.time.LocalDateTime;
import br.upe.lapes.certificados.certificados.negocio.arquivo.Arquivo;
import br.upe.lapes.certificados.certificados.negocio.certificado.TipoCertificadoEnum;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Encapsula todos os parâmetros de uma requisicão a um endpoint de tratamento de arquivos.
 * 
 * @author helainelins
 */
@SuperBuilder
@Data
public class ArquivoEnvelope implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String nome;
	private Long tamanho;
	private TipoCertificadoEnum tipo;
	private LocalDateTime dataInclusao;
	private LocalDateTime dataUltimaAlteracao;
	
	/**
	 * Transforma uma {@link Arquivo} em um {@link ArquivoEnvelope}.
	 * 
	 * @param arquivo a instância com os dados preenchidos.
	 * @return o {@link ArquivoEnvelope} contendo todos os dados preenchidos.
	 */
	public static ArquivoEnvelope get(Arquivo arquivo) {
		return ArquivoEnvelope.builder().id(arquivo.getId()).nome(arquivo.getNome())
		.tamanho(arquivo.getTamanho()).tipo(arquivo.getTipo()).dataInclusao(arquivo.getDataInclusao())
		.dataUltimaAlteracao(arquivo.getDataUltimaAlteracao()).build();
	}
}
