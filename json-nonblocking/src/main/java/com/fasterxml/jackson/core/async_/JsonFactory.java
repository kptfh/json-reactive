package com.fasterxml.jackson.core.async_;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;

/**
 * Temporary copy while not released fixed version
 */
public class JsonFactory extends com.fasterxml.jackson.core.JsonFactory {

	@Override
	public JsonParser createNonBlockingByteArrayParser() {
		IOContext ctxt = _createNonBlockingContext(null);
		ByteQuadsCanonicalizer can = _byteSymbolCanonicalizer.makeChild(_factoryFeatures);
		return new NonBlockingJsonParser(ctxt, _parserFeatures, can);
	}

}
