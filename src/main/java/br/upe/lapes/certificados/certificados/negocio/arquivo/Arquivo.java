package br.upe.lapes.certificados.certificados.negocio.arquivo;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import org.hibernate.annotations.Type;
import br.upe.lapes.certificados.certificados.base.Entidade;
import br.upe.lapes.certificados.certificados.negocio.atividade.PessoaCertificada;
import br.upe.lapes.certificados.certificados.negocio.certificado.TipoCertificadoEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Arquivo extends Entidade {

	private static final long serialVersionUID = 5331327329006569992L;

	@Lob
	@Type(type = "org.hibernate.type.BinaryType")
	private byte[] arquivo;

	private String nome;
	private Long tamanho;

	@Enumerated(EnumType.STRING)
	private TipoCertificadoEnum tipo;

	@OneToMany(mappedBy = "arquivo")
	List<PessoaCertificada> participantes;

}
