package br.upe.secap.certificados;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.upe.secap.certificados.arquivo.ArquivoEnvelope;
import br.upe.secap.certificados.arquivo.ArquivoService;
import br.upe.secap.certificados.arquivo.AtividadeEnvelope;
import br.upe.secap.certificados.arquivo.AtividadeService;
import br.upe.secap.certificados.arquivo.MonitorEnvelope;
import br.upe.secap.certificados.arquivo.MonitorService;
import br.upe.secap.certificados.arquivo.OuvinteEnvelope;
import br.upe.secap.certificados.arquivo.OuvinteService;
import br.upe.secap.certificados.arquivo.TrabalhoEnvelope;
import br.upe.secap.certificados.arquivo.TrabalhoService;
import br.upe.secap.certificados.certificado.CertificadoService;
import br.upe.secap.certificados.certificado.TipoCertificadoEnum;

@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/certificado")
@RestController
public class CertificadoAPI {

	@Autowired
	private ArquivoService arquivoService;

	@Autowired
	private OuvinteService ouvinteService;

	@Autowired
	private AtividadeService atividadeService;

	@Autowired
	private TrabalhoService trabalhoService;

	@Autowired
	private MonitorService monitorService;

	@Autowired
	private CertificadoService certificadoService;

	@PostMapping("/arquivo")
	public ResponseEntity<ArquivoEnvelope> enviarArquivo(@RequestParam("arquivo") MultipartFile arquivo,
			@RequestParam TipoCertificadoEnum tipo) {

		return new ResponseEntity<ArquivoEnvelope>(ArquivoEnvelope.get(arquivoService.salvar(arquivo, tipo)), HttpStatus.OK);
	}

	@DeleteMapping("/arquivo/{idArquivo}")
	public ResponseEntity<String> apagarArquivo(@PathVariable("idArquivo") Long idArquivo) {
		arquivoService.apagar(idArquivo);

		return new ResponseEntity<String>("Arquivo apagado com sucesso", HttpStatus.OK);
	}

	@GetMapping("/arquivos")
	public ResponseEntity<List<ArquivoEnvelope>> listarArquivos() {

		List<ArquivoEnvelope> arquivos = new ArrayList<ArquivoEnvelope>();

		this.arquivoService.listar().stream().map(arquivo -> arquivos.add(ArquivoEnvelope.get(arquivo)))
				.collect(Collectors.toList());

		return arquivos.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
				: new ResponseEntity<List<ArquivoEnvelope>>(arquivos, HttpStatus.OK);

	}

	@GetMapping("/extrair")
	public ResponseEntity<String> extrair(@RequestParam Long id, @RequestParam TipoCertificadoEnum tipo) {
		return new ResponseEntity<String>(
				"[" + this.arquivoService.extrair(id, tipo) + "] extração iniciada com sucesso.", HttpStatus.OK);
	}

	@SuppressWarnings("rawtypes")
	@GetMapping("/listar/{tipo}/{idArquivo}")
	public ResponseEntity<?> listar(@PathVariable TipoCertificadoEnum tipo, @PathVariable Long idArquivo) {
		ResponseEntity retorno = null;

		switch (tipo) {
		case OUVINTE:
			List<OuvinteEnvelope> ouvintes = new ArrayList<OuvinteEnvelope>();

			this.ouvinteService.listar(idArquivo).stream().map(ouvinte -> ouvintes.add(OuvinteEnvelope.get(ouvinte)))
					.collect(Collectors.toList());

			retorno = ouvintes.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
					: new ResponseEntity<List<OuvinteEnvelope>>(ouvintes, HttpStatus.OK);
			break;
		case ATIVIDADE:
			List<AtividadeEnvelope> atividades = new ArrayList<AtividadeEnvelope>();

			this.atividadeService.listar(idArquivo).stream()
					.map(atividade -> atividades.add(AtividadeEnvelope.get(atividade))).collect(Collectors.toList());

			retorno = atividades.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
					: new ResponseEntity<List<AtividadeEnvelope>>(atividades, HttpStatus.OK);
			break;
		case APRESENTADOR:
			List<TrabalhoEnvelope> trabalhos = new ArrayList<TrabalhoEnvelope>();

			this.trabalhoService.listar(idArquivo).stream()
					.map(trabalho -> trabalhos.add(TrabalhoEnvelope.get(trabalho))).collect(Collectors.toList());

			retorno = trabalhos.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
					: new ResponseEntity<List<TrabalhoEnvelope>>(trabalhos, HttpStatus.OK);
			break;
		case MONITOR:
			List<MonitorEnvelope> monitores = new ArrayList<MonitorEnvelope>();

			this.monitorService.listar(idArquivo).stream().map(monitor -> monitores.add(MonitorEnvelope.get(monitor)))
					.collect(Collectors.toList());

			retorno = monitores.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
					: new ResponseEntity<List<MonitorEnvelope>>(monitores, HttpStatus.OK);
			break;

		default:
			retorno = new ResponseEntity<>("Informe o tipo do certificado", HttpStatus.BAD_REQUEST);
			break;
		}

		return retorno;
	}

