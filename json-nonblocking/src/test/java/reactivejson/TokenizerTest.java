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

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.async_.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Copied from Spring's Jackson2TokenizerTest
 */
public class TokenizerTest {

	private ObjectReader objectReader;

	private JsonFactory jsonFactory;

	@Before
	public void createReader() {
		this.jsonFactory = new JsonFactory();
		this.objectReader = new ObjectMapper(this.jsonFactory).reader();
	}

	@Test
	public void doNotTokenizeArrayElements() {
		testTokenize(
				singletonList("{\"foo\": \"foofoo\", \"bar\": \"barbar\"}"),
				singletonList("{\"foo\": \"foofoo\", \"bar\": \"barbar\"}"), false);

		testTokenize(
				asList("{\"foo\": \"foofoo\"",
						", \"bar\": \"barbar\"}"),
				singletonList("{\"foo\":\"foofoo\",\"bar\":\"barbar\"}"), false);

		testTokenize(
				singletonList("[" +
						"{\"foo\": \"foofoo\", \"bar\": \"barbar\"}," +
						"{\"foo\": \"foofoofoo\", \"bar\": \"barbarbar\"}]"),
				singletonList("[" +
						"{\"foo\": \"foofoo\", \"bar\": \"barbar\"}," +
						"{\"foo\": \"foofoofoo\", \"bar\": \"barbarbar\"}]"), false);

		testTokenize(
				singletonList("[{\"foo\": \"bar\"},{\"foo\": \"baz\"}]"),
				singletonList("[{\"foo\": \"bar\"},{\"foo\": \"baz\"}]"), false);

		testTokenize(
				asList("[" +
						"{\"foo\": \"foofoo\", \"bar\"", ": \"barbar\"}," +
						"{\"foo\": \"foofoofoo\", \"bar\": \"barbarbar\"}]"),
				singletonList("[" +
						"{\"foo\": \"foofoo\", \"bar\": \"barbar\"}," +
						"{\"foo\": \"foofoofoo\", \"bar\": \"barbarbar\"}]"), false);

		testTokenize(
				asList("[",
						"{\"id\":1,\"name\":\"Robert\"}", ",",
						"{\"id\":2,\"name\":\"Raide\"}", ",",
						"{\"id\":3,\"name\":\"Ford\"}", "]"),
				singletonList("[" +
						"{\"id\":1,\"name\":\"Robert\"}," +
						"{\"id\":2,\"name\":\"Raide\"}," +
						"{\"id\":3,\"name\":\"Ford\"}]"), false);

		// SPR-16166: top-level JSON values
		testTokenize(asList("\"foo", "bar\""),singletonList("\"foobar\""), false);

		testTokenize(asList("12", "34"),singletonList("1234"), false);

		testTokenize(asList("12.", "34"),singletonList("12.34"), false);

		// note that we do not test for null, true, or false, which are also valid top-level values,
		// but are unsupported by JSONassert
	}

