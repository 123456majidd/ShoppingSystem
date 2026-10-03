package org.example;


import java.io.*;
import java.util.Scanner;

public class Admin extends User {
    final int MAX_NUM = 1000;
    private int customerNumber;
    private int goodNumber;
    private Customer[] customers = new Customer[MAX_NUM];
    private Good[] goods = new Good[MAX_NUM];

    public Admin() {
        this.setUserName("admin");
        this.setUserId("admin12345678900");
        this.setPassword("ynuinfo#777");
    }

    public Good[] getGoods() {
        return goods;
    }

    public void initializeCustomers() {
        while(true) {
            System.out.println("请输入客户数量（1-" + MAX_NUM + "）：");
            Scanner scanner = Tools.SCANNER;
            try{
                this.customerNumber = scanner.nextInt();
                if (this.customerNumber >= 1 && this.customerNumber <= MAX_NUM) {
                    break;
                }
                else {
                    System.out.println("客户数量应为 1-" + MAX_NUM + " 之间的整数，请重新输入：");
                }
            }catch (Exception e){
                System.out.println("请输入一个整数");
                scanner.nextLine();
                continue;
            }
        }
        for (int i = 0; i < this.customerNumber; i++) {
            customers[i] = new Customer();
            Tools.inputCustomerInformation(customers[i]);
            String userFolderPath = "src/customerInformation/" + customers[i].getUserId();
            File userFolder = new File(userFolderPath);
            userFolder.mkdirs();
            boolean saved = Tools.writeCustomerInformationToFile(customers[i], userFolderPath);
            if (!saved) {
                System.out.println("客户【" + customers[i].getUserName() + "】信息保存失败");
            }
            Tools.initShoppingHistory(userFolderPath);
        }
        System.out.println("客户初始化完成");
    }

    public void resetCustomerPassword() {
        customerNumber = Tools.loadAllCustomers(customers, "src/customerInformation");
        if (customerNumber == 0) {
            return;
        }
        System.out.println("请输入客户ID（格式：用户名手机号）：");
        Scanner sc = Tools.SCANNER;
        String customerId = sc.next();
        Customer customer = null;
        for (int i = 0; i < customerNumber; i++) {
            if (customers[i].getUserId().equals(customerId)) {
                customer = customers[i];
                break;
            }
        }
        if (customer == null) {
            System.out.println("客户不存在");
            return;
        }
        System.out.println("您确定要重置" + customer.getUserId() + "的密码吗？（1.确定 2.取消）");
        int result = 0;
        while (true) {
            try{
                result = sc.nextInt();
                if (result == 1 || result == 2) {
                    break;
                }
                else {
                    System.out.println("输入错误，请重新输入（1.确定 2.取消）：");
                }
            } catch (Exception e) {
                System.out.println("输入错误，请重新输入（1.确定 2.取消）：");
                sc.nextLine();
            }
        }
        if (result==1) {
            customer.setPassword("ynuinfo#777");
            customer.setFailedLoginCount(0);
            customer.setLocked(false);
            boolean saved = Tools.writeCustomerInformationToFile(customer, "src/customerInformation/" + customerId);
            if (saved) {
                System.out.println("密码重置成功，账户已解锁");
            } else {
                System.out.println("密码重置信息保存失败");
            }
        } else {
            System.out.println("重置取消");
        }
    }

    public void listCustomerInformation() {
        customerNumber = Tools.loadAllCustomers(customers, "src/customerInformation");
        if (customerNumber == 0) {
            return;
        }
        System.out.println("客户信息列表：");
        for (int i = 0; i < customerNumber; i++) {
            Customer c = customers[i];
            System.out.println("客户的用户名为：" + c.getUserName());
            System.out.println("客户的用户ID为：" + c.getUserId());
            System.out.println("客户的等级为：" + c.getLevel());
            System.out.println("客户的注册时间为：" + c.getRegisterTime());
            System.out.println("客户的总花费为：" + Tools.formatMoney(c.getTotalSpent()));
            System.out.println("客户的邮箱为：" + c.getEmail());
            System.out.println("客户的手机号为：" + c.getUserPhoneNumber());
            System.out.println("------------------");
        }
    }

