package br.upe.lapes.certificados.certificados.programacao;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, of = {"id"})
public class EventoBO implements Comparable<EventoBO>{

	private Integer id;
	private String titulo;
	private TipoEventoEnum tipo;
	private List<AreaEnum> areas;
	private ModalidadeEnum modalidade;
	private LocalDate data;
	private List<Integer> horarios;

	public static EventoBO getBO(String dados) {
		EventoBO bo = null;
		
		try {
			final String[] dadosSeparados = StringUtils.splitPreserveAllTokens(dados, ';');
			
			final List<String> areas = Arrays.asList(StringUtils.splitPreserveAllTokens(dadosSeparados[3], '*'));
			final List<AreaEnum> areasEnum = areas.stream().map(AreaEnum::valueOf).collect(Collectors.toList());
			
			final List<String> horariosString = Arrays.asList(StringUtils.splitPreserveAllTokens(dadosSeparados[6], '*'));
			final List<Integer> horarios = horariosString.stream().map(Integer::valueOf).collect(Collectors.toList());

			bo = EventoBO.builder().id(Integer.valueOf(dadosSeparados[0])).titulo(dadosSeparados[1])
					.tipo(TipoEventoEnum.valueOf(dadosSeparados[2])).areas(areasEnum)
					.modalidade(ModalidadeEnum.valueOf(dadosSeparados[4]))
					.data(LocalDate.parse(dadosSeparados[5], DateTimeFormat.forPattern("dd/MM/yyyy"))).horarios(horarios)
					.build();
		} catch (Exception e) {
			System.out.println("Ocorreu um erro ao converter os dados da linha: " + dados);
		}

		return bo;
	}
	
	public int compareTo(EventoBO o) {
		int comparacao = 0;
		
		comparacao = this.data.compareTo(o.data);
		
		if (comparacao == 0) {
			comparacao = this.horarios.get(0).compareTo(o.getHorarios().get(0));
			
			if (comparacao == 0) {
				boolean todasThis = this.areas.get(0).equals(AreaEnum.TODAS);
				boolean todasObj = o.areas.get(0).equals(AreaEnum.TODAS);
				
				if(todasThis && !todasObj) {
					comparacao = 1;
				} else if(todasObj && !todasThis){
					comparacao = -1;
				} else {
					comparacao = -0;
				}
				
				if (comparacao == 0) {
					comparacao = this.titulo.compareTo(o.titulo);
				}
			}
		}
		
		return comparacao;
	}
	
	public String toString() {
		return id + ";" + titulo + ";" + tipo + ";" + areas + ";" + modalidade + ";" + data + ";" + horarios;
	}
}
