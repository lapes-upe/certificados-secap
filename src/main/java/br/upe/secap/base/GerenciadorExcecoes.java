package br.upe.secap.base;

import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.LocalDateTime;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GerenciadorExcecoes extends ResponseEntityExceptionHandler {

	@ExceptionHandler(Throwable.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	protected Erro globalExceptionHandler(Exception exception, WebRequest request) {

		log.error("Refinando mensagem de erro padrão ao usuário", exception);

		return Erro.builder().message("Ocorreu um erro ao processar sua solicitação")
				.status(HttpStatus.INTERNAL_SERVER_ERROR).debugMessage(exception.getLocalizedMessage())
				.timestamp(LocalDateTime.now()).build();
	}

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	protected Erro validationException(Exception exception, WebRequest request) {
		log.warn("Refinando mensagem de erro de validação", exception);

		return Erro.builder().message("Os dados fornecidos são inválidos, verifique o preenchimento das informações")
				.status(HttpStatus.BAD_REQUEST).debugMessage(exception.getLocalizedMessage())
				.timestamp(LocalDateTime.now()).build();
	}
}
