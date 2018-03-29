package xqa.api.search;

import com.google.common.collect.Lists;

import java.util.List;

public class SearchResponse {
    private final List<SearchResult> searchResult = Lists.newArrayList();

    public List<SearchResult> getSearchResponse() {
        return this.searchResult;
    }
}