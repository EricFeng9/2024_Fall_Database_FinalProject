package io.pubmed.service;

import io.pubmed.dto.Journal;

public interface JournalService {

    /**
     * Please calculate the journal's Impact Factor(IF) at the given year.
     * At the given year, IF = the total number of citations of articles in the
     * given year / the total number of articles published in the journal in
     * the previous two years.
     *
     * if year = 2024, you need sum citations of given journal in 2024 /
     * [2022-2023] published articles num in the journal.
     * Example:
     * IF（2024） = A / B
     * A = The number of times all articles in the journal from 2022 to 2023 were cited in 2024.
     * B = Number of articles in the journal from 2022 to 2023.
     *
     *
     * @param journal_title need queried journal title
     * @param year need queried year
     * @return the title of the required Journal
     */
    double getImpactFactor(String journal_title, int year);

    /**
     * A journal changed its title from given year, but database data was not update,
     * please update the database, change the article's journal_title from given year
     * (include that year).
     * @param journal need update journal, only contain title and id fields
     * @param year need update from and include year
     * @param new_name need update old journal tile to new_name
     * @param new_id the new journal title's id
     * @return your implement success or not
     * Tips: After testing, you would better delete it from database for next testing.
     */
    boolean updateJournalName(Journal journal, int year, String new_name, String new_id);
}
