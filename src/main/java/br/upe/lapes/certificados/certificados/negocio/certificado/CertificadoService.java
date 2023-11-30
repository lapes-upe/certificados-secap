package br.upe.lapes.certificados.certificados.negocio.certificado;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.mail.Message;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.upe.lapes.certificados.certificados.negocio.arquivo.Arquivo;
import br.upe.lapes.certificados.certificados.negocio.arquivo.IArquivoDAO;
import br.upe.lapes.certificados.certificados.negocio.atividade.IPessoaCertificadaDAO;
import br.upe.lapes.certificados.certificados.negocio.atividade.PessoaCertificada;
import br.upe.lapes.certificados.certificados.negocio.certificado.beans.CertificadoVO;
import br.upe.lapes.certificados.certificados.negocio.certificado.modelo.ParametrizacaoCertificadoVO;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperRunManager;

@Slf4j
@Service
public class CertificadoService {

	@Value("${secap.edicao}")
	private String EDICAO_SECAP;

	// @Value("${secap.tema}") FIXME: RESOLVER O PROBLEMA DO UTF-8 COM O SPRING BOOT
	private String TEMA_SECAP = "EDUCAÇÃO ONIPRESENTE FOMENTANDO O ENSINO, PESQUISA E EXTENSÃO";

	@Value("${secap.data}")
	private String DATA_SECAP;

	@Value("${secap.email.tamanhoEnvioLote}")
	private String TAMANHO_LOTE_ENVIO_EMAIL;

	@Value("${secap.email.tempoEsperaEnvioLote}")
	private String TEMPO_ESPERA_ENVIO_LOTE_EMAIL;

	// @Value("${secap.titulo}") FIXME: RESOLVER O PROBLEMA DO UTF-8 COM O SPRING
	// BOOT
	private String TITULO_EVENTO = "IV Semana Científica do Agreste Pernambucano – SECAP";

	@Value("${secap.formulario.contato}")
	private String LINK_FORMULARIO_CONTATO;

	@Value("${secap.issn}")
	private String ISSN;

	@Autowired
	private IPessoaCertificadaDAO pessoaCertificadaDAO;

	@Autowired
	private IArquivoDAO arquivoDAO;

	@Autowired
	private VelocityEngine velocity;

	@Autowired
	private JavaMailSender emailSender;

	/**
	 * Envia todos as certificações de um lote por email.
	 *  
	 * @param idArquivo O identificador do arquivo de lote.
	 * @exception @link{RuntimeException} caso o identificador não seja informado ou o arquivo não seja  identificado
	 */
	@Transactional
	public void enviarEmailComCertificados(Long idArquivo) {

		if (idArquivo == null) {
			log.error("Não foi informado o identificador do arquivo para o envio dos emails com as certificações");
			throw new RuntimeException("Informe o identificador do arquivo de lote para o envio das certificações");
		}

		Optional<Arquivo> arquivo = this.arquivoDAO.findById(idArquivo);

		if (!arquivo.isPresent()) {
			log.error("Não foi possível identificar o arquivo de lote informado");
			throw new RuntimeException("Não foi possível identificar o arquivo de lote informado");
		}

		//FIXME: corrigir o código para ligar com a paginação dos resultados
		List<PessoaCertificada> pessoasCertificadas = this.pessoaCertificadaDAO.findAllNotSendedByArquivoId(idArquivo,
				PageRequest.of(0, Integer.valueOf(200)));

		if (pessoasCertificadas != null && !pessoasCertificadas.isEmpty()) {

			boolean enviado = false;
			
			for (PessoaCertificada participante : pessoasCertificadas) {
				enviado = this.enviarEmailComCertificado(participante, null);
				
				participante.setExportado(enviado);
				participante.setDataUltimaAlteracao(LocalDateTime.now());
			}

			this.pessoaCertificadaDAO.saveAll(pessoasCertificadas);
		}
	}

