package br.pucminas.moedaestudantil.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "empresa_parceira")
@PrimaryKeyJoinColumn(name = "id")
public class EmpresaParceira extends Usuario {

    @Column(name = "nome_fantasia", nullable = false, length = 150)
    private String nomeFantasia;

    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Composicao: as vantagens pertencem a empresa que as oferece. */
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vantagem> vantagens = new ArrayList<>();

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<Vantagem> getVantagens() { return vantagens; }
}
