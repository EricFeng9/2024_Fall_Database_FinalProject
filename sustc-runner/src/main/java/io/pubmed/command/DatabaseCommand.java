package io.pubmed.command;

import io.pubmed.benchmark.BenchmarkConfig;
import io.pubmed.benchmark.BenchmarkConstants;
import io.pubmed.benchmark.BenchmarkService;
import io.pubmed.benchmark.BenchmarkResult;
import io.pubmed.dto.Author;
import io.pubmed.dto.Journal;
import io.pubmed.dto.JournalIssue;
import io.pubmed.service.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

import io.fury.ThreadSafeFury;
import io.pubmed.dto.Article;

@Slf4j
@ShellComponent
@ConditionalOnBean(DatabaseService.class)
public class DatabaseCommand {

    @Autowired
    private BenchmarkConfig config;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private BenchmarkService benchmarkService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private GrantService grantService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private JournalService journalService;

    @Autowired
    private KeywordService keywordService;

    @Autowired
    private ThreadSafeFury fury;


    @ShellMethod(key = "db groupmember", value = "List group members")
    public List<Integer> listGroupMembers() {
        return databaseService.getGroupMembers();
    }


    @ShellMethod(key = "db truncate", value = "Truncate tables")
    public void truncate() {
        databaseService.truncate();
    }

    @ShellMethod(key = "db sum", value = "Demonstrate using DataSource")
    public Integer sum(int a, int b) {
        return databaseService.sum(a, b);
    }