	/**
	 * Envia a certificação da pessoa certificada para o email informado. Caso o
	 * email informado seja em branco será utilizado o email de cadastro da pessoa
	 * certificada.
	 * 
	 * @param idPessoaCertificada O identificador da pessoa certificada
	 * @param email               O email para envio da certificação ou
	 *                            <code>null</code> caso deseje que seja enviada a
	 *                            certificação para o email da pessoa certificada.
	 */
	@Transactional
	public void enviarEmailComCertificado(Long idPessoaCertificada, String email) {

		if (idPessoaCertificada == null) {
			log.error("O identificador da pessoa certificada não foi informado");
			throw new RuntimeException("Informe o identificador da pessoa certificada para localização do certificado");
		}

		Optional<PessoaCertificada> pessoasCertificadas = this.pessoaCertificadaDAO.findById(idPessoaCertificada);

		if (!pessoasCertificadas.isPresent()) {
			log.error("Não foi possível identificar pessoas certificadas");
			throw new RuntimeException("Não foi possível identificar a pessoa certificada através do identificador informado.");
		}

		this.enviarEmailComCertificado(pessoasCertificadas.get(), email);
	}

	/**
	 * Processo asíncrono de geração de certificados de um arquivo.
	 * 
	 * @param idArquivo O identificador do arquivo ao qual devem ser geradas as certificações.
	 */
	@Async
	@Transactional
	public void gerarCertificados(Long idArquivo) {
		log.info("iniciando geração de certificação do arquivo:" + idArquivo);

		if (idArquivo == null) {
			log.error("Não foi informado o identificador do arquivo para geração dos certificados");
			throw new RuntimeException("Informe o identificador do arquivo para a geração dos certificados");
		}

		Optional<Arquivo> arquivo = this.arquivoDAO.findById(idArquivo);

		if (!arquivo.isPresent()) {
			log.error("Não foi possível localizar um arquivo para o identificador:" + idArquivo);
			throw new RuntimeException("Não foi possível localizar um arquivo para o identificador:" + idArquivo);
		}

		try {

			List<PessoaCertificada> participantes = this.pessoaCertificadaDAO.findAllByArquivoIdOrderByNome(idArquivo);
			
			if (participantes == null || participantes.isEmpty()) {
				log.info("não existem certificados para gerar para o arquivo:" + idArquivo);
			} else {
				log.info(participantes.size() + " certificados para gerar para o arquivo:" + idArquivo);
				
				for (PessoaCertificada ouvinte : participantes) {
					this.gerarCertificado(ouvinte);
				}
			}
			
			this.pessoaCertificadaDAO.saveAll(participantes);

			log.info("Geração de certificados concluída para o arquivo:" + idArquivo);

		} catch (Exception e) {
			log.error("Ocorreu um erro ao gerar certificados do arquivo:" + idArquivo, e);
			throw new RuntimeException("Ocorreu um erro ao gerar certificados do arquivo:" + idArquivo, e);
		}
	}

	/**
	 * Gera certificação da pessoa.
	 * 
	 * @param id O identificador da pessoa a ser certificada.
	 */
	@Transactional
	public void gerarCertificado(Long id) {
		log.info("iniciando certificação de pessoa:" + id + ".");
		
		Optional<PessoaCertificada> pessoaCertificada = this.pessoaCertificadaDAO.findById(id);

		this.gerarCertificado(pessoaCertificada.get());

		this.pessoaCertificadaDAO.save(pessoaCertificada.get());
		
		log.info("certificação de pessoa:" + id + " gerada pelo sistema.");
	}