	@Test
	public void tokenizeArrayElements() {
		testTokenize(
				singletonList("{\"foo\": \"foofoo\", \"bar\": \"barbar\"}"),
				singletonList("{\"foo\": \"foofoo\", \"bar\": \"barbar\"}"), true);

		testTokenize(
				asList("{\"foo\": \"foofoo\"", ", \"bar\": \"barbar\"}"),
				singletonList("{\"foo\":\"foofoo\",\"bar\":\"barbar\"}"), true);

		testTokenize(
				singletonList("[" +
						"{\"foo\": \"foofoo\", \"bar\": \"barbar\"}," +
						"{\"foo\": \"foofoofoo\", \"bar\": \"barbarbar\"}]"),
				asList(
						"{\"foo\": \"foofoo\", \"bar\": \"barbar\"}",
						"{\"foo\": \"foofoofoo\", \"bar\": \"barbarbar\"}"), true);

		testTokenize(
				singletonList("[{\"foo\": \"bar\"},{\"foo\": \"baz\"}]"),
				asList("{\"foo\": \"bar\"}", "{\"foo\": \"baz\"}"), true);

		// SPR-15803: nested array
		testTokenize(
				singletonList("[" +
						"{\"id\":\"0\",\"start\":[-999999999,1,1],\"end\":[999999999,12,31]}," +
						"{\"id\":\"1\",\"start\":[-999999999,1,1],\"end\":[999999999,12,31]}," +
						"{\"id\":\"2\",\"start\":[-999999999,1,1],\"end\":[999999999,12,31]}" +
						"]"),
				asList(
						"{\"id\":\"0\",\"start\":[-999999999,1,1],\"end\":[999999999,12,31]}",
						"{\"id\":\"1\",\"start\":[-999999999,1,1],\"end\":[999999999,12,31]}",
						"{\"id\":\"2\",\"start\":[-999999999,1,1],\"end\":[999999999,12,31]}"), true);

		// SPR-15803: nested array, no top-level array
		testTokenize(
				singletonList("{\"speakerIds\":[\"tastapod\"],\"language\":\"ENGLISH\"}"),
				singletonList("{\"speakerIds\":[\"tastapod\"],\"language\":\"ENGLISH\"}"), true);

		testTokenize(
				asList("[" +
						"{\"foo\": \"foofoo\", \"bar\"", ": \"barbar\"}," +
						"{\"foo\": \"foofoofoo\", \"bar\": \"barbarbar\"}]"),
				asList(
						"{\"foo\": \"foofoo\", \"bar\": \"barbar\"}",
						"{\"foo\": \"foofoofoo\", \"bar\": \"barbarbar\"}"), true);

		testTokenize(
				asList("[",
						"{\"id\":1,\"name\":\"Robert\"}",
						",",
						"{\"id\":2,\"name\":\"Raide\"}",
						",",
						"{\"id\":3,\"name\":\"Ford\"}",
						"]"),
				asList("{\"id\":1,\"name\":\"Robert\"}",
						"{\"id\":2,\"name\":\"Raide\"}",
						"{\"id\":3,\"name\":\"Ford\"}"), true);

		// SPR-16166: top-level JSON values
		testTokenize(asList("\"foo", "bar\""),singletonList("\"foobar\""), true);

		testTokenize(asList("12", "34"),singletonList("1234"), true);

		testTokenize(asList("12.", "34"),singletonList("12.34"), true);

		// SPR-16407
		testTokenize(asList("[1", ",2,", "3]"), asList("1", "2", "3"), true);
	}

//	@Test(expected = DecodingException.class) // SPR-16521
//	public void jsonEOFExceptionIsWrappedAsDecodingError() {
//		Flux<DataBuffer> source = Flux.just(stringBuffer("{\"status\": \"noClosingQuote}"));
//		Flux<TokenBuffer> tokens = Jackson2Tokenizer.tokenize(source, this.jsonFactory, false);
//		tokens.blockLast();
//	}


	private void testTokenize(List<String> source, List<String> expected, boolean tokenizeArrayElements) {

		try {
			List<TreeNode> expectedTrees = expected.stream()
					.map(s -> {
						try {
							return objectReader.readTree(s);
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					}).collect(Collectors.toList());

			Tokenizer tokenizer = new Tokenizer(this.jsonFactory, tokenizeArrayElements);

			List<TokenBuffer> tokenBuffers = new ArrayList<>(source.size());

			for(String s : source){
				tokenBuffers.addAll(tokenizer.tokenize(stringBuffer(s)));
			}

			tokenBuffers.addAll(tokenizer.endOfInput());

			List<TreeNode> actual = new ArrayList<>(source.size());
			tokenBuffers.forEach(tokenBuffer -> {
				try {
					actual.add(this.objectReader.readTree(tokenBuffer.asParser()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			assertThat(actual).containsExactlyElementsOf(expectedTrees);

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private ByteBuffer stringBuffer(String value) {
		return ByteBuffer.wrap(value.getBytes(StandardCharsets.UTF_8));
	}

}
