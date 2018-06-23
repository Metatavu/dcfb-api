package fi.metatavu.dcfb.server.search.searchers;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import fi.metatavu.dcfb.server.rest.model.LocationListSort;
import fi.metatavu.dcfb.server.search.index.IndexableLocation;

/**
 * Searcher for locations
 */
@ApplicationScoped
public class LocationSearcher extends AbstractSearcher {

  /**
   * Searches locations and returns result as UUIDs
   * 
   * @param slug filter results by slug. Omitted if null
   * @param search free text search that must match the result. Omitted if null
   * @param firstResult first result. Defaults to 0
   * @param maxResults max results. Defaults to 20
   * @return search result 
   */
  public SearchResult<UUID> searchLocations(String slug, String search, Long firstResult, Long maxResults, List<LocationListSort> sorts) {
    boolean matchAll = slug == null && search == null;
    if (matchAll) {
      return executeSearch(matchAllQuery(), createSorts(sorts), firstResult, maxResults);
    } else {
      BoolQueryBuilder query = boolQuery();
      
      if (slug != null) {
        query.must(matchQuery(IndexableLocation.SLUG_FIELD, slug));
      }

      if (search != null) {
        query.must(queryStringQuery(search));
      }

      return executeSearch(query, createSorts(sorts), firstResult, maxResults);
    }
  }

  @Override
  public String getType() {
    return IndexableLocation.TYPE;
  }
   
  /**
   * Creates sorts. Defaults to created at ascending
   * 
   * @param sorts list of sorts
   * @return created sort builders
   */
  private List<SortBuilder<?>> createSorts(List<LocationListSort> sorts) {
    List<SortBuilder<?>> result = sorts == null ? Collections.emptyList() : sorts.stream()
      .map(sort -> {
        switch (sort) {
          case CREATED_AT_DESC:
            return SortBuilders.fieldSort(IndexableLocation.CREATED_AT_FIELD).order(SortOrder.DESC);
          case CREATED_AT_ASC:
            return SortBuilders.fieldSort(IndexableLocation.CREATED_AT_FIELD).order(SortOrder.ASC);
          case MODIFIED_AT_DESC:
            return SortBuilders.fieldSort(IndexableLocation.MODIFIED_AT_FIELD).order(SortOrder.DESC);
          case MODIFIED_AT_ASC:
            return SortBuilders.fieldSort(IndexableLocation.MODIFIED_AT_FIELD).order(SortOrder.ASC);
          case SCORE_DESC:
            return SortBuilders.scoreSort().order(SortOrder.DESC);
          case SCORE_ASC:
            return SortBuilders.scoreSort().order(SortOrder.ASC);
          default:
        }

        return null;
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toList());

    if (result.isEmpty()) {
      return Collections.singletonList(SortBuilders.fieldSort(IndexableLocation.CREATED_AT_FIELD).order(SortOrder.ASC));      
    }

    return result;
  }
  
}