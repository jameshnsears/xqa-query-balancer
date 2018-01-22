package xqa.api.search;

import java.util.List;

import com.google.common.collect.Lists;

public class SearchResponse {
    private List<SearchResult> searchResult = Lists.newArrayList();

    public SearchResponse() {
    }

    public SearchResponse(List<SearchResult> searchResult) {
        this.searchResult = searchResult;
    }

    public List<SearchResult> getSearchResponse() {
        return this.searchResult;
    }

    public void setSearchResponse(List<SearchResult> searchResult) {
        this.searchResult = searchResult;
    }
}