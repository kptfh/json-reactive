package reactivejson.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TestEntity {

	private final int id;
	private final String name;

	@JsonCreator
	public TestEntity(@JsonProperty("id") int id, @JsonProperty("name")String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj){
		return obj instanceof TestEntity
				&& ((TestEntity) obj).id == id
				&& ((TestEntity) obj).name.equals(name);
	}
}
