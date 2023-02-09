package br.upe.secap.certificados.certificado;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.upe.secap.certificados.arquivo.Atividade;
import br.upe.secap.certificados.arquivo.IAtividadeDAO;
import br.upe.secap.certificados.arquivo.IMonitorDAO;
import br.upe.secap.certificados.arquivo.IOuvinteDAO;
import br.upe.secap.certificados.arquivo.ITrabalhoDAO;
import br.upe.secap.certificados.arquivo.Monitor;
import br.upe.secap.certificados.arquivo.Ouvinte;
import br.upe.secap.certificados.arquivo.Trabalho;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperRunManager;

@Slf4j
@Service
public class CertificadoService {
	
	@Value( "${secap.edicao}" )
	private String EDICAO_SECAP;
	
	@Value( "${secap.tema}" )
	private String TEMA_SECAP;

	@Value( "${secap.data}" )
	private String DATA_SECAP;
	
	@Autowired
	private IOuvinteDAO ouvinteDAO;

	@Autowired
	private IAtividadeDAO atividadeDAO;

	@Autowired
	private ITrabalhoDAO trabalhoDAO;

	@Autowired
	private IMonitorDAO monitorDAO;

	@Autowired
	private VelocityEngine engine;

	@Autowired
	private JavaMailSender emailSender;

	@Transactional
	public void enviarEmailComCertificados(Long idArquivo, TipoCertificadoEnum tipo) {
		switch (tipo) {
		case OUVINTE:
			List<Ouvinte> ouvintes = this.ouvinteDAO.findAllByArquivoIdOrderByNome(idArquivo);

			for (Ouvinte participante : ouvintes) {
				this.enviarEmailComCertificado(participante, null);
			}
			break;
		case ATIVIDADE:
			List<Atividade> atividades = this.atividadeDAO.findAllByArquivoIdOrderByNome(idArquivo);

			for (Atividade atividade : atividades) {
				this.enviarEmailComCertificado(atividade, null);
			}
			break;
		case APRESENTADOR:
			this.trabalhoDAO.findAllByArquivoIdOrderByApresentador(idArquivo).stream()
					.map(t -> this.enviarEmailComCertificado(t, null)).collect(Collectors.toList());
			break;
		case MONITOR:

			this.monitorDAO.findAllByArquivoIdOrderByNome(idArquivo).stream()
					.map(monitor -> this.enviarEmailComCertificado(monitor, null)).collect(Collectors.toList());
			break;
		default:
			throw new RuntimeException("Informe o tipo de certificado a ser enviado.");
		}
	}

	@Transactional
	public boolean enviarEmailComCertificado(Long id, String email, TipoCertificadoEnum tipo) {
		boolean enviado = false;

		switch (tipo) {
		case OUVINTE:
			Optional<Ouvinte> ouvinte = this.ouvinteDAO.findById(id);
			enviado = this.enviarEmailComCertificado(ouvinte.get(), email);
			break;
		case ATIVIDADE:
			Optional<Atividade> atividade = this.atividadeDAO.findById(id);
			enviado = this.enviarEmailComCertificado(atividade.get(), email);
			break;
		case APRESENTADOR:
			Optional<Trabalho> trabalho = this.trabalhoDAO.findById(id);
			enviado = this.enviarEmailComCertificado(trabalho.get(), email);
			break;
		case MONITOR:
			Optional<Monitor> monitor = this.monitorDAO.findById(id);
			enviado = this.enviarEmailComCertificado(monitor.get(), email);
			break;
		default:
			throw new RuntimeException("Informe o tipo de certificado a ser enviado.");
		}

		return enviado;
	}

	@Transactional
	public boolean enviarEmailComCertificado(Object objeto, String email, TipoCertificadoEnum tipo) {
		boolean enviado = false;

		switch (tipo) {
		case OUVINTE:
			enviado = this.enviarEmailComCertificado((Ouvinte) objeto, email);
			break;
		case ATIVIDADE:
			enviado = this.enviarEmailComCertificado((Atividade) objeto, email);
			break;
		case APRESENTADOR:
			enviado = this.enviarEmailComCertificado((Trabalho) objeto, email);
			break;
		case MONITOR:
			enviado = this.enviarEmailComCertificado((Monitor) objeto, email);
			break;
		default:
			throw new RuntimeException("Você deve informar o tipo do certificado a ser emitido");
		}

		return enviado;
	}

