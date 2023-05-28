package searchengine.services;

import org.springframework.http.ResponseEntity;
import searchengine.config.SitesList;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public interface IndexingService {
    ResponseEntity<String> findLemmas(SitesList sitesList, URL url) throws IOException;

    ResponseEntity startIndexing();
    ResponseEntity stopIndexing();
}
