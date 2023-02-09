package br.upe.secap.certificados.arquivo;

public enum TipoParticipacaoAtividadeEnum {
	MEDIADOR,MODERADOR, PALESTRANTE; 
	
	public String paraImprimir() {
		return PALESTRANTE.equals(this) ? this.toString()  : this.toString() + "(A)";
	}
}