	@Transactional
	public void gerarCertificados(Long idArquivo, TipoCertificadoEnum tipo) {
		log.info("iniciando exportação de certificados de " + tipo.name() + "do arquivo:" + idArquivo);

		try {

			switch (tipo) {
			case OUVINTE:
				this.gerarCertificadosOuvintes(idArquivo);
				break;
			case ATIVIDADE:
				this.gerarCertificadosAtividades(idArquivo);
				break;
			case APRESENTADOR:
				this.gerarCertificadosTrabalhos(idArquivo);
				break;
			case MONITOR:
				this.gerarCertificadosMonitores(idArquivo);
				break;
			default:
				throw new RuntimeException("Informe o tipo de certificado a ser gerado");
			}

			log.info("Exportação de certificados concluída para o arquivo:" + idArquivo);
		} catch (Exception e) {
			log.error("Ocorreu um erro ao exportar certificados de " + tipo.name() + " do arquivo:" + idArquivo, e);

			throw new RuntimeException(
					"Ocorreu um erro ao exportar certificados de " + tipo.name() + " do arquivo:" + idArquivo, e);
		}
	}

	@Transactional
	public void exportarCertificados(Long idArquivo, TipoCertificadoEnum tipo) {
		log.info("iniciando exportação de certificados de " + tipo.name() + "do arquivo:" + idArquivo);

		try {

			switch (tipo) {
			case OUVINTE:
				this.exportarCertificadosOuvintes(idArquivo);
				break;
			case ATIVIDADE:
				this.exportarCertificadosAtividades(idArquivo);
				break;
			case APRESENTADOR:
				this.exportarCertificadosTrabalhos(idArquivo);
				break;
			case MONITOR:
				this.exportarCertificadosMonitores(idArquivo);
				break;
			default:
				throw new RuntimeException("Informe o tipo de certificado a ser exportado");
			}

			log.info("Exportação de certificados de ouvintes concluída para o arquivo:" + idArquivo);
		} catch (Exception e) {
			log.error("Ocorreu um erro ao exportar certificados de " + tipo.name() + " do arquivo:" + idArquivo, e);

			throw new RuntimeException(
					"Ocorreu um erro ao exportar certificados de " + tipo.name() + " do arquivo:" + idArquivo, e);
		}
	}

	private boolean enviarEmailComCertificado(Ouvinte ouvinte, String email) {

		if (ouvinte.getCertificado() == null) {
			throw new RuntimeException("Não existe certificado para o ouvinte:" + ouvinte.getId());
		}

		if (StringUtils.isBlank(ouvinte.getEmail())) {
			throw new RuntimeException("Não existe email cadastrado para o ouvinte:" + ouvinte.getId());
		}

		String corpoEmail = this.getCorpoEmail(ouvinte.getNome(), "OUVINTE");

		if (StringUtils.isBlank(email)) {
			email = ouvinte.getEmail();
		}

		MimeMessage mensagem = this.montarEmail("CERTIFICAÇÃO III SECAP - OUVINTES", corpoEmail, email,
				ouvinte.getCertificado(), "certificado_participacao_" + ouvinte.getNomeTratado());

		Boolean enviado = this.enviarEmail(mensagem);

		ouvinte.setDataUltimaAlteracao(LocalDateTime.now());
		ouvinte.setCertificadoEnviado(enviado);

		this.ouvinteDAO.save(ouvinte);

		return enviado;
	}

	private boolean enviarEmailComCertificado(Atividade atividade, String email) {

		if (atividade.getCertificado() == null) {
			throw new RuntimeException("Não existe certificado para a atividade:" + atividade.getId());
		}

		if (StringUtils.isBlank(atividade.getEmail())) {
			throw new RuntimeException("Não existe email cadastrado para a atividade:" + atividade.getId());
		}

		String corpoEmail = this.getCorpoEmail(atividade.getNome(), atividade.getTipoParticipacao().toString());

		if (StringUtils.isBlank(email)) {
			email = atividade.getEmail();
		}

		MimeMessage mensagem = this.montarEmail(
				"CERTIFICAÇÃO III SECAP - " + atividade.getTipoParticipacao().paraImprimir(), corpoEmail, email,
				atividade.getCertificado(), "certificado_atividade_" + atividade.getNomeTratado());

		Boolean enviado = this.enviarEmail(mensagem);

		atividade.setDataUltimaAlteracao(LocalDateTime.now());
		atividade.setCertificadoEnviado(enviado);

		this.atividadeDAO.save(atividade);

		return enviado;
	}

