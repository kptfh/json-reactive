# json-reactive

Use Json Reactive to make POJO binding reactively 

## Overview

Implementation of reactive json object reader over Jackson non blocking json parser.

## Modules
  
  **_json-nonblocking_** : non blocking implementation that can be wrapped with any reactive approach
  
  **_json-reactor_** : io.projectreactor implementation 
  
  **_json-rx2_** : rxJava2 implementation
  
## Usage io.projectreactor 

```java
ReactorObjectReader reader = new ReactorObjectReader(new JsonFactory());

Flux<TestEntity> testEntityRed = reader.readElements(byteBuffers, objectMapper.readerFor(TestEntity.class));
```

## Usage rxJava2 

```java
Rx2ObjectReader reader = new Rx2ObjectReader(new JsonFactory());

Flowable<TestEntity> testEntityRed = reader.readElements(byteBuffers, objectMapper.readerFor(TestEntity.class));
```
