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
    <version>1.0.0</version>
</dependency>
```

# Examples

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
        "?key=theLongAPIkey" +
        "&cx=theSearchEngineId" +
        "&searchType=image" +
        "&q=" + query +
        "&num=" + 9;
    var restClient = new se.alipsa.simplerest.RestClient();  
    return restClient.get(url).getObject(SearchResult.class);
  }
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
 * @param jwtToken
 * @return a List of Companies
 */
List<Company> getCompanies(String jwtToken) {
  var restClient = new RestClient();
  try {
    // Add the token to the header of the GET request
    Response response = restClient.get(
            "https://localhost:8080/api/company", 
            Map.of("Authorization", bearer(jwtToken))
    );
    // getObjectList return a list of the type passed in as a parameter 
    // when the payload is a JSON array or Objects
    return response.getObjectList(Company.class);
  } catch (RestException e) {
    System.err.println("Failed to talk to server: " + e);
  } catch (JsonProcessingException e) {
    System.err.println("Failed to deserialize token: " + e);
  }
  return Collections.emptyList();
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
    Map.of(AUTHORIZATION, basicAuth("myUserName", "myPassword"))
);
Company c = response.getObject(Company.class);
```