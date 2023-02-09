package br.upe.secap.certificados.arquivo;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MonitorService {

	@Autowired
	private IMonitorDAO dao;

	public List<Monitor> listar(Long idArquivo) {
		if (idArquivo == null) {
			throw new RuntimeException("Informe o identificador do m para listar os monitores");
		}

		return this.dao.findAllByArquivoIdOrderByNome(idArquivo);
	}

	public List<Monitor> listar() {
		return this.dao.findAllByOrderByNome();
	}

	public Monitor procurar(Long idMonitor) {
		if (idMonitor == null) {
			throw new RuntimeException("Informe o identificador do monitor");
		}

		Optional<Monitor> monitor = this.dao.findById(idMonitor);

		if (!monitor.isPresent()) {
			throw new RuntimeException("Não existe monitor com identificador:" + idMonitor);
		}

		return monitor.get();
	}

	public Monitor procurar(String email) {
		if (email == null) {
			throw new RuntimeException("Informe o email do monitor");
		}

		Monitor monitor = this.dao.findByEmail(email);

		if (monitor == null) {
			throw new RuntimeException("Não existe monitor com o email:" + email);
		}

		return monitor;
	}
}
