package br.upe.secap.certificados.arquivo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import com.opencsv.bean.CsvBindByPosition;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class AtividadeVO {

	@CsvBindByPosition(position = 0)
	private String atividade;

	@CsvBindByPosition(position = 1)
	private String titulo;

	@CsvBindByPosition(position = 2)
	private String moderador;

	@CsvBindByPosition(position = 3)
	private String emailModerador;

	@CsvBindByPosition(position = 4)
	private String mediadores;

	@CsvBindByPosition(position = 5)
	private String emailMediadores;

	@CsvBindByPosition(position = 6)
	private String palestrantes;

	@CsvBindByPosition(position = 7)
	private String emailPalestrantes;

	@CsvBindByPosition(position = 8)
	private String cargaHoraria;

	public List<Atividade> getRegistroAtividades(Long idArquivo) {
		List<Atividade> registroAtividade = new ArrayList<Atividade>();

		if (StringUtils.isNotEmpty(this.moderador)) {
			registroAtividade.addAll(
					this.getBase(idArquivo, this.moderador, emailModerador, TipoParticipacaoAtividadeEnum.MODERADOR));
		}

		if (StringUtils.isNotEmpty(this.mediadores)) {
			registroAtividade.addAll(this.getBase(idArquivo, this.mediadores, this.emailMediadores,
					TipoParticipacaoAtividadeEnum.MEDIADOR));
		}

		if (StringUtils.isNotEmpty(this.palestrantes)) {
			registroAtividade.addAll(this.getBase(idArquivo, this.palestrantes, this.emailPalestrantes,
					TipoParticipacaoAtividadeEnum.PALESTRANTE));
		}

		log.info("extraídos " + registroAtividade.size() + " registros");
		
		return registroAtividade;
	}

	private List<Atividade> getBase(Long idArquivo, String listaNomes, String listaEmails,
			TipoParticipacaoAtividadeEnum tipoParticipacao) {
		List<Atividade> atividades = new ArrayList<Atividade>();

		if (StringUtils.isNotEmpty(listaNomes) && StringUtils.isNotEmpty(listaEmails)) {

			String[] nomes = StringUtils.split(listaNomes, ",");
			String[] emails = StringUtils.split(listaEmails, ",");

			Atividade atividade = null;
			String nome = null;
			String email = null;
			for (int i = 0; i < nomes.length; i++) {
				nome = nomes[i];
				email = emails[i];

				atividade = Atividade.builder().titulo(StringUtils.upperCase(titulo))
						.cargaHoraria(Double.valueOf(cargaHoraria)).arquivo(Arquivo.builder().id(idArquivo).build())
						.tipoAtividade(TipoAtividadeEnum.getEnum(StringUtils.upperCase(this.atividade)))
						.dataInclusao(LocalDateTime.now()).tipoParticipacao(tipoParticipacao).exportado(false)
						.nome(StringUtils.upperCase(nome)).email(email).build();

				atividades.add(atividade);
			}
		}
		
		return atividades;
	}
}