	/**
	 * Processo assíncrono que gera a exportação em lotes de todos os certificados já gerados pelo sistema.
	 * 
	 * @exception RuntimeException caso ocorra algum erro durante a exportação dos certificados.
	 */
	@Async
	@Transactional
	public void exportarCertificados() {
		log.info("iniciando exportação de certificações");

		try {
			long qtdCertificados = this.pessoaCertificadaDAO.count();
			long tamBuffer = qtdCertificados / 100;

			int resto = (int) (qtdCertificados % 100);
			int offset = 0;
			
			if (qtdCertificados > 0) {

				List<PessoaCertificada> pessoasCertificadas = null;

				do {
					pessoasCertificadas = (List<PessoaCertificada>) this.pessoaCertificadaDAO.findAll(PageRequest.of(offset, 100));
					pessoasCertificadas.stream().forEach(participante -> this.exportarPDFCertificado(participante));
					
					offset++;
				} while (--tamBuffer > 0);

				if (resto > 0) {
					pessoasCertificadas = (List<PessoaCertificada>) this.pessoaCertificadaDAO.findAll(PageRequest.of(offset, 100));
					pessoasCertificadas.stream().forEach(participante -> this.exportarPDFCertificado(participante));
				}

			}

		} catch (Exception e) {
			log.error("Ocorreu um erro ao exportar certificações", e);
			throw new RuntimeException("Ocorreu um erro ao exportar certificações", e);
		}

		log.info("concluindo exportação de certificações");
	}

	/**
	 * Exporta todos os certificados de um determinado lote de arquivos do sistema.
	 * 
	 * @param idArquivo o identificador do arquivo.
	 * @exception RuntimeException caso ocorra algum erro durante a exportação, o identificador do arquivo não for informado ou o arquivo não for identificado. 
	 */
	@Transactional
	public void exportarCertificados(Long idArquivo) {
		log.info("iniciando exportação de certificações do arquivo:" + idArquivo);

		if (idArquivo == null) {
			log.error("Ocorreu um erro ao exportar certificações pois o identificador do arquivo não foi informado");
			throw new RuntimeException("Informe o arquivo para a exportação das certificações");
		}

		Optional<Arquivo> arquivo = this.arquivoDAO.findById(idArquivo);

		if (!arquivo.isPresent()) {
			log.error("Ocorreu um erro ao exportar certificações pois o arquivo não foi identificado");
			throw new RuntimeException("Não foi possível identificar o arquivo para exportação das certificações.");
		}

		try {
			List<PessoaCertificada> participantes = this.pessoaCertificadaDAO.findAllByArquivoIdOrderByNome(idArquivo);

			participantes.stream().forEach(participante -> this.exportarPDFCertificado(participante));

		} catch (Exception e) {
			log.error("Ocorreu um erro ao exportar certificações do arquivo:" + idArquivo, e);
			throw new RuntimeException("Ocorreu um erro ao exportar certificações do arquivo:" + idArquivo, e);
		}

		log.info("concluindo exportação de certificações do arquivo:" + idArquivo);
	}

	/**
	 * Exporta a certificação da pessoa.
	 * 
	 * @param idPessoaCertificada O identificador da pessoa.
	 * @exception {@link RuntimeException} coso ocorra algum erro ao identificar a pessoa ou ao exportar o pdf
	 */
	@Transactional
	public void exportarCertificado(Long idPessoaCertificada) {
		log.info("iniciando exportação de certificados para a pessoa certificada:" + idPessoaCertificada);

		if (idPessoaCertificada == null) {
			log.error("Ocorreu um erro ao exportar certificações pois o identificador da pessoa certificada não foi informado");
			throw new RuntimeException("Informe o identificador da pessoa certificada para a exportação da certificação");
		}

		try {
			Optional<PessoaCertificada> pessoaCertificada = this.pessoaCertificadaDAO.findById(idPessoaCertificada);

			this.exportarPDFCertificado(pessoaCertificada.get());

			log.info("Exportação de certificado para a pessoa:" + idPessoaCertificada);
		} catch (Exception e) {
			log.error("Ocorreu um erro ao exportar certificado da pessoa:" + idPessoaCertificada, e);
			throw new RuntimeException("Ocorreu um erro ao exportar certificado da pessoa:" + idPessoaCertificada, e);
		}

		log.info("finalizando exportação de certificado da pessoa:" + idPessoaCertificada);
	}

