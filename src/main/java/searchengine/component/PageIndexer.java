package searchengine.component;

import searchengine.model.Page;

public interface PageIndexer {
    void indexHtml(String html, Page indexingPage);
}
