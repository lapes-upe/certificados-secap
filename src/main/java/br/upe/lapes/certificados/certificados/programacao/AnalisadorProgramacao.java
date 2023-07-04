package br.upe.lapes.certificados.certificados.programacao;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jfree.util.Log;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class AnalisadorProgramacao {

	public static void main(String[] args) {
		HashMap<LocalDate, List<AnaliseEventoBO>> analiseConflitos = montarAnaliseEventoPorData();

		imprimirEventosPorData(analiseConflitos);
		exportarAnalisesEventosPorData(analiseConflitos);
		exportarAnalisesEventosPorArea(analiseConflitos);
	}

	private static List<EventoBO> lerArquivoProgramacao(String nomeArquivo) {
		List<EventoBO> lista = new ArrayList<>();

		try {
			File arquivoRetorno = new File("programacao/" + nomeArquivo);
			final List<String> registros = FileUtils.readLines(arquivoRetorno, Charset.defaultCharset());

			registros.remove(0);

			for (String registro : registros) {
				if (StringUtils.isNotBlank(registro)) {
					lista.add(EventoBO.getBO(registro));
				}
			}

		} catch (Exception e) {
			Log.error("Ocorreu um erro ao ler o arquivo da programacao", e);
			throw new RuntimeException("Erro ao ler o arquivo de isenção", e);
		}

		return lista;
	}

	private static HashMap<LocalDate, EventosPorDataBO> estruturarEventosPorData() {
		HashMap<LocalDate, EventosPorDataBO> dadosParaAnalise = new HashMap<>();

		List<EventoBO> programacao = lerArquivoProgramacao("secap_03.csv");

		EventosPorDataBO eventosData;

		for (EventoBO evento : programacao) {
			eventosData = dadosParaAnalise.get(evento.getData());

			if (eventosData == null) {
				eventosData = EventosPorDataBO.builder().data(evento.getData()).eventos(new ArrayList<>()).build();
				dadosParaAnalise.put(evento.getData(), eventosData);
			}

			eventosData.getEventos().add(AnaliseEventoBO.builder().evento(evento).conflitos(new HashSet<>()).build());
		}

		return dadosParaAnalise;
	}

	private static void exportarAnalisesEventosPorData(HashMap<LocalDate, List<AnaliseEventoBO>> dados) {
		List<LocalDate> datas = new ArrayList<>(dados.keySet());
		Collections.sort(datas);

		List<AnaliseEventoBO> dadosDia = null;
		for (LocalDate data : datas) {
			dadosDia = dados.get(data);

			Collections.sort(dadosDia);

			try (BufferedWriter writer = new BufferedWriter(
					new FileWriter("programacao/resultado_" + LocalDate.now().toString("dd-MM-yyyy") + ".csv", true))) {

				writer.append("ID;TITULO;TIPO;AREAS;MODALIDADE;DATA;HORARIOS;CONFLITOS;IDS_CONFLITOS");
				writer.append("\n");

				for (AnaliseEventoBO analise : dadosDia) {
					writer.append(analise.getEvento() + ";" + analise.getConflitos().size() + ";"
							+ analise.getConflitos().stream().map(EventoBO::getId).collect(Collectors.toList()));
					writer.append("\n");
				}

			} catch (Exception e) {
				System.out.println("Ocorreu um erro ao exportar os resultados \n" + e);
			}
		}
	}

	private static void exportarAnalisesEventosPorArea(HashMap<LocalDate, List<AnaliseEventoBO>> dados) {
		//HashMap<ProgramacaoArea, List<ProgramacaoArea>> eventosPorData = new HashMap<>();
		
		List<LocalDate> datas = new ArrayList<>(dados.keySet());
		Collections.sort(datas);

		List<AnaliseEventoBO> dadosDia = null;
		for (LocalDate data : datas) {
			dadosDia = dados.get(data);

			Collections.sort(dadosDia);

			for (AreaEnum area : AreaEnum.values()) {
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(
						"programacao/resultado_" + area.name() + "_" + data.toString("dd-MM-yyyy") + ".csv", true))) {

					writer.append("ID;TITULO;TIPO;AREAS;MODALIDADE;DATA;HORARIOS;CONFLITOS;IDS_CONFLITOS");
					writer.append("\n");

					for (AnaliseEventoBO analise : dadosDia) {
						writer.append(analise.getEvento() + ";" + analise.getConflitos().size() + ";"
								+ analise.getConflitos().stream().map(EventoBO::getId).collect(Collectors.toList()));
						writer.append("\n");
					}

				} catch (Exception e) {
					System.out.println("Ocorreu um erro ao exportar os resultados por área\n" + e);
				}
			}
		}
	}

	private static void imprimirEventosPorData(HashMap<LocalDate, List<AnaliseEventoBO>> dados) {
		System.out.println("---------------------------------------------------------------------------");
		System.out.println("ANALISE DE EVENTOS SEM CONFLITOS");
		System.out.println("---------------------------------------------------------------------------");

		List<LocalDate> datas = new ArrayList<>(dados.keySet());
		Collections.sort(datas);

		// mesmo dia, horário e mesma modalidade

		List<AnaliseEventoBO> dadosDia = null;
		for (LocalDate data : datas) {
			dadosDia = dados.get(data);

			Collections.sort(dadosDia);

			System.out.println("---------------------------------------------------------------------------");
			System.out.println("DIA: " + data + " EVENTOS SEM CONFLITOS");
			System.out.println("---------------------------------------------------------------------------");

			for (AnaliseEventoBO analise : dadosDia) {
				if (analise.getConflitos().size() == 0) {
					System.out.println(analise.getEvento() + ";" + analise.getConflitos().size() + ";"
							+ analise.getConflitos().stream().map(EventoBO::getId).collect(Collectors.toList()));
				}
			}

			System.out.println("---------------------------------------------------------------------------");
			System.out.println("DIA: " + data + " EVENTOS COM CONFLITOS");
			System.out.println("---------------------------------------------------------------------------");

			for (AnaliseEventoBO analise : dadosDia) {
				if (analise.getConflitos().size() != 0) {
					System.out.println(analise.getEvento() + ";" + analise.getConflitos().size() + ";"
							+ analise.getConflitos().stream().map(EventoBO::getId).collect(Collectors.toList()));
				}
			}
		}
	}

	private static HashMap<LocalDate, List<AnaliseEventoBO>> montarAnaliseEventoPorData() {
		HashMap<LocalDate, List<AnaliseEventoBO>> resultado = new HashMap<>();

		HashMap<LocalDate, EventosPorDataBO> dados = estruturarEventosPorData();

		List<LocalDate> datas = new ArrayList<>(dados.keySet());
		Collections.sort(datas);

		List<AnaliseEventoBO> eventos = null;
		EventosPorDataBO dadosDia = null;

		for (LocalDate data : datas) {
			dadosDia = dados.get(data);

			Collections.sort(dadosDia.getEventos());
			
			eventos = new ArrayList<AnaliseEventoBO>();
			
			for (AnaliseEventoBO analise : dadosDia.getEventos()) {
				for (Integer horario : analise.getEvento().getHorarios()) {
					for (AnaliseEventoBO analiseComparacao : dadosDia.getEventos()) {
						for (Integer horarioComparacao : analiseComparacao.getEvento().getHorarios()) {
							if (horario.equals(horarioComparacao)
									&& !analiseComparacao.getEvento().equals(analise.getEvento())) {

								// analise.getConflitos().add(analiseComparacao.getEvento());

								// INCLUIR A COMPARACAO POR AREA
								if (analise.getEvento().getAreas().contains(AreaEnum.TODAS)
										|| analiseComparacao.getEvento().getAreas().contains(AreaEnum.TODAS)) {
									analise.getConflitos().add(analiseComparacao.getEvento());
								} else {
									for (AreaEnum areaEvento : analise.getEvento().getAreas()) {
										if (analiseComparacao.getEvento().getAreas().contains(areaEvento)) {
											analise.getConflitos().add(analiseComparacao.getEvento());
										}
									}
								}
							}
						}
					}
				}

				eventos.add(analise);
			}

			resultado.put(data, eventos);
		}

		return resultado;
	}

}
