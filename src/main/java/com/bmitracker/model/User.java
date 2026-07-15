package com.bmitracker.model;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String userName;
    private String password;
    private int userAge;
    private int sex;
    private double height;
    private double weight;
    private String preferences;
    private String allergens;
    private String chronicDiseases;
    private LocalDateTime createTime;

    public User() {}

    public User(String userName, String password, int userAge, int sex) {
        this.userName = userName;
        this.password = password;
        this.userAge = userAge;
        this.sex = sex;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getUserAge() { return userAge; }
    public void setUserAge(int userAge) { this.userAge = userAge; }

    public int getSex() { return sex; }
    public void setSex(int sex) { this.sex = sex; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }

    public String getAllergens() { return allergens; }
    public void setAllergens(String allergens) { this.allergens = allergens; }

    public String getChronicDiseases() { return chronicDiseases; }
    public void setChronicDiseases(String chronicDiseases) { this.chronicDiseases = chronicDiseases; }

    public boolean needsHealthProfile() {
        return (allergens == null || allergens.isEmpty()) && (chronicDiseases == null || chronicDiseases.isEmpty());
    }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
