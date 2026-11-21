package br.pucminas.moedaestudantil.service;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.Foto;
import br.pucminas.moedaestudantil.repository.FotoRepository;

/**
 * Componente "Armazenamento de Fotos" do diagrama de componentes: guarda as fotos das vantagens no banco
 * com nome aleatorio gerado pelo sistema e devolve a URL publica.
 * Ate a Release 1 as fotos ficavam em disco; na nuvem (Render gratuito) o disco e apagado a cada reinicio,
 * entao passaram para a tabela "foto" (Lab04S02). A interface publica nao mudou.
 */
@Component
public class FotoStorage {

    private static final Map<String, String> EXTENSOES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif");

    private final FotoRepository fotoRepository;

    public FotoStorage(FotoRepository fotoRepository) {
        this.fotoRepository = fotoRepository;
    }

    /** Grava na mesma transacao da vantagem: se o cadastro falhar, a foto tambem nao fica. */
    @Transactional
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
        try {
            fotoRepository.save(new Foto(nome, foto.getBytes()));
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler a foto enviada", e);
        }
        return "/api/fotos/" + nome;
    }

    /** Devolve null se nao ha foto com esse nome. */
    @Transactional(readOnly = true)
    public Resource carregar(String nome) {
        return fotoRepository.findById(nome)
                .<Resource>map(f -> new ByteArrayResource(f.getDados()))
                .orElse(null);
    }
}
