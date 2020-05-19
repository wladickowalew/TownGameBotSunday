import java.util.Objects;

public class User {

    private String name;
    private Long chatId;
    private String town;
    private ImageLoader loader;
    private String condition;
    private int level;
    private int attempts;
    private int points;

    public User(Long id, String name){
        this.name = name;
        chatId = id;
        condition = "";
        level = 3;
    }

    @Override
    public String toString() {
        return "name=" + name + ", chatId=" + chatId;
    }

    public void startGame(String town){
        this.town = town;
        switch (level){
            case 1:  attempts = 8; break;
            case 3:  attempts = 5; break;
            case 7:  attempts = 3; break;
            case 40: attempts = 1; break;
        }
        loader = new ImageLoader(town, 5);
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
        return (attempts + 1) * level;
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
}
