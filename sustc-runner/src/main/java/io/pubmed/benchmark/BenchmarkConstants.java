package io.pubmed.benchmark;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class BenchmarkConstants {

    public static final double EPS = 1e-6;

    public static final String IMPORT_DATA = "import";

    public static final String IMPORT_DATA_PATH = "";


    public static final String TEST_DATA = "test";

    public static final String getArticleCitationsByYear = "getArticleCitationsByYear.ser";

    public static final String addArticleAndUpdateIF = "addArticleAndUpdateIF.ser";

    public static final String getArticlesByAuthorSortedByCitations = "getArticlesByAuthorSortedByCitations.ser";

    public static final String getJournalWithMostArticlesByAuthor = "getJournalWithMostArticlesByAuthor.ser";

    public static final String getMinArticlesToLinkAuthors = "getMinArticlesToLinkAuthors.ser";

    public static final String getCountryFundPapers = "getCountryFundPapers.ser";

    public static final String getImpactFactor = "getImpactFactor.ser";

    public static final String updateJournalName = "updateJournalName.ser";

    public static final String getArticleCountByKeywordInPastYears = "getArticleCountByKeywordInPastYears.ser";

    public static final String USER_INFO = "user-info.ser";

    public static final String DANMU_LIKE = "danmu-like.ser";

    public static final String VIDEO_COIN = "video-coin.ser";

    public static final String VIDEO_LIKE = "video-like.ser";

    public static final String VIDEO_COLLECT = "video-collect.ser";

    public static final String USER_REGISTER = "user-register.ser";

    public static final String VIDEO_POST = "video-post.ser";

    public static final String VIDEO_UPDATE = "video-update.ser";

    public static final String VIDEO_REVIEW = "video-review.ser";

    public static final String SUPER_USER_AUTH = "super-user-auth.ser";

    public static final String VIDEO_SEARCH_2 = "video-search-2.ser";

    public static final String USER_DELETE = "user-delete.ser";

    public static final String VIDEO_DELETE = "video-delete.ser";

    public static final String USER_FOLLOW = "user-follow.ser";
}
