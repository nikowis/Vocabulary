package pl.nikowis.ui;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import org.springframework.beans.factory.annotation.Autowired;
import pl.nikowis.entities.Quiz;
import pl.nikowis.entities.QuizAnswer;
import pl.nikowis.entities.Word;
import pl.nikowis.exceptions.UserHasNoWordsException;
import pl.nikowis.services.QuizService;
import pl.nikowis.ui.base.I18nCustomComponent;

import java.util.List;

/**
 * Quiz form.
 * Created by nikowis on 2016-08-21.
 *
 * @author nikowis
 */
@SpringComponent
public class QuizForm extends I18nCustomComponent {

    private static final String SUCCESS_STYLE ="highlight-green";
    private static final String FAILURE_STYLE ="highlight-red";
    private final String COL_ORIGINAL = "word.original";
    private final String COL_TRANSLATED = "word.translated";
    private final String COL_USER_ANSWER = "userAnswer";


    @Autowired
    private QuizService quizService;

    private TextField original, translated;
    private Button finish, quit, next;
    private Grid wordGrid;
    private ProgressBar progressBar;

    private int answersDoneCounter;
    private int allWordsCount;
    private QuizAnswer currentAnswer;

    private Quiz quiz;

    /**
     * Creates a quiz based on the provided word list.
     * @param wordList list of words to be in the quiz
     */
    public void initializeForm(List<Word> wordList) {
        answersDoneCounter = 0;

        quiz = quizService.createQuiz(wordList);
        allWordsCount = quiz.size();

        if (allWordsCount == 0) {
            throw new UserHasNoWordsException();
        }

        currentAnswer = quiz.getAnswer(answersDoneCounter);

        initializeComponents();

        setSizeFull();

        HorizontalLayout wordForm = new HorizontalLayout(original, translated);
        wordForm.setSpacing(true);
        wordForm.setSizeUndefined();

        VerticalLayout fields = new VerticalLayout(progressBar, wordForm, next, finish, wordGrid, quit);
        fields.setSpacing(true);
        fields.setMargin(new MarginInfo(true, true, true, false));
        fields.setSizeUndefined();

        VerticalLayout mainLayout = new VerticalLayout(fields);
        mainLayout.setSizeFull();
        mainLayout.setComponentAlignment(fields, Alignment.MIDDLE_CENTER);
        mainLayout.setStyleName(Reindeer.LAYOUT_BLUE);
        setCompositionRoot(mainLayout);
    }

    private void initializeComponents() {
        progressBar = new ProgressBar();
        progressBar.setCaption(getMessage("quizForm.progressBar"));
        progressBar.setValue(0.0f);
        progressBar.setWidthUndefined();
        setupGrid();

        original = new TextField();
        original.setCaption(getMessage("quizForm.original"));
        original.setEnabled(false);
        original.setValue(currentAnswer.getWord().getOriginal());
        translated = new TextField();
        translated.setCaption(getMessage("quizForm.translated"));

        finish = new Button(getMessage("quizForm.finish"), FontAwesome.CHECK_CIRCLE);
        finish.addClickListener(clickEvent -> finishQuiz());

        quit = new Button(getMessage("quizForm.quit"), FontAwesome.BAN);
        quit.addClickListener(clickEvent -> quitQuiz());
        next = new Button(getMessage("quizForm.next"), FontAwesome.ARROW_RIGHT);
        next.addClickListener(clickEvent -> goToNext());

        if (allWordsCount < 2) {
            finish.setVisible(true);
            next.setVisible(false);
        } else {
            finish.setVisible(false);
            next.setVisible(true);
        }
    }

    private void setupGrid() {
        wordGrid = new Grid(getMessage("quizForm.wordGrid.title"));
        wordGrid.setVisible(false);
        wordGrid.setSelectionMode(Grid.SelectionMode.NONE);
        BeanItemContainer<QuizAnswer> wordContainer = new BeanItemContainer<QuizAnswer>(QuizAnswer.class);
        wordContainer.addNestedContainerProperty(COL_ORIGINAL);
        wordContainer.addNestedContainerProperty(COL_TRANSLATED);
        wordContainer.addAll(quiz.getAnswers());
        wordGrid.setContainerDataSource(wordContainer);
        wordGrid.getColumn(COL_USER_ANSWER).setHeaderCaption(getMessage("quizForm.wordGrid.userAnswerCol"));
        wordGrid.setColumns(
                COL_ORIGINAL
                , COL_TRANSLATED
                , COL_USER_ANSWER
        );
    }

    private void goToNext() {
        progressBar.setValue(progressBar.getValue() + 1.0f / allWordsCount);
        commitAndCheckWord();
        switchToNextWord();
        if (answersDoneCounter + 1 >= allWordsCount) {
            changeToFinishButton();
        }
    }

    private void commitAndCheckWord() {
        currentAnswer.setUserAnswer(translated.getValue());
        if (translated.getValue().equals(currentAnswer.getWord().getTranslated())) {
            currentAnswer.getWord().incrementProgress();
            currentAnswer.setCorrect(true);
        }
    }

    private void switchToNextWord() {
        translated.setValue("");
        answersDoneCounter++;
        currentAnswer = quiz.getAnswer(answersDoneCounter);
        original.setValue(currentAnswer.getWord().getOriginal());
    }

    private void changeToFinishButton() {
        next.setVisible(false);
        finish.setVisible(true);
    }

    private void quitQuiz() {
        redirect(HomeView.VIEW_NAME);
    }

    private void finishQuiz() {
        progressBar.setValue(1.0f);
        commitAndCheckWord();
        quizService.save(quiz);
        hideQuizFields();
        showSummary();
    }

    private void hideQuizFields() {
        progressBar.setVisible(false);
        original.setVisible(false);
        translated.setVisible(false);
        finish.setVisible(false);
    }

    private void showSummary() {
        wordGrid.setVisible(true);
        int count = 0;
        wordGrid.setRowStyleGenerator(rowReference -> {
            if (((QuizAnswer) rowReference.getItemId()).isCorrect()) {
                return SUCCESS_STYLE;
            } else {
                return FAILURE_STYLE;
            }
        });
    }

    private void redirect(String viewName) {
        getUI().getNavigator().navigateTo(viewName);
    }

}
