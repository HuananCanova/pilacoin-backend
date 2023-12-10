package ufsm.csi.pilacoin.model;

import lombok.Data;

import java.util.Date;

@Data
public class Dificuldade {
    String dificuldade;
    Date inicio;
    Date validadeFinal;
}
