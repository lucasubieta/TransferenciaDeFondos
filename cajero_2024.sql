create database cajero_2024;
use cajero_2024;
create table cuentas
(id_cuenta int not null primary key,
saldo double not null,
tipo_cuenta varchar(20) not null,
check (tipo_cuenta in ('AHORRO','CORRIENTE','JUVENIL', 'NOMINA'))
);
INSERT INTO CUENTAS VALUES(1000, 2000,'AHORRO');
INSERT INTO CUENTAS VALUES(2000, 12000,'CORRIENTE');
commit;
CREATE TABLE MOVIMIENTOS
(ID_MOVIMIENTO INT NOT NULL auto_increment PRIMARY KEY,
ID_CUENTA INT NOT NULL,
FECHA DATETIME,
CANTIDAD DOUBLE,
OPERACION VARCHAR(45),
FOREIGN KEY(ID_CUENTA) REFERENCES CUENTAS(ID_CUENTA)
);
-- drop user ucajero;
CREATE USER ucajero identified by 'ucajero';
grant all privileges on cajero_2024.* to ucajero;
