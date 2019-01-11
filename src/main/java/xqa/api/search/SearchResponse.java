package xqa.api.search;

import java.util.List;

import com.google.common.collect.Lists;

public class SearchResponse {
    private final List<SearchResult> searchResult = Lists.newArrayList();

    public List<SearchResult> getSearchResponse() {
        return this.searchResult;
    }
}