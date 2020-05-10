import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String text = message.getText();
        long id = message.getChatId();
        System.out.println("" + id + ": " + text);
        if (nonAuthorizedUserHandler(message)) return;
        if (conditionHandler(message)) return;
        if (commandHandler(message)) return;
        sendMessage(message, users.get(id).getName() + " сказал: " + text);
    }

    private boolean nonAuthorizedUserHandler(Message message) {
        String text = message.getText();
        long id = message.getChatId();
        if (text.equals("/new_user")) {
            sendMessage(message, "Привет, как тебя зовут?");
            User user = new User();
            users.put(id, user);
            return true;
        }
        if (text.equals("/help")) {
            sendMessage(message, textHelp());
            return true;
        }
        if (text.equals("/start")) {
            sendMessage(message, "Привет, пользователь! Я буду следить за тобой) а ты сам мне всё расскажешь");
            sendMessage(message, textHelp());
            return true;
        }
        if (!users.containsKey(id)) {
            sendMessage(message, "Мы незнакомы");
            return true;
        }
        return false;
    }

    private boolean commandHandler(Message message) {
        String text = message.getText();
        long id = message.getChatId();
        User user = users.get(id);
        if (text.equals("/my_family")) {
            if (user.getSecondName().equals(""))
                sendMessage(message, "Я не знаю вашей фамилии");
            else
                sendMessage(message, "Ваша фамилия: " + user.getSecondName());
            return true;
        }
        if (text.equals("/my_name")) {
            sendMessage(message, "Ваше имя: " + user.getName());
            return true;
        }
        if (text.equals("/my_age")) {
            if (user.getAge() == -1)
                sendMessage(message, "Я не знаю вашего возраста");
            else
                sendMessage(message, "Ваш возраст: " + user.getAge());
            return true;
        }
        if (text.equals("/change_family")) {
            sendMessage(message, "Введите вашу фамилию");
            user.setCommand("family");
            return true;
        }
        if (text.equals("/change_name")) {
            sendMessage(message, "Введите ваше новое имя");
            user.setCommand("name");
            return true;
        }
        if (text.equals("/change_age")) {
            sendMessage(message, "Введите ваш возраст");
            user.setCommand("age");
            return true;
        }
        return false;
    }

    private boolean conditionHandler(Message message) {
        String text = message.getText();
        long id = message.getChatId();
        User user = users.get(id);
        String command = user.getCommand();
        if (command.equals(""))
            return false;
        if (text.equals("/stop")) {
            sendMessage(message, "Команда успешно прервана");
            user.setCommand("");
            return true;
        }
        if (command.equals("family")) {
            user.setSecondName(text);
            sendMessage(message, "Ваша фамилия сохранена успешно!");
            user.setCommand("");
            return true;
        }
        if (command.equals("name")) {
            user.setName(text);
            sendMessage(message, "Ваше новое имя сохранено сохранена успешно!");
            user.setCommand("");
            return true;
        }
        if (command.equals("newUser")) {
            user.setName(text);
            sendMessage(message, "Приятно познакомится, " + text);
            user.setCommand("");
            return true;
        }
        if (command.equals("age")) {
            try {
                int age = Integer.parseInt(text);
                if (age < 0 || 150 < age) {
                    throw new IllegalArgumentException("no valid Age");
                }
                user.setAge(age);
                sendMessage(message, "Ваш возраст успешно сохранён");
                user.setCommand("");
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage(message, "Вы ввели неверный возраст, попробуйтне ещё раз");
            }
            return true;
        }

        sendMessage(message, "Что-то пошло не так");
        user.setCommand("");
        return true;
    }

    private String textHelp() {
        return "Привет, я бот из Легасофта и умею мого чего:\n" +
                "/help - выведет это сообщение,\n" +
                "/start - начать работу с ботом с этой кнопки,\n" +
                "/new_user - создаст нового пользователя, перезаписав старого,\n" +
                "/my_name - выведет имя пользователя,\n" +
                "/change_name - изменит имя пользователя,\n" +
                "/my_family - выведет фамилию пользователя,\n" +
                "/change_family - изменит фамилию пользователя,\n" +
                "/my_age - выведет возраст пользователя,\n" +
                "/change_age - изменит возраст пользователя,\n";
    }


    private void sendMessage(Message m, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(m.getChatId());
        message.setText(text);
        setButtons(message);
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

        List<KeyboardRow> rows = new ArrayList<>();
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
