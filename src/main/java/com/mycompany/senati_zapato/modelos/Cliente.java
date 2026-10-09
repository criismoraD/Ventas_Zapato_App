package com.mycompany.senati_zapato.modelos;

public class Cliente {
    private int id;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String telefono;
    private String correo;
    private String direccion;

    public Cliente() {
        this.tipoDocumento = "DNI";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    @Override
    public String toString() {
        return nombres + " - " + tipoDocumento + ": " + numeroDocumento;
    }
}
