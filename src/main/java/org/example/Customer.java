package org.example;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Customer extends User {
    private String level;
    private String registerTime;
    private double totalSpent;
    private String email;
    private ShoppingCart shoppingCart = new ShoppingCart();
    private int failedLoginCount = 0;
    private boolean locked = false;
    private boolean lastLoginLocked = false;

    public boolean isLastLoginLocked() {
        return lastLoginLocked;
    }

    public Customer() {
        this.setUserName("Customer");
        this.setPassword("ynuinfo#777");
        this.setLevel("铜牌会员");
        this.totalSpent = 0;
    }

    public Customer(String name, String ID, String level, String registerTime, double totalSpent, String email, String phoneNumber) {
        this.setUserName(name);
        this.setUserId(ID);
        this.setUserPhoneNumber(phoneNumber);
        this.setLevel(level);
        this.setRegisterTime(registerTime);
        this.setTotalSpent(totalSpent);
        this.setEmail(email);
    }

    public void setLevel(String level) { this.level = level; }
    public void setRegisterTime(String registerTime) { this.registerTime = registerTime; }
    public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }
    public void setEmail(String email) { this.email = email; }
    public void setShoppingCart(ShoppingCart shoppingCart) { this.shoppingCart = shoppingCart; }
    public void setFailedLoginCount(int failedLoginCount) { this.failedLoginCount = failedLoginCount; }
    public void setLocked(boolean locked) { this.locked = locked; }
    public String getLevel() { return level; }
    public String getRegisterTime() { return registerTime; }
    public double getTotalSpent() { return totalSpent; }
    public String getEmail() { return email; }
    public ShoppingCart getShoppingCart() { return shoppingCart; }
    public int getFailedLoginCount() { return failedLoginCount; }
    public boolean isLocked() { return locked; }

    public void register() {
        Scanner sc = Tools.SCANNER;
        System.out.println("欢迎来到我们的购物系统！");
        System.out.println("您确定要注册吗？（1.是；2.否）");
        int choice = 0;
        while(true) {
            try{
                choice = sc.nextInt();
                if(choice == 1 || choice == 2) {
                    break;
                }
                else {
                    System.out.println("请输入1或2！");
                }
            }
            catch(Exception e) {
                System.out.println("请输入1或2！");
                sc.nextLine();
                continue;
            }
        }
        if (!(choice == 1)) {
            System.out.println("已取消注册。");
            return;
        }
        while (true) {
            System.out.println("请输入您的用户名：");
            String name = sc.next();
            if (Tools.isValidUserName(name)) {
                this.setUserName(name);
                break;
            }
            System.out.println("用户名不合法：不能为空、不能含字符，长度不少于5个字符，请重新输入：");
        }
        while (true) {
            System.out.println("请输入您的手机号：");
            String phone = sc.next();
            if (phone.matches("^1[3-9]\\d{9}$")) {
                this.setUserPhoneNumber(phone);
                break;
            }
            else {
                System.out.println("手机号格式错误，请输入11位有效的中国手机号！");
            }
        }
        while (true) {
            System.out.println("请输入您的邮箱：");
            String email = sc.next();
            if (email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                this.setEmail(email);
                break;
            }
            else {
                System.out.println("邮箱格式错误，请输入有效的邮箱地址（例如：zhangsan@example.com）！");
            }
        }
        this.setUserId(this.getUserName() + this.getUserPhoneNumber());
        if (Tools.customerExists(this.getUserId())) {
            System.out.println("该账号已存在，请登录！");
            return;
        }
        for (; true; ) {
            System.out.println("请输入您的密码（密码长度大于8个字符，必须是大小写字母，数字和标点符号的组合）：");
            String temPassword = sc.next();
            if (Tools.isValidPassword(temPassword)) {
                this.setPassword(temPassword);
                break;
            }
            else {
                System.out.println("密码格式错误，请重新输入：");
            }
        }
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (true) {
            System.out.println("请输入您的注册时间（格式为yyyy-MM-dd）：");
            String dateInput = sc.next();
            try {
                java.time.LocalDate.parse(dateInput, formatter);
                this.setRegisterTime(dateInput);
                break;
            } catch (java.time.format.DateTimeParseException e) {
                System.out.println("日期格式错误，请严格按照 yyyy-MM-dd 的格式输入（例如：2026-09-19）！");
            }
        }
        boolean saved = Tools.writeCustomerInformationToFile(this, this.getUserId());
        if (!saved) {
            System.out.println("客户信息保存失败，注册未完成，请重新注册！");
            return;
        }
        Tools.initShoppingHistory(this.getUserId());
        System.out.println("注册成功！");
    }

    public void resetPassword() {
        System.out.println("您确定要重置密码吗？（1.是；2.否）");
        Scanner sc = Tools.SCANNER;
        int choice = 0;
        while(true) {
            try{
                choice = sc.nextInt();
                if(choice == 1 || choice == 2) {
                    break;
                }
                else {
                    System.out.println("请输入1或2！");
                }
            }
            catch(Exception e) {
                System.out.println("请输入1或2！");
                sc.nextLine();
                continue;
            }
        }
        if (choice != 1) {
            System.out.println("密码重置取消。");
            return;
        }
        System.out.println("请输入您的用户名：");
        String inputName = sc.next();
        System.out.println("请输入您的注册邮箱：");
        String inputEmail = sc.next();
        Customer matched = Tools.findCustomerByUserNameAndEmail(inputName, inputEmail);
        if (matched == null) {
            System.out.println("用户名或注册邮箱不匹配，未找到该账户！");
            return;
        }
        String newPassword = Tools.generateRandomPassword();
        matched.setPassword(newPassword);
        matched.setFailedLoginCount(0);
        matched.setLocked(false);
        boolean saved = Tools.writeCustomerInformationToFile(matched, matched.getUserId());
        if (saved) {
            System.out.println("系统已生成随机密码并发送到您的注册邮箱 " + matched.getEmail() + "，请使用该密码登录！");
        } else {
            System.out.println("密码重置信息保存失败，请重新尝试！");
        }
    }

    public void addGoodToShoppingCart() {
        Scanner sc = Tools.SCANNER;
        if (this.shoppingCart == null) {
            this.shoppingCart = new ShoppingCart();
        }
        int cartIndex = 0;
        for (int k = 0; k < this.shoppingCart.goods.length; k++) {
            if (this.shoppingCart.goods[k] != null) {
                cartIndex++;
            }
            else {
                break;
            }
        }
        this.shoppingCart.numberOfGoods = cartIndex;
        for (; true; ) {
            String goodId = "";
            while (true) {
                System.out.println("请输入要添加的商品的ID（格式xxxx（x是数字）比如0001）：");
                goodId = sc.next();
                if (goodId.matches("^\\d{4}$")) {
                    break;
                }
                else {
                    System.out.println("商品ID格式错误，必须输入4位纯数字（例如：0001）！");
                }
            }
            File goodFolder = new File("src/gooodsInformation/" + goodId);
            Good cartGood = Tools.readGoodsInformationFromFolder(goodFolder);
            if (cartGood == null) {
                System.out.println("商品不存在，请重新输入：");
                continue;
            }
            if (cartIndex >= this.shoppingCart.goods.length) {
                System.out.println("购物车已满，无法添加更多商品");
                continue;
            }
            System.out.println("请问您要添加的" + cartGood.goodName + "的数量是多少？");
            while (!sc.hasNextInt()) {
                System.out.println("输入无效，请输入整数数量：");
                sc.next();
            }
            int quantity = sc.nextInt();
            if (quantity > cartGood.number) {
                System.out.println("库存不足，当前库存：" + cartGood.number);
                continue;
            }
            if (quantity <= 0) {
                System.out.println("添加数量必须大于0");
                continue;
            }
            boolean found = false;
            for (int j = 0; j < cartIndex; j++) {
                if (shoppingCart.goods[j] != null && shoppingCart.goods[j].goodId.equals(cartGood.goodId)) {
                    int newQuantity = shoppingCart.goods[j].number + quantity;
                    if (newQuantity > cartGood.number) {
                        System.out.println("库存不足，当前库存：" + cartGood.number + "，购物车已有：" + shoppingCart.goods[j].number);
                        found = true;
                        break;
                    }
                    shoppingCart.goods[j].number = newQuantity;
                    found = true;
                    System.out.println("商品已在购物车中，数量已累加为：" + newQuantity);
                    break;
                }
            }
            if (!found) {
                cartGood.number = quantity;
                this.shoppingCart.goods[cartIndex] = cartGood;
                cartIndex++;
                this.shoppingCart.numberOfGoods = cartIndex;
                System.out.println("商品已成功加入购物车");
            }

            System.out.println("请问您还要继续添加商品吗？（1.是；2.否）");
            while (!sc.hasNextInt()) {
                System.out.println("输入无效，请输入1或2：");
                sc.next();
            }
            int choice = sc.nextInt();
            if (choice != 1) {
                System.out.println("商品添加完成！");
                break;
            }
        }
    }

    public void changeGoodToShoppingCart() {
        Scanner sc = Tools.SCANNER;
        for (; true; ) {
            String goodId = "";
            while (true) {
                System.out.println("请输入要添加的商品的ID（格式xxxx（x是数字）比如0001）：");
                goodId = sc.next();
                if (goodId.matches("^\\d{4}$")) {
                    break;
                }
                else {
                    System.out.println("商品ID格式错误，必须输入4位纯数字（例如：0001）！");
                }
            }
            int i;
            boolean foundInCart = false;
            for (i = 0; i < this.shoppingCart.numberOfGoods; i++) {
                if (goodId.equals(this.shoppingCart.goods[i].goodId)) {
                    foundInCart = true;
                    System.out.println("请输入该商品的新数量（输入小于等于0的数量将从购物车清除该商品）：");
                    while(true) {
                        int number = 0;
                        try{
                            number = sc.nextInt();
                        }
                        catch (Exception e) {
                            System.out.println("输入无效，请输入整数数量：");
                            sc.nextLine();
                            continue;
                        }
                        if (number <= 0) {
                            String removedName = this.shoppingCart.goods[i].goodName;
                            for (int k = i; k < this.shoppingCart.numberOfGoods - 1; k++) {
                                this.shoppingCart.goods[k] = this.shoppingCart.goods[k + 1];
                            }
                            this.shoppingCart.goods[this.shoppingCart.numberOfGoods - 1] = null;
                            this.shoppingCart.numberOfGoods--;
                            System.out.println("数量小于等于0，商品【" + removedName + "】已从购物车清除");
                            break;
                        }
                        Good stock = Tools.readGoodsInformationFromFolder(new File("src/gooodsInformation/" + goodId));
                        if (stock == null) {
                            System.out.println("商品数据缺失，无法修改数量，请重新输入：");
                            continue;
                        }
                        if (number > stock.number) {
                            System.out.println("库存不足，当前库存：" + stock.number + "，修改失败，请重新输入数量：");
                            continue;
                        }
                        this.shoppingCart.goods[i].number = number;
                        System.out.println("商品修改成功！");
                        break;
                    }
                    break;
                }
            }
            if (!foundInCart) {
                System.out.println("商品不存在，请重新输入：");
            }
            else {
                System.out.println("请问您还要继续修改其他商品数量吗？（1.是；2.否）");
                int choice = 0;
                while(true) {
                    try{
                        choice = sc.nextInt();
                        if (choice == 1 || choice == 2) {
                            break;
                        }
                        else {
                            System.out.println("输入无效，请输入1或2：");
                        }
                    }
                    catch (Exception e) {
                        System.out.println("输入无效，请输入1或2：");
                        sc.nextLine();
                    }
                }
                if (choice != 1) {
                    break;
                }
            }
        }
    }

    public void deleteGoodFromShoppingCart() {
        Scanner sc = Tools.SCANNER;
        if (this.shoppingCart == null || this.shoppingCart.goods == null) {
            System.out.println("购物车为空，无法删除商品");
            return;
        }
        for (; true; ) {
            int cartSize = 0;
            for (int k = 0; k < this.shoppingCart.goods.length; k++) {
                if (this.shoppingCart.goods[k] != null) {
                    cartSize++;
                }
                else {
                    break;
                }
            }
            if (cartSize == 0) {
                System.out.println("购物车已空，没有可删除的商品");
                break;
            }
            String goodId = "";
            while (true) {
                System.out.println("请输入要添加的商品的ID（格式xxxx（x是数字）比如0001）：");
                goodId = sc.next();
                if (goodId.matches("^\\d{4}$")) {
                    break;
                } else {
                    System.out.println("商品ID格式错误，必须输入4位纯数字（例如：0001、1234）！");
                }
            }
            int foundIndex = -1;
            for (int i = 0; i < this.shoppingCart.goods.length; i++) {
                Good current = this.shoppingCart.goods[i];
                if (current == null) {
                    break;
                }
                if (current.goodId.equals(goodId)) {
                    foundIndex = i;
                    break;
                }
            }
            if (foundIndex == -1) {
                System.out.println("购物车中不存在该商品，请重新输入：");
                continue;
            }
            System.out.println("您确定要删除商品" + this.shoppingCart.goods[foundIndex].goodName + "吗？（1.是；2.否）");
            int choice = 0;
            while(true) {
                try{
                    choice = sc.nextInt();
                    if (choice == 1 || choice == 2) {
                        break;
                    }
                    else {
                        System.out.println("输入无效，请输入1或2：");
                    }
                }
                catch (Exception e) {
                    System.out.println("输入无效，请输入1或2：");
                    sc.next();
                }
            }
            if (choice == 1) {
                for (int j = foundIndex; j < cartSize - 1; j++) {
                    this.shoppingCart.goods[j] = this.shoppingCart.goods[j + 1];
                }
                this.shoppingCart.goods[cartSize - 1] = null;
                this.shoppingCart.numberOfGoods = cartSize - 1;
                System.out.println("商品删除成功！");
            } else {
                System.out.println("商品删除取消。");
            }
            System.out.println("请问您还要继续删除商品吗？（1.是；2.否）");
            int goOn = 0;
            while(true) {
                try{
                    goOn = sc.nextInt();
                    if (goOn == 1 || goOn == 2) {
                        break;
                    }
                    else {
                        System.out.println("输入无效，请输入1或2：");
                    }
                }
                catch (Exception e) {
                    System.out.println("输入无效，请输入1或2：");
                    sc.next();
                }
            }
            if (goOn == 2) {
                break;
            }
        }
    }

    public void checkOutShoppingCart() {
        Scanner sc = Tools.SCANNER;
        if (shoppingCart == null || shoppingCart.numberOfGoods <= 0) {
            System.out.println("购物车为空，请先添加商品再结算");
            return;
        }
        System.out.println("您的购物车商品如下：");
        double orderTotal = 0;
        for (int i = 0; i < shoppingCart.numberOfGoods; i++) {
            System.out.print("商品名：" + shoppingCart.goods[i].goodName);
            System.out.print(" 数量：" + shoppingCart.goods[i].number);
            System.out.println(" 商品价格：" + shoppingCart.goods[i].retailPrice);
            System.out.println("该商品总价：" + (shoppingCart.goods[i].retailPrice * shoppingCart.goods[i].number));
            orderTotal += shoppingCart.goods[i].retailPrice * shoppingCart.goods[i].number;
        }
        System.out.println("本单金额为：" + orderTotal);
        System.out.println("现在结账还是继续购物？（1.结账；2.继续购物）");
        int choice = 0;
        while(true) {
            try{
                choice = sc.nextInt();
                if (choice == 1 || choice == 2) {
                    break;
                }
                else {
                    System.out.println("输入无效，请输入1或2：");
                }
            }
            catch (Exception e) {
                System.out.println("输入无效，请输入1或2：");
                sc.nextLine();
            }
        }
        if (choice == 1) {
            boolean result = false;
            for (; true; ) {
                System.out.println("请问您要使用哪种支付方式？（1.支付宝；2.微信支付；3.银行卡支付）");
                int paymentMethod = 0;
                while(true) {
                    try{
                        paymentMethod = sc.nextInt();
                        if (paymentMethod >= 1 && paymentMethod <= 3) {
                            break;
                        }
                        else {
                            System.out.println("输入无效，请输入1-3：");
                        }
                    }
                    catch (Exception e) {
                        System.out.println("输入无效，请输入1-3：");
                        sc.nextLine();
                    }
                }
                String paymentMethodName = "";
                if (paymentMethod == 1) {
                    paymentMethodName = "支付宝";
                }
                if (paymentMethod == 2) {
                    paymentMethodName = "微信支付";
                }
                if (paymentMethod == 3) {
                    paymentMethodName = "银行卡支付";
                }
                if (paymentMethod >= 1 && paymentMethod <= 3) {
                    System.out.println(paymentMethodName + "支付中...");
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("支付成功！谢谢使用！！！");
                    result = true;
                } else {
                    System.out.println("支付方式错误，请重新输入：");
                }
                if (result) {
                    this.totalSpent += orderTotal;
                    String userFolderPath = "src/customerInformation/" + this.getUserId();
                    for (int i = 0; i < shoppingCart.numberOfGoods; i++) {
                        Good item = this.shoppingCart.goods[i];
                        Good stock = Tools.readGoodsInformationFromFolder(new File("src/gooodsInformation/" + item.goodId));
                        if (stock != null) {
                            if (stock.number >= item.number) {
                                stock.number -= item.number;
                            } else {
                                System.out.println("警告：商品【" + stock.goodName + "】库存不足（当前剩余 " + stock.number + "），已扣减至 0");
                                stock.number = 0;
                            }
                            Tools.writeGoodsInformationToFile(stock, "src/gooodsInformation/" + item.goodId);
                        } else {
                            System.out.println("警告：商品【" + item.goodId + "】不存在");
                        }
                    }
                    for (int i = 0; i < shoppingCart.numberOfGoods; i++) {
                        Good item = this.shoppingCart.goods[i];
                        double itemTotal = item.retailPrice * item.number;
                        String record = item.goodName + " | 数量：" + item.number + " | 金额：" + Tools.formatMoney(itemTotal);
                        Tools.appendShoppingHistory(userFolderPath, record);
                    }
                    Tools.appendShoppingHistory(userFolderPath, "--- 订单合计：" + Tools.formatMoney(orderTotal) + " ---");
                    System.out.println("订单记录已成功写入购物历史！");
                    boolean customerSaved = Tools.writeCustomerInformationToFile(this, userFolderPath);
                    if (!customerSaved) {
                        System.out.println("警告：总花费信息保存失败，请联系管理员！");
                    }
                    for (int i = 0; i < shoppingCart.numberOfGoods; i++) {
                        this.shoppingCart.goods[i] = null;
                    }
                    this.shoppingCart.numberOfGoods = 0;
                    System.out.println("谢谢使用！");
                    break;
                }
            }
        } else {
            System.out.println("谢谢使用！！！");
        }
    }

    public void checkShoppingHistory() {
        System.out.println("您的购物历史如下：");
        String userFolderPath = "src/customerInformation/" + this.getUserId();
        List<String> history = Tools.readShoppingHistory(userFolderPath);
        if (history.isEmpty()) {
            System.out.println("您暂无购物历史记录~");
            return;
        }
        System.out.println("\n========== 您的购物历史 ==========");
        List<String> orderNames = new ArrayList<>();
        List<Integer> orderQtys = new ArrayList<>();
        List<Double> orderAmts = new ArrayList<>();
        String currentTime = "";
        for (String line : history) {
            if (!line.startsWith("[")) {
                System.out.println(line);
                continue;
            }
            int timeEnd = line.indexOf("]");
            String time = line.substring(0, timeEnd + 1);
            String content = line.substring(timeEnd + 1).trim();
            if (content.startsWith("---")) {
                for (int i = 0; i < orderNames.size(); i++) {
                    System.out.println(currentTime + " " + orderNames.get(i)
                            + " | 数量：" + orderQtys.get(i)
                            + " | 金额：" + Tools.formatMoney(orderAmts.get(i)));
                }
                System.out.println(line);
                orderNames.clear();
                orderQtys.clear();
                orderAmts.clear();
                currentTime = "";
                continue;
            }
            currentTime = time;
            int nameEnd = content.indexOf(" | ");
            String name = content.substring(0, nameEnd);
            int qtyStart = content.indexOf("数量：") + 3;
            int qtyEnd = content.indexOf(" | ", qtyStart);
            int qty = Integer.parseInt(content.substring(qtyStart, qtyEnd).trim());
            int amtStart = content.indexOf("金额：") + 3;
            double amt = Double.parseDouble(content.substring(amtStart).trim());
            int idx = orderNames.indexOf(name);
            if (idx >= 0) {
                orderQtys.set(idx, orderQtys.get(idx) + qty);
                orderAmts.set(idx, orderAmts.get(idx) + amt);
            } else {
                orderNames.add(name);
                orderQtys.add(qty);
                orderAmts.add(amt);
            }
        }
        System.out.println("==================================\n");
    }

    public void changePassword() {
        for (; true; ) {
            System.out.println("请输入旧密码：");
            String oldPassword;
            Scanner sc = Tools.SCANNER;
            oldPassword = sc.next();
            String storedPassword = this.getPassword();
            boolean oldPasswordOk = Tools.hashPassword(oldPassword).equals(storedPassword)
                    || oldPassword.equals(storedPassword);
            if (oldPasswordOk) {
                System.out.println("请输入新密码（密码长度大于8个字符，必须是大小写字母，数字和标点符号的组合）：");
                String newPassword;
                for (; true; ) {
                    newPassword = sc.next();
                    if (Tools.isValidPassword(newPassword)) {
                        boolean sameAsOld = Tools.hashPassword(newPassword).equals(storedPassword)
                                || newPassword.equals(storedPassword);
                        if (!sameAsOld) {
                            this.setPassword(newPassword);
                            boolean saved = Tools.writeCustomerInformationToFile(this, "src/customerInformation/" + this.getUserId());
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
        System.out.println("请输入用户ID（用户名+手机号）：");
        String inputUserId = scanner.next();
        File userFolder = new File("src/customerInformation/" + inputUserId);
        while (true) {
            System.out.println("请输入密码：");
            String inputPassword = scanner.next();
            Customer loaded = Tools.readCustomerInformationFromFolder(userFolder);
            if (loaded == null) {
                System.out.println("该用户不存在，请先完成注册！");
                this.lastLoginLocked = false;
                return false;
            }
            if (loaded.isLocked()) {
                System.out.println("账户已锁定（密码连续错误3次），请联系管理员重置密码！");
                this.lastLoginLocked = true;
                return false;
            }
            if (loaded.getPassword() != null && loaded.getPassword().equals(Tools.hashPassword(inputPassword))) {
                this.lastLoginLocked = false;
                if (loaded.getFailedLoginCount() > 0) {
                    loaded.setFailedLoginCount(0);
                    Tools.writeCustomerInformationToFile(loaded, "src/customerInformation/" + inputUserId);
                }
                user.setUserId(loaded.getUserId());
                user.setUserName(loaded.getUserName());
                user.setPassword(inputPassword);
                user.setUserPhoneNumber(loaded.getUserPhoneNumber());
                if (user instanceof Customer) {
                    Customer customer = (Customer) user;
                    customer.setEmail(loaded.getEmail());
                    customer.setRegisterTime(loaded.getRegisterTime());
                    customer.setLevel(loaded.getLevel());
                    customer.setTotalSpent(loaded.getTotalSpent());
                    customer.setFailedLoginCount(loaded.getFailedLoginCount());
                    customer.setLocked(loaded.isLocked());
                }
                System.out.println("登录成功！");
                return true;
            } else {
                int failed = loaded.getFailedLoginCount() + 1;
                loaded.setFailedLoginCount(failed);
                if (failed >= 3) {
                    loaded.setLocked(true);
                    this.lastLoginLocked = true;
                    Tools.writeCustomerInformationToFile(loaded, "src/customerInformation/" + inputUserId);
                    System.out.println("密码连续错误3次，账户已锁定，请联系管理员重置密码！");
                    return false;
                }
                Tools.writeCustomerInformationToFile(loaded, "src/customerInformation/" + inputUserId);
                System.out.println("密码错误，登录失败！您还有 " + (3 - failed) + " 次机会，请重新输入密码：");
            }
        }
    }

}