package br.upe.secap.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class DetalheErro {

	private String object;
	private String field;
	private Object rejectedValue;
	private String message;

	public DetalheErro(String object, String message) {
		this.object = object;
		this.message = message;
	}
}
