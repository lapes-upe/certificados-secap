package br.upe.lapes.certificados.certificados.negocio.arquivo.vo;

import org.apache.commons.lang.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class OuvinteVO {

	private String nome;
	@SuppressWarnings("unused")
	private String nomeTratado;
	private String doitId;
	private String email;
	private String atividade;
	private Double cargaHoraria;
	private Long arquivo;

	public String getNomeTratado() {
		return StringUtils.replace(nome.toUpperCase(), " ", "_");
	}
}