	/**
	 * Busca o certificado de uma pessoa certificada através do seu identificador.
	 * 
	 * @param id O identificador da pessoa certificada.
	 * @return um {@link CertificadoVO} contendo o certificado da pessoa certificada.
	 * @exception uma {@link RuntimeException} caso não encontre um certficado para a pessoa.
	 */
	public CertificadoVO buscarCertificado(Long id) {
		log.info("iniciando a busca de certificação para a pessoa com identificador:" + id);

		Optional<PessoaCertificada> pessoaCertificada = this.pessoaCertificadaDAO.findById(id);

		if (!pessoaCertificada.isPresent()) {
			log.error("Não existe certificado para a pessoa:" + id);
			throw new RuntimeException("Não existe certificado para a pessoa:" + id);
		}

		if (pessoaCertificada.get().getCertificado() == null) {
			this.gerarCertificado(pessoaCertificada.get());
		}

		String nomeArquivo = gerarNomeArquivo(pessoaCertificada.get());

		return CertificadoVO.builder().certificado(pessoaCertificada.get().getCertificado()).nomeArquivo(nomeArquivo)
				.build();
	}

	/**
	 * Envia a certificação da pessoa certificada para o email informado. Caso o
	 * email informado seja em branco será utilizado o email de cadastro da pessoa
	 * certificada.
	 * 
	 * @param pessoaCertificada A pessoa certificada
	 * @param email O email apra o qual será enviada a certificação, caso não seja informado será utilizado o email de cadastro da pessoa certificada.
	 * @return <code>true</code> caso o email tenha sido enviado e <code>false</code> caso o sistema não tenha conseguido enviar o email.
	 * @exception {@link RuntimeException} caso não seja identificada a pessoa ou ocorra algum erro durante o envio do email.
	 */
	@Transactional
	private boolean enviarEmailComCertificado(PessoaCertificada pessoaCertificada, String email) {

		if (pessoaCertificada == null) {
			log.error("Não foi informada a pessoa certificada para o envio do email");
			throw new RuntimeException("Não é possível enviar email de certificados sem os dados do participante");
		}

		if (pessoaCertificada.getCertificado() == null) {
			log.error("Não existe certificado gerado para enviar a pessoa informada");
			throw new RuntimeException(
					this.getMensagemErroEnvioEmailCertificado(pessoaCertificada.getTipo(), pessoaCertificada.getId()));
		}

		String corpoEmail = this.getCorpoEmail(pessoaCertificada.getNome(), pessoaCertificada.getTipo().toString(), TITULO_EVENTO,
				LINK_FORMULARIO_CONTATO);

		if (StringUtils.isBlank(email)) {
			email = pessoaCertificada.getEmail();
		}

		Boolean enviado = false;

		if (StringUtils.isNotBlank(email)) {

			String tituloEmail = "CERTIFICAÇÃO " + TITULO_EVENTO + " - " + pessoaCertificada.getTipo().toString();
			String nomeAnexo = "certificado_participacao_" + pessoaCertificada.getTipo().toString().toLowerCase() + "_"
					+ pessoaCertificada.getNomeTratado();

			MimeMessage mensagem = this.montarEmail(tituloEmail, corpoEmail, email, pessoaCertificada.getCertificado(),
					nomeAnexo);

			enviado = this.enviarEmail(mensagem);

			pessoaCertificada.setDataUltimaAlteracao(LocalDateTime.now());
			pessoaCertificada.setCertificadoEnviado(enviado);

			this.pessoaCertificadaDAO.save(pessoaCertificada);
		} else {
			log.error("Não existe email para o envio do certificado do participante:" + pessoaCertificada.getId());
		}

		return enviado;
	}

	private String getMensagemErroEnvioEmailCertificado(TipoCertificadoEnum tipo, Long id) {
		return "Não existe certificado para a pessoa certificada " + tipo.toString().toLowerCase() + ": " + id;
	}

