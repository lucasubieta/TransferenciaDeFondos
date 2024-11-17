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
import cajeroweb.modelo.repository.CuentaRepository;
import cajeroweb.modelo.repository.MovimientoRepository;
import jakarta.servlet.http.HttpSession;

@Controller
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


    @PostMapping("/cuenta")
    public String consultarCuenta(@RequestParam int idCuenta, Model model, HttpSession sesion) {
        Cuenta cuenta = cuentaRepository.findById(idCuenta).orElse(null);
        if (cuenta == null) {
            model.addAttribute("error", "Cuenta no existe");
            return "index";
        }
        model.addAttribute("cuenta", cuenta);
        sesion.setAttribute("cuenta", cuenta);

        return "menu";
    }


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
	        }
        } else {
            model.addAttribute("error", "Ingresar un número positivo");
            model.addAttribute("cuenta", cuenta);
            return "menu"; 
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


    @GetMapping("/movimientos/{idCuenta}")
    public String verMovimientos(@PathVariable int idCuenta, Model model) {
        List<Movimiento> movimientos = movimientoRepository.findByCuentaIdCuenta(idCuenta);
        Cuenta cuenta = cuentaRepository.findById(idCuenta).orElse(null);
        model.addAttribute("movimientos", movimientos);
        model.addAttribute("cuenta", cuenta);
        return "movimientos";
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
