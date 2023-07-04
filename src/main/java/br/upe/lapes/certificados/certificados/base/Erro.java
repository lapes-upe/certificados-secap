package br.upe.lapes.certificados.certificados.base;

import java.util.List;

import org.joda.time.LocalDateTime;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Erro {

	private HttpStatus status;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
	private LocalDateTime timestamp;

	private String message;
	private String debugMessage;

	private List<DetalheErro> subErrors;

}