	/**
	 * Gera a certificação da pessoa.
	 * 
	 * @param pessoa A pessoa a ser certificada.
	 * @exception RuntimeException caso a pessoa ou os dados associados a sua certificação como a carga horária e o nome não sejam informados.
	 */
	@Transactional
	private void gerarCertificado(PessoaCertificada pessoa) {
		if (pessoa == null) {
			log.error("A pessoa a ser certificada não foi informada.");
			throw new RuntimeException("A pessoa a ser certificada não foi informada.");
		}

		if (StringUtils.isBlank(pessoa.getNome())) {
			log.error("O nome da pessoa a ser certificada não foi informado");
			throw new RuntimeException("O nome da pessoa a ser certificada não foi informado. Identificador da pessoa:" + pessoa.getId());
		}

		if ((!TipoCertificadoEnum.APRESENTADOR.equals(pessoa.getTipo())
				&& !TipoCertificadoEnum.AVALIADOR.equals(pessoa.getTipo()))
				&& pessoa.getCargaHoraria() == null) {
			
			log.error("A carga horária a ser certificada não foi informada");
			throw new RuntimeException("Verifique oa carga horária associada a pessoa:" + pessoa.getId());
		}

		ParametrizacaoCertificadoVO parametrizacao = this.getTextoCorpoCertificado(pessoa);

		Map<String, Object> parametros = new HashMap<>();
		parametros.put("Texto", parametrizacao.getTextoCorpo());

		parametros.put("Fundo", parametrizacao.getTemplate());

		pessoa.setCertificado(this.gerarPDFCertificado(parametros));
		pessoa.setDataUltimaAlteracao(LocalDateTime.now());
	}

