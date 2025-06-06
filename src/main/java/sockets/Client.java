package sockets;

import domain.Host;
import domain.Receptionist;
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
/// la clase cliente que se conecta al servidor y procesa las respuestas
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
    private String bookings;
    private Receptionist receptionist;//nuevo
    private Host host;

    // -> Observer
    private  boolean mostrado;
    private  boolean hotelesMostrado;
    private  boolean mostrarHotelSolicitado;
    private boolean mostrarRoomHotel;
    private boolean mostrarRecepcionistaSolicitado; //nuevo
    private int bookingNumberExiste;
    private int hostExist;

    private boolean bookingMostrado;

    private boolean habitacionesMostrado;
    private boolean mostrarHabitacionSolicitado;

    //

    private byte[] image;
    private boolean imageReceived;
    private ArrayList<byte[]> images;
    // -> Validación de acciones
    private int registered;
    private int updated;
    private int deleted;
    private volatile int loged = 0;

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
        this.mostrarRecepcionistaSolicitado = false;
        this.habitacionesMostrado = false;
        this.mostrarHabitacionSolicitado = false;
        this.bookingNumberExiste = 0; // 1 no existe, 2 existe, 0 listo

        this.bookingMostrado = false;
        //

        this.imageReceived = false;
        this.images = new ArrayList<>();

        // -> variables de validación de acciones => 1 = accion exitosa, 2 = accion no exitosa, 0 = no se ha realizado ninguna accion
        this.registered = 0;
        this.updated = 0;
        this.deleted = 0;
        this.hostExist = 0;

        this.infoMostrar ="";
        this.mostrarHoteles = "";
        this.hotelRooms = "";
        this.bookings = "";

        this.mostrarRooms = "";

        this.hotelSolicitado = null;
        this.roomSolicitado = null;
        this.receptionist = null;
        this.host = null;

    }


    @Override
    public void run() {
        try {
            while (true) {
                this.lectura = this.receive.readLine();
                //System.out.println(this.lectura);
                String[] datos = this.lectura.split("\\|\\|\\|");
                String accion = datos[0];
                switch (accion) {
                    case Action.HOTEL_LIST:
                        for (int i = 1; i < datos.length - 1; i+=3) {
                            this.mostrarHoteles += datos[i] + "|||" + datos[i+1] + "|||" + datos[i+2] + "\n";
                        }
                        this.hotelesMostrado = true;
                        break;
                    case Action.ROOM_LIST:
                        for (int i = 1; i < datos.length - 1; i+=5) {
                            this.mostrarRooms += datos[i] + "|||" + datos[i+1] + "|||" + datos[i+2] + "|||" + datos[i+3] + "|||" + datos[i+4] + "\n";
                        }
                        this.habitacionesMostrado = true;
                        break;
                    case Action.HOTEL_SEARCH:
                        this.hotelSolicitado = new Hotel(datos[1], datos[2], datos[3], new ArrayList<>());
                        this.mostrarHotelSolicitado = true;
                        break;
                    case Action.HOTEL_REGISTERED, Action.ROOM_REGISTERED, Action.RECEPTIONIST_REGISTERED:
                        this.registered = 1;
                        break;
                    ///
                    case Action.HOTEL_NOT_REGISTER, Action.RECEPTIONIST_NOT_REGISTERED:
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

                        if (datos.length < 6) {
                            this.roomSolicitado = null;  // No se encontró habitación
                            this.mostrarHabitacionSolicitado = false;
                        } else {

                            this.roomSolicitado = new Room(
                                    datos[1],         // roomNumber
                                    datos[2],         // hotelNumber
                                    datos[3],         // tipo
                                    Double.parseDouble(datos[4]),  // precio
                                    null,             // imágenes
                                    datos[5]          //
                            );
                            this.mostrarHabitacionSolicitado = true;
                        }
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
                        for (int i = 1; i < datos.length - 6; i += 8) {
                            this.hotelRooms += datos[i] + "|||" + datos[i+1] + "|||" + datos[i+2] + "|||" + datos[i+3] + "|||" + datos[i+7] + "\n";
                        }
                        this.mostrarRoomHotel = true;
                        break;
                    case Action.IMAGE_REQUEST:
                        this.images.clear();
                        for (int i = 1; i < datos.length; i++) {
                            byte[] imgBytes = Base64.getDecoder().decode(datos[i]);
                            this.images.add(imgBytes);
                        }
                        this.image = this.images.get(0);
                        this.imageReceived = true;
                        break;
                    case Action.RECEPTIONIST_LOGIN:
                        this.loged = 1;
                        this.receptionist = new Receptionist(datos[1], datos[2], datos[3], Integer.parseInt(datos[4]), datos[5], datos[6]);
                        break;
                    case Action.RECEPTIONIST_NOT_LOGIN:
                        this.loged = 2;
                        break;
                    case Action.HOST_REGISTERED:
                        this.registered=1;
                        break;
                    case Action.HOST_NOT_REGISTERED:
                        this.registered=2;
                        break;
                    case Action.BOOKING_NUMBER_EXIST:
                        this.bookingNumberExiste = 2;
                        break;
                    case Action.BOOKING_NUMBER_NO_EXIST:
                        this.bookingNumberExiste = 1;
                        break;
                    case Action.BOOKING_LIST:
                        for (int i = 1; i < datos.length - 1; i+=9) {
                            this.bookings += datos[i] + "|||" + datos[i+1] + "|||" + datos[i+2] + "|||" + datos[i+3] + "|||" + datos[i+4] +
                                    "|||" + datos[i+5] +"|||" + datos[i+6] + "|||" + datos[i+7] + "|||" + datos[i+8] + "\n";
                        }
                        this.bookingMostrado = true;
                        break;
                    case Action.HOST_EXIST:
                        System.out.println(this.lectura);
                        this.host = new Host(datos[1], datos[2], datos[3], Integer.parseInt(datos[4]), datos[5], datos[6], datos[7]);
                        this.hostExist = 1;
                        break;
                    case Action.HOST_NO_EXIST:
                        this.hostExist = 2;
                        break;
                    case Action.ROOM_NOT_REGISTERED:
                        this.registered=2;
                        break;
                    case Action.HOTEL_NOT_REGISTERED:
                        this.registered=2;
                        break;
                    case Action.BOOKING_REGISTERED:
                        this.registered = 1;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + accion);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public int getHostExist() {
        return hostExist;
    }

    public void setHostExist(int hostExist) {
        this.hostExist = hostExist;
    }

    public String getBookings() {
        return bookings;
    }

    public void setBookings(String bookings) {
        this.bookings = bookings;
    }

    public boolean isBookingMostrado() {
        return bookingMostrado;
    }

    public void setBookingMostrado(boolean bookingMostrado) {
        this.bookingMostrado = bookingMostrado;
    }

    public boolean isMostrarRecepcionistaSolicitado() {
        return mostrarRecepcionistaSolicitado;
    }

    public void setMostrarRecepcionistaSolicitado(boolean mostrarRecepcionistaSolicitado) {
        this.mostrarRecepcionistaSolicitado = mostrarRecepcionistaSolicitado;
    }

    public int getLoged() {
        return loged;
    }

    public void setLoged(int loged) {
        this.loged = loged;
    }

    public Receptionist getReceptionist() {
        return receptionist;
    }

    public void setReceptionist(Receptionist receptionist) {
        this.receptionist = receptionist;
    }

    public int getBookingNumberExiste() {
        return bookingNumberExiste;
    }

    public void setBookingNumberExiste(int bookingNumberExiste) {
        this.bookingNumberExiste = bookingNumberExiste;
    }

    public ArrayList<byte[]> getImages() {
        return images;
    }

    public void setImages(ArrayList<byte[]> images) {
        this.images = images;
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
