package io.pubmed.benchmark;

import io.fury.ThreadSafeFury;
import io.pubmed.benchmark.BenchmarkConfig;
import io.pubmed.benchmark.BenchmarkConstants;
import io.pubmed.benchmark.BenchmarkResult;
import io.pubmed.benchmark.BenchmarkStep;
import io.pubmed.dto.*;
import io.pubmed.service.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class BenchmarkService {

    @Autowired
    private BenchmarkConfig config;

    @Autowired
    private DatabaseService databaseService;

    @Autowired(required = false)
    private ArticleService articleService;

    @Autowired(required = false)
    private AuthorService authorService;

    @Autowired(required = false)
    private GrantService grantService;

    @Autowired(required = false)
    private JournalService journalService;

    @Autowired(required = false)
    private KeywordService keywordService;

    @Autowired
    private ThreadSafeFury fury;

    private final Map<Long, String> sentDanmu = new ConcurrentHashMap<>();

    private final Set<String> postedVideo = new ConcurrentSkipListSet<>();

    private final Set<Long> registeredUser = new ConcurrentSkipListSet<>();


    @io.pubmed.benchmark.BenchmarkStep(order = 1, timeout = 35, description = "Test getArticleCitationsByYear(int, int)")
    public BenchmarkResult getArticleCitationsByYear() {

        List<Map.Entry<Object[], Integer>> cases = deserialize(io.pubmed.benchmark.BenchmarkConstants.TEST_DATA, io.pubmed.benchmark.BenchmarkConstants.getArticleCitationsByYear);
        val pass = new AtomicLong();

        val startTime = System.currentTimeMillis();
        cases.parallelStream().forEach(it -> {
            try {
                val args = it.getKey();
                val res = articleService.getArticleCitationsByYear((int)args[0], (int)args[1]);
                if (it.getValue() == res) {
                    pass.incrementAndGet();
                } else {
                    log.debug("Wrong answer for {}: expected {}, got {}", it.getKey(), it.getValue(), res);
                }
            } catch (Exception e) {
                log.error("Exception thrown for {}", it, e);
            }
        });
        val endTime = System.currentTimeMillis();

        return new io.pubmed.benchmark.BenchmarkResult(pass, endTime - startTime);
    }

    @io.pubmed.benchmark.BenchmarkStep(order = 2, timeout = 35, description = "Test addArticleAndUpdateIF(Article)")
    public BenchmarkResult addArticleAndUpdateIF() {

        List<Map.Entry<Object[], Double>> cases = deserialize(io.pubmed.benchmark.BenchmarkConstants.TEST_DATA, io.pubmed.benchmark.BenchmarkConstants.addArticleAndUpdateIF);
        val pass = new AtomicLong();

        val startTime = System.currentTimeMillis();
        cases.parallelStream().forEach(it -> {
            try {
                val args = it.getKey();
                val res = articleService.addArticleAndUpdateIF((Article)args[0]);
                if (it.getValue() == res) {
                    pass.incrementAndGet();
                } else {
                    log.debug("Wrong answer for {}: expected {}, got {}", it.getKey(), it.getValue(), res);
                }
            } catch (Exception e) {
                log.error("Exception thrown for {}", it, e);
            }
        });
        val endTime = System.currentTimeMillis();

        return new io.pubmed.benchmark.BenchmarkResult(pass, endTime - startTime);
    }

    @io.pubmed.benchmark.BenchmarkStep(order = 3, timeout = 35, description = "Test getArticlesByAuthorSortedByCitations(Author)")
    public BenchmarkResult getArticlesByAuthorSortedByCitations() {

        List<Map.Entry<Object[], int[]>> cases = deserialize(io.pubmed.benchmark.BenchmarkConstants.TEST_DATA, io.pubmed.benchmark.BenchmarkConstants.getArticlesByAuthorSortedByCitations);
        val pass = new AtomicLong();

        val startTime = System.currentTimeMillis();
        cases.parallelStream().forEach(it -> {
            try {
                val args = it.getKey();
                val res = authorService.getArticlesByAuthorSortedByCitations((Author)args[0]);
                if (Arrays.equals(it.getValue(),res)) { //it.getValue() == res
                    pass.incrementAndGet();
                } else {
                    log.info("Wrong answer for {}: expected {}, got {}", it.getKey(), it.getValue(), res);
                }
            } catch (Exception e) {
                log.error("Exception thrown for {}", it, e);
            }
        });
        val endTime = System.currentTimeMillis();

        return new io.pubmed.benchmark.BenchmarkResult(pass, endTime - startTime);
    }

    @io.pubmed.benchmark.BenchmarkStep(order = 4, timeout = 35, description = "Test getJournalWithMostArticlesByAuthor(Author)")
    public BenchmarkResult getJournalWithMostArticlesByAuthor() {

        List<Map.Entry<Object[], String>> cases = deserialize(io.pubmed.benchmark.BenchmarkConstants.TEST_DATA, io.pubmed.benchmark.BenchmarkConstants.getJournalWithMostArticlesByAuthor);
        val pass = new AtomicLong();

        val startTime = System.currentTimeMillis();
        cases.parallelStream().forEach(it -> {
            try {
                val args = it.getKey();
                val res = authorService.getJournalWithMostArticlesByAuthor((Author)args[0]);
                if (it.getValue().equals(res)) {
                    pass.incrementAndGet();
                } else {
                    log.info("Wrong answer for {}: expected {}, got {}", it.getKey(), it.getValue(), res);
                }
            } catch (Exception e) {
                log.error("Exception thrown for {}", it, e);
            }
        });
        val endTime = System.currentTimeMillis();

        return new io.pubmed.benchmark.BenchmarkResult(pass, endTime - startTime);
    }
//    if you want test this bonus task, please uncomment the following code
//    @io.pubmed.benchmark.BenchmarkStep(order = 5, timeout = 35, description = "Test getMinArticlesToLinkAuthors(Author, Author)")
//    public BenchmarkResult getMinArticlesToLinkAuthors() {
//
//        List<Map.Entry<Object[], Integer>> cases = deserialize(io.pubmed.benchmark.BenchmarkConstants.TEST_DATA, io.pubmed.benchmark.BenchmarkConstants.getMinArticlesToLinkAuthors);
//        val pass = new AtomicLong();
//
//        val startTime = System.currentTimeMillis();
//        cases.parallelStream().forEach(it -> {
//            try {
//                val args = it.getKey();
//                val res = authorService.getMinArticlesToLinkAuthors((Author)args[0],(Author)args[0]);
//                if (it.getValue() == res) {
//                    pass.incrementAndGet();
//                } else {
//                    log.info("Wrong answer for {}: expected {}, got {}", it.getKey(), it.getValue(), res);
//                }
//            } catch (Exception e) {
//                log.error("Exception thrown for {}", it, e);
//            }
//        });
//        val endTime = System.currentTimeMillis();
//
//        return new io.pubmed.benchmark.BenchmarkResult(pass, endTime - startTime);
//    }


    @io.pubmed.benchmark.BenchmarkStep(order = 6, timeout = 35, description = "Test getCountryFundPapers(String)")
    public BenchmarkResult getCountryFundPapers() {

        List<Map.Entry<Object[], int[]>> cases = deserialize(io.pubmed.benchmark.BenchmarkConstants.TEST_DATA, io.pubmed.benchmark.BenchmarkConstants.getCountryFundPapers);
        val pass = new AtomicLong();

        val startTime = System.currentTimeMillis();
        cases.parallelStream().forEach(it -> {
            try {
                val args = it.getKey();
                val res = grantService.getCountryFundPapers((String) args[0]);
                if (ArraysEqual(it.getValue() , res)) {
                    pass.incrementAndGet();
                } else {
                    log.info("Wrong answer for {}: expected {}, got {}", it.getKey(), it.getValue(), res);
                }
            } catch (Exception e) {
                log.error("Exception thrown for {}", it, e);
            }
        });
        val endTime = System.currentTimeMillis();

        return new io.pubmed.benchmark.BenchmarkResult(pass, endTime - startTime);
    }

    @io.pubmed.benchmark.BenchmarkStep(order = 7, timeout = 35, description = "Test getImpactFactor(String, int)")
    public BenchmarkResult getImpactFactor() {

        List<Map.Entry<Object[], Double>> cases = deserialize(io.pubmed.benchmark.BenchmarkConstants.TEST_DATA, io.pubmed.benchmark.BenchmarkConstants.getImpactFactor);
        val pass = new AtomicLong();

        val startTime = System.currentTimeMillis();
        cases.parallelStream().forEach(it -> {
            try {
                val args = it.getKey();
                val res = journalService.getImpactFactor((String)args[0], (int)args[1]);
                if (it.getValue() == res) {
                    pass.incrementAndGet();
                } else {
                    log.info("Wrong answer for {}: expected {}, got {}", it.getKey(), it.getValue(), res);
                }
            } catch (Exception e) {
                log.error("Exception thrown for {}", it, e);
            }
        });
        val endTime = System.currentTimeMillis();

        return new io.pubmed.benchmark.BenchmarkResult(pass, endTime - startTime);
    }

    @io.pubmed.benchmark.BenchmarkStep(order = 8, timeout = 35, description = "Test updateJournalName(String, int, String)")
    public BenchmarkResult updateJournalName() {

        List<Map.Entry<Object[], Boolean>> cases = deserialize(io.pubmed.benchmark.BenchmarkConstants.TEST_DATA, io.pubmed.benchmark.BenchmarkConstants.updateJournalName);
        val pass = new AtomicLong();

        val startTime = System.currentTimeMillis();
        cases.parallelStream().forEach(it -> {
            try {
                val args = it.getKey();
                val res = journalService.updateJournalName((Journal)args[0],(int)args[1],(String) args[2], (String) args[3]);
                if (res) {
                    pass.incrementAndGet();
                } else {
                    log.info("Wrong answer for {}: expected {}, got {}", it.getKey(), it.getValue(), res);
                }
            } catch (Exception e) {
                log.error("Exception thrown for {}", it, e);
            }
        });
        val endTime = System.currentTimeMillis();

        return new io.pubmed.benchmark.BenchmarkResult(pass, endTime - startTime);
    }

    @io.pubmed.benchmark.BenchmarkStep(order = 9, timeout = 35, description = "Test getArticleCountByKeywordInPastYears(String, int, String)")
    public BenchmarkResult getArticleCountByKeywordInPastYears() {

        List<Map.Entry<Object[], int[]>> cases = deserialize(io.pubmed.benchmark.BenchmarkConstants.TEST_DATA, io.pubmed.benchmark.BenchmarkConstants.getArticleCountByKeywordInPastYears);
        val pass = new AtomicLong();

        val startTime = System.currentTimeMillis();
        cases.parallelStream().forEach(it -> {
            try {
                val args = it.getKey();
                val res = keywordService.getArticleCountByKeywordInPastYears((String)args[0]);
                if (ArraysEqual(it.getValue() , res)) { //TODO: check the return value
                    pass.incrementAndGet();
                } else {
                    log.info("Wrong answer for {}: expected {}, got {}", it.getKey(), it.getValue(), res);
                }
            } catch (Exception e) {
                log.error("Exception thrown for {}", it, e);
            }
        });
        val endTime = System.currentTimeMillis();

        return new io.pubmed.benchmark.BenchmarkResult(pass, endTime - startTime);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private <T> T deserialize(String... path) {
        val file = Paths.get(config.getDataPath(), path);
        return (T) fury.deserialize(Files.readAllBytes(file));
    }

    private static boolean collectionEquals(Collection<?> expect, Collection<?> actual) {
        return Objects.equals(expect, actual)
                || expect.isEmpty() && Objects.isNull(actual);
    }

    private static boolean longArrayAsSetEquals(long[] expect, long[] actual) {
        if (expect.length != actual.length) {
            return false;
        }
        val expectSet = new HashSet<Long>();
        for (val i : expect) {
            expectSet.add(i);
        }
        for (val i : actual) {
            if (!expectSet.remove(i)) {
                return false;
            }
        }
        return expectSet.isEmpty();
    }

    private static <T> boolean arrayAsSetEquals(T[] expect, T[] actual) {
        if (expect.length != actual.length) {
            return false;
        }
        return Objects.equals(new HashSet<>(Arrays.asList(expect)), new HashSet<>(Arrays.asList(actual)));
    }

    // Only compare the content, not the order
    public static boolean ArraysEqual(int[] array1, int[] array2) {
        // 排序
        Arrays.sort(array1);
        Arrays.sort(array2);

        // 使用 Arrays.equals 比较排序后的数组
        return Arrays.equals(array1, array2);
    }
}
