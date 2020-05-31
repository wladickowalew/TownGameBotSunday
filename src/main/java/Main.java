public class Main {

    private static Bot bot;

    public static void main(String[] args) {
        bot = Bot.init();
    }

    public static void sendMessage(Long chatID, String text){
        bot.sendMessage(chatID, text);
    }

    public static void sendImage(Long chatID, String url){
        bot.sendImage(chatID, url);
    }

    public static String getRandomTown(){
        String[] towns = {"Смоленск","Самара","Лондон","Торонто","Берлин","Токио","Пиза"};
        int n = towns.length;
        int r = (int)(Math.random() * n);
        return  towns[r];
    }
}
