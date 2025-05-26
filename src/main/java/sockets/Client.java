package sockets;

import domain.Hotel;
import domain.Room;
import utils.Action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;

public class Client extends Thread {

    private Socket socket;
    private InetAddress direccion;
    private PrintStream send;
    private BufferedReader receive;
    private String lectura;

    private String infoMostrar;
    private String mostrarHoteles;

    private Hotel hotelSolicitado;

    private String mostrarRooms;
    private Room roomSolicitado;
    private String hotelRooms;

    // -> Observer
    private  boolean mostrado;
    private  boolean hotelesMostrado;
    private  boolean mostrarHotelSolicitado;
    private boolean mostrarRoomHotel;

    private boolean habitacionesMostrado;
    private boolean mostrarHabitacionSolicitado;

    //

    private byte[] image;
    private boolean imageReceived;
    // -> Validación de acciones
    private int registered;
    private int updated;
    private int deleted;

    public Client(String ip, int puerto) throws UnknownHostException, IOException {
        this.direccion = InetAddress.getByName(ip);
        this.socket = new Socket(this.direccion, puerto);
        this.send = new PrintStream(this.socket.getOutputStream());
        this.receive = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

        // -> variables observer
        this.mostrado = false;
        this.hotelesMostrado = false;
        this.mostrarHotelSolicitado = false;
        this.mostrarRoomHotel = false;

        this.habitacionesMostrado = false;
        this.mostrarHabitacionSolicitado = false;
        //

        this.imageReceived = false;

        // -> variables de validación de acciones => 1 = accion exitosa, 2 = accion no exitosa, 0 = no se ha realizado ninguna accion
        this.registered = 0;
        this.updated = 0;
        this.deleted = 0;

        this.infoMostrar ="";
        this.mostrarHoteles = "";
        this.hotelRooms = "";

        this.mostrarRooms = "";

        this.hotelSolicitado = null;
        this.roomSolicitado = null;

    }


