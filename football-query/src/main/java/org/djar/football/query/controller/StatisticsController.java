package org.djar.football.query.controller;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.djar.football.model.MatchScore;
import org.djar.football.model.TeamRanking;
import org.djar.football.query.projection.MatchStatisticsBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(path = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
public class StatisticsController {

    private final KafkaStreams streams;

    public StatisticsController(KafkaStreams streams) {
        this.streams = streams;
    }

    @GetMapping("/matchScores")
    public Flux<MatchScore> getMatchScores() {
        return read(streams.store(MatchStatisticsBuilder.MATCH_SCORES_STORE,
            QueryableStoreTypes.<String, MatchScore>keyValueStore()));
    }

    @GetMapping("/rankings")
    public Flux<TeamRanking> getRankings() {
        return read(streams.store(MatchStatisticsBuilder.RANKING_STORE,
            QueryableStoreTypes.<String, TeamRanking>keyValueStore()));
    }

    private <T> Flux<T> read(ReadOnlyKeyValueStore<String, T> store) {
        return Flux.create(sink -> {
            KeyValueIterator<String, T> iterator = store.all();

            while (iterator.hasNext()) {
                sink.next(iterator.next().value);
            }
            iterator.close();
            sink.complete();
        });
    }
}
