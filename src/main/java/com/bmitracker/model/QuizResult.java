package com.bmitracker.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class QuizResult {
    private final IntegerProperty resultId = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final IntegerProperty score = new SimpleIntegerProperty();
    private final IntegerProperty total = new SimpleIntegerProperty();
    private final StringProperty answers = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createTime = new SimpleObjectProperty<>();

    public int getResultId() { return resultId.get(); }
    public IntegerProperty resultIdProperty() { return resultId; }
    public void setResultId(int v) { resultId.set(v); }

    public int getUserId() { return userId.get(); }
    public IntegerProperty userIdProperty() { return userId; }
    public void setUserId(int v) { userId.set(v); }

    public int getScore() { return score.get(); }
    public IntegerProperty scoreProperty() { return score; }
    public void setScore(int v) { score.set(v); }

    public int getTotal() { return total.get(); }
    public IntegerProperty totalProperty() { return total; }
    public void setTotal(int v) { total.set(v); }

    public String getAnswers() { return answers.get(); }
    public StringProperty answersProperty() { return answers; }
    public void setAnswers(String v) { answers.set(v); }

    public LocalDateTime getCreateTime() { return createTime.get(); }
    public ObjectProperty<LocalDateTime> createTimeProperty() { return createTime; }
    public void setCreateTime(LocalDateTime v) { createTime.set(v); }
}
