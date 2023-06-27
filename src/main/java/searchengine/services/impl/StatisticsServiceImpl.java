package searchengine.services.impl;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SitePage;
import searchengine.model.Status;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.StatisticsService;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        StatisticsResponse response = new StatisticsResponse();

        for (SitePage site : siteRepository.findAll()) {
            DetailedStatisticsItem detailedStatisticsItem = new DetailedStatisticsItem(
                    site.getUrl(),
                    site.getName(),
                    site.getStatus().name(),
                    site.getStatusTime().getTime(),
                    site.getLastError(),
                    pageRepository.countPagesBySitePage(site),
                    lemmaRepository.countLemmasBySiteId(site.getId())
            );
            detailed.add(detailedStatisticsItem);
        }

        TotalStatistics total = new TotalStatistics(
                (int) siteRepository.count(),
                (int) pageRepository.count(),
                (int) lemmaRepository.count(),
                siteRepository.countByStatus(Status.INDEXING) > 0
        );

        StatisticsData statisticsData = new StatisticsData(total, detailed);
        response.setStatistics(statisticsData);
        response.setResult(true);

        return response;
    }
}
