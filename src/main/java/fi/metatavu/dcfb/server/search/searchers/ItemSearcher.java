package fi.metatavu.dcfb.server.search.searchers;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.constantScoreQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
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
   * @param locationId location id that must exist on the result. Omitted if null
   * @param search free text search that must match the result. Omitted if null
   * @param firstResult first result. Defaults to 0
   * @param maxResults max results. Defaults to 20
   * @return search result 
   */
  public SearchResult<UUID> searchItems(List<UUID> categoryIds, List<UUID> locationIds, String search, UUID currentUserId, Long firstResult, Long maxResults, List<ItemListSort> sorts) {
    boolean matchAll = categoryIds == null && locationIds == null && search == null;
    if (matchAll) {
      ConstantScoreQueryBuilder query = constantScoreQuery(createPublicOrInAllowedIdsQuery(currentUserId.toString()));
      query.boost(1.0f);
      return executeSearch(query, createSorts(sorts), firstResult, maxResults);
    } else {
      BoolQueryBuilder query = boolQuery();
      query.must(createPublicOrInAllowedIdsQuery(currentUserId.toString()));

      if (categoryIds != null) {
        query.must(createOrMatchQuery(IndexableItem.CATEGORY_ID_FIELD, categoryIds));
      }

      if (locationIds != null) {
        query.must(createOrMatchQuery(IndexableItem.LOCATION_ID_FIELD, locationIds));
      }
      
      if (search != null) {
        query.must(queryStringQuery(search));
      }

      return executeSearch(query, createSorts(sorts), firstResult, maxResults);
    }
  }

  /**
   * Creates match queries for all given ids for given field. 
   * Fields are combined within bool query with or operator 
   * 
   * @param field field
   * @param ids ids
   * @return bool query
   */
  private BoolQueryBuilder createOrMatchQuery(String field, List<UUID> ids) {
    BoolQueryBuilder matchOrQuery = boolQuery();
    ids.forEach(id -> matchOrQuery.should(matchQuery(field, id.toString())));
    return matchOrQuery;
  }

  private BoolQueryBuilder createPublicOrInAllowedIdsQuery(String currentUserId) {
    BoolQueryBuilder publicOrInAllowedIdsQuery = boolQuery();
    publicOrInAllowedIdsQuery.should(termQuery(IndexableItem.VISIBILITY_LIMITED_FIELD, Boolean.FALSE));
    if (currentUserId != null) {
      publicOrInAllowedIdsQuery.should(termQuery(IndexableItem.ALLOWED_USER_IDS_FIELD, currentUserId));
    }

    return publicOrInAllowedIdsQuery;
  }

  @Override
  public String getType() {
    return IndexableItem.TYPE;
  }

  /**
   * Creates sorts. Defaults to created at ascending
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
      return Collections.singletonList(SortBuilders.fieldSort(IndexableCategory.CREATED_AT_FIELD).order(SortOrder.ASC));      
    }

    return result;  
  }
}