    @ShellMethod(key = "db gen", value = "Generate test instance ")
    public void generateData() throws ParseException {

        //---------------------------------------------------------------------ArticleService--------------------------------------------------------------------------------
        log.info("gen for :  articleService.getArticleCitationsByYear");
        List<Map.Entry<Object[], Integer>> list1 = new ArrayList<>();
        List<Object[]> input = new ArrayList<>();
        input.add(new Object[]{2985470,2023});
        input.add(new Object[]{2535461,2023});
        input.add(new Object[]{942051,2023});
        input.add(new Object[]{518835,2022});

        for (Object[] args : input){
            var res = articleService.getArticleCitationsByYear((int)args[0], (int)args[1]);
            log.info("answer for getArticleCitationsByYear:  got {}", res);
            Map.Entry<Object[], Integer> entry = new AbstractMap.SimpleEntry<>(args, res);
            list1.add(entry);
        }
        serialize(list1, io.pubmed.benchmark.BenchmarkConstants.TEST_DATA, io.pubmed.benchmark.BenchmarkConstants.getArticleCitationsByYear);

        log.info("gen for :  articleService.addArticleAndUpdateIF");
        List<Map.Entry<Object[], Double>> list2 = new ArrayList<>();
        List<Object[]> input2 = new ArrayList<>();
        Article article1 = new Article();
        article1.setId(1);
        article1.setTitle("Mechanisms of G protein-coupled receptor signaling in drug development");
        article1.setPub_model("Print");
        Journal journal = new Journal();
        journal.setTitle("Molecular pharmacology");
        article1.setJournal(journal);
        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        Date dateCreated = dateFormat1.parse("2023-01-18");
        java.sql.Date sqlDate1 = new java.sql.Date(dateCreated.getTime());
        article1.setCreated(sqlDate1);
        Date dateCompeleted = dateFormat1.parse("2023-12-18");
        java.sql.Date sqlDate2 = new java.sql.Date(dateCompeleted.getTime());
        article1.setCompleted(sqlDate2);
        input2.add(new Object[]{article1});

        for (Object[] args : input2){
            var res = articleService.addArticleAndUpdateIF((Article)args[0]);
            log.info("answer for getArticleCitationsByYear:  got {}", res);
            Map.Entry<Object[], Double> entry = new AbstractMap.SimpleEntry<>(args, res);
            list2.add(entry);
        }
        serialize(list2, io.pubmed.benchmark.BenchmarkConstants.TEST_DATA, io.pubmed.benchmark.BenchmarkConstants.addArticleAndUpdateIF);
//
        //---------------------------------------------------------------------GrantService---------------------------------------------------------------------
        log.info("gen for :  grantService.getCountryFundPapers");
        List<Map.Entry<Object[], int[]>> list3 = new ArrayList<>();
        List<Object[]> input3 = new ArrayList<>();
        input3.add(new Object[]{"Canada"});

        for (Object[] args : input3){
            var res = grantService.getCountryFundPapers((String) args[0]);
            log.info("answer for getCountryFundPapers:  got {}", res);

            Map.Entry<Object[], int[]> entry = new AbstractMap.SimpleEntry<>(args, res);

            list3.add(entry);
        }

        serialize(list3, io.pubmed.benchmark.BenchmarkConstants.TEST_DATA, io.pubmed.benchmark.BenchmarkConstants.getCountryFundPapers);

        //---------------------------------------------------------------------AuthorService---------------------------------------------------------------------
        log.info("gen for :  authorService.getArticlesByAuthorSortedByCitations");
        List<Map.Entry<Object[], int[]>> list4 = new ArrayList<>();
        List<Object[]> input4 = new ArrayList<>();
        Author author1 = new Author();
//        author1.setInitials("AB");
        author1.setFore_name("H");
        author1.setLast_name("Nakajima");
//        author1.setCollective_name("false");
        input4.add(new Object[]{author1});
        for (Object[] args : input4){
//            String s1 = ((Author) args[0]).getLast_name();
            var res = authorService.getArticlesByAuthorSortedByCitations((Author) args[0]);

            log.info("answer for getArticlesByAuthorSortedByCitations:  got {}", res);

            Map.Entry<Object[], int[]> entry = new AbstractMap.SimpleEntry<>(args, res);

            list4.add(entry);
        }
        serialize(list4, io.pubmed.benchmark.BenchmarkConstants.TEST_DATA, BenchmarkConstants.getArticlesByAuthorSortedByCitations);


        log.info("gen for :  authorService.getJournalWithMostArticlesByAuthor");
        List<Map.Entry<Object[], String>> list5 = new ArrayList<>();
        List<Object[]> input5 = new ArrayList<>();
        input5.add(new Object[]{author1});
        for (Object[] args : input5){
            var res = authorService.getJournalWithMostArticlesByAuthor((Author) args[0]);
            log.info("answer for getJournalWithMostArticlesByAuthor:  got {}", res);

            Map.Entry<Object[], String> entry = new AbstractMap.SimpleEntry<>(args, res);

            list5.add(entry);
        }
        serialize(list5, io.pubmed.benchmark.BenchmarkConstants.TEST_DATA, BenchmarkConstants.getJournalWithMostArticlesByAuthor);


//        log.info("gen for :  authorService.getMinArticlesToLinkAuthors");
//        List<Map.Entry<Object[], Integer>> list6 = new ArrayList<>();
//        List<Object[]> input6 = new ArrayList<>();
//        Author author2 = new Author();
//        author2.setInitials("KE");
//        author2.setFore_name("K E");
//        author2.setLast_name("McMartin");
//        author2.setCollective_name("false");
//        input6.add(new Object[]{author1, author2});
//        for (Object[] args : input6){
//            var res = authorService.getMinArticlesToLinkAuthors((Author) args[0], (Author) args[1]);
//            log.info("answer for getMinArticlesToLinkAuthors:  got {}", res);
//
//            Map.Entry<Object[], Integer> entry = new AbstractMap.SimpleEntry<>(args, res);
//
//            list6.add(entry);
//        }
//        serialize(list6, io.pubmed.benchmark.BenchmarkConstants.TEST_DATA, BenchmarkConstants.getMinArticlesToLinkAuthors);

        //---------------------------------------------------------------------JournalService---------------------------------------------------------------------
        log.info("gen for :  journalService.getImpactFactor");
        List<Map.Entry<Object[], Double>> list7 = new ArrayList<>();
        List<Object[]> input7 = new ArrayList<>();
//        Journal journal1 = new Journal("0151424", "United States", "0006-2944", "Biochemical medicine", new JournalIssue("13", "2"));
        input7.add(new Object[]{"Molecular pharmacology", 2023});
        for (Object[] args : input7){
            var res = journalService.getImpactFactor((String) args[0], (int) args[1]);
            log.info("answer for getImpactFactor:  got {}", res);

            Map.Entry<Object[], Double> entry = new AbstractMap.SimpleEntry<>(args, res);

            list7.add(entry);
        }
        serialize(list7, io.pubmed.benchmark.BenchmarkConstants.TEST_DATA, BenchmarkConstants.getImpactFactor);

        log.info("gen for :  journalService.updateJournalName");
        List<Map.Entry<Object[], Boolean>> list8 = new ArrayList<>();
        List<Object[]> input8 = new ArrayList<>();
        journal = new Journal();
        journal.setId("0151424");
        journal.setTitle("Biochemical medicine");
        input8.add(new Object[]{journal, 999,"Biochemical medicine NEW", "0000000"});
        for (Object[] args : input8){
            var res = journalService.updateJournalName((Journal)args[0],(int)args[1],(String) args[2], (String) args[3]);
            log.info("answer for updateJournalName:  got {}", res);

            Map.Entry<Object[], Boolean> entry = new AbstractMap.SimpleEntry<>(args, res);

            list8.add(entry);
        }
        serialize(list8, io.pubmed.benchmark.BenchmarkConstants.TEST_DATA, BenchmarkConstants.updateJournalName);

        //---------------------------------------------------------------------KeywordService---------------------------------------------------------------------
        log.info("gen for :  keywordService.getArticleCountByKeywordInPastYears");
        List<Map.Entry<Object[], int[]>> list9 = new ArrayList<>();
        List<Object[]> input9 = new ArrayList<>();
        input9.add(new Object[]{"Biology"});
        for (Object[] args : input9){
            var res = keywordService.getArticleCountByKeywordInPastYears((String) args[0]);
            log.info("answer for getArticleCountByKeywordInPastYears:  got {}", res);

            Map.Entry<Object[], int[]> entry = new AbstractMap.SimpleEntry<>(args, res);

            list9.add(entry);
        }
        serialize(list9, io.pubmed.benchmark.BenchmarkConstants.TEST_DATA, BenchmarkConstants.getArticleCountByKeywordInPastYears);
    }

