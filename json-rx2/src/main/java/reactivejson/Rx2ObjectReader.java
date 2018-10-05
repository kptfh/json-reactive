package reactivejson;


import com.fasterxml.jackson.core.async_.JsonFactory;
import com.fasterxml.jackson.databind.ObjectReader;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.reactivestreams.Publisher;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Rx2ObjectReader {

	private final JsonFactory jsonFactory;

	public Rx2ObjectReader(JsonFactory jsonFactory) {
		this.jsonFactory = jsonFactory;
	}

	public <T> Flowable<T> readElements(Publisher<ByteBuffer> input, ObjectReader objectReader) {
		try {
			NonBlockingObjectReader nonBlockingObjectReader = new NonBlockingObjectReader(
					jsonFactory, true, objectReader);

			return readImpl(input, nonBlockingObjectReader);
		}
		catch (IOException ex) {
			return Flowable.error(ex);
		}
	}

	public <T> Single<T> read(Publisher<ByteBuffer> input, ObjectReader objectReader) {
		try {
			NonBlockingObjectReader nonBlockingObjectReader = new NonBlockingObjectReader(
					jsonFactory, false, objectReader);

			return this.<T>readImpl(input, nonBlockingObjectReader).firstOrError();
		}
		catch (IOException ex) {
			return Single.error(ex);
		}
	}

	private <T> Flowable<T> readImpl(Publisher<ByteBuffer> input, NonBlockingObjectReader reader) {
		return Flowable.fromPublisher(input).flatMap(
				byteBuffer -> {
					try {
						return Flowable.fromIterable(reader.readObjects(byteBuffer));
					} catch (IOException e) {
						return Flowable.error(e);
					}
				},
				Flowable::error,
				() -> {
					try {
						return Flowable.fromIterable(reader.endOfInput());
					} catch (IOException e) {
						return Flowable.error(e);
					}
				});
	}
}
