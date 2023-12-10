package ufsm.csi.pilacoin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.model.Bloco;
import ufsm.csi.pilacoin.model.PilaCoin;
import ufsm.csi.pilacoin.model.QueryRecebe;
import ufsm.csi.pilacoin.model.Transacao;
import ufsm.csi.pilacoin.repo.PilacoinRepository;
import ufsm.csi.pilacoin.repo.UsuarioRepository;
import ufsm.csi.pilacoin.util.Constants;


import java.util.Arrays;

@Service
public class QueryService {
    private final UsuarioRepository usuarioRepository;
    private final PilacoinRepository pilacoinRepository;

    public QueryService(UsuarioRepository usuarioRepository, PilacoinRepository pilacoinRepository) {
        this.usuarioRepository = usuarioRepository;
        this.pilacoinRepository = pilacoinRepository;
    }

    @SneakyThrows
    public void recebeQuery(String queryStr){
        System.out.println(queryStr);
        ObjectMapper objectMapper = new ObjectMapper();
        QueryRecebe query = objectMapper.readValue(queryStr, QueryRecebe.class);
        if (query.getPilasResult() != null){
            for (PilaCoin pila: query.getPilasResult()){
                if (Arrays.equals(pila.getChaveCriador(), Constants.PUBLIC_KEY.getEncoded()) && (pila.getTransacoes() == null || pila.getTransacoes().size() <= 1)){
                    pilacoinRepository.save(pila);
                } else if (pila.getTransacoes() != null && !pila.getTransacoes().isEmpty()){
                    Transacao transacao = pila.getTransacoes().get(pila.getTransacoes().size() - 1);
                    if (Arrays.equals(transacao.getChaveUsuarioDestino(), Constants.PUBLIC_KEY.getEncoded())){
                        pilacoinRepository.save(pila);
                    } else if (Arrays.equals(transacao.getChaveUsuarioOrigem(), Constants.PUBLIC_KEY.getEncoded())){
                        pilacoinRepository.delete(pila);
                    }
                }
            }
        } else if (query.getUsuariosResult() != null) {
            usuarioRepository.saveAll(query.getUsuariosResult());
        } else if (query.getBlocosResult() != null){
            for (Bloco bloco: query.getBlocosResult()){
                for (Transacao transacao: bloco.getTransacoes()){
                    if (transacao.getChaveUsuarioDestino() != null && Arrays.equals(transacao.getChaveUsuarioDestino(), Constants.PUBLIC_KEY.getEncoded())){
                        pilacoinRepository.save(PilaCoin.builder().nonce(transacao.getNoncePila()).status("VALIDO").build());
                    } else if (transacao.getChaveUsuarioOrigem() != null && Arrays.equals(transacao.getChaveUsuarioOrigem(), Constants.PUBLIC_KEY.getEncoded())){
                        pilacoinRepository.delete(PilaCoin.builder().nonce(transacao.getNoncePila()).status("VALIDO").build());
                    }
                }
            }
        }
    }
}

