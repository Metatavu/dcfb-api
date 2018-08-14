package fi.metatavu.dcfb.server.search.searchers;

import java.util.List;

/**
 * Class that represents search result
 * 
 * @author Antti Leppä
 */
public class SearchResult <T> {
  
  private List<T> result;
  private long totalHits;
  
  /**
   * Constructor
   * 
   * @param result results
   * @param totalHits
   */
  public SearchResult(List<T> result, long totalHits) {
    this.result = result;
    this.totalHits = totalHits;
  }
  
  /**
   * Returns result
   * 
   * @return result
   */
  public List<T> getResult() {
    return result;
  }
  
  /**
   * Returns total hit count
   * 
   * @return total hit count
   */
  public long getTotalHits() {
    return totalHits;
  }
  
}
