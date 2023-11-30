package br.upe.lapes.certificados.certificados.negocio.atividade;

public enum TipoAtividadeEnum {
	MESA_REDONDA, CURSO_MINICURSO, OFICINA, PALESTRA;

	public static TipoAtividadeEnum getEnum(String tipo) {
		TipoAtividadeEnum enumeracao = null;

		switch (tipo) {
		case "MESA-REDONDA":
			enumeracao = MESA_REDONDA;
			break;
		case "MINICURSO":
			enumeracao = CURSO_MINICURSO;
			break;
		case "OFICINA":
			enumeracao = OFICINA;
			break;
		default:
			enumeracao = PALESTRA;
			break;
		}

		return enumeracao;
	}

	@Override
	public String toString() {
		String descricao = null;

		switch (this) {
		case MESA_REDONDA:
			descricao = "DA MESA-REDONDA";
			break;
		case CURSO_MINICURSO:
			descricao = "DO MINICURSO";
			break;
		case OFICINA:
			descricao = "DA OFICINA";
			break;
		default:
			descricao = "DA PALESTRA";
			break;
		}

		return descricao;
	}
}
