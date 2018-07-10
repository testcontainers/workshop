# Exploring the app

The app is a simple micro-service based on Spring Boot. It provides an API to track the ratings of the talks in real time.

## Storage
### SQL database with the talks
When the rating is sumbmitted, we must verify that the talk for the given ID is present in our database.

Our database of choice is PostgreSQL 10 accessed with Spring JDBC.

Check `com.example.demo.repository.TalksRepository`.

### Redis
We store the ratings in Redis database with Spring Data Redis (Reactive)

Check `com.example.demo.repository.RatingsRepository`.

### Kafka
We use ES/CQRS to materialize the events into the state. Kafka acts as a broker and we use Spring Kafka.

Check `com.example.demo.streams.RatingsListener`.

## API
The API is a Spring Webflux controller (`com.example.demo.api.RatingsController`) and exposes two endpoints:
* `POST /ratings { "talkId": ?, "value": 1-5 }` to add a rating
* `GET /ratings?talkId=?` to get the histogram of ratings of a talk


