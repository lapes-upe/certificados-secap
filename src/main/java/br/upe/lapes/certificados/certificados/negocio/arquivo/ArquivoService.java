package br.upe.lapes.certificados.certificados.negocio.arquivo;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.springframework.util.StringUtils.hasLength;
import static org.springframework.util.StringUtils.trimAllWhitespace;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.jfree.util.Log;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.bean.CsvToBeanBuilder;

import br.upe.lapes.certificados.certificados.negocio.arquivo.vo.OuvinteVO;
import br.upe.lapes.certificados.certificados.negocio.arquivo.vo.RegistroArquivoApresentadoresVO;
import br.upe.lapes.certificados.certificados.negocio.arquivo.vo.RegistroArquivoAvaliadoresVO;
import br.upe.lapes.certificados.certificados.negocio.arquivo.vo.RegistroArquivoDadosComplementaresUsuariosDoytVO;
import br.upe.lapes.certificados.certificados.negocio.arquivo.vo.RegistroArquivoMinistranteMediadorVO;
import br.upe.lapes.certificados.certificados.negocio.arquivo.vo.RegistroArquivoMonitoresVO;
import br.upe.lapes.certificados.certificados.negocio.arquivo.vo.RegistroArquivoOuvintesVO;
import br.upe.lapes.certificados.certificados.negocio.atividade.IPessoaCertificadaDAO;
import br.upe.lapes.certificados.certificados.negocio.atividade.PessoaCertificada;
import br.upe.lapes.certificados.certificados.negocio.certificado.TipoCertificadoEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * Responsável pela realização dos casos de uso relacionados a importação e
 * extração de informações de arquivos que contém informações que irão gerar
 * certificados no sistema.
 * 
 * @author helainelins
 */
@Slf4j
@Service
public class ArquivoService {

	@Autowired
	private IArquivoDAO dao;

	@Autowired
	private IPessoaCertificadaDAO pessoaCertificadaDao;

	/**
	 * Lista os arquivos ordenados por nome.
	 * 
	 * @return um {@link List} com os arquivos ordenados ou vazia caso não existam
	 *         arquivos cadastradios.
	 */
	@Transactional(readOnly = true)
	public List<Arquivo> listar() {
		// FIXME: acrescentar o tratamento de excećões
		return this.dao.findAllByOrderByNome();
	}

	/**
	 * Remove um registro de arquivo da base de dados do sistema, desde que não
	 * existam certificados emiitidos para os registros do arquivo.
	 * 
	 * @param idArquivo O identificador do arquivo
	 * @exception RuntimeException caso existam certificados emitidos para o
	 *                             arquivo.
	 */
	@Transactional
	public void apagar(Long idArquivo) {
		// FIXME: especializar a tipagem de excecoes
		if (idArquivo == null) {
			throw new RuntimeException("O identificador do arquivo não pode ser nulo");
		}

		Optional<Arquivo> arquivo = this.dao.findById(idArquivo);

		// FIXME: especializar a tipagem de excecoes
		if (!arquivo.isPresent()) {
			throw new RuntimeException("O arquivo de id:" + idArquivo + " não foi encontrado.");
		}

		if (this.pessoaCertificadaDao.countByArquivoId(idArquivo) > 0) {
			throw new RuntimeException("O arquivo não pode ser excluído pois possui certificados associados");
		}

		// FIXME: acrescentar tratamento de excecoes
		this.dao.deleteById(idArquivo);
	}

	/**
	 * Salva o arquivo na base de dados. Só são aceitos o formato text/csv
	 * 
	 * @param arquivo O arquivo a ser salvo na base de dados
	 * @param tipo    O tipo de certificacao a ser emitida pelo sistema
	 * @return o {@link Arquivo} salvo no sistema.
	 * @exception RuntimeException caso o arquivo informado seja nulo ou não esteja
	 *                             no formato text/cv ou o tipo não tenha sido
	 *                             informado
	 */
	@Transactional
	public Arquivo salvar(MultipartFile arquivo, TipoCertificadoEnum tipo) {
		Arquivo registro = null;

		if (arquivo == null) {
			// fixme: especializar a tipagem de excecoes
			throw new RuntimeException("O arquivo não pode ser nulo");
		}

		// fixme: fixar os tipos de arquivos supportados
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

			// fixme: melhorar o tratamento de excecoes utilizando aspectos
		} catch (Exception e) {
			log.error("Ocorreu um erro ao salvar arquivo:" + arquivo.getName(), e);
			throw new RuntimeException("Ocorreu um erro ao salvar arquivo:" + arquivo.getName(), e);
		}

