package domain;

public class Room {

    private String status, style, roomNumber;
    private int price;

    public Room(String status, String style, String roomNumber, int price) {
        this.status = status;
        this.style = style;
        this.roomNumber = roomNumber;
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "-"+this.status +"-"+this.style +"-"+this.roomNumber +"-"+this.price;
    }
}
