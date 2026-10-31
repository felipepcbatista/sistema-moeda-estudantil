package br.pucminas.moedaestudantil.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.moedaestudantil.repository.VantagemRepository;

/**
 * Regras de posse usadas nos @PreAuthorize dos controllers, ex.: "@acesso.ehOProprio(#id)".
 * Garante na API (e nao so na tela) que cada usuario so le e altera os proprios dados (US03, US05).
 */
@Component("acesso")
public class Acesso {

    private final VantagemRepository vantagemRepository;

    public Acesso(VantagemRepository vantagemRepository) {
        this.vantagemRepository = vantagemRepository;
    }

    /** O id da rota e o do usuario logado (subject do token). */
    public boolean ehOProprio(Long id) {
        Long logado = idLogado();
        return logado != null && logado.equals(id);
    }

    /** A vantagem pertence a empresa logada. Vantagem inexistente segue adiante para virar 404 no service. */
    @Transactional(readOnly = true)
    public boolean ehDonoDaVantagem(Long vantagemId) {
        return vantagemRepository.findById(vantagemId)
                .map(v -> v.getEmpresa().getId().equals(idLogado()))
                .orElse(true);
    }

    private static Long idLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
