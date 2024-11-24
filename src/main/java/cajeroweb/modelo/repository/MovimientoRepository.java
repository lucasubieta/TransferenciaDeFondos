package cajeroweb.modelo.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import cajeroweb.modelo.entidades.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Integer> {
    List<Movimiento> findByCuentaIdCuenta(int idCuenta);
}
//No se crea un dao específico y tiro del JpaRepository ya que no requiero de metodos especificos.