	/**
	 * obtém o texto do corpo do certificado em função do tipo de certificação a ser emitido pelo sistema.
	 * 
	 * @param participante A pessoa a ser certificada.
	 * @return uma instância de {@link ParametrizacaoCertificadoVO} contendo os dados da certificação a ser emitida.
	 * @exception {@link RuntimeException} caso não seja informada um tipo de certificação a ser emitida.
	 */
	private ParametrizacaoCertificadoVO getTextoCorpoCertificado(PessoaCertificada participante) {
		String template = null;
		String textoCorpo = null;

		switch (participante.getTipo()) {
		case OUVINTE:
			textoCorpo = "CERTIFICAMOS QUE <style isBold='true'>" + participante.getNome()
					+ "</style> PARTICIPOU COMO <style isBold='true'>" + participante.getTipo()
					+ "</style> NA <style isBold='true'>" + this.EDICAO_SECAP
					+ " SEMANA CIENTÍFICA DO AGRESTE PERNAMBUCANO</style> – " + "\"" + this.TEMA_SECAP + "\", "
					+ "PROMOVIDA PELA UNIVERSIDADE DE PERNAMBUCO - CAMPUS GARANHUNS, " + this.DATA_SECAP + ", "
					+ "COM CARGA HORÁRIA DE <style isBold='true'>"
					+ participante.getCargaHoraria().toString().replace(".0", "").replace(".", ",") + "</style> HORAS.";

			template = "certificado_ouvinte.png";

			break;
		case MINISTRANTE:
			textoCorpo = "CERTIFICAMOS QUE <style isBold='true'>" + participante.getNome()
					+ "</style> PARTICIPOU COMO <style isBold='true'>" + participante.getTipo() + "</style> "
					+ participante.getTipoAtividade().toString() + "<style isItalic='true'> \""
					+ participante.getAtividades() + "\"</style> NA <style isBold='true'>" + this.EDICAO_SECAP
					+ " SEMANA CIENTÍFICA DO AGRESTE PERNAMBUCANO</style> – " + "\"" + this.TEMA_SECAP + "\", "
					+ "PROMOVIDA PELA UNIVERSIDADE DE PERNAMBUCO - CAMPUS GARANHUNS, " + this.DATA_SECAP + ", "
					+ "COM CARGA HORÁRIA DE <style isBold='true'>"
					+ participante.getCargaHoraria().toString().replace(".0", "").replace(".", ",") + "</style> HORAS.";

			template = "certificado_palestrante.png";

			break;
		case APRESENTADOR:
			textoCorpo = "CERTIFICAMOS QUE O TRABALHO INTITULADO <style isBold='true'>" + participante.getAtividades()
					+ "</style> FOI APRESENTADO POR <style isBold='true'>" + participante.getNome() + "</style> ";

			if (StringUtils.isNotBlank(participante.getCoautores())) {
				textoCorpo += "COM COAUTORIA DE <style isBold='true'>" + participante.getCoautores();
				textoCorpo += "</style> ";
			}

			textoCorpo += "NA <style isBold='true'>" + this.EDICAO_SECAP
					+ " SEMANA CIENTÍFICA DO AGRESTE PERNAMBUCANO</style> – " + "\"" + this.TEMA_SECAP + "\", "
					+ "PROMOVIDA PELA UNIVERSIDADE DE PERNAMBUCO - CAMPUS GARANHUNS, " + this.DATA_SECAP + ", "
					+ "E PUBLICADO EM FORMATO DE RESUMO EXPANDIDO NOS ANAIS DO EVENTO, DE " + ISSN + ".";

			template = "certificado_palestrante.png";

			break;
		case DOCENTE_COLABORADOR:
			textoCorpo = "CERTIFICAMOS QUE <style isBold='true'>" + participante.getNome().toUpperCase()
					+ "</style> PARTICIPOU COMO DOCENTE COLABORADOR DO EVENTO "
					+ participante.getComissao().toUpperCase() + "<style isBold='true'>" + this.EDICAO_SECAP
					+ " SEMANA CIENTÍFICA DO AGRESTE PERNAMBUCANO</style> – " + "\"" + this.TEMA_SECAP + "\", "
					+ " APROVADO NO EDITAL DE FLUXO CONTÍNUO DE EXTENSÃO REALIZADO NO PERÍODO DE " + this.DATA_SECAP
					+ ", " + "PERFAZENDO UMA CARGA HORÁRIA TOTAL DE <style isBold='true'>"
					+ participante.getCargaHoraria().toString().replace(".0", "").replace(".", ",") + "</style> HORAS.";

			template = "certificado_monitor.png";

			break;

		case MONITOR:
			textoCorpo = "CERTIFICAMOS QUE <style isBold='true'>" + participante.getNome().toUpperCase()
					+ "</style> PARTICIPOU COMO MONITOR(A) DA COMISSÃO " + participante.getComissao().toUpperCase()
					+ " NA <style isBold='true'>" + this.EDICAO_SECAP
					+ " SEMANA CIENTÍFICA DO AGRESTE PERNAMBUCANO</style> – " + "\"" + this.TEMA_SECAP + "\", "
					+ "PROMOVIDA PELA UNIVERSIDADE DE PERNAMBUCO - CAMPUS GARANHUNS, " + this.DATA_SECAP + ", "
					+ "COM CARGA HORÁRIA DE <style isBold='true'>"
					+ participante.getCargaHoraria().toString().replace(".0", "").replace(".", ",") + "</style> HORAS.";

			template = "certificado_monitor.png";

		case AVALIADOR:
			textoCorpo = "CERTIFICAMOS QUE <style isBold='true'>" + participante.getNome().toUpperCase()
					+ "</style> PARTICIPOU COMO AVALIADOR(A) DE TRABALHOS" + " NA <style isBold='true'>"
					+ this.EDICAO_SECAP + " SEMANA CIENTÍFICA DO AGRESTE PERNAMBUCANO</style> – " + "\""
					+ this.TEMA_SECAP + "\", " + "PROMOVIDA PELA UNIVERSIDADE DE PERNAMBUCO - CAMPUS GARANHUNS, "
					+ this.DATA_SECAP + ".";

			template = "certificado_ouvinte.png";

			break;
		default:
			throw new RuntimeException("Informe o tipo de certificado a ser buscado");
		}

		return ParametrizacaoCertificadoVO.builder().textoCorpo(textoCorpo).template(template).build();
	}

	
	private String gerarNomeArquivo(PessoaCertificada participante) {
		ByteBuffer buffer = StandardCharsets.UTF_8.encode(TITULO_EVENTO.toLowerCase().replace(" ", "-") + "-");
		String evento = StandardCharsets.UTF_8.decode(buffer).toString();
		return "certificados/" + participante.getTipo().toString().toLowerCase() + "/" + "certificado-" + evento
				+ participante.getId() + participante.getTipo().toString().toLowerCase() + ".pdf";
	}

