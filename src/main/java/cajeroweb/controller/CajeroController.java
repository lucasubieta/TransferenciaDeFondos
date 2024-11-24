package cajeroweb.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import cajeroweb.modelo.entidades.Cuenta;
import cajeroweb.modelo.entidades.Movimiento;
import cajeroweb.modelo.entidades.Transferencia;
import cajeroweb.modelo.repository.CuentaRepository;
import cajeroweb.modelo.repository.MovimientoRepository;
import jakarta.servlet.http.HttpSession;




@Controller
// He utilizado un Controller para todas las clases porque en la actividad2 lo realice de esta manera,
// La proxima construire los metodos en las clases correspondientes

//Escucha el directorio raiz
@RequestMapping("/")
public class CajeroController {

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;


    @GetMapping
    public String inicio() {
        return "index";
    }

    
    
//Recibe los parametros del formulario
    @PostMapping("/cuenta")
    public String consultarCuenta(@RequestParam int idCuenta, Model model, HttpSession sesion) {
        Cuenta cuenta = cuentaRepository.findById(idCuenta).orElse(null);
        if (cuenta == null) {
            model.addAttribute("error", "Cuenta no existe");
            return "index";
        }
        //los model los utilizamos para almacenar variables entre hilos
        model.addAttribute("cuenta", cuenta);
        sesion.setAttribute("cuenta", cuenta);

        return "menu";
        
    }

//Recibe los parametros del formulario
    @PostMapping("/operacion")
    public String realizarOperacion(
            @RequestParam int idCuenta,
            @RequestParam double cantidad,
            @RequestParam String tipoOperacion,
            Model model) {

        Cuenta cuenta = cuentaRepository.findById(idCuenta).orElse(null);

        
        if (cuenta == null) {
            model.addAttribute("error", "La cuenta ingresada no existe");
            return "index";
        }
        
//Valido operacion
        if (cantidad > 0) {
	        if ("Extraccion".equals(tipoOperacion)) {
	            if (cuenta.getSaldo() < cantidad ) {
	                model.addAttribute("error", "Saldo insuficiente");
	                model.addAttribute("cuenta", cuenta);
	                return "menu"; 
	            } else {
	                cuenta.setSaldo(cuenta.getSaldo() - cantidad);
	            }
	        } else if ("Ingreso".equals(tipoOperacion)) {
	            cuenta.setSaldo(cuenta.getSaldo() + cantidad);
	        } else if ("Transferir".equals(tipoOperacion)) {
	        	Transferencia transferencia = new Transferencia();
	        	transferencia.setCantidad(cantidad);
	            model.addAttribute("cuenta", cuenta);
	            model.addAttribute("transferencia", transferencia);
	            return "transferencia"; 
	        }
        } else {
            model.addAttribute("error", "Ingresar un número positivo");
            model.addAttribute("cuenta", cuenta);
            return "index"; 
        }
	
	        Movimiento movimiento = new Movimiento();
	        movimiento.setCuenta(cuenta);
	        movimiento.setFecha(LocalDateTime.now());
	        movimiento.setCantidad(cantidad);
	        movimiento.setOperacion(tipoOperacion);
	        movimientoRepository.save(movimiento);
	
	        cuentaRepository.save(cuenta);

	        model.addAttribute("cuenta", cuenta);
	        return "menu"; 
    }

//Escucha la ruta y recibe los parametros

    @GetMapping("/movimientos/{idCuenta}")
    public String verMovimientos(@PathVariable int idCuenta, Model model) {
        List<Movimiento> movimientos = movimientoRepository.findByCuentaIdCuenta(idCuenta);
        Cuenta cuenta = cuentaRepository.findById(idCuenta).orElse(null);
        model.addAttribute("movimientos", movimientos);
        model.addAttribute("cuenta", cuenta);
        return "movimientos";
    }
    
    

//RECIBE PARAMETROS DEL FORM TRANSFERENCIA
    @PostMapping("/transferir")
    public String transferir(
            @RequestParam int cuentaDestino,
            @RequestParam double cantidad,
            HttpSession sesion,
            Model model) {

//VAlida cuentas
        Cuenta cuentaOrigen = (Cuenta) sesion.getAttribute("cuenta");
        if (cuentaOrigen == null) {
            model.addAttribute("error", "No hay una cuenta activa. Por favor, inicia sesión.");
            return "index";
        }

        Cuenta cuentaDestinoObj = cuentaRepository.findById(cuentaDestino).orElse(null);
        if (cuentaDestinoObj == null) {
            model.addAttribute("error", "La cuenta destino no existe.");
            model.addAttribute("cuenta", cuentaOrigen);
            return "transferencia";
        }

//Instancia cuentas y aplica cambios
        if (cantidad <= 0 ) {
            model.addAttribute("error", "El importe debe ser mayor a cero.");
            model.addAttribute("cuenta", cuentaOrigen);
            return "transferencia";
        } else if (cuentaOrigen.getSaldo() < cantidad) {
            model.addAttribute("error", "Saldo insuficiente, cancelada la operación");
            model.addAttribute("cuenta", cuentaOrigen);
            return "transferencia";
        }
        
        cuentaOrigen.setSaldo(cuentaOrigen.getSaldo() - cantidad); 
        cuentaDestinoObj.setSaldo(cuentaDestinoObj.getSaldo() + cantidad);
        cuentaRepository.save(cuentaOrigen);
        cuentaRepository.save(cuentaDestinoObj);

//Instancia movimiento y aplica cambios
        Movimiento movimientoOrigen = new Movimiento();
        movimientoOrigen.setCuenta(cuentaOrigen);
        movimientoOrigen.setFecha(LocalDateTime.now());
        movimientoOrigen.setCantidad(-cantidad); 
        movimientoOrigen.setOperacion("Extracto por transferencia a " + cuentaDestino);

        Movimiento movimientoDestino = new Movimiento();
        movimientoDestino.setCuenta(cuentaDestinoObj);
        movimientoDestino.setFecha(LocalDateTime.now());
        movimientoDestino.setCantidad(cantidad); 
        movimientoDestino.setOperacion("Ingreso por transferencia de " + cuentaOrigen.getIdCuenta());

//crea los movimientos en bbdd
        movimientoRepository.save(movimientoOrigen);
        movimientoRepository.save(movimientoDestino);

//Muestra el "mensaje" de transferencia en manu principal
        model.addAttribute("mensaje", "Transferencia realizada con éxito.");
        
//Define nuevamente la cuenta para pasarla
        model.addAttribute("cuenta", cuentaOrigen);

        return "menu";
    }


    
	@GetMapping("/logout")
	public String cerrarSesion(HttpSession sesion) {
		sesion.removeAttribute("cuenta");
		sesion.invalidate();
		return "forward:/";
	}
    
	
	@GetMapping("/volver")
	public String volver(HttpSession sesion, Model model) {
	    Cuenta cuenta = (Cuenta) sesion.getAttribute("cuenta");	    
	    model.addAttribute("cuenta", cuenta); 
	    return "menu"; 
	}


    
    
}
