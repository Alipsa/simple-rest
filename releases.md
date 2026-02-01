# Release history

### ver 1.1.2, in progress
- io.github.hakky54:sslcontext-kickstart 9.2.1 -> io.github.hakky54:ayza 10.0.3

### ver 1.1.1, 2026-02-01
- Fixed connection/body handling and UTF‑8 usage across request/response flows, and ensured error bodies are read when available while preserving existing GET error behaviour.
- Made basic auth encoding deterministic (UTF‑8).
- Corrected Response.equals/hashCode and made header lookup null‑safe and case‑insensitive.
- Added parameter count validation for URL params and tests for the new behaviours.
- Upgrade Jackson 2.18.2 -> 2.21.0

### ver 1.1.0, 2025-02-20
- Upgrade jackson 2.14.2 -> 2.18.2
- Upgrade jetty 11.0.14 -> 11.0.24
- Add support for https
- Require java 17 or later

### ver 1.0.5, 2023-03-19
- Add RequestMethod with static constants for all Http Request methods
- Update test dependencies

### ver 1.0.4, 2023-02-11
- Add getForType which takes a TypeReference as parameter to be able to return "anything"
- Upgrade dependencies for jackson, and test dependencies (jetty, junit, slf4j) 

### ver 1.0.3, 2022-08-01
- upgrade test dependencies (junit, jetty). 
- add some docs

### ver 1.0.2, 2022-05-25
- Describe how to handle REST/XML, add application/xml to the MediaType
- Fix url in pom.xml
- Update maven plugin versions
- add Accept header to common headers
- set content type even if requestHeaders param is null
- add UrlParameters utility method

### ver 1.0.1, 2022-05-06
- Default to the Object mapper in the RestClient instead of creating a new one.
- Add javadocs
- make headerRequest private, head and options should be enough.
- Use the same method to check for possible content in the delete request as in the put and post

### ver 1.0.0, 2022-05-06
Initial release. Works fine for the the projects I am working on at the moment (one using Google api's
and the other a Spring Boot app using JWT (jason web token)).