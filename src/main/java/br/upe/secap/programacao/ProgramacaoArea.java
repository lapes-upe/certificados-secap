package br.upe.secap.programacao;

import java.util.List;

import org.joda.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class ProgramacaoArea {
	private AreaEnum area;
	private LocalDate data;
	private List<AnaliseEventoBO> eventos;

	public boolean equals(Object obj) {
		boolean equals = false;

		if (this == obj) {
			equals = true;
		} else {

			if (obj instanceof ProgramacaoArea) {
				ProgramacaoArea outro = (ProgramacaoArea) obj;
				equals = this.getArea().equals(outro.getArea()) && this.getData().equals(outro.getData());
			}
		}

		return equals;
	}

}
