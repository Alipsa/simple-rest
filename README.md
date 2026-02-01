# simple-rest
[![Maven Central](https://maven-badges.sml.io/maven-central/se.alipsa/simple-rest/badge.svg)](https://maven-badges.sml.io/maven-central/se.alipsa/simple-rest)
[![javadoc](https://javadoc.io/badge2/se.alipsa/simple-rest/javadoc.svg)](https://javadoc.io/doc/se.alipsa/simple-rest)

Simple, modular Rest library for java 17+

When creating modular (jigsaw) applications, none of the existing rest libraries for java works well.
Simple-rest is a modular (as in jigsaw), straight forward library for rest clients (the module name is `se.alipsa.simplerest`).

The API is build around the se.alipsa.simplerest.RestClient and the se.alipsa.simplerest.Response classes where the
RestClient handles the request and the Response class (for the most part) handles the response.

Add the following dependency to your maven pom.xml (or equivalent for your build system)
```xml
<dependency>
    <groupId>se.alipsa</groupId>
    <artifactId>simple-rest</artifactId>
    <version>1.1.1</version>
</dependency>
```

# Examples

The [tests](https://github.com/Alipsa/simple-rest/tree/main/src/test/java/test/alipsa/simplerest) 
use most of the API capabilities and is a good, simple resource for how the api works.

## Google API's example
```groovy
  /**
 * A custom google image search
 * @param question
 * @return The SearchResult is a java class generated from the google json response as per their API
 * @throws se.alipsa.simplerest.RestException if an IO type of issue occurs
 * @throws com.fasterxml.jackson.core.JsonProcessingException if deserialization from String to a SearchResult failed
 */
  private SearchResult search(@NotBlank String question) throws RestException, JsonProcessingException {
    String query = java.net.URLEncoder.encode(question, StandardCharsets.UTF_8);
    String url = "https://www.googleapis.com/customsearch/v1" +
            UrlParameters.parameters(
                    "key", "theLongAPIkey",
                    "cx", "theSearchEngineId",
                    "searchType", "image",
                    "q", query,
                    "num", "9"
            );
    var restClient = new se.alipsa.simplerest.RestClient();  
    return restClient.get(url).getObject(SearchResult.class);
  }
```

## Basic authentication example

Basic auth is simply just a matter of adding the Authorization header for each request, e.g:

```groovy
import static se.alipsa.simplerest.CommonHeaders.*
import se.alipsa.simplerest.*;

// Company is an arbitrary data object
var company = new Company();
company.setName("Creative Design");
var restClient = new RestClient();
var response = restClient.post(
        "http://localhost:8080/companies/company", 
        company, 
        basicAuthHeader("myUserName", "myPassword")
);
Company c = response.getObject(Company.class);
// Do something with c...
```
A GET request is similar e.g:
```groovy
import static se.alipsa.simplerest.CommonHeaders.*;
import se.alipsa.simplerest.*;
var restClient = new RestClient();
var response = restClient.get(
    "http://localhost:8080/companies/company/123/name",
    basicAuthHeader("myUserName", "myPassword")
);
String companyName = response.getPayload();
```

## JSON web token example

```groovy
import static se.alipsa.simplerest.CommonHeaders.*
import se.alipsa.simplerest.*;
import java.util.*;
// 1. First log in to the server to get a JWT token

/**
 * Example of a log in to a server to get the Jason Web Token. The server in this case is a Spring boot app
 * using com.auth0:java-jwt to create and authenticate subsequent requests according to the JWT standard
 * @param userName
 * @param password
 * @return a JWT token if successful, otherwise null
 */
String login(String userName, String password) throws RestException {
  var loginInfo = new HashMap<String, String>();
  loginInfo.put("username", userName);
  loginInfo.put("password", password);
  var restClient = new RestClient();
  Response response = restClient.post("https://localhost:8080/api/login", loginInfo);
  System.out.println("Login: response code: " + response.getResponseCode());
  System.out.println("Got headers: " + response.getHeaders());
  if (response.getResponseCode() == 200) {
    return response.getPayload().trim();
  }
  return null;
}

// 2. Then you can use the JWT token in subsequent requests e.g:

/**
 * Example of a method that retrieves a json array of company objects
 * @param jwtToken the token retrieved using the login method above
 * @return a List of Companies
 */
List<Company> getCompanies(String jwtToken) {
  var restClient = new RestClient();
  try {
    // Add the token to the header of the GET request
    Response response = restClient.get(
            "https://localhost:8080/api/company",
            bearerHeader(jwtToken)
    );
    // getObjectList return a list of the type passed in as a parameter 
    // when the payload is a JSON array of Objects
    return response.getObjectList(Company.class);
  } catch (RestException e) {
    System.err.println("Failed to talk to server: " + e);
  } catch (JsonProcessingException e) {
    System.err.println("Failed to deserialize token: " + e);
  }
  return Collections.emptyList();
}
```

## Complex return types
In some cases you have some even more complex type that you want to get from the response.
In these cases you create a TypeReference and pass that to the getForType method.
Here is an example:

```groovy
import se.alipsa.simplerest.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;
import java.util.List;

var restClient = new RestClient();
Response response = restClient.get("https://localhost:8080/api/info");
Map<String, List<String>> info = response.getForType(new TypeReference<>(){});
```

## Using simple-rest for REST/XML
The Rest service response is assumed to be JSON. If you need to use REST/XML you need to
1. Add a dependency to com.fasterxml.jackson.dataformat:jackson-dataformat-xml
2. pass an XmlMapper (a subclass of ObjectMapper) when constructing the RestClient
3. Set the Content-Type header appropriately (probably application/xml) for each request. 

## Third party libraries used

### Jackson core, databind, and the jsr310 module
Used to convert Json to Java Objects. License: Apache 2.0

That's it! Very lean and simple!