	private boolean enviarEmailComCertificado(Trabalho trabalho, String email) {

		if (trabalho.getCertificado() == null) {
			throw new RuntimeException("Não existe certificado para a apresentação de trabalho:" + trabalho.getId());
		}

		if (StringUtils.isBlank(trabalho.getEmail())) {
			throw new RuntimeException(
					"Não existe email cadastrado para a apresentação de trabalho:" + trabalho.getId());
		}

		String corpoEmail = this.getCorpoEmail(trabalho.getApresentador(), "APRESENTADOR");

		if (StringUtils.isBlank(email)) {
			email = trabalho.getEmail();
		}

		MimeMessage mensagem = this.montarEmail("CERTIFICAÇÃO III SECAP - APRESENTADOR(A) DE TRABALHO", corpoEmail,
				email, trabalho.getCertificado(), "certificado_apresentador_" + trabalho.getNomeApresentadorTratado());

		Boolean enviado = this.enviarEmail(mensagem);

		trabalho.setDataUltimaAlteracao(LocalDateTime.now());
		trabalho.setCertificadoEnviado(enviado);

		this.trabalhoDAO.save(trabalho);

		return enviado;
	}

	private boolean enviarEmailComCertificado(Monitor monitor, String email) {

		if (monitor.getCertificado() == null) {
			throw new RuntimeException("Não existe certificado para o monitor:" + monitor.getId());
		}

		if (StringUtils.isBlank(monitor.getEmail())) {
			throw new RuntimeException("Não existe email cadastrado para o monitor:" + monitor.getId());
		}

		String corpoEmail = this.getCorpoEmail(monitor.getNome(), "MONITOR");

		if (StringUtils.isBlank(email)) {
			email = monitor.getEmail();
		}

		MimeMessage mensagem = this.montarEmail("CERTIFICAÇÃO III SECAP - MONITORES", corpoEmail, email,
				monitor.getCertificado(), "certificado_monitor_" + monitor.getNomeTratado());

		Boolean enviado = this.enviarEmail(mensagem);

		monitor.setDataUltimaAlteracao(LocalDateTime.now());
		monitor.setCertificadoEnviado(enviado);

		this.monitorDAO.save(monitor);

		return enviado;
	}

	private void gerarCertificadosOuvintes(Long idArquivo) {
		log.info("iniciando geração de certificados de ouvintes do arquivo:" + idArquivo);

		List<Ouvinte> ouvintes = this.ouvinteDAO.findAllByArquivoIdOrderByNome(idArquivo);

		for (Ouvinte ouvinte : ouvintes) {
			this.gerarCertificado(ouvinte);
		}

		this.ouvinteDAO.saveAll(ouvintes);

		log.info("Geração de certificados de ouvintes concluída para o arquivo:" + idArquivo);
	}

	private void gerarCertificadosAtividades(Long idArquivo) {
		log.info("iniciando geração de certificados de atividades do arquivo:" + idArquivo);

		List<Atividade> atividades = this.atividadeDAO.findAllByArquivoIdOrderByNome(idArquivo);

		for (Atividade atividade : atividades) {
			this.gerarCertificado(atividade);
		}

		this.atividadeDAO.saveAll(atividades);

		log.info("Geração de certificados de atividades concluída para o arquivo:" + idArquivo);
	}

	private void gerarCertificadosTrabalhos(Long idArquivo) {
		log.info("iniciando geração de certificados de apresentação de trabalhos do arquivo:" + idArquivo);

		List<Trabalho> trabalhos = this.trabalhoDAO.findAllByArquivoIdOrderByApresentador(idArquivo);

		for (Trabalho trabalho : trabalhos) {
			this.gerarCertificado(trabalho);
		}

		this.trabalhoDAO.saveAll(trabalhos);

		log.info("Geração de certificados de apresentação de trabalhos concluída para o arquivo:" + idArquivo);
	}

