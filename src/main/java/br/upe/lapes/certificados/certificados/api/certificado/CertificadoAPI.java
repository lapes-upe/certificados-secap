package br.upe.lapes.certificados.certificados.api.certificado;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.upe.lapes.certificados.certificados.negocio.certificado.CertificadoService;
import br.upe.lapes.certificados.certificados.negocio.certificado.beans.CertificadoVO;

/**
 * API responsável pela geração e acesso aos certificados.
 * 
 * @author helainelins
 */
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/certificado")
@RestController
public class CertificadoAPI {

	@Autowired
	private CertificadoService certificadoService;

	/**
	 * Realiza o download do certificado de uma pessoa certificada através do seu identificador.
	 * 
	 * @param id O identificador da pessoa certificada.
	 * @return O certificado da pessoa certificada.
	 */
	@GetMapping("/pessoa/{id}")
	public ResponseEntity<Resource> buscarCertificadoPessoaCertificada(@PathVariable Long id) {
		
		CertificadoVO certificado = this.certificadoService.buscarCertificado(id);

		ByteArrayResource resource = new ByteArrayResource(certificado.getCertificado());

		HttpHeaders headers = new HttpHeaders();
		headers.add("Pragma", "public");
		headers.add("Cache-Control", "max-age=0");
		headers.add("Pragma", "public");
		headers.add("Content-Disposition", "attachment;filename=" + certificado.getNomeArquivo());

		//FIXME: tem um bug que o nome do arquivo está aparecendo nulo, verificar isso!
		
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.contentLength(resource.contentLength()).contentType(MediaType.APPLICATION_PDF).body(resource);
	}

	/**
	 * Gera os certificaddos de um determinado arquivo.
	 * 
	 * @param idArquivo o identificador do arquivo.
	 * @return Uma mensagem informando que o processo de geração de certificados foi iniciado.
	 */
	@Async
	@SuppressWarnings("rawtypes")
	@GetMapping("/gerar/{idArquivo}")
	public ResponseEntity gerarCertificados(@PathVariable Long idArquivo) {
		
		this.certificadoService.gerarCertificados(idArquivo);
		
		return ResponseEntity.ok().body("Geração dos certificados iniciado");
	}

	/**
	 * Gera a certificação da pessoa certificada no sistema.
	 */
	@SuppressWarnings("rawtypes")
	@GetMapping("/gerar/{idPessoaCertificada}")
	public ResponseEntity gerarCertificacaoPessoaCertificada(@PathVariable Long idPessoaCertificada) {
		
		this.certificadoService.gerarCertificado(idPessoaCertificada);
		
		return ResponseEntity.ok().body("Geração da certificação da pessoa iniciado");
	}

	/**
	 * Exporta assíncronamente todos os certificados gerados pelo sistema para um determinado arquivo.
	 * 
	 * @param idArquivo O identificador do arquivo.
	 * @return uma mensagem informando que o processo de exportação foi inciado.
	 */
	@Async
	@SuppressWarnings("rawtypes")
	@GetMapping("/exportar/{idArquivo}")
	public ResponseEntity exportarCertificados(@PathVariable Long idArquivo) {
		
		this.certificadoService.exportarCertificados(idArquivo);
		
		return ResponseEntity.ok().body("Exportação dos certificados concluida.");
	}

	/**
	 * Exporta todas as certificações já geradas no sistema. Caso seja informado um
	 * identificador o sistema irá exportar apenas a certificação da pessoa
	 * certificada correspondente ao identificador informado.
	 * 
	 * @param idParticipante i identificador da pessoa certificada ou um valor em
	 *                       branco caso deseje que o sistema exporte todas as
	 *                       certificações cadastradas.
	 * @return Uma mensagem informandoq ue todos os certificados foram exportados.
	 */
	@Async
	@SuppressWarnings("rawtypes")
	@GetMapping("/exportar")
	public ResponseEntity exportar(@RequestParam Long idParticipante) {

		if (idParticipante == null || idParticipante == 0) {
			this.certificadoService.exportarCertificados();
		} else {
			this.certificadoService.exportarCertificado(idParticipante);
		}

		return ResponseEntity.ok().body("Exportação dos certificados concluída.");
	}

	/**
	 * Envia uma certificação para o email informado de acordo com a sua
	 * identificação. Caso o email não seja informado a certificação será enviada
	 * para o email cadastrado para a pessoa certificada.
	 * 
	 * @param id    O identificador da certificação
	 * @param email O email para envio da certificação. Pode ser vazio caso deseje
	 *              que a certificação seja enviado para a pessoa certificada.
	 * @return Uma mensagem informando que o email foi disparado
	 */
	@Async
	@SuppressWarnings("rawtypes")
	@GetMapping("/enviar/{id}")
	public ResponseEntity enviar(@PathVariable Long id, @RequestParam("email") String email) {
		
		this.certificadoService.enviarEmailComCertificado(id, email);
		
		return ResponseEntity.ok().body("Envio de email disparado");
	}

	/**
	 * Envia as certificações de um determinado arquivo de lote.
	 * 
	 * @param idArquivo O identificador do lote;
	 * @return uma mensagem informando que o envio foi inciado.
	 */
	@Async
	@SuppressWarnings("rawtypes")
	@GetMapping("/enviar_lote/{idArquivo}")
	public ResponseEntity enviarLote(@PathVariable Long idArquivo) {
		
		this.certificadoService.enviarEmailComCertificados(idArquivo);
		
		return ResponseEntity.ok().body("Envio de emails em lote disparado.");
	}

}
