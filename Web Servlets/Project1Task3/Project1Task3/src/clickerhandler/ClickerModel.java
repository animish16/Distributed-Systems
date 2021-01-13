package clickerhandler;

import java.util.ArrayList;
import java.util.List;

public class ClickerModel {
    List<String> answersList = new ArrayList<>();
    String lastAnswer = null;

    // Store and maintain all the answers
    void storeAnswer(String submittedAnswer) {
        answersList.add(submittedAnswer);
        lastAnswer = submittedAnswer;
    }

    // Clear all answers
    void clearAnswers() {
        answersList.clear();
    }
}
