package org.example;


import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Tools {

    public static final Scanner SCANNER = new Scanner(System.in);

    private static String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d) && !Double.isInfinite(d)) return String.valueOf((long) d);
                return String.valueOf(d);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try { return cell.getStringCellValue(); } catch (Exception e) { return String.valueOf(cell.getNumericCellValue()); }
            default: return "";
        }
    }

    private static void writeKeyValueSheet(Sheet sheet, String[][] kv) {
        for (int i = 0; i < kv.length; i++) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(kv[i][0]);
            row.createCell(1).setCellValue(kv[i][1] == null ? "" : kv[i][1]);
        }
    }

    private static Map<String, String> readKeyValueSheet(Sheet sheet) {
        Map<String, String> map = new LinkedHashMap<>();
        if (sheet == null) return map;
        for (Row row : sheet) {
            String key = getCellStringValue(row.getCell(0));
            String value = getCellStringValue(row.getCell(1));
            if (!key.isEmpty()) map.put(key, value);
        }
        return map;
    }

    private static XSSFWorkbook openOrCreateCustomerWorkbook(File file) throws IOException {
        XSSFWorkbook wb;
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                wb = new XSSFWorkbook(fis);
            }
        } else {
            wb = new XSSFWorkbook();
        }
        if (wb.getSheetIndex("购物历史") < 0) wb.createSheet("购物历史");
        return wb;
    }

    private static void saveWorkbook(XSSFWorkbook wb, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            wb.write(fos);
        }
    }

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

    public static boolean writeCustomerInformationToFile(Customer customer, String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs();
        File infoFile = new File(folder, "customer_information.xlsx");
        XSSFWorkbook wb = null;
        try {
            wb = openOrCreateCustomerWorkbook(infoFile);
            int idx = wb.getSheetIndex("基本信息");
            if (idx >= 0) wb.removeSheetAt(idx);
            Sheet s1 = wb.createSheet("基本信息");
            String[][] kv = {
                    {"userName", customer.getUserName()},
                    {"userId", customer.getUserId()},
                    {"password", hashPassword(customer.getPassword())},
                    {"userPhoneNumber", customer.getUserPhoneNumber()},
                    {"level", customer.getLevel()},
                    {"registerTime", customer.getRegisterTime()},
                    {"totalSpent", String.valueOf(customer.getTotalSpent())},
                    {"email", customer.getEmail()},
                    {"failedLoginCount", String.valueOf(customer.getFailedLoginCount())},
                    {"locked", String.valueOf(customer.isLocked())}
            };
            writeKeyValueSheet(s1, kv);
            saveWorkbook(wb, infoFile);
            return true;
        } catch (Exception e) {
            System.out.println("客户信息保存失败：" + e.getMessage());
            return false;
        } finally {
            if (wb != null) try { wb.close(); } catch (Exception ignored) {}
        }
    }

    public static Customer readCustomerInformationFromFolder(File folder) {
        File infoFile = new File(folder, "customer_information.xlsx");
        if (!infoFile.exists()) return null;
        try (FileInputStream fis = new FileInputStream(infoFile);
             XSSFWorkbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheet("基本信息");
            if (sheet == null) {
                System.out.println("客户【" + folder.getName() + "】Excel 缺少基本信息 Sheet，已跳过");
                return null;
            }
            Map<String, String> m = readKeyValueSheet(sheet);
            Customer c = new Customer();
            c.setUserName(m.getOrDefault("userName", ""));
            c.setUserId(m.getOrDefault("userId", ""));
            c.setPassword(m.getOrDefault("password", ""));
            c.setUserPhoneNumber(m.getOrDefault("userPhoneNumber", ""));
            c.setLevel(m.getOrDefault("level", ""));
            c.setRegisterTime(m.getOrDefault("registerTime", ""));
            try { c.setTotalSpent(Double.parseDouble(m.getOrDefault("totalSpent", "0"))); }
            catch (NumberFormatException e) { c.setTotalSpent(0); }
            c.setEmail(m.getOrDefault("email", ""));
            try { c.setFailedLoginCount(Integer.parseInt(m.getOrDefault("failedLoginCount", "0"))); }
            catch (NumberFormatException e) { c.setFailedLoginCount(0); }
            c.setLocked(Boolean.parseBoolean(m.getOrDefault("locked", "false")));
            return c;
        } catch (Exception e) {
            System.out.println("加载客户【" + folder.getName() + "】失败：" + e.getMessage());
            return null;
        }
    }

    public static int loadAllCustomers(Customer[] customers, String rootPath) {
        File rootDir = new File(rootPath);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            System.out.println("客户信息目录不存在：" + rootPath);
            return 0;
        }
        File[] folders = rootDir.listFiles(File::isDirectory);
        if (folders == null || folders.length == 0) {
            System.out.println("暂无客户数据");
            return 0;
        }
        int count = 0;
        for (File folder : folders) {
            if (count >= customers.length) break;
            Customer c = readCustomerInformationFromFolder(folder);
            if (c != null) {
                customers[count] = c;
                count++;
            }
        }
        return count;
    }

    public static boolean initShoppingHistory(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs();
        File file = new File(folder, "customer_information.xlsx");
        XSSFWorkbook wb = null;
        try {
            wb = openOrCreateCustomerWorkbook(file);
            saveWorkbook(wb, file);
            return true;
        } catch (Exception e) {
            System.out.println("初始化购物历史失败：" + e.getMessage());
            return false;
        } finally {
            if (wb != null) try { wb.close(); } catch (Exception ignored) {}
        }
    }

    public static boolean appendShoppingHistory(String folderPath, String record) {
        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs();
        File file = new File(folder, "customer_information.xlsx");
        XSSFWorkbook wb = null;
        try {
            wb = openOrCreateCustomerWorkbook(file);
            Sheet sheet = wb.getSheet("购物历史");
            int lastRow = sheet.getLastRowNum();
            Row row = sheet.createRow(lastRow + 1);
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            row.createCell(0).setCellValue("[" + now.format(fmt) + "] " + record);
            saveWorkbook(wb, file);
            return true;
        } catch (Exception e) {
            System.out.println("写入购物历史失败：" + e.getMessage());
            return false;
        } finally {
            if (wb != null) try { wb.close(); } catch (Exception ignored) {}
        }
    }

    public static List<String> readShoppingHistory(String folderPath) {
        List<String> history = new ArrayList<>();
        File file = new File(folderPath, "customer_information.xlsx");
        if (!file.exists()) return history;
        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheet("购物历史");
            if (sheet == null) return history;
            for (Row row : sheet) {
                String rec = getCellStringValue(row.getCell(0));
                if (!rec.isEmpty()) history.add(rec);
            }
        } catch (Exception e) {
            System.out.println("读取购物历史失败：" + e.getMessage());
        }
        return history;
    }

    public static boolean writeGoodsInformationToFile(Good good, String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs();
        File infoFile = new File(folder, "goods_information.xlsx");
        try (XSSFWorkbook wb = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(infoFile)) {
            Sheet sheet = wb.createSheet("商品信息");
            String[][] kv = {
                    {"goodName", good.goodName},
                    {"goodId", good.goodId},
                    {"producer", good.producer},
                    {"produceTime", good.produceTime},
                    {"type", good.type},
                    {"purchasePrice", String.valueOf(good.purchasePrice)},
                    {"retailPrice", String.valueOf(good.retailPrice)},
                    {"number", String.valueOf(good.number)}
            };
            writeKeyValueSheet(sheet, kv);
            wb.write(fos);
            return true;
        } catch (Exception e) {
            System.out.println("商品信息保存失败：" + e.getMessage());
            return false;
        }
    }

    public static Good readGoodsInformationFromFolder(File folder) {
        File infoFile = new File(folder, "goods_information.xlsx");
        if (!infoFile.exists()) return null;
        try (FileInputStream fis = new FileInputStream(infoFile);
             XSSFWorkbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheet("商品信息");
            if (sheet == null) {
                System.out.println("商品【" + folder.getName() + "】Excel 缺少商品信息 Sheet，已跳过");
                return null;
            }
            Map<String, String> m = readKeyValueSheet(sheet);
            Good g = new Good();
            g.goodName = m.getOrDefault("goodName", "");
            g.goodId = m.getOrDefault("goodId", "");
            g.producer = m.getOrDefault("producer", "");
            g.produceTime = m.getOrDefault("produceTime", "");
            g.type = m.getOrDefault("type", "");
            try { g.purchasePrice = Double.parseDouble(m.getOrDefault("purchasePrice", "0")); }
            catch (NumberFormatException e) { g.purchasePrice = 0; }
            try { g.retailPrice = Double.parseDouble(m.getOrDefault("retailPrice", "0")); }
            catch (NumberFormatException e) { g.retailPrice = 0; }
            try { g.number = Integer.parseInt(m.getOrDefault("number", "0")); }
            catch (NumberFormatException e) { g.number = 0; }
            return g;
        } catch (Exception e) {
            System.out.println("加载商品【" + folder.getName() + "】失败：" + e.getMessage());
            return null;
        }
    }

    public static int loadAllGoods(Good[] goods, String rootPath) {
        File rootDir = new File(rootPath);
        if (!rootDir.exists() || !rootDir.isDirectory()) return 0;
        File[] folders = rootDir.listFiles(File::isDirectory);
        if (folders == null || folders.length == 0) return 0;
        int count = 0;
        for (File folder : folders) {
            if (count >= goods.length) break;
            Good g = readGoodsInformationFromFolder(folder);
            if (g != null) {
                goods[count] = g;
                count++;
            }
        }
        return count;
    }

    public static boolean writeAdminToFile(String folderPath, String fileName, String name, String id, String phone, String pwd) {
        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs();
        File file = new File(folder, fileName);
        try (XSSFWorkbook wb = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(file)) {
            Sheet sheet = wb.createSheet("管理员信息");
            String[][] kv = {
                    {"name", name == null ? "" : name},
                    {"id", id == null ? "" : id},
                    {"phone", phone == null ? "" : phone},
                    {"pwd", hashPassword(pwd == null ? "" : pwd)}
            };
            writeKeyValueSheet(sheet, kv);
            wb.write(fos);
            return true;
        } catch (Exception e) {
            System.out.println("管理员信息保存失败：" + e.getMessage());
            return false;
        }
    }

    public static String[] readAdminFromFile(String folderPath, String fileName) {
        File file = new File(folderPath, fileName);
        if (!file.exists()) return null;
        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheet("管理员信息");
            if (sheet == null) {
                System.out.println("管理员数据文件格式异常");
                return null;
            }
            Map<String, String> m = readKeyValueSheet(sheet);
            String name = m.getOrDefault("name", "");
            String id = m.getOrDefault("id", "");
            String phone = m.getOrDefault("phone", "");
            String pwd = m.getOrDefault("pwd", "");
            if (id.isEmpty()) {
                System.out.println("管理员数据文件格式异常");
                return null;
            }
            return new String[]{name, id, phone, pwd};
        } catch (Exception e) {
            System.out.println("读取管理员信息失败：" + e.getMessage());
            return null;
        }
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
            File existFolder = new File("src/gooodsInformation/" + inputId);
            if (existFolder.exists()) {
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
        java.time.LocalDate parsedDate = null;
        while (true) {
            System.out.println("请输入该客户的注册时间（格式：yyyy-MM-dd）：");
            String dateInput = sc.next();
            try {
                parsedDate = java.time.LocalDate.parse(dateInput, formatter);
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

