package br.upe.secap.certificados.arquivo;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.bean.CsvToBeanBuilder;

import br.upe.secap.certificados.certificado.TipoCertificadoEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ArquivoService {

	@Autowired
	private IArquivoDAO dao;

	@Autowired
	private IOuvinteDAO participanteDao;

	@Autowired
	private IAtividadeDAO atividadeDao;

	@Autowired
	private ITrabalhoDAO trabalhoDao;

	@Autowired
	private IMonitorDAO monitorDao;

	@Transactional(readOnly = true)
	public List<Arquivo> listar() {
		return this.dao.findAllByOrderByNome();
	}

	@Transactional
	public void apagar(Long idArquivo) {
		if (idArquivo == null) {
			throw new RuntimeException("O identificador do arquivo não pode ser nulo");
		}

		Optional<Arquivo> arquivo = this.dao.findById(idArquivo);
		
		if (!arquivo.isPresent()) {
			throw new RuntimeException("O arquivo de id:" + idArquivo + " não foi encontrado.");
		}

		switch (arquivo.get().getTipo()) {
		case OUVINTE:
			if (this.participanteDao.countByArquivoId(idArquivo) > 0) {
				throw new RuntimeException("O arquivo não pode ser excluído pois possui ouvintes associados");
			}
			break;
		case ATIVIDADE:
			if (this.atividadeDao.countByArquivoId(idArquivo) > 0) {
				throw new RuntimeException("O arquivo não pode ser excluído pois possui atividades associadas");
			}
			break;
		case APRESENTADOR:
			if (this.trabalhoDao.countByArquivoId(idArquivo) > 0) {
				throw new RuntimeException(
						"O arquivo não pode ser excluído pois possui apresentadores de trabalho associados");
			}
			break;
		default:
			if (this.monitorDao.countByArquivoId(idArquivo) > 0) {
				throw new RuntimeException(
						"O arquivo não pode ser excluído pois possui monitores associados");
			}
			break;
		}

		this.dao.deleteById(idArquivo);
	}

	@Transactional
	public Arquivo salvar(MultipartFile arquivo, TipoCertificadoEnum tipo) {
		//fixme:associar o tipocertificadoenum ao arquivo
		Arquivo registro = null;

		if (arquivo == null) {
			throw new RuntimeException("O arquivo não pode ser nulo");
		}

		if (!"text/csv".equals(arquivo.getContentType())) {
			throw new RuntimeException("O arquivo deve ser no formato csv");
		}

		if (tipo == null) {
			throw new RuntimeException("O tipo do arquivo não pode ser nulo");
		}
		
		try {
			registro = Arquivo.builder().arquivo(arquivo.getBytes()).nome(arquivo.getOriginalFilename())
					.tamanho(arquivo.getSize()).tipo(tipo).dataInclusao(LocalDateTime.now()).build();

			this.dao.save(registro);

			log.info("Arquivo" + arquivo.getName() + " salvo com sucesso!");

		} catch (Exception e) {
			log.error("Ocorreu um erro ao salvar arquivo:" + arquivo.getName(), e);
			throw new RuntimeException("Ocorreu um erro ao salvar arquivo:" + arquivo.getName(), e);
		}

		return registro;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public int extrair(Long idArquivo, TipoCertificadoEnum tipo) {
		int qtd = 0;

		Optional<Arquivo> arquivo = this.dao.findById(idArquivo);

		if (!arquivo.isPresent()) {
			throw new RuntimeException("Não foi possível localizar o arquivo de registro:" + idArquivo);
		}

		List<?> registros = null;
		List<?> entidades = null;
		switch (tipo) {
		case ATIVIDADE:
			registros = this.lerArquivo(arquivo.get(), AtividadeVO.class);

			entidades = ((List<AtividadeVO>) registros).stream()
					.flatMap(vo -> vo.getRegistroAtividades(idArquivo).stream()).collect(Collectors.toList());

			qtd = ((Collection<?>) this.atividadeDao.saveAll((List<Atividade>) entidades)).size();
			break;
		case APRESENTADOR:
			registros = this.lerArquivo(arquivo.get(), TrabalhoVO.class);

			entidades = ((List<TrabalhoVO>) registros).stream().map(vo -> vo.getTrabalho(idArquivo))
					.collect(Collectors.toList());

			qtd = ((Collection<?>) this.trabalhoDao.saveAll((List<Trabalho>) entidades)).size();
			break;
		case MONITOR:
			registros = this.lerArquivo(arquivo.get(), MonitorVO.class);

			entidades = ((List<MonitorVO>) registros).stream()
					.map(vo -> vo.getMonitor(idArquivo)).collect(Collectors.toList());

			qtd = ((Collection<?>) this.monitorDao.saveAll((List<Monitor>) entidades)).size();
			break;
		case OUVINTE:
			registros = this.lerArquivo(arquivo.get(), OuvinteVO.class);

			entidades = ((List<OuvinteVO>) registros).stream()
					.map(vo -> vo.getParticipante(idArquivo)).collect(Collectors.toList());

			qtd = ((Collection<?>) this.participanteDao.saveAll((List<Ouvinte>) entidades)).size();
			break;
		default:
			throw new RuntimeException("Informe o tipo de extração do arquivo");
		}

		return qtd;
	}

	private <Objeto> List<Objeto> lerArquivo(Arquivo arquivo, Class<Objeto> objeto) {
		List<Objeto> planilha = new ArrayList<Objeto>();

		try (Reader reader = new InputStreamReader(new ByteArrayInputStream(arquivo.getArquivo()))) {

			planilha = new CsvToBeanBuilder<Objeto>(reader).withType(objeto).withSkipLines(1).withSeparator(';').build()
					.parse();

		} catch (Exception e) {
			log.error("Ocorreu um erro ao realizar leitura de arquivos", e);
			throw new RuntimeException(
					"Ocorreu um erro ao extrair os dados do arquivo[" + arquivo.getId() + "]:" + arquivo.getNome(), e);
		}

		return planilha;
	}
}