	private void gerarCertificadosMonitores(Long idArquivo) {
		log.info("iniciando geração de certificados de monitores do arquivo:" + idArquivo);

		List<Monitor> monitores = this.monitorDAO.findAllByArquivoIdOrderByNome(idArquivo);

		for (Monitor trabalho : monitores) {
			this.gerarCertificado(trabalho);
		}

		this.monitorDAO.saveAll(monitores);

		log.info("Geração de certificados de monitores concluída para o arquivo:" + idArquivo);
	}

	private void exportarCertificadosOuvintes(Long idArquivo) throws IOException {
		List<Ouvinte> ouvintes = this.ouvinteDAO.findAllByArquivoIdOrderByNome(idArquivo);

		FileUtils.cleanDirectory(new File("certificados/ouvintes/"));

		for (Ouvinte ouvinte : ouvintes) {
			ouvinte.setExportado(false);
		}

		this.ouvinteDAO.saveAll(ouvintes);

		for (Ouvinte ouvinte : ouvintes) {
			this.exportarPDFCertificado(ouvinte);
		}

		this.ouvinteDAO.saveAll(ouvintes);

	}

	private void exportarCertificadosAtividades(Long idArquivo) throws IOException {
		List<Atividade> atividades = this.atividadeDAO.findAllByArquivoIdOrderByNome(idArquivo);

		FileUtils.cleanDirectory(new File("certificados/atividades/"));

		for (Atividade atividade : atividades) {
			atividade.setExportado(false);
		}

		this.atividadeDAO.saveAll(atividades);

		for (Atividade atividade : atividades) {
			this.exportarPDFCertificado(atividade);
		}

		this.atividadeDAO.saveAll(atividades);
	}

	private void exportarCertificadosTrabalhos(Long idArquivo) throws IOException {

		List<Trabalho> trabalhos = this.trabalhoDAO.findAllByArquivoIdOrderByApresentador(idArquivo);

		FileUtils.cleanDirectory(new File("certificados/trabalhos/"));

		for (Trabalho trabalho : trabalhos) {
			trabalho.setExportado(false);
		}

		this.trabalhoDAO.saveAll(trabalhos);

		for (Trabalho trabalho : trabalhos) {
			this.exportarPDFCertificado(trabalho);
		}

		this.trabalhoDAO.saveAll(trabalhos);

		log.info("Exportação de certificados de apresentação de trabalhos concluída para o arquivo:" + idArquivo);

	}

	private void exportarCertificadosMonitores(Long idArquivo) throws IOException {

		List<Monitor> monitores = this.monitorDAO.findAllByArquivoIdOrderByNome(idArquivo);

		FileUtils.cleanDirectory(new File("certificados/monitores/"));

		for (Monitor monitor : monitores) {
			monitor.setExportado(false);
		}

		this.monitorDAO.saveAll(monitores);

		for (Monitor monitor : monitores) {
			this.exportarPDFCertificado(monitor);
		}

		this.monitorDAO.saveAll(monitores);

		log.info("Exportação de certificados de monitores concluída para o arquivo:" + idArquivo);

	}

	@Transactional
	private void gerarCertificadoOuvinte(Long id) {
		Optional<Ouvinte> ouvinte = this.ouvinteDAO.findById(id);

		this.gerarCertificado(ouvinte.get());

		this.ouvinteDAO.save(ouvinte.get());
	}

	@Transactional
	public void gerarCertificadoAtividade(Long id) {
		Optional<Atividade> atividade = this.atividadeDAO.findById(id);

		this.gerarCertificado(atividade.get());

		this.atividadeDAO.save(atividade.get());
	}

	@Transactional
	public void gerarCertificadoTrabalho(Long id) {
		Optional<Trabalho> trabalho = this.trabalhoDAO.findById(id);

		this.gerarCertificado(trabalho.get());

		this.trabalhoDAO.save(trabalho.get());
	}

	@Transactional
	public void gerarCertificadoMonitor(Long id) {
		Optional<Monitor> monitor = this.monitorDAO.findById(id);

		this.gerarCertificado(monitor.get());

		this.monitorDAO.save(monitor.get());
	}

