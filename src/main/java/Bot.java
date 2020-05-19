import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Bot extends TelegramLongPollingBot {

    public HashMap<Long, User> users = new HashMap<Long, User>();

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi botapi = new TelegramBotsApi();
        try {
            botapi.registerBot(new Bot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    public String getRandomTown(){
        String[] towns = {"Смоленск","Самара","Лондон","Торонто","Берлин","Токио","Пиза"};
        int n = towns.length;
        int r = (int)(Math.random() * n);
        return  towns[r];
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String text = message.getText();
        long id = message.getChatId();
        System.out.println("" + id + ": " + text);
        if (nonAuthorizedUserHandler(message)) return;
        if (conditionHandler(message)) return;
        if (commandHandler(message)) return;
    }

    private void logOut(Message message){
        sendMessage(message, "Вы вышли из игры");
        users.get(message.getChatId()).setCondition("");
    }

    private boolean nonAuthorizedUserHandler(Message message) {
        String text = message.getText();
        long id = message.getChatId();
        if (text.equals("/help")) {
            sendMessage(message, textHelp());
            return true;
        }
        if (text.equals("/start")) {
            String name = "No name";
            users.put(id, new User(message.getChatId(), name));
            sendMessage(message, "Приветствую тебя, пользователь! Вот, что я могу:");
            sendMessage(message, textHelp());
            return true;
        }
        if (!users.containsKey(id)) {
            sendMessage(message, "Ты не хочешь со мной играть( введи /start");
            return true;
        }
        return false;
    }

    private boolean commandHandler(Message message) {
        String text = message.getText();
        long id = message.getChatId();
        User user = users.get(id);
        if (text.equals("/stop")) {
            logOut(message);
            return true;
        }
        if (text.equals("/start_game")){
            sendMessage(message, "Привет! Не хочешь ли сыграть в игру?" +
                    "Тебе предстоит отгадать город на картинке. (Да/Нет)");
            users.get(id).setCondition("begin");
            return true;
        }
        if (text.equals("/level")){
            sendMessage(message, "Уровень сложности: " + user.getLevel());
            return true;
        }
        if (text.equals("/change_level")){
            sendMessage(message, "Выберите уровень сложности: лёгкий, средний, сложный или ХАРД");
            user.setCondition("change_level");
            return true;
        }
        return false;
    }

    private boolean conditionHandler(Message message) {
        String text = message.getText();
        long id = message.getChatId();
        User user = users.get(id);
        String condition = user.getCondition();
        if (condition.equals(""))
            return false;
        if (text.equals("/stop")) {
            logOut(message);
            return true;
        }
        if (condition.equals("begin")) {
            System.out.println("condition begin");
            if (text.equals("Да") || text.equals("да")){
                user.setCondition("SendImage");
                user.startGame(getRandomTown());
                sendMessage(message, "Что это за город? (Ответ на русском языке)");
                sendImage(message, user.getImageURL());
                return true;
            }
            if (text.equals("Нет") || text.equals("нет")){
                logOut(message);
                return true;
            }
            sendMessage(message, "я тебя не понимаю");
            return true;
        }
        if (condition.equals("SendImage")) {
            if (text.equals(user.getTown())){
                //win
                sendMessage(message, "Мои поздравления! Ты угадал! Хочешь ещё?");
                sendMessage(message, "Очков набрано: " + user.getPoints());
                user.setCondition("begin");
                return true;
            }
            if (user.isEnd()){
                //lose
                sendMessage(message, "Ты не силён в географии. Это город: " + user.getTown()+". Может ещё?");
                user.setCondition("begin");
                return true;
            }
            sendMessage(message, "Ты ошибся( Это не " + text + ". Попробуй ещё раз)");
            sendImage(message, user.getImageURL());
            return true;
        }
        if (condition.equals("change_level")) {
            if (text.equals("лёгкий") || text.equals("средний") || text.equals("сложный") ||text.equals("ХАРД")){
                user.setLevel(text);
                user.setCondition("");
                sendMessage(message, "Сложность успешно установлена");
            }else{
                sendMessage(message, "Выберите один из 4 вариантов ответа");
            }
            return true;
        }

        sendMessage(message, "Что-то пошло не так");
        user.setCondition("");
        return true;
    }

    private String textHelp() {
        return "Привет, я бот для игры в города и умею мого чего:\n" +
                "/help - выведет это сообщение,\n" +
                "/start - начать общение с ботом игру,\n" +
                "/level - посмотреть урвень сложности,\n" +
                "/change_level - изменить уровень сложности,\n" +
                "/start_game - начать игру\n"+
                "/stop  - остановить игру\n";
    }

    private void sendImage(Message message, String url){
        SendPhoto photo = new SendPhoto();
        photo.setChatId(message.getChatId());
        photo.setPhoto(url);
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Message m, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(m.getChatId());
        message.setText(text);
//        setButtons(message);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void setButtons(SendMessage message) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        message.setReplyMarkup(keyboard);
        keyboard.setSelective(true);
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);

        List<KeyboardRow> rows = new ArrayList<KeyboardRow>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("/help"));
        row1.add(new KeyboardButton("/new_user"));
        rows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("/stop"));
        rows.add(row2);

        keyboard.setKeyboard(rows);
    }

    @Override
    public String getBotUsername() {
        return "java_2_bot";
    }

    @Override
    public String getBotToken() {
        return "1226704230:AAFgksjADyi8hMORsT7K9OoOt2WOhN4BJ9s";
    }


    ///////////////////////////////////////TESTS////////////////////////////////////////////////////

    public static void testImageLoader() {
        ImageLoader test = new ImageLoader("Смоленск", 5);
        int i = 1;
        while (!test.isEmpty()) {
            Image img = test.getNextImage();
            if (img == null) break;
            BufferedImage bf = (BufferedImage) img;
            File out = new File("img" + i++ + ".png");
            try {
                ImageIO.write(bf, "png", out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
