package br.upe.lapes.certificados.certificados.negocio.atividade;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;
import br.upe.lapes.certificados.certificados.base.Entidade;
import br.upe.lapes.certificados.certificados.negocio.arquivo.Arquivo;
import br.upe.lapes.certificados.certificados.negocio.certificado.TipoCertificadoEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(of = { "id", "doitId", "nome", "email", "tipo" }, callSuper = true)
public class PessoaCertificada extends Entidade {

	private static final long serialVersionUID = 8550809576403182019L;

	private String nome;
	
	@SuppressWarnings("unused")
	private String nomeTratado;

	private String doitId;

	private String email;

	@Enumerated(EnumType.STRING)
	private TipoCertificadoEnum tipo;

	@Column(name = "tipo_atividade")
	@Enumerated(EnumType.STRING)
	private TipoAtividadeEnum tipoAtividade;

	@Column(name = "tipo_participacao")
	@Enumerated(EnumType.STRING)
	private TipoParticipacaoAtividadeEnum tipoParticipacao;

	@Column(columnDefinition = "TEXT")
	private String atividades;

	@Column(columnDefinition = "TEXT")
	private String coautores;

	private String comissao;

	private Double cargaHoraria;
	private Boolean exportado;

	@Lob
	@Type(type = "org.hibernate.type.BinaryType")
	private byte[] certificado;

	@Column(name = "certificado_enviado")
	private Boolean certificadoEnviado;

	@ManyToOne
	@JoinColumn(name = "id_arquivo")
	private Arquivo arquivo;

	public String getNomeTratado() {
		return StringUtils.replace(nome.toUpperCase(), " ", "_");
	}

}
