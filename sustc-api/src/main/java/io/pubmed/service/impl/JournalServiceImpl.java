package io.pubmed.service.impl;

import io.pubmed.dto.Journal;
import io.pubmed.service.JournalService;
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
public class JournalServiceImpl implements JournalService {
    @Autowired
    private DataSource dataSource;
    /**
     * Please calculate the journal's Impact Factor(IF) at the given year.
     * At the given year, IF = the total number of citations of articles in the
     * given year / the total number of articles published in the journal in
     * the previous two years.
     * <p>
     * if year = 2024, you need sum citations of given journal in 2024 /
     * [2022-2023] published articles num in the journal.
     * Example:
     * IF（2024） = A / B
     * A = The number of times all articles in the journal from 2022 to 2023 were cited in 2024.
     * B = Number of articles in the journal from 2022 to 2023.
     *
     * @param journal_title need queried journal title
     * @param year          need queried year
     * @return the title of the required Journal
     */
    @Override
    public double getImpactFactor(String journal_title, int year) {
        return 0;
    }

    /**
     * A journal changed its title from given year, but database data was not update,
     * please update the database, change the article's journal_title from given year
     * (include that year).
     *
     * @param journal  need update journal, only contain title and id fields
     * @param year     need update from and include year
     * @param new_name need update old journal tile to new_name
     * @param new_id   the new journal title's id
     * @return your implement success or not
     * Tips: After testing, you would better delete it from database for next testing.
     */
    @Override
    public boolean updateJournalName(Journal journal, int year, String new_name, String new_id) {
        return false;
    }
}
