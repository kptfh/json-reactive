package reactivejson;


import com.fasterxml.jackson.core.async_.JsonFactory;
import com.fasterxml.jackson.databind.ObjectReader;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ReactorObjectReader {

	private final JsonFactory jsonFactory;

	public ReactorObjectReader(JsonFactory jsonFactory) {
		this.jsonFactory = jsonFactory;
	}

	public <T> Flux<T> readElements(Publisher<ByteBuffer> input, ObjectReader objectReader) {
		try {
			NonBlockingObjectReader nonBlockingObjectReader = new NonBlockingObjectReader(
					jsonFactory, true, objectReader);

			return readImpl(input, nonBlockingObjectReader);
		}
		catch (IOException ex) {
			return Flux.error(ex);
		}
	}

	public <T> Mono<T> read(Publisher<ByteBuffer> input, ObjectReader objectReader) {
		try {
			NonBlockingObjectReader nonBlockingObjectReader = new NonBlockingObjectReader(
					jsonFactory, false, objectReader);

			return this.<T>readImpl(input, nonBlockingObjectReader).singleOrEmpty();
		}
		catch (IOException ex) {
			return Mono.error(ex);
		}
	}

	private <T> Flux<T> readImpl(Publisher<ByteBuffer> input, NonBlockingObjectReader reader) {
		return Flux.from(input).flatMap(
				byteBuffer -> {
					try {
						return Flux.fromIterable(reader.readObjects(byteBuffer));
					} catch (IOException e) {
						return Flux.error(e);
					}
				},
				Flux::error,
				() -> {
					try {
						return Flux.fromIterable(reader.endOfInput());
					} catch (IOException e) {
						return Flux.error(e);
					}
				});
	}
}
