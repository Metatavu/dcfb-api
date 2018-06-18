package fi.metatavu.dcfb.server.search.searchers;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import org.elasticsearch.index.query.BoolQueryBuilder;

import fi.metatavu.dcfb.server.search.index.IndexableItem;

/**
 * Searcher for items
 */
@ApplicationScoped
public class ItemSearcher extends AbstractSearcher {

  /**
   * Searches item and returns result as UUIDs
   * 
   * @param categoryIds category ids that must exist on the result. Omitted if null
   * @param search free text search that must match the result. Omitted if null
   * @param firstResult first result. Defaults to 0
   * @param maxResults max results. Defaults to 20
   * @return search result 
   */
  public SearchResult<UUID> searchItems(List<UUID> categoryIds, String search, Long firstResult, Long maxResults) {
    boolean matchAll = categoryIds == null && search == null;
    if (matchAll) {
      return executeSearch(matchAllQuery(), firstResult, maxResults);
    } else {    
      BoolQueryBuilder query = boolQuery();
      
      if (categoryIds != null) {
        BoolQueryBuilder matchOrQuery = boolQuery();

        categoryIds.forEach(categoryId -> {
          matchOrQuery.should(matchQuery(IndexableItem.CATEGORY_ID_FIELD, categoryId.toString()));
        });

        query.must(matchOrQuery);
      }

      if (search != null) {
        query.must(queryStringQuery(search));
      }

      return executeSearch(query, firstResult, maxResults);
    }
  }

  @Override
  public String getType() {
    return IndexableItem.TYPE;
  }
   
}
