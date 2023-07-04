package br.upe.lapes.certificados.certificados.base;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.joda.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Data
@Getter(value = AccessLevel.PUBLIC)
@Setter(value = AccessLevel.PUBLIC)
@NoArgsConstructor
@SuperBuilder
@MappedSuperclass
public class Entidade implements Serializable{

	private static final long serialVersionUID = 1659627522225539519L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "data_inclusao", columnDefinition = "timestamp")
	private LocalDateTime dataInclusao;

	@Column(name = "data_ultima_alteracao", columnDefinition = "timestamp")
	private LocalDateTime dataUltimaAlteracao;
}
