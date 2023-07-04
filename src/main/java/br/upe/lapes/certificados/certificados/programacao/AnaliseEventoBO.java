package br.upe.lapes.certificados.certificados.programacao;

import java.util.HashSet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, of = { "evento" })
@ToString
public class AnaliseEventoBO implements Comparable<AnaliseEventoBO> {

	private EventoBO evento;
	private HashSet<EventoBO> conflitos;

	public int compareTo(AnaliseEventoBO o) {
		return evento.compareTo(o.evento);
	}
}
