package ufsm.csi.pilacoin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.model.*;
import ufsm.csi.pilacoin.util.Constants;
import ufsm.csi.pilacoin.util.PilaUtil;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Data
public class RabbitService {
    private final RabbitTemplate rabbitTemplate;
    private Bloco currentBlock;
    private final QueryService queryService;


    public RabbitService(RabbitTemplate rabbitTemplate, QueryService queryService) {
        this.rabbitTemplate = rabbitTemplate;
        this.queryService = queryService;
    }

    @SneakyThrows
    @RabbitListener(queues = "pila-minerado")
    public void validatePila(String pilacoinStr) {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        BigInteger hash = new BigInteger(md.digest(pilacoinStr.getBytes(StandardCharsets.UTF_8))).abs();
        ObjectReader objectReader = new ObjectMapper().reader();
        PilaCoin pilaCoin = objectReader.readValue(pilacoinStr, PilaCoin.class);
        StringBuilder msg = new StringBuilder();
        if(!pilaCoin.getNomeCriador().equals(Constants.USERNAME)) {
            if (hash.compareTo(Constants.DIFICULDADE) < 0) {
                PilaUtil pilaUtil = new PilaUtil();
                msg.append("Pilacoin de ").append(pilaCoin.getNomeCriador()).append(" válido!");
                PilaCoinValido pilaCoinValido = PilaCoinValido.builder()
                        .nomeValidador(Constants.USERNAME)
                        .chavePublicaValidador(Constants.PUBLIC_KEY.getEncoded())
                        .assinaturaPilaCoin(pilaUtil.getAssinatura(pilacoinStr))
                        .pilaCoinJson(pilaCoin)
                        .build();
                String json = new ObjectMapper().writeValueAsString(pilaCoinValido);
                this.rabbitTemplate.convertAndSend("pila-validado", json);
            } else {
                this.rabbitTemplate.convertAndSend("pila-minerado", pilacoinStr);
                msg.append("Pilacoin de ").append(pilaCoin.getNomeCriador()).append(" invalido");
            }
        }
        System.out.println(msg);
    }

    @SneakyThrows
    @RabbitListener(queues = "bloco-minerado")
    public void validateBlock(@Payload String blocoStr) {
        ObjectMapper objectMapper = new ObjectMapper();
        Bloco bloco;
        try {
            bloco = objectMapper.readValue(blocoStr, Bloco.class);
            //CHECKPOINT 1
        } catch (JsonProcessingException e) {
            System.out.println("bloco formato ivnalido");
            return;
        }
        if (bloco.getNomeUsuarioMinerador() == null || bloco.getNomeUsuarioMinerador().equals(Constants.USERNAME)) {
            System.out.println(bloco.getNomeUsuarioMinerador()  );
            System.out.println("Ignora meu bloco\n" + "XXXXXXXXXX".repeat(4));
            rabbitTemplate.convertAndSend("bloco-minerado", blocoStr);
            return;
        }
        System.out.println("Validando bloco mienrado pelo(a): " + bloco.getNomeUsuarioMinerador());
        BigInteger hash;
        try {
            hash = PilaUtil.geraHash(blocoStr);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        System.out.println(blocoStr);
        System.out.println(hash);
        System.out.println(Constants.DIFICULDADE);
        System.out.println("Numero do bloco: " + bloco.getNumeroBloco());
        if (hash.compareTo(Constants.DIFICULDADE) < 0) {
            System.out.println("Valido!");
            BlocoValido valido = BlocoValido.builder().assinaturaBloco(PilaUtil.geraAssinatura(bloco))
                    .bloco(bloco).chavePublicaValidador(Constants.PUBLIC_KEY.getEncoded())
                    .nomeValidador(Constants.USERNAME).build();
            try {
                ObjectWriter objectWriter = objectMapper.writer();
                System.out.println(objectWriter.writeValueAsString(valido));
                System.out.println(objectMapper.writeValueAsString(valido));
                rabbitTemplate.convertAndSend("bloco-validado", objectMapper.writeValueAsString(valido));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Bloco invalido");
            rabbitTemplate.convertAndSend("bloco-minerado", blocoStr);
        }
    }


    @RabbitListener(queues = "huanan_canova-query")
    public void resultadoQuery(@Payload String resultado){
        queryService.recebeQuery(resultado);
    }

    @SneakyThrows
    @RabbitListener(queues = "report")
    public void getReport(@Payload String report) {
        ObjectReader or = new ObjectMapper().reader();
        List<Report> reports = List.of(or.readValue(report, Report[].class));
        Optional<Report> myReport = reports.stream()
                .filter(r  -> r.getNomeUsuario() != null && r.getNomeUsuario().equals(Constants.USERNAME))
                .findFirst();
        System.out.println(myReport);
    }

    @RabbitListener(queues = "huanan_canova")
    public void myQueue(String msg) {
        System.out.println(msg);
    }

    @SneakyThrows
    @RabbitListener(queues = "descobre-bloco")
    public void getBlock(@Payload String blockStr) {
        Bloco bloco = new ObjectMapper().readValue(blockStr, Bloco.class);
        if(!bloco.equals(currentBlock)) {
            this.currentBlock = bloco;
        }
    }

    @SneakyThrows
    public ResponseEntity<String> mineBlock() {
        byte[] byteArray = new byte[256/8];
        Random random = new Random();
        ObjectMapper om = new ObjectMapper();
        random.nextBytes(byteArray);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String json = om.writeValueAsString(this.currentBlock);
        BigInteger hash = new BigInteger(md.digest(json.getBytes(StandardCharsets.UTF_8))).abs();
        if(this.currentBlock != null) {
            while (hash.compareTo(Constants.DIFICULDADE) > 0) {
                random.nextBytes(byteArray);
                BigInteger nonce = new BigInteger(md.digest(byteArray)).abs();
                this.currentBlock.setNonce(nonce.toString());
                json = om.writeValueAsString(this.currentBlock);
                hash = new BigInteger(md.digest(json.getBytes(StandardCharsets.UTF_8))).abs();
            }
            System.out.println("Bloco de " + this.currentBlock.getNomeUsuarioMinerador() + " válido!");
            this.rabbitTemplate.convertAndSend("bloco-minerado", json);
            return ResponseEntity.ok(om.writerWithDefaultPrettyPrinter().writeValueAsString(this.currentBlock));
        }
        return ResponseEntity.ok("Bloco ainda não recebido para mineração");
    }

}
