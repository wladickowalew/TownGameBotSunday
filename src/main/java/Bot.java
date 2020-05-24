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

    public HashMap<Long, User> users = new HashMap<>();
    public HashMap<Integer, Room> rooms = new HashMap<>();

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
            String name = message.getFrom().getFirstName() + " " + message.getFrom().getLastName();
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
        if (text.equals("/new_room")){
            sendMessage(message, "Введите название вашей комнаты");
            user.setCondition("new_room");
            return true;
        }
        if (text.equals("/remove_room")){
            sendMessage(message, "Введите id комнаты");
            user.setCondition("remove_room");
            return true;
        }
        if (text.equals("/show_rooms")){
            if (rooms.isEmpty()){
                sendMessage(message, "Комнат нет");
                return true;
            }
            StringBuilder builder = new StringBuilder();
            for (Room room: rooms.values()){
                builder.append(room.toString() + "\n");
            }
            sendMessage(message, builder.toString());
            return true;
        }
        if (text.equals("/show_my_rooms")){
            StringBuilder builder = new StringBuilder();
            for (Room room: rooms.values()){
                if (room.isRoot(user))
                    builder.append(room.toString() + "\n");
            }
            String ans = builder.toString();
            if (ans.equals("")){
                sendMessage(message, "У вас нет комнат");
            }else {
                sendMessage(message, builder.toString());
            }
            return true;
        }
        if (text.equals("/to_room")){
            sendMessage(message, "Введите id комнаты");
            user.setCondition("to_room");
            return true;
        }
        if (text.equals("/from_room")){
            if(user.getRoom_id() == -1) {
                sendMessage(message, "Вы не находитесь ни в комнате.");
                return true;
            }
            rooms.get(user.getRoom_id()).removeUser(user);
            sendMessage(message, "Вы вышли из комнаты.");
            return true;
        }
        if (text.equals("/show_room_users")){
            sendMessage(message, "Введите id комнаты");
            user.setCondition("room_users");
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
        if (condition.equals("new_room")) {
            Room room = new Room(text, user);
            rooms.put(room.getId(), room);
            user.setCondition("");
            sendMessage(message, "Вы создали комнату " + room);
            return true;
        }
        if (condition.equals("remove_room")) {
            int room_id = getRoomId(message, user);
            if (room_id == -1)
                return true;
            if (rooms.get(room_id).isRoot(user)){
                rooms.get(room_id).removeAllUsers();
                rooms.remove(room_id);
                sendMessage(message, "Вы удалили комнату с id = "+ room_id);
            }else
                sendMessage(message, "Вы не можете выполнить эту операцию");
            user.setCondition("");
            return true;
        }
        if (condition.equals("to_room")){
            int room_id = getRoomId(message, user);
            if(room_id == -1)
                return true;
            if(user.getRoom_id() != -1) {
                sendMessage(message, "Вы уже находитесь в комнате. выйдете из неё, чтобы зайти в другую");
                return true;
            }
            rooms.get(room_id).addUser(user);
            sendMessage(message, "Вы зашли в комнату " + rooms.get(room_id));
            user.setCondition("");
            return true;
        }
        if (condition.equals("room_users")){
            int room_id = getRoomId(message, user);
            if(room_id == -1)
                return true;
            sendMessage(message, rooms.get(room_id).getUsers());
        }
        sendMessage(message, "Что-то пошло не так");
        user.setCondition("");
        return true;
    }

    private int getRoomId(Message message, User user){
        try{
            int room_id = Integer.parseInt(message.getText());
            return room_id;
        }catch (NullPointerException e){
            e.printStackTrace();
            sendMessage(message, "Комнаты с таким id не существует");
        }catch (Exception e){
            e.printStackTrace();
            sendMessage(message, "Я тебя не понимаю, введи id комнаты");
        }
        return -1;
    }

    private String textHelp() {
        return "Привет, я бот для игры в города и умею мого чего:\n" +
                "/help - выведет это сообщение,\n" +
                "/start - начать общение с ботом игру,\n" +
                "/level - посмотреть урвень сложности,\n" +
                "/change_level - изменить уровень сложности,\n" +
                "/start_game - начать игру\n"+
                "/stop  - остановить игру\n"+
                "/new_room - создать новую комнату,\n" +
                "/remove_room - удалить комнату,\n" +
                "/show_rooms - показать все комнаты\n"+
                "/show_my_rooms - показать мои комнаты\n"+
                "/to_room - войти в комнату\n" +
                "/from_room - выйти из комнаты\n"+
                "/show_room_users - показать пользователей в комнате\n";
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
