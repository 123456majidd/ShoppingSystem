package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ShoppingSystem {
    Admin admin = new Admin();
    Customer customer = new Customer();
    public void start() {
        try {
            for (; true; ) {
                System.out.println("欢迎来到购物系统：");
                System.out.println("1. 管理员登录");
                System.out.println("2. 客户登录");
                System.out.println("3. 客户注册");
                System.out.println("4. 退出系统");
                System.out.println("请输入您的选择：");
                Scanner sc = Tools.SCANNER;
                int loginChoice = 0;
                while(true){
                    try{
                        loginChoice = sc.nextInt();
                        break;
                    }
                    catch(Exception e){
                        System.out.println("请输入一个整数！");
                        sc.nextLine();
                        continue;
                    }
                }
                switch(loginChoice) {
                    case 1:
                        if (!Tools.chioceIfContinue()) {
                            break;
                        }
                        if(admin.login(admin)){
                            System.out.println("管理员登录成功");
                            if(admin.getUserName().equals("admin")&&admin.getPassword().equals("ynuinfo#777")){
                                String userPhoneNumber = "";
                                while (true) {
                                    System.out.println("请输入您的手机号：");
                                    userPhoneNumber = sc.next();
                                    if (userPhoneNumber.matches("^1[3-9]\\d{9}$")) {
                                        break;
                                    } else {
                                        System.out.println("手机号格式错误，请输入11位有效的中国大陆手机号（例如：13812345678）！");
                                    }
                                }
                                admin.setUserPhoneNumber(userPhoneNumber);
                                System.out.println("请输入您的新用户名：");
                                String userName = "";
                                while (true) {
                                    userName = sc.next();
                                    if (Tools.isValidUserName(userName)) {
                                        break;
                                    }
                                    System.out.println("用户名不合法：不能为空、不能含空格或 / \\ : * ? \" < > | 等字符，长度不超过20，请重新输入：");
                                }
                                admin.setUserName(userName);
                                System.out.println("您的用户ID成功注册为："+userName+userPhoneNumber);
                                admin.setUserId(userName+userPhoneNumber);
                                String userFolderPath = "src/adminInformation/" + admin.getUserId();
                                File userFolder = new File(userFolderPath);
                                if (!userFolder.exists()) {
                                    boolean createOk = userFolder.mkdirs();
                                    if (!createOk) {
                                        System.out.println("管理员文件夹创建失败，请检查路径权限");
                                        return;
                                    }
                                }
                                File infoFile = new File(userFolder, admin.getUserId()+"_information.txt");
                                boolean adminSaved = false;
                                try (BufferedWriter bw = new BufferedWriter(new FileWriter(infoFile, StandardCharsets.UTF_8))) {
                                    bw.write("用户名：" + admin.getUserName());
                                    bw.newLine();
                                    bw.write("用户ID：" + admin.getUserId());
                                    bw.newLine();
                                    bw.write("手机号：" + admin.getUserPhoneNumber());
                                    bw.newLine();
                                    bw.write("密码：" + admin.getPassword());
                                    bw.newLine();
                                    adminSaved = true;
                                }
                                catch (IOException e) {
                                    System.out.println("用户信息保存失败：" + e.getMessage());
                                    e.printStackTrace();
                                }
                                if (!adminSaved) {
                                    System.out.println("管理员信息未保存成功，请检查磁盘空间与目录权限后重新操作");
                                    return;
                                }
                                System.out.println("请修改您的密码（修改后请使用 新用户名 + 手机号 方式登录）：");
                                admin.changePassword();
                            }
                            for(;true;){
                                System.out.println("____菜单____");
                                System.out.println("1. 初始化客户信息");
                                System.out.println("2. 初始化商品信息");
                                System.out.println("3. 重置客户密码");
                                System.out.println("4. 列出客户信息");
                                System.out.println("5. 检查客户信息");
                                System.out.println("6. 删除客户信息");
                                System.out.println("7. 列出商品信息");
                                System.out.println("8. 添加商品");
                                System.out.println("9. 修改商品信息");
                                System.out.println("10. 删除商品信息");
                                System.out.println("11. 检查商品信息");
                                System.out.println("12. 修改密码");
                                System.out.println("13. 退出系统");
                                System.out.println("请输入您的选择：");
                                int choice = 0;
                                while(true) {
                                    try{
                                        choice = sc.nextInt();
                                        break;
                                    }
                                    catch(Exception e){
                                        System.out.println("请输入1-13之间的数字！");
                                        sc.nextLine();
                                        continue;
                                    }
                                }
                                switch(choice) {
                                    case 1:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        admin.initializeCustomers();
                                        break;
                                    case 2:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        admin.initializeGoods();
                                        break;
                                    case 3:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        admin.resetCustomerPassword();
                                        break;
                                    case 4:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        admin.listCustomerInformation();
                                        break;
                                    case 5:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        admin.checkCustomerInformation();
                                        break;
                                    case 6:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        admin.deleteCustomerInformation();
                                        break;
                                    case 7:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        admin.listGoodsInformation();
                                        break;
                                    case 8:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        admin.addGood();
                                        break;
                                    case 9:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        admin.changeGoodInformation();
                                        break;
                                    case 10:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        admin.deleteGoodInformation();
                                        break;
                                    case 11:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        admin.checkGoodInformation();
                                        break;
                                    case 12:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        admin.changePassword();
                                        break;
                                    case 13:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        this.end();
                                        admin.logout();
                                        break;
                                    default:
                                        System.out.println("输入错误，请重新输入");
                                        break;
                                }
                            }
                        }
                        else{
                            System.out.println("登录失败，请重新选择操作！");
                        }
                        break;
                    case 2:
                        if (!Tools.chioceIfContinue()) {
                            break;
                        }
                        if(customer.login(customer)){
                            for(;true;){
                                System.out.println("____菜单____");
                                System.out.println("1. 添加商品到购物车");
                                System.out.println("2. 修改商品数量");
                                System.out.println("3. 从购物车删除商品");
                                System.out.println("4. 结算购物车");
                                System.out.println("5. 检查购物历史");
                                System.out.println("6. 修改密码");
                                System.out.println("7. 退出系统");
                                int choice = 0;
                                while(true) {
                                    try{
                                        choice = sc.nextInt();
                                        break;
                                    }
                                    catch(Exception e){
                                        System.out.println("请输入1-7之间的数字！");
                                        sc.nextLine();
                                        continue;
                                    }
                                }
                                switch(choice) {
                                    case 1:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        customer.addGoodToShoppingCart();
                                        break;
                                    case 2:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        customer.changeGoodToShoppingCart();
                                        break;
                                    case 3:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        customer.deleteGoodFromShoppingCart();
                                        break;
                                    case 4:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        customer.checkOutShoppingCart();
                                        break;
                                    case 5:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        customer.checkShoppingHistory();
                                        break;
                                    case 6:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        customer.changePassword();
                                        break;
                                    case 7:
                                        if (!Tools.chioceIfContinue()) {
                                            break;
                                        }
                                        this.end();
                                        break;
                                    default:
                                        System.out.println("输入错误，请重新输入");
                                        break;
                                }
                            }
                        }
                        else{
                            System.out.println("登录失败");
                            if (customer.isLastLoginLocked()) {
                                System.out.println("您是否忘记密码？（1. 是 2. 否）");
                                int choice = 0;
                                while(true) {
                                    try{
                                        choice = sc.nextInt();
                                        if(choice == 1 || choice == 2 ) {
                                            break;
                                        }
                                        else {
                                            System.out.println("请输入1-2之间的数字！");
                                        }
                                    }
                                    catch(Exception e){
                                        System.out.println("请输入1-2之间的数字！");
                                        sc.nextLine();
                                        continue;
                                    }
                                }
                                if(choice == 1){
                                    customer.resetPassword();
                                }
                                else{
                                    System.out.println("已返回主界面。");
                                }
                            }
                        }
                        break;
                    case 3:
                        if (!Tools.chioceIfContinue()) {
                            break;
                        }
                        customer.register();
                        break;
                    case 4:
                        if (!Tools.chioceIfContinue()) {
                            break;
                        }
                        customer.logout();
                        this.end();
                        break;
                    default:
                        System.out.println("输入错误，请重新输入");
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("输入已终止或发生未处理异常，系统安全退出：" + e.getMessage());
        }
    }

    public void end(){
        System.exit(0);
    }
}