    @Override
    public void run() {
        try {
            while (true) {
                this.lectura = this.receive.readLine();
                //System.out.println(this.lectura);
                String[] datos = this.lectura.split("-");
                String accion = datos[0];
                switch (accion) {
                    case Action.HOTEL_LIST:
                        for (int i = 1; i < datos.length - 1; i+=3) {
                            this.mostrarHoteles += datos[i] + "-" + datos[i+1] + "-" + datos[i+2] + "\n";
                        }
                        this.hotelesMostrado = true;
                        break;
                    case Action.ROOM_LIST:
                        for (int i = 1; i < datos.length - 1; i+=5) {
                            this.mostrarRooms += datos[i] + "-" + datos[i+1] + "-" + datos[i+2] + "-" + datos[i+3] + "-" + datos[i+4] + "\n";
                        }
                        this.habitacionesMostrado = true;
                        break;
                    case Action.HOTEL_SEARCH:
                        this.hotelSolicitado = new Hotel(datos[1], datos[2], datos[3], new ArrayList<>());
                        this.mostrarHotelSolicitado = true;
                        break;
                    case Action.HOTEL_REGISTERED, Action.ROOM_REGISTERED:
                        this.registered = 1;
                        break;
                    ///
                    case Action.HOTEL_NOT_REGISTER:
                        this.registered=2;
                        break;
                        ///
                    case Action.HOTEL_UPDATED:
                        this.updated = 1;
                        break;
                    case Action.ROOM_UPDATED:
                        this.updated = 1;
                        break;
                    case Action.ROOM_SEARCH:
                        this.roomSolicitado = new Room(datos[1], datos[2], datos[3], Double.parseDouble(datos[4]), null, datos[7]);
                        this.mostrarHabitacionSolicitado = true;
                        break;
                    case Action.ROOM_NOT_REGISTER:
                        this.registered=2;
                        break;
                    ///
                    case Action.HOTEL_DELETED:
                        this.deleted = 1;
                        break;
                    case Action.ROOM_DELETED:
                        this.deleted = 1;
                        break;
                    case Action.HOTEL_ROOMS:
                        for (int i = 1; i < datos.length - 1; i+=6) {
                            this.hotelRooms += datos[i] + "-" + datos[i+1] + "-" + datos[i+2] + "-" + datos[i+3] + "-" + datos[i+6] + "\n";
                        }
                        System.out.println(hotelRooms);
                        this.mostrarRoomHotel = true;
                        break;
                    case Action.IMAGE_REQUEST:
                        this.image = Base64.getDecoder().decode(datos[1]);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + accion);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public boolean isImageReceived() {
        return imageReceived;
    }

    public void setImageReceived(boolean imageReceived) {
        this.imageReceived = imageReceived;
    }

    public boolean isMostrarRoomHotel() {
        return mostrarRoomHotel;
    }

    public void setMostrarRoomHotel(boolean mostrarRoomHotel) {
        this.mostrarRoomHotel = mostrarRoomHotel;
    }

    public String getHotelRooms() {
        return hotelRooms;
    }

    public void setHotelRooms(String hotelRooms) {
        this.hotelRooms = hotelRooms;
    }

    public String getMostrarRooms() {
        return mostrarRooms;
    }

    public void setMostrarRooms(String mostrarRooms) {
        this.mostrarRooms = mostrarRooms;
    }

    public Room getRoomSolicitado() {
        return roomSolicitado;
    }

    public void setRoomSolicitado(Room roomSolicitado) {
        this.roomSolicitado = roomSolicitado;
    }

    public boolean isHabitacionesMostrado() {
        return habitacionesMostrado;
    }

    public void setHabitacionesMostrado(boolean habitacionesMostrado) {
        this.habitacionesMostrado = habitacionesMostrado;
    }

    public boolean isMostrarHabitacionSolicitado() {
        return mostrarHabitacionSolicitado;
    }

    public void setMostrarHabitacionSolicitado(boolean mostrarHabitacionSolicitado) {
        this.mostrarHabitacionSolicitado = mostrarHabitacionSolicitado;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    public int getRegistered() {
        return registered;
    }

    public void setRegistered(int registered) {
        this.registered = registered;
    }

    public boolean isMostrarHotelSolicitado() {
        return mostrarHotelSolicitado;
    }

    public void setMostrarHotelSolicitado(boolean mostrarHotelSolicitado) {
        this.mostrarHotelSolicitado = mostrarHotelSolicitado;
    }

    public Hotel getHotelSolicitado() {
        return hotelSolicitado;
    }

    public void setHotelSolicitado(Hotel hotelSolicitado) {
        this.hotelSolicitado = hotelSolicitado;
    }

    public String getMostrarHoteles() {
        return mostrarHoteles;
    }

    public void setMostrarHoteles(String mostrarHoteles) {
        this.mostrarHoteles = mostrarHoteles;
    }

    public boolean isHotelesMostrado() {
        return hotelesMostrado;
    }

    public void setHotelesMostrado(boolean hotelesMostrado) {
        this.hotelesMostrado = hotelesMostrado;
    }

    public String getInfoMostrar() {
        return infoMostrar;
    }

    public void setInfoMostrar(String infoMostrar) {
        this.infoMostrar = infoMostrar;
    }

    public boolean isMostrado() {
        return mostrado;
    }

    public void setMostrado(boolean mostrado) {
        this.mostrado = mostrado;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public InetAddress getDireccion() {
        return direccion;
    }

    public void setDireccion(InetAddress direccion) {
        this.direccion = direccion;
    }

    public PrintStream getSend() {
        return send;
    }

    public void setSend(PrintStream send) {
        this.send = send;
    }

    public BufferedReader getReceive() {
        return receive;
    }

    public void setReceive(BufferedReader receive) {
        this.receive = receive;
    }

    public String getLectura() {
        return lectura;
    }

    public void setLectura(String lectura) {
        this.lectura = lectura;
    }
}
