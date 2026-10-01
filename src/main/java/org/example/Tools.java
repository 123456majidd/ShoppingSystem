package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Tools {

    public static final Scanner SCANNER = new Scanner(System.in);

    private static String readLabelValue(String line, String label) {
        if (line != null && line.startsWith(label)) {
            return line.substring(label.length()).trim();
        }
        return null;
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

    public static boolean writeCustomerInformationToFile(Customer customer, String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File infoFile = new File(folder, "customer_information.txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(infoFile, StandardCharsets.UTF_8))) {
            bw.write("用户名：" + (customer.getUserName() == null ? "" : customer.getUserName()));
            bw.newLine();
            bw.write("用户ID：" + (customer.getUserId() == null ? "" : customer.getUserId()));
            bw.newLine();
            bw.write("手机号：" + (customer.getUserPhoneNumber() == null ? "" : customer.getUserPhoneNumber()));
            bw.newLine();
            bw.write("邮箱：" + (customer.getEmail() == null ? "" : customer.getEmail()));
            bw.newLine();
            bw.write("密码：" + (customer.getPassword() == null ? "" : customer.getPassword()));
            bw.newLine();
            bw.write("注册时间：" + (customer.getRegisterTime() == null ? "" : customer.getRegisterTime()));
            bw.newLine();
            bw.write("等级：" + (customer.getLevel() == null ? "" : customer.getLevel()));
            bw.newLine();
            bw.write("总花费：" + customer.getTotalSpent());
            bw.newLine();
            bw.write("登录失败次数：" + customer.getFailedLoginCount());
            bw.newLine();
            bw.write("账户锁定：" + (customer.isLocked() ? "是" : "否"));
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("客户信息保存失败：" + e.getMessage());
            return false;
        }
    }

    public static Customer readCustomerInformationFromFolder(File folder) {
        File infoFile = new File(folder, "customer_information.txt");
        if (!infoFile.exists()) {
            return null;
        }
        Customer customer = new Customer();
        try (BufferedReader br = new BufferedReader(new FileReader(infoFile, StandardCharsets.UTF_8))) {
            String name = "", id = "", phone = "", email = "", pwd = "", registerTime = "", level = "";
            double totalSpent = 0;
            int failedLoginCount = 0;
            boolean locked = false;
            String line;
            while ((line = br.readLine()) != null) {
                String v;
                if ((v = readLabelValue(line, "用户名：")) != null) name = v;
                else if ((v = readLabelValue(line, "用户ID：")) != null) id = v;
                else if ((v = readLabelValue(line, "手机号：")) != null) phone = v;
                else if ((v = readLabelValue(line, "邮箱：")) != null) email = v;
                else if ((v = readLabelValue(line, "密码：")) != null) pwd = v;
                else if ((v = readLabelValue(line, "注册时间：")) != null) registerTime = v;
                else if ((v = readLabelValue(line, "等级：")) != null) level = v;
                else if ((v = readLabelValue(line, "总花费：")) != null) {
                    try { totalSpent = Double.parseDouble(v); } catch (NumberFormatException e) { totalSpent = 0; }
                }
                else if ((v = readLabelValue(line, "登录失败次数：")) != null) {
                    try { failedLoginCount = Integer.parseInt(v); } catch (NumberFormatException e) { failedLoginCount = 0; }
                }
                else if ((v = readLabelValue(line, "账户锁定：")) != null) locked = "是".equals(v);
            }
            customer.setUserName(name);
            customer.setUserId(id);
            customer.setUserPhoneNumber(phone);
            customer.setEmail(email);
            customer.setPassword(pwd);
            customer.setRegisterTime(registerTime);
            customer.setLevel(level);
            customer.setTotalSpent(totalSpent);
            customer.setFailedLoginCount(failedLoginCount);
            customer.setLocked(locked);
            return customer;
        } catch (IOException e) {
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
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder, "shopping_history.txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            bw.write("========== 购物历史记录 ==========");
            bw.newLine();
            bw.write("商品名称 | 数量 | 金额");
            bw.newLine();
            bw.write("----------------------------------");
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("初始化购物历史失败：" + e.getMessage());
            return false;
        }
    }

    public static boolean appendShoppingHistory(String folderPath, String record) {
        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs();
        File file = new File(folder, "shopping_history.txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8, true))) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            bw.write("[" + now.format(fmt) + "] " + record);
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("写入购物历史失败：" + e.getMessage());
            return false;
        }
    }

    public static List<String> readShoppingHistory(String folderPath) {
        List<String> history = new ArrayList<>();
        File file = new File(folderPath, "shopping_history.txt");
        if (!file.exists()) return history;
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                history.add(line);
            }
        } catch (IOException e) {
            System.out.println("读取购物历史失败：" + e.getMessage());
        }
        return history;
    }

    public static boolean writeGoodsInformationToFile(Good good, String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs();
        File infoFile = new File(folder, "goods_information.txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(infoFile, StandardCharsets.UTF_8))) {
            bw.write("商品名称：" + (good.goodName == null ? "" : good.goodName));
            bw.newLine();
            bw.write("商品编号：" + (good.goodId == null ? "" : good.goodId));
            bw.newLine();
            bw.write("生产者：" + (good.producer == null ? "" : good.producer));
            bw.newLine();
            bw.write("生产时间：" + (good.produceTime == null ? "" : good.produceTime));
            bw.newLine();
            bw.write("类型：" + (good.type == null ? "" : good.type));
            bw.newLine();
            bw.write("采购价格：" + good.purchasePrice);
            bw.newLine();
            bw.write("零售价格：" + good.retailPrice);
            bw.newLine();
            bw.write("数量：" + good.number);
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("商品信息保存失败：" + e.getMessage());
            return false;
        }
    }

    public static Good readGoodsInformationFromFolder(File folder) {
        File infoFile = new File(folder, "goods_information.txt");
        if (!infoFile.exists()) return null;
        Good good = new Good();
        try (BufferedReader br = new BufferedReader(new FileReader(infoFile, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String v;
                if ((v = readLabelValue(line, "商品名称：")) != null) good.goodName = v;
                else if ((v = readLabelValue(line, "商品编号：")) != null) good.goodId = v;
                else if ((v = readLabelValue(line, "生产者：")) != null) good.producer = v;
                else if ((v = readLabelValue(line, "生产时间：")) != null) good.produceTime = v;
                else if ((v = readLabelValue(line, "类型：")) != null) good.type = v;
                else if ((v = readLabelValue(line, "采购价格：")) != null) {
                    try { good.purchasePrice = Double.parseDouble(v); } catch (NumberFormatException e) { good.purchasePrice = 0; }
                }
                else if ((v = readLabelValue(line, "零售价格：")) != null) {
                    try { good.retailPrice = Double.parseDouble(v); } catch (NumberFormatException e) { good.retailPrice = 0; }
                }
                else if ((v = readLabelValue(line, "数量：")) != null) {
                    try { good.number = Integer.parseInt(v); } catch (NumberFormatException e) { good.number = 0; }
                }
            }
            if (good.goodId == null) good.goodId = folder.getName();
            return good;
        } catch (IOException e) {
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

    public static boolean writeAdminToFile(String folderPath, String fileName, String name, String id, String pwd) {
        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs();
        File file = new File(folder, fileName);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            bw.write("用户名：" + (name == null ? "" : name));
            bw.newLine();
            bw.write("用户ID：" + (id == null ? "" : id));
            bw.newLine();
            bw.write("密码：" + (pwd == null ? "" : pwd));
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("管理员信息保存失败：" + e.getMessage());
            return false;
        }
    }

    public static String[] readAdminFromFile(String folderPath, String fileName) {
        File file = new File(folderPath, fileName);
        if (!file.exists()) return null;
        String name = "", id = "", pwd = "";
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String v;
                if ((v = readLabelValue(line, "用户名：")) != null) name = v;
                else if ((v = readLabelValue(line, "用户ID：")) != null) id = v;
                else if ((v = readLabelValue(line, "密码：")) != null) pwd = v;
            }
            return new String[]{name, id, pwd};
        } catch (IOException e) {
            System.out.println("读取管理员信息失败：" + e.getMessage());
            return null;
        }
    }

    public static boolean writeShoppingCartToFile(String folderPath, ShoppingCart cart) {
        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs();
        File file = new File(folder, "shopping_cart.txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            for (int i = 0; i < cart.numberOfGoods; i++) {
                Good g = cart.goods[i];
                if (g == null || g.goodId == null) continue;
                bw.write(g.goodId + ":" + g.number);
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            System.out.println("购物车保存失败：" + e.getMessage());
            return false;
        }
    }

    public static ShoppingCart readShoppingCartFromFolder(File folder) {
        ShoppingCart cart = new ShoppingCart();
        File file = new File(folder, "shopping_cart.txt");
        if (!file.exists()) return cart;
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            int index = 0;
            while ((line = br.readLine()) != null && index < cart.goods.length) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(":");
                if (parts.length != 2) continue;
                String goodId = parts[0].trim();
                int qty;
                try { qty = Integer.parseInt(parts[1].trim()); } catch (NumberFormatException e) { continue; }
                if (goodId.isEmpty() || qty <= 0) continue;
                Good g = readGoodsInformationFromFolder(new File("src/gooodsInformation", goodId));
                if (g == null) {
                    System.out.println("购物车中的商品【" + goodId + "】已不存在，已跳过");
                    continue;
                }
                if (qty > g.number) {
                    System.out.println("商品【" + g.goodName + "】库存不足，已按当前库存 " + g.number + " 恢复");
                    qty = g.number;
                }
                g.number = qty;
                cart.goods[index++] = g;
            }
            cart.numberOfGoods = index;
        } catch (IOException e) {
            System.out.println("读取购物车失败：" + e.getMessage());
        }
        return cart;
    }

    public static void inputProductInformation(Good good){
        Scanner sc = SCANNER;
        System.out.println("请输入商品的名称：");
        good.goodName = sc.next();
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
        good.producer = sc.next();
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
        good.type = sc.next();
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
            System.out.println("用户名不合法：不能为空、不能含空格或 / \\ : * ? \" < > | 等字符，长度不超过20，请重新输入：");
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

    public static boolean chioceIfContinue() {
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

