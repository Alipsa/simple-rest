# simple-rest
Simple, modular Rest library for java 11+

When creating modular (jigsaw) applications, none of the existing rest libraries for java works well.
Simple-rest is a modular (as in jigsaw), straight forward library for rest clients.

The API is build around the se.alipsa.simplerest.RestClient and the se.alipsa.simplerest.Response classes where the
RestClient handles the request and the Response class (for the most part) handles the response. 

Add the following dependency to your maven pom.xml (or equivalent for your build system)
```xml
<dependency>
    <groupId>se.alipsa</groupId>
    <artifactId>simple-rest</artifactId>
    <version>1.0</version>
</dependency>
```

# Examples

## Google API's example
```groovy
```

## JSON web token example
```groovy
```

## Basic authentication example
```groovy
```