    @ShellMethod(key = "db test", value = "Test generate data")
    public void test_data() {

        List<Map.Entry<Object[], int[]>> cases = deserialize(io.pubmed.benchmark.BenchmarkConstants.TEST_DATA, io.pubmed.benchmark.BenchmarkConstants.getCountryFundPapers);
        val pass = new AtomicLong();

        val startTime = System.currentTimeMillis();
        cases.parallelStream().forEach(it -> {
            try {
                val args = it.getKey();
                var res = grantService.getCountryFundPapers((String)args[0]);
                if (compareIntArraysUnordered(it.getValue(), res)) { //it.getValue() == res
                    pass.incrementAndGet();
                } else {
                    log.info("Wrong answer for {}: expected {}, got {}", it.getKey(), it.getValue(), res);
                }
            } catch (Exception e) {
                log.error("Exception thrown for {}", it, e);
            }
        });
        log.info("Right ans");
        val endTime = System.currentTimeMillis();

        //return new io.pubmed.benchmark.BenchmarkResult(pass, endTime - startTime);

    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private <T> T deserialize(String... path) {
        val file = Paths.get(config.getDataPath(), path);
        return (T) fury.deserialize(Files.readAllBytes(file));
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> void  serialize(T object, String... path) {
        // 获取文件路径
        var file = Paths.get(config.getDataPath(), path);

        // 序列化对象
        byte[] serializedData = fury.serialize(object);

        // 确保目录存在
        Files.createDirectories(file.getParent());

        // 将序列化后的数据写入文件
        Files.write(file, serializedData);

        log.info("serialize path {}", file);
    }

    public static boolean compareIntArraysUnordered(int[] array1, int[] array2) {
        // 将 int[] 转换为 Set<Integer>，自动去重并忽略顺序
        Set<Integer> set1 = new HashSet<>();
        for (int num : array1) {
            set1.add(num);
        }

        Set<Integer> set2 = new HashSet<>();
        for (int num : array2) {
            set2.add(num);
        }

        // 比较两个集合
        return set1.equals(set2);
    }

}
