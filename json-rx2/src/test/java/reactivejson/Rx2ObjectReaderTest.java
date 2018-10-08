package reactivejson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.async_.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.junit.Test;
import org.reactivestreams.Publisher;
import reactivejson.domain.TestEntity;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rx2ObjectReaderTest {

	private ObjectMapper objectMapper = new ObjectMapper();
	private Rx2ObjectReader reader = new Rx2ObjectReader(new JsonFactory());

	@Test
	public void shouldReadEntity() throws JsonProcessingException {
		TestEntity testEntity = new TestEntity(7, "testName");
		Publisher<ByteBuffer> byteBuffers = stringBuffer(objectMapper.writeValueAsString(testEntity));

		Single<TestEntity> testEntityRed = reader.read(byteBuffers, objectMapper.readerFor(TestEntity.class));

		testEntityRed.test()
				.assertResult(testEntity);
	}

	@Test
	public void shouldReadElements() throws JsonProcessingException {
		TestEntity[] testEntities = new TestEntity[]{
				new TestEntity(1, "testName1"),
				new TestEntity(3, "testName3"),
				new TestEntity(7, "testName7")};

		Publisher<ByteBuffer> byteBuffers = stringBuffer(objectMapper.writeValueAsString(testEntities));

		Flowable<TestEntity> testEntityRed = reader.readElements(byteBuffers,
				objectMapper.readerFor(TestEntity.class));

		testEntityRed.test()
				.assertResult(testEntities);
	}

	@Test
	public void shouldReadElementsAsArray() throws JsonProcessingException {
		TestEntity[] testEntities = new TestEntity[]{
				new TestEntity(1, "testName1"),
				new TestEntity(3, "testName3"),
				new TestEntity(7, "testName7")};
		Publisher<ByteBuffer> byteBuffers = stringBuffer(objectMapper.writeValueAsString(testEntities));

		Single<TestEntity[]> testEntitiesRed = reader.read(byteBuffers, objectMapper.readerFor(TestEntity[].class));

		testEntitiesRed.test()
				.assertSubscribed()
				.assertValue(values -> Arrays.equals(values, testEntities))
				.assertNoErrors()
				.assertComplete();
	}

	private Publisher<ByteBuffer> stringBuffer(String value) {
		return Flowable.fromIterable(divideArray(value.getBytes(StandardCharsets.UTF_8), 5))
				.map(ByteBuffer::wrap);
	}

	private static List<byte[]> divideArray(byte[] source, int chunksize) {

		List<byte[]> result = new ArrayList<>();
		int start = 0;
		while (start < source.length) {
			int end = Math.min(source.length, start + chunksize);
			result.add(Arrays.copyOfRange(source, start, end));
			start += chunksize;
		}

		return result;
	}

}