	@SuppressWarnings("rawtypes")
	@GetMapping("/listar/{tipo}")
	public ResponseEntity<?> listar(@PathVariable TipoCertificadoEnum tipo) {
		ResponseEntity retorno = null;

		switch (tipo) {
		case OUVINTE:
			List<OuvinteEnvelope> ouvintes = new ArrayList<OuvinteEnvelope>();

			this.ouvinteService.listar().stream().map(ouvinte -> ouvintes.add(OuvinteEnvelope.get(ouvinte)))
					.collect(Collectors.toList());

			retorno = ouvintes.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
					: new ResponseEntity<List<OuvinteEnvelope>>(ouvintes, HttpStatus.OK);
			break;
		case ATIVIDADE:
			List<AtividadeEnvelope> atividades = new ArrayList<AtividadeEnvelope>();

			this.atividadeService.listar().stream().map(atividade -> atividades.add(AtividadeEnvelope.get(atividade)))
					.collect(Collectors.toList());

			retorno = atividades.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
					: new ResponseEntity<List<AtividadeEnvelope>>(atividades, HttpStatus.OK);
			break;
		case APRESENTADOR:
			List<TrabalhoEnvelope> trabalhos = new ArrayList<TrabalhoEnvelope>();

			this.trabalhoService.listar().stream().map(trabalho -> trabalhos.add(TrabalhoEnvelope.get(trabalho)))
					.collect(Collectors.toList());

			retorno = trabalhos.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
					: new ResponseEntity<List<TrabalhoEnvelope>>(trabalhos, HttpStatus.OK);
			break;
		case MONITOR:
			List<MonitorEnvelope> monitores = new ArrayList<MonitorEnvelope>();

			this.monitorService.listar().stream().map(monitor -> monitores.add(MonitorEnvelope.get(monitor)))
					.collect(Collectors.toList());

			retorno = monitores.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
					: new ResponseEntity<List<MonitorEnvelope>>(monitores, HttpStatus.OK);
			break;

		default:
			retorno = new ResponseEntity<>("Informe o tipo do certificado", HttpStatus.BAD_REQUEST);
			break;
		}

		return retorno;
	}

	@GetMapping("/{tipo}/{id}")
	public ResponseEntity<Resource> buscar(@PathVariable Long id, @PathVariable TipoCertificadoEnum tipo) {
		byte[] certificado = this.certificadoService.buscarCertificado(id, tipo);

		ByteArrayResource resource = new ByteArrayResource(certificado);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Pragma", "public");
		headers.add("Cache-Control", "max-age=0");
		headers.add("Pragma", "public");
		headers.add("Content-Disposition", "attachment;filename=certicado-secap-" + tipo + ".pdf");

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.contentLength(resource.contentLength()).contentType(MediaType.APPLICATION_PDF).body(resource);
	}

	@Async
	@SuppressWarnings("rawtypes")
	@GetMapping("/gerar/{tipo}/{idArquivo}")
	public ResponseEntity gerar(@PathVariable TipoCertificadoEnum tipo, @PathVariable Long idArquivo) {
		this.certificadoService.gerarCertificados(idArquivo, tipo);
		return ResponseEntity.ok().body("Processamento dos certificados iniciado");
	}

	@Async
	@SuppressWarnings("rawtypes")
	@GetMapping("/exportar/{tipo}/{idArquivo}")
	public ResponseEntity exportar(@PathVariable Long idArquivo, @PathVariable TipoCertificadoEnum tipo) {
		this.certificadoService.exportarCertificados(idArquivo, tipo);
		return ResponseEntity.ok().body("Exportação dos certificados iniciada.");
	}

	@Async
	@SuppressWarnings("rawtypes")
	@GetMapping("/enviar/{tipo}/{id}")
	public ResponseEntity enviar(@PathVariable Long id, @PathVariable TipoCertificadoEnum tipo,
			@RequestParam("email") String email) {
		this.certificadoService.enviarEmailComCertificado(id, email, tipo);
		return ResponseEntity.ok().body("Envio de email disparado");
	}

	@Async
	@SuppressWarnings("rawtypes")
	@GetMapping("/enviar_lote/{tipo}/{idArquivo}")
	public ResponseEntity enviarLote(@PathVariable Long idArquivo, @PathVariable TipoCertificadoEnum tipo) {
		this.certificadoService.enviarEmailComCertificados(idArquivo, tipo);
		return ResponseEntity.ok().body("Envio de emails em lote disparado.");
	}

}
