package sockets;

import domain.Hotel;
import utils.Action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends Thread {

    private Socket socket;
    private InetAddress direccion;
    private PrintStream send;
    private BufferedReader receive;
    private String lectura;

    private String infoMostrar;
    private String mostrarHoteles;

    private Hotel hotelSolicitado;

    // -> Observer
    private boolean mostrado;
    private boolean hotelesMostrado;
    private boolean mostrarHotelSolicitado;
    //

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
        //

        // -> variables de validación de acciones => 1 = accion exitosa, 2 = accion no exitosa, 0 = no se ha realizado ninguna accion
        this.registered = 0;
        this.updated = 0;
        this.deleted = 0;

        this.infoMostrar = "";
        this.mostrarHoteles = "";

        this.hotelSolicitado = null;
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
                        int numeroHotel = 1;
                        for (int i = 1; i < datos.length - 1; i+=3) {
                            this.mostrarHoteles += numeroHotel+". Numero: "+datos[i] + " Nombre: "+datos[i+1] +
                                    " Direccion: "+datos[i+2]+ "\n";
                            numeroHotel++;
                        }
                        this.hotelesMostrado = true;
                        break;
                    case Action.HOTEL_SEARCH:
                        this.hotelSolicitado = new Hotel(datos[1], datos[2], datos[3]);
                        this.mostrarHotelSolicitado = true;
                        break;
                    case Action.HOTEL_REGISTERED:
                        this.registered = 1;
                        break;
                    case Action.HOTEL_UPDATED:
                        this.updated = 1;
                        break;
                    case Action.HOTEL_DELETED:
                        this.deleted = 1;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + accion);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
