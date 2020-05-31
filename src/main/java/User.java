import java.util.Objects;

public class User {

    private String name;
    private Long chatId;
    private int room_id;
    private String town;
    private ImageLoader loader;
    private String condition;
    private int level;
    private int attempts;
    private int points;
    private boolean end_round;
    private boolean game_in_room;

    public User(Long id, String name){
        this.name = name;
        chatId = id;
        condition = "";
        level = 3;
        room_id = -1;
    }

    @Override
    public String toString() {
        return "name=" + name + ", chatId=" + chatId;
    }

    public void startGame(String town){
        this.town = town;
        setAttempts();
        loader = new ImageLoader(town, 5);
        game_in_room = false;
    }

    public void startGameInRoom() {
        game_in_room = true;
        points = 0;
        Main.sendMessage(chatId, "Игра в комнате началась!");
    }

    public void startRoundInRoom(ImageLoader loader, int round, String town) {
        setTown(town);
        setLoader(loader);
        end_round = false;
        setAttempts();
        Main.sendMessage(chatId,"Раунд: " + (round+1));
        setCondition("SendImage");
        Main.sendImage(chatId, getImageURL());
    }

    public void end_room_game(String results){
        condition = "";
        Main.sendMessage(chatId, results);
    }

    public void end_round(boolean win){
        end_round = true;
        if (win)
            points += (attempts + 1) * level;
        condition = "waiting";
    }

    private void setAttempts(){
        switch (level){
            case 1:  attempts = 8; break;
            case 3:  attempts = 5; break;
            case 7:  attempts = 3; break;
            case 40: attempts = 1; break;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getLevel() {
        switch (level){
            case 1:  return "лёгкий";
            case 3:  return "средний";
            case 7:  return "сложный";
            case 40: return "ХАРД";
        }
        return null;
    }

    public void setLevel(String level) {
        switch (level){
            case "лёгкий":  this.level = 1; break;
            case "средний": this.level = 3; break;
            case "сложный": this.level = 7; break;
            case "ХАРД":    this.level = 40; break;
        }
    }

    public int getPoints() {
        return points;
    }

    public boolean isEnd(){
        return attempts == 0;
    }

    public String getImageURL(){
        attempts--;
        return loader.getNextImageURL();
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return chatId.equals(user.chatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId);
    }

    public int getRoom_id() {
        return room_id;
    }

    public void setRoom_id(int room_id) {
        this.room_id = room_id;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public ImageLoader getLoader() {
        return loader;
    }

    public void setLoader(ImageLoader loader) {
        this.loader = loader;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public boolean isEnd_round() {
        return end_round;
    }

    public void setEnd_round(boolean end_round) {
        this.end_round = end_round;
    }

    public boolean isGame_in_room() {
        return game_in_room;
    }

    public void setGame_in_room(boolean game_in_room) {
        this.game_in_room = game_in_room;
    }
}
