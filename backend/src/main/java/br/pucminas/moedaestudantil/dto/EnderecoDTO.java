package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Endereco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoDTO(
        @NotBlank(message = "Logradouro é obrigatório") @Size(max = 150) String logradouro,
        @NotBlank(message = "Número é obrigatório") @Size(max = 10) String numero,
        @Size(max = 60) String complemento,
        @NotBlank(message = "Bairro é obrigatório") @Size(max = 80) String bairro,
        @NotBlank(message = "Cidade é obrigatória") @Size(max = 80) String cidade,
        @NotBlank(message = "UF é obrigatória")
        @Pattern(regexp = "[A-Za-z]{2}", message = "UF deve ter 2 letras") String uf,
        @NotBlank(message = "CEP é obrigatório")
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP deve ter 8 dígitos") String cep) {

    public Endereco toEntity() {
        Endereco e = new Endereco();
        e.setLogradouro(logradouro.trim());
        e.setNumero(numero.trim());
        e.setComplemento(complemento == null || complemento.isBlank() ? null : complemento.trim());
        e.setBairro(bairro.trim());
        e.setCidade(cidade.trim());
        e.setUf(uf.toUpperCase());
        e.setCep(Documentos.somenteDigitos(cep));
        return e;
    }

    public static EnderecoDTO from(Endereco e) {
        return new EnderecoDTO(e.getLogradouro(), e.getNumero(), e.getComplemento(),
                e.getBairro(), e.getCidade(), e.getUf(), e.getCep());
    }
}
