package ufsm.csi.pilacoin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Report {
    private Date geradoEm;
    private String nomeUsuario;
    private boolean minerouPila;
    private boolean validouPila;
    private boolean minerouBloco;
    private boolean validouBloco;
    private boolean transferiuPila;
}
