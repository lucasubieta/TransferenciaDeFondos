package cajeroweb.modelo.entidades;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data


@Entity
@Table(name="cuentas")
public class Cuenta implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="id_cuenta")
	private int idCuenta;
	private double saldo;
	@Column(name="tipo_cuenta")
	private String tipoCuenta;
	
	

}
