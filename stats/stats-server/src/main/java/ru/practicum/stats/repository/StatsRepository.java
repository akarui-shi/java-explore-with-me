package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long>, JpaSpecificationExecutor<EndpointHit> {


    @Query("select new ru.practicum.stats.model.ViewStats(hit.app, hit.uri, count(hit.id) as counter) " +
            "from EndpointHit as hit " +
            "where (cast(:start as timestamp) is null or hit.timestamp >= (:start)) " +
            "and (cast(:end as timestamp) is null or hit.timestamp <= (:end)) " +
            "and ((:uris) is null or hit.uri in (:uris)) " +
            "group by hit.app, hit.uri " +
            "order by counter desc")
    List<ViewStats> getStatsWithFilters(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end,
                                        @Param("uris") List<String> uris);

    @Query("select new ru.practicum.stats.model.ViewStats(hit.app, hit.uri, count(distinct(hit.ip)) as counter) " +
            "from EndpointHit as hit " +
            "where (cast(:start as timestamp) is null or hit.timestamp >= :start) " +
            "and (cast(:end as timestamp) is null or hit.timestamp <= :end) " +
            "and ((:uris) is null or hit.uri in (:uris)) " +
            "group by hit.app, hit.uri " +
            "order by counter desc")
    List<ViewStats> getStatsUniqueWithFilters(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end,
                                              @Param("uris") List<String> uris);
}