    public void checkCustomerInformation() {
        customerNumber = Tools.loadAllCustomers(customers, "src/customerInformation");
        if (customerNumber == 0) {
            return;
        }
        Scanner sc = Tools.SCANNER;
        for (; true; ) {
            System.out.println("请输入客户ID或用户名（输入 all 查询所有客户信息）：");
            String keyword = sc.next();
            if (keyword.equals("all")) {
                System.out.println("客户信息列表：");
                for (int i = 0; i < customerNumber; i++) {
                    Customer c = customers[i];
                    System.out.println("客户的用户名为：" + c.getUserName());
                    System.out.println("客户的用户ID为：" + c.getUserId());
                    System.out.println("客户的等级为：" + c.getLevel());
                    System.out.println("客户的注册时间为：" + c.getRegisterTime());
                    System.out.println("客户的总花费为：" + Tools.formatMoney(c.getTotalSpent()));
                    System.out.println("客户的邮箱为：" + c.getEmail());
                    System.out.println("客户的手机号为：" + c.getUserPhoneNumber());
                    System.out.println("------------------");
                }
                break;
            }
            boolean found = false;
            for (int i = 0; i < customerNumber; i++) {
                if (customers[i].getUserId().equals(keyword) || customers[i].getUserName().equals(keyword)) {
                    found = true;
                    System.out.println("客户的用户名为：" + customers[i].getUserName());
                    System.out.println("客户的用户ID为：" + customers[i].getUserId());
                    System.out.println("客户的等级为：" + customers[i].getLevel());
                    System.out.println("客户的注册时间为：" + customers[i].getRegisterTime());
                    System.out.println("客户的总花费为：" + Tools.formatMoney(customers[i].getTotalSpent()));
                    System.out.println("客户的邮箱为：" + customers[i].getEmail());
                    System.out.println("客户的手机号为：" + customers[i].getUserPhoneNumber());
                    System.out.println("------------------");
                }
            }
            if (!found) {
                System.out.println("客户不存在，请重新输入：");
                continue;
            }
            break;
        }
    }

    public void deleteCustomerInformation() {
        customerNumber = Tools.loadAllCustomers(customers, "src/customerInformation");
        if (customerNumber == 0) {
            return;
        }
        Scanner sc = Tools.SCANNER;
        while (true) {
            System.out.println("请输入客户ID（格式：用户名手机号）：");
            String customerId = sc.next();
            int foundIndex = -1;
            for (int i = 0; i < customerNumber; i++) {
                if (customers[i].getUserId().equals(customerId)) {
                    foundIndex = i;
                    break;
                }
            }
            if (foundIndex == -1) {
                System.out.println("客户不存在,请重新输入或结束删除（1.重新输入客户ID 2.结束删除操作）：");
                int choice = 0;
                while (true) {
                    try{
                        choice = sc.nextInt();
                        if (choice == 1 || choice == 2) {
                            break;
                        }
                        else {
                            System.out.println("输入错误，请重新输入（1.重新输入客户用户ID 2.结束删除操作）：");
                        }
                    }
                    catch (Exception e){
                        System.out.println("输入错误，请重新输入（1.重新输入客户用户ID 2.结束删除操作）：");
                        sc.nextLine();
                    }
                }
                if (choice == 2) {
                    System.out.println("删除操作结束");
                    break;
                }
                continue;
            }
            System.out.println("确认删除客户吗（输入1确认删除，否则取消删除）？");
            int choice = 0;
            while(true) {
                try{
                    choice = sc.nextInt();
                    break;
                }
                catch (Exception e){
                    System.out.println("输入错误，请重新输入（输入1确认删除，否则取消删除）：");
                    sc.nextLine();
                }
            }
            if (!(choice==1)) {
                System.out.println("删除取消");
                return;
            }
            for (int j = foundIndex; j < customerNumber - 1; j++) {
                customers[j] = customers[j + 1];
            }
            customers[customerNumber - 1] = null;
            customerNumber--;
            File userFolder = new File("src/customerInformation/" + customerId);
            if (userFolder.exists()) {
                File[] allFiles = userFolder.listFiles();
                if (allFiles != null) {
                    for (File file : allFiles) file.delete();
                }
                boolean folderDeleted = userFolder.delete();
                if (folderDeleted) System.out.println("客户文件已全部清除");
                else System.out.println("客户文件夹删除失败");
            }
            System.out.println("删除成功");
            break;
        }
    }

