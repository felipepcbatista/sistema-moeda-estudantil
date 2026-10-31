package br.pucminas.moedaestudantil.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.pucminas.moedaestudantil.dto.VantagemRequest;
import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.model.Vantagem;
import br.pucminas.moedaestudantil.repository.EmpresaParceiraRepository;
import br.pucminas.moedaestudantil.repository.VantagemRepository;

/** US06 (empresa gerencia vantagens) e US09 (aluno consulta vantagens ativas). */
@Service
public class VantagemService {

    private final VantagemRepository vantagemRepository;
    private final EmpresaParceiraRepository empresaRepository;
    private final FotoStorage fotoStorage;

    public VantagemService(VantagemRepository vantagemRepository, EmpresaParceiraRepository empresaRepository,
                           FotoStorage fotoStorage) {
        this.vantagemRepository = vantagemRepository;
        this.empresaRepository = empresaRepository;
        this.fotoStorage = fotoStorage;
    }

    /** RN08: nome, descricao, foto e custo sao obrigatorios. */
    @Transactional
    public Vantagem cadastrar(Long empresaId, VantagemRequest dados, MultipartFile foto) {
        EmpresaParceira empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa parceira", empresaId));
        Vantagem vantagem = new Vantagem();
        aplicar(vantagem, dados);
        vantagem.setFotoUrl(fotoStorage.salvar(foto));
        vantagem.setEmpresa(empresa);
        return vantagemRepository.save(vantagem);
    }

    /** Foto opcional na edicao: sem arquivo novo, mantem a atual. */
    @Transactional
    public Vantagem atualizar(Long id, VantagemRequest dados, MultipartFile foto) {
        Vantagem vantagem = buscarPorId(id);
        aplicar(vantagem, dados);
        if (foto != null && !foto.isEmpty()) {
            vantagem.setFotoUrl(fotoStorage.salvar(foto));
        }
        return vantagem;
    }

    /** Desativar em vez de excluir: resgates anteriores continuam apontando para a vantagem (US06). */
    @Transactional
    public Vantagem desativar(Long id) {
        Vantagem vantagem = buscarPorId(id);
        vantagem.setAtiva(false);
        return vantagem;
    }

    @Transactional
    public Vantagem reativar(Long id) {
        Vantagem vantagem = buscarPorId(id);
        vantagem.setAtiva(true);
        return vantagem;
    }

    @Transactional(readOnly = true)
    public List<Vantagem> listarAtivas() {
        return vantagemRepository.findByAtivaTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<Vantagem> listarDaEmpresa(Long empresaId) {
        return vantagemRepository.findByEmpresaIdOrderByNomeAsc(empresaId);
    }

    @Transactional(readOnly = true)
    public Vantagem buscarPorId(Long id) {
        return vantagemRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vantagem", id));
    }

    private static void aplicar(Vantagem vantagem, VantagemRequest dados) {
        vantagem.setNome(dados.nome().trim());
        vantagem.setDescricao(dados.descricao().trim());
        vantagem.setCustoMoedas(dados.custoMoedas());
    }
}