	private void gerarCertificado(Ouvinte ouvinte) {
		if (ouvinte == null) {
			throw new RuntimeException("Não existe ouvinte cadastrado com o identificador informado.");
		}

		if (StringUtils.isBlank(ouvinte.getNome()) || ouvinte.getCargaHoraria() == null) {
			throw new RuntimeException("Verifique o nome e a carga horária do ouvinte:" + ouvinte.getId());
		}

		String texto = "CERTIFICAMOS QUE <style isBold='true'>" + ouvinte.getNome()
				+ "</style> PARTICIPOU COMO <style isBold='true'>OUVINTE"
				+ "</style> NA <style isBold='true'>"
				+ this.EDICAO_SECAP
				+ " SEMANA CIENTÍFICA DO AGRESTE PERNAMBUCANO</style> – "
				+ "\""
				+ this.TEMA_SECAP
				+ "\", "
				+ "PROMOVIDA PELA UNIVERSIDADE DE PERNAMBUCO, "
				+ this.DATA_SECAP
				+", "
				+ "COM CARGA HORÁRIA DE <style isBold='true'>" + ouvinte.getCargaHoraria().toString()
				+ "</style> HORAS.";

		Map<String, Object> parametros = new HashMap<>();
		parametros.put("Texto", texto);
		parametros.put("Fundo", "certificado_ouvinte.png");

		ouvinte.setCertificado(this.gerarPDFCertificado(parametros));
		ouvinte.setDataUltimaAlteracao(LocalDateTime.now());
	}

	private void gerarCertificado(Atividade atividade) {
		if (atividade == null) {
			throw new RuntimeException("Não existe atividade cadastrada com o identificador informado.");
		}

		if (StringUtils.isBlank(atividade.getTitulo()) || atividade.getCargaHoraria() == null) {
			throw new RuntimeException("Verifique o título e a carga horária da atividade:" + atividade.getId());
		}

		String texto = "CERTIFICAMOS QUE <style isBold='true'>" + atividade.getNome()
				+ "</style> PARTICIPOU COMO <style isBold='true'>" + atividade.getTipoParticipacao().paraImprimir()
				+ "</style> " + atividade.getTipoAtividade().toString() + "<style isItalic='true'> \""
				+ atividade.getTitulo()
				+ "\"</style> NA <style isBold='true'>"
				+ this.EDICAO_SECAP
				+ " SEMANA CIENTÍFICA DO AGRESTE PERNAMBUCANO</style> – "
				+ "\""
				+ this.TEMA_SECAP
				+ "\", "
				+ "PROMOVIDA PELA UNIVERSIDADE DE PERNAMBUCO, "
				+ this.DATA_SECAP
				+", "
				+ "COM CARGA HORÁRIA DE <style isBold='true'>"
				+ atividade.getCargaHoraria().toString().replace(".0", "") + "</style> HORAS.";

		Map<String, Object> parametros = new HashMap<>();
		parametros.put("Texto", texto);
		parametros.put("Fundo", "certificado_palestrante.png");

		atividade.setCertificado(this.gerarPDFCertificado(parametros));
		atividade.setDataUltimaAlteracao(LocalDateTime.now());
	}

	private void gerarCertificado(Trabalho trabalho) {
		if (trabalho == null) {
			throw new RuntimeException("Não existe apresentação de trabalho cadastrada com o identificador informado.");
		}

		if (StringUtils.isBlank(trabalho.getTitulo()) || trabalho.getApresentador() == null) {
			throw new RuntimeException("Verifique o título e apresentador da atividade:" + trabalho.getId());
		}

		String texto = "CERTIFICAMOS QUE O TRABALHO INTITULADO <style isBold='true'>" + trabalho.getTitulo()
				+ "</style> FOI APRESENTADO POR <style isBold='true'>" + trabalho.getApresentador() + "</style>, ";

		if (StringUtils.isNotBlank(trabalho.getCoautores())) {
			texto += "COM COAUTORIA DE <style isBold='true'>" + trabalho.getCoautores();
			texto += "</style> ";
		}

		texto += "NA <style isBold='true'>"
				+ this.EDICAO_SECAP
				+ " SEMANA CIENTÍFICA DO AGRESTE PERNAMBUCANO</style> – "
				+ "\""
				+ this.TEMA_SECAP
				+ "\", "
				+ "PROMOVIDA PELA UNIVERSIDADE DE PERNAMBUCO, "
				+ this.DATA_SECAP
				+", "
				+ "E PUBLICADO EM FORMATO DE RESUMO EXPANDIDO NOS ANAIS DO EVENTO, DE ISSN 2675-3731.";

		Map<String, Object> parametros = new HashMap<>();
		parametros.put("Texto", texto);
		parametros.put("Fundo", "certificado_palestrante.png");
		parametros.put("FontePequena", Boolean.TRUE);

		trabalho.setCertificado(this.gerarPDFCertificado(parametros));
		trabalho.setDataUltimaAlteracao(LocalDateTime.now());
	}

