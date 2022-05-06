# Release history

### ver 1.0.2, in progress

### ver 1.0.1, 2022-05-06
- Default to the Object mapper in the RestClient instead of creating a new one.
- Add javadocs
- make headerRequest private, head and options should be enough.
- Use the same method to check for possible content in the delete request as in the put and post

### ver 1.0.0, 2022-05-06
Initial release. Works fine for the the projects I am working on at the moment (one using Google api's
and the other a Spring Boot app using JWT (jason web token)).