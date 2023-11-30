package br.upe.lapes.certificados.certificados.api.arquivo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.upe.lapes.certificados.certificados.negocio.arquivo.ArquivoService;
import br.upe.lapes.certificados.certificados.negocio.certificado.TipoCertificadoEnum;

/**
 * API responsável pela geração e acesso aos certificados.
 * 
 * @author helainelins
 */
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/arquivo")
@RestController
public class ArquivoAPI {

	@Autowired
	private ArquivoService arquivoService;

	/**
	 * Realiza o upload de arquivo que contém todos os dados acerca das
	 * certificaćões a serem emitidas. Este arquivo é salvo na base de dados da
	 * aplicaćào para posteriormente ser extraído e tratado pela aplicaćão.
	 * 
	 * @param arquivo o arquivo contendo os dados para emissão dos certificados
	 * @param tipo    informa o tipo de certificacao a ser emitida (OUVINTE,
	 *                APRESENTADOR, MONITOR, DOCENTE_COLABORADOR, MINISTRANTE,
	 *                AVALIADOR)
	 * @return {@link HttpStatus.<code>OK</code>} caso o arquivo seja incluído com
	 *         sucesso.
	 */
	@PostMapping("/arquivo")
	public ResponseEntity<ArquivoEnvelope> enviarArquivo(@RequestParam("arquivo") MultipartFile arquivo,
			@RequestParam TipoCertificadoEnum tipo) {

		return new ResponseEntity<ArquivoEnvelope>(ArquivoEnvelope.get(this.arquivoService.salvar(arquivo, tipo)),
				HttpStatus.OK);
	}

	/**
	 * Remove o arquivo da base de dados do sistema, desde que não tenham sido
	 * emitidos certificados para ele.
	 * 
	 * @param idArquivo O identificador do arquivo a ser removido.
	 * @return {@link HttpStatus.<code>OK</code>} caso o arquivo seja apagado com
	 *         sucesso.
	 */
	@DeleteMapping("/arquivo/{idArquivo}")
	public ResponseEntity<String> apagarArquivo(@PathVariable("idArquivo") Long idArquivo) {

		this.arquivoService.apagar(idArquivo);

		return new ResponseEntity<String>("Arquivo apagado com sucesso", HttpStatus.OK);
	}

	/**
	 * Lista os arquivos cadastrados ordenados pelo nome.
	 * 
	 * @return um {@link List} de {@link ArquivoEnvelope} com os arquivos ordenados
	 *         e o {@link HttpStatus.<code>OK</code>} ou um
	 *         {@link HttpStatus.<code>NO_CONTENT</code>} caso não exista arquivos
	 *         cadastrados
	 */
	@GetMapping("/arquivos")
	public ResponseEntity<List<ArquivoEnvelope>> listarArquivos() {

		List<ArquivoEnvelope> arquivos = new ArrayList<ArquivoEnvelope>();

		this.arquivoService.listar().stream().map(arquivo -> arquivos.add(ArquivoEnvelope.get(arquivo)))
				.collect(Collectors.toList());

		return arquivos.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
				: new ResponseEntity<List<ArquivoEnvelope>>(arquivos, HttpStatus.OK);

	}

	/**
	 * Extrai do arquivo armazenado na base de dados todas informaćões que serão
	 * necessárias para posteriormente emitir os certificados.
	 * 
	 * @param id O identificador do arquivo armazenado
	 * @return {@link HttpStatus.<code>OK</code>} caso as informaćòes do arquivo
	 *         tenham sido extraídas com sucesso.
	 */
	@GetMapping("/extrair/{id}")
	public ResponseEntity<String> extrair(@PathVariable Long id) {
		return new ResponseEntity<String>("[" + this.arquivoService.extrair(id) + "] extração iniciada com sucesso.",
				HttpStatus.OK);
	}

}
