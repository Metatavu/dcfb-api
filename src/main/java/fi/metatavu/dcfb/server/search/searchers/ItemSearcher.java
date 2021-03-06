package fi.metatavu.dcfb.server.search.searchers;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.constantScoreQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

import java.util.ArrayList;
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
   * @param nearLat prefer items near geo point
   * @param nearLon prefer items near geo point
   * @param sellerIds view only seller ids
   * @param categoryIds category ids that must exist on the result. Omitted if null
   * @param locationId location id that must exist on the result. Omitted if null
   * @param search free text search that must match the result. Omitted if null
   * @param currentUserId currentUserId
   * @param includeExhausted whether to include exhausted items
   * @param firstResult first result. Defaults to 0
   * @param maxResults max results. Defaults to 20
   * @return search result 
   */
  @SuppressWarnings ("squid:S00107")
  public SearchResult<UUID> searchItems(Double nearLat, Double nearLon, List<UUID> sellerIds, List<UUID> categoryIds, List<UUID> locationIds, 
      String search, UUID currentUserId, boolean includeExhausted, Long firstResult, Long maxResults, List<ItemListSort> sorts) {
    
    boolean matchAll = categoryIds == null && locationIds == null && search == null && sellerIds == null && includeExhausted;
    if (matchAll) {
      ConstantScoreQueryBuilder query = constantScoreQuery(createPublicOrInAllowedIdsQuery(currentUserId.toString()));
      query.boost(1.0f);
      return executeSearch(query, createSorts(nearLat, nearLon, sorts), firstResult, maxResults);
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
      
      if (sellerIds != null) {
        query.must(createOrMatchQuery(IndexableItem.SELLER_ID_FIELD, sellerIds));
      }
      
      if (!includeExhausted) {
        query.must(rangeQuery(IndexableItem.ITEMS_LEFT).gt(0));
      }
      
      return executeSearch(query, createSorts(nearLat, nearLon, sorts), firstResult, maxResults);
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
   * @param nearLat prefer items near geo point
   * @param nearLon prefer items near geo point
   * @param sorts list of sorts
   * @return created sort builders
   */
  private List<SortBuilder<?>> createSorts(Double nearLat, Double nearLon, List<ItemListSort> sorts) {
    List<SortBuilder<?>> result = new ArrayList<>();
    
    if (nearLat != null && nearLon != null) {
      result.add(SortBuilders.geoDistanceSort(IndexableItem.GEOPOINT, nearLat, nearLon));
    }    
    
    List<SortBuilder<?>> fieldSorts = sorts == null ? Collections.emptyList() : sorts.stream().map(sort -> {
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
    
    result.addAll(fieldSorts);
    
    if (result.isEmpty()) {
      return Collections.singletonList(SortBuilders.fieldSort(IndexableCategory.CREATED_AT_FIELD).order(SortOrder.ASC));      
    }

    return result;  
  }
}
