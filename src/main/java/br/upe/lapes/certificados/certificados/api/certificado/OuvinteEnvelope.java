package br.upe.lapes.certificados.certificados.api.certificado;

import java.io.Serializable;
import org.joda.time.LocalDateTime;
import br.upe.lapes.certificados.certificados.negocio.atividade.PessoaCertificada;
import br.upe.lapes.certificados.certificados.negocio.certificado.TipoCertificadoEnum;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class OuvinteEnvelope implements Serializable {

	private static final long serialVersionUID = 8550809576403182019L;

	private Long id;

	private String nome;
	private String nomeTratado;
	private String doitId;
	private String email;
	private String atividades;
	private TipoCertificadoEnum tipo;
	private TipoCertificadoEnum tipoAtividade;
	private Double cargaHoraria;
	private String coautores;
	private String comissao;

	private Boolean exportado;

	private LocalDateTime dataInclusao;
	private LocalDateTime dataUltimaAlteracao;

	private Boolean certificadoEnviado;

	private Long arquivo;

	public static OuvinteEnvelope get(PessoaCertificada ouvinte) {
		return OuvinteEnvelope.builder().id(ouvinte.getId()).nome(ouvinte.getNome()).email(ouvinte.getEmail())
				.atividades(ouvinte.getAtividades()).cargaHoraria(ouvinte.getCargaHoraria())
				.coautores(ouvinte.getCoautores()).comissao(ouvinte.getComissao()).exportado(ouvinte.getExportado())
				.doitId(ouvinte.getDoitId()).certificadoEnviado(ouvinte.getCertificadoEnviado())
				.arquivo(ouvinte.getArquivo().getId()).dataInclusao(ouvinte.getDataInclusao())
				.dataUltimaAlteracao(ouvinte.getDataUltimaAlteracao()).build();
	}

}
