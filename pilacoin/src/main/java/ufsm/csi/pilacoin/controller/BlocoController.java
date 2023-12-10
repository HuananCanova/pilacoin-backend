package ufsm.csi.pilacoin.controller;

import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ufsm.csi.pilacoin.model.Bloco;
import ufsm.csi.pilacoin.service.RabbitService;


@RestController
@RequestMapping("/bloco")
public class BlocoController {
    private final RabbitService rabbitService;
    private Bloco currentBlock;

    public BlocoController(RabbitService rabbitService) {
        this.rabbitService = rabbitService;
        this.currentBlock = rabbitService.getCurrentBlock();
    }

    @SneakyThrows
    @GetMapping("/minerar")
    public ResponseEntity<String> mine() {
        return this.rabbitService.mineBlock();
    }
}
