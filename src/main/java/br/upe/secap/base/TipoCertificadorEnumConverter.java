package br.upe.secap.base;

import org.springframework.core.convert.converter.Converter;

import br.upe.secap.certificados.certificado.TipoCertificadoEnum;

public class TipoCertificadorEnumConverter implements Converter<String, TipoCertificadoEnum>{

	@Override
	public TipoCertificadoEnum convert(String source) {
		return TipoCertificadoEnum.valueOf(source.toUpperCase());
	}

}