		return registro;
	}

	/**
	 * Extrai do arquivo as informações para geração dos certificados. As
	 * informações são extraídas em função do tipo de certificado que foi informado
	 * no arquivo.
	 * 
	 * @param idArquivo O identificador do arquivo que está armazenado na base de
	 *                  dados do sistema.
	 * @return A quantidade de registros que foram extraídos.
	 */
	@Transactional
	public int extrair(Long idArquivo) {
		int qtdExtraidos = 0;

		Optional<Arquivo> arquivo = this.dao.findById(idArquivo);

		if (!arquivo.isPresent()) {
			throw new RuntimeException("Não foi possível localizar o arquivo:" + idArquivo);
		}

		switch (arquivo.get().getTipo()) {
		case MINISTRANTE:
			qtdExtraidos = this.extrairDadosMinistrantes(arquivo);
			break;
		case APRESENTADOR:
			qtdExtraidos = this.extrairDadosApresentadores(arquivo);
			break;
		case AVALIADOR:
			qtdExtraidos = this.extrairDadosAvaliadores(arquivo);
			break;
		case MONITOR:
			qtdExtraidos = this.extrairDadosMonitores(arquivo);
			break;
		case OUVINTE:
			qtdExtraidos = this.extrairDadosOuvintes(arquivo);
			break;
		default:
			throw new RuntimeException("Não foi possível identificar o tipo de extração do arquivo");
		}

		return qtdExtraidos;
	}

	private int extrairDadosOuvintes(Optional<Arquivo> arquivo) {
		int naoEncontrados = 0;
		int qtdExtraidos = 0;

		List<RegistroArquivoOuvintesVO> registrosDeCertificados = this.lerArquivo(arquivo.get(),
				RegistroArquivoOuvintesVO.class);

		List<PessoaCertificada> entidades = new ArrayList<>();
		OuvinteVO ouvinteListaAtividades = null;
		OuvinteVO ouvinteListaDoyt = null;

		byte[] conteudoArquivoDoyt;

		try {
			conteudoArquivoDoyt = FileUtils.readFileToByteArray(new File("doyt/DADOS-DOYT-2022.csv"));

		} catch (IOException e) {
			throw new RuntimeException("Ocorreu um erro ao extrair os dados dos ouvintes.", e);
		}

		Arquivo arquivoDadosComplementaresDoyt = Arquivo.builder().arquivo(conteudoArquivoDoyt).build();

		List<RegistroArquivoDadosComplementaresUsuariosDoytVO> ouvintesDoyt = this
				.lerArquivo(arquivoDadosComplementaresDoyt, RegistroArquivoDadosComplementaresUsuariosDoytVO.class);

		for (RegistroArquivoOuvintesVO registroCertificado : registrosDeCertificados) {
			if (registroCertificado != null) {

				ouvinteListaAtividades = registroCertificado.getOuvinteBO(arquivo.get().getId());

				for (RegistroArquivoDadosComplementaresUsuariosDoytVO regDoytVO : ouvintesDoyt) {

					ouvinteListaDoyt = regDoytVO.getOuvinteBO(arquivo.get().getId());

					if (equalsIgnoreCase(trimAllWhitespace(ouvinteListaAtividades.getNomeTratado()),
							trimAllWhitespace(ouvinteListaDoyt.getNomeTratado()))) {

						registroCertificado.setEmail(ouvinteListaDoyt.getEmail());
						registroCertificado.setDoitId(ouvinteListaDoyt.getDoitId());

						break;
					}
				}

				if (!hasLength(ouvinteListaAtividades.getEmail())) {
					naoEncontrados++;
				}

			}
		}

		Map<String, PessoaCertificada> ouvintes = new HashMap<>();
		PessoaCertificada ouvinte = null;
		String atividade = null;

		for (RegistroArquivoOuvintesVO registroCertificado : registrosDeCertificados) {

			ouvinte = ouvintes.get(registroCertificado.getNomeTratado());

			if (ouvinte == null) {

				ouvinte = registroCertificado.getOuvinte(arquivo.get().getId());

				ouvinte.setCargaHoraria(0d);
				ouvinte.setAtividades("");
				ouvinte.setExportado(false);
				ouvinte.setTipo(TipoCertificadoEnum.OUVINTE);
				ouvinte.setCertificadoEnviado(false);
				ouvinte.setArquivo(Arquivo.builder().id(arquivo.get().getId()).build());

				ouvintes.put(registroCertificado.getNomeTratado(), ouvinte);
			}

			atividade = ouvinte.getAtividades().length() == 0 ? "" : ";";

			atividade += registroCertificado.getAtividade();

			ouvinte.setCargaHoraria(ouvinte.getCargaHoraria() + Double.valueOf(registroCertificado.getCargaHoraria()));
			ouvinte.setAtividades(ouvinte.getAtividades() + atividade);

			entidades.add(ouvinte);
		}

		qtdExtraidos = ((Collection<?>) this.pessoaCertificadaDao.saveAll((List<PessoaCertificada>) entidades)).size();

		Log.info("registros extraídos: " + qtdExtraidos + " incompletos: " + naoEncontrados + " registros.");

		return qtdExtraidos;
	}

	private int extrairDadosMonitores(Optional<Arquivo> arquivo) {
		int qtdExtraidos = 0;

		List<RegistroArquivoMonitoresVO> registrosDeCertificados = this.lerArquivo(arquivo.get(),
				RegistroArquivoMonitoresVO.class);

		List<PessoaCertificada> entidades = new ArrayList<>();

		for (RegistroArquivoMonitoresVO registroCertificado : registrosDeCertificados) {
			if (registroCertificado != null) {
				entidades.add(registroCertificado.getParticipante(arquivo.get().getId()));
			}
		}

		qtdExtraidos = ((Collection<?>) this.pessoaCertificadaDao.saveAll((List<PessoaCertificada>) entidades)).size();

		Log.info("registros extraídos: " + qtdExtraidos);

		return qtdExtraidos;

	}

	private int extrairDadosAvaliadores(Optional<Arquivo> arquivo) {
		int qtdExtraidos = 0;

		List<RegistroArquivoAvaliadoresVO> registrosDeCertificados = this.lerArquivo(arquivo.get(),
				RegistroArquivoAvaliadoresVO.class);

		List<PessoaCertificada> entidades = new ArrayList<>();
		for (RegistroArquivoAvaliadoresVO registroCertificado : registrosDeCertificados) {
			if (registroCertificado != null) {
				entidades.add(registroCertificado.getParticipante(arquivo.get().getId()));
			}
		}

		qtdExtraidos = ((Collection<?>) this.pessoaCertificadaDao.saveAll((List<PessoaCertificada>) entidades)).size();

		Log.info("registros extraídos: " + qtdExtraidos);

		return qtdExtraidos;

	}

	private int extrairDadosApresentadores(Optional<Arquivo> arquivo) {
		int qtdExtraidos = 0;

		List<RegistroArquivoApresentadoresVO> registrosDeCertificados = this.lerArquivo(arquivo.get(),
				RegistroArquivoApresentadoresVO.class);

		List<PessoaCertificada> entidades = new ArrayList<>();
		for (RegistroArquivoApresentadoresVO registroCertificado : registrosDeCertificados) {
			if (registroCertificado != null) {
				entidades.add(registroCertificado.getParticipante(arquivo.get().getId()));
			}
		}

		qtdExtraidos = ((Collection<?>) this.pessoaCertificadaDao.saveAll((List<PessoaCertificada>) entidades)).size();

		Log.info("registros extraídos: " + qtdExtraidos);

		return qtdExtraidos;

	}

	private int extrairDadosMinistrantes(Optional<Arquivo> arquivo) {
		int qtdExtraidos = 0;

		List<RegistroArquivoMinistranteMediadorVO> registrosDeCertificados = this.lerArquivo(arquivo.get(),
				RegistroArquivoMinistranteMediadorVO.class);

		List<PessoaCertificada> entidades = new ArrayList<>();
		for (RegistroArquivoMinistranteMediadorVO registroCertificado : registrosDeCertificados) {
			if (registroCertificado != null) {
				entidades.add(registroCertificado.getParticipante(arquivo.get().getId()));
			}
		}

		qtdExtraidos = ((Collection<?>) this.pessoaCertificadaDao.saveAll((List<PessoaCertificada>) entidades)).size();

		Log.info("registros extraídos: " + qtdExtraidos);

		return qtdExtraidos;

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
