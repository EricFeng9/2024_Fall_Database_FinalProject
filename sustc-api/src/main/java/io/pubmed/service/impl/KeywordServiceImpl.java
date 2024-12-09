package io.pubmed.service.impl;

import io.pubmed.service.KeywordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
/**
 * It's important to mark your implementation class with {@link Service} annotation.
 * As long as the class is annotated and implements the corresponding interface, you can place it under any package.
 */
@Service
@Slf4j
public class KeywordServiceImpl implements KeywordService {
    @Autowired
    private DataSource dataSource;
    /**
     * Find the number of published articles containing a keyword over the past year, in descending order by year.
     *
     * @param keyword the keyword need queried
     * @return article's numbers in past years which contains given keyword.
     */
    @Override
    public int[] getArticleCountByKeywordInPastYears(String keyword) {
        return new int[0];
    }
}
