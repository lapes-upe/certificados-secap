package br.upe.lapes.certificados.certificados.base;

import org.springframework.core.convert.converter.Converter;
import br.upe.lapes.certificados.certificados.negocio.certificado.TipoCertificadoEnum;

public class TipoCertificadorEnumConverter implements Converter<String, TipoCertificadoEnum>{

	@Override
	public TipoCertificadoEnum convert(String source) {
		return TipoCertificadoEnum.valueOf(source.toUpperCase());
	}

}
