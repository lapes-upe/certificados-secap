package br.upe.lapes.certificados.certificados.negocio.arquivo.vo;

import static org.springframework.util.StringUtils.trimAllWhitespace;
import static org.springframework.util.StringUtils.trimWhitespace;

import com.opencsv.bean.CsvBindByPosition;

import lombok.Data;

/**
 * Objeto que representa a estrutura de dados dos registros do arquivo exportado
 * contendo todos os usuários que participaram do evento.
 * 
 * @author helainelins
 */
@Data
public class RegistroArquivoDadosComplementaresUsuariosDoytVO {

	@CsvBindByPosition(position = 0)
	private String nome;

	@CsvBindByPosition(position = 1)
	private String nomeTratado;

	@CsvBindByPosition(position = 2)
	private String doitId;

	@CsvBindByPosition(position = 3)
	private String email;

	public OuvinteVO getOuvinteBO(Long idArquivo) {
		return OuvinteVO.builder().nomeTratado(trimAllWhitespace(this.nomeTratado.toUpperCase()))
				.nome(trimWhitespace(this.nome.toUpperCase())).doitId(trimWhitespace(this.doitId))
				.email(trimWhitespace(this.email.toLowerCase())).cargaHoraria(0d).build();
	}

}
