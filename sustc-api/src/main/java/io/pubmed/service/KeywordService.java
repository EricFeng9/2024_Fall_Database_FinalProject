package io.pubmed.service;

public interface KeywordService {

    /**
     * Find the number of published articles containing a keyword over the past year, in descending order by year.
     *
     * @param keyword the keyword need queried
     * @return article's numbers in past years which contains given keyword.
     */
    int[] getArticleCountByKeywordInPastYears(String keyword);
}
