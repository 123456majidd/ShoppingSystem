package org.example;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 版本4：本地 SQLite 数据库存储。
 * 所有"退出程序后会丢失的信息"（管理员、客户、商品、购物历史）统一写入本地数据库文件 src/shopping_system.db。
 */
public class Tools {

    public static final Scanner SCANNER = new Scanner(System.in);

    private static final String DB_PATH = "src" + File.separator + "shopping_system.db";
    private static volatile boolean dbInitialized = false;

    /** 确保数据库文件与全部数据表存在（幂等，可重复调用）。 */
    private static synchronized void ensureDatabase() {
        if (dbInitialized) return;
        File srcDir = new File("src");
        if (!srcDir.exists()) srcDir.mkdirs();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
             Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS admin ("
                    + "id TEXT PRIMARY KEY, "
                    + "name TEXT, "
                    + "phone TEXT, "
                    + "pwd TEXT)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS customer ("
                    + "userId TEXT PRIMARY KEY, "
                    + "userName TEXT, "
                    + "password TEXT, "
                    + "userPhoneNumber TEXT, "
                    + "level TEXT, "
                    + "registerTime TEXT, "
                    + "totalSpent REAL, "
                    + "email TEXT, "
                    + "failedLoginCount INTEGER, "
                    + "locked INTEGER)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS goods ("
                    + "goodId TEXT PRIMARY KEY, "
                    + "goodName TEXT, "
                    + "producer TEXT, "
                    + "produceTime TEXT, "
                    + "type TEXT, "
                    + "purchasePrice REAL, "
                    + "retailPrice REAL, "
                    + "number INTEGER)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS shopping_history ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "userId TEXT, "
                    + "record TEXT)");
            dbInitialized = true;
        } catch (Exception e) {
            System.out.println("数据库初始化失败：" + e.getMessage());
        }
    }

    /** 获取数据库连接（首次会自动建库建表）。 */
    private static Connection getConnection() throws Exception {
        ensureDatabase();
        return DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
    }

    /** 从目录路径中提取最后一段作为 ID（兼容 "src/customerInformation/xxx" 与 "xxx" 两种传参）。 */
    private static String extractIdFromFolderPath(String folderPath) {
        if (folderPath == null) return "";
        int idx = Math.max(folderPath.lastIndexOf('/'), folderPath.lastIndexOf('\\'));
        return idx >= 0 ? folderPath.substring(idx + 1) : folderPath;
    }

    // ==================== 管理员 ====================

    public static boolean writeAdminToFile(String folderPath, String fileName, String name, String id, String phone, String pwd) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR REPLACE INTO admin(id, name, phone, pwd) VALUES(?,?,?,?)")) {
            ps.setString(1, id == null ? "" : id);
            ps.setString(2, name == null ? "" : name);
            ps.setString(3, phone == null ? "" : phone);
            ps.setString(4, toStoredPassword(pwd));
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("管理员信息保存失败：" + e.getMessage());
            return false;
        }
    }

    /** 按管理员 ID（用户名+手机号）读取，返回 {name, id, phone, pwd}；不存在返回 null。 */
    public static String[] readAdminFromFile(String folderPath, String fileName) {
        String id = extractIdFromFolderPath(folderPath);
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT name, id, phone, pwd FROM admin WHERE id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{rs.getString("name"), rs.getString("id"), rs.getString("phone"), rs.getString("pwd")};
                }
            }
            return null;
        } catch (Exception e) {
            System.out.println("读取管理员信息失败：" + e.getMessage());
            return null;
        }
    }

    // ==================== 客户 ====================

    public static boolean writeCustomerInformationToFile(Customer customer, String folderPath) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR REPLACE INTO customer(userId, userName, password, userPhoneNumber, level, registerTime, totalSpent, email, failedLoginCount, locked) "
                             + "VALUES(?,?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, customer.getUserId());
            ps.setString(2, customer.getUserName());
            ps.setString(3, toStoredPassword(customer.getPassword()));
            ps.setString(4, customer.getUserPhoneNumber());
            ps.setString(5, customer.getLevel());
            ps.setString(6, customer.getRegisterTime());
            ps.setDouble(7, customer.getTotalSpent());
            ps.setString(8, customer.getEmail());
            ps.setInt(9, customer.getFailedLoginCount());
            ps.setInt(10, customer.isLocked() ? 1 : 0);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("客户信息保存失败：" + e.getMessage());
            return false;
        }
    }

    public static Customer readCustomerInformationFromFolder(File folder) {
        String userId = folder == null ? "" : folder.getName();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM customer WHERE userId = ?")) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return resultSetToCustomer(rs);
            }
        } catch (Exception e) {
            System.out.println("加载客户【" + userId + "】失败：" + e.getMessage());
            return null;
        }
    }

    private static Customer resultSetToCustomer(ResultSet rs) throws Exception {
        Customer c = new Customer();
        c.setUserName(rs.getString("userName") == null ? "" : rs.getString("userName"));
        c.setUserId(rs.getString("userId") == null ? "" : rs.getString("userId"));
        c.setPassword(rs.getString("password") == null ? "" : rs.getString("password"));
        c.setUserPhoneNumber(rs.getString("userPhoneNumber") == null ? "" : rs.getString("userPhoneNumber"));
        c.setLevel(rs.getString("level") == null ? "" : rs.getString("level"));
        c.setRegisterTime(rs.getString("registerTime") == null ? "" : rs.getString("registerTime"));
        c.setTotalSpent(rs.getDouble("totalSpent"));
        c.setEmail(rs.getString("email") == null ? "" : rs.getString("email"));
        c.setFailedLoginCount(rs.getInt("failedLoginCount"));
        c.setLocked(rs.getInt("locked") == 1);
        return c;
    }

    public static int loadAllCustomers(Customer[] customers, String rootPath) {
        List<Customer> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM customer ORDER BY userId")) {
            while (rs.next()) {
                list.add(resultSetToCustomer(rs));
            }
        } catch (Exception e) {
            System.out.println("加载客户列表失败：" + e.getMessage());
            return 0;
        }
        int count = 0;
        for (Customer c : list) {
            if (count >= customers.length) break;
            customers[count] = c;
            count++;
        }
        return count;
    }

    /** 判断客户（用户名+手机号）是否已存在，用于注册查重。 */
    public static boolean customerExists(String userId) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM customer WHERE userId = ?")) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            System.out.println("客户查重失败：" + e.getMessage());
            return false;
        }
    }

    /** 按用户名 + 注册邮箱查找客户，用于"忘记密码"；找不到返回 null。 */
    public static Customer findCustomerByUserNameAndEmail(String userName, String email) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM customer WHERE userName = ? AND email = ?")) {
            ps.setString(1, userName);
            ps.setString(2, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return resultSetToCustomer(rs);
            }
            return null;
        } catch (Exception e) {
            System.out.println("查找客户失败：" + e.getMessage());
            return null;
        }
    }

    /** 删除客户及其全部购物历史记录。 */
    public static boolean deleteCustomer(String userId) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM customer WHERE userId = ?");
                 PreparedStatement ps2 = conn.prepareStatement("DELETE FROM shopping_history WHERE userId = ?")) {
                ps1.setString(1, userId);
                ps1.executeUpdate();
                ps2.setString(1, userId);
                ps2.executeUpdate();
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.out.println("删除客户失败：" + e.getMessage());
            return false;
        }
    }

    // ==================== 购物历史 ====================

    public static boolean initShoppingHistory(String folderPath) {
        ensureDatabase();
        return true;
    }

    public static boolean appendShoppingHistory(String folderPath, String record) {
        String userId = extractIdFromFolderPath(folderPath);
        String fullRecord = record;
        if (!record.startsWith("[")) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            fullRecord = "[" + now.format(fmt) + "] " + record;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO shopping_history(userId, record) VALUES(?,?)")) {
            ps.setString(1, userId);
            ps.setString(2, fullRecord);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("写入购物历史失败：" + e.getMessage());
            return false;
        }
    }

    public static List<String> readShoppingHistory(String folderPath) {
        List<String> history = new ArrayList<>();
        String userId = extractIdFromFolderPath(folderPath);
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT record FROM shopping_history WHERE userId = ? ORDER BY id")) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String rec = rs.getString("record");
                    if (rec != null && !rec.isEmpty()) history.add(rec);
                }
            }
        } catch (Exception e) {
            System.out.println("读取购物历史失败：" + e.getMessage());
        }
        return history;
    }

    // ==================== 商品 ====================

    public static boolean writeGoodsInformationToFile(Good good, String folderPath) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR REPLACE INTO goods(goodId, goodName, producer, produceTime, type, purchasePrice, retailPrice, number) "
                             + "VALUES(?,?,?,?,?,?,?,?)")) {
            ps.setString(1, good.goodId);
            ps.setString(2, good.goodName);
            ps.setString(3, good.producer);
            ps.setString(4, good.produceTime);
            ps.setString(5, good.type);
            ps.setDouble(6, good.purchasePrice);
            ps.setDouble(7, good.retailPrice);
            ps.setInt(8, good.number);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("商品信息保存失败：" + e.getMessage());
            return false;
        }
    }

    public static Good readGoodsInformationFromFolder(File folder) {
        String goodId = folder == null ? "" : folder.getName();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM goods WHERE goodId = ?")) {
            ps.setString(1, goodId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Good g = new Good();
                g.goodName = rs.getString("goodName") == null ? "" : rs.getString("goodName");
                g.goodId = rs.getString("goodId") == null ? "" : rs.getString("goodId");
                g.producer = rs.getString("producer") == null ? "" : rs.getString("producer");
                g.produceTime = rs.getString("produceTime") == null ? "" : rs.getString("produceTime");
                g.type = rs.getString("type") == null ? "" : rs.getString("type");
                g.purchasePrice = rs.getDouble("purchasePrice");
                g.retailPrice = rs.getDouble("retailPrice");
                g.number = rs.getInt("number");
                return g;
            }
        } catch (Exception e) {
            System.out.println("加载商品【" + goodId + "】失败：" + e.getMessage());
            return null;
        }
    }

    public static int loadAllGoods(Good[] goods, String rootPath) {
        List<Good> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM goods ORDER BY goodId")) {
            while (rs.next()) {
                Good g = new Good();
                g.goodName = rs.getString("goodName") == null ? "" : rs.getString("goodName");
                g.goodId = rs.getString("goodId") == null ? "" : rs.getString("goodId");
                g.producer = rs.getString("producer") == null ? "" : rs.getString("producer");
                g.produceTime = rs.getString("produceTime") == null ? "" : rs.getString("produceTime");
                g.type = rs.getString("type") == null ? "" : rs.getString("type");
                g.purchasePrice = rs.getDouble("purchasePrice");
                g.retailPrice = rs.getDouble("retailPrice");
                g.number = rs.getInt("number");
                list.add(g);
            }
        } catch (Exception e) {
            System.out.println("加载商品列表失败：" + e.getMessage());
            return 0;
        }
        int count = 0;
        for (Good g : list) {
            if (count >= goods.length) break;
            goods[count] = g;
            count++;
        }
        return count;
    }

    /** 判断商品编号是否已存在，用于添加/修改商品时查重。 */
    public static boolean goodIdExists(String goodId) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM goods WHERE goodId = ?")) {
            ps.setString(1, goodId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            System.out.println("商品编号查重失败：" + e.getMessage());
            return false;
        }
    }

    /** 删除商品记录。 */
    public static boolean deleteGood(String goodId) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM goods WHERE goodId = ?")) {
            ps.setString(1, goodId);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("删除商品失败：" + e.getMessage());
            return false;
        }
    }

    // ==================== 密码与校验工具 ====================

    private static final String PASSWORD_SALT = "ShoppingSystemV3_2026";

    public static String hashPassword(String password) {
        if (password == null) return "";
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((PASSWORD_SALT + password).getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            System.out.println("密码加密失败：" + e.getMessage());
            return password;
        }
    }

    /**
     * 写入数据库前统一处理密码：明文才做 SHA-256 哈希；已是 64 位十六进制哈希则直接保存，
     * 避免内存中已是哈希时再次哈希造成"双重哈希"导致密码失效。
     */
    public static String toStoredPassword(String password) {
        if (password == null) return "";
        if (password.matches("^[0-9a-f]{64}$")) return password;
        return hashPassword(password);
    }

    public static boolean isValidUserName(String name) {
        if (name == null || name.isEmpty() || name.length() < 5 || name.length() > 20) {
            return false;
        }
        if (name.equals(".") || name.equals("..")) {
            return false;
        }
        return !name.matches(".*[\\\\/:*?\"<>|\\s].*");
    }

    public static String generateRandomPassword() {
        String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        String lower = "abcdefghijkmnpqrstuvwxyz";
        String digits = "23456789";
        String symbols = "!@#$%^&*";
        java.util.Random rnd = new java.util.Random();
        StringBuilder sb = new StringBuilder();
        sb.append(upper.charAt(rnd.nextInt(upper.length())));
        sb.append(lower.charAt(rnd.nextInt(lower.length())));
        sb.append(digits.charAt(rnd.nextInt(digits.length())));
        sb.append(symbols.charAt(rnd.nextInt(symbols.length())));
        String all = upper + lower + digits;
        for (int i = 0; i < 6; i++) {
            sb.append(all.charAt(rnd.nextInt(all.length())));
        }
        char[] arr = sb.toString().toCharArray();
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            char t = arr[i];
            arr[i] = arr[j];
            arr[j] = t;
        }
        return new String(arr);
    }

    public static void inputProductInformation(Good good){
        Scanner sc = SCANNER;
        System.out.println("请输入商品的名称：");
        String inputName;
        do {
            inputName = sc.nextLine();
        } while (inputName.trim().isEmpty());
        good.goodName = inputName.trim();
        while (true) {
            System.out.println("请输入商品的编号（格式：4位数字，比如0001）：");
            String inputId = sc.next();
            if (!inputId.matches("^\\d{4}$")) {
                System.out.println("编号格式错误，必须输入4位纯数字（例如：0001、1234）！");
                continue;
            }
            if (goodIdExists(inputId)) {
                System.out.println("编号 " + inputId + " 已存在，不能重复使用，请重新输入：");
                continue;
            }
            good.goodId = inputId;
            break;
        }
        System.out.println("请输入商品的生产者：");
        String inputProducer;
        do {
            inputProducer = sc.nextLine();
        } while (inputProducer.trim().isEmpty());
        good.producer = inputProducer.trim();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (true) {
            System.out.println("请输入商品的生产时间（格式：yyyy-MM-dd）：");
            String dateInput = sc.next();
            try {
                java.time.LocalDate.parse(dateInput, formatter);
                good.produceTime = dateInput;
                break;
            } catch (java.time.format.DateTimeParseException e) {
                System.out.println("日期格式错误，请严格按照 yyyy-MM-dd 的格式输入（例如：2026-09-19）！");
            }
        }
        System.out.println("请输入商品的类型：");
        String inputType;
        do {
            inputType = sc.nextLine();
        } while (inputType.trim().isEmpty());
        good.type = inputType.trim();
        System.out.println("请输入商品的采购价格：");
        while (true) {
            double price = 0;
            try{
                price = sc.nextDouble();
                if (price > 0) {
                    good.purchasePrice = price;
                    break;
                }
                else{
                    System.out.println("价格应大于0，请重新输入：");
                }
            }
            catch (Exception e) {
                System.out.println("请输入一个大于0的数字：");
                sc.nextLine();
                continue;
            }
        }
        System.out.println("请输入商品的零售价格：");
        while (true) {
            double price = 0;
            try{
                price = sc.nextDouble();
                if (price > 0) {
                    good.retailPrice = price;
                    break;
                }
                else{
                    System.out.println("价格应大于0，请重新输入：");
                }
            }
            catch (Exception e) {
                System.out.println("请输入一个大于0的数字：");
                sc.nextLine();
                continue;
            }
        }
        System.out.println("请输入商品的数量库存：");
        while (true) {
            int number = 0;
            try{
                number = sc.nextInt();
                if (number >= 0) {
                    good.number = number;
                    break;
                }
                else{
                    System.out.println("数量应大于等于0，请重新输入：");
                }
            }
            catch (Exception e) {
                System.out.println("请输入一个大于等于0的整数：");
                sc.nextLine();
                continue;
            }
        }
    }

    public static void inputCustomerInformation(Customer customer){
        Scanner sc = SCANNER;
        while (true) {
            System.out.println("请输入该客户的用户名：");
            String name = sc.next();
            if (isValidUserName(name)) {
                customer.setUserName(name);
                break;
            }
            System.out.println("用户名不合法：不能为空、不能含字符，长度不少于5个字符，请重新输入：");
        }
        while (true) {
            System.out.println("请输入该客户的手机号：");
            String phone = sc.next();
            if (phone.matches("^1[3-9]\\d{9}$")) {
                customer.setUserPhoneNumber(phone);
                break;
            } else {
                System.out.println("手机号格式错误，请输入11位有效的中国大陆手机号（例如：13812345678）！");
            }
        }
        customer.setUserId(customer.getUserName() + customer.getUserPhoneNumber());
        System.out.println("请输入该客户的等级（1.金牌会员/2.银牌会员/3.铜牌会员）：");
        int level = 0;
        while (true) {
            try{
                level = sc.nextInt();
                if (level >= 1 && level <= 3) {
                    break;
                }
                else{
                    System.out.println("等级输入错误，请重新输入");
                }
            }
            catch (Exception e){
                System.out.println("请输入一个（1-3）之间的整数：");
                sc.nextLine();
                continue;
            }
        }
        if (level == 1) {
            customer.setLevel("金牌会员");
        }
        else if (level == 2) {
            customer.setLevel("银牌会员");
        }
        else if (level == 3) {
            customer.setLevel("铜牌会员");
        }
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (true) {
            System.out.println("请输入该客户的注册时间（格式：yyyy-MM-dd）：");
            String dateInput = sc.next();
            try {
                java.time.LocalDate.parse(dateInput, formatter);
                customer.setRegisterTime(dateInput);
                break;
            } catch (java.time.format.DateTimeParseException e) {
                System.out.println("日期格式错误，请严格按照 yyyy-MM-dd 的格式输入（例如：2026-09-19）！");
            }
        }
        System.out.println("请输入该客户的总花费：");
        double spent;
        while (true) {
            try{
                spent = sc.nextDouble();
                break;
            }
            catch (Exception e){
                System.out.println("请输入一个数字：");
                sc.nextLine();
                continue;
            }
        }
        customer.setTotalSpent(spent);
        while (true) {
            System.out.println("请输入该客户的邮箱：");
            String email = sc.next();
            if (email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                customer.setEmail(email);
                break;
            } else {
                System.out.println("邮箱格式错误，请输入有效的邮箱地址（例如：zhangsan@example.com）！");
            }
        }
        customer.setPassword("ynuinfo#777");
    }

    public static boolean isValidPassword(String password) {
        String regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{9,}$";
        return password.matches(regex);
    }

    public static String formatMoney(double value) {
        return java.math.BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    public static boolean chooseIfContinue() {
        Scanner sc = SCANNER;
        int chioce = 0;
        while(true){
            System.out.println("是否继续操作？（1.继续/0.退出）");
            try {
                chioce = sc.nextInt();
                if (chioce == 1 || chioce == 0) {
                    break;
                } else {
                    System.out.println("输入错误，请输入1或0！");
                }
            } catch (Exception e) {
                System.out.println("输入错误，请输入1或0！");
                sc.nextLine();
            }
        }
        if(chioce == 0){
            System.out.println("正在返回菜单...");
            return false;
        }
        return true;
    }

}
