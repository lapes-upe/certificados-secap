package br.upe.lapes.certificados.certificados.negocio.certificado.modelo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ParametrizacaoCertificadoVO {

	private String textoCorpo;
	private String template;
}
