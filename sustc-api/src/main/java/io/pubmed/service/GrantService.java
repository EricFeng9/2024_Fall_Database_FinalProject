package io.pubmed.service;

public interface GrantService {

    /**
     * Find all the funded articles by the given country
     *
     * @param country you need query
     * @return the pmid list of funded articles
     */
    int[] getCountryFundPapers(String country);
}
