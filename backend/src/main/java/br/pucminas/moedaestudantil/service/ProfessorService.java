package br.pucminas.moedaestudantil.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import br.pucminas.moedaestudantil.repository.ProfessorRepository;

/** Professores sao pre-cadastrados (RN03): aqui so ha consultas. */
@Service
public class ProfessorService {

    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;

    public ProfessorService(ProfessorRepository professorRepository, AlunoRepository alunoRepository) {
        this.professorRepository = professorRepository;
        this.alunoRepository = alunoRepository;
    }

    @Transactional(readOnly = true)
    public Professor buscarPorId(Long id) {
        return professorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor", id));
    }

    /** Alunos que o professor pode reconhecer: os da instituicao da qual ele faz parte. */
    @Transactional(readOnly = true)
    public List<Aluno> listarAlunos(Long professorId) {
        return alunoRepository.findByInstituicaoIdOrderByNomeAsc(buscarPorId(professorId).getInstituicao().getId());
    }
}
