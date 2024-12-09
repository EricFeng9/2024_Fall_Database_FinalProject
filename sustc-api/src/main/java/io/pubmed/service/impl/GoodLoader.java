package io.pubmed.service.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.sql.*;
import java.net.URL;
import com.alibaba.fastjson2.*;

public class GoodLoader {
    private static final int BATCH_SIZE = 30000;

    private static Connection con = null;
    private static boolean verbose = false;
    private static PreparedStatement[] stmt = new PreparedStatement[13];

    private static int authorid = 1;
    private static int keyid = 1;
    private static int article_idsid = 1;
    private static int grant_zizeng_id = 1;

    private static HashSet<String> a = new HashSet<>();
    private static HashMap<String, Integer> b = new HashMap();
    private static HashMap<String, Integer> c = new HashMap();
    private static HashMap<String, Integer> d = new HashMap();
    private static HashMap<String, Integer> key = new HashMap();
    private static HashMap<String, Integer> grant_unique = new HashMap();
    private static HashSet<String> journalunique = new HashSet<>();
    private static HashSet<String> publication_typeunique = new HashSet<>();

    private static void openDB(String host, String dbname,
                               String user, String pwd) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (Exception e) {
            System.err.println("Cannot find the Postgres driver. Check CLASSPATH.");
            System.exit(1);
        }
        String url = "jdbc:postgresql://" + host + "/" + dbname;
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pwd);
        try {
            con = DriverManager.getConnection(url, props);
            if (verbose) {
                System.out.println("Successfully connected to the database "
                        + dbname + " as " + user);
            }
            con.setAutoCommit(false);
        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }
        try {
            stmt[0] = con.prepareStatement("insert into article(id,title,pub_model,date_created,date_completed)"
                    + " values(?,?,?,?,?)");
            stmt[1] = con.prepareStatement("insert into article_authors(article_id,author_id)"
                    + " values(?,?)");
            stmt[2] = con.prepareStatement("insert into article_grants(article_id,grant_id)"
                    + " values(?,?)");
            stmt[3] = con.prepareStatement("insert into article_keywords(article_id,keyword_id)"
                    + " values(?,?)");
            stmt[4] = con.prepareStatement("insert into article_publication_types(article_id,pub_type_id)"
                    + " values(?,?)");
            stmt[5] = con.prepareStatement("INSERT INTO Authors (author_id,fore_name, last_name, initials, is_collective_name,affiliation) " +
                    "VALUES (?,?,?,?,?,?) ");
            stmt[6] = con.prepareStatement("insert into article_ids(id,article_id,type,identifier)"
                    + " values(?,?,?,?)");
            stmt[7] = con.prepareStatement("insert into grant_info(id,grant_id,acronym,agency,country)"
                    + " values(?,?,?,?,?)");
            stmt[8] = con.prepareStatement("insert into journal(id,country,issn,title,volume,issue)"
                    + " values(?,?,?,?,?,?)");
            stmt[9] = con.prepareStatement("insert into article_journal(journal_id,article_id)"
                    + " values(?,?)");
            stmt[10] = con.prepareStatement("insert into keywords(id,keyword)"
                    + " values(?,?)");
            stmt[11] = con.prepareStatement("insert into publication_types(id,name)"
                    + " values(?,?)");
            stmt[12] = con.prepareStatement("insert into article_references(article_id,reference_id)"
                    + " values(?,?)");
        } catch (SQLException e) {
            System.err.println("Insert statement failed");
            System.err.println(e.getMessage());
            closeDB();
            System.exit(1);
        }
    }

    private static void closeDB() {
        if (con != null) {
            try {
                if (stmt != null) {
                    for (int i = 0; i < stmt.length; i++) {
                        stmt[i].close();
                    }
                }
                con.close();
                con = null;
            } catch (Exception e) {
                System.err.println("Failed to close database connection");
                System.err.println(e.getMessage());
            }
        }
    }

    private static void loadDataArticle(int id, String title, String pub_model, String date_created, String date_completed)
            throws SQLException {
        if (con != null) {
            stmt[0].setInt(1, id);
            stmt[0].setString(2, title);
            stmt[0].setString(3, pub_model);
            // 将日期字符串转换为java.sql.Date
            Date sqlDate = Date.valueOf(date_created);
            stmt[0].setDate(4, sqlDate);
            sqlDate = Date.valueOf(date_completed);
            stmt[0].setDate(5, sqlDate);
            stmt[0].addBatch();
        }
    }

    private static void loadDataAuthor(int id, String fore_name, String last_name, String initials, boolean is_collective_name, String affiliation)
            throws SQLException {
        if (con != null) {
            stmt[5].setInt(1, id);
            stmt[5].setString(2, fore_name);
            stmt[5].setString(3, last_name);
            stmt[5].setString(4, initials);
            stmt[5].setBoolean(5, is_collective_name);
            stmt[5].setString(6, affiliation);
            stmt[5].addBatch();
        }
    }

    private static void loadDataAuthorArticle(int id, int authorid)
            throws SQLException {
        if (con != null) {
            stmt[1].setInt(1, id);
            stmt[1].setInt(2, authorid);
            stmt[1].addBatch();
        }
    }

    private static void loadDataKey(int id, String keyword)
            throws SQLException {
        if (con != null) {
            stmt[10].setInt(1, id);
            stmt[10].setString(2, keyword);
            stmt[10].addBatch();
        }
    }

    private static void loadDataArticleKey(int articleid, int keyid)
            throws SQLException {
        if (con != null) {
            stmt[3].setInt(1, articleid);
            stmt[3].setInt(2, keyid);
            stmt[3].addBatch();
        }
    }

    private static void loadDataJournal(String id, String country, String issn, String title, String volume, String issue)
            throws SQLException {
        if (con != null) {
            stmt[8].setString(1, id);
            stmt[8].setString(2, country);
            stmt[8].setString(3, issn);
            stmt[8].setString(4, title);
            stmt[8].setString(5, volume);
            stmt[8].setString(6, issue);
            stmt[8].addBatch();
        }
    }

    private static void loadDataArticleJournal(int article_id, String journal_id)
            throws SQLException {
        if (con != null) {
            stmt[9].setString(1, journal_id);
            stmt[9].setInt(2, article_id);
            stmt[9].addBatch();
        }
    }

    private static void loadDatapublicationTypes(String pub_id, String name)
            throws SQLException {
        if (con != null) {
            stmt[11].setString(1, pub_id);
            stmt[11].setString(2, name);
        }
        stmt[11].addBatch();
    }

    private static void loadDataArticlePub(int article_id, String pub_id)
            throws SQLException {
        if (con != null) {
            stmt[4].setInt(1, article_id);
            stmt[4].setString(2, pub_id);
            stmt[4].addBatch();
        }
    }

    private static void loadDataArticleIds(int zizengid, int article_id, String type, String identifier)
            throws SQLException {
        if (con != null) {
            stmt[6].setInt(1, zizengid);
            stmt[6].setInt(2, article_id);
            stmt[6].setString(3, type);
            stmt[6].setString(4, identifier);
            stmt[6].addBatch();
        }
    }

    private static void loadDataReference(int article_id, int ref_id)
            throws SQLException {
        if (con != null) {
            stmt[12].setInt(1, article_id);
            stmt[12].setInt(2, ref_id);
            stmt[12].addBatch();
        }
    }

    private static void loadDataGrantInfo(int id, String grant_id, String acronym, String agency, String country)
            throws SQLException {
        if (con != null) {
            stmt[7].setInt(1, id);
            stmt[7].setString(2, grant_id);
            stmt[7].setString(3, acronym);
            stmt[7].setString(4, agency);
            stmt[7].setString(5, country);
            stmt[7].addBatch();

        }
    }

    private static void loadDataArticleGrant(int article_id, int grant_id)
            throws SQLException {
        if (con != null) {
            stmt[2].setInt(1, article_id);
            stmt[2].setInt(2, grant_id);
            stmt[2].addBatch();
        }
    }

    public static void main(String[] args) {
        String fileName;
        //F:\Sustech\24Fall_Database\FinalProject
        fileName = "F:\\Sustech\\24Fall_Database\\FinalProject\\pubmed24n.ndjson";
        Properties properties = new Properties();
        properties.put("host", "localhost");
        properties.put("user", "postgres");
        properties.put("password", "Lekge66811616");
        properties.put("database", "postgres");
        Properties prop = new Properties(properties);
        try (BufferedReader infile
                     = new BufferedReader(new FileReader(fileName))) {
            long start;
            long end;
            String line;
            int id;
            String title;
            String pub_model;
            String date_completed;
            String date_created;
            String fore_name;
            String last_name;
            String initials;
            boolean is_collective_name;
            String affiliation;
            long cnt = 0;
            Integer store;
            Integer store1;
            String journal_id;
            String country;
            String issn;
            String journal_title;
            JSONObject journal_issue;
            String volume;
            String issue;
            JSONObject publication_type;
            String publication_type_id;
            JSONObject article_ids;
            String identifier;
            int reference;
            JSONArray grants;
            JSONObject grant;
            String grant_id;
            String acronym ;
            String agency ;
            String gr_country;
            int count=0;
//             Empty target table
            openDB(prop.getProperty("host"), prop.getProperty("database"),
                    prop.getProperty("user"), prop.getProperty("password"));
            Statement stmt0;
            if (con != null) {
                stmt0 = con.createStatement();
                stmt0.execute("drop table if exists article_authors cascade;");
                stmt0.execute("drop table if exists article_journal cascade;");
                stmt0.execute("drop table if exists article_references cascade;");
                stmt0.execute("drop table if exists article_keywords cascade;");
                stmt0.execute("drop table if exists article_publication_types cascade;");
                stmt0.execute("drop table if exists article_grants cascade;");
                stmt0.execute("drop table if exists article_ids cascade;");
                stmt0.execute("drop table if exists article cascade;");
                stmt0.execute("drop table if exists authors cascade;");
                stmt0.execute("drop table if exists journal cascade;");
                stmt0.execute("drop table if exists keywords cascade;");
                stmt0.execute("drop table if exists publication_types cascade;");
                stmt0.execute("drop table if exists grant_info cascade;");
                stmt0.execute("""
                    CREATE TABLE Article (
                        id             INT PRIMARY KEY CHECK (id BETWEEN 1 AND 99999999),         -- 唯一标识符，1到8位整数
                        title          VARCHAR(1000) NOT NULL,                                    -- 文章标题
                        pub_model      VARCHAR(50)   NOT NULL CHECK (pub_model IN
                                                                     ('Print', 'Print-Electronic', 'Electronic', 'Electronic-Print',
                                                                      'Electronic-eCollection')), -- 出版模式，五种可能的值
                        date_created   DATE          NOT NULL,                                    -- 创建日期
                        date_completed DATE                                                       -- 完成日期，允许为空
                    );
                    CREATE TABLE Journal (
                                             id VARCHAR(20) PRIMARY KEY,                             -- 期刊的唯一ID
                                             country VARCHAR(200) NOT NULL,                          -- 期刊所属国家
                                             issn VARCHAR(9) CHECK (LENGTH(issn) = 9 or issn=''),               -- ISSN编号，8位字符
                                             title VARCHAR(1000) NOT NULL,                             -- 期刊标题
                                             volume VARCHAR(50),                                     -- 期刊卷号，允许为空
                                             issue VARCHAR(50)
                    );
                    
                    CREATE TABLE Article_Journal (
                                                    journal_id varchar(20) references journal(id),
                                                    article_id int references article(id),
                                                    primary key(journal_id,article_id)
                    );
                    CREATE TABLE Authors (
                                             author_id INT PRIMARY KEY,
                                             fore_name VARCHAR(255) DEFAULT '',              -- 个人作者的名字，空值存储为 ''
                                             last_name VARCHAR(255) DEFAULT '',              -- 个人作者的姓氏，空值存储为 ''
                                             initials VARCHAR(10) DEFAULT '',                -- 个人作者的姓名缩写，空值存储为 ''
                                             is_collective_name BOOLEAN DEFAULT FALSE,             -- 是否为团队名称
                                             affiliation text default '':: text not null
                    );
                    
                    -- 唯一性部分索引: collective_name 的唯一性（仅适用于 collective_name 非空记录）
                    CREATE UNIQUE INDEX unique_collective_name
                        ON Authors (last_name)
                        WHERE is_collective_name=true;
                    
                    -- 唯一性部分索引: last_name 的唯一性（仅适用于只有 last_name 的个人作者记录）
                    CREATE UNIQUE INDEX unique_last_name
                        ON Authors (last_name)
                        WHERE fore_name = '' AND initials = ''AND is_collective_name=false;
                    
                    -- 唯一性部分索引: fore_name, last_name, initials 的唯一性（仅适用于完整个人作者记录）
                    CREATE UNIQUE INDEX unique_full_name
                        ON Authors (fore_name, last_name, initials)
                        WHERE fore_name <> '' or initials <> ''and is_collective_name=false;
                    
                    
                    CREATE TABLE article_references (
                                                        article_id INT NOT NULL,
                                                        reference_id INT NOT NULL,
                                                        PRIMARY KEY (article_id, reference_id),
                                                        FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE
                    );
                    CREATE TABLE Article_Authors (
                                                     article_id INT REFERENCES Article(id),                  -- 外键，关联 Article 表
                                                     author_id INT REFERENCES Authors(author_id),                   -- 外键，关联 Authors 表
                                                     PRIMARY KEY (article_id, author_id)                     -- 复合主键，确保每篇文章和作者唯一对应
                    );
                    CREATE TABLE Publication_Types (
                                                       id VARCHAR(20) PRIMARY KEY,                             -- 出版类型的唯一ID
                                                       name VARCHAR(200) NOT NULL                              -- 出版类型名称
                    );
                    CREATE TABLE Article_Publication_Types (
                                                               article_id INT REFERENCES Article(id),                  -- 外键，关联 Article 表
                                                               pub_type_id VARCHAR(20) REFERENCES Publication_Types(id), -- 外键，关联 Publication_Types 表
                                                               PRIMARY KEY (article_id, pub_type_id)                   -- 复合主键
                    );
                    CREATE TABLE Grant_info (
                                                id INT PRIMARY KEY,
                                                grant_id VARCHAR(50) ,                                  -- 资助的ID
                                                acronym VARCHAR(10),                                    -- 资助简称，允许为空
                                                agency VARCHAR(500) NOT NULL,                           -- 资助机构
                                                country VARCHAR(100)                                    -- 资助国家，允许为空
                    );
                    CREATE TABLE Article_Grants (
                                                    article_id INT REFERENCES Article(id),                  -- 外键，关联 Article 表
                                                    grant_id int REFERENCES Grant_info(id),        -- 外键，关联 Grant 表
                                                    PRIMARY KEY (article_id, grant_id)                      -- 复合主键
                    );
                    CREATE TABLE Article_Ids (
                                                 id SERIAL PRIMARY KEY,                                  -- 主键，自动递增
                                                 article_id INT REFERENCES Article(id),                  -- 外键，关联 Article 表
                                                 type VARCHAR(50) NOT NULL,                              -- ID类型（例如 pubmed, doi等）
                                                 identifier VARCHAR(255) NOT NULL                        -- 具体的ID值
                    );
                    CREATE TABLE Keywords (
                                              id SERIAL PRIMARY KEY,                                  -- 关键词的唯一ID
                                              keyword VARCHAR(100) NOT NULL                           -- 关键词内容
                    );
                    CREATE TABLE Article_Keywords (
                                                      article_id INT REFERENCES Article(id),                  -- 外键，关联 Article 表
                                                      keyword_id INT REFERENCES Keywords(id),                 -- 外键，关联 Keywords 表
                                                      PRIMARY KEY (article_id, keyword_id)                    -- 复合主键
                    );
                    
                    -- trigger
                    
                    
                    -- 创建函数检查 date_completed 是否不早于 date_created
                    CREATE OR REPLACE FUNCTION check_date_completed()
                    RETURNS TRIGGER AS $$
                    BEGIN
                        IF NEW.date_completed IS NOT NULL THEN
                            IF NEW.date_completed < NEW.date_created THEN
                                RAISE EXCEPTION 'date_completed cannot be earlier than date_created';
                    END IF;
                    END IF;
                    RETURN NEW;
                    END;
                    $$ LANGUAGE plpgsql;
                    
                    -- 在 Article 表上创建触发器
                    CREATE TRIGGER trg_check_date_completed
                        BEFORE INSERT OR UPDATE ON Article
                                             FOR EACH ROW EXECUTE FUNCTION check_date_completed();
                    
                    -- 创建函数检查 Authors 表的数据一致性
                    CREATE OR REPLACE FUNCTION check_authors_consistency()
                    RETURNS TRIGGER AS $$
                    BEGIN
                        IF NEW.is_collective_name THEN
                            IF NEW.last_name IS NULL OR NEW.last_name = '' THEN
                                RAISE EXCEPTION 'Collective name must be provided when is_collective is TRUE';
                    END IF;
                            IF NEW.fore_name != ''  OR NEW.initials != '' THEN
                                RAISE EXCEPTION 'Fore_name, last_name, and initials must be empty when is_collective is TRUE';
                    END IF;
                    ELSE
                    
                            IF NEW.last_name = '' THEN
                                RAISE EXCEPTION 'Last name must be provided when is_collective is FALSE';
                    END IF;
                    END IF;
                    RETURN NEW;
                    END;
                    $$ LANGUAGE plpgsql;
                    
                    -- 在 Authors 表上创建触发器
                    CREATE TRIGGER trg_check_authors_consistency
                        BEFORE INSERT OR UPDATE ON Authors
                                             FOR EACH ROW EXECUTE FUNCTION check_authors_consistency();
                    
                    -- 创建函数检查 ISSN 的格式
                    CREATE OR REPLACE FUNCTION check_issn_format()
                    RETURNS TRIGGER AS $$
                    BEGIN
                        IF NEW.issn = '' OR NEW.issn ~ '^[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$' THEN
                            RETURN NEW;
                    ELSE
                            RAISE EXCEPTION 'Invalid ISSN format in Journal';
                    END IF;
                    RETURN NEW;
                    END;
                    $$ LANGUAGE plpgsql;
                    -- 在 Journal 表上创建触发器
                    CREATE TRIGGER trg_check_issn_format
                        BEFORE INSERT OR UPDATE ON Journal
                                             FOR EACH ROW EXECUTE FUNCTION check_issn_format();
                    
                    CREATE OR REPLACE VIEW Article_Citations AS
                    SELECT
                        ar.reference_id AS article_id, -- 被引用的文章 ID
                        COUNT(ar.article_id) AS citation_count, -- 被引用的次数
                        EXTRACT(YEAR FROM a.date_created) AS citation_year -- 引用发生的年份
                    FROM article_references ar
                    JOIN Article a ON ar.article_id = a.id -- 引用文章的创建日期
                    GROUP BY ar.reference_id, EXTRACT(YEAR FROM a.date_created)
""");
                stmt0.close();
                con.commit();
            }
            closeDB();
            start = System.currentTimeMillis();
            openDB(prop.getProperty("host"), prop.getProperty("database"),
                    prop.getProperty("user"), prop.getProperty("password"));

            while ((line = infile.readLine()) != null) {
                JSONObject jsonObject = JSON.parseObject(line);
                id = Integer.parseInt(jsonObject.getString("id"));
                title = jsonObject.getString("title");
                pub_model = jsonObject.getString("pub_model");
                JSONObject dateCreated = jsonObject.getJSONObject("date_created");
                int year = dateCreated.getIntValue("year");
                int month = dateCreated.getIntValue("month");
                int day = dateCreated.getIntValue("day");

                // 构造日期字符串
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                date_created = sdf.format(new java.util.Date(year - 1900, month - 1, day)); // 注意月份需要减1，因为java.util.Date的月份是从0开始的
                JSONObject dataCompleted = jsonObject.getJSONObject("date_created");
                year = dateCreated.getIntValue("year");
                month = dateCreated.getIntValue("month");
                day = dateCreated.getIntValue("day");

                // 构造日期字符串
                sdf = new SimpleDateFormat("yyyy-MM-dd");
                date_completed = sdf.format(new java.util.Date(year - 1900, month - 1, day)); // 注意月份需要减1，因为java.util.Date的月份是从0开始的
                loadDataArticle(id, title, pub_model, date_created, date_completed);
                cnt++;
                //------------------------------------------------------------add author
                JSONArray authors = jsonObject.getJSONArray("author");
                if (authors != null) {
                    a.clear();
                    for (int i = 0; i < authors.size(); i++) {
                        JSONObject author = authors.getJSONObject(i);
                        is_collective_name = author.containsKey("collective_name");
                        if (is_collective_name) {
                            last_name = author.getString("collective_name");
                            store1 = b.get(last_name);
                            store = b.put(last_name, authorid);
                            if (store != null) {
                                b.put(last_name, store1);
                                if (!a.contains(last_name)) {
                                    loadDataAuthorArticle(id, store);
                                    a.add(last_name);
                                }
                            } else {
                                if (!a.contains(last_name)) {
                                    loadDataAuthor(authorid, "", last_name, "", true, "");
                                    loadDataAuthorArticle(id, authorid++);
                                    a.add(last_name);
                                }
                            }
                        } else {
                            last_name = author.getString("last_name");
                            if (author.containsKey("fore_name")) {
                                fore_name = author.getString("fore_name");
                            } else {
                                fore_name = "";
                            }
                            if (author.containsKey("affiliation")) {
                                affiliation = author.getString("affiliation").replace("[", "").replace("]", "");
                            } else {
                                affiliation = "";
                            }
                            if (author.containsKey("initials")) {
                                initials = author.getString("initials");
                            } else {
                                initials = "";
                            }
                            if (fore_name == "" && initials == "") {
                                store1 = c.get(last_name);
                                store = c.put(last_name, authorid);
                                if (store != null) {
                                    c.put(last_name, store1);
                                    if (!a.contains(last_name)) {
                                        loadDataAuthorArticle(id, store);
                                        a.add(last_name);
                                    }
                                } else {
                                    if (!a.contains(last_name)) {
                                        loadDataAuthor(authorid, "", last_name, "", false, affiliation);
                                        loadDataAuthorArticle(id, authorid++);
                                        a.add(last_name);
                                    }
                                }
                            } else {
                                store1 = d.get(fore_name + "-" + last_name + "-" + initials);
                                store = d.put(fore_name + "-" + last_name + "-" + initials, authorid);
                                if (store != null) {
                                    d.put(fore_name + "-" + last_name + "-" + initials, store1);
                                    if (!a.contains(fore_name + "-" + last_name + "-" + initials)) {
                                        loadDataAuthorArticle(id, store);
                                        a.add(fore_name + "-" + last_name + "-" + initials);
                                    }
                                } else {
                                    if (!a.contains(fore_name + "-" + last_name + "-" + initials)) {
                                        loadDataAuthor(authorid, fore_name, last_name, initials, false, affiliation);
                                        loadDataAuthorArticle(id, authorid++);
                                        a.add(fore_name + "-" + last_name + "-" + initials);
                                    }
                                }
                            }
                        }
                    }
                }
                //--------------------------------------------------------add keywords
                if (jsonObject.containsKey("keywords")) {
                    a.clear();
                    JSONArray keywords = jsonObject.getJSONArray("keywords");
                    for (int i = 0; i < keywords.size(); i++) {
                        String keyword = keywords.getString(i);
                        store1 = key.get(keyword);
                        store = key.put(keyword, keyid);
                        if (store != null) {
                            key.put(keyword, store1);
                            if (!a.contains(keyword)) {
                                loadDataArticleKey(id, store);
                                a.add(keyword);
                            }
                        } else {
                            if (!a.contains(keyword)) {
                                loadDataKey(keyid, keyword);
                                loadDataArticleKey(id, keyid++);
                                a.add(keyword);
                            }
                        }
                    }
                }
                //--------------------------------------------------------------------add journal
                a.clear();
                JSONObject journal = jsonObject.getJSONObject("journal");
                journal_id = journal.getString("id");
                country = journal.getString("country");
                issn = journal.getString("issn");
                journal_title = journal.getString("title");
                journal_issue = journal.getJSONObject("journal_issue");
                volume = journal_issue.getString("volume");
                issue = journal_issue.getString("issue");
                if (journalunique.add(journal_id)) {
                    loadDataJournal(journal_id, country, issn, journal_title, volume, issue);
                    loadDataArticleJournal(id, journal_id);
                } else {
                    loadDataArticleJournal(id, journal_id);
                }
                //------------------------------------------------------------------add publication_type
                if (jsonObject.containsKey("publication_types")) {
                    JSONArray publication_types = jsonObject.getJSONArray("publication_types");
                    for (int i = 0; i < publication_types.size(); i++) {
                        publication_type = publication_types.getJSONObject(i);
                        publication_type_id = publication_type.getString("id");
                        String pub_name = publication_type.getString("name");
                        if (publication_typeunique.add(publication_type_id)) {
                            loadDatapublicationTypes(publication_type_id, pub_name);
                            loadDataArticlePub(id, publication_type_id);
                        } else {
                            loadDataArticlePub(id, publication_type_id);
                        }
                    }
                }
                //----------------------------------------------------------------add article_ids

                if (jsonObject.containsKey("article_ids")) {
                    a.clear();
                    JSONArray article_idss = jsonObject.getJSONArray("article_ids");
                    for (int i = 0; i < article_idss.size(); i++) {
                        article_ids = article_idss.getJSONObject(i);
                        identifier = article_ids.getString("id");
                        if (identifier == null) {
                            identifier = "";
                        }
                        String type = article_ids.getString("ty");
                        loadDataArticleIds(article_idsid++, id, type, identifier);
                    }
                }
//------------------------------------------------------------------------------------add reference

                if (jsonObject.containsKey("references")) {
                    JSONArray references = jsonObject.getJSONArray("references");
                    for (int i = 0; i < references.size(); i++) {
                        reference = references.getIntValue(i);
                        loadDataReference(id, reference);
                    }
                }
                //----------------------------------------------------------------------add grant
                if (jsonObject.containsKey("grant")) {
                    a.clear();
                    grants = jsonObject.getJSONArray("grant");
                    for (int i = 0; i < grants.size(); i++) {
                        grant = grants.getJSONObject(i);
                        grant_id = grant.getString("id");
                        acronym = grant.getString("acronym");
                        agency = grant.getString("agency");
                        gr_country = grant.getString("country");
                        store1 = grant_unique.get(agency);
                        store = grant_unique.put(agency, grant_zizeng_id);
                        if (store != null) {
                            grant_unique.put(agency, store1);
                            if (!a.contains(agency)) {
                                loadDataArticleGrant(id, store);
                                a.add(agency);
                            }
                        } else {
                            if (!a.contains(agency)) {
                                loadDataGrantInfo(grant_zizeng_id, grant_id, acronym, agency, gr_country);
                                loadDataArticleGrant(id, grant_zizeng_id++);
                                a.add(agency);
                            }
                        }
                    }


                }
                if (cnt % BATCH_SIZE == 0) {
                    try {
                        stmt[0].executeBatch();
                        stmt[5].executeBatch();
                        stmt[1].executeBatch();
                        stmt[8].executeBatch();
                        stmt[9].executeBatch();
                        stmt[10].executeBatch();
                        stmt[3].executeBatch();
                        stmt[11].executeBatch();
                        stmt[4].executeBatch();
                        stmt[6].executeBatch();
                        stmt[7].executeBatch();
                        stmt[2].executeBatch();
                        stmt[12].executeBatch();
                        con.commit();
                    } catch (BatchUpdateException bue) {
                        System.err.println("BatchUpdateException: " + bue.getMessage());
                        SQLException nextException = bue.getNextException();
                        while (nextException != null) {
                            System.err.println("SQLState: " + nextException.getSQLState());
                            System.err.println("Error Code: " + nextException.getErrorCode());
                            System.err.println("Message: " + nextException.getMessage());
                            nextException = nextException.getNextException();
                        }
                        con.rollback();
                        System.exit(1);
                    } catch (SQLException se) {
                        System.err.println("SQLException: " + se.getMessage());
                        con.rollback();
                        System.exit(1);
                    }
                }
                if(cnt % 1000 == 0){
                    long current = System.currentTimeMillis();
                    System.out.println(cnt+" "+(cnt * 1000) / (current - start));
                }

            }
            con.commit();
            for (PreparedStatement preparedStatement : stmt) {
                preparedStatement.close();
            }
            closeDB();
            end = System.currentTimeMillis();
            System.out.println("Total Loading time : "+(end-start)*1.0/1000);
            System.out.println(cnt + " records successfully loaded");
            System.out.println("Loading speed : "
                    + (cnt * 1000) / (end - start)
                    + " records/s");
        } catch (SQLException se) {
            System.err.println("SQL error: " + se.getMessage());
            try {
                con.rollback();
                for (PreparedStatement preparedStatement : stmt) {
                    preparedStatement.close();
                }
            } catch (Exception ignored) {
            }
            closeDB();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Fatal error: " + e.getMessage());
            try {
                con.rollback();
                for (PreparedStatement preparedStatement : stmt) {
                    preparedStatement.close();
                }
            } catch (Exception ignored) {
            }
            closeDB();
            System.exit(1);
        }
        closeDB();
    }
}