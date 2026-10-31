package br.pucminas.moedaestudantil.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import br.pucminas.moedaestudantil.exception.RegraNegocioException;

/**
 * Componente "Armazenamento de Fotos" do diagrama de componentes: grava as fotos das vantagens em disco
 * com nome aleatorio (o nome enviado pelo usuario nunca vira caminho) e devolve a URL publica.
 */
@Component
public class FotoStorage {

    private static final Map<String, String> EXTENSOES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif");

    private final Path diretorio;

    public FotoStorage(@Value("${app.fotos.dir}") String diretorio) throws IOException {
        this.diretorio = Path.of(diretorio).toAbsolutePath().normalize();
        Files.createDirectories(this.diretorio);
    }

    public String salvar(MultipartFile foto) {
        if (foto == null || foto.isEmpty()) {
            throw new RegraNegocioException("A foto do produto é obrigatória");
        }
        String tipo = foto.getContentType() == null ? "" : foto.getContentType().toLowerCase(Locale.ROOT);
        String extensao = EXTENSOES.get(tipo);
        if (extensao == null) {
            throw new RegraNegocioException("A foto deve ser JPG, PNG, WEBP ou GIF");
        }
        String nome = UUID.randomUUID() + "." + extensao;
        try (InputStream in = foto.getInputStream()) {
            Files.copy(in, diretorio.resolve(nome), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao gravar a foto", e);
        }
        return "/api/fotos/" + nome;
    }

    /** Devolve null se o arquivo nao existe ou se o nome tenta sair do diretorio (ex.: "../"). */
    public Resource carregar(String nome) {
        Path arquivo = diretorio.resolve(nome).normalize();
        if (!arquivo.startsWith(diretorio) || !Files.isRegularFile(arquivo)) {
            return null;
        }
        return new PathResource(arquivo);
    }
}
