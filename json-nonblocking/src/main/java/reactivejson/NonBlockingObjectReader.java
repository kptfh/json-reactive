package reactivejson;

import com.fasterxml.jackson.core.async_.JsonFactory;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NonBlockingObjectReader {

	private final Tokenizer tokenizer;
	private final ObjectReader reader;

	public NonBlockingObjectReader(
			JsonFactory jsonFactory, boolean tokenizeArrayElements,
			ObjectReader reader) throws IOException {

		this.tokenizer = new Tokenizer(jsonFactory, tokenizeArrayElements);
		this.reader = reader;
	}

	public <T> List<T> readObjects(ByteBuffer byteBuffer) throws IOException {
		List<TokenBuffer> tokenBuffers = tokenizer.tokenize(byteBuffer);
		List<T> objects = new ArrayList<>(tokenBuffers.size());
		for(TokenBuffer tokenBuffer : tokenBuffers){
			objects.add(reader.readValue(tokenBuffer.asParser(reader)));
		}
		return objects;
	}

	public <T> List<T> endOfInput() throws IOException {
		List<TokenBuffer> tokenBuffers = tokenizer.endOfInput();
		if(tokenBuffers.isEmpty()){
			return Collections.emptyList();
		}
		List<T> objects = new ArrayList<>(tokenBuffers.size());
		for(TokenBuffer tokenBuffer : tokenBuffers){
			objects.add(reader.readValue(tokenBuffer.asParser(reader)));
		}
		return objects;
	}

}
