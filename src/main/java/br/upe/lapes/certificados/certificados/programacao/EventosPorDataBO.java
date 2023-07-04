package br.upe.lapes.certificados.certificados.programacao;

import java.util.List;

import org.joda.time.LocalDate;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EventosPorDataBO {
	private LocalDate data;
	private List<AnaliseEventoBO> eventos;
	// private HashMap<Integer, List<EventoBO>> programacao;
}
