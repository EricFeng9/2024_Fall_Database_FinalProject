package io.pubmed.service;

import io.pubmed.dto.Article;
import io.pubmed.dto.Author;
import io.pubmed.dto.Journal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.AbstractResource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

@Service
@Slf4j
public class Manager {
    @Autowired
    private DatabaseService databaseService;

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
    private DataSource dataSource;
    private String userName ;
    private int role = -1;//-1是没登陆
    /*(1, 'Admin'),--管理员，不设限制
      (2, 'Author'),--作者，个别接口只能操作自己的文章，其余也不设限制
      (3, 'Visitor');--只能使用查询相关的接口，不能进行更新等操作*/

    public void runManager (DatabaseService databaseService,
    ArticleService articleService,
    GrantService grantService,
    AuthorService authorService,
    JournalService journalService,
    KeywordService keywordService
    ) throws ParseException {
        System.out.println("数据库管理系统启动中");
        System.out.println("配置接口");
        this.databaseService = databaseService;
        this.dataSource = databaseService.getDataSource();
        this.articleService = articleService;
        this.grantService = grantService;
        this.authorService = authorService;
        this.journalService = journalService;
        this.keywordService = keywordService;
        System.out.println("接口配置完毕");
        System.out.println("数据库管理系统启动完毕");
        print_MainMenu();
        Scanner in = new Scanner(System.in);
        while (true){
            String input = in.next();
            if (input.equals("1")){
                register_User();
                print_MainMenu();
            }else if (input.equals("2")){
                if (logIn_User()==-1){
                    print_MainMenu();
                }else {
                   if (role == 1){
                       admin_Menu();
                       print_MainMenu();
                   }//todo role==2 role==3
                };
            }else if (input.equals("3")){
                System.out.println("!!!欢迎下次使用!!!");
                break;
            }else {
                System.out.println("非法的操作，请重新输入");
            }
        }
    }
    public void admin_Menu() throws ParseException {
        Scanner in = new Scanner(System.in);
        print_adminMenu();
        while (true){
            String inputS = in.next();
            if (inputS.equals("1")){
                service_getArticleCitationsByYear();
                print_adminMenu();
            }else if (inputS.equals("2")){
                service_addArticleAndUpdateIF();
                print_adminMenu();
            }else if (inputS.equals("3")){
                service_getArticlesByAuthorSortedByCitations();
                print_adminMenu();
            }else if (inputS.equals("-1")){
                return;
            }
        }
    }
    private void print_adminMenu(){
        System.out.println("------------------------------");//30个-
        System.out.println("------欢迎来到数据库管理系统-----");
        System.out.println("------------------------------");//30个-
        System.out.println("------当前身份为【管理员】--------");
        System.out.println("------------------------------");//30个-
        System.out.println("------请输入数据进行对应操作-----");
        System.out.println("------------------------------");//30个-
        System.out.println("------//Article Service//-----");
        System.out.println("--【1】根据年份获取文章引用次数----");
        System.out.println("--【2】插入新文章并更新影响因子----");
        System.out.println("------//Author Service//------");
        System.out.println("-【3】根据引用次数获取某作者所有文章-");
        System.out.println("-【4】获取某作者发布文章最多的期刊--");
        System.out.println("------//Database Service//----");
        System.out.println("--【5】返回小组成员列表----------");
        System.out.println("------//Grant Service//-------");
        System.out.println("--【6】获取某国资助的所有文章-----");
        System.out.println("------//Journal Service//-----");
        System.out.println("--【7】获取某期刊的影响因子-------");
        System.out.println("------//Keyword Service//-----");
        System.out.println("--【8】获取含某关键词的文章数量----");
        System.out.println("-------------------------------");//30个-
        System.out.println("------【-1】退出登陆-------------");
        System.out.println("-------------------------------");//30个-
    }
    public void print_MainMenu(){
        System.out.println("------------------------------");//30个-
        System.out.println("------欢迎来到数据库管理系统-----");
        System.out.println("------------------------------");//30个-
        System.out.println("------请输入数据进行对应操作-----");
        System.out.println("------------------------------");//30个-
        System.out.println("------【1】新用户注册-----------");
        System.out.println("------【2】用户登陆-------------");
        System.out.println("------【3】退出系统-------------");
        System.out.println("------------------------------");//30个-
        System.out.println("------------------------------");//30个-
    }
    public int logIn_User(){
        Scanner in = new Scanner(System.in);
        while (true){
            System.out.println("------登陆，在任意过程中输入-1即可退出------");
            System.out.println("请输入用户id:");
            int id = in.nextInt();
            if (id == -1){
                return -1;//返回-1为非正常退出
            }
            System.out.println("请输入用户密码:");
            String password = in.next();
            if (password.equals("-1")){
                return -1;
            }
            if (!SQL_checkUserExi(id)){
                System.out.println("当前用户或密码错误，请重新输入");
            }else {
                if (SQL_checkUserPassword(id,password)){
                    //登陆成功，获取当前账户角色，名称
                    System.out.println("登陆成功");
                    role = SQL_getUserRole(id);
                    userName = SQL_getUserName(id);
                    String roleLabel="空";
                    if (role == 1){
                        roleLabel="管理员";
                    }if (role == 2){
                        roleLabel="作者";
                    }if (role == 3){
                        roleLabel="游客";
                    }
                    System.out.println("登陆成功,当前id:"+id+"|当前用户名:"+userName+"|当前身份:"+roleLabel);
                    break;
                }else {
                    System.out.println("当前用户或密码错误，请重新输入");
                }
            }

        }
        return 0;
    }
    public void register_User(){
        Scanner in = new Scanner(System.in);
        while (true){
            System.out.println("------注册新用户，在任意过程中输入-1即可退出------");
            System.out.println("请输入用户id:");
            int id = in.nextInt();
            if (id == -1){
                break;
            }
            while (SQL_checkUserExi(id)){
                System.out.println("当前注册的id:"+id+"与已有的账户id冲突，请重新选择");
                id = in.nextInt();
                if (id == -1){
                    break;
                }
            }
            if (id==-1){
                break;
            }

            System.out.println("请输入用户名:");
            String userName = in.next();
            if (userName.equals("-1")){
                break;
            }

            System.out.println("请输入密码:");
            String password = in.next();
            if (password.equals("-1")){
                break;
            }


            System.out.println("请输入对应数字选择你的用户类型：\n【1】管理员 【2】作者 【3】游客");
            int role_id = in.nextInt();
            if (role_id == -1){
                break;
            }

            int authorId = -1;
            while (role_id<1 || role_id >3 || role_id ==2){
                if (role_id ==2){
                    System.out.println("您选择了【2】作者，请输入您的作者id进行验证:");
                    authorId = in.nextInt();
                    if (authorId ==-1){
                        role_id=-1;
                        break;
                    }else {
                        if(SQL_checkAuthorId(authorId)){
                            //如能找到对应的authorid，则注册的时候插入user表格中
                            break;
                        }else{
                            System.out.println("数据库中找不到对应的作者id，请重新选择身份");
                            role_id = 0;
                            continue;
                        }
                    }
                }else if (role_id == 0){
                    System.out.println("请输入对应数字选择你的用户类型：\n【1】管理员 【2】作者 【3】游客");
                    role_id = in.nextInt();
                    if (role_id == -1){
                        break;
                    }
                }else {
                    System.out.println("输入的用户类型不合法，请重新选择：\n【1】管理员 【2】作者 【3】游客");
                    role_id = in.nextInt();
                    if (role_id == -1){
                        break;
                    }
                }
            }
            if (role_id == -1){
                break;
            }



            String roleLabel="空";
            if (role_id == 1){
                roleLabel="管理员";
            }if (role_id == 2){
                roleLabel="作者";
            }if (role_id == 3){
                roleLabel="游客";
            }

            System.out.println("请确认您的用户信息:"+"【id:"+id+"】"+"【用户名:"+userName+"】"+"【当前用户类型:"+roleLabel+"】\n输入-1返回登陆界面，输入-2重新输入用户信息，输入任意值则执行注册");
            String info = in.next();
            if (info.equals("-1")){
                break;
            }else if (info.equals("-2")){
                continue;
            }

            if (SQL_createUser(id,userName,password,role_id)){
                SQL_setUserAuthorId(authorId,id);
                System.out.println("用户注册成功，id:"+id+"\n输入-1并回车返回登陆界面，输入任意值则继续注册：");
                String info2 = in.next();
                if (info2.equals("-1")){
                    break;
                }
            }else {
                System.out.println("当前用户注册失败");
            }

        }
    }
    public int service_getArticlesByAuthorSortedByCitations(){
        Scanner in = new Scanner(System.in);
        System.out.println("------------------------------");//30个-
        System.out.println("------//Author Service//-----");
        System.out.println("-----根据年份获取文章引用次数-----");
        System.out.println("-----(在任意时刻输入-1返回)------");
        System.out.println("------------------------------");//30个-
        while (true){
            Author authorTest = new Author();
            System.out.println("请输入作者的fore name:");
            String foreName = in.next();
            if (foreName.equals("-1")){
                break;
            }
            System.out.println("请输入作者的last name:");
            String lastName = in.next();
            if (lastName.equals("-1")){
                break;
            }
            authorTest.setFore_name(foreName);
            authorTest.setLast_name(lastName);
            int[] temp = authorService.getArticlesByAuthorSortedByCitations(authorTest);
            System.out.println("【Author Service】:查询结果为"+ Arrays.toString(temp));
            System.out.println("【Author Service】:输入任意值进行查询，输入-1返回上一层");
            String input = in.next();
            if (input.equals("-1")){
                break;
            }
        }
        return 0;
    }
    public int service_addArticleAndUpdateIF() throws ParseException {
        Scanner in = new Scanner(System.in);
        System.out.println("------------------------------");//30个-
        System.out.println("------//Article Service//-----");
        System.out.println("-----插入新文章并更新影响因子-----");
        System.out.println("-----(在任意时刻输入-1返回)------");
        System.out.println("------------------------------");//30个-
        while (true){
            Article article1 = new Article();
            System.out.println("请输入文章id:");
            int article_id = in.nextInt();
            if (article_id==-1){
                break;
            }
            article1.setId(article_id);
            System.out.println("请输入文章标题:");
            in.nextLine();//用来读掉\n
            String title = in.nextLine();
            article1.setTitle(title);
            String pub_model="-1";

            while (true){
                System.out.println("请选择文章的Pub_mode");
                System.out.println("【1】Print");
                System.out.println("【2】Print-Electronic");
                System.out.println("【3】Electronic");
                System.out.println("【4】Electronic-Print");
                System.out.println("【5】Electronic-eCollection");
                int choice = in.nextInt();
                if (choice==1){
                    pub_model = "Print";
                    break;
                }else if (choice==2){
                    pub_model = "Print-Electronic";
                    break;
                }else if (choice==3){
                    pub_model = "Electronic";
                    break;
                }else if (choice==4){
                    pub_model = "Electronic-Print";
                    break;
                }else if (choice==5){
                    pub_model = "Electronic-eCollection";
                    break;
                }else if (choice == -1){
                    break;
                }else {
                    System.out.println("非法的输入，请重新选择，或输入-1返回上层菜单");
                }
            }
            if (pub_model.equals("-1")){
                break;
            }
            article1.setPub_model(pub_model);

            Journal journal = new Journal();
            System.out.println("请输入期刊标题:");
            in.nextLine();//用来读掉\n
            String journalTitle = in.nextLine();
            if (journalTitle.equals("-1")){
                break;
            }
            journal.setTitle(journalTitle);
            article1.setJournal(journal);

            System.out.println("请输入文章创建日期：(格式yyyy-MM-dd)");
            String date_create = in.next();
            if (date_create.equals("-1")){
                break;
            }
            System.out.println("请输入文章完成日期：(格式yyyy-MM-dd)");
            String date_completed = in.next();
            if (date_completed.equals("-1")){
                break;
            }

            DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
            Date dateCreated = dateFormat1.parse(date_create);
            java.sql.Date sqlDate1 = new java.sql.Date(dateCreated.getTime());
            article1.setCreated(sqlDate1);
            Date dateCompeleted = dateFormat1.parse(date_completed);
            java.sql.Date sqlDate2 = new java.sql.Date(dateCompeleted.getTime());
            article1.setCompleted(sqlDate2);

            double result = articleService.addArticleAndUpdateIF(article1);
            System.out.println("【Article Service】:文章"+article1.getTitle()+"更新后的影响因子为"+result);
            System.out.println("【Article Service】:输入任意值进行查询，输入-1返回上一层");
            String input = in.next();
            if (input.equals("-1")){
                break;
            }
        }
        return 0;
    }
    public int service_getArticleCitationsByYear(){
        //todo 更新完接口再检查
        Scanner in = new Scanner(System.in);
        System.out.println("------------------------------");//30个-
        System.out.println("------//Article Service//-----");
        System.out.println("-----根据年份获取文章引用次数-----");
        System.out.println("-----(在任意时刻输入-1返回)------");
        System.out.println("------------------------------");//30个-
        while (true){
            System.out.println("请输入文章id:");
            int article_id = in.nextInt();
            if (article_id==-1){
                break;
            }
            System.out.println("请输入要查询的年份:");
            int year = in.nextInt();
            if (year == -1){
                break;
            }
            int result = articleService.getArticleCitationsByYear(article_id,year);
            System.out.println("【Article Service】:查询结果为"+result);
            System.out.println("【Article Service】:输入任意值进行查询，输入-1返回上一层");
            String input = in.next();
            if (input.equals("-1")){
                break;
            }
        }
        return 0;
    }
    public void SQL_setUserAuthorId(int authorid,int userid){
        String sql = "update users set author_id = ? where user_id =?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1,authorid);
            stmt.setInt(2,userid);
            log.info(authorid+" " +userid);
            int rs = stmt.executeUpdate();
            if (rs==0){
                log.info("SQL_setUserAuthorId设置失败");
            }else return;
        } catch (SQLException e) {
            log.info("莫名其妙失败了SQL_setUserAuthorId:" + e.toString());
            return;
        }
    }
    public int SQL_getUserRole(int id){
        String sql = "select role_id from users where user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1,id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()){
                return rs.getInt("role_id");
            }else return -1;
        } catch (SQLException e) {
            log.info("莫名其妙失败了SQL_getUserRole:" + e.toString());
            return -1;
        }
    }
    public String SQL_getUserName(int id){
        String sql = "select username from users where user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1,id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()){
                return rs.getString("username");
            }else return "-1";
        } catch (SQLException e) {
            log.info("莫名其妙失败了SQL_getUserName:" + e.toString());
            return "-1";
        }
    }
    public boolean SQL_checkUserPassword(int id,String passwordIn){
        String sql = "select password_hash from users where user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            String passwordGet;
            if (rs.next()){
                passwordGet = rs.getString("password_hash");
            }else {
                log.info("SQL_checkUserPassword:没有获取到该id的密码");
                return false;
            }
            //log.info("获取的密码:"+passwordGet);
            if (passwordGet.equals(passwordIn)){
                return true;
            }else return false;
        } catch (SQLException e) {
            log.info("莫名其妙失败了SQL_checkUserPassword:"+e.toString());
            return true;
        }
    }
    public boolean SQL_checkUserExi(int id){
        String sql = "select * from users where user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            //log.info(rs.getString("user_id"))
            return rs.next();
        } catch (SQLException e) {
            log.info("莫名其妙失败了SQL_checkUserExi:" + e.toString());
            return true;
        }
    }
    public boolean SQL_checkAuthorId(int authorIdIn){
        String sql = "select * from authors where author_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, authorIdIn);
            ResultSet rs = stmt.executeQuery();
            //log.info(rs.getString("user_id"))
            return rs.next();
        } catch (SQLException e) {
            log.info("莫名其妙失败了SQL_checkAuthorId:" + e.toString());
            return true;
        }
    }
    public boolean SQL_createUser(int id ,String userName,String password,int roleId){
        String sql = """
        Insert into users values (?,?,?,?)
        """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, userName);
            stmt.setString(3, password);
            stmt.setInt(4, roleId);

            int rs = stmt.executeUpdate();
            if (rs>0) {
                return true;
            }
        } catch (SQLException e) {
            log.info(e.toString());
            return false;
        }
        return false;
    }


}