	private void gerarCertificado(Monitor monitor) {
		if (monitor == null) {
			throw new RuntimeException("Não existe apresentação de trabalho cadastrada com o identificador informado.");
		}

		if (StringUtils.isBlank(monitor.getNome()) || monitor.getComissao() == null) {
			throw new RuntimeException("Verifique o nome e comissão do monitor:" + monitor.getId());
		}

		String texto = "CERTIFICAMOS QUE <style isBold='true'>" + monitor.getNome().toUpperCase()
				+ "</style> PARTICIPOU COMO MONITOR(A) DA COMISSÃO " + monitor.getComissao().toUpperCase();

		texto += " NA <style isBold='true'>"
				+ this.EDICAO_SECAP
				+ " SEMANA CIENTÍFICA DO AGRESTE PERNAMBUCANO</style> – "
				+ "\""
				+ this.TEMA_SECAP
				+ "\", "
				+ "PROMOVIDA PELA UNIVERSIDADE DE PERNAMBUCO, "
				+ this.DATA_SECAP
				+", "
				+ "COM CARGA HORÁRIA DE <style isBold='true'>" + monitor.getCargaHoraria() + "</style> HORAS.";

		Map<String, Object> parametros = new HashMap<>();
		parametros.put("Texto", texto);
		parametros.put("Fundo", "certificado_monitor.png");
		parametros.put("FontePequena", Boolean.TRUE);

		monitor.setCertificado(this.gerarPDFCertificado(parametros));
		monitor.setDataUltimaAlteracao(LocalDateTime.now());
	}

	@Transactional
	public byte[] buscarCertificado(Long id, TipoCertificadoEnum tipo) {
		log.info("iniciando a busca de certificados de: " + tipo.name() + " com id:" + id);
		byte[] certificado = null;

		try {

			switch (tipo) {
			case OUVINTE:
				certificado = this.buscarCertificadoOuvinte(id);
				break;
			case ATIVIDADE:
				certificado = this.buscarCertificadoAtividade(id);
				break;
			case APRESENTADOR:
				certificado = this.buscarCertificadoTrabalho(id);
				break;
			case MONITOR:
				certificado = this.buscarCertificadoMonitor(id);
				break;
			default:
				throw new RuntimeException("Informe o tipo de certificado a ser buscado");
			}

			log.info("Busca de certificado de: " + tipo.name() + "concluída para o id:" + id);
		} catch (Exception e) {
			log.error("Ocorreu um erro ao buscar certificados de " + tipo.name() + " para o id:" + id, e);

			throw new RuntimeException("Ocorreu um erro ao buscar certificados de " + tipo.name() + " para o id:" + id,
					e);
		}

		return certificado;
	}

	private byte[] buscarCertificadoOuvinte(Long id) {
		Optional<Ouvinte> ouvinte = this.ouvinteDAO.findById(id);

		if (!ouvinte.isPresent()) {
			throw new RuntimeException("Não existe ouvinte cadastrado com o identificador informado.");
		}

		if (ouvinte.get().getCertificado() == null) {
			this.gerarCertificado(ouvinte.get());
		}

		return ouvinte.get().getCertificado();
	}

	private byte[] buscarCertificadoAtividade(Long id) {
		Optional<Atividade> atividades = this.atividadeDAO.findById(id);

		if (!atividades.isPresent()) {
			throw new RuntimeException("Não existe atividade cadastrada com o identificador informado.");
		}

		if (atividades.get().getCertificado() == null) {
			this.gerarCertificado(atividades.get());
		}

		return atividades.get().getCertificado();
	}

