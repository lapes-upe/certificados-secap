package br.upe.secap.certificados.arquivo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;

import br.upe.secap.base.Entidade;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Atividade extends Entidade {

	private static final long serialVersionUID = 5584839606573329207L;

	private String titulo;
	private String nome;
	private String email;
	private Boolean exportado;
	
	
	@Column(name="tipo_atividade")
	@Enumerated(EnumType.STRING)
	private TipoAtividadeEnum tipoAtividade;

	@Column(name="tipo_participacao")
	@Enumerated(EnumType.STRING)
	private TipoParticipacaoAtividadeEnum  tipoParticipacao;
	
	@Column(name = "carga_horaria")
	private Double cargaHoraria;

	@Lob
	@Type(type = "org.hibernate.type.BinaryType")
	private byte[] certificado;

	@Column(name = "certificado_enviado")
	private Boolean certificadoEnviado;

	@ManyToOne
	@JoinColumn(name = "id_arquivo")
	private Arquivo arquivo;
	
	public String getNomeTratado() {
		return StringUtils.replace(nome.toLowerCase(), " ", "_");
	}
}
