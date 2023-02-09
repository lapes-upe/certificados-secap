package br.upe.secap.certificados.arquivo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

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
public class Ouvinte extends Entidade {

	private static final long serialVersionUID = 8550809576403182019L;

	private String nome;
	private String email;
	private Integer cargaHoraria;
	private Integer qtdComentarios;
	private LocalTime permanencia;
	private Boolean credenciado;
	private LocalDateTime primeiroAcesso;
	private LocalDateTime primeiroAcessoTransmissao;
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
		return StringUtils.replace(nome.toLowerCase(), " ", "_");
	}
}
