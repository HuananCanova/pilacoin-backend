package ufsm.csi.pilacoin.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ufsm.csi.pilacoin.model.PilaCoin;

import java.util.List;

@Repository
public interface PilacoinRepository extends JpaRepository<PilaCoin, String> {
    List<PilaCoin> findByStatus(String status);
}