    public void initializeGoods() {
        Scanner scanner = Tools.SCANNER;
        int tempNumber = 0;
        while (true) {
            System.out.println("请输入商品数量（1-" + MAX_NUM + "）：");
            try {
                tempNumber = scanner.nextInt();
                if (tempNumber >= 1 && tempNumber <= MAX_NUM) {
                    break;
                }
                else {
                    System.out.println("商品数量应为 1-" + MAX_NUM + " 之间的整数，请重新输入：");
                }
            }
            catch (Exception e) {
                System.out.println("请输入一个整数：");
                scanner.nextLine();
                continue;
            }
        }
        goodNumber = tempNumber;
        for (int i = 0; i < goodNumber; i++) {
            goods[i] = new Good();
            Tools.inputProductInformation(goods[i]);
            String goodsFolderPath = "src/gooodsInformation/" + goods[i].goodId;
            boolean saved = Tools.writeGoodsInformationToFile(goods[i], goodsFolderPath);
            if (!saved) {
                System.out.println("商品【" + goods[i].goodName + "】信息保存失败");
            }
        }
        System.out.println("商品初始化完成");
    }

    public void listGoodsInformation() {
        goodNumber = Tools.loadAllGoods(goods, "src/gooodsInformation");
        if (goodNumber == 0) {
            return;
        }
        System.out.println("商品信息列表：");
        for (int i = 0; i < goodNumber; i++) {
            System.out.println("商品的名称为：" + goods[i].goodName);
            System.out.println("商品的编号为：" + goods[i].goodId);
            System.out.println("商品的生产者为：" + goods[i].producer);
            System.out.println("商品的生产时间为：" + goods[i].produceTime);
            System.out.println("商品的类型为：" + goods[i].type);
            System.out.println("商品的采购价格为：" + goods[i].purchasePrice);
            System.out.println("商品的零售价格为：" + goods[i].retailPrice);
            System.out.println("商品的数量库存为：" + goods[i].number);
            System.out.println("-----------------");
        }
    }

    public void addGood() {
        System.out.println("是否要添加商品（1.是 2.否）？");
        Scanner sc = Tools.SCANNER;
        int result = 0;
        while (true) {
            try {
                result = sc.nextInt();
                if(result == 1||result == 2) {
                    break;
                }
                else{
                    System.out.println("输入错误，请重新输入1/2：");
                }
            }
            catch (Exception e){
                System.out.println("输入错误，请重新输入1/2：");
                sc.nextLine();
            }
        }
        if (result==1) {
            goodNumber = Tools.loadAllGoods(goods, "src/gooodsInformation");
            for (; true; ) {
                if (goodNumber >= goods.length) {
                    Good[] newGoods = new Good[goods.length * 2];
                    System.arraycopy(goods, 0, newGoods, 0, goods.length);
                    goods = newGoods;
                    System.out.println("商品容量已扩容至：" + goods.length);
                }
                goods[goodNumber] = new Good();
                Tools.inputProductInformation(goods[goodNumber]);
                String goodsFolderPath = "src/gooodsInformation/" + goods[goodNumber].goodId;
                boolean saved = Tools.writeGoodsInformationToFile(goods[goodNumber], goodsFolderPath);
                if (!saved) {
                    System.out.println("警告：商品【" + goods[goodNumber].goodName + "】信息保存失败");
                }
                goodNumber++;
                System.out.println("添加商品成功");
                System.out.println("是否要继续添加商品（1.是 2.否）：");
                while (true) {
                    try {
                        result = sc.nextInt();
                        if(result == 1||result == 2) {
                            break;
                        }
                        else{
                            System.out.println("输入错误，请重新输入1/2：");
                        }
                    }
                    catch (Exception e){
                        System.out.println("输入错误，请重新输入1/2：");
                        sc.nextLine();
                    }
                }
                if (!(result==1)) {
                    break;
                }
            }
        } else if (result==2) {
            System.out.println("添加商品取消");
        }
    }

