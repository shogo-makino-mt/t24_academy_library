package jp.co.metateam.library.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.RentalManage;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
    List<RentalManage> findAll();

    Optional<RentalManage> findById(Long id);

    @Query("SELECT COUNT(rm) FROM RentalManage rm " +
            " WHERE rm.stock.id = ?1 " +
            " AND (rm.status = 0 OR rm.status = 1) ")
    Long countByStockIdAndStatusIn(String stockId);

    @Query("SELECT COUNT(rm) FROM RentalManage rm " +
            " WHERE rm.stock.id = ?1 " +
            " AND rm.status IN (0, 1) " +
            " AND (rm.expectedReturnOn < ?2 OR ?3 < rm.expectedRentalOn)")
    Long countByStatusAndExpectedReturnBefore(String stockId,
            Date expectedRentalOn,
            Date expectedReturnlOn);

    @Query("SELECT COUNT(rm) FROM RentalManage rm " +
            " WHERE rm.stock.id = ?2 " +
            " AND (rm.status = 0 OR rm.status = 1) " +
            " AND rm.id <> ?1")
    Long countByStatusAndNotId(Long rentalId, String stockId);

    @Query("SELECT COUNT(rm) FROM RentalManage rm " +
            " WHERE rm.stock.id = ?2 " +
            " AND rm.status IN (0, 1) " +
            " AND rm.id <> ?1 " +
            " AND (rm.expectedReturnOn < ?3 OR ?4 < rm.expectedRentalOn)")
    Long countByStatusAndExpectedReturnBeforeAndNotId(Long Id,
            String stockId,
            Date expectedRentalOn,
            Date expectedReturnlOn);

}