	private byte[] buscarCertificadoTrabalho(Long id) {
		Optional<Trabalho> trabalhos = this.trabalhoDAO.findById(id);

		if (!trabalhos.isPresent()) {
			throw new RuntimeException("Não existe trabalho cadastrado com o identificador informado.");
		}

		if (trabalhos.get().getCertificado() == null) {
			this.gerarCertificado(trabalhos.get());
		}

		return trabalhos.get().getCertificado();
	}

	private byte[] buscarCertificadoMonitor(Long id) {
		Optional<Monitor> monitores = this.monitorDAO.findById(id);

		if (!monitores.isPresent()) {
			throw new RuntimeException("Não existe monitor cadastrado com o identificador informado.");
		}

		if (monitores.get().getCertificado() == null) {
			this.gerarCertificado(monitores.get());
		}

		return monitores.get().getCertificado();
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

	private void exportarPDFCertificado(Ouvinte ouvinte) {

		try {

			FileUtils.writeByteArrayToFile(
					new File("certificados/ouvintes/" + ouvinte.getNomeTratado() + "_" + ouvinte.getId() + ".pdf"),
					ouvinte.getCertificado());

			ouvinte.setExportado(true);

			log.info("certificado ouvinte " + ouvinte.getNomeTratado() + " exportado");

		} catch (Exception e) {

			log.error("ocorreu um erro ao exportar o certificado do ouvinte(" + ouvinte.getId() + "):"
					+ ouvinte.getNome(), e);

			ouvinte.setExportado(false);
		}

		ouvinte.setDataUltimaAlteracao(LocalDateTime.now());
	}

	private void exportarPDFCertificado(Monitor monitor) {

		try {

			FileUtils.writeByteArrayToFile(
					new File("certificados/monitores/" + monitor.getNomeTratado() + "_" + monitor.getId() + ".pdf"),
					monitor.getCertificado());

			monitor.setExportado(true);

			log.info("certificado ouvinte " + monitor.getNomeTratado() + " exportado");

		} catch (Exception e) {

			log.error("ocorreu um erro ao exportar o certificado do ouvinte(" + monitor.getId() + "):"
					+ monitor.getNome(), e);

			monitor.setExportado(false);
		}

		monitor.setDataUltimaAlteracao(LocalDateTime.now());
	}

	private void exportarPDFCertificado(Atividade atividade) {

		try {

			FileUtils.writeByteArrayToFile(
					new File(
							"certificados/atividades/" + atividade.getNomeTratado() + "_" + atividade.getId() + ".pdf"),
					atividade.getCertificado());

			atividade.setExportado(true);

			log.info("certificado atividade " + atividade.getNomeTratado() + " exportado");

		} catch (Exception e) {

			log.error("ocorreu um erro ao exportar o certificado da atividade(" + atividade.getId() + "):"
					+ atividade.getNome(), e);

			atividade.setExportado(false);
		}

		atividade.setDataUltimaAlteracao(LocalDateTime.now());
	}

	private void exportarPDFCertificado(Trabalho trabalho) {

		try {

			FileUtils.writeByteArrayToFile(new File("certificados/trabalhos/" + trabalho.getNomeApresentadorTratado()
					+ "_" + trabalho.getIdentificador() + ".pdf"), trabalho.getCertificado());

			trabalho.setExportado(true);

			log.info("certificado apresentação de trabalho " + trabalho.getNomeApresentadorTratado() + " exportado");

		} catch (Exception e) {

			log.error("ocorreu um erro ao exportar o certificado a apresentação(" + trabalho.getId() + "):"
					+ trabalho.getNomeApresentadorTratado(), e);

			trabalho.setExportado(false);
		}

		trabalho.setDataUltimaAlteracao(LocalDateTime.now());
	}

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

	private String getCorpoEmail(String nome, String participacao) {
		Template template = engine.getTemplate("email.vm", "UTF-8");

		VelocityContext context = new VelocityContext();
		context.put("nome", nome);
		context.put("participacao", participacao);

		StringWriter writer = new StringWriter();
		template.merge(context, writer);

		return writer.toString();
	}

}