    public void changeGoodInformation() {
        goodNumber = Tools.loadAllGoods(goods, "src/gooodsInformation");
        if (goodNumber == 0) {
            return;
        }
        Scanner sc = Tools.SCANNER;
        System.out.println("是否要改变商品信息（1.是，2.否）：");
        int result = 0;
        while (true) {
            try {
                result = sc.nextInt();
                if(result == 1||result == 2) {
                    break;
                }
                else{
                    System.out.println("输入错误，请重新输入1/2：");
                }
            }
            catch (Exception e){
                System.out.println("输入错误，请重新输入1/2：");
                sc.nextLine();
            }
        }
        if (result==1) {
            int i;
            for (; true; ) {
                System.out.println("请输入商品名称：");
                String goodName;
                do {
                    goodName = sc.nextLine();
                } while (goodName.trim().isEmpty());
                goodName = goodName.trim();
                for (i = 0; i < goodNumber; i++) {
                    if (goods[i].goodName.equals(goodName)) {
                        for (; true; ) {
                            System.out.println("请问要改变该商品的哪个信息（1.名称 2.编号 3.生产者 4.生产日期 5.类型 6.采购价 7.零售价 8.库存）：");
                            String change = sc.next();
                            switch (change) {
                                case "1":
                                    System.out.println("请输入新的商品名称：");
                                    String newName;
                                    do {
                                        newName = sc.nextLine();
                                    } while (newName.trim().isEmpty());
                                    goods[i].goodName = newName.trim();
                                    break;
                                case "2":
                                    String oldGoodId = goods[i].goodId;
                                    while (true) {
                                        System.out.println("请输入新的商品编号（格式：4位数字，比如0001）：");
                                        String inputId = sc.next();
                                        if (!inputId.matches("^\\d{4}$")) {
                                            System.out.println("编号格式错误，必须输入4位纯数字（例如：0001）！");
                                            continue;
                                        }
                                        if (inputId.equals(oldGoodId)) {
                                            goods[i].goodId = inputId;
                                            break;
                                        }
                                        File newFolder = new File("src/gooodsInformation/" + inputId);
                                        if (newFolder.exists()) {
                                            System.out.println("编号 " + inputId + " 已存在，不能与现有商品编号冲突，请重新输入：");
                                            continue;
                                        }
                                        File oldFolder = new File("src/gooodsInformation/" + oldGoodId);
                                        if (oldFolder.exists() && !oldFolder.renameTo(newFolder)) {
                                            System.out.println("商品目录重命名失败（可能被占用），编号修改未生效，请重新输入：");
                                            continue;
                                        }
                                        goods[i].goodId = inputId;
                                        break;
                                    }
                                    break;
                                case "3":
                                    System.out.println("请输入新的商品生产者：");
                                    String newProducer;
                                    do {
                                        newProducer = sc.nextLine();
                                    } while (newProducer.trim().isEmpty());
                                    goods[i].producer = newProducer.trim();
                                    break;
                                case "4":
                                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                    while (true) {
                                        System.out.println("请输入新的商品生产日期（格式：yyyy-MM-dd）：");
                                        String dateInput = sc.next();
                                        try {
                                            java.time.LocalDate.parse(dateInput, formatter);
                                            goods[i].produceTime = dateInput;
                                            break;
                                        } catch (java.time.format.DateTimeParseException e) {
                                            System.out.println("日期格式错误，请严格按照 yyyy-MM-dd 的格式输入（例如：2026-09-19）！");
                                        }
                                    }
                                    break;
                                case "5":
                                    System.out.println("请输入新的商品类型：");
                                    String newType;
                                    do {
                                        newType = sc.nextLine();
                                    } while (newType.trim().isEmpty());
                                    goods[i].type = newType.trim();
                                    break;
                                case "6":
                                    System.out.println("请输入新的采购价格（必须大于0）：");
                                    double purchasePrice = 0;
                                    while(true) {
                                        try {
                                            purchasePrice = sc.nextDouble();
                                            if (purchasePrice > 0) {
                                                break;
                                            }
                                            else {
                                                System.out.println("采购价格必须大于0，请重新输入：");
                                            }
                                        }
                                        catch (Exception e) {
                                            System.out.println("输入错误，请输入一个大于0的数字：");
                                            sc.nextLine();
                                        }
                                    }
                                    goods[i].purchasePrice = purchasePrice;
                                    break;
                                case "7":
                                    System.out.println("请输入新的零售价格（必须大于0）：");
                                    double retailPrice = 0;
                                    while(true) {
                                        try {
                                            retailPrice = sc.nextDouble();
                                            if (retailPrice > 0) {
                                                break;
                                            }
                                            else {
                                                System.out.println("零售价格必须大于0，请重新输入：");
                                            }
                                        }
                                        catch (Exception e) {
                                            System.out.println("输入错误，请输入一个大于0的数字：");
                                            sc.nextLine();
                                        }
                                    }
                                    goods[i].retailPrice = retailPrice;
                                    break;
                                case "8":
                                    System.out.println("请输入新的数量库存（必须大于等于0）：");
                                    int number = 0;
                                    while(true) {
                                        try {
                                            number = sc.nextInt();
                                            if (number >= 0) {
                                                break;
                                            }
                                            else {
                                                System.out.println("库存必须大于等于0，请重新输入：");
                                            }
                                        }
                                        catch (Exception e) {
                                            System.out.println("输入错误，请输入一个大于等于0的整数：");
                                            sc.nextLine();
                                        }
                                    }
                                    goods[i].number = number;
                                    break;
                                default:
                                    System.out.println("输入无效！");
                                    continue;
                            }
                            System.out.println("改变成功");
                            boolean saved = Tools.writeGoodsInformationToFile(goods[i], "src/gooodsInformation/" + goods[i].goodId);
                            if (!saved) {
                                System.out.println("警告：商品信息保存失败");
                            }
                            System.out.println("是否要继续改变其他商品信息（1.是 2.否）：");
                            while (true) {
                                try{
                                    result = sc.nextInt();
                                    if (result == 1 || result == 2) {
                                        break;
                                    }
                                    else {
                                        System.out.println("输入无效！请输入1或2：");
                                    }
                                }
                                catch (Exception e) {
                                    System.out.println("输入错误，请输入1或2：");
                                    sc.nextLine();
                                }
                            }
                            if (result==1) {
                                break;
                            }
                            else if (result==2) {
                                return;
                            }
                        }
                        break;
                    }
                }
                if (i == goodNumber) {
                    System.out.println("该商品不存在，请重新输入：");
                }
                else {
                    continue;
                }
            }
        }
    }

