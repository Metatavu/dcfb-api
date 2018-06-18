package fi.metatavu.dcfb.server.search.searchers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import fi.metatavu.dcfb.server.search.io.IndexReader;

/**
 * Abstract base class for searchers
 */
public abstract class AbstractSearcher {

  public static final int DEFALT_MAX_RESULTS = 20;

  @Inject
  private IndexReader indexReader;

  /**
   * Returns type the searcher uses
   * 
   * @return type
   */
  public abstract String getType();

  /**
   * Executes a search and returns result as UUIDs
   * 
   * @param query query
   * @param firstResult first result
   * @param maxResults max results
   * @return result
   */
  protected SearchResult<UUID> executeSearch(QueryBuilder query, Long firstResult, Long maxResults) {
    SearchRequestBuilder requestBuilder = indexReader
      .requestBuilder(getType())
      .setQuery(query)
      .setFrom(firstResult != null ? firstResult.intValue() : 0)
      .setSize(maxResults != null ? maxResults.intValue() : DEFALT_MAX_RESULTS);
    
    SearchResponse response = indexReader.executeSearch(requestBuilder);
    SearchHits searchHits = response.getHits();
    return fromHits(searchHits);
  }
  
  /**
   * Extracts search result for hits
   * 
   * @param searchHits hits
   * @return search result
   */
  private SearchResult<UUID> fromHits(SearchHits searchHits) {
    List<UUID> result = Arrays.stream(searchHits.getHits())
      .map(SearchHit::getId)
      .map(UUID::fromString)
      .collect(Collectors.toList());

    return new SearchResult<>(result, searchHits.getTotalHits());
  }
}