	private byte[] gerarPDFCertificado(Map<String, Object> parametros) {
		byte[] certificado = null;

		try {
			InputStream fis = this.getClass().getClassLoader().getResourceAsStream("certificado.jasper");

			ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();

			JasperRunManager.runReportToPdfStream(fis, pdfStream, parametros, new JREmptyDataSource());

			certificado = pdfStream.toByteArray();
		} catch (Exception e) {
			log.error("ocorreu um erro ao gerar certificado", e);
			throw new RuntimeException("ocorreu um erro ao gerar certificado", e);
		}

		return certificado;
	}

	/**
	 * Realiza a exportação do arquivo pdf da certificação da pessoa certificada.
	 * 
	 * @param participante O participante com a informação da certificação.
	 * @return <code>true</code> caso a certificação tenha sido exportada, caso contrário retorna <code>false</code>.
	 */
	@Transactional
	private boolean exportarPDFCertificado(PessoaCertificada participante) {
		boolean exportado = false;

		try {

			FileUtils.writeByteArrayToFile(new File(gerarNomeArquivo(participante)), participante.getCertificado());

			exportado = true;

			log.info("certificado da pessoa certificada " + participante.getNomeTratado() + " exportado");

		} catch (Exception e) {

			log.error("ocorreu um erro ao exportar o certificado da pessoa certificada (" + participante.getId() + "):"
					+ participante.getNome(), e);
		}

		return exportado;
	}

	/**
	 * Envia mensagem de email. Realiza 4 tentativas.
	 * 
	 * @param mensagem A mensagem a ser enviada.
	 * @return <code>true</code> se a mensagem foi enviada, caso contrário retorna <code>false</code>.
	 */
	private boolean enviarEmail(MimeMessage mensagem) {
		boolean enviado = false;

		int reenvio = 1;

		do {
			String destinatario = null;

			try {
				destinatario = mensagem.getRecipients(Message.RecipientType.TO)[0].toString();

				log.info("Iniciando montagem de email para : " + destinatario + " tentativa:" + reenvio);

				this.emailSender.send(mensagem);

				enviado = true;

				log.info("Email enviado para o participante: " + destinatario + " tentativa:" + reenvio);
				break;

			} catch (Exception e) {
				log.info("Erro ao tentar enviar email para o participante: " + destinatario + " tentativa:" + reenvio,
						e);
				reenvio++;
			}

		} while (reenvio < 4);

		return enviado;
	}

	private MimeMessage montarEmail(String subject, String mensagem, String email, byte[] anexo, String nomeAnexo) {
		MimeMessage message = null;

		try {

			message = this.emailSender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom("secap@upe.br");
			helper.setTo(email);
			helper.setSubject(subject);
			helper.setText(mensagem, true);
			helper.addAttachment(nomeAnexo, new ByteArrayResource(anexo), "application/pdf");

		} catch (Exception e) {
			log.error("Erro ao tentar montar email para o participante: " + email, e);
		}

		return message;
	}

	private String getCorpoEmail(String nome, String participacao, String tituloEvento, String linkfFormulario) {
		Template template = velocity.getTemplate("email.vm", "UTF-8");

		VelocityContext context = new VelocityContext();
		context.put("nome", nome);
		context.put("participacao", participacao);
		context.put("tituloEvento", tituloEvento);
		context.put("linkfFormulario", linkfFormulario);

		StringWriter writer = new StringWriter();
		template.merge(context, writer);

		return writer.toString();
	}

}