    public void deleteGoodInformation() {
        goodNumber = Tools.loadAllGoods(goods, "src/gooodsInformation");
        if (goodNumber == 0) {
            return;
        }
        Scanner sc = Tools.SCANNER;
        System.out.println("请输入商品ID（格式xxxx（x是数字）比如0001）：");
        String goodId = sc.next();
        for (int i = 0; true; i++) {
            if (i == goodNumber) {
                System.out.println("该商品不存在，请重新输入：");
                goodId = sc.next();
                i = -1;
                continue;
            }
            if (goods[i].goodId.equals(goodId)) {
                System.out.println("确认删除吗？（1.是 2.否）：");
                int result = 0;
                while (true) {
                    try {
                        result = sc.nextInt();
                        if (result == 1 || result == 2) {
                            break;
                        }
                        else {
                            System.out.println("输入无效！请输入1或2：");
                        }
                    }
                    catch (Exception e) {
                        System.out.println("输入错误，请输入1或2：");
                        sc.nextLine();
                    }
                }
                if (!(result==1)) {
                    System.out.println("删除操作已取消");
                    return;
                }
                for (int j = i; j < goodNumber - 1; j++) {
                    goods[j] = goods[j + 1];
                }
                goods[goodNumber - 1] = null;
                goodNumber--;
                File goodFolder = new File("src/gooodsInformation/" + goodId);
                if (goodFolder.exists()) {
                    File[] allFiles = goodFolder.listFiles();
                    if (allFiles != null) {
                        for (File file : allFiles) {
                            file.delete();
                        }
                    }
                    goodFolder.delete();
                }
                System.out.println("删除成功");
                break;
            }
        }
    }

    public void checkGoodInformation() {
        goodNumber = Tools.loadAllGoods(goods, "src/gooodsInformation");
        if (goodNumber == 0) {
            return;
        }
        Scanner sc = Tools.SCANNER;
        for (; true; ) {
            System.out.println("请输入商品名称（不输入请选择no）：");
            String goodName;
            do {
                goodName = sc.nextLine();
            } while (goodName.trim().isEmpty());
            goodName = goodName.trim();
            System.out.println("请输入生产厂家（不输入请选择no）：");
            String producer;
            do {
                producer = sc.nextLine();
            } while (producer.trim().isEmpty());
            producer = producer.trim();
            System.out.println("请输入零售价格下限（不输入请选择0）：");
            double retailPrice = 0;
            while(true) {
                try {
                    retailPrice = sc.nextDouble();
                    break;
                }
                catch (Exception e) {
                    System.out.println("输入错误，请输入数字：");
                    sc.nextLine();
                }
            }
            if (goodName.equals("no") && producer.equals("no") && retailPrice == 0) {
                System.out.println("无法查询！！请重新输入：");
                continue;
            }
            Good[] result = new Good[goodNumber];
            int resultCount = 0;
            for (int i = 0; i < goodNumber; i++) {
                Good current = goods[i];
                boolean match = true;
                if (!goodName.equals("no") && !current.goodName.equals(goodName)) {
                    match = false;
                }
                if (!producer.equals("no") && !current.producer.equals(producer)) {
                    match = false;
                }
                if (retailPrice != 0 && current.retailPrice < retailPrice) {
                    match = false;
                }
                if (match) {
                    result[resultCount] = current;
                    resultCount++;
                }
            }
            if (resultCount == 0) {
                System.out.println("未找到符合条件的商品。");
            }
            else {
                System.out.println("========== 查询结果 ==========");
                for (int i = 0; i < resultCount; i++) {
                    Good g = result[i];
                    System.out.println("商品名称：" + g.goodName + "，编码：" + g.goodId + "，厂家：" + g.producer + "，零售价：" + g.retailPrice);
                }
            }
            break;
        }
    }

