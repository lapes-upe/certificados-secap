package br.upe.secap.certificados.arquivo;

import javax.persistence.Column;
import javax.persistence.Entity;
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
public class Trabalho extends Entidade {

	private static final long serialVersionUID = 5584839606573329207L;

	@Column(length = 400)
	private String titulo;
	private String apresentador;
	@Column(length = 400)
	private String coautores;
	private String email;
	private Long identificador;
	private Boolean exportado;
	
	@Lob
	@Type(type = "org.hibernate.type.BinaryType")
	private byte[] certificado;

	@Column(name = "certificado_enviado")
	private Boolean certificadoEnviado;

	@ManyToOne
	@JoinColumn(name = "id_arquivo")
	private Arquivo arquivo;
	
	public String getNomeApresentadorTratado() {
		return StringUtils.replace(apresentador.toLowerCase(), " ", "_");
	}
}
