package br.upe.lapes.certificados.certificados.negocio.atividade;

public enum TipoParticipacaoAtividadeEnum {
	MEDIADOR,MODERADOR, PALESTRANTE; 
	
	public static TipoParticipacaoAtividadeEnum getEnum(String tipo) {
		TipoParticipacaoAtividadeEnum enumeracao = null;

		switch (tipo) {
		case "MEDIADOR":
			enumeracao = MEDIADOR;
			break;
		case "MODERADOR":
			enumeracao = MODERADOR;
			break;
		default:
			enumeracao = PALESTRANTE;
			break;
		}

		return enumeracao;
	}
	
	public String paraImprimir() {
		return PALESTRANTE.equals(this) ? this.toString()  : this.toString() + "(A)";
	}
}
