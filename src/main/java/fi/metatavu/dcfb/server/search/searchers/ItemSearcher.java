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

import fi.metatavu.dcfb.server.rest.model.ItemListSort;
import fi.metatavu.dcfb.server.search.index.IndexableCategory;
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
  public SearchResult<UUID> searchItems(List<UUID> categoryIds, String search, Long firstResult, Long maxResults, List<ItemListSort> sorts) {
    boolean matchAll = categoryIds == null && search == null;
    if (matchAll) {
      return executeSearch(matchAllQuery(), createSorts(sorts), firstResult, maxResults);
    } else {    
      BoolQueryBuilder query = boolQuery();
      
      if (categoryIds != null) {
        BoolQueryBuilder matchOrQuery = boolQuery();

        categoryIds.forEach(categoryId -> 
          matchOrQuery.should(matchQuery(IndexableItem.CATEGORY_ID_FIELD, categoryId.toString()))
        );

        query.must(matchOrQuery);
      }

      if (search != null) {
        query.must(queryStringQuery(search));
      }

      return executeSearch(query, createSorts(sorts), firstResult, maxResults);
    }
  }

  @Override
  public String getType() {
    return IndexableItem.TYPE;
  }

  /**
   * Creates sorts. Defaults to created at descending
   * 
   * @param sorts list of sorts
   * @return created sort builders
   */
  private List<SortBuilder<?>> createSorts(List<ItemListSort> sorts) {
    List<SortBuilder<?>> result = sorts == null ? Collections.emptyList() : sorts.stream().map(sort -> {
      switch (sort) {
        case CREATED_AT_DESC:
          return SortBuilders.fieldSort(IndexableItem.CREATED_AT_FIELD).order(SortOrder.DESC);
        case CREATED_AT_ASC:
          return SortBuilders.fieldSort(IndexableItem.CREATED_AT_FIELD).order(SortOrder.ASC);
        case MODIFIED_AT_DESC:
          return SortBuilders.fieldSort(IndexableItem.MODIFIED_AT_FIELD).order(SortOrder.DESC);
        case MODIFIED_AT_ASC:
          return SortBuilders.fieldSort(IndexableItem.MODIFIED_AT_FIELD).order(SortOrder.ASC);
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
      return Collections.singletonList(SortBuilders.fieldSort(IndexableCategory.CREATED_AT_FIELD).order(SortOrder.DESC));      
    }

    return result;  
  }
   
}
