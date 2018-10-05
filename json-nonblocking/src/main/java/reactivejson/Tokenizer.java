/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package reactivejson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.async.ByteBufferFeeder;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows to read nonblocking a JSON stream of arbitrary size, byte array
 * chunks as TokenBuffers where each token buffer is a
 * well-formed JSON object.
 *
 * Copied from Spring's Jackson2Tokenizer
 *
 */
class Tokenizer {

	private final JsonParser parser;

	private final boolean tokenizeArrayElements;

	private TokenBuffer tokenBuffer;

	private int objectDepth;

	private int arrayDepth;

	private final ByteBufferFeeder inputFeeder;

	public Tokenizer(JsonFactory jsonFactory, boolean tokenizeArrayElements) throws IOException {
		this.parser = jsonFactory.createNonBlockingByteArrayParser();
		this.tokenizeArrayElements = tokenizeArrayElements;
		this.tokenBuffer = new TokenBuffer(parser);
		this.inputFeeder = (ByteBufferFeeder) this.parser.getNonBlockingInputFeeder();
	}

	public List<TokenBuffer> tokenize(ByteBuffer byteBuffer) throws IOException {
		inputFeeder.feedInput(byteBuffer);
		return parse();
	}

	public List<TokenBuffer> endOfInput() throws IOException {
		inputFeeder.endOfInput();
		return parse();
	}

	private List<TokenBuffer> parse() throws IOException {
		List<TokenBuffer> result = new ArrayList<>();

		while (true) {
			JsonToken token = this.parser.nextToken();
			// SPR-16151: Smile data format uses null to separate documents
			if ((token == JsonToken.NOT_AVAILABLE) ||
					(token == null && (token = this.parser.nextToken()) == null)) {
				break;
			}
			updateDepth(token);

			if (!this.tokenizeArrayElements) {
				processTokenNormal(token, result);
			}
			else {
				processTokenArray(token, result);
			}
		}
		return result;
	}

	private void updateDepth(JsonToken token) {
		switch (token) {
			case START_OBJECT:
				this.objectDepth++;
				break;
			case END_OBJECT:
				this.objectDepth--;
				break;
			case START_ARRAY:
				this.arrayDepth++;
				break;
			case END_ARRAY:
				this.arrayDepth--;
				break;
		}
	}

	private void processTokenNormal(JsonToken token, List<TokenBuffer> result) throws IOException {
		this.tokenBuffer.copyCurrentEvent(this.parser);

		if ((token.isStructEnd() || token.isScalarValue()) &&
				this.objectDepth == 0 && this.arrayDepth == 0) {
			result.add(this.tokenBuffer);
			this.tokenBuffer = new TokenBuffer(this.parser);
		}

	}

	private void processTokenArray(JsonToken token, List<TokenBuffer> result) throws IOException {
		if (!isTopLevelArrayToken(token)) {
			this.tokenBuffer.copyCurrentEvent(this.parser);
		}

		if (this.objectDepth == 0 &&
				(this.arrayDepth == 0 || this.arrayDepth == 1) &&
				(token == JsonToken.END_OBJECT || token.isScalarValue())) {
			result.add(this.tokenBuffer);
			this.tokenBuffer = new TokenBuffer(this.parser);
		}
	}

	private boolean isTopLevelArrayToken(JsonToken token) {
		return this.objectDepth == 0 && ((token == JsonToken.START_ARRAY && this.arrayDepth == 1) ||
				(token == JsonToken.END_ARRAY && this.arrayDepth == 0));
	}

}