    public void changePassword() {
        for (; true; ) {
            System.out.println("请输入旧密码：");
            Scanner sc = Tools.SCANNER;
            String oldPassword = sc.next();
            if (Tools.hashPassword(oldPassword).equals(this.getPassword())) {
                System.out.println("请输入新密码（密码长度大于8个字符，必须是大小写字母，数字和标点符号的组合）：");
                for (; true; ) {
                    String newPassword = sc.next();
                    if (Tools.isValidPassword(newPassword)) {
                        if (!this.getPassword().equals(Tools.hashPassword(newPassword))) {
                            this.setPassword(newPassword);
                            boolean saved = Tools.writeAdminToFile(
                                    "src/adminInformation/" + this.getUserId(),
                                    this.getUserId() + "_information.xlsx",
                                    this.getUserName(),
                                    this.getUserId(),
                                    this.getUserPhoneNumber(),
                                    this.getPassword()
                            );
                            if (saved) {
                                System.out.println("密码修改成功");
                                break;
                            } else {
                                System.out.println("密码修改信息保存失败，请重新输入新密码");
                            }
                        } else {
                            System.out.println("新密码不能与旧密码相同,请重新输入新密码");
                        }
                    } else {
                        System.out.println("新密码格式错误,请重新输入新密码");
                    }
                }
                break;
            } else {
                System.out.println("旧密码错误,请重新输入旧密码");
            }
        }
    }

    public boolean login(User user) {
        Scanner scanner = Tools.SCANNER;
        System.out.println("请输入用户名（首次注册的管理员请输入 admin）：");
        String inputUserName = scanner.next();
        if (inputUserName.equals("admin")) {
            System.out.println("请输入管理员密码：");
            String adminPassword = scanner.next();
            if (adminPassword.equals("ynuinfo#777")) {
                user.setUserName("admin");
                user.setPassword(Tools.hashPassword("ynuinfo#777"));
                user.setUserId("admin12345678900");
                user.setUserPhoneNumber("");
                return true;
            } else {
                System.out.println("管理员密码错误,登录失败！");
                return false;
            }
        }
        user.setUserName(inputUserName);
        String inputUserPhoneNumber = "";
        while (true) {
            System.out.println("请输入手机号：");
            inputUserPhoneNumber = scanner.next();
            if (inputUserPhoneNumber.matches("^1[3-9]\\d{9}$")) {
                break;
            } else {
                System.out.println("手机号格式错误，请输入11位有效的中国手机号！");
            }
        }
        user.setUserPhoneNumber(inputUserPhoneNumber);
        String inputUserId = inputUserName + inputUserPhoneNumber;
        user.setUserId(inputUserId);
        System.out.println("请输入密码：");
        String inputPassword = scanner.next();
        user.setPassword(Tools.hashPassword(inputPassword));
        String folderPath = "src/adminInformation/" + getUserId();
        String fileName = inputUserId + "_information.xlsx";
        String[] adminInfo = Tools.readAdminFromFile(folderPath, fileName);
        if (adminInfo == null) {
            System.out.println("该管理员不存在！");
            return false;
        }
        String filePassword = adminInfo[3];
        if (filePassword != null && filePassword.equals(Tools.hashPassword(inputPassword))) {
            System.out.println("登录成功！");
            return true;
        } else {
            System.out.println("密码错误，登录失败！");
            return false;
        }
    }
}

