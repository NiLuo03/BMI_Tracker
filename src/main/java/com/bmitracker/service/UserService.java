package com.bmitracker.service;

import com.bmitracker.dao.UserDao;
import com.bmitracker.model.User;
import java.sql.SQLException;

public class UserService {

    private final UserDao userDao = new UserDao();

    // 注册：返回错误信息，null 表示成功
    public String register(String userName, String password, String confirmPassword,
                            int userAge, int sex) {
        if (userName == null || userName.trim().isEmpty()) return "请输入账号";
        if (password == null || password.isEmpty()) return "请输入密码";
        if (!password.equals(confirmPassword)) return "两次密码输入不一致";
        if (userAge <= 0 || userAge > 150) return "请输入有效年龄";

        try {
            User exist = userDao.findByUserName(userName.trim());
            if (exist != null) return "账号已被占用";
            User user = new User(userName.trim(), password, userAge, sex);
            int id = userDao.insert(user);
            return id > 0 ? null : "注册失败，请稍后再试";
        } catch (SQLException e) {
            return "系统繁忙，请稍后再试";
        }
    }

    // 登录：返回 userId 或错误信息
    public int login(String userName, String password) {
        if (userName == null || userName.trim().isEmpty()) return -1;
        if (password == null || password.isEmpty()) return -1;
        try {
            return userDao.login(userName.trim(), password);
        } catch (SQLException e) {
            return -1;
        }
    }

    // 获取用户信息
    public User getUserById(int userId) {
        try {
            return userDao.findById(userId);
        } catch (SQLException e) {
            return null;
        }
    }

    // 修改密码
    public String changePassword(int userId, String oldPwd, String newPwd, String confirmPwd) {
        if (oldPwd == null || oldPwd.isEmpty()) return "请输入原密码";
        if (newPwd == null || newPwd.isEmpty()) return "请输入新密码";
        if (!newPwd.equals(confirmPwd)) return "两次新密码输入不一致";

        try {
            User user = userDao.findById(userId);
            if (user == null) return "用户不存在";
            if (!user.getPassword().equals(oldPwd)) return "原密码不正确";
            int n = userDao.updatePassword(userId, newPwd);
            return n > 0 ? null : "修改失败，请稍后再试";
        } catch (SQLException e) {
            return "系统繁忙，请稍后再试";
        }
    }

    // 更新个人信息
    public String updateProfile(User user) {
        if (user.getHeight() <= 0) return "请输入有效身高";
        if (user.getWeight() <= 0) return "请输入有效体重";
        if (user.getUserAge() <= 0 || user.getUserAge() > 150) return "请输入有效年龄";
        try {
            int n = userDao.update(user);
            return n > 0 ? null : "更新失败，请稍后再试";
        } catch (SQLException e) {
            return "系统繁忙，请稍后再试";
        }
    }
}
