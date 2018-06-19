package fi.metatavu.dcfb.server.search.searchers;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import fi.metatavu.dcfb.server.rest.model.CategoryListSort;
import fi.metatavu.dcfb.server.search.index.IndexableCategory;

/**
 * Searcher for categories
 */
@ApplicationScoped
public class CategorySearcher extends AbstractSearcher {

  /**
   * Searches category and returns result as UUIDs
   * 
   * @param parentId parent id of result categories. Omitted if null
   * @param search free text search that must match the result. Omitted if null
   * @param firstResult first result. Defaults to 0
   * @param maxResults max results. Defaults to 20
   * @return search result 
   */
  public SearchResult<UUID> searchCategories(UUID parentId, String search, Long firstResult, Long maxResults, List<CategoryListSort> sorts) {
    boolean matchAll = parentId == null && search == null;
    if (matchAll) {
      return executeSearch(matchAllQuery(), createSorts(sorts), firstResult, maxResults);
    } else {    
      BoolQueryBuilder query = boolQuery();
      
      if (parentId != null) {
        query.must(matchQuery(IndexableCategory.PARENT_ID_FIELD, parentId.toString()));
      }

      if (search != null) {
        query.must(queryStringQuery(search));
      }

      return executeSearch(query, createSorts(sorts), firstResult, maxResults);
    }
  }

  @Override
  public String getType() {
    return IndexableCategory.TYPE;
  }
   
  /**
   * Creates sorts. Defaults to created at descending
   * 
   * @param sorts list of sorts
   * @return created sort builders
   */
  private List<SortBuilder<?>> createSorts(List<CategoryListSort> sorts) {
    if (sorts == null) {
      return Collections.singletonList(SortBuilders.fieldSort(IndexableCategory.CREATED_AT_FIELD).order(SortOrder.DESC));
    }

	  return sorts.stream().map(sort -> {
      switch (sort) {
        case CREATED_AT_DESC:
          return SortBuilders.fieldSort(IndexableCategory.CREATED_AT_FIELD).order(SortOrder.DESC);
        case CREATED_AT_ASC:
          return SortBuilders.fieldSort(IndexableCategory.CREATED_AT_FIELD).order(SortOrder.ASC);
        case MODIFIED_AT_DESC:
          return SortBuilders.fieldSort(IndexableCategory.MODIFIED_AT_FIELD).order(SortOrder.DESC);
        case MODIFIED_AT_ASC:
          return SortBuilders.fieldSort(IndexableCategory.MODIFIED_AT_FIELD).order(SortOrder.ASC);
        case SCORE_DESC:
          return SortBuilders.scoreSort().order(SortOrder.DESC);
        case SCORE_ASC:
          return SortBuilders.scoreSort().order(SortOrder.ASC);
        default:
      }

      return null;
    }).collect(Collectors.toList());
  }
}
