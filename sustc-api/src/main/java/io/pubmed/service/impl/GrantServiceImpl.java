package io.pubmed.service.impl;

import io.pubmed.service.GrantService;
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
public class GrantServiceImpl implements GrantService {
    @Autowired
    private DataSource dataSource;
    /**
     * Find all the funded articles by the given country
     *
     * @param country you need query
     * @return the pmid list of funded articles
     */
    @Override
    public int[] getCountryFundPapers(String country) {
        return new int[0];
    }
}
