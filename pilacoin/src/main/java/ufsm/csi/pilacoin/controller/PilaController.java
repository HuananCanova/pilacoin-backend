package ufsm.csi.pilacoin.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ufsm.csi.pilacoin.model.PilaCoin;
import ufsm.csi.pilacoin.service.RabbitService;
import ufsm.csi.pilacoin.util.Constants;
import ufsm.csi.pilacoin.util.PilaUtil;


import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

@RestController
@RequestMapping("/pilacoin")
public class PilaController {
    private final RabbitService rabbitService;

    public PilaController(RabbitService rabbitService) {
        this.rabbitService = rabbitService;
    }

    @GetMapping(value = "/minerar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getPila() throws JsonProcessingException, NoSuchAlgorithmException {
        Constants.initializeKeys();
        PilaUtil pilaUtil = new PilaUtil();
        ObjectMapper om = new ObjectMapper();
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        boolean loop = true;
        PilaCoin pj = createPilaCoin();
        BigInteger hash;
        int tentativa = 0;

        while (loop) {
            tentativa++;
            pj.setNonce(pilaUtil.geraNonce());
            hash = new BigInteger(md.digest(om.writeValueAsString(pj).getBytes(StandardCharsets.UTF_8))).abs();

            if (hash.compareTo(Constants.DIFICULDADE) < 0) {
                loop = false;
            }
        }

        // Validar a assinatura
        //validateSignature(pj);

        System.out.println("-=+=-=+=-=+=".repeat(4));
        System.out.println(tentativa + " tentativas");
        System.out.println("-=+=-=+=-=+=".repeat(4));
        String json = om.writeValueAsString(pj);
        this.rabbitService.getRabbitTemplate().convertAndSend("pila-minerado", json);
        String formattedJson = om.writerWithDefaultPrettyPrinter().writeValueAsString(pj);
        return ResponseEntity.ok(formattedJson);
    }

    private void validateSignature(PilaCoin pj) {
        byte[] assinatura = new PilaUtil().getAssinatura(pj);
        String assinaturaStr = Base64.getEncoder().encodeToString(assinatura);

        // Aqui você deve comparar a assinatura gerada com a assinatura existente em pj
        // Supondo que PilaCoin tenha um método getAssinatura()
        String msg = assinaturaStr.equals(assinatura) ? "Assinatura válida! :)" : "Assinatura inválida! :(";
        System.out.println(msg);
    }

    private PilaCoin createPilaCoin() {
        return PilaCoin.builder()
                .dataCriacao(new Date())
                .chaveCriador(Constants.PUBLIC_KEY.toString().getBytes())
                .nomeCriador(Constants.USERNAME)
                .build();
    }

}
